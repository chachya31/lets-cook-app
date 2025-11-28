# Design Document

## Overview

自炊支援・食費節約アプリケーションは、ユーザーが料理習慣を維持し、食費を節約するためのWebアプリケーションです。本システムは、レシピ管理、スケジュール管理、買い物リスト管理、AIアドバイザー機能を提供し、ユーザーの自炊をサポートします。

システムは、Clean Architectureを採用したバックエンド（Java + Spring Boot + Gradle）、モダンなフロントエンド（TypeScript + React + Redux + shadcn/ui）、AWSサーバーレスインフラ（DynamoDB、S3、Cognito、Lambda）で構成されます。

主要な設計目標：
- ユーザーフレンドリーなUI/UX
- 高速なレスポンス（90%のリクエストを2秒以内）
- スケーラブルなアーキテクチャ
- セキュアな認証・認可
- 多言語対応（日本語・韓国語）
- アクセシブルなUIコンポーネント（shadcn/ui + Radix UI）

## Architecture

### システムアーキテクチャ

```
┌─────────────────────────────────────────────────────────────┐
│                        Client Layer                          │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  React + TypeScript + Redux + shadcn/ui              │   │
│  │  - UI Components (shadcn/ui + Radix UI)             │   │
│  │  - State Management (Redux Toolkit)                 │   │
│  │  - Styling (Tailwind CSS)                           │   │
│  │  - i18n (日本語/韓国語)                              │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                            │ HTTPS (TLS 1.2+)
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                      API Gateway (AWS)                       │
│  - REST API Endpoints                                        │
│  - Request Validation                                        │
│  - Rate Limiting                                             │
└─────────────────────────────────────────────────────────────┘
                            │
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                   Backend Layer (Lambda)                     │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  Spring Boot + Java + Gradle                         │   │
│  │  Clean Architecture:                                 │   │
│  │  ├─ Presentation Layer (Controllers)                 │   │
│  │  ├─ Application Layer (Use Cases)                    │   │
│  │  ├─ Domain Layer (Entities, Business Logic)          │   │
│  │  └─ Infrastructure Layer (Repositories, External)    │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                            │
        ┌───────────────────┼───────────────────┐
        ↓                   ↓                   ↓
┌──────────────┐  ┌──────────────────┐  ┌──────────────┐
│   DynamoDB   │  │   S3 Storage     │  │   Cognito    │
│  (Database)  │  │   (Images)       │  │   (Auth)     │
└──────────────┘  └──────────────────┘  └──────────────┘
                            │
                            ↓
                  ┌──────────────────┐
                  │  Gemini API      │
                  │  (AI Advisor)    │
                  └──────────────────┘
```

### レイヤー構成（Clean Architecture）

**1. Presentation Layer（プレゼンテーション層）**
- REST APIコントローラー
- リクエスト/レスポンスDTO
- 入力バリデーション
- 多言語対応（Accept-Languageヘッダー処理）

**2. Application Layer（アプリケーション層）**
- ユースケース実装
- ビジネスロジックのオーケストレーション
- トランザクション管理

**3. Domain Layer（ドメイン層）**
- エンティティ（User, Recipe, Schedule, ShoppingList, Review）
- ドメインロジック
- ビジネスルール

**4. Infrastructure Layer（インフラストラクチャ層）**
- DynamoDBリポジトリ実装
- S3ストレージサービス
- Cognito認証サービス
- Gemini API統合
- キャッシュ管理

## Components and Interfaces

### バックエンドコンポーネント

**1. User Management Module**
- UserController: ユーザー登録、ログイン、プロフィール管理
- UserService: ユーザー関連ビジネスロジック
- UserRepository: DynamoDBアクセス
- CognitoAuthService: 認証・認可

**2. Recipe Management Module**
- RecipeController: レシピCRUD操作
- RecipeService: レシピ検索、バリデーション
- RecipeRepository: DynamoDBアクセス
- S3ImageService: 画像アップロード/取得

**3. Schedule Management Module**
- ScheduleController: スケジュールCRUD操作
- ScheduleService: 予定/実績管理、アラート判定
- ScheduleRepository: DynamoDBアクセス

**4. Shopping List Module**
- ShoppingListController: 買い物リストCRUD操作
- ShoppingListService: 数量合算、自動削除
- ShoppingListRepository: DynamoDBアクセス

**5. Review Module**
- ReviewController: レビューCRUD操作
- ReviewService: 通報処理、自動非表示
- ReviewRepository: DynamoDBアクセス

**6. AI Advisor Module**
- AIAdvisorController: AIアドバイス取得
- AIAdvisorService: Gemini API呼び出し、キャッシュ管理
- CacheService: 24時間キャッシュ

**7. Admin Module**
- AdminController: 管理者機能
- AdminService: ユーザー管理、レシピ審査
- AdminRepository: DynamoDBアクセス

