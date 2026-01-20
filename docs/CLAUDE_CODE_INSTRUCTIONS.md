# Claude Code 実装依頼書

## Phase 1: プロジェクト構築と認証機能

### 📋 依頼内容

以下の順序で実装をお願いします：

1. **プロジェクト構築**（backend、frontend）
2. **認証機能**（ユーザー登録・ログイン・プロフィール管理）

---

## 🎯 依頼文（Claude Codeにコピー&ペースト）

```
# プロジェクト構築と認証機能の実装依頼

## 📚 必読ドキュメント

実装前に以下のドキュメントを必ず確認してください：

1. **docs/README_FOR_CLAUDE_CODE.md** - クイックスタートガイド
2. **docs/IMPLEMENTATION_GUIDE.md** - 実装ガイド（特に「Phase 1: 基盤機能」セクション）
3. **CODING_STANDARDS.md** - コーディング規約（必須）
4. **docs/openapi.yaml** - API仕様（Users APIセクション）
5. **docs/DATABASE_DESIGN.md** - データベース設計（Usersテーブル）

## 🚀 実装依頼

### Step 1: プロジェクト構築

#### バックエンド（Java + Spring Boot）

以下の構成でプロジェクトを作成してください：

**ディレクトリ構造**:
```
backend/
├── src/main/java/com/cookingapp/
│   ├── presentation/          # Controllers, DTOs
│   │   ├── controller/
│   │   ├── dto/
│   │   └── exception/
│   ├── application/           # Use Cases
│   │   └── usecase/
│   ├── domain/                # Entities, Repository Interfaces
│   │   ├── entity/
│   │   └── repository/
│   └── infrastructure/        # Repository Implementations
│       ├── repository/
│       ├── config/
│       └── service/
├── src/main/resources/
│   └── application.yml
├── build.gradle
└── settings.gradle
```

**build.gradle の依存関係**:
- Spring Boot 3.x
- Spring Web
- AWS SDK for Java 2.x (DynamoDB, Cognito, S3)
- Lombok（@Builder, @Getter, @AllArgsConstructorのみ使用）
- Validation API

**重要な設定**:
- Java 17
- Clean Architecture構成
- application.ymlでDynamoDBテーブル名を設定可能に

#### フロントエンド（TypeScript + React）

以下の構成でプロジェクトを作成してください：

**ディレクトリ構造**:
```
frontend/
├── src/
│   ├── components/            # Reactコンポーネント
│   │   ├── auth/
│   │   ├── common/
│   │   └── layout/
│   ├── store/                 # Redux Store
│   │   ├── slices/
│   │   └── store.ts
│   ├── utils/                 # ユーティリティ
│   │   ├── apiClient.ts       # API呼び出しヘルパー
│   │   └── auth.ts
│   ├── types/                 # TypeScript型定義
│   ├── App.tsx
│   └── main.tsx
├── package.json
├── tsconfig.json
├── vite.config.ts
└── tailwind.config.js
```

**package.json の依存関係**:
- React 18.x
- TypeScript 5.x
- React Router 6.x
- Redux Toolkit
- Tailwind CSS
- Vite

**重要な設定**:
- ESLint設定（prefer-const, no-explicit-any）
- Prettier設定（singleQuote: true）

### Step 2: 認証機能の実装

#### 実装するAPI（docs/openapi.yaml参照）

1. **POST /api/users/register** - ユーザー登録
2. **POST /api/users/confirm** - メール確認
3. **POST /api/users/resend-code** - 確認コード再送信
4. **POST /api/users/login** - ログイン
5. **GET /api/users/profile/{userId}** - プロフィール取得
6. **PUT /api/users/profile/{userId}** - プロフィール更新
7. **DELETE /api/users/account/{userId}** - アカウント削除
8. **POST /api/users/profile/image** - プロフィール画像アップロード

#### バックエンド実装の流れ

1. **Entity作成** (domain/entity/User.java)
   - UserId, Email, Nickname, DisplayName, ProfileImageUrl等

2. **Repository Interface作成** (domain/repository/UserRepository.java)
   - save(), findById(), findByEmail(), delete()

3. **Repository Implementation作成** (infrastructure/repository/DynamoDbUserRepository.java)
   - DynamoDBクライアントを使用
   - @Valueでテーブル名を注入
   - 個別importを使用（ワイルドカード禁止）

4. **Cognito Service作成** (infrastructure/service/CognitoService.java)
   - signUp(), confirmSignUp(), resendConfirmationCode()
   - initiateAuth(), getUser()

5. **S3 Service作成** (infrastructure/service/S3Service.java)
   - uploadProfileImage()

6. **UseCase作成** (application/usecase/)
   - RegisterUserUseCase
   - ConfirmUserUseCase
   - LoginUserUseCase
   - GetUserProfileUseCase
   - UpdateUserProfileUseCase
   - DeleteUserAccountUseCase
   - UploadProfileImageUseCase

7. **Controller作成** (presentation/controller/UserController.java)
   - 各エンドポイントの実装
   - @Valid でバリデーション

8. **DTO作成** (presentation/dto/)
   - RegisterUserRequest, LoginUserRequest, UserResponse等

9. **Exception Handler作成** (presentation/exception/GlobalExceptionHandler.java)
   - @RestControllerAdvice
   - 適切なHTTPステータスコード

#### フロントエンド実装の流れ

1. **API Client作成** (utils/apiClient.ts)
   - apiGet(), apiPost(), apiPut(), apiDelete()
   - エラーハンドリング
   - トークン管理

2. **型定義作成** (types/)
   - User.ts, AuthTypes.ts

3. **Redux Store作成** (store/)
   - userSlice.ts（currentUser, loading, error）
   - authSlice.ts（isAuthenticated, tokens）

4. **認証ユーティリティ作成** (utils/auth.ts)
   - saveTokens(), getAccessToken(), clearTokens()
   - isAuthenticated()

5. **コンポーネント作成** (components/auth/)
   - RegisterPage.tsx - ユーザー登録画面
   - ConfirmPage.tsx - メール確認画面
   - LoginPage.tsx - ログイン画面
   - ProfilePage.tsx - プロフィール画面
   - ProfileEditPage.tsx - プロフィール編集画面

6. **ルーティング設定** (App.tsx)
   - React Router設定
   - 認証ガード

## ⚠️ 必須のコーディング規約

### バックエンド（Java）

**❌ 禁止事項**:
- `@Slf4j` アノテーション
- `System.getProperty()`
- `System.out.println()`
- ワイルドカードimport (`import java.util.*`)
- `public` フィールド

**✅ 必須事項**:
```java
// Logger の直接定義
private static final Logger log = LoggerFactory.getLogger(MyClass.class);

