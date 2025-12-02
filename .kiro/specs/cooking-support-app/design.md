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

**1. User Management Module（実装済み）**
- Domain Layer:
  - User Entity: ユーザードメインエンティティ（実装済み）
  - Language Value Object: 言語設定（実装済み）
- Application Layer:
  - RegisterUserUseCase: ユーザー登録ユースケース（実装済み）
  - LoginUserUseCase: ログインユースケース（実装済み）
  - GetUserProfileUseCase: プロフィール取得（実装済み）
  - UpdateUserProfileUseCase: プロフィール更新（実装済み）
  - DeleteUserAccountUseCase: アカウント削除（実装済み）
- Infrastructure Layer:
  - CognitoAuthService: Cognito認証サービス（実装済み）
  - DynamoDBUserRepository: DynamoDBリポジトリ（実装済み）
- Presentation Layer:
  - UserController: REST APIコントローラー（実装済み）

**2. Image Management Module（実装済み）**
- Infrastructure Layer:
  - S3Config: S3クライアント設定（実装済み）
  - S3ImageService: 画像アップロード、取得、削除（実装済み）
- Application Layer:
  - ImageValidator: 画像バリデーション（サイズ、フォーマット）（実装済み）
  - UploadProfileImageUseCase: プロフィール画像アップロード（実装済み）
- Presentation Layer:
  - UserController: 画像アップロードエンドポイント追加（実装済み）

**3. Recipe Management Module（実装済み）**
- Domain Layer:
  - Recipe Entity: レシピドメインエンティティ（実装済み）
  - Ingredient Value Object: 食材バリューオブジェクト（実装済み）
  - Unit Enum: 単位enum（実装済み）
- Application Layer:
  - CreateRecipeUseCase: レシピ作成ユースケース（実装済み）
  - UpdateRecipeUseCase: レシピ更新ユースケース（実装済み）
  - DeleteRecipeUseCase: レシピ削除ユースケース（論理削除）（実装済み）
  - GetRecipeUseCase: レシピ取得ユースケース（実装済み）
  - SearchRecipesUseCase: レシピ検索ユースケース（実装済み）
  - UploadRecipeImageUseCase: レシピ画像アップロードユースケース（実装済み）
- Infrastructure Layer:
  - DynamoDBRecipeRepository: DynamoDBリポジトリ（実装済み）
- Presentation Layer:
  - RecipeController: レシピCRUD操作REST APIコントローラー（実装済み）
  - RecipeRequest DTO: レシピ作成・更新リクエスト（実装済み）
  - RecipeResponse DTO: レシピレスポンス（実装済み）
  - IngredientDto: 食材DTO（実装済み）

**4. Schedule Management Module（実装済み）**
- Domain Layer:
  - Schedule Entity: スケジュールドメインエンティティ（実装済み）
  - ScheduleType Value Object: スケジュールタイプ（実装済み）
- Application Layer:
  - CreateScheduleUseCase: スケジュール作成ユースケース（実装済み）
  - UpdateScheduleUseCase: スケジュール更新ユースケース（実装済み）
  - DeleteScheduleUseCase: スケジュール削除ユースケース（実装済み）
  - GetSchedulesUseCase: スケジュール取得ユースケース（実装済み）
  - ConvertScheduleToCookedUseCase: 予定を実績に変換するユースケース（実装済み）
- Infrastructure Layer:
  - DynamoDBScheduleRepository: DynamoDBリポジトリ（実装済み）
- Presentation Layer:
  - ScheduleController: スケジュールCRUD操作REST APIコントローラー（実装済み）
  - CreateScheduleRequest DTO: スケジュール作成リクエスト（実装済み）
  - UpdateScheduleRequest DTO: スケジュール更新リクエスト（実装済み）
  - ScheduleResponse DTO: スケジュールレスポンス（実装済み）

**5. Shopping List Module（実装済み）**
- Domain Layer:
  - ShoppingListItem Entity: 買い物リストアイテムドメインエンティティ（実装済み）
    - 正規化キー生成ロジック（名前+単位）
    - 数量更新（合算）
    - チェック済み/未チェック切り替え
    - 自動削除判定（チェック済みから3日経過）
- Application Layer:
  - AddShoppingListItemUseCase: アイテム追加ユースケース（数量合算ロジック含む）（実装済み）
  - UpdateShoppingListItemUseCase: アイテム更新ユースケース（チェック状態）（実装済み）
  - DeleteShoppingListItemUseCase: アイテム削除ユースケース（実装済み）
  - GetShoppingListUseCase: 買い物リスト取得ユースケース（実装済み）
  - CleanupExpiredItemsUseCase: 期限切れアイテムクリーンアップユースケース（実装済み）
- Infrastructure Layer:
  - DynamoDBShoppingListRepository: DynamoDBリポジトリ（実装済み）
  - GSI_NormalizedKey: 正規化キーによる検索（実装済み）
- Presentation Layer:
  - ShoppingListController: 買い物リストCRUD操作REST APIコントローラー（実装済み）
  - AddShoppingListItemRequest DTO: アイテム追加リクエスト（実装済み）
  - UpdateShoppingListItemRequest DTO: アイテム更新リクエスト（実装済み）
  - ShoppingListItemResponse DTO: アイテムレスポンス（実装済み）

**6. Review Module（実装済み）**
- Domain Layer:
  - Review Entity: レビュードメインエンティティ（実装済み）
  - ReviewStatus Value Object: レビューステータス（実装済み）
- Application Layer:
  - CreateReviewUseCase: レビュー作成ユースケース（実装済み）
  - UpdateReviewUseCase: レビュー更新ユースケース（実装済み）
  - DeleteReviewUseCase: レビュー削除ユースケース（実装済み）
  - GetReviewsByRecipeUseCase: レシピIDでレビュー一覧取得（実装済み）
  - ReportReviewUseCase: レビュー通報ユースケース（実装済み）
- Infrastructure Layer:
  - DynamoDBReviewRepository: DynamoDBリポジトリ（実装済み）
- Presentation Layer:
  - ReviewController: レビューCRUD操作REST APIコントローラー（実装済み）
  - CreateReviewRequest DTO: レビュー作成リクエスト（実装済み）
  - UpdateReviewRequest DTO: レビュー更新リクエスト（実装済み）
  - ReviewResponse DTO: レビューレスポンス（実装済み）

**7. Alert Module（実装済み）**
- Application Layer:
  - CheckAlertUseCase: アラート判定ユースケース（実装済み）
    - 最終料理日から3日経過判定（4日目の0時）
    - アラートメッセージのランダム選択（警告/励まし）
  - AlertResponse: アラートレスポンスDTO（実装済み）
- Presentation Layer:
  - AlertController: アラート判定エンドポイント（実装済み）
    - GET /api/alerts/check: アラート表示判定

**8. AI Advisor Module（未実装）**
- AIAdvisorController: AIアドバイス取得
- AIAdvisorService: Gemini API呼び出し、キャッシュ管理
- CacheService: 24時間キャッシュ

**9. Admin Module（実装済み）**
- Application Layer:
  - GetAdminDashboardStatsUseCase: 管理者ダッシュボード統計取得（実装済み）
  - SuspendUserUseCase: ユーザー停止（実装済み）
  - DeleteUserByAdminUseCase: ユーザー削除（実装済み）
  - GetAllRecipesForAdminUseCase: すべてのレシピ取得（実装済み）
  - SetRecipeStatusUseCase: レシピステータス設定（実装済み）
  - DeleteRecipeByAdminUseCase: レシピ削除（実装済み）