### フロントエンドコンポーネント

**1. Authentication Components**
- LoginPage: ログイン画面
- RegisterPage: ユーザー登録画面
- PasswordResetPage: パスワードリセット画面

**2. Dashboard Components**
- DashboardPage: ホーム画面
- AlertModal: サボり防止アラート

**3. Recipe Components**
- RecipeSearchPage: レシピ検索画面
- RecipeDetailPage: レシピ詳細画面
- RecipeEditPage: レシピ編集画面
- AIAdvisorPanel: AIアドバイザーパネル
- ReviewList: レビュー一覧
- ReviewForm: レビュー投稿フォーム

**4. Schedule Components**
- SchedulePage: スケジュール管理画面
- CalendarView: カレンダー表示
- ScheduleForm: 予定/実績登録フォーム

**5. Shopping List Components**
- ShoppingListPage: 買い物リスト画面
- ShoppingListItem: リストアイテム

**6. Profile Components**
- ProfilePage: プロフィール編集画面
- ImageUploader: 画像アップロードコンポーネント
- LanguageSelector: 言語選択

**7. Admin Components**
- AdminDashboard: 管理ダッシュボード
- UserManagement: ユーザー管理
- RecipeManagement: レシピ管理

### API Endpoints

**User Management**
- POST /api/users/register - ユーザー登録
- POST /api/users/login - ログイン
- GET /api/users/profile - プロフィール取得
- PUT /api/users/profile - プロフィール更新
- DELETE /api/users/account - アカウント削除
- POST /api/users/profile/image - プロフィール画像アップロード

**Recipe Management**
- GET /api/recipes - レシピ検索
- GET /api/recipes/{id} - レシピ詳細取得
- POST /api/recipes - レシピ作成
- PUT /api/recipes/{id} - レシピ更新
- DELETE /api/recipes/{id} - レシピ削除
- POST /api/recipes/{id}/image - レシピ画像アップロード

**Schedule Management**
- GET /api/schedules - スケジュール一覧取得
- POST /api/schedules - スケジュール作成
- PUT /api/schedules/{id} - スケジュール更新
- DELETE /api/schedules/{id} - スケジュール削除
- GET /api/schedules/alert - アラート判定

**Shopping List**
- GET /api/shopping-lists - 買い物リスト取得
- POST /api/shopping-lists - アイテム追加
- PUT /api/shopping-lists/{id} - アイテム更新
- DELETE /api/shopping-lists/{id} - アイテム削除

**Review**
- GET /api/recipes/{id}/reviews - レビュー一覧取得
- POST /api/recipes/{id}/reviews - レビュー作成
- PUT /api/reviews/{id} - レビュー更新
- DELETE /api/reviews/{id} - レビュー削除
- POST /api/reviews/{id}/report - レビュー通報

**AI Advisor**
- POST /api/ai-advisor/advice - AIアドバイス取得

**Admin**
- GET /api/admin/dashboard - ダッシュボード統計
- GET /api/admin/users - ユーザー一覧
- PUT /api/admin/users/{id}/suspend - ユーザー停止
- DELETE /api/admin/users/{id} - ユーザー削除
- GET /api/admin/recipes - レシピ一覧
- PUT /api/admin/recipes/{id}/status - レシピステータス更新

## Data Models

### DynamoDB テーブル設計

**Users テーブル**
```
Partition Key: UserId (String, UUID)
Attributes:
  - Email (String)
  - Nickname (String)
  - DisplayName (String)
  - ProfileImageUrl (String)
  - PreferredLanguage (String: "ja" | "ko")
  - CreatedAt (String, ISO8601)
  - LastCookingDate (String, ISO8601)
  - LastLoginDate (String, ISO8601)
  - Timezone (String, default: "Asia/Tokyo")
  - MarketingOptOut (Boolean)
```

**Recipes テーブル**
```
Partition Key: RecipeId (String, UUID)
Sort Key: CreatedAt (String, ISO8601)
GSI_Author: AuthorId (PK)
GSI_Category: Category (PK)
Attributes:
  - Title (String)
  - AuthorId (String)
  - Ingredients (List<Map>)
    - name (String)
    - quantity (Number)
    - unit (String)
    - note (String, optional)
    - optional (Boolean, optional)
  - Steps (List<String>)
  - CookingTime (Number, minutes)
  - TotalTimeMin (Number, minutes)
  - ServingsDefault (Number)
  - ImageUrl (String)
  - IsPublic (Boolean)
  - IsDeleted (Boolean)
  - CreatedAt (String, ISO8601)
  - UpdatedAt (String, ISO8601)
```

**Schedules テーブル**
```
Partition Key: UserId (String)
Sort Key: Date#Type#RecipeId (String)
Attributes:
  - Date (String, YYYY-MM-DD)
  - Type (String: "PLANNED" | "COOKED")
  - RecipeId (String)
  - RecipeTitle (String)
  - Memo (String, max 120 chars)
  - CreatedAt (String, ISO8601)
```

