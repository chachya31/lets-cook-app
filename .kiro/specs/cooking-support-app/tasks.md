# Implementation Plan

- [x] 1. プロジェクト初期セットアップ
  - バックエンド（Spring Boot + Gradle）とフロントエンド（React + TypeScript）のプロジェクト構造を作成
  - 依存関係の設定（jqwik、fast-check、i18next、shadcn/ui等）
  - Clean Architectureに基づくディレクトリ構造の構築
  - Gradle 8.11.1、Java 21+対応
  - shadcn/ui + Tailwind CSS統合
  - _Requirements: 全体_

- [x] 2. AWS インフラストラクチャのセットアップ
  - DynamoDBテーブルの作成（Users、Recipes、Schedules、ShoppingLists、Reviews）
  - S3バケットの作成（画像ストレージ）
  - Cognito ユーザープールの設定
  - API Gatewayの設定
  - AWS CDK定義の作成
  - LocalStack環境の構築と検証
  - _Requirements: 1.1, 1.2, 3.5, 11.5_

- [x] 3. ユーザー管理機能の実装
- [x] 3.1 ドメイン層：Userエンティティとバリューオブジェクトの実装
  - Userエンティティ（userId、email、nickname、profileImageUrl、preferredLanguage、lastCookingDate）
  - パスワードバリデーションロジック
  - _Requirements: 1.1, 11.1_

- [ ]* 3.2 プロパティテスト：アカウント作成の成功
  - **Property 1: アカウント作成の成功**
  - **Validates: Requirements 1.1**

- [ ]* 3.3 プロパティテスト：パスワードバリデーション
  - **Property 44: パスワードバリデーション**
  - **Validates: Requirements 11.1**

- [x] 3.4 インフラ層：CognitoAuthServiceの実装
  - Cognito統合（ユーザー登録、ログイン、トークン管理）
  - セッション管理（アクセストークン1時間、リフレッシュトークン90日）
  - 例外クラス（AuthenticationException、UserAlreadyExistsException）
  - AuthTokens DTO
  - CognitoConfig設定クラス
  - _Requirements: 1.2, 1.3, 11.5_

- [ ]* 3.5 プロパティテスト：認証の成功
  - **Property 2: 認証の成功**
  - **Validates: Requirements 1.2**

- [ ]* 3.6 プロパティテスト：同時セッションの許可
  - **Property 45: 同時セッションの許可**
  - **Validates: Requirements 11.3**

- [x] 3.7 インフラ層：UserRepositoryの実装
  - UserRepositoryインターフェース（save、findById、findByEmail、delete、existsById）
  - DynamoDBUserRepository実装（CRUD操作）
  - DynamoDBConfig設定クラス
  - _Requirements: 1.1, 1.5_

- [x] 3.8 アプリケーション層：ユーザー管理ユースケースの実装
  - RegisterUserUseCase: ユーザー登録（パスワードバリデーション、重複チェック、Cognito登録、DynamoDB保存）
  - LoginUserUseCase: ログイン（Cognito認証、最終ログイン日時更新）
  - UpdateUserProfileUseCase: プロフィール更新、プロフィール画像URL更新
  - DeleteUserAccountUseCase: アカウント削除（Cognito削除、DynamoDB削除）
  - GetUserProfileUseCase: ユーザープロフィール取得
  - _Requirements: 1.1, 1.2, 1.5_

- [ ]* 3.9 プロパティテスト：アカウント削除時の匿名化
  - **Property 4: アカウント削除時の匿名化**
  - **Validates: Requirements 1.5**

- [x] 3.10 プレゼンテーション層：UserControllerの実装
  - UserController: REST APIエンドポイント（POST /register, POST /login, GET /profile/{userId}, PUT /profile/{userId}, DELETE /account/{userId}）
  - リクエストDTO: RegisterUserRequest, LoginRequest, UpdateProfileRequest
  - レスポンスDTO: UserResponse, LoginResponse
  - GlobalExceptionHandler: グローバル例外ハンドラー（バリデーションエラー、認証エラー、ユーザー重複エラー等）
  - バリデーション: Jakarta Validation（@NotBlank, @Email, @Size, @Pattern）
  - バリデーション（フロントエンド・バックエンド両方）
  - _Requirements: 1.1, 1.2, 1.5_