- Presentation Layer:
  - AdminController: 管理者機能コントローラー（実装済み）
    - GET /api/admin/dashboard: ダッシュボード統計取得
    - PUT /api/admin/users/{userId}/suspend: ユーザー停止
    - DELETE /api/admin/users/{userId}: ユーザー削除
    - GET /api/admin/recipes: すべてのレシピ取得
    - PUT /api/admin/recipes/{recipeId}/status: レシピステータス設定
    - DELETE /api/admin/recipes/{recipeId}: レシピ削除
  - AdminDashboardResponse: ダッシュボードレスポンスDTO（実装済み）
  - SetRecipeStatusRequest: レシピステータス設定リクエストDTO（実装済み）

### フロントエンドコンポーネント

**1. Authentication Components（実装済み）**
- Login/
  - LoginPage: ログイン画面（実装済み）
  - loginFormConfig: ログインフォーム設定（バリデーションルール、フィールド定義）（実装済み）
- Register/
  - RegisterPage: ユーザー登録画面（実装済み）
  - registerFormConfig: 登録フォーム設定（バリデーションルール、フィールド定義、言語オプション）（実装済み）
- ConfirmEmail/
  - ConfirmEmailPage: メール確認画面（実装済み）
  - confirmEmailFormConfig: メール確認フォーム設定（バリデーションルール、フィールド定義）（実装済み）
- PasswordReset/
  - PasswordResetPage: パスワードリセット画面（プレースホルダー）

**2. Common Components（実装済み）**
- FormField: 再利用可能なフォームフィールドコンポーネント（maxLengthサポート追加）（実装済み）
- Header: アプリケーション全体のナビゲーションバー（実装済み）
  - ロゴとナビゲーションメニュー
  - ログイン/ログアウト機能
  - レスポンシブデザイン
  - 認証状態に応じた表示切り替え
- Footer: アプリケーション全体のフッター（実装済み）
  - アプリケーション名と著作権表示
  - シンプルで控えめなデザイン
- LoadingSkeleton: ローディング中のプレースホルダー（実装済み）
  - 3つのタイプ（card、list、text）をサポート
  - アニメーション付きのスケルトン表示
  - カスタマイズ可能（行数、クラス名）
- ErrorBanner: エラーバナーコンポーネント（実装済み）

**3. Dashboard Components（実装済み）**
- DashboardPage: ホーム画面（実装済み）
  - 最近のレシピ表示（最大3件）
  - スケジュール概要表示（今後7日間、最大5件）
  - 買い物リスト概要表示（未チェックのみ、最大5件）
  - クイックアクションセクション（レシピ作成、予定追加、買い物リスト追加、レシピ検索）
  - LoadingSkeletonによるローディング表示
  - 多言語対応（日本語・韓国語）
- alert/
  - AlertModal: サボり防止アラートモーダル（実装済み）
    - 最終料理日から3日経過した場合に表示
    - localStorageによる再表示制御（同日の再表示なし）
    - クイック料理登録ボタン（スケジュール画面に遷移）
    - ランダムメッセージ表示（警告/励まし）
- alertApi: アラートAPI呼び出し関数（実装済み）

**4. Recipe Components（実装済み）**
- RecipeSearchPage: レシピ検索画面（実装済み）
- RecipeDetailPage: レシピ詳細画面（レビューセクション含む）（実装済み）
- RecipeEditPage: レシピ編集画面（作成・更新両対応）（実装済み）
- recipeSlice: Redux状態管理（実装済み）
- recipeApi: API呼び出し関数（実装済み）
- Recipe型定義: TypeScript型定義（実装済み）
- ReviewList: レビュー一覧コンポーネント（実装済み）
- ReviewForm: レビュー投稿フォームコンポーネント（実装済み）
- reviewSlice: Redux状態管理（実装済み）
- reviewApi: API呼び出し関数（実装済み）
- useReview: カスタムフック（実装済み）
- Review型定義: TypeScript型定義（実装済み）
- AIAdvisorPanel: AIアドバイザーパネル（未実装）

**5. Schedule Components（実装済み）**
- SchedulePage: スケジュール管理画面（実装済み）
- scheduleSlice: Redux状態管理（実装済み）
- scheduleApi: API呼び出し関数（実装済み）
- Schedule型定義: TypeScript型定義（実装済み）
- 多言語対応（日本語・韓国語）（実装済み）

**6. Shopping List Components（実装済み）**
- ShoppingListPage: 買い物リスト画面（実装済み）
  - アイテム追加フォーム（名前、数量、単位）
  - 未チェックアイテム一覧（購入予定）
  - チェック済みアイテム一覧（購入済み）
  - チェック状態の切り替え
  - アイテム削除
- shoppingListSlice: Redux状態管理（実装済み）
- shoppingListApi: API呼び出し関数（実装済み）
- ShoppingListItem型定義: TypeScript型定義（実装済み）
- 多言語対応（日本語・韓国語）（実装済み）

**7. Profile Components（一部実装済み）**
- ProfilePage: プロフィール編集画面（未実装）
- ImageUploader: 画像アップロードコンポーネント（実装済み）
- LanguageSelector: 言語選択（未実装）

**8. Admin Components（実装済み）**
- AdminDashboardPage: 管理者ダッシュボード（実装済み）
  - 統計情報表示（総ユーザー数、総レシピ数）
  - ユーザー管理へのナビゲーション
  - レシピ管理へのナビゲーション
  - LoadingSkeletonによるローディング表示
  - 多言語対応（日本語・韓国語）
- UserManagementPage: ユーザー管理画面（実装済み）
  - ユーザーID入力フォーム
  - ユーザー停止機能
  - ユーザー削除機能（確認ダイアログ付き）
  - 注意事項表示
  - 多言語対応（日本語・韓国語）
- RecipeManagementPage: レシピ管理画面（実装済み）
  - すべてのレシピ一覧表示（審査待ち含む）
  - レシピ詳細表示（ID、作成者ID、調理時間、ステータス）
  - レシピステータス切り替え（公開/非公開）
  - レシピ削除機能（確認ダイアログ付き）
  - レシピ詳細へのナビゲーション
  - 多言語対応（日本語・韓国語）
- adminSlice: Redux状態管理（実装済み）
  - fetchAdminDashboardStats: ダッシュボード統計取得
  - suspendUser: ユーザー停止
  - deleteUserByAdmin: ユーザー削除
  - fetchAllRecipesForAdmin: すべてのレシピ取得
  - setRecipeStatus: レシピステータス設定
  - deleteRecipeByAdmin: レシピ削除
- adminApi: API呼び出し関数（実装済み）
  - getAdminDashboardStats: ダッシュボード統計取得
  - suspendUser: ユーザー停止
  - deleteUserByAdmin: ユーザー削除
  - getAllRecipesForAdmin: すべてのレシピ取得
  - setRecipeStatus: レシピステータス設定
  - deleteRecipeByAdmin: レシピ削除
- Admin型定義: TypeScript型定義（実装済み）
  - AdminDashboardStats: ダッシュボード統計
  - SetRecipeStatusRequest: レシピステータス設定リクエスト
  - AdminState: 管理者状態
- App.tsxルーティング追加（/admin、/admin/users、/admin/recipes）（実装済み）
- Headerに管理者メニュー追加（実装済み）

### API Endpoints

**User Management（実装済み）**
- POST /api/users/register - ユーザー登録
  - Request: RegisterUserRequest (email, password, nickname, preferredLanguage)
  - Response: UserResponse
  - Validation: Email形式、パスワード8文字以上（大文字・小文字・数字）、ニックネーム1-50文字
  - Status: ✅ 実装済み
- POST /api/users/login - ログイン
  - Request: LoginRequest (email, password)
  - Response: LoginResponse (accessToken, refreshToken, idToken, expiresIn, user)
  - Status: ✅ 実装済み
- GET /api/users/profile/{userId} - プロフィール取得
  - Response: UserResponse
  - Status: ✅ 実装済み
