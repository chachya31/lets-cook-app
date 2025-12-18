---
name: "lets-cook-best-practices"
displayName: "Let's Cook ベストプラクティス"
description: "自炊支援アプリのコーディング規約、セキュリティ、アーキテクチャパターンを網羅したベストプラクティスガイド。"
keywords: ["コーディング規約", "セキュリティ", "アーキテクチャ", "Java", "TypeScript", "React", "DynamoDB"]
author: "Let's Cook Development Team"
---

# Let's Cook ベストプラクティス

## 概要

このパワーは、自炊支援・食費節約アプリ「Let's Cook」の開発における包括的なベストプラクティスガイドです。コーディング規約、セキュリティ対策、アーキテクチャパターンを網羅し、高品質なコードを維持するための指針を提供します。

**対象者**: バックエンド（Java/Spring Boot）、フロントエンド（TypeScript/React）の開発者

**技術スタック**:
- バックエンド: Java 17, Spring Boot, Clean Architecture
- フロントエンド: TypeScript, React, Redux, React Router
- データベース: Amazon DynamoDB
- インフラ: AWS (Cognito, S3, CloudWatch)

## 基本原則

1. **セキュリティファースト** - 機密情報の保護を最優先
2. **Clean Architecture** - レイヤー間の依存関係を明確に
3. **型安全性** - TypeScriptの型システムを最大限活用
4. **テスタビリティ** - テストしやすい設計を心がける
5. **可読性** - 将来の自分とチームのために明確なコードを書く


## バックエンド（Java）ベストプラクティス

### 1. ロギング

#### ❌ 禁止事項

```java
// @Slf4jアノテーションの使用
@Slf4j
public class UserService {
    // コード生成ツールのセキュリティリスク
}

// System.out.println()の使用
System.out.println("Debug: " + value);
```

#### ✅ 推奨事項

```java
// Loggerの直接定義
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    
    public void processUser(String userId) {
        log.info("Processing user: userId={}", userId);
        log.debug("User details: {}", userDetails);
    }
}
```

**理由**:
- セキュリティ: コード生成ツールのリスク軽減
- 透明性: ロガーの定義が明示的
- 本番環境: CloudWatchでの適切なログ管理

#### ⚠️ 機密情報のログ出力禁止

```java
// ❌ 絶対にやってはいけない
log.info("Password: {}", password);
log.info("Credit card: {}", creditCard);
log.info("API Key: {}", apiKey);
log.info("Email: {}", email);  // 個人情報

// ✅ 正しい方法
log.info("User action: userId={}, action={}", userId, action);
log.info("Payment processed: last4={}", creditCard.getLast4Digits());
log.info("API call successful: endpoint={}", endpoint);
```


### 2. DynamoDBテーブル名管理

#### ❌ 禁止事項

```java
// System.getProperty()の使用
private String getTableName() {
    return System.getProperty("aws.dynamodb.table.users");
}
```

#### ✅ 推奨事項

```java
// @Value アノテーション + コンストラクタインジェクション
@Repository
public class UserRepository {
    private final DynamoDbClient dynamoDbClient;
    private final String tableName;
    
    public UserRepository(
        DynamoDbClient dynamoDbClient,
        @Value("${aws.dynamodb.table.users:Users}") String tableName) {
        this.dynamoDbClient = dynamoDbClient;
        this.tableName = tableName;
    }
}
```

**理由**:
- Springの設定管理と統合
- テストでのモック化が容易
- デフォルト値の設定が可能

### 3. Clean Architecture

#### レイヤー構成

```
presentation/    # Controllers, DTOs, Request/Response
application/     # Use Cases, Application Services
domain/          # Entities, Value Objects, Repository Interfaces
infrastructure/  # Repository Implementations, External APIs
```

#### 依存関係のルール

```java
// ✅ 正しい依存関係
// presentation → application → domain ← infrastructure

// ❌ 間違った依存関係
// domain → infrastructure (内側が外側に依存してはいけない)
```

#### 実装例

```java
// domain/repository/UserRepository.java (インターフェース)
public interface UserRepository {
    Optional<User> findById(String userId);
    void save(User user);
}

// infrastructure/repository/DynamoDbUserRepository.java (実装)
@Repository
public class DynamoDbUserRepository implements UserRepository {
    // DynamoDB固有の実装
}

// application/usecase/GetUserUseCase.java
@Service
public class GetUserUseCase {
    private final UserRepository userRepository;
    
    public GetUserUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    public User execute(String userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));
    }
}
```


### 4. Lombokの使用

#### ✅ 許可されているアノテーション

```java
@Builder
@Getter
@AllArgsConstructor
public class User {
    private String userId;
    private String email;
    private String nickname;
}
```

#### ❌ 禁止されているアノテーション

```java
@Slf4j  // セキュリティリスク
@RequiredArgsConstructor  // 使用しない場合
```

### 5. エラーハンドリング

#### カスタム例外の定義

```java
// domain/exception/UserNotFoundException.java
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String userId) {
        super("User not found: " + userId);
    }
}

// presentation/exception/GlobalExceptionHandler.java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(
        UserNotFoundException ex) {
        
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.NOT_FOUND.value())
            .error("Not Found")
            .message(ex.getMessage())
            .build();
            
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
}
```

### 6. バリデーション