- [x] 3.11 フロントエンド：認証コンポーネントの実装
  - 機能ごとにフォルダ分け（Login/、Register/、PasswordReset/）
  - 各フォルダにページコンポーネントと設定ファイルを配置
  - Login/LoginPage.tsx: ログインフォーム（バリデーション、エラー表示）
  - Login/loginFormConfig.ts: フォーム設定（バリデーションルール、フィールド定義）
  - Register/RegisterPage.tsx: ユーザー登録フォーム（パスワード確認、言語選択）
  - Register/registerFormConfig.ts: フォーム設定（バリデーションルール、フィールド定義、言語オプション）
  - PasswordReset/PasswordResetPage.tsx: パスワードリセット（プレースホルダー）
  - common/FormField.tsx: 再利用可能なフォームフィールドコンポーネント
  - Redux状態管理（authSlice）: login, register, logout アクション
  - useAuth カスタムフック: 認証状態とアクションを提供
  - useForm カスタムフック: フォーム状態管理とバリデーション
  - userApi: API呼び出し関数（registerUser, loginUser, getUserProfile）
  - utils/validation.ts: バリデーションルール（required, email, minLength, maxLength, pattern, matchField）
  - 型定義: User, RegisterRequest, LoginRequest, LoginResponse, AuthState
  - i18n翻訳: 日本語・韓国語対応
  - App.tsx: ルーティング設定（/login, /register, /password-reset）
  - _Requirements: 1.1, 1.2_

- [x] 4. プロフィール画像管理機能の実装
- [x] 4.1 インフラ層：S3ImageServiceの実装
  - 画像アップロード（pre-signed URL使用）
  - 画像取得
  - 画像削除
  - _Requirements: 1.4, 3.5_

- [x] 4.2 アプリケーション層：画像バリデーションロジックの実装
  - ファイルサイズチェック（5MB以下）
  - フォーマットチェック（JPEG、PNG）
  - _Requirements: 1.4, 3.5_

- [ ]* 4.3 プロパティテスト：プロフィール画像バリデーション
  - **Property 3: プロフィール画像バリデーション**
  - **Validates: Requirements 1.4**

- [x] 4.4 プレゼンテーション層：画像アップロードエンドポイントの実装
  - POST /api/users/profile/image
  - POST /api/recipes/{id}/image
  - _Requirements: 1.4, 3.5_

- [x] 4.5 フロントエンド：ImageUploaderコンポーネントの実装
  - ドラッグ&ドロップ対応
  - プレビュー表示
  - バリデーション（サイズ、フォーマット）
  - _Requirements: 1.4_

- [x] 5. レシピ管理機能の実装
- [x] 5.1 ドメイン層：Recipeエンティティとバリューオブジェクトの実装
  - Recipeエンティティ（recipeId、title、authorId、ingredients、steps、cookingTime、imageUrl、isPublic、isDeleted）
  - Ingredientバリューオブジェクト（name、quantity、unit、note、optional）
  - Unit Enum（g、kg、ml、l、tbsp、tsp、cup、piece、pack、can、bottle、slice、clove、pinch、to_taste、as_needed）
  - 食材バリデーションロジック
  - 論理削除機能（markAsDeleted）
  - 権限チェック（canEdit、canDelete）
  - _Requirements: 3.1, 3.2_

- [ ]* 5.2 プロパティテスト：食材バリデーション
  - **Property 10: 食材バリデーション**
  - **Validates: Requirements 3.2**

- [x] 5.3 インフラ層：RecipeRepositoryの実装
  - RecipeRepositoryインターフェース（save、findById、findByAuthorId、findAllPublic、delete、existsById）
  - DynamoDBRecipeRepository実装（CRUD操作）
  - GSI検索（GSI_Author: AuthorId）
  - 食材・手順のシリアライズ/デシリアライズ
  - _Requirements: 2.1, 3.1, 3.3, 3.4_

- [x] 5.4 アプリケーション層：レシピ管理ユースケースの実装
  - CreateRecipeUseCase: レシピ作成
  - UpdateRecipeUseCase: レシピ更新（権限チェック付き）
  - DeleteRecipeUseCase: レシピ削除（論理削除、権限チェック付き）
  - GetRecipeUseCase: レシピ詳細取得
  - SearchRecipesUseCase: レシピ検索（公開レシピ、作成者別、キーワード）
  - UploadRecipeImageUseCase: レシピ画像アップロード（S3統合）
  - RecipeNotFoundException、UnauthorizedException例外クラス
  - _Requirements: 2.1, 3.1, 3.3, 3.4_

