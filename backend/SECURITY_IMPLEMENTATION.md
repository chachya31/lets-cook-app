# Spring Security + Cognito 認証実装ガイド

## 実装概要

AWS Cognito Groups と Spring Security を組み合わせた JWT 認証・認可システムを実装しました。

## アーキテクチャ

```
┌─────────┐     ┌──────────┐     ┌─────────────────────┐
│ Client  │────►│ Cognito  │     │ Spring Boot Backend │
└─────────┘     └──────────┘     └─────────────────────┘
     │               │                      │
     │ 1. Login      │                      │
     ├──────────────►│                      │
     │◄──────────────┤                      │
     │   JWT Token   │                      │
     │   (cognito:groups含む)               │
     │                                      │
     │ 2. API Request (Authorization: Bearer <JWT>)
     ├─────────────────────────────────────►│
     │                                      │ 3. JwtAuthenticationFilter
     │                                      │    - JWT検証
     │                                      │    - cognito:groups抽出
     │                                      │    - Spring Security認証設定
     │                                      │
     │                                      │ 4. SecurityConfig
     │                                      │    - エンドポイント認可
     │                                      │    - ロールチェック
     │◄─────────────────────────────────────┤
     │   Response                           │
```

## 実装ファイル

### 1. 依存関係（build.gradle）
```gradle
implementation 'org.springframework.boot:spring-boot-starter-security'
implementation 'com.auth0:java-jwt:4.4.0'
```

### 2. JwtAuthenticationFilter
**パス**: `backend/src/main/java/com/cookingapp/infrastructure/security/JwtAuthenticationFilter.java`

**役割**:
- リクエストヘッダーから JWT トークンを抽出
- Cognito で JWT を検証
- JWT から `cognito:groups` クレームを抽出
- Spring Security の認証情報（`Authentication`）を設定

**処理フロー**:
```java
1. Authorization ヘッダーから "Bearer <token>" を抽出
2. CognitoAuthService.validateToken() でトークン検証
3. JWT.decode() で cognito:groups を取得
4. グループを ROLE_ADMINS, ROLE_USERS に変換
5. SecurityContextHolder に認証情報を設定
```

### 3. SecurityConfig
**パス**: `backend/src/main/java/com/cookingapp/infrastructure/config/SecurityConfig.java`

**役割**:
- エンドポイントごとの認可ルールを定義
- JWT フィルターを Spring Security フィルターチェーンに追加

**認可ルール**:
```java
// 認証不要
/api/users/register
/api/users/login
/api/users/confirm
/api/users/resend-code
GET /api/recipes
GET /api/recipes/*
GET /api/recipes/*/reviews

// 管理者のみ（ROLE_ADMINS）
/api/admin/**

// その他は認証必須
anyRequest().authenticated()
```

### 4. CognitoAuthService（拡張）
**パス**: `backend/src/main/java/com/cookingapp/infrastructure/external/cognito/CognitoAuthService.java`

**追加メソッド**:
- `addUserToGroup(username, groupName)` - ユーザーをグループに追加
- `removeUserFromGroup(username, groupName)` - ユーザーをグループから削除

### 5. RegisterUserUseCase（更新）
**パス**: `backend/src/main/java/com/cookingapp/application/usecase/user/RegisterUserUseCase.java`

**変更点**:
- ユーザー登録時に自動的に `Users` グループに追加

### 6. AdminController（更新）
**パス**: `backend/src/main/java/com/cookingapp/presentation/controller/AdminController.java`

**変更点**:
- `@PreAuthorize("hasRole('ADMINS')")` アノテーションを追加

## Cognito グループ設定

### グループ一覧
| グループ名 | 説明         | 優先順位 | Spring Security ロール |
| ---------- | ------------ | -------- | ---------------------- |
| Admins     | 管理者       | 10       | ROLE_ADMINS            |
| Users      | 一般ユーザー | 1        | ROLE_USERS             |

### JWT トークンの構造
```json
{
  "sub": "550e8400-e29b-41d4-a716-446655440000",
  "cognito:groups": ["Admins"],
  "email": "admin@example.com",
  "email_verified": true,
  "iss": "https://cognito-idp.ap-northeast-1.amazonaws.com/...",
  "exp": 1734567890
}
```

## テスト方法

### 1. 一般ユーザーの登録・ログイン