- PUT /api/users/profile/{userId} - プロフィール更新
  - Request: UpdateProfileRequest (nickname, displayName, preferredLanguage, timezone, marketingOptOut)
  - Response: UserResponse
  - Status: ✅ 実装済み
- DELETE /api/users/account/{userId} - アカウント削除
  - Response: 204 No Content
  - Status: ✅ 実装済み
- POST /api/users/profile/image - プロフィール画像アップロード
  - Request: multipart/form-data (userId, file)
  - Response: UserResponse
  - Validation: ファイルサイズ5MB以下、JPEG/PNG形式
  - Status: ✅ 実装済み

**Recipe Management（実装済み）**
- GET /api/recipes - レシピ検索
  - Query Parameters: keyword (optional), authorId (optional)
  - Response: List<RecipeResponse>
  - Status: ✅ 実装済み
- GET /api/recipes/{id} - レシピ詳細取得
  - Response: RecipeResponse
  - Status: ✅ 実装済み
- POST /api/recipes - レシピ作成
  - Request: RecipeRequest (title, ingredients, steps, cookingTime)
  - Header: X-User-Id
  - Response: RecipeResponse
  - Validation: タイトル100文字以内、食材1つ以上、手順1つ以上、調理時間0以上
  - Status: ✅ 実装済み
- PUT /api/recipes/{id} - レシピ更新
  - Request: RecipeRequest (title, ingredients, steps, cookingTime)
  - Header: X-User-Id
  - Response: RecipeResponse
  - Status: ✅ 実装済み
- DELETE /api/recipes/{id} - レシピ削除（論理削除）
  - Header: X-User-Id
  - Response: 204 No Content
  - Status: ✅ 実装済み
- POST /api/recipes/{id}/image - レシピ画像アップロード
  - Request: multipart/form-data (file)
  - Header: X-User-Id
  - Response: RecipeResponse
  - Validation: ファイルサイズ5MB以下、JPEG/PNG形式
  - Status: ✅ 実装済み

**Schedule Management（実装済み）**
- GET /api/schedules - スケジュール一覧取得
  - Query Parameters: startDate (required), endDate (required)
  - Header: X-User-Id
  - Response: List<ScheduleResponse>
  - Status: ✅ 実装済み
- POST /api/schedules - スケジュール作成
  - Request: CreateScheduleRequest (date, type, recipeId, recipeTitle, memo)
  - Header: X-User-Id
  - Response: ScheduleResponse
  - Validation: 日付形式YYYY-MM-DD、タイプplanned/cooked、メモ120文字以内
  - Status: ✅ 実装済み
- PUT /api/schedules/{id} - スケジュール更新
  - Request: UpdateScheduleRequest (memo)
  - Header: X-User-Id
  - Response: ScheduleResponse
  - Status: ✅ 実装済み
- DELETE /api/schedules/{id} - スケジュール削除
  - Header: X-User-Id
  - Response: 204 No Content
  - Status: ✅ 実装済み
- POST /api/schedules/{id}/convert-to-cooked - 予定を実績に変換
  - Header: X-User-Id
  - Response: ScheduleResponse
  - LastCookingDateも更新
  - Status: ✅ 実装済み

**Shopping List（実装済み）**
- GET /api/shopping-lists - 買い物リスト取得
  - Header: X-User-Id
  - Response: List<ShoppingListItemResponse>
  - 自動的に期限切れアイテムをクリーンアップ
  - Status: ✅ 実装済み
- POST /api/shopping-lists - アイテム追加
  - Request: AddShoppingListItemRequest (name, quantity, unit, sourceRecipeId)
  - Header: X-User-Id
  - Response: ShoppingListItemResponse
  - Validation: 名前100文字以内、数量0.01-9999、単位必須
  - 同じ正規化キー（名前+単位）のアイテムがある場合は数量を合算
  - Status: ✅ 実装済み
- PUT /api/shopping-lists/{id} - アイテム更新（チェック状態）
  - Request: UpdateShoppingListItemRequest (isChecked)
  - Header: X-User-Id
  - Response: ShoppingListItemResponse
  - Status: ✅ 実装済み
- DELETE /api/shopping-lists/{id} - アイテム削除
  - Header: X-User-Id
  - Response: 204 No Content
  - Status: ✅ 実装済み

**Review（実装済み）**
- GET /api/recipes/{recipeId}/reviews - レシピのレビュー一覧取得
  - Response: List<ReviewResponse>
  - 表示可能なレビュー（VISIBLE）のみを返す
  - Status: ✅ 実装済み
- POST /api/recipes/{recipeId}/reviews - レビュー作成
  - Request: CreateReviewRequest (rating, comment)
  - Header: X-User-Id
  - Response: ReviewResponse
  - Validation: 星評価1-5、コメント300文字以内
  - Status: ✅ 実装済み
- PUT /api/reviews/{reviewId} - レビュー更新
  - Request: UpdateReviewRequest (rating, comment)
  - Header: X-User-Id
  - Response: ReviewResponse
  - 権限チェック：自分のレビューのみ更新可能
  - Status: ✅ 実装済み
- DELETE /api/reviews/{reviewId} - レビュー削除
  - Header: X-User-Id
  - Response: 204 No Content
  - 権限チェック：自分のレビューのみ削除可能
  - Status: ✅ 実装済み
- POST /api/reviews/{reviewId}/report - レビュー通報
  - Response: ReviewResponse
  - 通報カウント増加、3回以上で自動非表示
  - Status: ✅ 実装済み

**Alert（実装済み）**
- GET /api/alerts/check - アラート表示判定
  - Header: X-User-Id
  - Response: AlertResponse (shouldShow, message)
  - 最終料理日から3日経過（4日目の0時）でアラート表示
  - メッセージはランダムに選択（警告/励まし）
  - Status: ✅ 実装済み

**AI Advisor（未実装）**
- POST /api/ai-advisor/advice - AIアドバイス取得
- Status: ⏳ 未実装

**Admin（実装済み）**
- GET /api/admin/dashboard - ダッシュボード統計取得
  - Response: AdminDashboardResponse (message, totalUsers, totalRecipes)
  - 統計情報を返す
  - Status: ✅ 実装済み
- PUT /api/admin/users/{userId}/suspend - ユーザー停止
  - Response: 204 No Content
  - ユーザーのログインを無効化
  - Status: ✅ 実装済み
- DELETE /api/admin/users/{userId} - ユーザー削除
  - Response: 204 No Content
  - ユーザーアカウントとデータを完全削除
  - Status: ✅ 実装済み
- GET /api/admin/recipes - すべてのレシピ取得（管理者用）
  - Response: List<RecipeResponse>
  - 審査待ちを含むすべてのレシピを返す
  - Status: ✅ 実装済み
- PUT /api/admin/recipes/{recipeId}/status - レシピステータス設定
  - Request: SetRecipeStatusRequest (isPublic)
  - Response: RecipeResponse
  - レシピの公開/非公開を設定
  - Status: ✅ 実装済み
- DELETE /api/admin/recipes/{recipeId} - レシピ削除（管理者用）
  - Response: 204 No Content
  - レシピを論理削除（参照保持）
  - Status: ✅ 実装済み

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

**Recipes テーブル（実装済み）**
```
Partition Key: RecipeId (String, UUID)
GSI_Author: AuthorId (PK)
Attributes:
  - Title (String, max 100 chars)
  - AuthorId (String)
  - Ingredients (List<Map>)
    - name (String, max 100 chars)
    - quantity (Number, BigDecimal)
    - unit (String, Unit enum code)
    - note (String, max 200 chars, optional)
    - optional (Boolean)
  - Steps (List<String>)
  - CookingTime (Number, minutes)
  - ImageUrl (String, optional)
  - IsPublic (Boolean)
  - IsDeleted (Boolean, 論理削除フラグ)
  - CreatedAt (String, ISO8601)
  - UpdatedAt (String, ISO8601)

Unit Enum: g, kg, ml, l, tbsp, tsp, cup, piece, pack, can, bottle, slice, clove, pinch, to_taste, as_needed
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

**Reviews テーブル（実装済み）**
```
Partition Key: RecipeId (String)
Sort Key: ReviewId (String, UUID)
GSI_User: UserId (PK), CreatedAt (SK)
Attributes:
  - UserId (String)
  - Rating (Number, 1-5)
  - Comment (String, max 300 chars, optional)
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