- [ ]* 5.5 プロパティテスト：レシピ作成の成功
  - **Property 9: レシピ作成の成功**
  - **Validates: Requirements 3.1**

- [ ]* 5.6 プロパティテスト：レシピ更新の反映
  - **Property 11: レシピ更新の反映**
  - **Validates: Requirements 3.3**

- [ ]* 5.7 プロパティテスト：レシピ削除時の参照保持
  - **Property 12: レシピ削除時の参照保持**
  - **Validates: Requirements 3.4**

- [x] 5.8 プレゼンテーション層：RecipeControllerの実装
  - RecipeController: REST APIエンドポイント
    - GET /api/recipes - レシピ検索（keyword、authorIdパラメータ）
    - GET /api/recipes/{id} - レシピ詳細取得
    - POST /api/recipes - レシピ作成（X-User-Idヘッダー）
    - PUT /api/recipes/{id} - レシピ更新（X-User-Idヘッダー）
    - DELETE /api/recipes/{id} - レシピ削除（論理削除、X-User-Idヘッダー）
    - POST /api/recipes/{id}/image - レシピ画像アップロード
  - RecipeRequest DTO（title、ingredients、steps、cookingTime）
  - RecipeResponse DTO（recipeId、title、authorId、ingredients、steps、cookingTime、imageUrl、isPublic、createdAt、updatedAt）
  - IngredientDto（name、quantity、unit、note、optional）
  - Jakarta Validationバリデーション（@NotBlank、@Size、@Min、@DecimalMin、@Valid）
  - GlobalExceptionHandlerに例外ハンドラー追加（RecipeNotFoundException、UnauthorizedException）
  - _Requirements: 2.1, 2.2, 3.1, 3.3, 3.4_

- [ ]* 5.9 プロパティテスト：レシピ検索の一致
  - **Property 5: レシピ検索の一致**
  - **Validates: Requirements 2.1**

- [ ]* 5.10 プロパティテスト：レシピ詳細の完全性
  - **Property 6: レシピ詳細の完全性**
  - **Validates: Requirements 2.2**

- [ ]* 5.11 プロパティテスト：食材情報の完全性
  - **Property 8: 食材情報の完全性**
  - **Validates: Requirements 2.5**

- [ ]* 5.12 プロパティテスト：レシピ画像のストレージとバリデーション
  - **Property 13: レシピ画像のストレージとバリデーション**
  - **Validates: Requirements 3.5**

- [x] 5.13 フロントエンド：レシピコンポーネントの実装
  - RecipeSearchPage: レシピ検索画面（キーワード検索、レシピカード表示）
  - RecipeDetailPage: レシピ詳細画面（食材リスト、手順表示、編集・削除ボタン）
  - RecipeEditPage: レシピ編集画面（作成・更新両対応、食材・手順の動的追加/削除）
  - recipeSlice: Redux状態管理（searchRecipes、fetchRecipe、createRecipe、updateRecipe、deleteRecipe、uploadRecipeImage）
  - recipeApi: API呼び出し関数（searchRecipes、getRecipe、createRecipe、updateRecipe、deleteRecipe、uploadRecipeImage）
  - Recipe型定義（Recipe、Ingredient、RecipeRequest、RecipeSearchParams）
  - shadcn/uiコンポーネント追加（Input、Label、Textarea）
  - i18n翻訳追加（日本語・韓国語）
  - App.tsxルーティング追加（/recipes、/recipes/new、/recipes/:id、/recipes/:id/edit）
  - _Requirements: 2.1, 2.2, 3.1, 3.3_

- [ ] 6. AIアドバイザー機能の実装
- [ ] 6.1 インフラ層：GeminiAPIServiceの実装
  - Gemini API統合
  - エラーハンドリング（API失敗、レート制限）
  - _Requirements: 4.1, 4.4, 4.5_

- [ ]* 6.2 プロパティテスト：AI APIの呼び出し
  - **Property 14: AI APIの呼び出し**
  - **Validates: Requirements 4.1**

- [ ] 6.3 インフラ層：CacheServiceの実装
  - 24時間キャッシュ管理
  - キャッシュ取得・保存・削除
  - _Requirements: 4.2_

