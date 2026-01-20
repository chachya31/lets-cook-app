# 実装ガイド（Claude Code向け）

## 概要

このドキュメントは、Claude Codeを使用して自炊支援・食費節約アプリケーションを実装するための包括的なガイドです。

## 📚 必読ドキュメント

実装前に以下のドキュメントを必ず確認してください：

1. **[要件定義書](./要件定義書_自炊支援アプリ.md)** - 機能要件と非機能要件
2. **[API仕様書](./API_SPECIFICATION.md)** - REST APIエンドポイント定義
3. **[OpenAPI仕様](./openapi.yaml)** - 詳細なAPI定義
4. **[データベース設計](./DATABASE_DESIGN.md)** - DynamoDBテーブル設計
5. **[コーディング規約](../CODING_STANDARDS.md)** - コーディングルール

---

## 🏗️ アーキテクチャ概要

### システム構成

```
┌─────────────┐
│   Browser   │
│  (React)    │
└──────┬──────┘
       │ HTTPS
       ▼
┌─────────────┐
│ CloudFront  │
│   + S3      │
└──────┬──────┘
       │
       ▼
┌─────────────┐     ┌─────────────┐
│   API GW    │────▶│   Lambda    │
│             │     │ (Java)      │
└─────────────┘     └──────┬──────┘
                           │
                    ┌──────┴──────┐
                    ▼             ▼
              ┌──────────┐  ┌──────────┐
              │ DynamoDB │  │ Cognito  │
              └──────────┘  └──────────┘
```

### バックエンド（Java + Spring Boot）


**Clean Architecture採用**:
```
presentation/    # Controllers, DTOs
application/     # Use Cases
domain/          # Entities, Repository Interfaces
infrastructure/  # Repository Implementations
```

**技術スタック**:
- Java 17
- Spring Boot 3.x
- AWS SDK for Java 2.x
- DynamoDB
- AWS Cognito

### フロントエンド（TypeScript + React）

**技術スタック**:
- TypeScript 5.x
- React 18.x
- React Router 6.x
- Redux Toolkit
- Tailwind CSS
- Vite

---

## 🚀 セットアップ手順

### 前提条件

- Java 17以上
- Node.js 18以上
- AWS CLI設定済み
- AWS Cognito User Pool作成済み

### バックエンドセットアップ

```bash
cd backend

# 環境変数設定
cp .env.example .env
# .envファイルを編集してAWS認証情報を設定

# ビルド
./gradlew.bat build -x test

# ローカル実行
./gradlew.bat bootRun
```

### フロントエンドセットアップ

```bash
cd frontend

# 依存関係インストール
npm install

# 環境変数設定
cp .env.example .env.dev
# .env.devファイルを編集してAPI URLを設定

# 開発サーバー起動
npm run dev
```

---

## 📝 実装の進め方

### Phase 1: 基盤機能

1. **ユーザー管理**
   - ユーザー登録・ログイン
   - プロフィール編集
   - 画像アップロード

2. **レシピ管理**
   - レシピ作成・編集・削除
   - レシピ検索
   - 画像アップロード

### Phase 2: コア機能

3. **スケジュール管理**
   - カレンダー表示
   - 予定・実績登録
   - サボり防止アラート

4. **買い物リスト**
   - アイテム追加・削除
   - チェック機能
   - 数量合算

### Phase 3: 拡張機能

5. **在庫管理**
   - 在庫登録・編集
   - 賞味期限管理

6. **AIチャット**
   - Gemini API連携
   - 会話履歴管理
   - レシピ推薦

7. **レビュー機能**
   - 星評価・コメント
   - 通報機能

---

## 🔧 実装詳細

### バックエンド実装

#### 1. Controller層

**場所**: `backend/src/main/java/com/cookingapp/presentation/controller/`

**例**: UserController.java
```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final RegisterUserUseCase registerUserUseCase;
    
    public UserController(RegisterUserUseCase registerUserUseCase) {
        this.registerUserUseCase = registerUserUseCase;
    }
    
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterUserRequest request) {
        log.info("User registration request: email={}", request.getEmail());
        // UseCase呼び出し
        return ResponseEntity.ok(registerUserUseCase.execute(request));
    }
}
```

**重要ポイント**:
- `@RestController` + `@RequestMapping` でエンドポイント定義
- コンストラクタインジェクション使用
- `Logger` を直接定義（`@Slf4j` 禁止）
- `@Valid` でバリデーション

#### 2. UseCase層

**場所**: `backend/src/main/java/com/cookingapp/application/usecase/`

**例**: RegisterUserUseCase.java
```java
@Service
public class RegisterUserUseCase {
    private static final Logger log = LoggerFactory.getLogger(RegisterUserUseCase.class);
    private final UserRepository userRepository;
    private final CognitoService cognitoService;
    
    public RegisterUserUseCase(UserRepository userRepository, CognitoService cognitoService) {
        this.userRepository = userRepository;
        this.cognitoService = cognitoService;
    }
    
    public UserResponse execute(RegisterUserRequest request) {
        // ビジネスロジック実装
        log.info("Registering user: email={}", request.getEmail());
        
        // Cognito登録
        String userId = cognitoService.signUp(request.getEmail(), request.getPassword());
        
        // DynamoDB保存
        User user = User.builder()
            .userId(userId)
            .email(request.getEmail())
            .nickname(request.getNickname())
            .build();
        userRepository.save(user);
        
        return UserResponse.from(user);
    }
}
```