**Review Entity（実装済み）**
- reviewId: UUID
- recipeId: UUID
- userId: UUID
- rating: int (1-5)
- comment: String (optional, max 300 chars)
- status: ReviewStatus (enum: VISIBLE, HIDDEN)
- reportedCount: int
- createdAt: Instant
- updatedAt: Instant
- create(recipeId, userId, rating, comment): Review
- update(rating, comment): void
- incrementReportCount(): void
- shouldHide(): boolean
- canEdit(requestUserId): boolean
- isVisible(): boolean


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

**画面構造パターン（実装済み）**：

本プロジェクトでは、コンポーネントの責任を明確に分離するため、以下の2つのパターンを採用しています。

**パターン1: シンプルな画面（表示メイン）**
- 各画面は機能ごとにフォルダ分け（例：Login/、Register/、ConfirmEmail/）
- 各フォルダには以下のファイルを配置：
  - `○○Page.tsx`: ページコンポーネント（UI、ロジック、状態管理）
  - `○○Config.ts`: フォーム設定ファイル（初期値、バリデーションルール、フィールド定義）
- 適用対象：フォームがシンプル、イベントハンドラーが少ない画面
- 例：
  ```
  Login/
  ├── LoginPage.tsx          # ページコンポーネント
  └── loginFormConfig.ts     # フォーム設定
  ```

**パターン2: 複雑な画面（フォーム・操作が多い）**
- 各画面は機能ごとにフォルダ分け（例：RecipeEdit/）
- 各フォルダには以下の3つのファイルを配置：
  - `○○Page.tsx`: 表示コンポーネント（JSX/UIのみ、ビジネスロジックなし）
  - `use○○Handlers.ts`: イベントハンドラー（カスタムフック、Redux dispatch、ナビゲーション）
  - `○○Config.ts`: フォーム設定（初期値、バリデーションルール、定数、オプション）
- 責任の分離：
  - **表示（Page.tsx）**: UIの構造、propsの受け渡し
  - **操作（useHandlers.ts）**: イベント処理、状態管理、副作用
  - **属性（Config.ts）**: 設定値、バリデーションルール、定数
- 適用対象：複雑なフォーム（5つ以上のフィールド）、多数のイベントハンドラー
- メリット：
  - テスタビリティの向上（ロジックを独立してテスト可能）
  - 再利用性の向上（ハンドラーや設定を他のコンポーネントで共有可能）
  - 保守性の向上（各ファイルの責任が明確）
- 例：
  ```
  RecipeEdit/
  ├── RecipeEditPage.tsx           # 表示コンポーネント（UI）
  ├── useRecipeEditHandlers.ts     # イベントハンドラー（ロジック）
  └── recipeEditConfig.ts          # フォーム設定（属性）
  ```

**パターン選択の基準**：
- パターン1を使用：表示メイン、シンプルなフォーム、イベントハンドラーが1-2個
- パターン2を使用：複雑なフォーム、多数のイベントハンドラー、動的な要素追加/削除

## Validation Strategy

### バリデーション方針

本システムでは、フロントエンドとバックエンドの両方でバリデーションを実施し、多層防御を実現します。

**フロントエンドバリデーション（実装済み）**：
- 目的：ユーザーエクスペリエンスの向上、即座のフィードバック
- タイミング：リアルタイム（入力中）およびフォーム送信時
- 実装：カスタムuseFormフックとvalidation.tsユーティリティを使用
- 実装済みバリデーションルール：
  - `required`: 必須フィールドチェック
  - `email`: メールアドレス形式検証
  - `minLength`: 最小文字数チェック
  - `maxLength`: 最大文字数チェック
  - `pattern`: 正規表現パターンマッチング
  - `matchField`: フィールド一致チェック（パスワード確認用）
- 対象：
  - 必須フィールドチェック
  - 文字数制限（例：ニックネーム1-50文字、確認コード6桁）
  - フォーマット検証（例：メールアドレス、パスワード強度）
  - 数値範囲チェック（例：食材数量0〜9999）
  - ファイルサイズとフォーマット（例：画像5MB以下、JPEG/PNG）

**バックエンドバリデーション（実装済み）**：
- 目的：セキュリティ、データ整合性の保証
- タイミング：APIリクエスト受信時
- 実装：Spring Boot Validation（JSR-380）を使用
- 実装済みアノテーション：
  - `@NotBlank`: 空白文字列チェック
  - `@Email`: メールアドレス形式検証
  - `@Size`: 文字数範囲チェック
  - `@Pattern`: 正規表現パターンマッチング
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

**ユーザー登録（実装済み）**：
- Email：有効なメールアドレス形式
- Password：8文字以上、大文字・小文字・数字を含む
- Nickname：1〜50文字
- PreferredLanguage：'ja' または 'ko'

**ユーザープロフィール更新（実装済み）**：
- Nickname：1〜50文字（オプション）
- DisplayName：1〜100文字（オプション）
- PreferredLanguage：'ja' または 'ko'（オプション）
- Timezone：有効なタイムゾーン文字列（オプション）
- MarketingOptOut：boolean（オプション）

**メール確認（実装済み）**：
- ConfirmationCode：6桁の数字

**レシピ作成・更新（実装済み）**：
- Title：1〜100文字
- Ingredients：最低1つ必要
  - Name：1〜100文字
  - Quantity：0より大きい数値
  - Unit：Unit enumから選択（g, kg, ml, l, tbsp, tsp, cup, piece, pack, can, bottle, slice, clove, pinch, to_taste, as_needed）
  - Note：0〜200文字（オプション）
  - Optional：boolean
- Steps：最低1つの手順、各手順は空でない文字列
- CookingTime：0以上の整数（分）
- Image：5MB以下、JPEG/PNG（オプション）

**レビュー投稿（実装済み）**：
- Rating：1〜5の整数（必須）
- Comment：0〜300文字（オプション）

## Project Structure

### 全体構造

```
.
├── backend/                    # バックエンド（Spring Boot）
├── frontend/                   # フロントエンド（React + TypeScript）
├── infrastructure/             # インフラストラクチャ定義
│   ├── cdk/                   # AWS CDK（本番環境）
│   ├── localstack/            # LocalStack初期化スクリプト
│   ├── docker-compose.yml     # ローカル開発環境
│   ├── README.md              # インフラ概要
│   ├── SETUP.md               # セットアップガイド
│   └── VERIFICATION.md        # 検証ガイド
├── docs/                      # ドキュメント
└── .kiro/specs/               # 仕様書
```

### インフラストラクチャディレクトリ構造