- [ ] 6.4 アプリケーション層：AIアドバイザーユースケースの実装
  - AIアドバイス取得
  - キャッシュフォールバック
  - レート制限時の自動再試行（30秒後）
  - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5_

- [ ] 6.5 プレゼンテーション層：AIAdvisorControllerの実装
  - POST /api/ai-advisor/advice
  - エラーレスポンス（キャッシュ有無、レート制限）
  - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5_

- [ ] 6.6 フロントエンド：AIAdvisorPanelコンポーネントの実装
  - AIアドバイス表示
  - 再試行ボタン
  - レート制限バナー
  - 静的ヒント表示
  - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5_

- [x] 7. レビュー機能の実装
- [x] 7.1 ドメイン層：Reviewエンティティの実装
  - Reviewエンティティ（reviewId、recipeId、userId、rating、comment、status、reportedCount、createdAt、updatedAt）
  - ReviewStatus Value Object（VISIBLE、HIDDEN）
  - 通報カウント増加ロジック（incrementReportCount）
  - 自動非表示判定ロジック（shouldHide）
  - 権限チェック（canEdit）
  - バリデーション（rating 1-5、comment 300文字以内）
  - _Requirements: 5.1, 5.3, 5.4_

- [x] 7.2 インフラ層：ReviewRepositoryの実装
  - ReviewRepositoryインターフェース（save、findById、findByRecipeId、findByUserId、delete、existsById）
  - DynamoDBReviewRepository実装（CRUD操作）
  - GSI検索（GSI_User: UserId + CreatedAt）
  - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5_

- [x] 7.3 アプリケーション層：レビュー管理ユースケースの実装
  - CreateReviewUseCase: レビュー作成
  - UpdateReviewUseCase: レビュー更新（権限チェック付き）
  - DeleteReviewUseCase: レビュー削除（権限チェック付き）
  - GetReviewsByRecipeUseCase: レシピIDでレビュー一覧取得（表示可能なレビューのみ）
  - ReportReviewUseCase: レビュー通報（通報カウント増加、3回以上で自動非表示）
  - ReviewNotFoundException例外クラス
  - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5_

- [ ]* 7.4 プロパティテスト：レビュー作成の成功
  - **Property 15: レビュー作成の成功**
  - **Validates: Requirements 5.1**

- [ ]* 7.5 プロパティテスト：自分のレビューの編集・削除権限
  - **Property 16: 自分のレビューの編集・削除権限**
  - **Validates: Requirements 5.2**

- [ ]* 7.6 プロパティテスト：レビュー通報カウントの増加
  - **Property 17: レビュー通報カウントの増加**
  - **Validates: Requirements 5.3**

- [ ]* 7.7 プロパティテスト：通報による自動非表示
  - **Property 18: 通報による自動非表示**
  - **Validates: Requirements 5.4**

- [ ]* 7.8 プロパティテスト：表示可能レビューのみの表示
  - **Property 19: 表示可能レビューのみの表示**
  - **Validates: Requirements 5.5**

- [ ]* 7.9 プロパティテスト：表示可能レビューのフィルタリング
  - **Property 7: 表示可能レビューのフィルタリング**
  - **Validates: Requirements 2.3**

- [x] 7.10 プレゼンテーション層：ReviewControllerの実装
  - ReviewController: REST APIエンドポイント
    - GET /api/recipes/{recipeId}/reviews - レビュー一覧取得
    - POST /api/recipes/{recipeId}/reviews - レビュー作成（X-User-Idヘッダー）
    - PUT /api/reviews/{reviewId} - レビュー更新（X-User-Idヘッダー）
    - DELETE /api/reviews/{reviewId} - レビュー削除（X-User-Idヘッダー）
    - POST /api/reviews/{reviewId}/report - レビュー通報
  - CreateReviewRequest DTO（rating、comment）
  - UpdateReviewRequest DTO（rating、comment）
  - ReviewResponse DTO（reviewId、recipeId、userId、rating、comment、status、reportedCount、createdAt、updatedAt）
  - Jakarta Validationバリデーション（@NotNull、@Min、@Max、@Size）
  - GlobalExceptionHandlerに例外ハンドラー追加（ReviewNotFoundException）
  - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5_