```java
// presentation/dto/CreateUserRequest.java
public class CreateUserRequest {
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
    
    @NotBlank(message = "Nickname is required")
    @Size(min = 1, max = 50, message = "Nickname must be 1-50 characters")
    private String nickname;
}

// presentation/controller/UserController.java
@PostMapping("/register")
public ResponseEntity<UserResponse> register(
    @Valid @RequestBody CreateUserRequest request) {
    // バリデーションは自動的に実行される
}
```


## フロントエンド（TypeScript/React）ベストプラクティス

### 1. API呼び出し

#### ❌ 禁止事項

```typescript
// axiosの直接使用
import axios from 'axios';
const response = await axios.get('/api/users');

// fetchの直接使用
const response = await fetch('/api/users');
```

#### ✅ 推奨事項

```typescript
// utils/apiClient.ts のヘルパー関数を使用
import { apiGet, apiPost, apiPut, apiDelete } from '../utils/apiClient';

// services/userService.ts
export const getUsers = async (): Promise<User[]> => {
  return apiGet<User[]>('/api/users');
};

export const createUser = async (data: CreateUserRequest): Promise<User> => {
  return apiPost<User>('/api/users', data);
};
```

**理由**:
- エラーハンドリングの一元管理
- 認証トークンの自動付与
- コードの重複削減
- 型安全性の確保

### 2. エラーハンドリング

```typescript
// ✅ 正しいエラーハンドリング
export const fetchUserProfile = async (userId: string): Promise<User> => {
  try {
    const user = await apiGet<User>(`/api/users/profile/${userId}`);
    return user;
  } catch (error) {
    console.error('Failed to fetch user profile:', error);
    throw error; // 上位層で処理
  }
};

// Redux Thunkでのエラーハンドリング
export const fetchUserProfileThunk = createAsyncThunk(
  'user/fetchProfile',
  async (userId: string, { rejectWithValue }) => {
    try {
      return await fetchUserProfile(userId);
    } catch (error) {
      return rejectWithValue('ユーザー情報の取得に失敗しました');
    }
  }
);
```


### 3. コンポーネント設計

#### シンプルな画面（UI + ロジック統合）

```typescript
// pages/Login/LoginPage.tsx
export const LoginPage: React.FC = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const dispatch = useDispatch();
  
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await dispatch(loginThunk({ email, password })).unwrap();
      // ログイン成功処理
    } catch (error) {
      // エラー処理
    }
  };
  
  return (
    <form onSubmit={handleSubmit}>
      <input value={email} onChange={(e) => setEmail(e.target.value)} />
      <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} />
      <button type="submit">ログイン</button>
    </form>
  );
};
```

#### 複雑な画面（責任の分離）

```typescript
// pages/RecipeEdit/RecipeEditPage.tsx (表示のみ)
export const RecipeEditPage: React.FC = () => {
  const handlers = useRecipeEditHandlers();
  
  return (
    <div>
      <RecipeForm
        recipe={handlers.recipe}
        onTitleChange={handlers.handleTitleChange}
        onIngredientAdd={handlers.handleIngredientAdd}
        onSubmit={handlers.handleSubmit}
      />
    </div>
  );
};

// pages/RecipeEdit/useRecipeEditHandlers.ts (ロジック)
export const useRecipeEditHandlers = () => {
  const [recipe, setRecipe] = useState<Recipe>(initialRecipe);
  const dispatch = useDispatch();
  
  const handleTitleChange = (title: string) => {
    setRecipe(prev => ({ ...prev, title }));
  };
  
  const handleIngredientAdd = (ingredient: Ingredient) => {
    setRecipe(prev => ({
      ...prev,
      ingredients: [...prev.ingredients, ingredient]
    }));
  };
  
  const handleSubmit = async () => {
    await dispatch(updateRecipeThunk(recipe)).unwrap();
  };
  
  return {
    recipe,
    handleTitleChange,
    handleIngredientAdd,
    handleSubmit
  };
};

// pages/RecipeEdit/recipeEditConfig.ts (設定)
export const recipeEditConfig = {
  maxTitleLength: 100,
  maxSteps: 20,
  validationRules: {
    title: { required: true, maxLength: 100 },
    ingredients: { minItems: 1 }
  }
};
```

**責任の分離**:
- **Page.tsx**: UIの構造、propsの受け渡し
- **useHandlers.ts**: イベント処理、状態管理、副作用
- **Config.ts**: 設定値、バリデーションルール、定数


### 4. 型定義

#### ✅ 必須事項

```typescript
// types/user.ts
export interface User {
  userId: string;
  email: string;
  nickname: string;
  displayName: string;
  profileImageUrl?: string;
  preferredLanguage: 'ja' | 'ko';
  lastCookingDate?: string;
  createdAt: string;
}

export interface CreateUserRequest {
  email: string;
  password: string;
  nickname: string;
  preferredLanguage?: 'ja' | 'ko';
}

// services/userService.ts
export const getUser = async (userId: string): Promise<User> => {
  return apiGet<User>(`/api/users/profile/${userId}`);
};

export const createUser = async (data: CreateUserRequest): Promise<User> => {
  return apiPost<User>('/api/users/register', data);
};
```

#### ❌ 避けるべきパターン

```typescript
// any型の使用
const data: any = await apiGet('/api/users');

// 型定義なし
const getUser = async (userId) => {
  return apiGet(`/api/users/${userId}`);
};
```

### 5. デバッグコード

#### ❌ 本番コードに含めてはいけない

```typescript
console.log('Debug:', data);
console.error('Error:', error);
debugger;
```