#### 3. Repository層

**場所**: `backend/src/main/java/com/cookingapp/infrastructure/repository/`

**例**: DynamoDbUserRepository.java
```java
@Repository
public class DynamoDbUserRepository implements UserRepository {
    private static final Logger log = LoggerFactory.getLogger(DynamoDbUserRepository.class);
    private final DynamoDbClient dynamoDbClient;
    private final String tableName;
    
    public DynamoDbUserRepository(
        DynamoDbClient dynamoDbClient,
        @Value("${aws.dynamodb.table.users:Users}") String tableName) {
        this.dynamoDbClient = dynamoDbClient;
        this.tableName = tableName;
    }
    
    @Override
    public void save(User user) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("UserId", AttributeValue.builder().s(user.getUserId()).build());
        item.put("Email", AttributeValue.builder().s(user.getEmail()).build());
        // ...
        
        PutItemRequest request = PutItemRequest.builder()
            .tableName(tableName)
            .item(item)
            .build();
        
        dynamoDbClient.putItem(request);
        log.info("User saved: userId={}", user.getUserId());
    }
}
```

**重要ポイント**:
- `@Value` でテーブル名を注入（`System.getProperty()` 禁止）
- コンストラクタインジェクション
- ワイルドカードimport禁止（個別import使用）
- `private final` フィールド

#### 4. エラーハンドリング

**場所**: `backend/src/main/java/com/cookingapp/presentation/exception/`

**例**: GlobalExceptionHandler.java
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(Instant.now().toString())
            .status(404)
            .error("Not Found")
            .message(ex.getMessage())
            .build();
        return ResponseEntity.status(404).body(error);
    }
}
```

### フロントエンド実装

#### 1. API Client

**場所**: `frontend/src/utils/apiClient.ts`

**重要**: `axios` や `fetch` の直接使用は禁止。必ず `apiClient.ts` のヘルパー関数を使用。

```typescript
// ✅ 正しい使用方法
import { apiGet, apiPost } from '../utils/apiClient';

export const getUsers = async (): Promise<User[]> => {
  return apiGet<User[]>('/api/users');
};

export const createUser = async (data: CreateUserRequest): Promise<User> => {
  return apiPost<User>('/api/users', data);
};
```

#### 2. Redux Store

**場所**: `frontend/src/store/`

**例**: userSlice.ts
```typescript
import { createSlice, PayloadAction } from '@reduxjs/toolkit';

interface UserState {
  currentUser: User | null;
  loading: boolean;
  error: string | null;
}

const initialState: UserState = {
  currentUser: null,
  loading: false,
  error: null,
};

const userSlice = createSlice({
  name: 'user',
  initialState,
  reducers: {
    setUser: (state, action: PayloadAction<User>) => {
      state.currentUser = action.payload;
    },
    setLoading: (state, action: PayloadAction<boolean>) => {
      state.loading = action.payload;
    },
    setError: (state, action: PayloadAction<string>) => {
      state.error = action.payload;
    },
  },
});

export const { setUser, setLoading, setError } = userSlice.actions;
export default userSlice.reducer;
```

#### 3. コンポーネント設計

**シンプルな画面**: UI + ロジックを1ファイルに
```typescript
// LoginPage.tsx
const LoginPage: React.FC = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    // ログイン処理
  };
  
  return (
    <form onSubmit={handleSubmit}>
      {/* UI */}
    </form>
  );
};
```

**複雑な画面**: UI、ロジック、設定を分離
```typescript
// RecipeEditPage.tsx (UI)
const RecipeEditPage: React.FC = () => {
  const handlers = useRecipeEditHandlers();
  
  return (
    <form onSubmit={handlers.handleSubmit}>
      {/* UI */}
    </form>
  );
};

// useRecipeEditHandlers.ts (ロジック)
export const useRecipeEditHandlers = () => {
  const handleSubmit = async (e: React.FormEvent) => {
    // 処理
  };
  
  return { handleSubmit };
};
```

---

## 🔒 セキュリティ実装

### 認証フロー

1. **ユーザー登録**
   - Cognito SignUp
   - メール確認コード送信
   - 確認後にDynamoDBへユーザー情報保存

2. **ログイン**
   - Cognito InitiateAuth
   - トークン取得（Access, ID, Refresh）
   - トークンをlocalStorageに保存

3. **API認証**
   - リクエストヘッダーに `Authorization: Bearer {token}` を付与
   - Lambda Authorizerでトークン検証

### パスワード要件

- 8文字以上
- 大文字・小文字・数字を含む
- Cognitoのパスワードポリシーで強制

---

## 📊 データベース操作

### DynamoDB操作パターン

#### GetItem（単一アイテム取得）
```java
GetItemRequest request = GetItemRequest.builder()
    .tableName(tableName)
    .key(Map.of("UserId", AttributeValue.builder().s(userId).build()))
    .build();