- [x] 7.11 フロントエンド：レビューコンポーネントの実装
  - ReviewList: レビュー一覧コンポーネント（星評価表示、編集・削除・通報ボタン）
  - ReviewForm: レビュー投稿フォームコンポーネント（星評価選択、コメント入力、バリデーション）
  - reviewSlice: Redux状態管理（fetchReviewsByRecipe、createReview、updateReview、deleteReview、reportReview）
  - reviewApi: API呼び出し関数（getReviewsByRecipe、createReview、updateReview、deleteReview、reportReview）
  - useReview: カスタムフック（レビュー管理）
  - Review型定義（Review、CreateReviewRequest、UpdateReviewRequest、ReviewState）
  - RecipeDetailPageにレビューセクション追加
  - i18n翻訳追加（日本語・韓国語）
  - フォームバリデーション（星評価1-5、コメント300文字以内）
  - _Requirements: 5.1, 5.2, 5.3_

- [x] 8. スケジュール管理機能の実装
- [x] 8.1 ドメイン層：Scheduleエンティティの実装
  - Scheduleエンティティ（scheduleId、userId、date、type、recipeId、recipeTitle、memo）
  - ScheduleType Value Object（PLANNED、COOKED）
  - 予定→実績変換ロジック（convertToCooked）
  - メモ更新ロジック（updateMemo）
  - 権限チェック（canEdit）
  - _Requirements: 6.1, 6.2, 6.3, 6.4_

- [x] 8.2 インフラ層：ScheduleRepositoryの実装
  - ScheduleRepositoryインターフェース（save、findById、findByUserIdAndDateRange、delete、existsById）
  - DynamoDBScheduleRepository実装（CRUD操作）
  - SortKey構築（Date#Type#RecipeId形式）
  - 日付範囲検索（QueryRequest）
  - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5_

- [x] 8.3 アプリケーション層：スケジュール管理ユースケースの実装
  - CreateScheduleUseCase: スケジュール作成
  - UpdateScheduleUseCase: スケジュール更新（権限チェック付き）
  - DeleteScheduleUseCase: スケジュール削除（権限チェック付き）
  - GetSchedulesUseCase: スケジュール一覧取得（日付範囲検索）
  - ConvertScheduleToCookedUseCase: 予定→実績変換（LastCookingDate更新）
  - ScheduleNotFoundException例外クラス
  - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5_

- [ ]* 8.4 プロパティテスト：カレンダー表示の完全性
  - **Property 20: カレンダー表示の完全性**
  - **Validates: Requirements 6.1**

- [ ]* 8.5 プロパティテスト：料理実績登録とLastCookingDate更新
  - **Property 21: 料理実績登録とLastCookingDate更新**
  - **Validates: Requirements 6.2**

- [ ]* 8.6 プロパティテスト：料理予定の作成
  - **Property 22: 料理予定の作成**
  - **Validates: Requirements 6.3**

- [ ]* 8.7 プロパティテスト：予定から実績への変換
  - **Property 23: 予定から実績への変換**
  - **Validates: Requirements 6.4**

- [ ]* 8.8 プロパティテスト：過去のスケジュール編集・削除権限
  - **Property 24: 過去のスケジュール編集・削除権限**
  - **Validates: Requirements 6.5**

- [x] 8.9 プレゼンテーション層：ScheduleControllerの実装
  - ScheduleController: REST APIエンドポイント
    - GET /api/schedules - スケジュール一覧取得（startDate、endDateパラメータ、X-User-Idヘッダー）
    - POST /api/schedules - スケジュール作成（X-User-Idヘッダー）
    - PUT /api/schedules/{id} - スケジュール更新（X-User-Idヘッダー）
    - DELETE /api/schedules/{id} - スケジュール削除（X-User-Idヘッダー）
    - POST /api/schedules/{id}/convert-to-cooked - 予定を実績に変換（X-User-Idヘッダー）
  - CreateScheduleRequest DTO（date、type、recipeId、recipeTitle、memo）
  - UpdateScheduleRequest DTO（memo）
  - ScheduleResponse DTO（scheduleId、userId、date、type、recipeId、recipeTitle、memo、createdAt）
  - Jakarta Validationバリデーション（@NotBlank、@Pattern、@Size）
  - GlobalExceptionHandlerに例外ハンドラー追加（ScheduleNotFoundException）
  - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5_