// @Value でテーブル名注入
public DynamoDbUserRepository(
    DynamoDbClient dynamoDbClient,
    @Value("${aws.dynamodb.table.users:Users}") String tableName) {
    this.dynamoDbClient = dynamoDbClient;
    this.tableName = tableName;
}

// 個別import
import java.util.List;
import java.util.Map;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

// private final フィールド
private final DynamoDbClient dynamoDbClient;
private final String tableName;
```

### フロントエンド（TypeScript）

**❌ 禁止事項**:
- `axios` の使用
- `fetch` の直接使用
- `console.log()` （本番コード）
- `let` （再代入不要な場合）
- `any` 型

**✅ 必須事項**:
```typescript
// apiClient.ts のヘルパー関数を使用
import { apiGet, apiPost } from '../utils/apiClient';

export const getUser = async (userId: string): Promise<User> => {
  return apiGet<User>(`/api/users/profile/${userId}`);
};

// const 優先
const apiUrl = '/api/users';

// 適切な型定義
interface User {
  userId: string;
  email: string;
  nickname: string;
}
```

## 📝 実装完了後のチェックリスト

- [ ] ビルドが成功する
  - バックエンド: `./gradlew.bat build -x test`
  - フロントエンド: `npm run build`
- [ ] コーディング規約に準拠
- [ ] デバッグコード削除
- [ ] 未使用import削除
- [ ] ワイルドカードimport未使用
- [ ] `@Slf4j` 未使用
- [ ] `axios`/`fetch` 直接使用なし
- [ ] `private final` フィールド使用
- [ ] `const` 優先
- [ ] `any` 型未使用
- [ ] すべてのエンドポイントが実装済み
- [ ] エラーハンドリング実装済み
- [ ] バリデーション実装済み

## 🐛 トラブルシューティング

### DynamoDB接続エラー
- テーブル名が正しいか確認
- AWS認証情報を確認（環境変数またはAWS CLI設定）

### Cognito認証エラー
- User Pool IDとClient IDが正しいか確認
- パスワード要件（8文字以上、大文字・小文字・数字）を確認

### CORS エラー
- API GatewayのCORS設定を確認
- 開発環境ではプロキシ設定を使用

## 📚 参考情報

- **実装例**: docs/IMPLEMENTATION_GUIDE.md の「実装詳細」セクション
- **API仕様**: docs/openapi.yaml の Users API
- **データベース**: docs/DATABASE_DESIGN.md の Usersテーブル
- **コーディング規約**: CODING_STANDARDS.md

---

**質問があれば遠慮なく聞いてください！**
```

---

## 💡 依頼のポイント

### 1. 段階的に依頼
- まずプロジェクト構築
- 次に認証機能
- 一度に全部ではなく、確認しながら進める

### 2. 具体的な指示
- ディレクトリ構造を明示
- 実装する順序を明確に
- 禁止事項と必須事項を強調

### 3. ドキュメント参照
- 必読ドキュメントを明示
- 実装例の場所を指定
- トラブルシューティング情報を提供

### 4. チェックリスト
- 実装完了後の確認項目
- コーディング規約の遵守確認

---

## 🔄 次のステップ

Phase 1完了後、以下を順次依頼：

1. **Phase 2**: レシピ管理機能
2. **Phase 3**: スケジュール管理機能
3. **Phase 4**: 買い物リスト機能
4. **Phase 5**: 在庫管理機能
5. **Phase 6**: AIチャット機能
6. **Phase 7**: レビュー機能

各Phaseごとに同様の依頼書を作成できます。