#### ✅ 開発中のみ許可（コミット前に削除）

```typescript
// 開発中のデバッグ（コミット前に必ず削除）
console.log('TODO: Remove this debug log', data);
```

### 6. import文の整理

```typescript
// ✅ 推奨順序

// 1. 外部ライブラリ
import React, { useState, useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';

// 2. 内部モジュール（絶対パス）
import { apiGet, apiPost } from '../utils/apiClient';
import { User } from '../types/user';

// 3. 相対パス
import { LoginForm } from './LoginForm';
import { useLoginHandlers } from './useLoginHandlers';

// 4. スタイル
import './LoginPage.css';
```


## セキュリティベストプラクティス

### 1. 認証・認可

#### AWS Cognito統合

```java
// バックエンド: JWTトークン検証
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {
        
        String token = extractToken(request);
        if (token != null && validateToken(token)) {
            String userId = extractUserId(token);
            // 認証情報をセキュリティコンテキストに設定
        }
        filterChain.doFilter(request, response);
    }
}
```

```typescript
// フロントエンド: トークン管理
export const setAuthToken = (token: string): void => {
  localStorage.setItem('authToken', token);
};

export const getAuthToken = (): string | null => {
  return localStorage.getItem('authToken');
};

export const clearAuthToken = (): void => {
  localStorage.removeItem('authToken');
};

// apiClient.ts でトークンを自動付与
const token = getAuthToken();
if (token) {
  headers['Authorization'] = `Bearer ${token}`;
}
```

### 2. 機密情報の保護

#### ❌ 絶対にやってはいけない

```java
// ログに機密情報を出力
log.info("User password: {}", password);
log.info("API Key: {}", apiKey);
log.info("Credit card: {}", creditCard);

// ハードコードされた機密情報
private static final String API_KEY = "sk-abc123xyz";
```

```typescript
// コンソールに機密情報を出力
console.log('Password:', password);
console.log('Token:', authToken);
```

#### ✅ 正しい方法

```java
// 環境変数から取得
@Value("${api.key}")
private String apiKey;

// ログには識別子のみ
log.info("User authenticated: userId={}", userId);
log.info("Payment processed: transactionId={}", transactionId);
```

```typescript
// 環境変数から取得
const apiKey = import.meta.env.VITE_API_KEY;

// ログには識別子のみ
console.log('User logged in:', userId);
```

### 3. 入力バリデーション

#### バックエンド

```java
// SQLインジェクション対策（DynamoDBでは不要だが、原則として）
public class UserService {
    
    public User findByEmail(String email) {
        // バリデーション
        if (!isValidEmail(email)) {
            throw new InvalidEmailException("Invalid email format");
        }
        
        // サニタイズ
        String sanitizedEmail = sanitizeEmail(email);
        
        return userRepository.findByEmail(sanitizedEmail);
    }
    
    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
}
```

#### フロントエンド

```typescript
// XSS対策: ユーザー入力のサニタイズ
import DOMPurify from 'dompurify';

export const sanitizeInput = (input: string): string => {
  return DOMPurify.sanitize(input);
};

// 使用例
const handleCommentSubmit = (comment: string) => {
  const sanitizedComment = sanitizeInput(comment);
  await apiPost('/api/reviews', { comment: sanitizedComment });
};
```

### 4. CORS設定

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins("https://app.example.com")
            .allowedMethods("GET", "POST", "PUT", "DELETE")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600);
    }
}
```

### 5. ファイルアップロードのセキュリティ

```java
@Service
public class ImageUploadService {
    
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final Set<String> ALLOWED_TYPES = 
        Set.of("image/jpeg", "image/png");
    
    public String uploadImage(MultipartFile file) {
        // ファイルサイズチェック
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new FileSizeExceededException("File size exceeds 5MB");
        }
        
        // ファイルタイプチェック
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new InvalidFileTypeException("Only JPEG and PNG allowed");
        }
        
        // ファイル名のサニタイズ
        String sanitizedFilename = sanitizeFilename(file.getOriginalFilename());
        
        // S3にアップロード
        return s3Service.upload(file, sanitizedFilename);
    }
}
```


## アーキテクチャパターン

### 1. Clean Architecture（バックエンド）

#### レイヤー構造

```
src/main/java/com/example/letscook/
├── presentation/          # 外側のレイヤー
│   ├── controller/       # REST Controllers
│   ├── dto/              # Request/Response DTOs
│   └── exception/        # Exception Handlers
├── application/          # アプリケーション層
│   ├── usecase/          # Use Cases
│   └── service/          # Application Services
├── domain/               # 内側のレイヤー（ビジネスロジック）
│   ├── model/            # Entities, Value Objects
│   ├── repository/       # Repository Interfaces
│   └── exception/        # Domain Exceptions
└── infrastructure/       # 外側のレイヤー（技術詳細）
    ├── repository/       # Repository Implementations
    ├── config/           # Configuration
    └── external/         # External API Clients
```

#### 依存関係の方向

```
presentation → application → domain ← infrastructure
```

**重要**: 内側のレイヤー（domain）は外側のレイヤーに依存してはいけない

#### 実装例: ユーザー登録

```java
// domain/model/User.java (Entity)
public class User {
    private String userId;
    private String email;
    private String nickname;
    
    // ビジネスロジック
    public void updateProfile(String nickname) {
        if (nickname == null || nickname.length() > 50) {
            throw new InvalidNicknameException();
        }
        this.nickname = nickname;
    }
}

