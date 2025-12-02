# API仕様書

## 概要

自炊支援・食費節約アプリケーションのREST API仕様書です。

### ベースURL
- **ローカル開発**: `http://localhost:8080`
- **開発環境**: `https://api-dev.example.com`
- **本番環境**: `https://api.example.com`

### 認証
- **方式**: AWS Cognito
- **アクセストークン有効期限**: 1時間
- **リフレッシュトークン有効期限**: 90日

### 多言語対応
- **対応言語**: 日本語（ja）、韓国語（ko）
- **ヘッダー**: `Accept-Language: ja` または `Accept-Language: ko`

### 共通ヘッダー
- `Content-Type: application/json`
- `Accept-Language: ja` (オプション)
- `X-User-Id: {userId}` (認証が必要なエンドポイント)

---

## 目次

1. [Users API](#users-api) - ユーザー管理
2. [Recipes API](#recipes-api) - レシピ管理
3. [Reviews API](#reviews-api) - レビュー管理
4. [Schedules API](#schedules-api) - スケジュール管理
5. [Shopping Lists API](#shopping-lists-api) - 買い物リスト管理
6. [Alerts API](#alerts-api) - アラート機能
7. [Admin API](#admin-api) - 管理者機能
8. [共通データモデル](#共通データモデル)
9. [エラーレスポンス](#エラーレスポンス)

---

## Users API

### 1. ユーザー登録

**エンドポイント**: `POST /api/users/register`

**説明**: 新規ユーザーを登録します。

**リクエストボディ**:
```json
{
  "email": "user@example.com",
  "password": "Password123!",
  "nickname": "山田太郎",
  "preferredLanguage": "ja"
}
```

**バリデーション**:
- `email`: 必須、メール形式
- `password`: 必須、8文字以上、大文字・小文字・数字を含む
- `nickname`: 必須、1-50文字
- `preferredLanguage`: オプション、"ja" または "ko"

**レスポンス**: `201 Created`
```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "email": "user@example.com",
  "nickname": "山田太郎",
  "displayName": "山田太郎",
  "preferredLanguage": "ja",
  "createdAt": "2024-12-02T10:00:00"
}
```

**エラー**:
- `400`: バリデーションエラー
- `409`: ユーザーが既に存在

---

### 2. メール確認

**エンドポイント**: `POST /api/users/confirm`

**説明**: メールアドレスを確認します。

**リクエストボディ**:
```json
{
  "email": "user@example.com",
  "confirmationCode": "123456"
}
```

**レスポンス**: `200 OK`

---

### 3. 確認コード再送信

**エンドポイント**: `POST /api/users/resend-code`

**リクエストボディ**:
```json
{
  "email": "user@example.com"
}
```

**レスポンス**: `200 OK`

---

### 4. ログイン

**エンドポイント**: `POST /api/users/login`

**リクエストボディ**:
```json
{
  "email": "user@example.com",
  "password": "Password123!"
}
```

**レスポンス**: `200 OK`
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "idToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 3600,
  "user": {
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "email": "user@example.com",
    "nickname": "山田太郎"
  }
}
```

---

### 5. プロフィール取得

**エンドポイント**: `GET /api/users/profile/{userId}`

**パスパラメータ**:
- `userId`: ユーザーID

**レスポンス**: `200 OK`
```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "email": "user@example.com",
  "nickname": "山田太郎",
  "displayName": "Taro Yamada",
  "profileImageUrl": "https://s3.../profile.jpg",
  "preferredLanguage": "ja",
  "lastCookingDate": "2024-12-01",
  "timezone": "Asia/Tokyo",
  "createdAt": "2024-11-01T09:00:00"
}
```

---

### 6. プロフィール更新

**エンドポイント**: `PUT /api/users/profile/{userId}`

**リクエストボディ**:
```json
{
  "nickname": "山田太郎",
  "displayName": "Taro Yamada",
  "preferredLanguage": "ja",
  "timezone": "Asia/Tokyo",
  "marketingOptOut": false
}
```

**レスポンス**: `200 OK` (UserResponse)

---

### 7. アカウント削除

**エンドポイント**: `DELETE /api/users/account/{userId}`

**レスポンス**: `204 No Content`

---

### 8. プロフィール画像アップロード

**エンドポイント**: `POST /api/users/profile/image`

**Content-Type**: `multipart/form-data`

**リクエストパラメータ**:
- `userId`: ユーザーID
- `file`: 画像ファイル（JPEG/PNG、最大5MB）

**レスポンス**: `200 OK` (UserResponse)

---

## Recipes API

### 1. レシピ検索

**エンドポイント**: `GET /api/recipes`

**クエリパラメータ**:
- `keyword`: 検索キーワード（オプション）
- `authorId`: 作成者ID（オプション）

**レスポンス**: `200 OK`
```json
[
  {
    "recipeId": "660e8400-e29b-41d4-a716-446655440001",
    "title": "簡単カレーライス",
    "authorId": "550e8400-e29b-41d4-a716-446655440000",
    "cookingTime": 30,
    "imageUrl": "https://s3.../recipe.jpg",
    "isPublic": true,
    "createdAt": "2024-11-15T14:30:00"
  }
]
```

---

### 2. レシピ詳細取得

**エンドポイント**: `GET /api/recipes/{id}`

**レスポンス**: `200 OK`
```json
{
  "recipeId": "660e8400-e29b-41d4-a716-446655440001",
  "title": "簡単カレーライス",
  "authorId": "550e8400-e29b-41d4-a716-446655440000",
  "ingredients": [
    {
      "name": "玉ねぎ",
      "quantity": 2,
      "unit": "piece",
      "note": "中サイズ",
      "optional": false
    }
  ],
  "steps": [
    "野菜を切る",
    "炒める",
    "煮込む"
  ],
  "cookingTime": 30,
  "imageUrl": "https://s3.../recipe.jpg",
  "isPublic": true,
  "createdAt": "2024-11-15T14:30:00",
  "updatedAt": "2024-11-20T16:45:00"
}
```

---

### 3. レシピ作成

**エンドポイント**: `POST /api/recipes`

**ヘッダー**: `X-User-Id: {userId}`

**リクエストボディ**:
```json
{
  "title": "簡単カレーライス",
  "ingredients": [
    {
      "name": "玉ねぎ",
      "quantity": 2,
      "unit": "piece",
      "note": "中サイズ",
      "optional": false
    }
  ],
  "steps": [
    "野菜を切る",
    "炒める"
  ],
  "cookingTime": 30
}
```

**バリデーション**:
- `title`: 必須、最大100文字
- `ingredients`: 必須、最低1つ
- `steps`: 必須、最低1つ
- `cookingTime`: 必須、0以上

**レスポンス**: `201 Created` (RecipeResponse)

---

### 4. レシピ更新

**エンドポイント**: `PUT /api/recipes/{id}`

**ヘッダー**: `X-User-Id: {userId}`

**リクエストボディ**: (レシピ作成と同じ)

**レスポンス**: `200 OK` (RecipeResponse)

---

### 5. レシピ削除（論理削除）

**エンドポイント**: `DELETE /api/recipes/{id}`

**ヘッダー**: `X-User-Id: {userId}`

**レスポンス**: `204 No Content`

---

### 6. レシピ画像アップロード

**エンドポイント**: `POST /api/recipes/{id}/image`

**ヘッダー**: `X-User-Id: {userId}`

**Content-Type**: `multipart/form-data`

**リクエストパラメータ**:
- `file`: 画像ファイル（JPEG/PNG、最大5MB）

**レスポンス**: `200 OK` (RecipeResponse)

---

## Reviews API

### 1. レビュー一覧取得

**エンドポイント**: `GET /api/recipes/{recipeId}/reviews`

**レスポンス**: `200 OK`
```json
[
  {
    "reviewId": "770e8400-e29b-41d4-a716-446655440002",
    "recipeId": "660e8400-e29b-41d4-a716-446655440001",
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "rating": 5,
    "comment": "とても美味しかったです！",
    "status": "visible",
    "reportedCount": 0,
    "createdAt": "2024-11-16T10:00:00Z",
    "updatedAt": "2024-11-16T10:00:00Z"
  }
]
```

---

### 2. レビュー作成

**エンドポイント**: `POST /api/recipes/{recipeId}/reviews`

**ヘッダー**: `X-User-Id: {userId}`

**リクエストボディ**:
```json
{
  "rating": 5,
  "comment": "とても美味しかったです！"
}
```

**バリデーション**:
- `rating`: 必須、1-5
- `comment`: オプション、最大300文字

**レスポンス**: `201 Created` (ReviewResponse)

---

### 3. レビュー更新

**エンドポイント**: `PUT /api/reviews/{reviewId}`

**ヘッダー**: `X-User-Id: {userId}`

**リクエストボディ**: (レビュー作成と同じ)

**レスポンス**: `200 OK` (ReviewResponse)

---

### 4. レビュー削除

**エンドポイント**: `DELETE /api/reviews/{reviewId}`

**ヘッダー**: `X-User-Id: {userId}`

**レスポンス**: `204 No Content`

---

### 5. レビュー通報

**エンドポイント**: `POST /api/reviews/{reviewId}/report`

**説明**: 通報カウントが3以上になると自動的に非表示になります。

**レスポンス**: `200 OK` (ReviewResponse)

---

## Schedules API

### 1. スケジュール一覧取得

**エンドポイント**: `GET /api/schedules`

**ヘッダー**: `X-User-Id: {userId}`

**クエリパラメータ**:
- `startDate`: 開始日（YYYY-MM-DD形式、必須）
- `endDate`: 終了日（YYYY-MM-DD形式、必須）

**レスポンス**: `200 OK`
```json
[
  {
    "scheduleId": "880e8400-e29b-41d4-a716-446655440003",
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "date": "2024-12-01",
    "type": "PLANNED",
    "recipeId": "660e8400-e29b-41d4-a716-446655440001",
    "recipeTitle": "簡単カレーライス",
    "memo": "夕食用",
    "createdAt": "2024-11-30T15:00:00Z"
  }
]
```

---

### 2. スケジュール作成

**エンドポイント**: `POST /api/schedules`

**ヘッダー**: `X-User-Id: {userId}`

**リクエストボディ**:
```json
{
  "date": "2024-12-01",
  "type": "PLANNED",
  "recipeId": "660e8400-e29b-41d4-a716-446655440001",
  "recipeTitle": "簡単カレーライス",
  "memo": "夕食用"
}
```

**バリデーション**:
- `date`: 必須、YYYY-MM-DD形式
- `type`: 必須、"PLANNED" または "COOKED"
- `recipeId`: 必須
- `recipeTitle`: 必須
- `memo`: オプション、最大120文字

**レスポンス**: `201 Created` (ScheduleResponse)

---

### 3. スケジュール更新

**エンドポイント**: `PUT /api/schedules/{scheduleId}`

**ヘッダー**: `X-User-Id: {userId}`

**リクエストボディ**:
```json
{
  "memo": "昼食用に変更"
}
```

**レスポンス**: `200 OK` (ScheduleResponse)

---

### 4. スケジュール削除

**エンドポイント**: `DELETE /api/schedules/{scheduleId}`

**ヘッダー**: `X-User-Id: {userId}`

**レスポンス**: `204 No Content`

---

### 5. 予定を実績に変換

**エンドポイント**: `POST /api/schedules/{scheduleId}/convert-to-cooked`

**ヘッダー**: `X-User-Id: {userId}`

**説明**: PLANNEDをCOOKEDに変換し、LastCookingDateを更新します。

**レスポンス**: `200 OK` (ScheduleResponse)

---

## Shopping Lists API

### 1. 買い物リスト取得

**エンドポイント**: `GET /api/shopping-lists`

**ヘッダー**: `X-User-Id: {userId}`

**説明**: 期限切れアイテム（チェック済みから3日経過）を自動削除します。

**レスポンス**: `200 OK`
```json
[
  {
    "itemId": "990e8400-e29b-41d4-a716-446655440004",
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "name": "玉ねぎ",
    "quantity": 2,
    "unit": "piece",
    "isChecked": false,
    "isCheckedAt": null,
    "addedAt": "2024-11-30T10:00:00Z",
    "sourceRecipeId": "660e8400-e29b-41d4-a716-446655440001"
  }
]
```

---

### 2. アイテム追加

**エンドポイント**: `POST /api/shopping-lists`

**ヘッダー**: `X-User-Id: {userId}`

**リクエストボディ**:
```json
{
  "name": "玉ねぎ",
  "quantity": 2,
  "unit": "piece",
  "sourceRecipeId": "660e8400-e29b-41d4-a716-446655440001"
}
```

**バリデーション**:
- `name`: 必須、最大100文字
- `quantity`: 必須、0.01-9999
- `unit`: 必須
- `sourceRecipeId`: オプション

**説明**: 同じ正規化キー（名前+単位）のアイテムがある場合、数量を合算します。

**レスポンス**: `201 Created` (ShoppingListItemResponse)

---

### 3. アイテム更新（チェック状態）

**エンドポイント**: `PUT /api/shopping-lists/{itemId}`

**ヘッダー**: `X-User-Id: {userId}`

**リクエストボディ**:
```json
{
  "isChecked": true
}
```

**レスポンス**: `200 OK` (ShoppingListItemResponse)

---

### 4. アイテム削除

**エンドポイント**: `DELETE /api/shopping-lists/{itemId}`

**ヘッダー**: `X-User-Id: {userId}`

**レスポンス**: `204 No Content`

---

## Alerts API

### 1. アラート表示判定

**エンドポイント**: `GET /api/alerts/check`

**ヘッダー**: `X-User-Id: {userId}`

**説明**: 最終料理日から3日経過（4日目の0時）でアラートを表示します。

**レスポンス**: `200 OK`
```json
{
  "shouldShow": true,
  "message": "もう3日も料理していませんよ！"
}
```

**メッセージ種類**:
- 警告メッセージ（50%の確率）
  - "もう3日も料理していませんよ！"
  - "料理をサボっていませんか？"
  - "自炊習慣が途切れそうです！"
- 励ましメッセージ（50%の確率）
  - "今日は何か作ってみませんか？"
  - "簡単なレシピから始めてみましょう！"
  - "料理を再開して、健康的な生活を取り戻しましょう！"

---

## Admin API

### 1. ダッシュボード統計取得

**エンドポイント**: `GET /api/admin/dashboard`

**説明**: 管理者用のダッシュボード統計情報を取得します。

**レスポンス**: `200 OK`
```json
{
  "message": "管理者ダッシュボード",
  "totalUsers": 150,
  "totalRecipes": 320
}
```

---

### 2. ユーザー停止

**エンドポイント**: `PUT /api/admin/users/{userId}/suspend`

**説明**: ユーザーのログインを無効化します。

**レスポンス**: `204 No Content`

---

### 3. ユーザー削除

**エンドポイント**: `DELETE /api/admin/users/{userId}`

**説明**: ユーザーアカウントとデータを完全削除します。

**レスポンス**: `204 No Content`

---

### 4. すべてのレシピ取得（管理者用）

**エンドポイント**: `GET /api/admin/recipes`

**説明**: 審査待ちを含むすべてのレシピを取得します。

**レスポンス**: `200 OK` (RecipeResponse配列)

---

### 5. レシピステータス設定

**エンドポイント**: `PUT /api/admin/recipes/{recipeId}/status`

**リクエストボディ**:
```json
{
  "isPublic": false
}
```

**説明**: レシピの公開/非公開を設定します。

**レスポンス**: `200 OK` (RecipeResponse)

---

### 6. レシピ削除（管理者用）

**エンドポイント**: `DELETE /api/admin/recipes/{recipeId}`

**説明**: レシピを論理削除します（参照は保持）。

**レスポンス**: `204 No Content`

---

## 共通データモデル

### UserResponse
```json
{
  "userId": "string (UUID)",
  "email": "string",
  "nickname": "string (1-50文字)",
  "displayName": "string (1-50文字)",
  "profileImageUrl": "string (optional)",
  "preferredLanguage": "string (ja|ko)",
  "lastCookingDate": "string (ISO8601, optional)",
  "timezone": "string",
  "createdAt": "string (ISO8601)"
}
```

### RecipeResponse
```json
{
  "recipeId": "string (UUID)",
  "title": "string (最大100文字)",
  "authorId": "string (UUID)",
  "ingredients": [
    {
      "name": "string (最大100文字)",
      "quantity": "number (0.01-9999)",
      "unit": "string (単位コード)",
      "note": "string (最大200文字, optional)",
      "optional": "boolean"
    }
  ],
  "steps": ["string"],
  "cookingTime": "number (分)",
  "imageUrl": "string (optional)",
  "isPublic": "boolean",
  "createdAt": "string (ISO8601)",
  "updatedAt": "string (ISO8601)"
}
```

### 単位コード一覧
- `g`: グラム
- `kg`: キログラム
- `ml`: ミリリットル
- `l`: リットル
- `tbsp`: 大さじ
- `tsp`: 小さじ
- `cup`: カップ
- `piece`: 個
- `pack`: パック
- `can`: 缶
- `bottle`: 本
- `slice`: 枚
- `clove`: 片
- `pinch`: ひとつまみ
- `to_taste`: 適量
- `as_needed`: 必要に応じて

### ReviewResponse
```json
{
  "reviewId": "string (UUID)",
  "recipeId": "string (UUID)",
  "userId": "string (UUID)",
  "rating": "number (1-5)",
  "comment": "string (最大300文字, optional)",
  "status": "string (visible|hidden)",
  "reportedCount": "number",
  "createdAt": "string (ISO8601)",
  "updatedAt": "string (ISO8601)"
}
```

### ScheduleResponse
```json
{
  "scheduleId": "string (UUID)",
  "userId": "string (UUID)",
  "date": "string (YYYY-MM-DD)",
  "type": "string (PLANNED|COOKED)",
  "recipeId": "string (UUID)",
  "recipeTitle": "string",
  "memo": "string (最大120文字, optional)",
  "createdAt": "string (ISO8601)"
}
```

### ShoppingListItemResponse
```json
{
  "itemId": "string (UUID)",
  "userId": "string (UUID)",
  "name": "string (最大100文字)",
  "quantity": "number (0.01-9999)",
  "unit": "string (単位コード)",
  "isChecked": "boolean",
  "isCheckedAt": "string (ISO8601, optional)",
  "addedAt": "string (ISO8601)",
  "sourceRecipeId": "string (UUID, optional)"
}
```

---

## エラーレスポンス

### ErrorResponse
```json
{
  "timestamp": "2024-12-02T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "バリデーションエラー",
  "path": "/api/users/register"
}
```

### HTTPステータスコード

| コード | 説明 | 例 |
|--------|------|-----|
| 200 | OK | 正常なGET、PUT、POSTリクエスト |
| 201 | Created | リソースの作成成功 |
| 204 | No Content | 削除成功 |
| 400 | Bad Request | バリデーションエラー |
| 401 | Unauthorized | 認証エラー |
| 403 | Forbidden | 権限エラー |
| 404 | Not Found | リソースが見つからない |
| 409 | Conflict | リソースの重複 |
| 500 | Internal Server Error | サーバーエラー |

### エラーメッセージの多言語対応

`Accept-Language` ヘッダーに応じてエラーメッセージの言語が変わります。

**日本語（ja）**:
```json
{
  "message": "ユーザーが見つかりません"
}
```

**韓国語（ko）**:
```json
{
  "message": "사용자를 찾을 수 없습니다"
}
```

---

## バリデーションルール

### ユーザー関連
- **Email**: メール形式
- **Password**: 8文字以上、大文字・小文字・数字を含む
- **Nickname**: 1-50文字
- **DisplayName**: 1-50文字

### レシピ関連
- **Title**: 最大100文字
- **Ingredients**: 最低1つ
- **Steps**: 最低1つ
- **CookingTime**: 0以上

### 食材関連
- **Name**: 最大100文字
- **Quantity**: 0.01-9999
- **Note**: 最大200文字

### レビュー関連
- **Rating**: 1-5
- **Comment**: 最大300文字

### スケジュール関連
- **Date**: YYYY-MM-DD形式
- **Type**: "PLANNED" または "COOKED"
- **Memo**: 最大120文字

### 買い物リスト関連
- **Name**: 最大100文字
- **Quantity**: 0.01-9999

### 画像関連
- **ファイルサイズ**: 最大5MB
- **フォーマット**: JPEG、PNG

---

## レート制限

現在、レート制限は実装されていませんが、将来的にAPI Gatewayで以下の制限を設定予定：

- **レート制限**: 1000リクエスト/秒
- **バースト**: 2000リクエスト

---

## セキュリティ

### 認証
- AWS Cognitoを使用したJWT認証
- アクセストークンは各リクエストのAuthorizationヘッダーに含める

### 認可
- ユーザーは自分のデータのみアクセス可能
- 管理者APIは管理者権限が必要

### データ暗号化
- **転送時**: TLS 1.2以上
- **保存時**: DynamoDB暗号化（AWS KMS）

---

## 変更履歴

| バージョン | 日付 | 変更内容 |
|-----------|------|---------|
| 1.0.0 | 2024-12-02 | 初版作成 |

---

## サポート

質問や問題がある場合は、以下にお問い合わせください：
- **Email**: support@example.com
- **GitHub Issues**: https://github.com/example/cooking-app/issues