- [x] 8.10 フロントエンド：スケジュールコンポーネントの実装
  - SchedulePage: スケジュール管理画面（検索、作成、更新、削除、予定→実績変換）
  - scheduleSlice: Redux状態管理（fetchSchedules、createSchedule、updateSchedule、deleteSchedule、convertToCooked）
  - scheduleApi: API呼び出し関数（getSchedules、createSchedule、updateSchedule、deleteSchedule、convertToCooked）
  - Schedule型定義（Schedule、CreateScheduleRequest、UpdateScheduleRequest、ScheduleSearchParams、ScheduleState）
  - i18n翻訳追加（日本語・韓国語）
  - App.tsxルーティング追加（/schedules）
  - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5_

- [x] 9. サボり防止アラート機能の実装
- [x] 9.1 アプリケーション層：アラート判定ロジックの実装
  - LastCookingDateから3日経過判定（4日目の0時）
  - アラートメッセージのランダム選択（警告/励まし）
  - CheckAlertUseCase: アラート判定ユースケース（実装済み）
  - AlertResponse: アラートレスポンスDTO（実装済み）
  - _Requirements: 7.1, 7.4_

- [ ]* 9.2 プロパティテスト：アラート表示判定
  - **Property 25: アラート表示判定**
  - **Validates: Requirements 7.1**

- [ ]* 9.3 プロパティテスト：アラートメッセージのランダム選択
  - **Property 28: アラートメッセージのランダム選択**
  - **Validates: Requirements 7.4**

- [ ]* 9.4 プロパティテスト：アラート非表示条件
  - **Property 29: アラート非表示条件**
  - **Validates: Requirements 7.5**

- [x] 9.5 プレゼンテーション層：アラート判定エンドポイントの実装
  - GET /api/alerts/check: アラート表示判定（X-User-Idヘッダー）
  - AlertController: REST APIコントローラー（実装済み）
  - _Requirements: 7.1_

- [x] 9.6 フロントエンド：AlertModalコンポーネントの実装
  - AlertModal: サボり防止アラートモーダル（実装済み）
  - モーダル表示（Dialog/shadcn/ui使用）
  - localStorageによる再表示制御（同日の再表示なし）
  - クイック料理登録ボタン（スケジュール画面に遷移）
  - ランダムメッセージ表示（警告/励まし）
  - alertApi: API呼び出し関数（実装済み）
  - i18n翻訳追加（日本語・韓国語）
  - App.tsxにAlertModal追加（実装済み）
  - _Requirements: 7.1, 7.2, 7.3_

- [ ]* 9.7 プロパティテスト：アラート再表示防止
  - **Property 26: アラート再表示防止**
  - **Validates: Requirements 7.2**

- [ ]* 9.8 プロパティテスト：クイック料理登録とアラート非表示
  - **Property 27: クイック料理登録とアラート非表示**
  - **Validates: Requirements 7.3**

- [x] 10. 買い物リスト管理機能の実装
- [x] 10.1 ドメイン層：ShoppingListItemエンティティの実装
  - ShoppingListItemエンティティ（itemId、userId、name、quantity、unit、isChecked、isCheckedAt、normalizedKey）
  - 正規化キー生成ロジック（名前+単位）
  - 数量更新（合算）ロジック
  - チェック済み/未チェック切り替え
  - 自動削除判定ロジック（チェック済みから3日経過）
  - 権限チェック（canEdit）
  - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5_

- [x] 10.2 インフラ層：ShoppingListRepositoryの実装
  - ShoppingListRepositoryインターフェース（save、findById、findByUserId、findByNormalizedKey、delete、existsById、deleteExpiredCheckedItems）
  - DynamoDBShoppingListRepository実装（CRUD操作）
  - GSI_NormalizedKey: 正規化キーによる検索
  - 期限切れアイテム削除
  - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5_

- [x] 10.3 アプリケーション層：買い物リスト管理ユースケースの実装
  - AddShoppingListItemUseCase: アイテム追加（数量合算ロジック）
  - UpdateShoppingListItemUseCase: アイテム更新（チェック状態）
  - DeleteShoppingListItemUseCase: アイテム削除
  - GetShoppingListUseCase: 買い物リスト取得
  - CleanupExpiredItemsUseCase: 期限切れアイテムクリーンアップ
  - ShoppingListItemNotFoundException例外クラス
  - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5_

- [ ]* 10.4 プロパティテスト：買い物リストアイテムの作成
  - **Property 30: 買い物リストアイテムの作成**
  - **Validates: Requirements 8.1**

- [ ]* 10.5 プロパティテスト：数量の合算
  - **Property 31: 数量の合算**
  - **Validates: Requirements 8.2**