// domain/repository/UserRepository.java (Interface)
public interface UserRepository {
    Optional<User> findById(String userId);
    Optional<User> findByEmail(String email);
    void save(User user);
    void delete(String userId);
}

// application/usecase/RegisterUserUseCase.java
@Service
public class RegisterUserUseCase {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    public RegisterUserUseCase(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    public User execute(String email, String password, String nickname) {
        // ビジネスルールの検証
        if (userRepository.findByEmail(email).isPresent()) {
            throw new UserAlreadyExistsException(email);
        }
        
        // ユーザー作成
        User user = User.builder()
            .userId(UUID.randomUUID().toString())
            .email(email)
            .nickname(nickname)
            .createdAt(LocalDateTime.now())
            .build();
        
        userRepository.save(user);
        return user;
    }
}

// infrastructure/repository/DynamoDbUserRepository.java
@Repository
public class DynamoDbUserRepository implements UserRepository {
    private final DynamoDbClient dynamoDbClient;
    private final String tableName;
    
    @Override
    public void save(User user) {
        // DynamoDB固有の実装
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("UserId", AttributeValue.builder().s(user.getUserId()).build());
        item.put("Email", AttributeValue.builder().s(user.getEmail()).build());
        // ...
        
        PutItemRequest request = PutItemRequest.builder()
            .tableName(tableName)
            .item(item)
            .build();
            
        dynamoDbClient.putItem(request);
    }
}

// presentation/controller/UserController.java
@RestController
@RequestMapping("/api/users")
public class UserController {
    private final RegisterUserUseCase registerUserUseCase;
    
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(
        @Valid @RequestBody CreateUserRequest request) {
        
        User user = registerUserUseCase.execute(
            request.getEmail(),
            request.getPassword(),
            request.getNickname()
        );
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(UserResponse.from(user));
    }
}
```


### 2. Redux状態管理（フロントエンド）

#### ディレクトリ構造

```
src/
├── store/
│   ├── index.ts              # Store設定
│   ├── slices/
│   │   ├── userSlice.ts      # ユーザー状態
│   │   ├── recipeSlice.ts    # レシピ状態
│   │   └── alertSlice.ts     # アラート状態
│   └── hooks.ts              # 型付きhooks
```

#### Redux Toolkit実装例

```typescript
// store/slices/userSlice.ts
import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import { User } from '../../types/user';
import { fetchUserProfile } from '../../services/userService';

interface UserState {
  currentUser: User | null;
  loading: boolean;
  error: string | null;
}

const initialState: UserState = {
  currentUser: null,
  loading: false,
  error: null
};

// 非同期アクション
export const fetchUserProfileThunk = createAsyncThunk(
  'user/fetchProfile',
  async (userId: string, { rejectWithValue }) => {
    try {
      return await fetchUserProfile(userId);
    } catch (error) {
      return rejectWithValue('ユーザー情報の取得に失敗しました');
    }
  }
);

// Slice定義
const userSlice = createSlice({
  name: 'user',
  initialState,
  reducers: {
    setUser: (state, action: PayloadAction<User>) => {
      state.currentUser = action.payload;
    },
    clearUser: (state) => {
      state.currentUser = null;
    }
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchUserProfileThunk.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchUserProfileThunk.fulfilled, (state, action) => {
        state.loading = false;
        state.currentUser = action.payload;
      })
      .addCase(fetchUserProfileThunk.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload as string;
      });
  }
});

export const { setUser, clearUser } = userSlice.actions;
export default userSlice.reducer;

// store/hooks.ts (型付きhooks)
import { TypedUseSelectorHook, useDispatch, useSelector } from 'react-redux';
import type { RootState, AppDispatch } from './index';

export const useAppDispatch = () => useDispatch<AppDispatch>();
export const useAppSelector: TypedUseSelectorHook<RootState> = useSelector;

// コンポーネントでの使用例
import { useAppDispatch, useAppSelector } from '../../store/hooks';
import { fetchUserProfileThunk } from '../../store/slices/userSlice';

export const ProfilePage: React.FC = () => {
  const dispatch = useAppDispatch();
  const { currentUser, loading, error } = useAppSelector(state => state.user);
  
  useEffect(() => {
    dispatch(fetchUserProfileThunk('user-id'));
  }, [dispatch]);
  
  if (loading) return <div>読み込み中...</div>;
  if (error) return <div>エラー: {error}</div>;
  
  return <div>{currentUser?.nickname}</div>;
};
```

### 3. DynamoDBアクセスパターン

#### 効率的なクエリ設計

```java
// ✅ 推奨: GetItemを使用（Partition Keyで直接取得）
public Optional<User> findById(String userId) {
    GetItemRequest request = GetItemRequest.builder()
        .tableName(tableName)
        .key(Map.of("UserId", AttributeValue.builder().s(userId).build()))
        .build();
    
    GetItemResponse response = dynamoDbClient.getItem(request);
    return response.hasItem() ? Optional.of(mapToUser(response.item())) : Optional.empty();
}

// ✅ 推奨: Queryを使用（Partition Key + Sort Key）
public List<Schedule> findByUserIdAndDateRange(
    String userId, String startDate, String endDate) {
    
    QueryRequest request = QueryRequest.builder()
        .tableName(tableName)
        .keyConditionExpression("UserId = :userId AND DateTypeRecipeId BETWEEN :start AND :end")
        .expressionAttributeValues(Map.of(
            ":userId", AttributeValue.builder().s(userId).build(),
            ":start", AttributeValue.builder().s(startDate).build(),
            ":end", AttributeValue.builder().s(endDate).build()
        ))
        .build();
    
    return dynamoDbClient.query(request).items().stream()
        .map(this::mapToSchedule)
        .collect(Collectors.toList());
}

