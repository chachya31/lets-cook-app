# Webアプリケーション要件定義書

**プロジェクト名**: 自炊支援・食費節約アプリ（仮称）  
**ドキュメント版数**: 1.0  
**作成日**: 2024/05/XX

---

## 1. 概要

### 1.1 目的

- **食費の節約**: Uber Eatsなどのデリバリーサービス依存からの脱却を支援する。
- **自炊のハードル低下**: 簡単なレシピを提供し、料理への心理的ハードルを下げる。
- **食品ロス削減**: 食材の賞味期限や保管方法を提示し、計画的な買い物を促進する。

### 1.2 ターゲットユーザー

- 一人暮らし、または2〜4人家族の世帯。
- 食費を抑えたいが、献立を考えるのが面倒な層。

### 1.3 対象範囲 (Scope)

**主要機能**:
- レシピ検索・編集
- スケジュール管理（実績記録）
- 買い物リスト
- アラート表示

**アラート仕様**:
- 3日間以上料理実績がない場合、Webサイト内（ログイン時モーダル等）で警告を表示。

**対象外機能**:
- レシピの登録審査（次フェーズ）
- 外部へのアラート通知（メール、LINE等）
- 高度な栄養計算
- EC（食材購入）連携

---

## 2. 機能要件

### 2.1 ユーザー管理機能

**基本機能**:
- 新規登録、ログイン、ログアウト
- プロフィール編集（画像アップロード含む）
- パスワードリセット、変更

**画像アップロード制限**:
- **ファイルサイズ上限**: 5MB
- **対応フォーマット**: JPEG, PNGを必須とし、WebPは対応を検討
- **トリミング仕様**: アスペクト比自由（トリミング機能はPhase 2で実装）

**セッション管理**:
- **有効期限**: Cognitoの標準設定に準拠（アクセストークン: 1時間、リフレッシュトークン: 90日）
- **自動ログアウト**: リフレッシュトークン失効時にログイン画面へ遷移
- **複数デバイス**: 同時ログインを許可

**ユーザー退会**:
- アカウントおよびプロフィール画像は即座に削除
- 投稿したレシピと、そのレシピへの評価・コメントは、匿名化（anonymous）して残す

### 2.2 レシピ機能

**レシピ検索**:
- カテゴリ、食材、調理時間などでの検索。

**レシピ登録・編集**:
- タイトル、食材、手順、画像（S3連携）の登録
- 食材は名前、数量、単位、メモ（任意）、省略可フラグ（任意）を含む

**レシピ詳細**:
- 手順の閲覧、AIアドバイザー機能へのアクセス

**レシピ削除時の挙動**:
- そのレシピを参照しているスケジュールおよび買い物リストのデータは削除せず残す
- レシピIDは残るが、詳細画面では「削除されたレシピです」と表示

**AIアドバイザー**:
- Google Gemini API等を活用
- 選択した食材に対し「日持ちする保存法」や「代替食材」を提示

**エラーハンドリング**:
- **API失敗時**: 直近24時間のキャッシュがあれば表示、なければ「再試行」ボタン＋簡易静的ヒントを表示
- **レート制限時**: 30秒後の自動再試行＋手動リトライボタンを提供。画面上部に「混み合っています」バナーを表示

**レシピ評価**:
- 星評価（5段階）やコメントの投稿
- **閲覧**: ログインユーザーなら誰でも閲覧可能
- **編集・削除**: 自分の評価は編集・削除が可能
- **通報機能**: 簡易「通報する」ボタンを設置。通報件数をカウントし、3件以上で自動非表示

### 2.3 スケジュール管理機能

**カレンダー表示**:
- 月次/週次での料理予定・実績の確認

**実績登録**:
- 「料理した日」をワンタップで登録
- 実績登録時、LastCookingDateを即時更新

**予定と実績の関係**:
- **予定 (PLANNED)**: カレンダーにマークする機能
- **実績 (COOKED)**: アラート判定に用いる最終的な記録
- **実績登録（予定なし）**: 予定を立てていなくても、実績（COOKED）のみ登録可能
- **予定→実績変換**: 予定日のセルをタップし「実績として登録」ボタンを押すことで、その日のSchedulesレコードのStatusをCOOKEDに上書き
- **過去の編集・削除**: 過去の実績（COOKED）および予定（PLANNED）は、いつでも編集・削除が可能

**サボり防止アラート**:
- 最終料理日から3日目の0時（当日を含め4日目）にアラート表示
- 例：月曜日に料理した場合、木曜日の0時以降にアラートが表示される
- **トリガー**: ダッシュボード表示時に判定し、該当ならモーダル表示
- **再表示制御**: 同日の再表示なし（localStorageに当日フラグを保存）
- **解除**: 「今日は作った」ボタンでCOOKED登録→即座に非表示

### 2.4 買い物リスト管理機能

**基本機能**:
- レシピ詳細画面から、不足食材をリストに追加
- リスト内の項目チェック（購入済みステータス変更）、削除

**追加時の数量合算**:
- 同じ食材名＋同じ単位なら数量を自動合算
- 単位が異なる場合は別行として追加（後で手動編集可能）
- 合算キー: `normalize(name) + ":" + unit`

**削除タイミング**:
- 購入済み（IsChecked=true）のアイテムは、購入から3日後に自動削除

### 2.5 在庫管理機能

**基本機能**:
- 購入した食材の在庫を管理
- 買い物リストでチェックしたアイテムを在庫に自動追加
- 賞味期限の設定・編集