```
infrastructure/
├── cdk/                                       # AWS CDK（本番環境用）
│   ├── bin/
│   │   └── app.ts                            # CDKアプリエントリーポイント
│   ├── lib/
│   │   └── cooking-app-stack.ts              # インフラ定義
│   ├── package.json                          # CDK依存関係
│   ├── tsconfig.json                         # TypeScript設定
│   └── cdk.json                              # CDK設定
├── localstack/                                # ローカル開発環境用
│   ├── 01-create-dynamodb-tables.sh          # DynamoDBテーブル作成
│   └── 02-create-s3-bucket.sh                # S3バケット作成
├── docker-compose.yml                         # LocalStackコンテナ定義
├── README.md                                  # インフラ概要・コスト見積もり
├── SETUP.md                                   # セットアップ手順
└── VERIFICATION.md                            # 検証チェックリスト
```

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
│   │   │           │   │   ├── UserController.java  # （実装済み）
│   │   │           │   │   ├── RecipeController.java # （実装済み）
│   │   │           │   │   ├── ScheduleController.java # （実装済み）
│   │   │           │   │   ├── ReviewController.java # （実装済み）
│   │   │           │   │   └── AlertController.java # （実装済み）
│   │   │           │   ├── dto/               # リクエスト/レスポンスDTO
│   │   │           │   │   ├── request/       # リクエストDTO（実装済み）
│   │   │           │   │   └── response/      # レスポンスDTO（実装済み）
│   │   │           │   ├── exception/         # グローバル例外ハンドラー
│   │   │           │   │   └── GlobalExceptionHandler.java  # （実装済み）
│   │   │           │   └── validation/        # カスタムバリデーター
│   │   │           ├── application/           # アプリケーション層
│   │   │           │   ├── usecase/           # ユースケース実装（機能ごとにパッケージ分割）
│   │   │           │   │   ├── user/          # ユーザー管理
│   │   │           │   │   │   ├── RegisterUserUseCase.java      # （実装済み）
│   │   │           │   │   │   ├── LoginUserUseCase.java         # （実装済み）
│   │   │           │   │   │   ├── ConfirmSignUpUseCase.java     # （実装済み）
│   │   │           │   │   │   ├── ResendConfirmationCodeUseCase.java # （実装済み）
│   │   │           │   │   │   ├── GetUserProfileUseCase.java    # （実装済み）
│   │   │           │   │   │   ├── UpdateUserProfileUseCase.java # （実装済み）
│   │   │           │   │   │   ├── DeleteUserAccountUseCase.java # （実装済み）
│   │   │           │   │   │   └── UploadProfileImageUseCase.java # （実装済み）
│   │   │           │   │   ├── recipe/        # レシピ管理
│   │   │           │   │   │   ├── CreateRecipeUseCase.java      # （実装済み）
│   │   │           │   │   │   ├── UpdateRecipeUseCase.java      # （実装済み）
│   │   │           │   │   │   ├── DeleteRecipeUseCase.java      # （実装済み）
│   │   │           │   │   │   ├── GetRecipeUseCase.java         # （実装済み）
│   │   │           │   │   │   ├── SearchRecipesUseCase.java     # （実装済み）
│   │   │           │   │   │   └── UploadRecipeImageUseCase.java # （実装済み）
│   │   │           │   │   ├── schedule/      # スケジュール管理
│   │   │           │   │   │   ├── CreateScheduleUseCase.java    # （実装済み）
│   │   │           │   │   │   ├── UpdateScheduleUseCase.java    # （実装済み）
│   │   │           │   │   │   ├── DeleteScheduleUseCase.java    # （実装済み）
│   │   │           │   │   │   ├── GetSchedulesUseCase.java      # （実装済み）
│   │   │           │   │   │   └── ConvertScheduleToCookedUseCase.java # （実装済み）
│   │   │           │   │   ├── review/        # レビュー管理
│   │   │           │   │   │   ├── CreateReviewUseCase.java      # （実装済み）
│   │   │           │   │   │   ├── UpdateReviewUseCase.java      # （実装済み）
│   │   │           │   │   │   ├── DeleteReviewUseCase.java      # （実装済み）
│   │   │           │   │   │   ├── GetReviewsByRecipeUseCase.java # （実装済み）
│   │   │           │   │   │   └── ReportReviewUseCase.java      # （実装済み）
│   │   │           │   │   └── alert/         # アラート機能
│   │   │           │   │       ├── CheckAlertUseCase.java        # （実装済み）
│   │   │           │   │       └── AlertResponse.java            # （実装済み）
│   │   │           │   └── validation/        # バリデーション
│   │   │           │       ├── ImageValidator.java           # （実装済み）
│   │   │           │       └── ImageValidationException.java # （実装済み）
│   │   │           ├── domain/                # ドメイン層
│   │   │           │   ├── entity/            # エンティティ
│   │   │           │   │   ├── User.java      # （実装済み）
│   │   │           │   │   └── Recipe.java    # （実装済み）
│   │   │           │   ├── valueobject/       # バリューオブジェクト
│   │   │           │   │   ├── Language.java  # （実装済み）
│   │   │           │   │   ├── Unit.java      # （実装済み）
│   │   │           │   │   └── Ingredient.java # （実装済み）
│   │   │           │   ├── repository/        # リポジトリインターフェース
│   │   │           │   │   └── UserRepository.java  # （実装済み）
│   │   │           │   └── exception/         # ドメイン例外
│   │   │           │       ├── AuthenticationException.java      # （実装済み）
│   │   │           │       ├── UserAlreadyExistsException.java   # （実装済み）
│   │   │           │       └── UserNotFoundException.java        # （実装済み）
│   │   │           └── infrastructure/        # インフラストラクチャ層
│   │   │               ├── repository/        # リポジトリ実装（DynamoDB）
│   │   │               │   └── DynamoDBUserRepository.java  # （実装済み）
│   │   │               ├── external/          # 外部API統合
│   │   │               │   ├── cognito/       # Cognito認証
│   │   │               │   │   └── CognitoAuthService.java  # （実装済み）
│   │   │               │   ├── s3/            # S3サービス
│   │   │               │   │   └── S3ImageService.java      # （実装済み）
│   │   │               │   └── gemini/        # Gemini API（未実装）
│   │   │               ├── config/            # 設定クラス
│   │   │               │   ├── DynamoDBConfig.java      # （実装済み）
│   │   │               │   ├── CognitoConfig.java       # （実装済み）
│   │   │               │   ├── S3Config.java            # （実装済み）
│   │   │               │   ├── CorsConfig.java          # （実装済み）
│   │   │               │   └── MessageConfig.java       # （実装済み）
│   │   │               └── cache/             # キャッシュ管理（未実装）
│   │   └── resources/
│   │       ├── application.yml                # アプリケーション設定（共通）
│   │       ├── application-local.yml          # ローカル開発設定（LocalStack）
│   │       └── messages/                      # 多言語メッセージ
│   │           ├── messages_ja.properties     # 日本語（実装済み）
│   │           └── messages_ko.properties     # 韓国語（実装済み）
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
│   │   │   ├── button.tsx                     # （実装済み）
│   │   │   ├── card.tsx                       # （実装済み）
│   │   │   ├── dialog.tsx                     # （実装済み）
│   │   │   ├── form.tsx                       # （実装済み）
│   │   │   ├── input.tsx                      # （実装済み）
│   │   │   ├── select.tsx                     # （実装済み）
│   │   │   ├── checkbox.tsx                   # （実装済み）
│   │   │   ├── alert.tsx                      # （実装済み）
│   │   │   ├── toast.tsx                      # （未実装）
│   │   │   ├── calendar.tsx                   # （未実装）
│   │   │   └── label.tsx                      # （実装済み）
│   │   ├── auth/                              # 認証関連（実装済み）
│   │   │   ├── Login/                         # ログイン機能
│   │   │   │   ├── LoginPage.tsx              # ログインページ（実装済み）
│   │   │   │   └── loginFormConfig.ts         # フォーム設定（実装済み）
│   │   │   ├── Register/                      # ユーザー登録機能
│   │   │   │   ├── RegisterPage.tsx           # 登録ページ（実装済み）
│   │   │   │   └── registerFormConfig.ts      # フォーム設定（実装済み）
│   │   │   ├── ConfirmEmail/                  # メール確認機能
│   │   │   │   ├── ConfirmEmailPage.tsx       # メール確認ページ（実装済み）
│   │   │   │   └── confirmEmailFormConfig.ts  # フォーム設定（実装済み）
│   │   │   └── PasswordReset/                 # パスワードリセット機能
│   │   │       └── PasswordResetPage.tsx      # リセットページ（プレースホルダー）
│   │   ├── common/                            # 共通コンポーネント（実装済み）
│   │   │   ├── FormField.tsx                  # 再利用可能なフォームフィールド（maxLength対応）（実装済み）
│   │   │   ├── ErrorBanner.tsx                # エラーバナーコンポーネント（実装済み）
│   │   │   ├── Header.tsx                     # ナビゲーションバー（実装済み）
│   │   │   ├── Footer.tsx                     # フッター（実装済み）
│   │   │   └── LoadingSkeleton.tsx            # ローディングプレースホルダー（実装済み）
│   │   ├── alert/                             # アラート機能（実装済み）
│   │   │   └── AlertModal.tsx                 # サボり防止アラートモーダル（実装済み）
│   │   ├── dashboard/                         # ダッシュボード（実装済み）
│   │   │   └── DashboardPage.tsx              # ホーム画面（実装済み）
│   │   ├── recipe/                            # レシピ関連（未実装）
│   │   │   ├── RecipeSearchPage.tsx
│   │   │   ├── RecipeDetailPage.tsx
│   │   │   ├── RecipeEditPage.tsx
│   │   │   ├── AIAdvisorPanel.tsx
│   │   │   ├── ReviewList.tsx
│   │   │   └── ReviewForm.tsx
│   │   ├── schedule/                          # スケジュール関連（実装済み）
│   │   │   └── SchedulePage.tsx               # （実装済み）
│   │   ├── shopping/                          # 買い物リスト（未実装）
│   │   │   ├── ShoppingListPage.tsx
│   │   │   └── ShoppingListItem.tsx
│   │   ├── profile/                           # プロフィール（一部実装済み）
│   │   │   ├── ProfilePage.tsx                # （未実装）
│   │   │   ├── ImageUploader.tsx              # （実装済み）
│   │   │   └── LanguageSelector.tsx           # （実装済み）
│   │   └── admin/                             # 管理者機能（実装済み）
│   │       ├── AdminDashboardPage.tsx         # 管理者ダッシュボード（実装済み）
│   │       ├── UserManagementPage.tsx         # ユーザー管理（実装済み）
│   │       └── RecipeManagementPage.tsx       # レシピ管理（実装済み）
│   ├── store/                                 # Redux状態管理（実装済み）
│   │   ├── slices/                            # Reduxスライス
│   │   │   ├── authSlice.ts                   # 認証状態管理（実装済み）
│   │   │   ├── reviewSlice.ts                 # レビュー状態管理（実装済み）
│   │   │   ├── scheduleSlice.ts               # スケジュール状態管理（実装済み）
│   │   │   ├── shoppingListSlice.ts           # 買い物リスト状態管理（実装済み）
│   │   │   └── adminSlice.ts                  # 管理者状態管理（実装済み）
│   │   ├── recipeSlice.ts                     # レシピ状態管理（実装済み）
│   │   └── store.ts                           # Reduxストア設定（実装済み）
│   ├── api/                                   # API呼び出し（実装済み）
│   │   ├── userApi.ts                         # ユーザーAPI（実装済み）
│   │   ├── recipeApi.ts                       # レシピAPI（実装済み）
│   │   ├── reviewApi.ts                       # レビューAPI（実装済み）
│   │   ├── scheduleApi.ts                     # スケジュールAPI（実装済み）
│   │   ├── alertApi.ts                        # アラートAPI（実装済み）
│   │   ├── shoppingListApi.ts                 # 買い物リストAPI（実装済み）
│   │   ├── adminApi.ts                        # 管理者API（実装済み）
│   │   └── aiAdvisorApi.ts                    # （未実装）
│   ├── hooks/                                 # カスタムフック（実装済み）
│   │   ├── useAuth.ts                         # 認証フック（実装済み）
│   │   ├── useForm.ts                         # フォームフック（実装済み）
│   │   ├── useReview.ts                       # レビューフック（実装済み）
│   │   ├── useError.ts                        # エラーハンドリングフック（実装済み）
│   │   ├── useRecipe.ts                       # （未実装）
│   │   └── useAlert.ts                        # （未実装）
│   ├── lib/                                   # ライブラリユーティリティ（実装済み）
│   │   └── utils.ts                           # cn()関数など（実装済み）
│   ├── utils/                                 # ユーティリティ関数（実装済み）
│   │   ├── validation.ts                      # バリデーション関数（実装済み）
│   │   ├── apiClient.ts                       # API共通ユーティリティ（エラーハンドリング強化）（実装済み）
│   │   ├── dateUtils.ts                       # （未実装）
│   │   └── normalize.ts                       # （未実装）
│   ├── i18n/                                  # 多言語対応（実装済み）
│   │   ├── i18n.ts                            # i18next設定（localStorage、ブラウザ言語設定対応）（実装済み）
│   │   └── locales/
│   │       ├── ja.json                        # 日本語翻訳（認証、バリデーション、画像アップロード、レシピ、レビュー、スケジュール、アラート、買い物リスト、プロフィール、管理者）（実装済み）
│   │       └── ko.json                        # 韓国語翻訳（認証、バリデーション、画像アップロード、レシピ、レビュー、スケジュール、アラート、買い物リスト、プロフィール、管理者）（実装済み）
│   ├── types/                                 # TypeScript型定義（実装済み）
│   │   ├── user.ts                            # ユーザー型（実装済み）
│   │   ├── auth.ts                            # 認証型（実装済み）
│   │   ├── recipe.ts                          # レシピ型（実装済み）
│   │   ├── review.ts                          # レビュー型（実装済み）
│   │   ├── schedule.ts                        # スケジュール型（実装済み）
│   │   ├── shoppingList.ts                    # 買い物リスト型（実装済み）
│   │   └── admin.ts                           # 管理者型（実装済み）
│   ├── App.tsx                                # ルートコンポーネント（実装済み）
│   ├── index.tsx                              # エントリーポイント（実装済み）
│   ├── index.css                              # グローバルスタイル（Tailwind）（実装済み）
│   └── vite-env.d.ts                          # Vite環境変数型定義（実装済み）
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
- ロギング：INFOレベルでログ記録、フィールド名とエラー内容を記録