**ShoppingLists テーブル**
```
Partition Key: UserId (String)
Sort Key: ItemId (String, UUID)
Attributes:
  - Name (String)
  - Quantity (Number)
  - Unit (String)
  - IsChecked (Boolean)
  - IsCheckedAt (String, ISO8601)
  - AddedAt (String, ISO8601)
  - SourceRecipeId (String, optional)
  - NormalizedKey (String)
```

**Reviews テーブル**
```
Partition Key: RecipeId (String)
Sort Key: ReviewId (String, UUID)
GSI_User: UserId (PK)
Attributes:
  - UserId (String)
  - Rating (Number, 1-5)
  - Comment (String, max 300 chars)
  - Status (String: "visible" | "hidden")
  - ReportedCount (Number)
  - CreatedAt (String, ISO8601)
  - UpdatedAt (String, ISO8601)
```

### ドメインエンティティ

**User Entity**
- userId: UUID
- email: String
- nickname: String
- profileImageUrl: String
- preferredLanguage: Language (enum)
- lastCookingDate: LocalDate
- validatePassword(password: String): boolean
- canDeleteAccount(): boolean

**Recipe Entity**
- recipeId: UUID
- title: String
- authorId: UUID
- ingredients: List<Ingredient>
- steps: List<String>
- cookingTime: int
- imageUrl: String
- isPublic: boolean
- isDeleted: boolean
- validateIngredient(ingredient: Ingredient): boolean
- markAsDeleted(): void

**Ingredient Value Object**
- name: String (1-50 chars)
- quantity: BigDecimal (0 < qty <= 9999)
- unit: Unit (enum)
- note: String (optional, max 60 chars)
- optional: boolean
- normalize(): String

**Schedule Entity**
- scheduleId: UUID
- userId: UUID
- date: LocalDate
- type: ScheduleType (enum: PLANNED, COOKED)
- recipeId: UUID
- recipeTitle: String
- memo: String
- convertToCooked(): void

**ShoppingListItem Entity**
- itemId: UUID
- userId: UUID
- name: String
- quantity: BigDecimal
- unit: Unit
- isChecked: boolean
- isCheckedAt: LocalDateTime
- normalizedKey: String
- shouldAutoDelete(): boolean

**Review Entity**
- reviewId: UUID
- recipeId: UUID
- userId: UUID
- rating: int (1-5)
- comment: String
- status: ReviewStatus (enum: VISIBLE, HIDDEN)
- reportedCount: int
- incrementReportCount(): void
- shouldHide(): boolean


## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: アカウント作成の成功

*すべての*有効なメールアドレスとパスワード要件を満たすパスワードに対して、アカウント作成は成功し、Cognitoにユーザーが登録される

**Validates: Requirements 1.1**

### Property 2: 認証の成功

*すべての*有効な認証情報に対して、認証は成功し、ユーザーはシステムにアクセスできる

**Validates: Requirements 1.2**

### Property 3: プロフィール画像バリデーション

*すべての*画像アップロードに対して、ファイルサイズが5MB以下でフォーマットがJPEGまたはPNGの場合のみアップロードが成功する

**Validates: Requirements 1.4**

### Property 4: アカウント削除時の匿名化

*すべての*ユーザーに対して、アカウント削除時にアカウントとプロフィール画像は削除され、投稿したレシピとレビューは匿名化される

**Validates: Requirements 1.5**

### Property 5: レシピ検索の一致

*すべての*検索条件に対して、返されるレシピは指定された条件（カテゴリ、食材、調理時間）に一致する

**Validates: Requirements 2.1**

### Property 6: レシピ詳細の完全性

*すべての*レシピに対して、詳細表示にはタイトル、食材（数量・単位含む）、調理手順、画像が含まれる

**Validates: Requirements 2.2**

### Property 7: 表示可能レビューのフィルタリング

*すべての*レシピ詳細表示に対して、表示されるレビューはvisibleステータスのもののみである

**Validates: Requirements 2.3**

### Property 8: 食材情報の完全性

*すべての*レシピの食材に対して、名前、数量、単位、任意のメモ、任意フラグが表示される

**Validates: Requirements 2.5**

### Property 9: レシピ作成の成功

*すべての*有効なレシピデータ（タイトル、食材、手順、任意の画像）に対して、レシピ作成は成功する

**Validates: Requirements 3.1**

### Property 10: 食材バリデーション

*すべての*食材情報に対して、名前が1〜50文字、数量が0〜9999の範囲、単位が事前定義リストからの場合のみバリデーションが成功する

**Validates: Requirements 3.2**

### Property 11: レシピ更新の反映

*すべての*レシピ編集に対して、更新された情報が正しく保存され、次回取得時に反映される

