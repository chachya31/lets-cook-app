# API仕様書

## 概要

自炊支援・食費節約アプリケーションのREST API仕様書です。

詳細なエンドポイント定義は [openapi.yaml](./openapi.yaml) を参照してください。

### ベースURL
| 環境         | URL                           |
| ------------ | ----------------------------- |
| ローカル開発 | `http://localhost:8080`       |
| 開発環境     | `https://api-dev.example.com` |
| 本番環境     | `https://api.example.com`     |

### 多言語対応
- 対応言語: 日本語（ja）、韓国語（ko）
- ヘッダー: `Accept-Language: ja` または `Accept-Language: ko`

### 共通ヘッダー
| ヘッダー          | 説明                                     | 必須 |
| ----------------- | ---------------------------------------- | ---- |
| `Content-Type`    | `application/json`                       | ○    |
| `Accept-Language` | 言語設定（ja/ko）                        | -    |
| `X-User-Id`       | ユーザーID（認証が必要なエンドポイント） | △    |

---

## 認証フロー

### AWS Cognito認証

```
┌─────────┐     ┌─────────┐     ┌─────────┐
│ Client  │     │ Backend │     │ Cognito │
└────┬────┘     └────┬────┘     └────┬────┘
     │               │               │
     │ 1. Register   │               │
     ├──────────────►│               │
     │               │ 2. SignUp     │
     │               ├──────────────►│
     │               │               │
     │ 3. Confirm    │               │
     ├──────────────►│               │
     │               │ 4. ConfirmSignUp
     │               ├──────────────►│
     │               │               │
     │ 5. Login      │               │
     ├──────────────►│               │
     │               │ 6. InitiateAuth
     │               ├──────────────►│
     │               │◄──────────────┤
     │◄──────────────┤ 7. Tokens     │
     │               │               │
```

### トークン仕様
| トークン             | 有効期限 | 用途         |
| -------------------- | -------- | ------------ |
| アクセストークン     | 1時間    | API認証      |
| リフレッシュトークン | 90日     | トークン更新 |
| IDトークン           | 1時間    | ユーザー情報 |

---

## API一覧

詳細なリクエスト/レスポンス仕様は [openapi.yaml](./openapi.yaml) を参照してください。

### Users API
| メソッド | エンドポイント                | 説明                         |
| -------- | ----------------------------- | ---------------------------- |
| POST     | `/api/users/register`         | ユーザー登録                 |
| POST     | `/api/users/confirm`          | メール確認                   |
| POST     | `/api/users/resend-code`      | 確認コード再送信             |
| POST     | `/api/users/login`            | ログイン                     |
| GET      | `/api/users/profile/{userId}` | プロフィール取得             |
| PUT      | `/api/users/profile/{userId}` | プロフィール更新             |
| DELETE   | `/api/users/account/{userId}` | アカウント削除               |
| POST     | `/api/users/profile/image`    | プロフィール画像アップロード |

### Recipes API
| メソッド | エンドポイント            | 説明                   |
| -------- | ------------------------- | ---------------------- |
| GET      | `/api/recipes`            | レシピ検索             |
| POST     | `/api/recipes`            | レシピ作成             |
| GET      | `/api/recipes/{id}`       | レシピ詳細取得         |
| PUT      | `/api/recipes/{id}`       | レシピ更新             |
| DELETE   | `/api/recipes/{id}`       | レシピ削除（論理削除） |
| POST     | `/api/recipes/{id}/image` | レシピ画像アップロード |

### Reviews API
| メソッド | エンドポイント                    | 説明             |
| -------- | --------------------------------- | ---------------- |
| GET      | `/api/recipes/{recipeId}/reviews` | レビュー一覧取得 |
| POST     | `/api/recipes/{recipeId}/reviews` | レビュー作成     |
| PUT      | `/api/reviews/{reviewId}`         | レビュー更新     |
| DELETE   | `/api/reviews/{reviewId}`         | レビュー削除     |
| POST     | `/api/reviews/{reviewId}/report`  | レビュー通報     |