**賞味期限管理**:
- 賞味期限の任意設定（未設定可）
- 賞味期限切れの視覚的表示（赤色バッジ）
- 賞味期限間近（3日以内）の視覚的表示（黄色バッジ）
- 賞味期限順でのソート表示

**在庫の活用**:
- AIチャット機能との連携（レシピ推薦、賞味期限確認）
- 在庫一覧からAIチャットへの食材追加

### 2.6 AIチャット機能

**基本機能**:
- Google Gemini APIを活用した対話型AIアシスタント
- 会話履歴の保存・管理
- 複数の会話セッション対応

**会話タイプ**:
- **一般チャット**: 料理に関する一般的な質問・相談
- **レシピ推薦**: 在庫の食材を基にしたレシピ提案
- **賞味期限確認**: 在庫の賞味期限に関するアドバイス

**在庫連携**:
- レシピ推薦・賞味期限確認時に在庫パネルを表示
- 在庫アイテムをクリックして入力欄に追加
- 全在庫を一括で入力欄に追加

**会話管理**:
- 会話一覧の表示（更新日時順）
- 会話の削除
- 会話タイトルの自動生成

### 2.7 管理機能（管理者向け）

- ダッシュボード（全ユーザーの統計など）
- ユーザー管理（アカウント停止・削除）
- レシピ管理（不適切なレシピの削除、公開/非公開審査）

### 2.8 多言語対応機能

**対応言語**:
- 日本語（既定言語）
- 韓国語

**多言語化の範囲**:
- **UI要素**: ボタン、ラベル、メニュー、エラーメッセージ、バリデーションメッセージ
- **システムメッセージ**: アラート文、通知、ガイダンステキスト
- **静的コンテンツ**: ヘルプテキスト、利用規約、プライバシーポリシー

**多言語化の対象外**:
- **ユーザー生成コンテンツ**: レシピタイトル、食材名、手順、コメント（ユーザーが入力した言語のまま表示）
- **AIアドバイザーの応答**: APIの応答言語に依存（将来的に多言語化を検討）

**言語切り替え**:
- プロフィール設定で言語を選択可能
- 選択した言語設定はUsersテーブルに保存（PreferredLanguage属性）
- ブラウザの言語設定を初期値として使用（未設定時）

**実装方式**:
- **フロントエンド**: i18next等の国際化ライブラリを使用
- **バックエンド**: エラーメッセージやバリデーションメッセージは、リクエストヘッダー（Accept-Language）に基づいて適切な言語で返却
- **翻訳ファイル管理**: JSON形式で言語ごとに管理（例: `ja.json`, `ko.json`）

---

## 3. 非機能要件

### 3.1 性能・拡張性

**応答時間**:
- 90%のリクエストを2秒以内に完了。

**同時接続数**:
- 想定 10名程度（初期フェーズ）。

**アーキテクチャ**:
- **バックエンド**: Java (Spring Boot) / Clean Architecture採用。
- **フロントエンド**: TypeScript (React, React Router, Redux)。
- **インフラ**: AWSサーバーレス構成（運用負荷軽減）。

### 3.2 セキュリティ

**認証**:
- AWS Cognito（メールアドレス/パスワード）。

**パスワード要件**:
- 8文字以上、大文字・小文字・数字を含む。

**通信**:
- 全経路HTTPS（TLS 1.2以上）必須。

### 3.3 動作環境

**クライアント**:
- PC/スマホブラウザ（Chrome, Safari, Edge）
- 対応言語: 日本語、韓国語

**データベース**:
- Amazon DynamoDB

**ストレージ**:
- Amazon S3（画像用）

### 3.5 国際化・ローカライゼーション

**文字エンコーディング**:
- UTF-8を使用（日本語、韓国語の文字を正しく表示）

**日付・時刻フォーマット**:
- 日本語: YYYY年MM月DD日 HH:mm
- 韓国語: YYYY년 MM월 DD일 HH:mm

**数値フォーマット**:
- 日本語: 1,234.56
- 韓国語: 1,234.56

**通貨表示**（将来的な拡張用）:
- 日本語: ¥1,234
- 韓国語: ₩1,234

### 3.4 運用・保守

**ロギング**:
- Amazon CloudWatch (INFOレベル以上)。

**エラーハンドリング**:
- APIエラー時やAIレート制限時は、ユーザーフレンドリーなメッセージを表示。

---

## 4. UI/UX・画面遷移

### 4.1 デザインコンセプト