**Validates: Requirements 3.3**

### Property 12: レシピ削除時の参照保持

*すべての*レシピ削除に対して、スケジュールと買い物リストの参照は保持され、レシピは削除済みとしてマークされる

**Validates: Requirements 3.4**

### Property 13: レシピ画像のストレージとバリデーション

*すべての*レシピ画像アップロードに対して、ファイルサイズが5MB以下でフォーマットがJPEGまたはPNGの場合のみS3に保存される

**Validates: Requirements 3.5**

### Property 14: AI APIの呼び出し

*すべての*食材選択とAIアドバイス要求に対して、Gemini APIが呼び出され、保存方法と代替食材が返される

**Validates: Requirements 4.1**

### Property 15: レビュー作成の成功

*すべての*有効な星評価（1〜5）と任意のコメント（最大300文字）に対して、レビュー作成は成功する

**Validates: Requirements 5.1**

### Property 16: 自分のレビューの編集・削除権限

*すべての*ユーザーの自分のレビューに対して、編集と削除が許可される

**Validates: Requirements 5.2**

### Property 17: レビュー通報カウントの増加

*すべての*レビュー通報に対して、通報カウントが1増加する

**Validates: Requirements 5.3**

### Property 18: 通報による自動非表示

*すべての*レビューに対して、通報カウントが3以上になった場合、ステータスが自動的にhiddenに設定される

**Validates: Requirements 5.4**

### Property 19: 表示可能レビューのみの表示

*すべての*レビュー閲覧に対して、visibleステータスのレビューのみが表示される

**Validates: Requirements 5.5**

### Property 20: カレンダー表示の完全性

*すべての*カレンダー表示に対して、選択された月または週の予定と実績のエントリがすべて表示される

**Validates: Requirements 6.1**

### Property 21: 料理実績登録とLastCookingDate更新

*すべての*料理活動登録に対して、COOKEDスケジュールエントリが作成され、LastCookingDateが更新される

**Validates: Requirements 6.2**

### Property 22: 料理予定の作成

*すべての*将来の日付への料理予定登録に対して、PLANNEDスケジュールエントリが作成される

**Validates: Requirements 6.3**

### Property 23: 予定から実績への変換

*すべての*PLANNEDエントリに対して、COOKEDへの変換時にスケジュールタイプがCOOKEDに更新される

**Validates: Requirements 6.4**

### Property 24: 過去のスケジュール編集・削除権限

*すべての*過去のスケジュールエントリに対して、編集と削除が許可される

**Validates: Requirements 6.5**

### Property 25: アラート表示判定

*すべての*ダッシュボードアクセスに対して、LastCookingDateから3日が経過して4日目の0時になった場合、アラートモーダルが表示される

**Validates: Requirements 7.1**

### Property 26: アラート再表示防止

*すべての*アラートモーダル閉じる操作に対して、localStorageにフラグが保存され、同日の再表示が防止される

**Validates: Requirements 7.2**

### Property 27: クイック料理登録とアラート非表示

*すべての*アラート内のクイック料理登録ボタンクリックに対して、COOKEDエントリが作成され、アラートが即座に非表示になる

**Validates: Requirements 7.3**

### Property 28: アラートメッセージのランダム選択

*すべての*アラート表示に対して、警告メッセージまたは励ましメッセージがランダムに選択される

**Validates: Requirements 7.4**

### Property 29: アラート非表示条件

*すべての*ダッシュボードアクセスに対して、3日以内に料理をした場合、アラートは表示されない

**Validates: Requirements 7.5**

### Property 30: 買い物リストアイテムの作成

*すべての*レシピからの食材追加に対して、買い物リストアイテムが作成される

**Validates: Requirements 8.1**

### Property 31: 数量の合算

*すべての*食材追加に対して、既存のアイテムと同じ正規化された名前と単位を持つ場合、数量が合算される

**Validates: Requirements 8.2**

### Property 32: 単位違いの別アイテム作成

*すべての*食材追加に対して、同じ名前だが異なる単位を持つ既存のアイテムがある場合、別のアイテムが作成される

**Validates: Requirements 8.3**

### Property 33: チェック済みフラグとタイムスタンプの設定

*すべての*アイテムチェック操作に対して、IsCheckedがtrueに設定され、IsCheckedAtタイムスタンプが記録される

**Validates: Requirements 8.4**

### Property 34: チェック済みアイテムの自動削除

*すべての*チェック済みアイテムに対して、IsCheckedAtから3日が経過した場合、自動的に削除される

**Validates: Requirements 8.5**

### Property 35: 優先言語の保存

*すべての*言語選択に対して、PreferredLanguage属性に設定が保存される

**Validates: Requirements 9.1**

### Property 36: デフォルト言語の設定

*すべての*言語設定なしのアプリケーションアクセスに対して、ブラウザの言語設定がデフォルトとして使用される

