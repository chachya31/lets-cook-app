# 実装状況

このドキュメントは、バックエンドとフロントエンドの実装状況を追跡します。

最終更新: 2024-12-18

---

## 📊 実装状況サマリー

| 機能カテゴリ | バックエンド | フロントエンド | 状態 |
|------------|------------|--------------|------|
| ユーザー認証 | ✅ | ✅ | 完了 |
| プロフィール管理 | ✅ | ✅ | 完了 |
| レシピ管理 | ✅ | ⚠️ | 一部未実装 |
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
| `/api/recipes/{id}` | PUT | ✅ | ✅ | RecipeEditPage |
| `/api/recipes/{id}` | DELETE | ✅ | ✅ | RecipeDetailPage |

#### ❌ 未実装（フロントエンド）
- **レシピ画像アップロード機能**
  - エンドポイント: `POST /api/recipes/{id}/image`
  - バックエンド: ✅ 実装済み
  - フロントエンド: ❌ 未実装
  - 必要な作業:
    1. `RecipeEditPage`に`ImageUploader`コンポーネントを追加
    2. `recipeApi.ts`に`uploadRecipeImage`関数を追加（既に存在）
    3. レシピ作成・編集フローに画像アップロードを統合
  - 優先度: **高**
  - 参考実装: `ProfileEditPage`の画像アップロード実装

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

### 高優先度
1. **レシピ画像アップロード機能**
   - 場所: `RecipeEditPage`
   - 参考: `ProfileEditPage`の実装
   - 工数: 2-3時間

### 低優先度
2. **アカウント削除機能**
   - 場所: プロフィール設定ページ（新規作成）
   - 工数: 1-2時間

---

## 📝 実装ガイド

### レシピ画像アップロード機能の実装手順

#### 1. RecipeEditPageの修正
```typescript
// frontend/src/components/recipe/RecipeEdit/RecipeEditPage.tsx

import { ImageUploader } from '../../profile/ImageUploader';
import { uploadRecipeImage } from '../../../api/recipeApi';

// 状態追加
const [selectedImageFile, setSelectedImageFile] = useState<File | null>(null);
const [recipeImageUrl, setRecipeImageUrl] = useState<string | undefined>();

// 画像選択ハンドラー
const handleImageSelect = (file: File) => {
  setSelectedImageFile(file);
};

// 送信時に画像アップロード
const handleSubmit = async (e: React.FormEvent) => {
  // ... レシピ作成/更新 ...
  
  // 画像アップロード
  if (selectedImageFile && createdRecipe.recipeId) {
    await uploadRecipeImage(createdRecipe.recipeId, currentUser.userId, selectedImageFile);
  }
};

// JSX内に追加
<Card>
  <CardHeader>
    <CardTitle>{t('recipe.image')}</CardTitle>
  </CardHeader>
  <CardContent>
    <ImageUploader
      currentImageUrl={recipeImageUrl}
      onImageSelect={handleImageSelect}
      maxSizeMB={5}
    />
  </CardContent>
</Card>
```

#### 2. 翻訳追加
```json
// frontend/src/i18n/locales/ja.json
{
  "recipe": {
    "image": "レシピ画像"
  }
}
```

---

## 🔄 更新履歴

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