// ⚠️ 注意: Scanは最小限に（全テーブルスキャン）
public List<Recipe> findAllPublicRecipes() {
    ScanRequest request = ScanRequest.builder()
        .tableName(tableName)
        .filterExpression("IsPublic = :isPublic AND IsDeleted = :isDeleted")
        .expressionAttributeValues(Map.of(
            ":isPublic", AttributeValue.builder().bool(true).build(),
            ":isDeleted", AttributeValue.builder().bool(false).build()
        ))
        .build();
    
    return dynamoDbClient.scan(request).items().stream()
        .map(this::mapToRecipe)
        .collect(Collectors.toList());
}
```

#### GSI（Global Secondary Index）の活用

```java
// GSI_Authorを使用してAuthorIdで検索
public List<Recipe> findByAuthorId(String authorId) {
    QueryRequest request = QueryRequest.builder()
        .tableName(tableName)
        .indexName("GSI_Author")
        .keyConditionExpression("AuthorId = :authorId")
        .expressionAttributeValues(Map.of(
            ":authorId", AttributeValue.builder().s(authorId).build()
        ))
        .build();
    
    return dynamoDbClient.query(request).items().stream()
        .map(this::mapToRecipe)
        .collect(Collectors.toList());
}
```


## コミット前チェックリスト

### 必須チェック項目

#### 1. ビルド確認

```bash
# バックエンド
cd backend
./gradlew build -x test

# フロントエンド
cd frontend
npm run build
```

#### 2. リンター確認

```bash
# バックエンド（Checkstyle）
./gradlew checkstyleMain

# フロントエンド（ESLint）
npm run lint
```

#### 3. デバッグコード削除

**チェック項目**:
- [ ] `console.log()` が残っていないか
- [ ] `System.out.println()` が残っていないか
- [ ] `debugger` が残っていないか
- [ ] TODOコメントが残っていないか（意図的なものを除く）

#### 4. 未使用import削除

```typescript
// ❌ 未使用のimport
import { useState, useEffect, useMemo } from 'react'; // useMemoは未使用

// ✅ 使用しているimportのみ
import { useState, useEffect } from 'react';
```

#### 5. コーディング規約遵守

**バックエンド**:
- [ ] `@Slf4j` を使用していないか
- [ ] `System.getProperty()` を使用していないか
- [ ] Logger を直接定義しているか
- [ ] `@Value` アノテーションを使用しているか

**フロントエンド**:
- [ ] `axios` や `fetch` を直接使用していないか
- [ ] `apiClient.ts` のヘルパー関数を使用しているか
- [ ] すべての関数に型定義があるか
- [ ] `any` 型を使用していないか

#### 6. セキュリティチェック

- [ ] 機密情報をログに出力していないか
- [ ] パスワードやAPIキーをハードコードしていないか
- [ ] ユーザー入力をサニタイズしているか
- [ ] バリデーションを実装しているか

### コミットメッセージ規約

```bash
# フォーマット: <type>: <description>

# Types:
# feat: 新機能
# fix: バグ修正
# docs: ドキュメント変更
# refactor: リファクタリング
# test: テスト追加
# chore: メンテナンスタスク

# 例:
git commit -m "feat: ユーザー登録機能を追加"
git commit -m "fix: ログイン時のnullポインタを解決"
git commit -m "docs: API仕様書を更新"
```


## コードレビュー観点

### 1. コーディング規約の遵守

**チェック項目**:
- ロギング方法は正しいか（Logger直接定義）
- API呼び出し方法は正しいか（apiClient使用）
- 型定義は適切か
- import文は整理されているか

### 2. セキュリティ

**チェック項目**:
- 機密情報のログ出力はないか
- 入力バリデーションは実装されているか
- SQLインジェクション対策（該当する場合）
- XSS対策（ユーザー入力のサニタイズ）
- CSRF対策（該当する場合）
- 認証・認可は適切か

### 3. パフォーマンス

**チェック項目**:
- 不要なループはないか
- 非効率なデータベースクエリはないか（Scan多用）
- メモリリークの可能性はないか
- 不要な再レンダリングはないか（React）
- 適切なキャッシング戦略があるか

### 4. 可読性

**チェック項目**:
- 変数名は適切か（意味が明確）
- 関数名は適切か（動詞で始まる）
- コメントは適切か（なぜを説明）
- 関数の責任は単一か
- マジックナンバーは定数化されているか

### 5. テスタビリティ

**チェック項目**:
- 依存関係の注入は適切か
- モック化は容易か
- 単体テストは書きやすい設計か
- テストカバレッジは十分か

### 6. アーキテクチャ

**チェック項目**:
- Clean Architectureの原則に従っているか
- レイヤー間の依存関係は正しいか
- ビジネスロジックはdomainレイヤーにあるか
- 技術詳細はinfrastructureレイヤーにあるか


## トラブルシューティング

### バックエンド

#### 問題: DynamoDBテーブルが見つからない

**症状**:
```
ResourceNotFoundException: Requested resource not found
```

**原因**: テーブル名の設定が間違っている

**解決策**:
```java
// application.properties または application.yml を確認
aws.dynamodb.table.users=Users
aws.dynamodb.table.recipes=Recipes

