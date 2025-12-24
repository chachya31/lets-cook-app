# 実装状況

このドキュメントは、バックエンドとフロントエンドの実装状況を追跡します。

最終更新: 2024-12-24

---

## 📊 実装状況サマリー

| 機能カテゴリ | バックエンド | フロントエンド | 状態 |
|------------|------------|--------------|------|
| ユーザー認証 | ✅ | ✅ | 完了 |
| プロフィール管理 | ✅ | ✅ | 完了 |
| レシピ管理 | ✅ | ✅ | 完了 |
| レシピ画像アップロード | ✅ | ✅ | 完了 |
| 食材検索機能 | ✅ | ✅ | 完了 |
| レビュー機能 | ✅ | ✅ | 完了 |
| スケジュール管理 | ✅ | ✅ | 完了 |
| 買い物リスト | ✅ | ✅ | 完了 |
| アラート機能 | ✅ | ✅ | 完了 |
| 管理者機能 | ✅ | ✅ | 完了 |

---

## 🔍 詳細実装状況

### 1. ユーザー認証・管理

#### ✅ 完全実装
| エンドポイント | メソッド | バックエンド | フロントエンド | 画面 |
|--------------|---------|------------|--------------|------|
| `/api/users/register` | POST | ✅ | ✅ | RegisterPage |
| `/api/users/confirm` | POST | ✅ | ✅ | ConfirmEmailPage |
| `/api/users/resend-code` | POST | ✅ | ✅ | ConfirmEmailPage |
| `/api/users/login` | POST | ✅ | ✅ | LoginPage |
| `/api/users/profile/{userId}` | GET | ✅ | ✅ | ProfileEditPage |
| `/api/users/profile/{userId}` | PUT | ✅ | ✅ | ProfileEditPage |
| `/api/users/profile/image` | POST | ✅ | ✅ | ProfileEditPage |
| `/api/users/account/{userId}` | DELETE | ✅ | ❌ | - |

#### ❌ 未実装（フロントエンド）
- **アカウント削除機能**
  - エンドポイント: `DELETE /api/users/account/{userId}`
  - 必要な画面: プロフィール設定ページに「アカウント削除」ボタン
  - 優先度: 低

---

### 2. プロフィール管理

#### ✅ 完全実装
- プロフィール編集（ProfileEditPage）
- プロフィール画像アップロード（ImageUploader）
- 言語設定（LanguageSelector）

---

### 3. レシピ管理

#### ✅ 完全実装
| エンドポイント | メソッド | バックエンド | フロントエンド | 画面 |
|--------------|---------|------------|--------------|------|
| `/api/recipes` | GET | ✅ | ✅ | RecipeSearchPage |
| `/api/recipes/{id}` | GET | ✅ | ✅ | RecipeDetailPage |
| `/api/recipes` | POST | ✅ | ✅ | RecipeEditPage |
| `/api/recipes/with-images` | POST | ✅ | ✅ | RecipeEditPage |
| `/api/recipes/{id}` | PUT | ✅ | ✅ | RecipeEditPage |
| `/api/recipes/{id}` | DELETE | ✅ | ✅ | RecipeDetailPage |
| `/api/recipes/{id}/image` | POST | ✅ | ✅ | RecipeEditPage |
| `/api/recipes/{id}/steps/{index}/image` | POST | ✅ | ✅ | RecipeEditPage |
| `/api/recipes/search/by-ingredients` | POST | ✅ | ✅ | RecipeSearchPage |

---

### 3.1 食材検索機能（新規追加: 2024-12-24）

#### ✅ 完全実装
| 機能 | バックエンド | フロントエンド | 説明 |
|-----|------------|--------------|------|
| 食材からレシピ検索 | ✅ | ✅ | 複数食材のAND条件検索 |
| RecipeIngredientsテーブル | ✅ | - | 逆引きインデックス |
| トランザクション処理 | ✅ | - | Recipe + RecipeIngredients の原子性保証 |
| タブUI | - | ✅ | レシピ名検索 / 食材検索の切り替え |