**Validates: Requirements 9.2**

### Property 37: 優先言語でのUI表示

*すべての*UI要素表示に対して、ユーザーの優先言語でテキストがレンダリングされる

**Validates: Requirements 9.3**

### Property 38: 優先言語でのエラーメッセージ

*すべての*エラーメッセージまたはバリデーションメッセージに対して、Accept-Languageヘッダーに基づいてユーザーの優先言語でメッセージが提供される

**Validates: Requirements 9.4**

### Property 39: ユーザー生成コンテンツの元言語表示

*すべての*ユーザー生成コンテンツに対して、ユーザーが入力した元の言語でコンテンツが表示される

**Validates: Requirements 9.5**

### Property 40: 管理者のユーザーアカウント管理権限

*すべての*ユーザーアカウントに対して、管理者はアカウントの停止または削除を実行できる

**Validates: Requirements 10.2**

### Property 41: 管理者のレシピ表示

*すべての*管理者のレシピ閲覧に対して、審査待ちを含むすべてのレシピが表示される

**Validates: Requirements 10.3**

### Property 42: レシピの非公開ステータス設定

*すべての*レシピに対して、管理者が不適切としてマークした場合、レシピが非公開ステータスに設定される

**Validates: Requirements 10.4**

### Property 43: 管理者によるレシピ削除時の参照保持

*すべての*管理者によるレシピ削除に対して、参照が保持され、レシピが削除済みとしてマークされる

**Validates: Requirements 10.5**

### Property 44: パスワードバリデーション

*すべての*パスワード作成に対して、少なくとも8文字で、大文字、小文字、数字を含む場合のみバリデーションが成功する

**Validates: Requirements 11.1**

### Property 45: 同時セッションの許可

*すべての*複数デバイスからのログインに対して、同時セッションが許可される

**Validates: Requirements 11.3**

### Property 46: エラーログの記録

*すべての*エラー発生に対して、INFOレベル以上でCloudWatchにイベントがログ記録される

**Validates: Requirements 12.4**

### Property 47: ユーザーフレンドリーなエラーメッセージ

*すべての*APIエラー発生に対して、ユーザーフレンドリーなエラーメッセージが表示される

**Validates: Requirements 12.5**

## UI Component Library

### shadcn/ui

本プロジェクトでは、UIコンポーネントライブラリとして**shadcn/ui**を採用します。

**選定理由**：
1. **完全無料・オープンソース**: MITライセンスで商用利用可能、費用は一切発生しない
2. **アクセシビリティ**: Radix UIベースで、WCAG 2.1準拠のアクセシブルなコンポーネント
3. **カスタマイズ性**: Tailwind CSSベースで、プロジェクトのテーマカラー（明るい緑）に容易に調整可能
4. **TypeScript完全対応**: 型安全性が高く、開発体験が向上
5. **軽量**: 必要なコンポーネントのみをプロジェクトにコピーする方式で、バンドルサイズを最小化
6. **保守性**: コンポーネントのコードが直接プロジェクトに含まれるため、カスタマイズや修正が容易

**使用するコンポーネント**：
- **Button**: CTA、フォーム送信、アクション実行
- **Card**: レシピカード、ダッシュボードウィジェット
- **Dialog/Modal**: アラート表示、確認ダイアログ
- **Form**: レシピ登録、ログイン、プロフィール編集
- **Input**: テキスト入力フィールド
- **Select/Dropdown**: 食材選択、言語切り替え、カテゴリ選択
- **Checkbox**: 買い物リストのチェック
- **Toast**: 成功/エラー通知
- **Calendar**: スケジュール管理
- **Label**: フォームラベル
- **Textarea**: レシピ手順、コメント入力

**テーマ設定**：
- プライマリカラー: `hsl(142, 76%, 36%)` （明るい緑）
- CSS変数ベースのテーマシステムで、ライト/ダークモード対応
- Tailwind CSSのユーティリティクラスで細かいスタイル調整

**実装方針**：
- コンポーネントは`src/components/ui/`ディレクトリに配置
- `cn()`ユーティリティ関数でクラス名を動的に結合
- React Hook Formと統合してフォームバリデーションを実装

## Validation Strategy

### バリデーション方針

本システムでは、フロントエンドとバックエンドの両方でバリデーションを実施し、多層防御を実現します。

**フロントエンドバリデーション**：
- 目的：ユーザーエクスペリエンスの向上、即座のフィードバック
- タイミング：リアルタイム（入力中）およびフォーム送信時
- 実装：React Hook Formとshadcn/ui Formコンポーネントを使用
- 対象：
  - 必須フィールドチェック
  - 文字数制限（例：レシピタイトル、コメント）
  - フォーマット検証（例：メールアドレス、パスワード強度）
  - 数値範囲チェック（例：食材数量0〜9999）
  - ファイルサイズとフォーマット（例：画像5MB以下、JPEG/PNG）