// @Value アノテーションで正しく読み込まれているか確認
@Value("${aws.dynamodb.table.users:Users}")
private String tableName;
```

#### 問題: Cognitoトークン検証エラー

**症状**:
```
InvalidTokenException: Token signature verification failed
```

**原因**: トークンの検証設定が間違っている

**解決策**:
```java
// Cognito設定を確認
aws.cognito.userPoolId=ap-northeast-1_XXXXXXXXX
aws.cognito.region=ap-northeast-1
```

### フロントエンド

#### 問題: API呼び出しでCORSエラー

**症状**:
```
Access to fetch at 'http://localhost:8080/api/users' from origin 'http://localhost:5173' 
has been blocked by CORS policy
```

**原因**: バックエンドのCORS設定が不足

**解決策**:
```java
// WebConfig.java でCORS設定を追加
@Override
public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/api/**")
        .allowedOrigins("http://localhost:5173")
        .allowedMethods("GET", "POST", "PUT", "DELETE")
        .allowCredentials(true);
}
```

#### 問題: Redux状態が更新されない

**症状**: 状態を更新しても画面に反映されない

**原因**: Immutableな更新ができていない

**解決策**:
```typescript
// ❌ 間違い: 直接変更
state.user.nickname = action.payload;

// ✅ 正しい: 新しいオブジェクトを作成
state.user = { ...state.user, nickname: action.payload };

// または Redux Toolkit の Immer を使用（推奨）
state.user.nickname = action.payload; // Redux Toolkitでは自動的にImmutableに
```

#### 問題: 環境変数が読み込めない

**症状**:
```
undefined is not a valid value for VITE_API_URL
```

**原因**: 環境変数の命名規則が間違っている

**解決策**:
```bash
# .env.dev
# ✅ Viteでは VITE_ プレフィックスが必要
VITE_API_URL=http://localhost:8080
VITE_API_KEY=your-api-key

# ❌ 間違い
API_URL=http://localhost:8080
```

```typescript
// 使用方法
const apiUrl = import.meta.env.VITE_API_URL;
```


## パフォーマンス最適化

### バックエンド

#### 1. DynamoDBクエリ最適化

```java
// ❌ 非効率: Scanを使用
public List<Recipe> findAllRecipes() {
    ScanRequest request = ScanRequest.builder()
        .tableName(tableName)
        .build();
    return dynamoDbClient.scan(request).items();
}

// ✅ 効率的: GSIを使用したQuery
public List<Recipe> findByAuthorId(String authorId) {
    QueryRequest request = QueryRequest.builder()
        .tableName(tableName)
        .indexName("GSI_Author")
        .keyConditionExpression("AuthorId = :authorId")
        .expressionAttributeValues(Map.of(
            ":authorId", AttributeValue.builder().s(authorId).build()
        ))
        .build();
    return dynamoDbClient.query(request).items();
}
```

#### 2. バッチ処理

```java
// 複数アイテムの取得
public List<User> findByIds(List<String> userIds) {
    List<Map<String, AttributeValue>> keys = userIds.stream()
        .map(id -> Map.of("UserId", AttributeValue.builder().s(id).build()))
        .collect(Collectors.toList());
    
    BatchGetItemRequest request = BatchGetItemRequest.builder()
        .requestItems(Map.of(tableName, KeysAndAttributes.builder()
            .keys(keys)
            .build()))
        .build();
    
    return dynamoDbClient.batchGetItem(request)
        .responses()
        .get(tableName)
        .stream()
        .map(this::mapToUser)
        .collect(Collectors.toList());
}
```

### フロントエンド

#### 1. React.memoでの最適化

```typescript
// 不要な再レンダリングを防ぐ
export const RecipeCard = React.memo<RecipeCardProps>(({ recipe, onClick }) => {
  return (
    <div onClick={() => onClick(recipe.recipeId)}>
      <h3>{recipe.title}</h3>
      <p>{recipe.cookingTime}分</p>
    </div>
  );
});
```

#### 2. useCallbackとuseMemoの活用

```typescript
export const RecipeList: React.FC = () => {
  const [recipes, setRecipes] = useState<Recipe[]>([]);
  
  // 関数のメモ化
  const handleRecipeClick = useCallback((recipeId: string) => {
    navigate(`/recipes/${recipeId}`);
  }, [navigate]);
  
  // 計算結果のメモ化
  const sortedRecipes = useMemo(() => {
    return [...recipes].sort((a, b) => 
      a.cookingTime - b.cookingTime
    );
  }, [recipes]);
  
  return (
    <div>
      {sortedRecipes.map(recipe => (
        <RecipeCard 
          key={recipe.recipeId}
          recipe={recipe}
          onClick={handleRecipeClick}
        />
      ))}
    </div>
  );
};
```

#### 3. 遅延ローディング

```typescript
// コンポーネントの遅延ローディング
import { lazy, Suspense } from 'react';

const RecipeEditPage = lazy(() => import('./pages/RecipeEdit/RecipeEditPage'));
const ProfilePage = lazy(() => import('./pages/Profile/ProfilePage'));

export const App: React.FC = () => {
  return (
    <Suspense fallback={<div>読み込み中...</div>}>
      <Routes>
        <Route path="/recipes/edit/:id" element={<RecipeEditPage />} />
        <Route path="/profile" element={<ProfilePage />} />
      </Routes>
    </Suspense>
  );
};
```


## テストベストプラクティス

### バックエンド（JUnit）

#### 1. 単体テスト

```java
@ExtendWith(MockitoExtension.class)
class RegisterUserUseCaseTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @InjectMocks
    private RegisterUserUseCase registerUserUseCase;
    
    @Test
    void execute_成功_新規ユーザーを登録できる() {
        // Given
        String email = "test@example.com";
        String password = "Password123!";
        String nickname = "テストユーザー";
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(password)).thenReturn("encoded-password");
        
        // When
        User result = registerUserUseCase.execute(email, password, nickname);
        
        // Then
        assertNotNull(result);
        assertEquals(email, result.getEmail());
        assertEquals(nickname, result.getNickname());
        verify(userRepository).save(any(User.class));
    }
    
    @Test
    void execute_失敗_既存ユーザーの場合は例外をスロー() {
        // Given
        String email = "existing@example.com";
        when(userRepository.findByEmail(email))
            .thenReturn(Optional.of(new User()));
        
        // When & Then
        assertThrows(UserAlreadyExistsException.class, () -> {
            registerUserUseCase.execute(email, "password", "nickname");
        });
    }
}
```

#### 2. 統合テスト

```java
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    void register_成功_ユーザー登録できる() throws Exception {
        // Given
        CreateUserRequest request = CreateUserRequest.builder()
            .email("test@example.com")
            .password("Password123!")
            .nickname("テストユーザー")
            .build();
        
        // When & Then
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.email").value("test@example.com"))
            .andExpect(jsonPath("$.nickname").value("テストユーザー"));
    }
}
```

### フロントエンド（Jest + React Testing Library）

#### 1. コンポーネントテスト

```typescript
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { LoginPage } from './LoginPage';
import { Provider } from 'react-redux';
import { store } from '../../store';

describe('LoginPage', () => {
  it('ログインフォームが表示される', () => {
    render(
      <Provider store={store}>
        <LoginPage />
      </Provider>
    );
    
    expect(screen.getByLabelText('メールアドレス')).toBeInTheDocument();
    expect(screen.getByLabelText('パスワード')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'ログイン' })).toBeInTheDocument();
  });
  
  it('フォーム送信時にログイン処理が実行される', async () => {
    const mockLogin = jest.fn();
    
    render(
      <Provider store={store}>
        <LoginPage onLogin={mockLogin} />
      </Provider>
    );
    
    // 入力
    fireEvent.change(screen.getByLabelText('メールアドレス'), {
      target: { value: 'test@example.com' }
    });
    fireEvent.change(screen.getByLabelText('パスワード'), {
      target: { value: 'Password123!' }
    });
    
    // 送信
    fireEvent.click(screen.getByRole('button', { name: 'ログイン' }));
    
    // 検証
    await waitFor(() => {
      expect(mockLogin).toHaveBeenCalledWith({
        email: 'test@example.com',
        password: 'Password123!'
      });
    });
  });
});
```

#### 2. カスタムフックのテスト

```typescript
import { renderHook, act } from '@testing-library/react';
import { useRecipeEditHandlers } from './useRecipeEditHandlers';

describe('useRecipeEditHandlers', () => {
  it('タイトル変更が正しく動作する', () => {
    const { result } = renderHook(() => useRecipeEditHandlers());
    
    act(() => {
      result.current.handleTitleChange('新しいレシピ');
    });
    
    expect(result.current.recipe.title).toBe('新しいレシピ');
  });
  
  it('食材追加が正しく動作する', () => {
    const { result } = renderHook(() => useRecipeEditHandlers());
    
    const ingredient = {
      name: '玉ねぎ',
      quantity: 2,
      unit: 'piece'
    };
    
    act(() => {
      result.current.handleIngredientAdd(ingredient);
    });
    
    expect(result.current.recipe.ingredients).toHaveLength(1);
    expect(result.current.recipe.ingredients[0]).toEqual(ingredient);
  });
});
```


## 多言語対応（国際化）

### バックエンド

#### メッセージプロパティファイル

```properties
# messages_ja.properties
user.not.found=ユーザーが見つかりません
user.already.exists=ユーザーが既に存在します
validation.email.invalid=メールアドレスの形式が正しくありません
validation.password.weak=パスワードは8文字以上で、大文字・小文字・数字を含む必要があります

# messages_ko.properties
user.not.found=사용자를 찾을 수 없습니다
user.already.exists=사용자가 이미 존재합니다
validation.email.invalid=이메일 주소 형식이 올바르지 않습니다
validation.password.weak=비밀번호는 8자 이상이어야 하며 대문자, 소문자, 숫자를 포함해야 합니다
```

#### メッセージソースの設定

```java
@Configuration
public class MessageConfig {
    
    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = 
            new ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }
    
    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
        resolver.setDefaultLocale(Locale.JAPANESE);
        resolver.setSupportedLocales(Arrays.asList(
            Locale.JAPANESE,
            Locale.KOREAN
        ));
        return resolver;
    }
}
```

#### 使用例

```java
@Service
public class UserService {
    private final MessageSource messageSource;
    
    public User findById(String userId, Locale locale) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(
                messageSource.getMessage("user.not.found", null, locale)
            ));
    }
}
```

### フロントエンド（i18next）

#### 翻訳ファイル

```typescript
// locales/ja.json
{
  "common": {
    "login": "ログイン",
    "logout": "ログアウト",
    "save": "保存",
    "cancel": "キャンセル"
  },
  "user": {
    "profile": "プロフィール",
    "email": "メールアドレス",
    "password": "パスワード"
  },
  "errors": {
    "userNotFound": "ユーザーが見つかりません",
    "invalidEmail": "メールアドレスの形式が正しくありません"
  }
}

// locales/ko.json
{
  "common": {
    "login": "로그인",
    "logout": "로그아웃",
    "save": "저장",
    "cancel": "취소"
  },
  "user": {
    "profile": "프로필",
    "email": "이메일 주소",
    "password": "비밀번호"
  },
  "errors": {
    "userNotFound": "사용자를 찾을 수 없습니다",
    "invalidEmail": "이메일 주소 형식이 올바르지 않습니다"
  }
}
```

#### i18next設定

```typescript
// i18n.ts
import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import ja from './locales/ja.json';
import ko from './locales/ko.json';

i18n
  .use(initReactI18next)
  .init({
    resources: {
      ja: { translation: ja },
      ko: { translation: ko }
    },
    lng: 'ja',
    fallbackLng: 'ja',
    interpolation: {
      escapeValue: false
    }
  });

export default i18n;
```

#### 使用例

```typescript
import { useTranslation } from 'react-i18next';

export const LoginPage: React.FC = () => {
  const { t, i18n } = useTranslation();
  
  const changeLanguage = (lang: string) => {
    i18n.changeLanguage(lang);
  };
  
  return (
    <div>
      <h1>{t('common.login')}</h1>
      <input placeholder={t('user.email')} />
      <input type="password" placeholder={t('user.password')} />
      <button>{t('common.login')}</button>
      
      <select onChange={(e) => changeLanguage(e.target.value)}>
        <option value="ja">日本語</option>
        <option value="ko">한국어</option>
      </select>
    </div>
  );
};
```


## 参考資料

### プロジェクトドキュメント

- **コーディング規約**: `CODING_STANDARDS.md`
- **要件定義書**: `docs/要件定義書_自炊支援アプリ.md`
- **データベース設計**: `docs/DATABASE_DESIGN.md`
- **API仕様書**: `docs/API_SPECIFICATION.md`
- **OpenAPI仕様**: `docs/openapi.yaml`

### 技術ドキュメント

**バックエンド**:
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [AWS SDK for Java](https://aws.amazon.com/sdk-for-java/)
- [DynamoDB Developer Guide](https://docs.aws.amazon.com/dynamodb/)

**フロントエンド**:
- [React Documentation](https://react.dev/)
- [TypeScript Handbook](https://www.typescriptlang.org/docs/)
- [Redux Toolkit](https://redux-toolkit.js.org/)
- [React Router](https://reactrouter.com/)

**AWS**:
- [AWS Cognito](https://docs.aws.amazon.com/cognito/)
- [Amazon S3](https://docs.aws.amazon.com/s3/)
- [Amazon CloudWatch](https://docs.aws.amazon.com/cloudwatch/)

## クイックリファレンス

### 禁止事項まとめ

#### バックエンド
```java
// ❌ 禁止
@Slf4j
System.out.println()
System.getProperty()

// ✅ 使用
private static final Logger log = LoggerFactory.getLogger(MyClass.class);
log.info()
@Value("${property.name}")
```

#### フロントエンド
```typescript
// ❌ 禁止
import axios from 'axios';
await fetch('/api/users');
console.log() // 本番コード

// ✅ 使用
import { apiGet, apiPost } from '../utils/apiClient';
await apiGet<User[]>('/api/users');
// デバッグログは開発中のみ、コミット前に削除
```

### よく使うコマンド

```bash
# バックエンド
cd backend
./gradlew build -x test          # ビルド
./gradlew checkstyleMain         # Checkstyle
./gradlew bootRun                # 起動

# フロントエンド
cd frontend
npm run build                    # ビルド
npm run lint                     # ESLint
npm run dev                      # 開発サーバー起動
npm test                         # テスト実行
```

### 環境変数設定

```bash
# バックエンド (application.yml)
aws:
  dynamodb:
    table:
      users: Users
      recipes: Recipes
  cognito:
    userPoolId: ap-northeast-1_XXXXXXXXX
    region: ap-northeast-1

# フロントエンド (.env.dev)
VITE_API_URL=http://localhost:8080
VITE_COGNITO_USER_POOL_ID=ap-northeast-1_XXXXXXXXX
VITE_COGNITO_CLIENT_ID=XXXXXXXXXXXXXXXXXXXXXXXXXX
```

## まとめ

このベストプラクティスガイドは、Let's Cookプロジェクトの開発における重要な指針を提供します。

**重要なポイント**:

1. **セキュリティ**: 機密情報の保護を最優先に
2. **Clean Architecture**: レイヤー間の依存関係を守る
3. **型安全性**: TypeScriptの型システムを活用
4. **コーディング規約**: プロジェクト固有のルールを遵守
5. **コミット前チェック**: 必ずビルドとリンターを実行

**開発フロー**:
1. コーディング規約を確認
2. 実装
3. ビルド確認
4. リンター確認
5. デバッグコード削除
6. コミット

このガイドを参照しながら、高品質なコードを維持しましょう！

---

**このパワーはナレッジベースパワーです** - MCPサーバーは不要

**作成日**: 2024-12-18  
**バージョン**: 1.0.0