**関連ファイル:**
- `backend/src/main/java/com/cookingapp/domain/entity/RecipeIngredient.java`
- `backend/src/main/java/com/cookingapp/infrastructure/repository/DynamoDBRecipeIngredientRepository.java`
- `backend/src/main/java/com/cookingapp/application/usecase/recipe/SearchRecipesByIngredientsUseCase.java`
- `frontend/src/components/recipe/RecipeSearchPage.tsx`

---

### 3.2 画像アップロード機能

#### ✅ 完全実装
| 機能 | バックエンド | フロントエンド | 説明 |
|-----|------------|--------------|------|
| メイン画像アップロード | ✅ | ✅ | S3 + Presigned URL |
| 手順画像アップロード | ✅ | ✅ | S3 + Presigned URL |
| ドラッグ&ドロップUI | - | ✅ | ImageUploaderコンポーネント |
| 画像圧縮・リサイズ | - | ✅ | browser-image-compression |

**画像圧縮設定:**
- メイン画像: 最大幅 1920px、最大 1MB
- 手順画像: 最大幅 1280px、最大 0.5MB

**関連ファイル:**
- `frontend/src/components/common/ImageUploader.tsx`
- `frontend/src/utils/imageCompression.ts`
- `backend/src/main/java/com/cookingapp/domain/service/ImageStorageService.java`

---

### 4. レビュー機能

#### ✅ 完全実装
| エンドポイント | メソッド | バックエンド | フロントエンド | 画面 |
|--------------|---------|------------|--------------|------|
| `/api/recipes/{recipeId}/reviews` | GET | ✅ | ✅ | RecipeDetailPage |
| `/api/recipes/{recipeId}/reviews` | POST | ✅ | ✅ | ReviewForm |
| `/api/reviews/{reviewId}` | PUT | ✅ | ✅ | ReviewForm |
| `/api/reviews/{reviewId}` | DELETE | ✅ | ✅ | ReviewList |
| `/api/reviews/{reviewId}/report` | POST | ✅ | ✅ | ReviewList |

---

### 5. スケジュール管理

#### ✅ 完全実装
| エンドポイント | メソッド | バックエンド | フロントエンド | 画面 |
|--------------|---------|------------|--------------|------|
| `/api/schedules` | GET | ✅ | ✅ | SchedulePage |
| `/api/schedules` | POST | ✅ | ✅ | SchedulePage |
| `/api/schedules/{scheduleId}` | PUT | ✅ | ✅ | SchedulePage |
| `/api/schedules/{scheduleId}` | DELETE | ✅ | ✅ | SchedulePage |
| `/api/schedules/{scheduleId}/convert-to-cooked` | POST | ✅ | ✅ | SchedulePage |

---

### 6. 買い物リスト

#### ✅ 完全実装
| エンドポイント | メソッド | バックエンド | フロントエンド | 画面 |
|--------------|---------|------------|--------------|------|
| `/api/shopping-list` | GET | ✅ | ✅ | ShoppingListPage |
| `/api/shopping-list` | POST | ✅ | ✅ | ShoppingListPage |
| `/api/shopping-list/{itemId}` | PUT | ✅ | ✅ | ShoppingListPage |
| `/api/shopping-list/{itemId}` | DELETE | ✅ | ✅ | ShoppingListPage |

---

### 7. アラート機能

#### ✅ 完全実装
| エンドポイント | メソッド | バックエンド | フロントエンド | 画面 |
|--------------|---------|------------|--------------|------|
| `/api/alerts/check` | GET | ✅ | ✅ | AlertModal |

---

### 8. 管理者機能

#### ✅ 完全実装
| エンドポイント | メソッド | バックエンド | フロントエンド | 画面 |
|--------------|---------|------------|--------------|------|
| `/api/admin/dashboard` | GET | ✅ | ✅ | AdminDashboardPage |
| `/api/admin/users/{userId}/suspend` | PUT | ✅ | ✅ | UserManagementPage |
| `/api/admin/users/{userId}` | DELETE | ✅ | ✅ | UserManagementPage |
| `/api/admin/recipes` | GET | ✅ | ✅ | RecipeManagementPage |
| `/api/admin/recipes/{recipeId}/status` | PUT | ✅ | ✅ | RecipeManagementPage |
| `/api/admin/recipes/{recipeId}` | DELETE | ✅ | ✅ | RecipeManagementPage |