**バックエンドバリデーション**：
- 目的：セキュリティ、データ整合性の保証
- タイミング：APIリクエスト受信時
- 実装：Spring Boot Validation（JSR-380）を使用
- 対象：
  - すべてのフロントエンドバリデーション項目を再検証
  - ビジネスルール検証（例：レシピ削除権限、レビュー編集権限）
  - データベース整合性チェック（例：重複チェック、外部キー制約）
  - 認証・認可チェック

**バリデーションエラーメッセージ**：
- フロントエンド：フィールド直下に日本語/韓国語でエラーメッセージを表示
- バックエンド：APIレスポンスにフィールド名とエラーコードを含める
- 多言語対応：Accept-Languageヘッダーに基づいてメッセージを返す

### バリデーションルール例

**ユーザー登録**：
- Email：有効なメールアドレス形式
- Password：8文字以上、大文字・小文字・数字を含む
- Nickname：1〜50文字

**レシピ作成**：
- Title：1〜100文字
- Ingredients：
  - Name：1〜50文字
  - Quantity：0 < qty <= 9999
  - Unit：事前定義リストから選択
- Steps：最低1つの手順
- Image：5MB以下、JPEG/PNG

**レビュー投稿**：
- Rating：1〜5の整数
- Comment：0〜300文字

## Project Structure

### バックエンドディレクトリ構造

```
backend/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── cookingapp/
│   │   │           ├── presentation/          # プレゼンテーション層
│   │   │           │   ├── controller/        # REST APIコントローラー
│   │   │           │   ├── dto/               # リクエスト/レスポンスDTO
│   │   │           │   └── validation/        # カスタムバリデーター
│   │   │           ├── application/           # アプリケーション層
│   │   │           │   ├── usecase/           # ユースケース実装
│   │   │           │   └── service/           # アプリケーションサービス
│   │   │           ├── domain/                # ドメイン層
│   │   │           │   ├── entity/            # エンティティ
│   │   │           │   ├── valueobject/       # バリューオブジェクト
│   │   │           │   ├── repository/        # リポジトリインターフェース
│   │   │           │   └── exception/         # ドメイン例外
│   │   │           └── infrastructure/        # インフラストラクチャ層
│   │   │               ├── repository/        # リポジトリ実装（DynamoDB）
│   │   │               ├── external/          # 外部API統合
│   │   │               │   ├── gemini/        # Gemini API
│   │   │               │   ├── s3/            # S3サービス
│   │   │               │   └── cognito/       # Cognito認証
│   │   │               ├── config/            # 設定クラス
│   │   │               └── cache/             # キャッシュ管理
│   │   └── resources/
│   │       ├── application.yml                # アプリケーション設定
│   │       └── messages/                      # 多言語メッセージ
│   │           ├── messages_ja.properties     # 日本語
│   │           └── messages_ko.properties     # 韓国語
│   └── test/
│       ├── java/
│       │   └── com/
│       │       └── cookingapp/
│       │           ├── unit/                  # ユニットテスト
│       │           └── property/              # プロパティベーステスト
│       └── resources/
│           └── test-data/                     # テストデータ
├── build.gradle                               # Gradleビルド設定
├── settings.gradle                            # Gradleプロジェクト設定
└── README.md                                  # プロジェクト説明
```

### フロントエンドディレクトリ構造