```bash
# 1. ユーザー登録
POST /api/users/register
{
  "email": "user@example.com",
  "password": "Password123!",
  "nickname": "テストユーザー",
  "preferredLanguage": "ja"
}

# 2. メール確認
POST /api/users/confirm
{
  "email": "user@example.com",
  "confirmationCode": "123456"
}

# 3. ログイン
POST /api/users/login
{
  "email": "user@example.com",
  "password": "Password123!"
}

# レスポンス
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "...",
  "idToken": "...",
  "expiresIn": 3600
}

# 4. 認証が必要なAPIを呼び出し
GET /api/users/profile/{userId}
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### 2. 管理者ユーザーの作成

```bash
# Cognito コンソールで実施:
1. ユーザープール → ユーザー → ユーザーを作成
2. メールアドレス: admin@example.com
3. パスワード: Admin123!
4. 作成後、「グループ」タブから「Admins」グループに追加

# または、既存ユーザーを管理者に昇格:
# CognitoAuthService.addUserToGroup("user@example.com", "Admins")
```

### 3. 管理者APIのテスト

```bash
# 1. 管理者でログイン
POST /api/users/login
{
  "email": "admin@example.com",
  "password": "Admin123!"
}

# 2. 管理者APIを呼び出し
GET /api/admin/dashboard
Authorization: Bearer <admin_access_token>

# 成功レスポンス
{
  "message": "管理者ダッシュボード",
  "totalUsers": 150,
  "totalRecipes": 320
}

# 一般ユーザーのトークンで呼び出すと 403 Forbidden
```

### 4. JWT トークンの確認

https://jwt.io/ でトークンをデコードして、`cognito:groups` クレームを確認:

```json
{
  "cognito:groups": ["Admins"]  // または ["Users"]
}
```

## トラブルシューティング

### 問題: 403 Forbidden が返される

**原因1**: JWT に `cognito:groups` が含まれていない
- **解決**: Cognito コンソールでユーザーがグループに所属しているか確認
- ログイン後、新しいトークンを取得

**原因2**: グループ名が間違っている
- **解決**: グループ名は `Admins` と `Users`（複数形）
- Spring Security では `ROLE_ADMINS`, `ROLE_USERS` に変換される

**原因3**: トークンが期限切れ
- **解決**: 再ログインして新しいトークンを取得

### 問題: ユーザー登録時にグループ追加エラー

**原因**: Cognito にグループが存在しない
- **解決**: Cognito コンソールで `Admins` と `Users` グループを作成

### 問題: ビルドエラー

```
error: cannot find symbol
  symbol:   class PreAuthorize
```

**解決**: `build.gradle` に Spring Security 依存を追加
```gradle
implementation 'org.springframework.boot:spring-boot-starter-security'
```

## セキュリティ考慮事項

### ✅ 実装済み
- JWT トークンの検証（Cognito GetUser API）
- ロールベースアクセス制御（RBAC）
- ステートレス認証（セッション不使用）
- CSRF 無効化（JWT 使用のため）

### 🔒 推奨事項
- HTTPS 必須（本番環境）
- トークンの有効期限を短く設定（1時間）
- リフレッシュトークンの適切な管理
- ログ監視（不正アクセス検知）

## 今後の拡張

### 1. より細かいロール管理
```java
// 例: レシピ編集者ロール
@PreAuthorize("hasAnyRole('ADMINS', 'EDITORS')")
public ResponseEntity<RecipeResponse> updateRecipe(...) {
    // ...
}
```

### 2. ユーザー単位の権限チェック
```java
// 例: 自分のデータのみアクセス可能
@PreAuthorize("#userId == authentication.principal")
public ResponseEntity<UserResponse> getUserProfile(@PathVariable String userId) {
    // ...
}
```

### 3. 監査ログ
```java
@Aspect
public class AuditAspect {
    @Around("@annotation(PreAuthorize)")
    public Object logAccess(ProceedingJoinPoint joinPoint) {
        // アクセスログを記録
    }
}
```

## 参考資料

- [Spring Security Reference](https://docs.spring.io/spring-security/reference/)
- [AWS Cognito User Pools](https://docs.aws.amazon.com/cognito/latest/developerguide/cognito-user-identity-pools.html)
- [JWT.io](https://jwt.io/) - JWT デバッグツール