---

## 🚀 優先実装タスク

### 完了済み ✅
1. **レシピ画像アップロード機能** - 2024-12-24完了
2. **食材検索機能** - 2024-12-24完了
3. **画像圧縮・リサイズ機能** - 2024-12-24完了
4. **ドラッグ&ドロップUI** - 2024-12-24完了

### 低優先度（Nice to have）
1. **アカウント削除機能**
   - 場所: プロフィール設定ページ（新規作成）
   - 工数: 1-2時間

2. **画像削除機能**
   - 場所: RecipeEditPage
   - 工数: 1-2時間

3. **アップロード進捗表示**
   - 場所: ImageUploader
   - 工数: 2-3時間

---

## 📝 実装ガイド

### 汎用ImageUploaderコンポーネントの使用方法

```typescript
// frontend/src/components/common/ImageUploader.tsx

import { ImageUploader } from '../../common/ImageUploader';

// プロフィール画像（円形）
<ImageUploader
  currentImageUrl={profileImageUrl}
  onImageSelect={handleImageSelect}
  onImageRemove={handleImageRemove}
  shape="circle"
  enableCompression={true}
/>

// レシピ画像（矩形）
<ImageUploader
  currentImageUrl={recipeImageUrl}
  onImageSelect={handleImageSelect}
  onImageRemove={handleImageRemove}
  shape="rectangle"
  height="h-40"
  enableCompression={true}
  compressionOptions={{ maxWidthOrHeight: 1920, maxSizeMB: 1 }}
/>
```

### ImageUploaderのプロパティ

| プロパティ | 型 | デフォルト | 説明 |
|-----------|-----|-----------|------|
| currentImageUrl | string | - | 現在の画像URL |
| onImageSelect | (file: File) => void | - | 画像選択時のコールバック |
| onImageRemove | () => void | - | 画像削除時のコールバック |
| maxSizeMB | number | 5 | 最大ファイルサイズ（MB） |
| allowedFormats | string[] | ['image/jpeg', 'image/png'] | 許可するファイル形式 |
| shape | 'circle' \| 'rectangle' | 'circle' | プレビューの形状 |
| height | string | 'h-32' | ドロップエリアの高さ |
| enableCompression | boolean | true | 圧縮の有効/無効 |
| compressionOptions | object | { maxWidthOrHeight: 1920, maxSizeMB: 1 } | 圧縮設定 |

### 画像圧縮ユーティリティの使用方法

```typescript
// frontend/src/utils/imageCompression.ts

import { compressImage, compressImages } from '../../utils/imageCompression';

// 単一画像の圧縮
const compressedFile = await compressImage(file, {
  maxWidthOrHeight: 1920,
  maxSizeMB: 1,
});

// 複数画像の圧縮
const compressedFiles = await compressImages(files, {
  maxWidthOrHeight: 1280,
  maxSizeMB: 0.5,
});
```

---

## 🔄 更新履歴

- **2024-12-24**: 食材検索機能・画像アップロード機能完了
  - RecipeIngredientsテーブル追加（逆引きインデックス）
  - 食材検索API実装（AND条件）
  - RecipeSearchPageにタブUI追加
  - ImageUploaderコンポーネント汎用化
  - 画像圧縮・リサイズ機能追加（browser-image-compression）
  - ドラッグ&ドロップUI対応
- **2024-12-18**: 初版作成
  - プロフィール編集機能完了
  - レシピ画像アップロード未実装を確認
  - アカウント削除機能未実装を確認

---

## 📌 メモ

### 既存の実装パターン
- **画像アップロード**: `ProfileEditPage` + `ImageUploader`
- **CRUD操作**: `RecipeEditPage` + `useRecipeEditHandlers`
- **API呼び出し**: `apiClient.ts`のヘルパー関数

### テスト
- フロントエンド: Vitest + Testing Library
- バックエンド: JUnit + Mockito
- 新機能実装時は必ずテストも追加