```
frontend/
├── src/
│   ├── components/                            # Reactコンポーネント
│   │   ├── ui/                                # shadcn/uiコンポーネント
│   │   │   ├── button.tsx
│   │   │   ├── card.tsx
│   │   │   ├── dialog.tsx
│   │   │   ├── form.tsx
│   │   │   ├── input.tsx
│   │   │   ├── select.tsx
│   │   │   ├── checkbox.tsx
│   │   │   ├── toast.tsx
│   │   │   ├── calendar.tsx
│   │   │   └── label.tsx
│   │   ├── auth/                              # 認証関連
│   │   │   ├── LoginPage.tsx
│   │   │   ├── RegisterPage.tsx
│   │   │   └── PasswordResetPage.tsx
│   │   ├── dashboard/                         # ダッシュボード
│   │   │   ├── DashboardPage.tsx
│   │   │   └── AlertModal.tsx
│   │   ├── recipe/                            # レシピ関連
│   │   │   ├── RecipeSearchPage.tsx
│   │   │   ├── RecipeDetailPage.tsx
│   │   │   ├── RecipeEditPage.tsx
│   │   │   ├── AIAdvisorPanel.tsx
│   │   │   ├── ReviewList.tsx
│   │   │   └── ReviewForm.tsx
│   │   ├── schedule/                          # スケジュール関連
│   │   │   ├── SchedulePage.tsx
│   │   │   ├── CalendarView.tsx
│   │   │   └── ScheduleForm.tsx
│   │   ├── shopping/                          # 買い物リスト
│   │   │   ├── ShoppingListPage.tsx
│   │   │   └── ShoppingListItem.tsx
│   │   ├── profile/                           # プロフィール
│   │   │   ├── ProfilePage.tsx
│   │   │   ├── ImageUploader.tsx
│   │   │   └── LanguageSelector.tsx
│   │   ├── admin/                             # 管理者機能
│   │   │   ├── AdminDashboard.tsx
│   │   │   ├── UserManagement.tsx
│   │   │   └── RecipeManagement.tsx
│   │   └── common/                            # 共通コンポーネント
│   │       ├── Header.tsx
│   │       ├── Footer.tsx
│   │       ├── ErrorBanner.tsx
│   │       └── LoadingSkeleton.tsx
│   ├── store/                                 # Redux状態管理
│   │   ├── slices/                            # Reduxスライス
│   │   │   ├── authSlice.ts
│   │   │   ├── recipeSlice.ts
│   │   │   ├── scheduleSlice.ts
│   │   │   ├── shoppingListSlice.ts
│   │   │   └── reviewSlice.ts
│   │   └── store.ts                           # Reduxストア設定
│   ├── api/                                   # API呼び出し
│   │   ├── client.ts                          # APIクライアント設定
│   │   ├── userApi.ts
│   │   ├── recipeApi.ts
│   │   ├── scheduleApi.ts
│   │   ├── shoppingListApi.ts
│   │   ├── reviewApi.ts
│   │   └── aiAdvisorApi.ts
│   ├── hooks/                                 # カスタムフック
│   │   ├── useAuth.ts
│   │   ├── useRecipe.ts
│   │   └── useAlert.ts
│   ├── lib/                                   # ライブラリユーティリティ
│   │   └── utils.ts                           # cn()関数など
│   ├── utils/                                 # ユーティリティ関数
│   │   ├── validation.ts                      # バリデーション関数
│   │   ├── dateUtils.ts                       # 日付処理
│   │   └── normalize.ts                       # 正規化関数
│   ├── i18n/                                  # 多言語対応
│   │   ├── i18n.ts                            # i18next設定
│   │   ├── locales/
│   │   │   ├── ja.json                        # 日本語翻訳
│   │   │   └── ko.json                        # 韓国語翻訳
│   ├── types/                                 # TypeScript型定義
│   │   ├── user.ts
│   │   ├── recipe.ts
│   │   ├── schedule.ts
│   │   ├── shoppingList.ts
│   │   └── review.ts
│   ├── App.tsx                                # ルートコンポーネント
│   ├── index.tsx                              # エントリーポイント
│   ├── index.css                              # グローバルスタイル（Tailwind）
│   └── routes.tsx                             # ルーティング設定
├── public/                                    # 静的ファイル
│   ├── index.html
│   └── assets/
├── tests/                                     # テスト
│   ├── unit/                                  # ユニットテスト
│   └── property/                              # プロパティベーステスト
├── package.json                               # npm設定
├── tsconfig.json                              # TypeScript設定
├── tailwind.config.js                         # Tailwind CSS設定
├── postcss.config.js                          # PostCSS設定
├── .eslintrc.js                               # ESLint設定
└── README.md                                  # プロジェクト説明
```

## Error Handling

### エラー分類

**1. バリデーションエラー（400 Bad Request）**
- 入力値が要件を満たさない場合
- 例：パスワードが8文字未満、画像サイズが5MB超過
- 対応：詳細なエラーメッセージを返し、フロントエンドでフィールド単位で表示

**2. 認証エラー（401 Unauthorized）**
- 認証情報が無効または期限切れの場合
- 対応：ログイン画面にリダイレクト

**3. 認可エラー（403 Forbidden）**
- リソースへのアクセス権限がない場合
- 例：他人のレシピを編集しようとした場合
- 対応：エラーメッセージを表示し、前の画面に戻る

**4. リソース未検出エラー（404 Not Found）**
- 指定されたリソースが存在しない場合
- 例：削除されたレシピにアクセス
- 対応：「削除されたレシピです」メッセージを表示

**5. サーバーエラー（500 Internal Server Error）**
- システム内部エラー
- 対応：汎用エラーメッセージを表示し、CloudWatchにログ記録

**6. 外部APIエラー**
- Gemini APIの呼び出し失敗
- 対応：
  - 24時間以内のキャッシュがあれば表示
  - キャッシュがなければ再試行ボタンと静的ヒントを表示
  - レート制限の場合は30秒後に自動再試行

### エラーレスポンス形式

```json
{
  "ok": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "パスワードは8文字以上で、大文字、小文字、数字を含む必要があります",
    "field": "password",
    "timestamp": "2024-11-28T12:00:00Z"
  }
}
```

### リトライ戦略