- [ ]* 10.6 プロパティテスト：単位違いの別アイテム作成
  - **Property 32: 単位違いの別アイテム作成**
  - **Validates: Requirements 8.3**

- [ ]* 10.7 プロパティテスト：チェック済みフラグとタイムスタンプの設定
  - **Property 33: チェック済みフラグとタイムスタンプの設定**
  - **Validates: Requirements 8.4**

- [ ]* 10.8 プロパティテスト：チェック済みアイテムの自動削除
  - **Property 34: チェック済みアイテムの自動削除**
  - **Validates: Requirements 8.5**

- [x] 10.9 プレゼンテーション層：ShoppingListControllerの実装
  - ShoppingListController: REST APIエンドポイント
    - GET /api/shopping-lists - 買い物リスト取得（X-User-Idヘッダー、自動クリーンアップ）
    - POST /api/shopping-lists - アイテム追加（X-User-Idヘッダー）
    - PUT /api/shopping-lists/{id} - アイテム更新（チェック状態、X-User-Idヘッダー）
    - DELETE /api/shopping-lists/{id} - アイテム削除（X-User-Idヘッダー）
  - AddShoppingListItemRequest DTO（name、quantity、unit、sourceRecipeId）
  - UpdateShoppingListItemRequest DTO（isChecked）
  - ShoppingListItemResponse DTO（itemId、userId、name、quantity、unit、isChecked、isCheckedAt、addedAt、sourceRecipeId）
  - Jakarta Validationバリデーション（@NotBlank、@Size、@DecimalMin、@DecimalMax）
  - GlobalExceptionHandlerに例外ハンドラー追加（ShoppingListItemNotFoundException）
  - _Requirements: 8.1, 8.2, 8.3, 8.4_

- [x] 10.10 フロントエンド：買い物リストコンポーネントの実装
  - ShoppingListPage: 買い物リスト画面（アイテム追加、チェック状態切り替え、削除）
  - shoppingListSlice: Redux状態管理（fetchShoppingList、addShoppingListItem、updateShoppingListItem、deleteShoppingListItem）
  - shoppingListApi: API呼び出し関数（getShoppingList、addShoppingListItem、updateShoppingListItem、deleteShoppingListItem）
  - ShoppingListItem型定義: TypeScript型定義（ShoppingListItem、AddShoppingListItemRequest、UpdateShoppingListItemRequest、ShoppingListState）
  - Checkbox: UIコンポーネント（実装済み）
  - i18n翻訳追加（日本語・韓国語）
  - App.tsxルーティング追加（/shopping-list）
  - フォームバリデーション（名前100文字以内、数量0.01-9999）
  - _Requirements: 8.1, 8.2, 8.3, 8.4_

- [x] 11. 多言語対応機能の実装
- [x] 11.1 バックエンド：多言語メッセージファイルの作成
  - messages_ja.properties（日本語）
  - messages_ko.properties（韓国語）
  - エラーメッセージ、バリデーションメッセージ
  - _Requirements: 9.3, 9.4_

- [x] 11.2 バックエンド：Accept-Languageヘッダー処理の実装
  - リクエストヘッダーから言語を取得
  - メッセージソースから適切な言語のメッセージを返す
  - MessageConfig設定クラス（MessageSource、LocaleResolver）
  - GlobalExceptionHandlerに国際化対応追加
  - _Requirements: 9.4_

- [ ]* 11.3 プロパティテスト：優先言語でのエラーメッセージ
  - **Property 38: 優先言語でのエラーメッセージ**
  - **Validates: Requirements 9.4**

- [x] 11.4 フロントエンド：i18next設定とロケールファイルの作成
  - i18n.ts設定（localStorageとブラウザ言語設定からの初期言語取得）
  - ja.json（日本語翻訳）
  - ko.json（韓国語翻訳）
  - プロフィール翻訳追加
  - _Requirements: 9.1, 9.2, 9.3_

- [x] 11.5 フロントエンド：LanguageSelectorコンポーネントの実装
  - 言語選択UI（Select UIコンポーネント）
  - PreferredLanguage保存（localStorage）
  - i18n言語切り替え
  - _Requirements: 9.1_

- [ ]* 11.6 プロパティテスト：優先言語の保存
  - **Property 35: 優先言語の保存**
  - **Validates: Requirements 9.1**