**2. 認証エラー（401 Unauthorized）**
- 認証情報が無効または期限切れの場合
- 対応：ログイン画面にリダイレクト
- ロギング：INFOレベルでログ記録

**3. 認可エラー（403 Forbidden）**
- リソースへのアクセス権限がない場合
- 例：他人のレシピを編集しようとした場合
- 対応：エラーメッセージを表示し、前の画面に戻る
- ロギング：INFOレベルでログ記録、アクセス試行の詳細を記録

**4. リソース未検出エラー（404 Not Found）**
- 指定されたリソースが存在しない場合
- 例：削除されたレシピにアクセス
- 対応：「削除されたレシピです」メッセージを表示
- ロギング：INFOレベルでログ記録

**5. サーバーエラー（500 Internal Server Error）**
- システム内部エラー
- 対応：汎用エラーメッセージを表示し、CloudWatchにログ記録
- ロギング：ERRORレベルでログ記録、スタックトレース（最初の10行）を含む

**6. 外部APIエラー**
- Gemini APIの呼び出し失敗
- 対応：
  - 24時間以内のキャッシュがあれば表示
  - キャッシュがなければ再試行ボタンと静的ヒントを表示
  - レート制限の場合は30秒後に自動再試行

### エラーレスポンス形式

**バックエンド（実装済み）**：
```json
{
  "code": "VALIDATION_ERROR",
  "message": "パスワードは8文字以上で、大文字、小文字、数字を含む必要があります",
  "details": {
    "password": "パスワードは8文字以上である必要があります"
  },
  "timestamp": "2024-11-28T12:00:00"
}
```