GetItemResponse response = dynamoDbClient.getItem(request);
```

#### Query（範囲検索）
```java
QueryRequest request = QueryRequest.builder()
    .tableName(tableName)
    .keyConditionExpression("UserId = :userId AND #date BETWEEN :start AND :end")
    .expressionAttributeNames(Map.of("#date", "Date"))
    .expressionAttributeValues(Map.of(
        ":userId", AttributeValue.builder().s(userId).build(),
        ":start", AttributeValue.builder().s(startDate).build(),
        ":end", AttributeValue.builder().s(endDate).build()
    ))
    .build();

QueryResponse response = dynamoDbClient.query(request);
```

#### PutItem（保存）
```java
PutItemRequest request = PutItemRequest.builder()
    .tableName(tableName)
    .item(itemMap)
    .build();

dynamoDbClient.putItem(request);
```

#### UpdateItem（更新）
```java
UpdateItemRequest request = UpdateItemRequest.builder()
    .tableName(tableName)
    .key(keyMap)
    .updateExpression("SET #name = :name, UpdatedAt = :updatedAt")
    .expressionAttributeNames(Map.of("#name", "Name"))
    .expressionAttributeValues(Map.of(
        ":name", AttributeValue.builder().s(newName).build(),
        ":updatedAt", AttributeValue.builder().s(Instant.now().toString()).build()
    ))
    .build();

dynamoDbClient.updateItem(request);
```

---

## 🧪 テスト

### バックエンドテスト

**単体テスト**: JUnit 5
```java
@Test
void testRegisterUser() {
    // Given
    RegisterUserRequest request = new RegisterUserRequest("test@example.com", "password", "Test User");
    
    // When
    UserResponse response = registerUserUseCase.execute(request);
    
    // Then
    assertNotNull(response.getUserId());
    assertEquals("test@example.com", response.getEmail());
}
```

### フロントエンドテスト

**コンポーネントテスト**: Vitest + React Testing Library
```typescript
test('renders login form', () => {
  render(<LoginPage />);
  expect(screen.getByLabelText('Email')).toBeInTheDocument();
  expect(screen.getByLabelText('Password')).toBeInTheDocument();
});
```

---

## 📦 デプロイ

### バックエンドデプロイ

```bash
# ビルド
./gradlew.bat build

# Lambda関数としてデプロイ
aws lambda update-function-code \
  --function-name cooking-app-api \
  --zip-file fileb://build/libs/backend.jar
```

### フロントエンドデプロイ

```bash
# ビルド
npm run build

# S3へアップロード
aws s3 sync dist/ s3://cooking-app-frontend/

# CloudFrontキャッシュクリア
aws cloudfront create-invalidation \
  --distribution-id XXXXX \
  --paths "/*"
```

---

## 🐛 トラブルシューティング

### よくあるエラー

#### 1. DynamoDB接続エラー
```
ResourceNotFoundException: Requested resource not found
```
**解決策**: テーブル名が正しいか確認、AWS認証情報を確認

#### 2. Cognito認証エラー
```
NotAuthorizedException: Incorrect username or password
```
**解決策**: パスワード要件を確認、ユーザーが確認済みか確認

#### 3. CORS エラー
```
Access to fetch at 'https://api.example.com' from origin 'http://localhost:3000' has been blocked by CORS policy
```
**解決策**: API GatewayでCORS設定を確認

---

## 📚 参考リンク

- [AWS SDK for Java 2.x](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [React Documentation](https://react.dev/)
- [DynamoDB Best Practices](https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/best-practices.html)

---

## 📝 チェックリスト

実装完了前に以下を確認してください：

### コード品質
- [ ] コーディング規約に準拠
- [ ] ワイルドカードimport未使用
- [ ] `@Slf4j` 未使用（Logger直接定義）
- [ ] `System.getProperty()` 未使用（@Value使用）
- [ ] `axios`/`fetch` 直接使用なし（apiClient使用）
- [ ] `console.log()` 削除済み
- [ ] 未使用import削除済み
- [ ] `private final` フィールド使用
- [ ] `const` 優先（`let` は必要時のみ）
- [ ] `any` 型未使用

### 機能
- [ ] すべてのエンドポイントが実装済み
- [ ] エラーハンドリング実装済み
- [ ] バリデーション実装済み
- [ ] 認証・認可実装済み

### テスト
- [ ] 単体テスト実装済み
- [ ] ビルドが成功する

### ドキュメント
- [ ] API仕様書と実装が一致
- [ ] データベース設計と実装が一致

---

## 🎯 次のステップ

1. 基盤機能（ユーザー管理、レシピ管理）から実装開始
2. 各機能のテスト実装
3. コア機能（スケジュール、買い物リスト）の実装
4. 拡張機能（在庫、AIチャット、レビュー）の実装
5. 統合テスト
6. デプロイ

---

**実装時の注意**: 
- 必ず設計書とコーディング規約を確認してから実装
- 不明点があれば設計書を参照
- コミット前にチェックリストを確認