**テーマカラー**:
- 明るい緑 (#明るい緑のカラーコード) を基調とし、健康的でポジティブな印象を与える。

### 4.2 画面遷移フロー (Mermaid)

```mermaid
graph TD
    %% 認証エリア
    Start((開始)) --> Login[ログイン画面]
    Login --> |未登録| Register[ユーザー登録画面]
    Login --> |PW忘れ| PwReset[PW再発行画面]
    Register --> Verify[検証コード入力]
    Verify --> Login
    
    %% メインエリア
    Login --> |認証成功| Dashboard[ダッシュボード/ホーム]
    
    %% ダッシュボードからの分岐
    Dashboard --> |実績なし3日以上| AlertModal[アラートモーダル]
    Dashboard --> Schedule[スケジュール管理]
    Dashboard --> RecipeSearch[レシピ検索]
    Dashboard --> ShoppingList[買い物リスト]
    Dashboard --> Inventory[在庫管理]
    Dashboard --> AIChat[AIチャット]
    Dashboard --> Profile[プロフィール編集]
    
    %% レシピ関連フロー
    RecipeSearch --> |選択| RecipeDetail[レシピ詳細]
    RecipeDetail --> |編集(自作のみ)| RecipeEdit[レシピ編集]
    RecipeDetail --> |食材追加| ShoppingList
    RecipeDetail --> |評価| Review[評価入力]
    
    %% 在庫・AIチャット連携
    ShoppingList --> |チェック| Inventory
    Inventory --> |食材選択| AIChat
    AIChat --> |レシピ推薦| RecipeDetail
    
    %% 管理者エリア
    Login --> |管理者| AdminDash[管理ダッシュボード]
    AdminDash --> UserMgmt[ユーザー管理]
    AdminDash --> RecipeMgmt[レシピ審査・管理]
```

---

## 5. データモデル設計

データベース設計の詳細は **[DATABASE_DESIGN.md](./DATABASE_DESIGN.md)** を参照してください。

本セクションでは要件定義に関連する概要のみ記載します。

### 5.1 テーブル一覧

| テーブル名        | 用途                     |
| ----------------- | ------------------------ |
| Users             | ユーザー基本情報の管理   |
| Recipes           | レシピ情報の格納         |
| Schedules         | 料理の実績・予定管理     |
| ShoppingLists     | 買い物リスト             |
| Reviews           | レシピ評価・コメント管理 |
| Inventory         | 食材在庫管理             |
| ChatConversations | AIチャット会話管理       |
| ChatMessages      | AIチャットメッセージ管理 |

詳細なテーブル構造、属性、インデックス、アクセスパターンについては [DATABASE_DESIGN.md](./DATABASE_DESIGN.md) を参照してください。

---

## 6. 開発・運用要件詳細

### 6.1 テスト計画

**単体テスト**:
- JUnitを使用（カバレッジ目標を設定することを推奨）。

**結合テスト**:
- 開発者自身によるAPI疎通確認およびシナリオテスト。

### 6.2 納品物

- ソースコード一式（Gitリポジトリ）
- 要件定義書（本書）
- 基本設計書（API仕様書/Swagger、ER図/テーブル定義書）
- テスト仕様書・結果報告書

---

## 7. 今後の検討事項 (Pending)

### 外部連携

- アラート機能の通知手段拡張（LINE Messaging API等の導入検討）。

### 機能拡張

- レシピの登録審査機能（次フェーズ）
- 画像トリミング機能（Phase 2で実装）
- 栄養計算機能
- EC（食材購入）連携


# API仕様書

## 概要

自炊支援・食費節約アプリケーションのREST API仕様書です。

### 📖 API仕様の閲覧方法

詳細なエンドポイント定義は以下の方法で確認できます：

- **[openapi.yaml](./openapi.yaml)** - OpenAPI 3.0.3仕様ファイル
- **[Swagger Editor](https://editor.swagger.io/?url=https://raw.githubusercontent.com/chachya31/lets-cook-app/kiro-docs/docs/openapi.yaml)** - オンラインプレビュー（要URL更新）
- **[Swagger UI](./index.html)** - ローカルプレビュー（GitHub Pages対応）

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
| ヘッダー          | 説明                                               | 必須 |
| ----------------- | -------------------------------------------------- | ---- |
| `Content-Type`    | `application/json`                                 | ○    |
| `Accept-Language` | 言語設定（ja/ko）                                  | -    |
| `Authorization`   | Bearer {accessToken}（認証が必要なエンドポイント） | △    |

> **Note**: 以前使用していた `X-User-Id` ヘッダーは廃止されました。ユーザー識別はJWTトークン（SecurityContext）から取得します。

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
| メソッド | エンドポイント                          | 説明                        |
| -------- | --------------------------------------- | --------------------------- |
| GET      | `/api/recipes`                          | レシピ検索                  |
| POST     | `/api/recipes`                          | レシピ作成                  |
| POST     | `/api/recipes/with-images`              | レシピ作成（画像付き）      |
| GET      | `/api/recipes/{id}`                     | レシピ詳細取得              |
| PUT      | `/api/recipes/{id}`                     | レシピ更新                  |
| DELETE   | `/api/recipes/{id}`                     | レシピ削除（論理削除）      |
| POST     | `/api/recipes/{id}/image`               | レシピ画像アップロード      |
| POST     | `/api/recipes/{id}/steps/{index}/image` | 手順画像アップロード        |
| POST     | `/api/recipes/search/by-ingredients`    | 食材でレシピ検索（AND条件） |

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

### セキュリティ

### 認証・認可
- AWS Cognitoを使用したJWT認証
- **ユーザーIDはJWTトークンのsubクレームから取得**（SecurityContextで管理）
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

| バージョン | 日付       | 変更内容                                         |
| ---------- | ---------- | ------------------------------------------------ |
| 1.0.0      | 2024-12-02 | 初版作成                                         |
| 1.1.0      | 2024-12-18 | openapi.yamlへ詳細仕様を移行                     |
| 1.2.0      | 2024-12-23 | 手順画像・動画アップロードエンドポイント追加     |
| 1.3.0      | 2024-12-24 | 食材検索エンドポイント追加                       |
| 1.4.0      | 2026-02-03 | X-User-Idヘッダー廃止、SecurityContext認証に移行 |
# データベース設計書

## 概要

本アプリケーションは、Amazon DynamoDBをデータストアとして使用します。DynamoDBはNoSQLデータベースであり、スケーラビリティと高可用性を提供します。

## テーブル一覧

| テーブル名        | 用途                   | Partition Key  | Sort Key       | GSI               |
| ----------------- | ---------------------- | -------------- | -------------- | ----------------- |
| Users             | ユーザー情報           | UserId         | -              | -                 |
| Recipes           | レシピ情報             | RecipeId       | -              | GSI_Author        |
| RecipeIngredients | 食材逆引きインデックス | IngredientName | RecipeId       | -                 |
| Reviews           | レビュー情報           | RecipeId       | ReviewId       | GSI_User          |
| Schedules         | スケジュール情報       | UserId         | DateRecipeId   | -                 |
| ShoppingLists     | 買い物リスト           | UserId         | ItemId         | GSI_NormalizedKey |
| Inventory         | 食材在庫               | UserId         | ItemId         | GSI_ExpiryDate    |
| ChatConversations | AIチャット会話         | UserId         | ConversationId | -                 |
| ChatMessages      | AIチャットメッセージ   | ConversationId | MessageId      | -                 |

---

## 1. Users テーブル

### 概要
ユーザーアカウント情報とプロフィールを管理するテーブル。

### キー構造
- **Partition Key**: `UserId` (String, Cognito sub)
- **Sort Key**: なし

### 属性

| 属性名            | 型      | 必須 | 説明                            | 例                                     |
| ----------------- | ------- | ---- | ------------------------------- | -------------------------------------- |
| UserId            | String  | ✓    | ユーザーID（Cognito sub）       | "a1b2c3d4-e5f6-7890-abcd-ef1234567890" |
| Email             | String  | ✓    | メールアドレス                  | "user@example.com"                     |
| Nickname          | String  | ✓    | ニックネーム（1-50文字）        | "山田太郎"                             |
| DisplayName       | String  | ✓    | 表示名（1-50文字）              | "Taro Yamada"                          |
| ProfileImageUrl   | String  |      | プロフィール画像URL             | "https://s3.../profile.jpg"            |
| PreferredLanguage | String  | ✓    | 優先言語（"ja" or "ko"）        | "ja"                                   |
| LastCookingDate   | String  |      | 最終料理日（ISO8601形式）       | "2024-12-01"                           |
| LastLoginDate     | String  |      | 最終ログイン日時（ISO8601形式） | "2024-12-02T10:30:00"                  |
| CreatedAt         | String  | ✓    | 作成日時（ISO8601形式）         | "2024-11-01T09:00:00"                  |
| Timezone          | String  | ✓    | タイムゾーン                    | "Asia/Tokyo"                           |
| MarketingOptOut   | Boolean | ✓    | マーケティング配信拒否フラグ    | false                                  |

### インデックス
なし

### アクセスパターン
1. **ユーザーIDで検索**: `UserId` で直接取得（GetItem）
2. **メールアドレスで検索**: `Email` でスキャン（Scan）
   - ⚠️ 本番環境では GSI の追加を推奨

### 備考
- Cognitoと連携してユーザー認証を管理
- **UserIdはCognito subを使用**（ユーザー登録時にCognitoから取得したsubをそのまま使用）
- `LastCookingDate` はサボり防止アラート機能で使用
- メールアドレスでの検索は現在Scanを使用しているため、ユーザー数が増加した場合はGSIの追加が必要

---

## 2. Recipes テーブル

### 概要
レシピ情報を管理するテーブル。食材、手順、画像URLなどを含む。

### キー構造
- **Partition Key**: `RecipeId` (String, UUID)
- **Sort Key**: なし

### 属性

| 属性名      | 型        | 必須 | 説明                          | 例                                     |
| ----------- | --------- | ---- | ----------------------------- | -------------------------------------- |
| RecipeId    | String    | ✓    | レシピID（UUID）              | "660e8400-e29b-41d4-a716-446655440001" |
| Title       | String    | ✓    | レシピタイトル（最大100文字） | "簡単カレーライス"                     |
| AuthorId    | String    | ✓    | 作成者ID（Cognito sub）       | "a1b2c3d4-e5f6-7890-abcd-ef1234567890" |
| Ingredients | List<Map> | ✓    | 食材リスト                    | 下記参照                               |
| Steps       | List<Map> | ✓    | 調理手順リスト                | 下記参照                               |
| CookingTime | Number    | ✓    | 調理時間（分）                | 30                                     |
| ImageUrl    | String    |      | レシピ画像URL                 | "https://s3.../recipe.jpg"             |
| IsPublic    | Boolean   | ✓    | 公開フラグ                    | true                                   |
| IsDeleted   | Boolean   | ✓    | 論理削除フラグ                | false                                  |
| CreatedAt   | String    | ✓    | 作成日時（ISO8601形式）       | "2024-11-15T14:30:00"                  |
| UpdatedAt   | String    | ✓    | 更新日時（ISO8601形式）       | "2024-11-20T16:45:00"                  |

### Ingredients（食材）の構造

各食材は以下の属性を持つMapオブジェクト：

| 属性名   | 型      | 必須 | 説明                         | 例         |
| -------- | ------- | ---- | ---------------------------- | ---------- |
| name     | String  | ✓    | 食材名（最大100文字）        | "玉ねぎ"   |
| quantity | Number  |      | 数量（0.01-9999、任意）      | 2          |
| unit     | String  |      | 単位（最大50文字、自由入力） | "個"       |
| note     | String  |      | メモ（最大200文字）          | "中サイズ" |
| optional | Boolean | ✓    | 任意フラグ                   | false      |

※ quantity と unit は任意項目です。「適量」「少々」など数量を指定しない食材に対応しています。

### Steps（手順）の構造

各手順は以下の属性を持つMapオブジェクト：

| 属性名      | 型     | 必須 | 説明                                   | 例                                    |
| ----------- | ------ | ---- | -------------------------------------- | ------------------------------------- |
| description | String | ✓    | 手順の説明                             | "野菜を一口大に切る"                  |
| imageUrl    | String |      | 手順の画像URL（任意）                  | "https://s3.../step1.jpg"             |
| videoUrl    | String |      | 手順の動画URL（任意、YouTube等に対応） | "https://www.youtube.com/watch?v=..." |

※ 旧形式（文字列のみ）との後方互換性を維持しています。

| slice | 枚 | 枚 |
| clove | 片 | 片 |
| pinch | ひとつまみ | ひとつまみ |
| to_taste | 適量 | 適量 |
| as_needed | 必要に応じて | 必要に応じて |

### インデックス

#### GSI_Author
- **Partition Key**: `AuthorId` (String)
- **用途**: 作成者別のレシピ検索

### アクセスパターン
1. **レシピIDで検索**: `RecipeId` で直接取得（GetItem）
2. **作成者別検索**: `GSI_Author` を使用して `AuthorId` で検索（Query）
3. **公開レシピ一覧**: `IsPublic = true AND IsDeleted = false` でスキャン（Scan）
4. **キーワード検索**: アプリケーション層でフィルタリング

### 備考
- 論理削除を採用（`IsDeleted` フラグ）
- スケジュールと買い物リストからの参照を保持するため、物理削除は行わない
- 食材と手順はJSON形式でシリアライズして保存
- レシピの保存・更新時は `RecipeIngredients` テーブルとトランザクションで同期

---

## 2.1 RecipeIngredients テーブル（逆引きインデックス）

### 概要
食材名からレシピを検索するための逆引きインデックステーブル。「冷蔵庫にある食材でレシピを探す」機能で使用。

### キー構造
- **Partition Key**: `IngredientName` (String, 正規化済み)
- **Sort Key**: `RecipeId` (String, UUID)

### 属性

| 属性名         | 型     | 必須 | 説明                     | 例                                     |
| -------------- | ------ | ---- | ------------------------ | -------------------------------------- |
| IngredientName | String | ✓    | 食材名（正規化済み）     | "玉ねぎ"                               |
| RecipeId       | String | ✓    | レシピID                 | "660e8400-e29b-41d4-a716-446655440001" |
| RecipeTitle    | String | ✓    | レシピタイトル（参照用） | "簡単カレーライス"                     |
| RecipeImageUrl | String |      | レシピ画像URL（参照用）  | "https://s3.../recipe.jpg"             |

### インデックス
なし（Partition Keyでの検索で十分）

### アクセスパターン
1. **食材名でレシピ検索**: `IngredientName` で検索（Query）
2. **複数食材のAND検索**: アプリケーション層で各食材のレシピIDを取得し、交差集合を計算

### トランザクション処理
レシピの保存・更新・削除時は `TransactWriteItems` を使用して `Recipes` テーブルと `RecipeIngredients` テーブルを原子的に更新：

```
┌─────────────────────────────────────────────────────────────┐
│ TransactWriteItems                                          │
├─────────────────────────────────────────────────────────────┤
│ 1. Put/Update Recipe in Recipes table                       │
│ 2. Delete old RecipeIngredients (removed ingredients)       │
│ 3. Put new RecipeIngredients (added ingredients)            │
└─────────────────────────────────────────────────────────────┘
```

### 備考
- 非正規化データ（RecipeTitle, RecipeImageUrl）を含むため、検索結果を即座に表示可能
- レシピ更新時は差分計算を行い、削除された食材のみ削除、追加された食材のみ追加
- 食材名は `trim()` で正規化（将来的にカタカナ/ひらがな統一を追加予定）

---

## 3. Reviews テーブル

### 概要
レシピに対するレビュー（星評価とコメント）を管理するテーブル。

### キー構造
- **Partition Key**: `RecipeId` (String)
- **Sort Key**: `ReviewId` (String, UUID)

### 属性

| 属性名        | 型     | 必須 | 説明                                | 例                                     |
| ------------- | ------ | ---- | ----------------------------------- | -------------------------------------- |
| RecipeId      | String | ✓    | レシピID                            | "660e8400-e29b-41d4-a716-446655440001" |
| ReviewId      | String | ✓    | レビューID（UUID）                  | "770e8400-e29b-41d4-a716-446655440002" |
| UserId        | String | ✓    | レビュー投稿者ID（Cognito sub）     | "a1b2c3d4-e5f6-7890-abcd-ef1234567890" |
| Rating        | Number | ✓    | 星評価（1-5）                       | 5                                      |
| Comment       | String |      | コメント（最大300文字）             | "とても美味しかったです！"             |
| Status        | String | ✓    | ステータス（"visible" or "hidden"） | "visible"                              |
| ReportedCount | Number | ✓    | 通報カウント                        | 0                                      |
| CreatedAt     | String | ✓    | 作成日時（ISO8601形式）             | "2024-11-16T10:00:00Z"                 |
| UpdatedAt     | String | ✓    | 更新日時（ISO8601形式）             | "2024-11-16T10:00:00Z"                 |

### ステータス値

| 値      | 説明                           |
| ------- | ------------------------------ |
| visible | 表示可能                       |
| hidden  | 非表示（通報により自動非表示） |

### インデックス

#### GSI_User
- **Partition Key**: `UserId` (String)
- **Sort Key**: `CreatedAt` (String)
- **用途**: ユーザー別のレビュー一覧取得

### アクセスパターン
1. **レシピ別レビュー一覧**: `RecipeId` で検索（Query）
2. **ユーザー別レビュー一覧**: `GSI_User` を使用して `UserId` で検索（Query）
3. **レビューID検索**: `ReviewId` でスキャン（Scan）
   - ⚠️ 非効率なため、本番環境では改善が必要

### 備考
- 通報カウントが3以上になると自動的に `Status` が "hidden" に変更される
- 編集・削除は投稿者本人のみ可能
- 表示時は `Status = "visible"` のレビューのみフィルタリング

---

## 4. Schedules テーブル

### 概要
料理予定と実績を管理するテーブル。カレンダー機能で使用。

### キー構造
- **Partition Key**: `UserId` (String)
- **Sort Key**: `DateRecipeId` (String, 複合キー)
  - 形式: `{Date}#{RecipeId}`
  - 例: `2024-12-01#660e8400-e29b-41d4-a716-446655440001`

### 属性

| 属性名       | 型      | 必須 | 説明                                | 例                                     |
| ------------ | ------- | ---- | ----------------------------------- | -------------------------------------- |
| UserId       | String  | ✓    | ユーザーID（Cognito sub）           | "a1b2c3d4-e5f6-7890-abcd-ef1234567890" |
| DateRecipeId | String  | ✓    | 複合ソートキー                      | "2024-12-01#660e..."                   |
| ScheduleId   | String  | ✓    | スケジュールID（UUID）              | "880e8400-e29b-41d4-a716-446655440003" |
| Date         | String  | ✓    | 日付（YYYY-MM-DD形式）              | "2024-12-01"                           |
| RecipeId     | String  | ✓    | レシピID                            | "660e8400-e29b-41d4-a716-446655440001" |
| RecipeTitle  | String  | ✓    | レシピタイトル（参照用）            | "簡単カレーライス"                     |
| IsDone       | Boolean | ✓    | 完了フラグ（false=予定、true=実績） | false                                  |
| Memo         | String  |      | メモ（最大120文字）                 | "夕食用"                               |
| CreatedAt    | String  | ✓    | 作成日時（ISO8601形式）             | "2024-11-30T15:00:00Z"                 |

### インデックス
なし

### アクセスパターン
1. **日付範囲検索**: `UserId` と `DateRecipeId` の範囲検索（Query）
   - 例: 2024-12-01 から 2024-12-07 までのスケジュール
2. **スケジュールID検索**: `ScheduleId` でスキャン（Scan）
   - ⚠️ 非効率だが、IDでの検索は稀なため許容

### 備考
- Sort Keyに日付を含めることで、日付範囲での効率的な検索が可能
- `IsDone`フラグをtrueにすることで予定を実績に変換
- 実績登録時は `Users` テーブルの `LastCookingDate` も更新

---

## 5. ShoppingLists テーブル

### 概要
買い物リストのアイテムを管理するテーブル。

### キー構造
- **Partition Key**: `UserId` (String)
- **Sort Key**: `ItemId` (String, UUID)

### 属性

| 属性名         | 型      | 必須 | 説明                                 | 例                                     |
| -------------- | ------- | ---- | ------------------------------------ | -------------------------------------- |
| UserId         | String  | ✓    | ユーザーID（Cognito sub）            | "a1b2c3d4-e5f6-7890-abcd-ef1234567890" |
| ItemId         | String  | ✓    | アイテムID（UUID）                   | "990e8400-e29b-41d4-a716-446655440004" |
| Name           | String  | ✓    | アイテム名（最大100文字）            | "玉ねぎ"                               |
| Quantity       | Number  | ✓    | 数量（0.01-9999）                    | 2                                      |
| Unit           | String  | ✓    | 単位コード                           | "piece"                                |
| IsChecked      | Boolean | ✓    | チェック済みフラグ                   | false                                  |
| IsCheckedAt    | String  |      | チェック日時（ISO8601形式）          | "2024-12-01T18:00:00Z"                 |
| AddedAt        | String  | ✓    | 追加日時（ISO8601形式）              | "2024-11-30T10:00:00Z"                 |
| SourceRecipeId | String  |      | 元レシピID（レシピから追加した場合） | "660e8400-e29b-41d4-a716-446655440001" |
| NormalizedKey  | String  | ✓    | 正規化キー（名前+単位）              | "玉ねぎ#piece"                         |

### インデックス

#### GSI_NormalizedKey
- **Partition Key**: `UserId` (String)
- **Sort Key**: `NormalizedKey` (String)
- **用途**: 同じ食材（名前+単位）の検索と数量合算

### アクセスパターン
1. **ユーザー別一覧**: `UserId` で検索（Query）
2. **正規化キー検索**: `GSI_NormalizedKey` を使用して同じ食材を検索（Query）
3. **期限切れアイテム削除**: チェック済みから3日経過したアイテムを削除

### 備考
- 同じ食材（名前+単位が同じ）を追加した場合、数量を合算
- 単位が異なる場合は別アイテムとして扱う
- チェック済みアイテムは3日後に自動削除
- `NormalizedKey` は小文字化した名前と単位コードの組み合わせ

---

## 6. Inventory テーブル

### 概要
購入済み食材の在庫を管理するテーブル。買い物リストでチェックされたアイテムが自動的に登録される。

### キー構造
- **Partition Key**: `UserId` (String)
- **Sort Key**: `ItemId` (String, UUID)

### 属性

| 属性名      | 型     | 必須 | 説明                             | 例                                     |
| ----------- | ------ | ---- | -------------------------------- | -------------------------------------- |
| UserId      | String | ✓    | ユーザーID                       | "550e8400-e29b-41d4-a716-446655440000" |
| ItemId      | String | ✓    | アイテムID（UUID）               | "990e8400-e29b-41d4-a716-446655440004" |
| Name        | String | ✓    | 食材名（最大100文字）            | "玉ねぎ"                               |
| Quantity    | Number | ✓    | 数量（0.01-9999）                | 2                                      |
| Unit        | String | ✓    | 単位                             | "個"                                   |
| ExpiryDate  | String |      | 賞味期限（YYYY-MM-DD形式、任意） | "2024-12-31"                           |
| PurchasedAt | String | ✓    | 購入日時（ISO8601形式）          | "2024-12-25T10:00:00Z"                 |
| CreatedAt   | String | ✓    | 登録日時（ISO8601形式）          | "2024-12-25T10:00:00Z"                 |

### インデックス

#### GSI_ExpiryDate
- **Partition Key**: `UserId` (String)
- **Sort Key**: `ExpiryDate` (String)
- **用途**: 賞味期限順での在庫検索（期限切れ間近の食材を優先表示）

### アクセスパターン
1. **ユーザー別在庫一覧**: `UserId` で検索（Query）
2. **賞味期限順検索**: `GSI_ExpiryDate` を使用して `UserId` で検索、`ExpiryDate` でソート
3. **特定アイテム取得**: `UserId` と `ItemId` で取得（GetItem）

### 備考
- 買い物リストでアイテムをチェックすると自動的に在庫に追加される
- 賞味期限は任意入力（未設定の場合は期限なしとして扱う）
- 同じ食材を追加した場合は数量を合算
- 在庫を使い切った場合はユーザーが手動で削除

---

## 7. ChatConversations テーブル

### 概要
AIチャットの会話セッションを管理するテーブル。ユーザーごとの会話履歴を保持。

### キー構造
- **Partition Key**: `UserId` (String)
- **Sort Key**: `ConversationId` (String, UUID)

### 属性

| 属性名           | 型     | 必須 | 説明                                                             | 例                                     |
| ---------------- | ------ | ---- | ---------------------------------------------------------------- | -------------------------------------- |
| UserId           | String | ✓    | ユーザーID（Cognito sub）                                        | "a1b2c3d4-e5f6-7890-abcd-ef1234567890" |
| ConversationId   | String | ✓    | 会話ID（UUID）                                                   | "aa0e8400-e29b-41d4-a716-446655440005" |
| Title            | String | ✓    | 会話タイトル（最大100文字）                                      | "カレーのレシピについて"               |
| ConversationType | String | ✓    | 会話タイプ（"general", "recipe_recommendation", "expiry_check"） | "general"                              |
| CreatedAt        | String | ✓    | 作成日時（ISO8601形式）                                          | "2024-12-25T10:00:00Z"                 |
| UpdatedAt        | String | ✓    | 更新日時（ISO8601形式）                                          | "2024-12-25T10:30:00Z"                 |

### 会話タイプ

| 値                    | 説明               |
| --------------------- | ------------------ |
| general               | 一般的なチャット   |
| recipe_recommendation | レシピ推薦         |
| expiry_check          | 食材の賞味期限確認 |

### インデックス
なし

### アクセスパターン
1. **ユーザー別会話一覧**: `UserId` で検索（Query）
2. **特定会話取得**: `UserId` と `ConversationId` で取得（GetItem）
3. **最新会話取得**: `UserId` で検索し、`UpdatedAt` でソート

### 備考
- 会話タイトルは最初のメッセージから自動生成、または手動設定
- `UpdatedAt` は新しいメッセージが追加されるたびに更新
- 会話タイプにより、将来的に異なる処理やUI表示が可能

---

## 8. ChatMessages テーブル

### 概要
AIチャットの個別メッセージを管理するテーブル。会話ごとのメッセージ履歴を保持。

### キー構造
- **Partition Key**: `ConversationId` (String)
- **Sort Key**: `MessageId` (String, ULID形式で時系列ソート可能)

### 属性

| 属性名          | 型     | 必須 | 説明                                  | 例                                     |
| --------------- | ------ | ---- | ------------------------------------- | -------------------------------------- |
| ConversationId  | String | ✓    | 会話ID                                | "aa0e8400-e29b-41d4-a716-446655440005" |
| MessageId       | String | ✓    | メッセージID（ULID）                  | "01ARZ3NDEKTSV4RRFFQ69G5FAV"           |
| Role            | String | ✓    | 送信者ロール（"user" or "assistant"） | "user"                                 |
| Content         | String | ✓    | メッセージ内容                        | "カレーの作り方を教えて"               |
| GeneratedRecipe | Map    |      | Geminiが生成したレシピ情報（JSON）    | 下記参照                               |
| CreatedAt       | String | ✓    | 作成日時（ISO8601形式）               | "2024-12-25T10:00:00Z"                 |

### Role（ロール）

| 値        | 説明                     |
| --------- | ------------------------ |
| user      | ユーザーからのメッセージ |
| assistant | AIからの応答             |

### GeneratedRecipe（生成レシピ）の構造

Geminiがレシピを生成した場合に保存されるMapオブジェクト：

| 属性名      | 型           | 必須 | 説明           | 例                                                 |
| ----------- | ------------ | ---- | -------------- | -------------------------------------------------- |
| title       | String       | ✓    | レシピタイトル | "簡単チキンカレー"                                 |
| ingredients | List<Map>    | ✓    | 食材リスト     | [{"name": "鶏肉", "quantity": 300, "unit": "g"}]   |
| steps       | List<String> | ✓    | 調理手順       | ["野菜を切る", "鶏肉を炒める", "水を加えて煮込む"] |
| cookingTime | Number       |      | 調理時間（分） | 45                                                 |
| tips        | String       |      | 調理のコツ     | "鶏肉は一口大に切ると火が通りやすい"               |

### インデックス
なし

### アクセスパターン
1. **会話別メッセージ一覧**: `ConversationId` で検索（Query）
2. **時系列順取得**: `ConversationId` で検索し、`MessageId`（ULID）でソート
3. **最新N件取得**: `ConversationId` で検索し、`ScanIndexForward=false` で降順取得

### 備考
- `MessageId` にULIDを使用することで、時系列順のソートが自然に行える
- `GeneratedRecipe` はGeminiがレシピを生成した場合のみ設定
- 通常のチャットメッセージでは `GeneratedRecipe` は null
- メッセージ内容は最大4000文字を想定（Geminiの応答を考慮）

---

## 設計書と実装の差異

### ✅ 一致している点
1. **テーブル構造**: 8つのテーブルすべてが設計書通りに実装されている
2. **キー構造**: Partition Key、Sort Key、GSIの構造が一致
3. **属性**: 必須属性とオプション属性が設計書通り
4. **データ型**: String、Number、Boolean、List、Mapの使い分けが適切

### ⚠️ 差異・注意点

#### 1. Users テーブル
**差異**: メールアドレス検索にGSIが未実装
- **設計書**: GSIの追加を推奨（記載なし）
- **実装**: Scanを使用（非効率）
- **影響**: ユーザー数が増加するとパフォーマンスが低下
- **推奨**: `GSI_Email` の追加を検討

#### 2. Reviews テーブル
**差異**: ReviewIdのみでの検索が非効率
- **設計書**: 特に記載なし
- **実装**: Scanを使用（非効率）
- **影響**: レビュー数が増加するとパフォーマンスが低下
- **推奨**: RecipeIdとReviewIdの組み合わせで検索することを推奨

#### 3. Schedules テーブル
**差異**: ScheduleIdのみでの検索が非効率
- **設計書**: 特に記載なし
- **実装**: Scanを使用（非効率）
- **影響**: スケジュール数が増加するとパフォーマンスが低下
- **備考**: IDでの検索は稀なため、現状は許容範囲

#### 4. Ingredient の note 属性
**差異**: 最大文字数の記載
- **設計書**: 最大60文字
- **実装**: 最大200文字（ValidationConstants.INGREDIENT_NOTE_MAX_LENGTH）
- **影響**: なし（実装が正しい）
- **対応**: 設計書を実装に合わせて修正済み

---

## パフォーマンス最適化の推奨事項

### 1. GSIの追加

#### Users テーブル
```
GSI_Email
- Partition Key: Email (String)
- 用途: メールアドレスでの高速検索
```

#### Reviews テーブル
現状のGSI_Userで十分だが、ReviewIdのみでの検索を避ける設計を推奨

### 2. クエリパターンの最適化

#### 推奨パターン
- ✅ GetItem: 単一アイテムの取得
- ✅ Query: キーを使った範囲検索
- ⚠️ Scan: 全テーブルスキャン（最小限に）

#### 避けるべきパターン
- ❌ Scanでの頻繁な検索
- ❌ 大量データのフィルタリング

### 3. キャパシティモードの選択

#### 開発・テスト環境
- **オンデマンドモード**: 使用量に応じた課金
- **メリット**: 管理不要、柔軟なスケーリング

#### 本番環境（予測可能な負荷）
- **プロビジョニングモード**: 事前にキャパシティを設定
- **メリット**: コスト最適化、Auto Scaling対応

---

## セキュリティ考慮事項

### 1. アクセス制御
- IAMロールによる最小権限の原則
- アプリケーション層での権限チェック
- ユーザーは自分のデータのみアクセス可能

### 2. データ暗号化
- **保存時**: DynamoDB暗号化（AWS KMS）
- **転送時**: TLS 1.2以上

### 3. バックアップ
- **Point-in-Time Recovery (PITR)**: 有効化推奨
- **オンデマンドバックアップ**: 定期的に実施

---

## 運用・監視

### 1. CloudWatch メトリクス
- 読み取り/書き込みキャパシティの使用率
- スロットリングイベント
- レイテンシ

### 2. アラート設定
- キャパシティ使用率が80%を超えた場合
- エラー率が閾値を超えた場合
- レイテンシが閾値を超えた場合

### 3. ログ記録
- DynamoDB Streams: データ変更の追跡
- CloudTrail: API呼び出しの監査ログ

---

## まとめ

本データベース設計は、DynamoDBの特性を活かしたスケーラブルな構造となっています。主な特徴：

1. **効率的なアクセスパターン**: Partition KeyとSort Keyを適切に設計
2. **GSIの活用**: 複数のアクセスパターンに対応
3. **論理削除**: データの整合性を保ちながら削除を実現
4. **正規化キー**: 買い物リストの数量合算を効率的に実現

今後のスケーリングに備えて、GSIの追加やクエリパターンの最適化を継続的に検討することを推奨します。