- [ ]* 11.7 プロパティテスト：デフォルト言語の設定
  - **Property 36: デフォルト言語の設定**
  - **Validates: Requirements 9.2**

- [ ]* 11.8 プロパティテスト：優先言語でのUI表示
  - **Property 37: 優先言語でのUI表示**
  - **Validates: Requirements 9.3**

- [ ]* 11.9 プロパティテスト：ユーザー生成コンテンツの元言語表示
  - **Property 39: ユーザー生成コンテンツの元言語表示**
  - **Validates: Requirements 9.5**

- [ ] 12. 管理者機能の実装
- [ ] 12.1 アプリケーション層：管理者ユースケースの実装
  - ユーザー管理（停止、削除）
  - レシピ管理（審査、ステータス変更、削除）
  - 統計情報取得
  - _Requirements: 10.1, 10.2, 10.3, 10.4, 10.5_

- [ ]* 12.2 プロパティテスト：管理者のユーザーアカウント管理権限
  - **Property 40: 管理者のユーザーアカウント管理権限**
  - **Validates: Requirements 10.2**

- [ ]* 12.3 プロパティテスト：管理者のレシピ表示
  - **Property 41: 管理者のレシピ表示**
  - **Validates: Requirements 10.3**

- [ ]* 12.4 プロパティテスト：レシピの非公開ステータス設定
  - **Property 42: レシピの非公開ステータス設定**
  - **Validates: Requirements 10.4**

- [ ]* 12.5 プロパティテスト：管理者によるレシピ削除時の参照保持
  - **Property 43: 管理者によるレシピ削除時の参照保持**
  - **Validates: Requirements 10.5**

- [ ] 12.6 プレゼンテーション層：AdminControllerの実装
  - REST APIエンドポイント（/api/admin/*）
  - 管理者権限チェック
  - _Requirements: 10.1, 10.2, 10.3, 10.4, 10.5_

- [ ] 12.7 フロントエンド：管理者コンポーネントの実装
  - AdminDashboard、UserManagement、RecipeManagement
  - 管理者専用ルーティング
  - _Requirements: 10.1, 10.2, 10.3, 10.4, 10.5_

- [x] 13. エラーハンドリングとロギングの実装
- [x] 13.1 バックエンド：グローバル例外ハンドラーの実装
  - バリデーションエラー（400）
  - 認証エラー（401）
  - 認可エラー（403）
  - リソース未検出エラー（404）
  - サーバーエラー（500）
  - INFOレベル以上のログ記録
  - 多言語対応（Accept-Languageヘッダー処理）
  - _Requirements: 12.5_

- [ ]* 13.2 プロパティテスト：ユーザーフレンドリーなエラーメッセージ
  - **Property 47: ユーザーフレンドリーなエラーメッセージ**
  - **Validates: Requirements 12.5**

- [x] 13.3 バックエンド：CloudWatchロギングの実装
  - INFOレベル以上のログ記録
  - エラー詳細のログ記録
  - logback-spring.xml設定
  - _Requirements: 12.4_

- [ ]* 13.4 プロパティテスト：エラーログの記録
  - **Property 46: エラーログの記録**
  - **Validates: Requirements 12.4**

- [x] 13.5 フロントエンド：エラーバナーコンポーネントの実装
  - ErrorBannerコンポーネント（画面上部にエラーメッセージ表示）
  - useErrorカスタムフック（エラーハンドリングロジック）
  - apiClient.ts強化（Accept-Languageヘッダー、ステータスコード別エラーハンドリング）
  - shadcn/ui Alertコンポーネント追加
  - 多言語対応（日本語・韓国語のエラーメッセージ）
  - 再試行ボタンと閉じるボタン
  - _Requirements: 12.5_

- [x] 14. ダッシュボードとホーム画面の実装
- [x] 14.1 フロントエンド：DashboardPageコンポーネントの実装
  - ホーム画面レイアウト
  - 最近のレシピ表示
  - スケジュール概要表示
  - 買い物リスト概要表示
  - _Requirements: 全体_

- [x] 14.2 フロントエンド：共通コンポーネントの実装
  - Header、Footer
  - LoadingSkeleton
  - ErrorBanner
  - _Requirements: 全体_

- [ ] 15. 最終チェックポイント - すべてのテストが合格することを確認
  - すべてのテストが合格することを確認し、質問があればユーザーに尋ねる
  - _Requirements: 全体_
