# アプリケーションアーキテクチャ

**プロジェクト名**: 自炊支援・食費節約アプリ  
**ドキュメント版数**: 1.0  
**作成日**: 2026-01-16

---

## 目次

1. [概要](#概要)
2. [システムアーキテクチャ](#システムアーキテクチャ)
3. [バックエンドアーキテクチャ](#バックエンドアーキテクチャ)
4. [フロントエンドアーキテクチャ](#フロントエンドアーキテクチャ)
5. [インフラストラクチャ](#インフラストラクチャ)
6. [データフロー](#データフロー)
7. [セキュリティアーキテクチャ](#セキュリティアーキテクチャ)
8. [デプロイメントアーキテクチャ](#デプロイメントアーキテクチャ)

---

## 概要

### アプリケーションの目的

本アプリケーションは、一人暮らしまたは2〜4人家族の世帯を対象とした自炊支援・食費節約Webアプリケーションです。

**主要な価値提供**:
- 食費の節約（デリバリーサービス依存からの脱却）
- 自炊のハードル低下（簡単なレシピ提供）
- 食品ロス削減（計画的な買い物支援）

### 技術スタック概要

| レイヤー         | 技術                                                  |
| ---------------- | ----------------------------------------------------- |
| フロントエンド   | React 18, TypeScript, Redux Toolkit, Tailwind CSS     |
| バックエンド     | Java 21, Spring Boot 3.1, Clean Architecture          |
| インフラ         | AWS Lambda, DynamoDB, S3, Cognito, API Gateway        |
| 外部API          | Google Gemini API（AIアドバイザー）                   |
| ビルドツール     | Vite（フロントエンド）, Gradle 8.11（バックエンド）  |
| テスト           | Vitest, fast-check（フロントエンド）, JUnit, jqwik（バックエンド） |

---

## システムアーキテクチャ

### 全体構成図

```
┌─────────────────────────────────────────────────────────────────────┐
│                          クライアント層                              │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │  React SPA (TypeScript)                                       │  │
│  │  - Redux Toolkit (状態管理)                                   │  │
│  │  - React Router (ルーティング)                                │  │
│  │  - shadcn/ui + Tailwind CSS (UIコンポーネント)                │  │
│  │  - i18next (多言語対応: 日本語・韓国語)                       │  │
│  └──────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────┘
                                  │
                                  │ HTTPS (REST API)
                                  ▼
┌─────────────────────────────────────────────────────────────────────┐
│                          API Gateway層                               │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │  Amazon API Gateway                                           │  │
│  │  - REST API                                                   │  │
│  │  - Cognito Authorizer (JWT認証)                               │  │
│  │  - CORS設定                                                   │  │
│  │  - スロットリング (1000 req/sec)                              │  │
│  └──────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────┘
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────────────┐
│                        アプリケーション層                            │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │  AWS Lambda (Java 21 + Spring Boot 3.1)                       │  │
│  │  - Clean Architecture                                         │  │
│  │  - Presentation Layer (REST Controllers)                      │  │
│  │  - Application Layer (Use Cases)                              │  │
│  │  - Domain Layer (Entities, Value Objects)                     │  │
│  │  - Infrastructure Layer (Repositories, External APIs)         │  │
│  └──────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────┘
                    │                           │
                    │                           │
        ┌───────────┴──────────┐    ┌──────────┴──────────┐
        ▼                      ▼    ▼                     ▼
┌──────────────┐      ┌──────────────┐      ┌──────────────┐
│  Amazon      │      │  Amazon      │      │  Amazon      │
│  DynamoDB    │      │  S3          │      │  Cognito     │
│              │      │              │      │              │
│  - Users     │      │  - Profile   │      │  - User Pool │
│  - Recipes   │      │    Images    │      │  - JWT Token │
│  - Reviews   │      │  - Recipe    │      │  - Auth Flow │
│  - Schedules │      │    Images    │      │              │
│  - Shopping  │      │              │      │              │
│  - Inventory │      │              │      │              │
│  - Chat      │      │              │      │              │
└──────────────┘      └──────────────┘      └──────────────┘
                                  │
                                  │
                                  ▼
                        ┌──────────────────┐
                        │  Google Gemini   │
                        │  API             │
                        │  - AIアドバイザー │
                        │  - レシピ推薦     │
                        └──────────────────┘
```

### アーキテクチャの特徴

#### 1. サーバーレスアーキテクチャ
- **AWS Lambda**: オンデマンドでスケール、運用負荷の軽減
- **コスト効率**: 使用量に応じた課金（初期フェーズで約$5/月）
- **高可用性**: AWSのマネージドサービスによる自動スケーリング

#### 2. Clean Architecture（バックエンド）
- **レイヤー分離**: 関心の分離による保守性向上
- **依存性の逆転**: ドメイン層が外部に依存しない設計
- **テスタビリティ**: 各レイヤーの独立したテストが可能

#### 3. マイクロフロントエンド的アプローチ
- **コンポーネント駆動開発**: 再利用可能なUIコンポーネント
- **状態管理の分離**: Redux Sliceによる機能別状態管理
- **ルーティング**: React Routerによる宣言的なルーティング

---

## バックエンドアーキテクチャ

### Clean Architecture レイヤー構成

```
backend/src/main/java/com/cookingapp/
│
├── presentation/              # プレゼンテーション層
│   ├── controller/            # REST APIコントローラー
│   │   ├── UserController.java
│   │   ├── RecipeController.java
│   │   ├── ReviewController.java
│   │   ├── ScheduleController.java
│   │   ├── ShoppingListController.java
│   │   ├── InventoryController.java
│   │   ├── GeminiController.java
│   │   └── AdminController.java
│   ├── dto/                   # データ転送オブジェクト
│   │   ├── request/           # リクエストDTO
│   │   └── response/          # レスポンスDTO
│   ├── exception/             # グローバル例外ハンドラー
│   │   └── GlobalExceptionHandler.java
│   └── mapper/                # DTO ↔ Entity マッパー
│
├── application/               # アプリケーション層
│   ├── usecase/               # ユースケース実装
│   │   ├── user/              # ユーザー管理ユースケース
│   │   ├── recipe/            # レシピ管理ユースケース
│   │   ├── review/            # レビュー管理ユースケース
│   │   ├── schedule/          # スケジュール管理ユースケース
│   │   ├── shoppinglist/      # 買い物リスト管理ユースケース
│   │   ├── inventory/         # 在庫管理ユースケース
│   │   └── gemini/            # AIチャット管理ユースケース
│   └── validation/            # バリデーションロジック
│
├── domain/                    # ドメイン層
│   ├── entity/                # エンティティ
│   │   ├── User.java
│   │   ├── Recipe.java
│   │   ├── Review.java
│   │   ├── Schedule.java
│   │   ├── ShoppingListItem.java
│   │   ├── InventoryItem.java
│   │   ├── ChatConversation.java
│   │   └── ChatMessage.java
│   ├── valueobject/           # バリューオブジェクト
│   │   ├── Ingredient.java    # 食材（名前、数量、単位）
│   │   ├── Step.java          # 調理手順
│   │   └── Unit.java          # 単位（Enum）
│   ├── repository/            # リポジトリインターフェース
│   │   ├── UserRepository.java
│   │   ├── RecipeRepository.java
│   │   └── ...
│   ├── service/               # ドメインサービス
│   │   └── RecipeSearchService.java
│   ├── exception/             # ドメイン例外
│   │   ├── ResourceNotFoundException.java
│   │   ├── UnauthorizedException.java
│   │   └── ValidationException.java
│   └── constants/             # ドメイン定数
│       └── ValidationConstants.java
│
└── infrastructure/            # インフラストラクチャ層
    ├── repository/            # リポジトリ実装
    │   ├── DynamoDBUserRepository.java
    │   ├── DynamoDBRecipeRepository.java
    │   └── ...
    ├── external/              # 外部API統合
    │   ├── cognito/           # AWS Cognito統合
    │   │   └── CognitoService.java
    │   ├── s3/                # AWS S3統合
    │   │   └── S3Service.java
    │   └── gemini/            # Google Gemini API統合
    │       └── GeminiService.java
    ├── config/                # 設定クラス
    │   ├── AwsConfig.java
    │   ├── DynamoDBConfig.java
    │   ├── S3Config.java
    │   └── SecurityConfig.java
    └── security/              # セキュリティ実装
        ├── JwtAuthenticationFilter.java
        └── CognitoJwtValidator.java
```

### レイヤー間の依存関係

```
┌─────────────────────────────────────────────────────────────┐
│  Presentation Layer (Controller, DTO, Mapper)               │
│  - HTTPリクエスト/レスポンスの処理                          │
│  - DTOとEntityの変換                                        │
└────────────────────┬────────────────────────────────────────┘
                     │ 依存
                     ▼
┌─────────────────────────────────────────────────────────────┐
│  Application Layer (Use Case, Validation)                   │
│  - ビジネスロジックの調整                                   │
│  - トランザクション管理                                     │
└────────────────────┬────────────────────────────────────────┘
                     │ 依存
                     ▼
┌─────────────────────────────────────────────────────────────┐
│  Domain Layer (Entity, Value Object, Repository Interface)  │
│  - ビジネスルールの実装                                     │
│  - ドメインモデルの定義                                     │
│  - 外部依存なし（Pure Java）                                │
└────────────────────▲────────────────────────────────────────┘
                     │ 実装
                     │
┌────────────────────┴────────────────────────────────────────┐
│  Infrastructure Layer (Repository Impl, External API)       │
│  - データベースアクセス（DynamoDB）                         │
│  - 外部API呼び出し（Cognito, S3, Gemini）                   │
└─────────────────────────────────────────────────────────────┘
```

### 主要コンポーネント

#### 1. Presentation Layer（プレゼンテーション層）

**責務**:
- HTTPリクエストの受信とレスポンスの返却
- DTOとEntityの変換
- バリデーションエラーのハンドリング

**主要クラス**:
- `UserController`: ユーザー登録、ログイン、プロフィール管理
- `RecipeController`: レシピCRUD、検索、画像アップロード
- `ReviewController`: レビューCRUD、通報機能
- `GlobalExceptionHandler`: 統一的なエラーハンドリング

#### 2. Application Layer（アプリケーション層）

**責務**:
- ユースケースの実装
- トランザクション管理
- 複数のドメインサービスの調整

**主要ユースケース**:
- `RegisterUserUseCase`: ユーザー登録（Cognito + DynamoDB）
- `CreateRecipeUseCase`: レシピ作成（Recipes + RecipeIngredients）
- `AddToShoppingListUseCase`: 買い物リスト追加（数量合算ロジック）
- `CheckAlertUseCase`: サボり防止アラート判定

#### 3. Domain Layer（ドメイン層）

**責務**:
- ビジネスルールの実装
- ドメインモデルの定義
- 外部依存なし（Pure Java）

**主要エンティティ**:
- `Recipe`: レシピ情報（食材、手順、画像URL）
- `Ingredient`: 食材バリューオブジェクト（名前、数量、単位）
- `Review`: レビュー（星評価、コメント、通報カウント）

**ドメインサービス**:
- `RecipeSearchService`: 食材による逆引き検索

#### 4. Infrastructure Layer（インフラストラクチャ層）

**責務**:
- データベースアクセス（DynamoDB）
- 外部API呼び出し（Cognito, S3, Gemini）
- 設定管理

**主要サービス**:
- `DynamoDBRecipeRepository`: レシピのCRUD操作
- `CognitoService`: ユーザー認証・登録
- `S3Service`: 画像アップロード・取得
- `GeminiService`: AIアドバイザー機能

---

## フロントエンドアーキテクチャ

### ディレクトリ構成

```
frontend/src/
│
├── components/                # Reactコンポーネント
│   ├── ui/                    # shadcn/ui基本コンポーネント
│   │   ├── button.tsx
│   │   ├── card.tsx
│   │   ├── input.tsx
│   │   ├── dialog.tsx
│   │   └── ...
│   ├── common/                # 共通コンポーネント
│   │   ├── Header.tsx         # ヘッダー（ナビゲーション）
│   │   ├── Footer.tsx         # フッター
│   │   ├── FormField.tsx      # 再利用可能なフォームフィールド
│   │   ├── ImageUploader.tsx  # 画像アップローダー
│   │   ├── ProtectedRoute.tsx # 認証ガード
│   │   └── ErrorBanner.tsx    # エラー表示バナー
│   ├── auth/                  # 認証関連
│   │   ├── Login/
│   │   │   ├── LoginPage.tsx
│   │   │   └── loginFormConfig.ts
│   │   ├── Register/
│   │   │   ├── RegisterPage.tsx
│   │   │   └── registerFormConfig.ts
│   │   └── ConfirmEmail/
│   │       ├── ConfirmEmailPage.tsx
│   │       └── confirmEmailFormConfig.ts
│   ├── recipe/                # レシピ関連
│   │   ├── RecipeSearchPage.tsx
│   │   ├── RecipeDetailPage.tsx
│   │   ├── RecipeEdit/
│   │   │   ├── RecipeEditPage.tsx
│   │   │   ├── useRecipeEditHandlers.ts
│   │   │   └── recipeEditConfig.ts
│   │   ├── ReviewForm.tsx
│   │   └── ReviewList.tsx
│   ├── schedule/              # スケジュール関連
│   │   ├── SchedulePage.tsx
│   │   └── RecipeSelectModal.tsx
│   ├── shopping/              # 買い物リスト関連
│   │   └── ShoppingListPage.tsx
│   ├── inventory/             # 在庫管理関連
│   │   └── InventoryPage.tsx
│   ├── chat/                  # AIチャット関連
│   │   └── GeminiChatPage.tsx
│   ├── dashboard/             # ダッシュボード
│   │   └── DashboardPage.tsx
│   ├── profile/               # プロフィール関連
│   │   ├── ProfileEditPage.tsx
│   │   ├── LanguageSelector.tsx
│   │   └── ImageUploader.tsx
│   ├── alert/                 # アラート関連
│   │   └── AlertModal.tsx
│   └── admin/                 # 管理者機能
│       ├── AdminDashboardPage.tsx
│       ├── UserManagementPage.tsx
│       └── RecipeManagementPage.tsx
│
├── store/                     # Redux状態管理
│   ├── store.ts               # Reduxストア設定
│   ├── recipeSlice.ts         # レシピ状態管理
│   └── slices/
│       ├── authSlice.ts       # 認証状態管理
│       ├── reviewSlice.ts     # レビュー状態管理
│       ├── scheduleSlice.ts   # スケジュール状態管理
│       ├── shoppingListSlice.ts # 買い物リスト状態管理
│       └── adminSlice.ts      # 管理者機能状態管理
│
├── api/                       # API呼び出し
│   ├── userApi.ts             # ユーザーAPI
│   ├── recipeApi.ts           # レシピAPI
│   ├── reviewApi.ts           # レビューAPI
│   ├── scheduleApi.ts         # スケジュールAPI
│   ├── shoppingListApi.ts     # 買い物リストAPI
│   ├── inventoryApi.ts        # 在庫API
│   ├── geminiApi.ts           # GeminiAPI
│   ├── alertApi.ts            # アラートAPI
│   ├── profileApi.ts          # プロフィールAPI
│   └── adminApi.ts            # 管理者API
│
├── hooks/                     # カスタムフック
│   ├── useAuth.ts             # 認証フック
│   ├── useForm.ts             # フォームフック
│   ├── useReview.ts           # レビューフック
│   ├── useError.ts            # エラーハンドリングフック
│   └── useScrollToMessage.ts  # スクロールフック
│
├── utils/                     # ユーティリティ関数
│   ├── apiClient.ts           # API呼び出しヘルパー
│   ├── validation.ts          # バリデーション関数
│   ├── imageCompression.ts    # 画像圧縮
│   ├── unitHelper.ts          # 単位変換ヘルパー
│   └── videoHelper.ts         # 動画URL検証
│
├── types/                     # TypeScript型定義
│   ├── user.ts
│   ├── recipe.ts
│   ├── review.ts
│   ├── schedule.ts
│   ├── shoppingList.ts
│   ├── profile.ts
│   └── admin.ts
│
├── i18n/                      # 多言語対応
│   ├── i18n.ts                # i18next設定
│   └── locales/
│       ├── ja.json            # 日本語翻訳
│       └── ko.json            # 韓国語翻訳
│
├── constants/                 # 定数定義
│   └── validation.ts          # バリデーション定数
│
├── App.tsx                    # ルートコンポーネント
└── index.tsx                  # エントリーポイント
```

### 状態管理アーキテクチャ（Redux Toolkit）

```
┌─────────────────────────────────────────────────────────────┐
│  React Components                                           │
│  - useSelector() でstateを取得                              │
│  - useDispatch() でactionをdispatch                         │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│  Redux Store (store.ts)                                     │
│  - 全アプリケーション状態の単一ソース                       │
└────────────────────┬────────────────────────────────────────┘
                     │
        ┌────────────┼────────────┬────────────┐
        ▼            ▼            ▼            ▼
┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐
│ authSlice│  │recipeSlice│ │reviewSlice│ │scheduleSlice│
│          │  │          │  │          │  │          │
│ - user   │  │ - recipes│  │ - reviews│  │ - schedules│
│ - token  │  │ - loading│  │ - loading│  │ - loading│
│ - loading│  │ - error  │  │ - error  │  │ - error  │
└──────────┘  └──────────┘  └──────────┘  └──────────┘
```

### コンポーネント設計パターン

#### 1. Atomic Design的アプローチ

```
Atoms (原子)
└─ ui/ (shadcn/ui基本コンポーネント)
   ├─ Button
   ├─ Input
   ├─ Card
   └─ ...

Molecules (分子)
└─ common/
   ├─ FormField (Label + Input + Error)
   ├─ ImageUploader (Input + Preview + Button)
   └─ ...

Organisms (有機体)
└─ recipe/
   ├─ ReviewForm (FormField + Button + Rating)
   ├─ ReviewList (Card + ReviewForm)
   └─ ...

Templates (テンプレート)
└─ auth/Login/
   ├─ LoginPage (Header + Form + Footer)
   └─ ...

Pages (ページ)
└─ App.tsx (Routes + Pages)
```

#### 2. Container/Presentational パターン

**Container Component（ロジック担当）**:
- Redux stateの取得
- API呼び出し
- イベントハンドラーの定義

**Presentational Component（表示担当）**:
- propsを受け取って表示
- UIロジックのみ
- 再利用可能

例:
```typescript
// Container
const RecipeSearchPage: React.FC = () => {
  const dispatch = useDispatch();
  const recipes = useSelector((state) => state.recipe.recipes);
  
  const handleSearch = (keyword: string) => {
    dispatch(searchRecipes(keyword));
  };
  
  return <RecipeList recipes={recipes} onSearch={handleSearch} />;
};

// Presentational
const RecipeList: React.FC<Props> = ({ recipes, onSearch }) => {
  return (
    <div>
      <SearchBar onSearch={onSearch} />
      {recipes.map(recipe => <RecipeCard key={recipe.id} recipe={recipe} />)}
    </div>
  );
};
```

### API呼び出しパターン

#### apiClient.ts（共通ヘルパー）

```typescript
// ❌ 禁止: axios直接使用
import axios from 'axios';

// ✅ 推奨: apiClient.tsのヘルパー関数
import { apiGet, apiPost, apiPut, apiDelete } from '@/utils/apiClient';

// 使用例
const recipes = await apiGet<Recipe[]>('/api/recipes');
const newRecipe = await apiPost<Recipe>('/api/recipes', recipeData);
```

**特徴**:
- 統一的なエラーハンドリング
- 認証トークンの自動付与
- レスポンスの型安全性
- リトライロジック

---

## インフラストラクチャ

### AWSサービス構成

```
┌─────────────────────────────────────────────────────────────┐
│  Amazon CloudFront (CDN)                                    │
│  - 静的コンテンツ配信                                       │
│  - キャッシュ最適化                                         │
└────────────────────┬────────────────────────────────────────┘
                     │
        ┌────────────┴────────────┐
        ▼                         ▼
┌──────────────┐          ┌──────────────┐
│  Amazon S3   │          │  AWS Amplify │
│  (Images)    │          │  (Frontend)  │
│              │          │              │
│  - Profile   │          │  - React SPA │
│  - Recipe    │          │  - CI/CD     │
│  - Step      │          │              │
└──────────────┘          └──────────────┘
        ▲
        │
        │ Pre-signed URL
        │
┌───────┴──────────────────────────────────────────────────────┐
│  Amazon API Gateway                                          │
│  - REST API                                                  │
│  - Cognito Authorizer                                        │
│  - Request/Response Validation                               │
│  - Throttling: 1000 req/sec, Burst: 2000                    │
└────────────────────┬─────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│  AWS Lambda                                                 │
│  - Runtime: Java 21                                         │
│  - Memory: 512MB - 1024MB                                   │
│  - Timeout: 30秒                                            │
│  - Cold Start対策: Provisioned Concurrency（本番環境）      │
└────────────────────┬────────────────────────────────────────┘
                     │
        ┌────────────┼────────────┬────────────┐
        ▼            ▼            ▼            ▼
┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐
│ DynamoDB │  │    S3    │  │ Cognito  │  │ Secrets  │
│          │  │          │  │          │  │ Manager  │
│ - Users  │  │ - Images │  │ - Auth   │  │ - Gemini │
│ - Recipes│  │          │  │          │  │   API Key│
│ - Reviews│  │          │  │          │  │          │
│ - etc.   │  │          │  │          │  │          │
└──────────┘  └──────────┘  └──────────┘  └──────────┘
```

### データベース設計（DynamoDB）

#### テーブル一覧

| テーブル名           | Partition Key  | Sort Key       | GSI                    | 用途                 |
| -------------------- | -------------- | -------------- | ---------------------- | -------------------- |
| Users                | UserId         | -              | -                      | ユーザー情報         |
| Recipes              | RecipeId       | -              | GSI_Author             | レシピ情報           |
| RecipeIngredients    | IngredientName | RecipeId       | -                      | 食材逆引きインデックス |
| Reviews              | RecipeId       | ReviewId       | GSI_User               | レビュー情報         |
| Schedules            | UserId         | DateRecipeId   | -                      | スケジュール情報     |
| ShoppingLists        | UserId         | ItemId         | GSI_NormalizedKey      | 買い物リスト         |
| Inventory            | UserId         | ItemId         | GSI_ExpiryDate         | 食材在庫             |
| ChatConversations    | UserId         | ConversationId | -                      | AIチャット会話       |
| ChatMessages         | ConversationId | MessageId      | -                      | AIチャットメッセージ |

詳細は [DATABASE_DESIGN.md](./DATABASE_DESIGN.md) を参照。

#### アクセスパターン最適化

**効率的なクエリ**:
- ✅ GetItem: 単一アイテムの取得
- ✅ Query: Partition Key + Sort Keyでの範囲検索
- ✅ GSI: セカンダリインデックスでの検索

**避けるべきパターン**:
- ❌ Scan: 全テーブルスキャン（非効率）
- ❌ フィルタリング: 大量データの後処理

### ストレージ設計（S3）

#### バケット構成

```
cooking-app-images-{環境}/
├── profiles/
│   └── {userId}/
│       └── profile.jpg
├── recipes/
│   └── {recipeId}/
│       ├── main.jpg
│       └── steps/
│           ├── step-0.jpg
│           ├── step-1.jpg
│           └── ...
└── temp/
    └── {uploadId}/
        └── temp-image.jpg
```

#### セキュリティ設定

- **パブリックアクセス**: ブロック
- **アクセス方法**: Pre-signed URL（有効期限: 15分）
- **暗号化**: AES-256（サーバーサイド暗号化）
- **バージョニング**: 有効
- **ライフサイクルポリシー**: 古いバージョンを90日後に削除

### 認証・認可（Cognito）

#### ユーザープール設定

```
┌─────────────────────────────────────────────────────────────┐
│  Amazon Cognito User Pool                                   │
│                                                             │
│  認証フロー: USER_PASSWORD_AUTH                             │
│  パスワードポリシー:                                        │
│    - 最小長: 8文字                                          │
│    - 大文字、小文字、数字を必須                             │
│                                                             │
│  トークン有効期限:                                          │
│    - アクセストークン: 1時間                                │
│    - リフレッシュトークン: 90日                             │
│                                                             │
│  属性:                                                      │
│    - email (必須、検証必要)                                 │
│    - nickname                                               │
│    - preferred_language                                     │
└─────────────────────────────────────────────────────────────┘
```

#### JWT検証フロー

```
1. クライアント → API Gateway
   Authorization: Bearer {JWT}

2. API Gateway → Cognito Authorizer
   JWT検証（署名、有効期限、発行者）

3. Cognito Authorizer → Lambda
   検証成功 → ユーザー情報をコンテキストに追加

4. Lambda → ビジネスロジック
   コンテキストからユーザーIDを取得
```

---

## データフロー

### 1. ユーザー登録フロー

```
┌────────┐     ┌──────────┐     ┌─────────┐     ┌──────────┐
│ Client │     │ Backend  │     │ Cognito │     │ DynamoDB │
└───┬────┘     └────┬─────┘     └────┬────┘     └────┬─────┘
    │               │                │               │
    │ 1. POST /api/users/register    │               │
    ├──────────────►│                │               │
    │               │ 2. SignUp      │               │
    │               ├───────────────►│               │
    │               │◄───────────────┤               │
    │               │ 3. UserId      │               │
    │               │                │               │
    │               │ 4. Put User    │               │
    │               ├───────────────────────────────►│
    │               │◄───────────────────────────────┤
    │◄──────────────┤ 5. Success     │               │
    │               │                │               │
    │ 6. POST /api/users/confirm     │               │
    ├──────────────►│                │               │
    │               │ 7. ConfirmSignUp               │
    │               ├───────────────►│               │
    │               │◄───────────────┤               │
    │◄──────────────┤ 8. Confirmed   │               │
```

### 2. レシピ作成フロー（画像付き）

```
┌────────┐     ┌──────────┐     ┌─────┐     ┌──────────┐
│ Client │     │ Backend  │     │ S3  │     │ DynamoDB │
└───┬────┘     └────┬─────┘     └──┬──┘     └────┬─────┘
    │               │               │             │
    │ 1. POST /api/recipes/with-images            │
    ├──────────────►│               │             │
    │               │ 2. Generate Pre-signed URL  │
    │               ├──────────────►│             │
    │               │◄──────────────┤             │
    │◄──────────────┤ 3. Pre-signed URL           │
    │               │               │             │
    │ 4. PUT (Upload Image)         │             │
    ├──────────────────────────────►│             │
    │◄──────────────────────────────┤             │
    │               │               │             │
    │ 5. POST /api/recipes (with imageUrl)        │
    ├──────────────►│               │             │
    │               │ 6. TransactWriteItems       │
    │               │  - Put Recipe               │
    │               │  - Put RecipeIngredients    │
    │               ├────────────────────────────►│
    │               │◄────────────────────────────┤
    │◄──────────────┤ 7. Success    │             │
```

### 3. 買い物リスト追加フロー（数量合算）

```
┌────────┐     ┌──────────┐     ┌──────────┐
│ Client │     │ Backend  │     │ DynamoDB │
└───┬────┘     └────┬─────┘     └────┬─────┘
    │               │                │
    │ 1. POST /api/shopping-lists    │
    │    { name: "玉ねぎ", quantity: 2, unit: "個" }
    ├──────────────►│                │
    │               │ 2. Query GSI_NormalizedKey
    │               │    (UserId + "玉ねぎ#個")
    │               ├───────────────►│
    │               │◄───────────────┤
    │               │ 3. Existing Item Found
    │               │    (quantity: 3)
    │               │                │
    │               │ 4. Update Item │
    │               │    (quantity: 3 + 2 = 5)
    │               ├───────────────►│
    │               │◄───────────────┤
    │◄──────────────┤ 5. Success     │
    │               │    (quantity: 5)
```

### 4. サボり防止アラート判定フロー

```
┌────────┐     ┌──────────┐     ┌──────────┐
│ Client │     │ Backend  │     │ DynamoDB │
└───┬────┘     └────┬─────┘     └────┬─────┘
    │               │                │
    │ 1. GET /api/alerts/check       │
    ├──────────────►│                │
    │               │ 2. Get User    │
    │               ├───────────────►│
    │               │◄───────────────┤
    │               │ 3. User (LastCookingDate)
    │               │                │
    │               │ 4. Calculate Days Since Last Cooking
    │               │    Today - LastCookingDate >= 3 days?
    │               │                │
    │◄──────────────┤ 5. { shouldShow: true/false }
```

### 5. AIチャット（レシピ推薦）フロー

```
┌────────┐  ┌──────────┐  ┌──────────┐  ┌────────┐  ┌──────────┐
│ Client │  │ Backend  │  │ DynamoDB │  │ Gemini │  │ DynamoDB │
└───┬────┘  └────┬─────┘  └────┬─────┘  └───┬────┘  └────┬─────┘
    │            │             │            │            │
    │ 1. POST /api/chat/conversations/{id}/messages      │
    │    { content: "玉ねぎとじゃがいもでレシピ教えて" }  │
    ├───────────►│             │            │            │
    │            │ 2. Get Inventory Items   │            │
    │            ├────────────►│            │            │
    │            │◄────────────┤            │            │
    │            │ 3. Inventory: [玉ねぎ, じゃがいも]    │
    │            │             │            │            │
    │            │ 4. Call Gemini API       │            │
    │            │    (with inventory context)           │
    │            ├─────────────────────────►│            │
    │            │◄─────────────────────────┤            │
    │            │ 5. Recipe Recommendation │            │
    │            │             │            │            │
    │            │ 6. Save Message (user)   │            │
    │            ├─────────────────────────────────────►│
    │            │ 7. Save Message (assistant + recipe)  │
    │            ├─────────────────────────────────────►│
    │            │◄─────────────────────────────────────┤
    │◄───────────┤ 8. Response with Recipe  │            │
```

---

## セキュリティアーキテクチャ

### 認証・認可の階層

```
┌─────────────────────────────────────────────────────────────┐
│  レイヤー1: API Gateway Authorizer                          │
│  - Cognito JWT検証                                          │
│  - トークンの署名、有効期限、発行者を検証                   │
│  - 検証失敗 → 401 Unauthorized                              │
└────────────────────┬────────────────────────────────────────┘
                     │ 検証成功
                     ▼
┌─────────────────────────────────────────────────────────────┐
│  レイヤー2: Lambda Security Filter                          │
│  - JwtAuthenticationFilter                                  │
│  - ユーザーIDの抽出                                         │
│  - SecurityContextへの設定                                  │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│  レイヤー3: Application Layer Authorization                 │
│  - リソース所有者チェック                                   │
│  - 管理者権限チェック                                       │
│  - 例: 自分のレシピのみ編集可能                             │
└─────────────────────────────────────────────────────────────┘
```

### データ保護

#### 1. 転送時の暗号化

- **TLS 1.2以上**: すべてのHTTP通信
- **HTTPS強制**: API Gateway、CloudFront
- **証明書管理**: AWS Certificate Manager

#### 2. 保存時の暗号化

| サービス  | 暗号化方式                    | キー管理      |
| --------- | ----------------------------- | ------------- |
| DynamoDB  | AES-256（サーバーサイド暗号化）| AWS KMS       |
| S3        | AES-256（SSE-S3）             | AWS管理       |
| Secrets   | AES-256                       | AWS KMS       |

#### 3. 機密情報管理

```
┌─────────────────────────────────────────────────────────────┐
│  AWS Secrets Manager                                        │
│  - Gemini API Key                                           │
│  - データベース接続情報（将来的に）                         │
│  - 自動ローテーション対応                                   │
└─────────────────────────────────────────────────────────────┘
```

### アクセス制御

#### IAMロール設計

```
Lambda Execution Role
├── DynamoDB: GetItem, PutItem, Query, UpdateItem, DeleteItem
├── S3: GetObject, PutObject, DeleteObject
├── Cognito: AdminGetUser, AdminDeleteUser
├── Secrets Manager: GetSecretValue
└── CloudWatch Logs: CreateLogGroup, CreateLogStream, PutLogEvents
```

#### リソースベースのアクセス制御

| リソース | アクセス制御ルール                                |
| -------- | ------------------------------------------------- |
| レシピ   | 作成者のみ編集・削除可能                          |
| レビュー | 投稿者のみ編集・削除可能                          |
| プロフィール | 本人のみ編集可能                              |
| 買い物リスト | 本人のみアクセス可能                          |
| 在庫     | 本人のみアクセス可能                              |
| チャット | 本人のみアクセス可能                              |

### 入力検証

#### 1. クライアントサイド検証

- **React Hook Form**: フォームバリデーション
- **Zod/Yup**: スキーマバリデーション
- **即時フィードバック**: ユーザー体験向上

#### 2. サーバーサイド検証

- **Spring Validation**: `@Valid`, `@NotNull`, `@Size`
- **カスタムバリデーター**: ビジネスルール検証
- **エラーメッセージ**: 多言語対応

#### 3. API Gateway検証

- **リクエストバリデーション**: OpenAPI仕様に基づく
- **レート制限**: 1000 req/sec
- **ペイロードサイズ制限**: 5MB

### セキュリティベストプラクティス

#### 実装済み

- ✅ JWT認証（Cognito）
- ✅ HTTPS強制
- ✅ CORS設定
- ✅ 入力検証（クライアント・サーバー）
- ✅ SQLインジェクション対策（DynamoDB使用）
- ✅ XSS対策（React自動エスケープ）
- ✅ CSRF対策（JWT使用）
- ✅ パスワードポリシー（8文字以上、複雑性要件）
- ✅ Pre-signed URL（S3アクセス制御）
- ✅ 論理削除（データ保持）

#### 今後の検討事項

- ⚠️ レート制限（ユーザー単位）
- ⚠️ 監査ログ（DynamoDB Streams）
- ⚠️ WAF（Web Application Firewall）
- ⚠️ セキュリティヘッダー（CSP, X-Frame-Options）

---

## デプロイメントアーキテクチャ

### 環境構成

```
┌─────────────────────────────────────────────────────────────┐
│  Local Development (LocalStack)                             │
│  - DynamoDB Local                                           │
│  - S3 Local                                                 │
│  - Cognito Local (未対応、モック使用)                       │
│  - ポート: 4566                                             │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│  Development Environment (AWS)                              │
│  - Lambda: cooking-app-dev                                  │
│  - DynamoDB: Users-dev, Recipes-dev, ...                    │
│  - S3: cooking-app-images-dev                               │
│  - Cognito: cooking-app-users-dev                           │
│  - API Gateway: https://api-dev.example.com                 │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│  Production Environment (AWS)                               │
│  - Lambda: cooking-app-prod                                 │
│  - DynamoDB: Users, Recipes, ...                            │
│  - S3: cooking-app-images                                   │
│  - Cognito: cooking-app-users                               │
│  - API Gateway: https://api.example.com                     │
│  - CloudFront: https://www.example.com                      │
└─────────────────────────────────────────────────────────────┘
```

### CI/CDパイプライン

#### バックエンド（Lambda）

```
┌─────────────────────────────────────────────────────────────┐
│  1. コード変更（Git Push）                                  │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│  2. GitHub Actions / AWS CodePipeline                       │
│     - Checkout Code                                         │
│     - Run Tests (JUnit + jqwik)                             │
│     - Build (Gradle shadowJar)                              │
│     - Security Scan (SonarQube)                             │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│  3. Deploy to Lambda                                        │
│     - Upload JAR to S3                                      │
│     - Update Lambda Function                                │
│     - Run Smoke Tests                                       │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│  4. Post-Deployment                                         │
│     - CloudWatch Alarms                                     │
│     - Rollback on Error                                     │
└─────────────────────────────────────────────────────────────┘
```

#### フロントエンド（Amplify）

```
┌─────────────────────────────────────────────────────────────┐
│  1. コード変更（Git Push）                                  │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│  2. AWS Amplify Build                                       │
│     - npm install                                           │
│     - npm run lint                                          │
│     - npm run test                                          │
│     - npm run build                                         │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│  3. Deploy to Amplify Hosting                               │
│     - Upload to S3                                          │
│     - Invalidate CloudFront Cache                           │
│     - Update DNS (Route 53)                                 │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│  4. Post-Deployment                                         │
│     - Smoke Tests                                           │
│     - Performance Monitoring                                │
└─────────────────────────────────────────────────────────────┘
```

### インフラストラクチャ as Code

#### AWS CDK構成

```
infrastructure/cdk/
├── bin/
│   └── cdk.ts                 # CDKアプリエントリーポイント
├── lib/
│   ├── dynamodb-stack.ts      # DynamoDBテーブル定義
│   ├── s3-stack.ts            # S3バケット定義
│   ├── cognito-stack.ts       # Cognitoユーザープール定義
│   ├── lambda-stack.ts        # Lambda関数定義
│   └── api-gateway-stack.ts   # API Gateway定義
├── cdk.json                   # CDK設定
└── package.json
```

#### デプロイコマンド

```bash
# 開発環境
cd infrastructure/cdk
cdk deploy --all --context env=dev

# 本番環境
cdk deploy --all --context env=prod
```

### モニタリング・ロギング

#### CloudWatch設定

```
┌─────────────────────────────────────────────────────────────┐
│  CloudWatch Logs                                            │
│  - Lambda実行ログ                                           │
│  - API Gatewayアクセスログ                                  │
│  - エラーログ                                               │
│  - 保持期間: 30日（開発）、90日（本番）                     │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│  CloudWatch Metrics                                         │
│  - Lambda: Invocations, Duration, Errors, Throttles         │
│  - DynamoDB: ConsumedReadCapacity, ConsumedWriteCapacity    │
│  - API Gateway: Count, Latency, 4XXError, 5XXError          │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│  CloudWatch Alarms                                          │
│  - Lambda Error Rate > 5%                                   │
│  - API Gateway 5XX Error > 10 requests/min                  │
│  - DynamoDB Throttled Requests > 0                          │
│  - Lambda Duration > 25秒（タイムアウト間近）               │
└─────────────────────────────────────────────────────────────┘
```

### スケーリング戦略

#### Lambda

- **同時実行数**: 予約済み同時実行数（本番: 100）
- **Provisioned Concurrency**: コールドスタート対策（本番: 5）
- **メモリ**: 512MB - 1024MB（負荷に応じて調整）

#### DynamoDB

- **キャパシティモード**: オンデマンド（開発）、プロビジョニング（本番）
- **Auto Scaling**: 読み取り/書き込みキャパシティの自動調整
- **ターゲット使用率**: 70%

#### API Gateway

- **スロットリング**: 1000 req/sec（レート制限）
- **バースト**: 2000 req（一時的なスパイク対応）

### コスト最適化

#### 月額コスト見積もり（本番環境）

| サービス      | 使用量（想定）          | 月額コスト |
| ------------- | ----------------------- | ---------- |
| Lambda        | 100万リクエスト         | 無料枠内   |
| DynamoDB      | 25GB、読み書き各25ユニット | 無料枠内 |
| S3            | 5GB、1000リクエスト     | $0.50      |
| Cognito       | 50,000 MAU              | 無料枠内   |
| API Gateway   | 100万リクエスト         | $3.50      |
| Amplify       | ビルド時間、ストレージ  | $1.00      |
| **合計**      |                         | **約$5/月**|

#### コスト削減策

- ✅ サーバーレスアーキテクチャ（使用量課金）
- ✅ DynamoDBオンデマンドモード（開発環境）
- ✅ S3ライフサイクルポリシー（古いバージョン削除）
- ✅ CloudWatch Logsの保持期間設定
- ✅ Lambda Provisioned Concurrencyの最小化

---

## パフォーマンス最適化

### フロントエンド最適化

#### 1. コード分割（Code Splitting）

```typescript
// React.lazy + Suspense
const RecipeDetailPage = React.lazy(() => import('./components/recipe/RecipeDetailPage'));
const AdminDashboardPage = React.lazy(() => import('./components/admin/AdminDashboardPage'));

// ルーティングでの使用
<Suspense fallback={<LoadingSkeleton />}>
  <Route path="/recipes/:id" element={<RecipeDetailPage />} />
</Suspense>
```

#### 2. 画像最適化

- **圧縮**: browser-image-compression（アップロード前）
- **フォーマット**: JPEG（写真）、PNG（透過）、WebP（将来対応）
- **サイズ制限**: 最大5MB
- **レスポンシブ画像**: srcset対応（将来対応）

#### 3. キャッシング戦略

```
┌─────────────────────────────────────────────────────────────┐
│  Browser Cache                                              │
│  - 静的アセット: 1年（immutable）                           │
│  - HTML: no-cache（常に最新）                               │
│  - API Response: no-cache                                   │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│  CloudFront Cache                                           │
│  - 静的アセット: 24時間                                     │
│  - 画像: 7日間                                              │
│  - API: キャッシュなし                                      │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│  Redux State Cache                                          │
│  - レシピ一覧: メモリキャッシュ                             │
│  - ユーザー情報: セッションストレージ                       │
│  - 認証トークン: セキュアストレージ                         │
└─────────────────────────────────────────────────────────────┘
```

### バックエンド最適化

#### 1. DynamoDBクエリ最適化

```java
// ❌ 非効率: Scan
List<Recipe> recipes = dynamoDB.scan(scanRequest);

// ✅ 効率的: Query with GSI
List<Recipe> recipes = dynamoDB.query(
    QueryRequest.builder()
        .tableName("Recipes")
        .indexName("GSI_Author")
        .keyConditionExpression("AuthorId = :authorId")
        .build()
);
```

#### 2. バッチ処理

```java
// ❌ 非効率: 個別GetItem
for (String recipeId : recipeIds) {
    Recipe recipe = recipeRepository.findById(recipeId);
}

// ✅ 効率的: BatchGetItem
List<Recipe> recipes = recipeRepository.findByIds(recipeIds);
```

#### 3. 接続プーリング

```java
// DynamoDB Client設定
DynamoDbClient.builder()
    .httpClientBuilder(ApacheHttpClient.builder()
        .maxConnections(50)
        .connectionTimeout(Duration.ofSeconds(5))
        .socketTimeout(Duration.ofSeconds(30))
    )
    .build();
```

### Lambda最適化

#### 1. コールドスタート対策

```
┌─────────────────────────────────────────────────────────────┐
│  Provisioned Concurrency                                    │
│  - 本番環境: 5インスタンス常時起動                          │
│  - 開発環境: オンデマンドのみ                               │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│  初期化最適化                                               │
│  - AWS SDK Client: 静的フィールドで再利用                   │
│  - Spring Context: Lambda起動時に初期化                     │
│  - 遅延初期化: 必要になるまで初期化しない                   │
└─────────────────────────────────────────────────────────────┘
```

#### 2. メモリ最適化

```
Lambda設定:
- メモリ: 512MB（軽量処理）、1024MB（画像処理）
- タイムアウト: 30秒
- 環境変数: 最小限（Secrets Manager使用）
```

### ネットワーク最適化

#### 1. API呼び出し削減

```typescript
// ❌ 非効率: 個別API呼び出し
const recipe = await apiGet(`/api/recipes/${id}`);
const reviews = await apiGet(`/api/recipes/${id}/reviews`);
const author = await apiGet(`/api/users/${recipe.authorId}`);

// ✅ 効率的: 1回のAPI呼び出しで必要なデータを取得
const recipeDetail = await apiGet(`/api/recipes/${id}?include=reviews,author`);
```

#### 2. ペイロード最適化

- **圧縮**: gzip/brotli（API Gateway自動対応）
- **フィールド選択**: 必要なフィールドのみ返却
- **ページネーション**: 大量データの分割取得

---

## テスト戦略

### テストピラミッド

```
                    ┌─────────────┐
                    │   E2E Tests │  ← 少数（重要フロー）
                    │   (Cypress) │
                    └─────────────┘
                  ┌───────────────────┐
                  │ Integration Tests │  ← 中程度（API統合）
                  │  (JUnit + Mock)   │
                  └───────────────────┘
              ┌─────────────────────────────┐
              │      Unit Tests             │  ← 多数（ロジック）
              │  (JUnit + Vitest)           │
              └─────────────────────────────┘
          ┌───────────────────────────────────────┐
          │   Property-Based Tests                │  ← 境界値・ランダム
          │   (jqwik + fast-check)                │
          └───────────────────────────────────────┘
```

### バックエンドテスト

#### 1. ユニットテスト（JUnit）

```java
@Test
void testCreateRecipe_Success() {
    // Given
    Recipe recipe = Recipe.builder()
        .title("Test Recipe")
        .authorId("user123")
        .build();
    
    // When
    Recipe created = recipeService.createRecipe(recipe);
    
    // Then
    assertNotNull(created.getRecipeId());
    assertEquals("Test Recipe", created.getTitle());
}
```

#### 2. プロパティベーステスト（jqwik）

```java
@Property
void testIngredientQuantity_AlwaysPositive(@ForAll @Positive double quantity) {
    Ingredient ingredient = new Ingredient("Test", quantity, Unit.GRAM);
    assertTrue(ingredient.getQuantity() > 0);
}
```

### フロントエンドテスト

#### 1. ユニットテスト（Vitest）

```typescript
describe('apiClient', () => {
  it('should add authorization header', async () => {
    const mockFetch = vi.fn();
    global.fetch = mockFetch;
    
    await apiGet('/api/recipes');
    
    expect(mockFetch).toHaveBeenCalledWith(
      expect.any(String),
      expect.objectContaining({
        headers: expect.objectContaining({
          'Authorization': expect.stringContaining('Bearer')
        })
      })
    );
  });
});
```

#### 2. プロパティベーステスト（fast-check）

```typescript
import fc from 'fast-check';

describe('validation', () => {
  it('should validate email format', () => {
    fc.assert(
      fc.property(fc.emailAddress(), (email) => {
        return isValidEmail(email) === true;
      })
    );
  });
});
```

---

## 多言語対応アーキテクチャ

### 対応言語

- **日本語（ja）**: デフォルト言語
- **韓国語（ko）**: 第二言語

### フロントエンド多言語化（i18next）

#### 翻訳ファイル構成

```
frontend/src/i18n/
├── i18n.ts                    # i18next設定
└── locales/
    ├── ja.json                # 日本語翻訳
    └── ko.json                # 韓国語翻訳
```

#### 使用例

```typescript
import { useTranslation } from 'react-i18next';

const LoginPage: React.FC = () => {
  const { t } = useTranslation();
  
  return (
    <div>
      <h1>{t('auth.login.title')}</h1>
      <Button>{t('auth.login.submit')}</Button>
    </div>
  );
};
```

### バックエンド多言語化（Spring MessageSource）

#### メッセージファイル構成

```
backend/src/main/resources/messages/
├── messages.properties        # デフォルト（日本語）
└── messages_ko.properties     # 韓国語
```

#### 使用例

```java
@Service
public class UserService {
    private final Logger log = LoggerFactory.getLogger(UserService.class);
    private final MessageSource messageSource;
    
    public void registerUser(User user) {
        String message = messageSource.getMessage(
            "user.registered.success",
            new Object[]{user.getNickname()},
            LocaleContextHolder.getLocale()
        );
        log.info(message);
    }
}
```

### 言語切り替えフロー

```
┌────────┐     ┌──────────┐     ┌──────────┐
│ Client │     │ Backend  │     │ DynamoDB │
└───┬────┘     └────┬─────┘     └────┬─────┘
    │               │                │
    │ 1. PUT /api/users/profile      │
    │    { preferredLanguage: "ko" } │
    ├──────────────►│                │
    │               │ 2. Update User │
    │               ├───────────────►│
    │               │◄───────────────┤
    │◄──────────────┤ 3. Success     │
    │               │                │
    │ 4. i18n.changeLanguage("ko")   │
    │               │                │
```

---

## 外部API統合

### Google Gemini API

#### 統合アーキテクチャ

```
┌────────────────────────────────────────────────────────────┐
│  Lambda (GeminiService)                                    │
│  - API Key取得（Secrets Manager）                          │
│  - リクエスト構築                                          │
│  - レート制限対策（Exponential Backoff）                   │
│  - エラーハンドリング                                      │
└────────────────────┬───────────────────────────────────────┘
                     │
                     │ HTTPS
                     ▼
┌────────────────────────────────────────────────────────────┐
│  Google Gemini API                                         │
│  - Model: gemini-1.5-flash                                 │
│  - Temperature: 0.7                                        │
│  - Max Tokens: 2048                                        │
└────────────────────────────────────────────────────────────┘
```

#### エラーハンドリング

```java
public class GeminiService {
    private final Logger log = LoggerFactory.getLogger(GeminiService.class);
    
    public String generateRecipeAdvice(String ingredients) {
        try {
            return callGeminiApi(ingredients);
        } catch (RateLimitException e) {
            log.warn("Gemini API rate limit exceeded, retrying...");
            return retryWithBackoff(() -> callGeminiApi(ingredients));
        } catch (ApiException e) {
            log.error("Gemini API error", e);
            return getCachedResponse(ingredients)
                .orElse("現在サービスが混み合っています。しばらくしてから再度お試しください。");
        }
    }
}
```

---

## 障害対策・復旧戦略

### 高可用性設計

#### 1. マルチAZ構成

```
┌─────────────────────────────────────────────────────────────┐
│  AWS Region: ap-northeast-1 (東京)                          │
│                                                             │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │  AZ-1a       │  │  AZ-1c       │  │  AZ-1d       │     │
│  │              │  │              │  │              │     │
│  │  Lambda      │  │  Lambda      │  │  Lambda      │     │
│  │  DynamoDB    │  │  DynamoDB    │  │  DynamoDB    │     │
│  │  (Replica)   │  │  (Replica)   │  │  (Replica)   │     │
│  └──────────────┘  └──────────────┘  └──────────────┘     │
└─────────────────────────────────────────────────────────────┘
```

#### 2. 自動フェイルオーバー

- **Lambda**: 自動的に別AZで再実行
- **DynamoDB**: 自動レプリケーション、自動フェイルオーバー
- **S3**: 99.999999999%の耐久性

### バックアップ戦略

#### DynamoDB

```
┌─────────────────────────────────────────────────────────────┐
│  Point-in-Time Recovery (PITR)                              │
│  - 有効化: 本番環境                                         │
│  - 保持期間: 35日                                           │
│  - リカバリ: 任意の時点に復元可能                           │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│  On-Demand Backup                                           │
│  - 頻度: 毎日（自動化）                                     │
│  - 保持期間: 30日                                           │
│  - 用途: 長期保存、コンプライアンス                         │
└─────────────────────────────────────────────────────────────┘
```

#### S3

```
┌─────────────────────────────────────────────────────────────┐
│  Versioning                                                 │
│  - 有効化: すべてのバケット                                 │
│  - 保持期間: 90日（ライフサイクルポリシー）                 │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│  Cross-Region Replication (将来対応)                        │
│  - レプリケーション先: us-west-2                            │
│  - 用途: 災害復旧                                           │
└─────────────────────────────────────────────────────────────┘
```

### 障害検知・通知

```
┌─────────────────────────────────────────────────────────────┐
│  CloudWatch Alarms                                          │
│  - Lambda Error Rate > 5%                                   │
│  - API Gateway 5XX Error > 10 requests/min                  │
│  - DynamoDB Throttled Requests > 0                          │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│  Amazon SNS                                                 │
│  - トピック: cooking-app-alerts                             │
│  - サブスクリプション: メール、Slack（将来対応）            │
└─────────────────────────────────────────────────────────────┘
```

---

## 今後の拡張計画

### Phase 2（短期）

- ✅ レシピ登録審査機能
- ✅ 画像トリミング機能
- ✅ 栄養計算機能
- ✅ プッシュ通知（LINE Messaging API）

### Phase 3（中期）

- ✅ EC連携（食材購入）
- ✅ ソーシャルログイン（Google、LINE）
- ✅ レシピ動画対応
- ✅ コミュニティ機能（フォロー、いいね）

### Phase 4（長期）

- ✅ マルチリージョン展開
- ✅ モバイルアプリ（React Native）
- ✅ 音声アシスタント統合（Alexa、Google Assistant）
- ✅ AI献立自動生成

---

## 関連ドキュメント

- **[要件定義書](./要件定義書_自炊支援アプリ.md)** - 機能要件・非機能要件
- **[データベース設計](./DATABASE_DESIGN.md)** - テーブル設計・アクセスパターン
- **[API仕様書](./API_SPECIFICATION.md)** - REST APIエンドポイント定義
- **[OpenAPI仕様](./openapi.yaml)** - 詳細なAPI定義
- **[コーディング規約](../CODING_STANDARDS.md)** - プロジェクトのコーディング規約
- **[セットアップガイド](../SETUP_GUIDE.md)** - 開発環境構築手順

---

## 変更履歴

| バージョン | 日付       | 変更内容                     | 作成者 |
| ---------- | ---------- | ---------------------------- | ------ |
| 1.0.0      | 2026-01-16 | 初版作成                     | Kiro   |

---

## まとめ

本アプリケーションは、**Clean Architecture**と**サーバーレスアーキテクチャ**を組み合わせた、スケーラブルで保守性の高い設計となっています。

### アーキテクチャの強み

1. **レイヤー分離**: 関心の分離による保守性向上
2. **サーバーレス**: 運用負荷の軽減、コスト効率
3. **マネージドサービス**: 高可用性、自動スケーリング
4. **セキュリティ**: 多層防御、暗号化、認証・認可
5. **多言語対応**: 日本語・韓国語のシームレスな切り替え
6. **テスタビリティ**: 各レイヤーの独立したテスト

### 技術選定の理由

- **Java + Spring Boot**: エンタープライズグレードの信頼性
- **React + TypeScript**: 型安全性、開発者体験
- **AWS**: 豊富なマネージドサービス、グローバル展開
- **DynamoDB**: NoSQL、高速、スケーラブル
- **Clean Architecture**: 長期的な保守性、テスタビリティ

このアーキテクチャにより、初期フェーズでは約$5/月の低コストで運用しながら、将来的なスケールアップにも柔軟に対応できる設計となっています。