**フロントエンド（実装済み）**：
- ErrorBannerコンポーネントで画面上部に表示
- 再試行ボタンと閉じるボタンを提供
- 多言語対応（日本語・韓国語）

### ロギング戦略（実装済み）

**バックエンド**：
- **GlobalExceptionHandler**：すべての例外を捕捉し、適切なログレベルで記録
  - バリデーションエラー：INFO
  - 認証・認可エラー：INFO
  - リソース未検出エラー：INFO
  - サーバーエラー：ERROR（スタックトレース含む）
- **logback-spring.xml**：
  - ローカル開発環境：DEBUGレベル
  - 本番環境：INFOレベル以上
  - コンソールとファイルに出力
  - ファイルローテーション：日次、30日保持

**フロントエンド**：
- **apiClient.ts**：
  - Accept-Languageヘッダーを自動追加
  - ステータスコード別の詳細なエラーメッセージ
  - ネットワークエラーの適切なハンドリング
- **useError カスタムフック**：
  - エラーハンドリングのロジックを集約
  - ネットワークエラーの検出と適切なメッセージ表示

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

### エラーハンドリングコンポーネント（実装済み）

**バックエンド**：
- `GlobalExceptionHandler.java`：グローバル例外ハンドラー
  - すべての例外タイプに対応
  - 多言語対応（Accept-Languageヘッダー処理）
  - 詳細なログ記録

**フロントエンド**：
- `ErrorBanner.tsx`：エラーバナーコンポーネント
  - 画面上部に固定表示
  - 再試行ボタンと閉じるボタン
  - shadcn/ui Alert コンポーネント使用
- `useError.ts`：エラーハンドリングカスタムフック
  - エラー状態管理
  - エラーメッセージの変換
- `apiClient.ts`：API共通ユーティリティ
  - エラーレスポンスの型定義
  - ステータスコード別のエラーハンドリング
  - ネットワークエラーの検出

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

**インフラストラクチャ管理**：
- **AWS CDK** - Infrastructure as Code（TypeScript）
- バージョン：AWS CDK 2.110.0
- 定義場所：`infrastructure/cdk/`
- Lambda関数としてバックエンドをデプロイ
- CloudFront + S3でフロントエンドを配信

**インフラリソース**：
- DynamoDB：5テーブル（Users, Recipes, Schedules, ShoppingLists, Reviews）
- S3：画像ストレージバケット
- Cognito：ユーザープール
- API Gateway：REST API

**デプロイメントフロー**：
1. mainブランチへのマージをトリガー
2. CI/CDパイプライン（GitHub Actions / AWS CodePipeline）が起動
3. インフラ：AWS CDKでリソースをプロビジョニング
4. バックエンド：Gradleビルド → Lambda関数デプロイ
5. フロントエンド：npmビルド → S3アップロード → CloudFront無効化

### 環境管理

**ローカル開発環境（local）**：
- **LocalStack** - AWSサービスのローカルエミュレーション
- Docker Composeで起動：`docker-compose up -d`
- エンドポイント：`http://localhost:4566`
- 設定ファイル：`backend/src/main/resources/application-local.yml`
- 初期化スクリプト：`infrastructure/localstack/`
- **利点**：
  - AWSアカウント不要
  - 完全無料
  - オフライン開発可能
  - 高速なイテレーション

**開発環境（dev）**：
- AWS CDKでデプロイ：`cdk deploy CookingAppStack-Dev`
- 開発者間で共有する環境
- 統合テスト用

**ステージング環境（staging）**：
- 本番環境と同等の構成
- ユーザー受け入れテスト
- パフォーマンステスト

**本番環境（production）**：
- AWS CDKでデプロイ：`cdk deploy CookingAppStack-Prod`
- 実際のユーザーが利用する環境
- 高可用性とスケーラビリティを確保
- Point-in-Time Recovery有効化

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


## 実装状況サマリー

### 完了済み機能

**1. スケジュール管理機能（✅ 完了）**
- バックエンド：
  - スケジュール作成（日付、タイプ、レシピID、レシピ名、メモ）
  - スケジュール更新（メモ更新）
  - スケジュール削除（権限チェック付き）
  - スケジュール一覧取得（日付範囲検索）
  - 予定を実績に変換（LastCookingDate更新）
  - DynamoDB統合（Schedules テーブル）
  - バリデーション（Jakarta Validation）
- フロントエンド：
  - SchedulePage: スケジュール管理画面（検索、作成、更新、削除、変換）
  - Redux状態管理（scheduleSlice）
  - API呼び出し（scheduleApi）
  - 多言語対応（日本語・韓国語）
  - フォームバリデーション

**2. レビュー機能（✅ 完了）**
- バックエンド：
  - レビュー作成（星評価1-5、コメント300文字以内）
  - レビュー更新（自分のレビューのみ）
  - レビュー削除（自分のレビューのみ）
  - レシピIDでレビュー一覧取得（表示可能なレビューのみ）
  - レビュー通報（通報カウント増加、3回以上で自動非表示）
  - DynamoDB統合（Reviews テーブル、GSI_User）
  - バリデーション（Jakarta Validation）
- フロントエンド：
  - ReviewList: レビュー一覧コンポーネント（星評価表示、編集・削除・通報ボタン）
  - ReviewForm: レビュー投稿フォームコンポーネント（星評価選択、コメント入力）
  - Redux状態管理（reviewSlice）
  - API呼び出し（reviewApi）
  - カスタムフック（useReview）
  - RecipeDetailPageにレビューセクション統合
  - 多言語対応（日本語・韓国語）
  - フォームバリデーション

**3. ユーザー管理機能（✅ 完了）**
- ユーザー登録（Cognito + DynamoDB）
- ログイン（Cognito認証）
- プロフィール取得・更新
- アカウント削除
- メール確認機能
- 確認コード再送信
- プロフィール画像アップロード（S3）

**4. 画像管理機能（✅ 完了）**
- S3統合（LocalStack対応）
- 画像バリデーション（サイズ、フォーマット）
- Pre-signed URL生成
- 画像アップロード・取得・削除

**5. レシピ管理機能（✅ 完了）**
- レシピ作成
- レシピ更新
- レシピ削除（論理削除）
- レシピ詳細取得
- レシピ検索（キーワード、作成者）
- レシピ画像アップロード
- 食材管理（Ingredient Value Object）
- 単位管理（Unit Enum）
- フロントエンド：
  - レシピ検索ページ（RecipeSearchPage）
  - レシピ詳細ページ（RecipeDetailPage）
  - レシピ編集ページ（RecipeEdit/: RecipeEditPage + useRecipeEditHandlers + recipeEditConfig）
    - 3層分離パターン適用（表示・操作・属性）
  - Redux状態管理（recipeSlice）
  - 多言語対応（日本語・韓国語）
  - shadcn/uiコンポーネント（Input、Label、Textarea）

**6. 認証フロー（✅ 完了）**
- ログインページ（Login/: LoginPage + loginFormConfig）
- ユーザー登録ページ（Register/: RegisterPage + registerFormConfig）
- メール確認ページ（ConfirmEmail/: ConfirmEmailPage + confirmEmailFormConfig）
- パスワードリセットページ（プレースホルダー）
- 2層分離パターン適用（表示+ロジック・設定）

**7. 共通コンポーネント（✅ 完了）**
- FormField（再利用可能なフォームフィールド）
- ImageUploader（ドラッグ&ドロップ対応）
- Header（アプリケーション全体のナビゲーションバー）
- Footer（アプリケーション全体のフッター）
- LoadingSkeleton（ローディング中のプレースホルダー）
- ErrorBanner（エラーバナーコンポーネント）
- shadcn/uiコンポーネント（Button、Card、Input、Label、Textarea、Alert、Dialog）
- Tailwind CSS統合
- カスタムフック（useForm、useAuth、useError、useReview）