**1. AI APIリトライ**
- 初回失敗：キャッシュチェック
- レート制限：30秒後に自動再試行（最大3回）
- その他のエラー：手動再試行ボタンを提供

**2. DynamoDBリトライ**
- スロットリングエラー：指数バックオフで最大3回リトライ
- その他のエラー：即座に失敗

**3. S3リトライ**
- ネットワークエラー：最大3回リトライ
- その他のエラー：即座に失敗

## Testing Strategy

### テスト方針

本システムでは、ユニットテストとプロパティベーステストの両方を実施し、包括的なテストカバレッジを確保します。

**ユニットテスト**：
- 特定の例やエッジケースを検証
- 統合ポイントの動作確認
- カバレッジ目標：50%以上（重要ロジックを優先）

**プロパティベーステスト**：
- 普遍的なプロパティを検証
- 多様な入力に対する正しさを保証
- 各プロパティは最低100回の反復実行

### プロパティベーステストライブラリ

**バックエンド（Java）**：
- **jqwik** - Java用プロパティベーステストライブラリ
- Gradle依存関係：
  ```gradle
  testImplementation 'net.jqwik:jqwik:1.7.4'
  ```

**フロントエンド（TypeScript）**：
- **fast-check** - TypeScript/JavaScript用プロパティベーステストライブラリ
- npm依存関係：
  ```json
  "devDependencies": {
    "fast-check": "^3.13.0"
  }
  ```

### テスト実装要件

1. 各プロパティベーステストは、最低100回の反復実行を設定する
2. 各プロパティベーステストには、対応するCorrectness Propertyを明示的に参照するコメントを付ける
   - フォーマット：`// Feature: cooking-support-app, Property {number}: {property_text}`
3. 各Correctness Propertyは、1つのプロパティベーステストで実装する
4. テストは実際の機能を検証し、モックやフェイクデータで合格させない

### テスト対象

**バックエンドユニットテスト**：
- ドメインロジック（エンティティ、バリューオブジェクト）
- ユースケース（ビジネスロジック）
- リポジトリ（DynamoDBアクセス）
- 外部API統合（Gemini API、S3）

**フロントエンドユニットテスト**：
- コンポーネントのレンダリング
- ユーザーインタラクション
- 状態管理（Redux）
- API呼び出し

**プロパティベーステスト**：
- 上記のCorrectness Propertiesに対応するテスト
- ジェネレーターを使用してランダムな入力を生成
- 境界値やエッジケースを含む

### テスト実行

**バックエンド**：
```bash
./gradlew test
```

**フロントエンド**：
```bash
npm test
```

### CI/CD統合

- PRごとにlint（ESLint）とユニットテストを実行
- mainブランチへのマージで自動デプロイ
- テスト失敗時はマージをブロック

## Build and Deployment

### ビルドツール

**バックエンド**：
- **Gradle** - Java/Spring Bootプロジェクトのビルドツール
- バージョン：Gradle 8.11.1
- Java：21+ (Java 23推奨)
- 主要タスク：
  - `./gradlew build` - プロジェクトビルド
  - `./gradlew test` - テスト実行
  - `./gradlew bootJar` - 実行可能JARの作成

**フロントエンド**：
- **npm** - Node.jsパッケージマネージャー
- 主要コマンド：
  - `npm install` - 依存関係インストール
  - `npm run build` - プロダクションビルド
  - `npm test` - テスト実行

### デプロイメント戦略

**インフラストラクチャ**：
- AWS SAM（Serverless Application Model）またはAWS CDKを使用
- Lambda関数としてバックエンドをデプロイ
- CloudFront + S3でフロントエンドを配信

**デプロイメントフロー**：
1. mainブランチへのマージをトリガー
2. CI/CDパイプライン（GitHub Actions / AWS CodePipeline）が起動
3. バックエンド：Gradleビルド → Lambda関数デプロイ
4. フロントエンド：npmビルド → S3アップロード → CloudFront無効化
5. DynamoDBテーブル、S3バケット、Cognitoユーザープールは事前にプロビジョニング

### 環境管理

**開発環境（dev）**：
- 開発者のローカル環境
- LocalStack等でAWSサービスをエミュレート

**ステージング環境（staging）**：
- 本番環境と同等の構成
- 統合テストとユーザー受け入れテスト

**本番環境（production）**：
- 実際のユーザーが利用する環境
- 高可用性とスケーラビリティを確保

### モニタリング

**CloudWatch**：
- アプリケーションログ（INFOレベル以上）
- メトリクス（リクエスト数、エラー率、レイテンシ）
- アラーム（5xx率 > 2%で通知）

**X-Ray**：
- 分散トレーシング
- パフォーマンスボトルネックの特定

### バックアップとリカバリ

**DynamoDB**：
- Point-in-Time Recovery（PITR）を有効化
- 日次バックアップ

**S3**：
- バージョニングを有効化
- ライフサイクルポリシーで古いバージョンを削除