### Schedules API
| メソッド | エンドポイント                                  | 説明                 |
| -------- | ----------------------------------------------- | -------------------- |
| GET      | `/api/schedules`                                | スケジュール一覧取得 |
| POST     | `/api/schedules`                                | スケジュール作成     |
| PUT      | `/api/schedules/{scheduleId}`                   | スケジュール更新     |
| DELETE   | `/api/schedules/{scheduleId}`                   | スケジュール削除     |
| POST     | `/api/schedules/{scheduleId}/convert-to-cooked` | 予定を実績に変換     |

### Shopping Lists API
| メソッド | エンドポイント                 | 説明             |
| -------- | ------------------------------ | ---------------- |
| GET      | `/api/shopping-lists`          | 買い物リスト取得 |
| POST     | `/api/shopping-lists`          | アイテム追加     |
| PUT      | `/api/shopping-lists/{itemId}` | アイテム更新     |
| DELETE   | `/api/shopping-lists/{itemId}` | アイテム削除     |

### Alerts API
| メソッド | エンドポイント      | 説明             |
| -------- | ------------------- | ---------------- |
| GET      | `/api/alerts/check` | アラート表示判定 |

### Admin API
| メソッド | エンドポイント                         | 説明                   |
| -------- | -------------------------------------- | ---------------------- |
| GET      | `/api/admin/dashboard`                 | ダッシュボード統計取得 |
| PUT      | `/api/admin/users/{userId}/suspend`    | ユーザー停止           |
| DELETE   | `/api/admin/users/{userId}`            | ユーザー削除           |
| GET      | `/api/admin/recipes`                   | すべてのレシピ取得     |
| PUT      | `/api/admin/recipes/{recipeId}/status` | レシピステータス設定   |
| DELETE   | `/api/admin/recipes/{recipeId}`        | レシピ削除             |

---

## エラーハンドリング

### HTTPステータスコード
| コード | 説明                  | 例                             |
| ------ | --------------------- | ------------------------------ |
| 200    | OK                    | 正常なGET、PUT、POSTリクエスト |
| 201    | Created               | リソースの作成成功             |
| 204    | No Content            | 削除成功                       |
| 400    | Bad Request           | バリデーションエラー           |
| 401    | Unauthorized          | 認証エラー                     |
| 403    | Forbidden             | 権限エラー                     |
| 404    | Not Found             | リソースが見つからない         |
| 409    | Conflict              | リソースの重複                 |
| 500    | Internal Server Error | サーバーエラー                 |

### エラーレスポンス形式
```json
{
  "timestamp": "2024-12-02T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "バリデーションエラー",
  "path": "/api/users/register"
}
```

### 多言語エラーメッセージ
`Accept-Language` ヘッダーに応じてエラーメッセージの言語が変わります。

---

## バリデーションルール

詳細なバリデーションルールは [openapi.yaml](./openapi.yaml) の各スキーマ定義を参照してください。

### 主要なルール
| 項目                 | ルール                                |
| -------------------- | ------------------------------------- |
| Email                | メール形式                            |
| Password             | 8文字以上、大文字・小文字・数字を含む |
| Nickname/DisplayName | 1-50文字                              |
| Recipe Title         | 最大100文字                           |
| Rating               | 1-5                                   |
| Comment              | 最大300文字                           |
| Memo                 | 最大120文字                           |
| 画像ファイル         | JPEG/PNG、最大5MB                     |

---

## セキュリティ

### 認証・認可
- AWS Cognitoを使用したJWT認証
- ユーザーは自分のデータのみアクセス可能
- 管理者APIは管理者権限が必要

### データ暗号化
| 種類   | 方式                      |
| ------ | ------------------------- |
| 転送時 | TLS 1.2以上               |
| 保存時 | DynamoDB暗号化（AWS KMS） |

---

## 関連ドキュメント

- [OpenAPI仕様](./openapi.yaml) - 詳細なAPI定義
- [データベース設計](./DATABASE_DESIGN.md) - テーブル設計・単位コード一覧
- [要件定義書](./要件定義書_自炊支援アプリ.md) - 機能要件

---

## 変更履歴

| バージョン | 日付       | 変更内容                     |
| ---------- | ---------- | ---------------------------- |
| 1.0.0      | 2024-12-02 | 初版作成                     |
| 1.1.0      | 2024-12-18 | openapi.yamlへ詳細仕様を移行 |