**8. インフラストラクチャ（✅ 完了）**
- LocalStack環境構築
- DynamoDB テーブル作成（Users, Recipes, Reviews）
- S3バケット作成
- Cognito ユーザープール設定
- Docker Compose設定

**9. 多言語対応（✅ 完了）**
- バックエンド：
  - messages_ja.properties（日本語メッセージファイル）
  - messages_ko.properties（韓国語メッセージファイル）
  - MessageConfig（MessageSource、LocaleResolver設定）
  - GlobalExceptionHandler（Accept-Languageヘッダー処理、国際化対応）
  - エラーメッセージ、バリデーションメッセージ、成功メッセージ
- フロントエンド：
  - i18next設定（localStorage、ブラウザ言語設定からの初期言語取得）
  - LanguageSelector（言語選択UIコンポーネント、localStorage保存）
  - Select UIコンポーネント（shadcn/ui）
  - 認証、レシピ、レビュー、スケジュール、アラート、買い物リスト、プロフィール、エラーメッセージの翻訳

**10. エラーハンドリングとロギング（✅ 完了）**
- バックエンド：
  - GlobalExceptionHandler（グローバル例外ハンドラー）
    - すべての例外タイプに対応（バリデーション、認証、認可、リソース未検出、サーバーエラー）
    - INFOレベル以上のログ記録
    - 多言語対応（Accept-Languageヘッダー処理）
    - 詳細なエラーメッセージとスタックトレース
  - logback-spring.xml（CloudWatchロギング設定）
    - INFOレベル以上のログをコンソールとファイルに出力
    - ファイルローテーション（日次、30日保持）
- フロントエンド：
  - ErrorBanner（エラーバナーコンポーネント）
    - 画面上部に固定表示
    - 再試行ボタンと閉じるボタン
    - shadcn/ui Alert コンポーネント使用
  - useError（エラーハンドリングカスタムフック）
    - エラー状態管理
    - ネットワークエラーの検出と適切なメッセージ表示
  - apiClient.ts（API共通ユーティリティ強化）
    - Accept-Languageヘッダーの自動追加
    - ステータスコード別の詳細なエラーメッセージ
    - ネットワークエラーの適切なハンドリング
  - 多言語対応（日本語・韓国語のエラーメッセージ）

**11. ダッシュボードとホーム画面（✅ 完了）**
- DashboardPage（ホーム画面）
  - 最近のレシピ表示（最大3件、レシピ詳細へのナビゲーション）
  - スケジュール概要表示（今後7日間、最大5件）
  - 買い物リスト概要表示（未チェックのみ、最大5件）
  - クイックアクションセクション（レシピ作成、予定追加、買い物リスト追加、レシピ検索）
  - LoadingSkeletonによるローディング表示
  - レスポンシブデザイン（Tailwind CSSのグリッドシステム）
  - 多言語対応（日本語・韓国語）
- Header（ナビゲーションバー）
  - ロゴとナビゲーションメニュー（レシピ検索、スケジュール、買い物リスト）
  - ログイン/ログアウト機能
  - レスポンシブデザイン
  - 認証状態に応じた表示切り替え
  - 認証ページでは非表示
- Footer（フッター）
  - アプリケーション名と著作権表示
  - シンプルで控えめなデザイン
  - 認証ページでは非表示
- LoadingSkeleton（ローディングプレースホルダー）
  - 3つのタイプ（card、list、text）をサポート
  - アニメーション付きのスケルトン表示
  - カスタマイズ可能（行数、クラス名）

**12. 管理者機能（✅ 完了）**
- バックエンド：
  - GetAdminDashboardStatsUseCase: ダッシュボード統計取得
  - SuspendUserUseCase: ユーザー停止
  - DeleteUserByAdminUseCase: ユーザー削除
  - GetAllRecipesForAdminUseCase: すべてのレシピ取得
  - SetRecipeStatusUseCase: レシピステータス設定
  - DeleteRecipeByAdminUseCase: レシピ削除
  - AdminController: REST APIエンドポイント（/api/admin/*）
  - AdminDashboardResponse: ダッシュボードレスポンスDTO
  - SetRecipeStatusRequest: レシピステータス設定リクエストDTO
- フロントエンド：
  - AdminDashboardPage: 管理者ダッシュボード（統計情報表示、ナビゲーション）
  - UserManagementPage: ユーザー管理（停止、削除、確認ダイアログ）
  - RecipeManagementPage: レシピ管理（一覧表示、ステータス切り替え、削除）
  - adminSlice: Redux状態管理（fetchAdminDashboardStats、suspendUser、deleteUserByAdmin、fetchAllRecipesForAdmin、setRecipeStatus、deleteRecipeByAdmin）
  - adminApi: API呼び出し関数
  - Admin型定義: TypeScript型定義（AdminDashboardStats、SetRecipeStatusRequest、AdminState）
  - App.tsxルーティング追加（/admin、/admin/users、/admin/recipes）
  - Headerに管理者メニュー追加
  - 多言語対応（日本語・韓国語）

### 未実装機能

**1. AIアドバイザー機能**
- Gemini API統合
- キャッシュ管理
- レート制限処理

### 技術スタック

**バックエンド**
- Java 21+
- Spring Boot 3.x
- Gradle 8.11.1
- AWS SDK for Java v2
- DynamoDB
- S3
- Cognito
- Lombok（最小限使用：`@Builder`, `@Getter`のみ、セキュリティ考慮）
- SLF4J（ロギング：`Logger`直接定義、`@Slf4j`不使用）

**フロントエンド**
- TypeScript
- React 18
- Redux Toolkit
- React Router
- i18next（多言語対応）
- shadcn/ui + Radix UI
- Tailwind CSS
- Vite
- Fetch API（axios不使用、バンドルサイズ最適化）

**インフラストラクチャ**
- LocalStack（ローカル開発）
- Docker Compose
- AWS CDK（本番環境、未実装）

**テスト**
- jqwik（Property-Based Testing、未実装）
- fast-check（Property-Based Testing、未実装）
- JUnit 5（Unit Testing、未実装）
- Vitest（Unit Testing、未実装）

### コード品質とベストプラクティス

**バックエンド**
- ✅ **テーブル名管理**: すべてのDynamoDBリポジトリで`@Value`アノテーション + コンストラクタインジェクションを使用
- ✅ **ロギング**: `Logger`直接定義（セキュリティ重視、`@Slf4j`不使用）
- ✅ **依存関係の最小化**: Lombokの使用を安全な機能のみに限定
- ✅ **設定の一元管理**: `application.yml`でテーブル名を管理
- ✅ **Clean Architecture**: レイヤー分離の徹底

**フロントエンド**
- ✅ **API共通化**: `apiClient.ts`で重複コード削減（59%削減）
- ✅ **エラーハンドリング**: 一元管理されたエラー処理
- ✅ **型安全性**: TypeScriptジェネリクスの活用
- ✅ **バンドルサイズ最適化**: 依存関係の最小化（355KB、10%削減）
- ✅ **コンポーネント設計**: 責任の明確な分離（表示・操作・属性）

**セキュリティ**
- ✅ **透明性**: コード生成ツールの使用を最小限に
- ✅ **ログ出力**: 機密情報のマスキング
- ✅ **依存関係**: 業界標準ライブラリの使用（SLF4J、Fetch API）

### 次のステップ

1. AIアドバイザー機能の実装（タスク6）
2. 管理者機能の実装（タスク12）
3. Property-Based Testingの実装（各機能のテストタスク）
4. 最終チェックポイント - すべてのテストが合格することを確認（タスク15）
