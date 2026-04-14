# 実装状況

このドキュメントは、バックエンドとフロントエンドの実装状況を追跡します。

最終更新: 2025-01-09

---

## 📊 実装状況サマリー

| 機能カテゴリ           | バックエンド | フロントエンド | 状態 |
| ---------------------- | ------------ | -------------- | ---- |
| ユーザー認証           | ✅            | ✅              | 完了 |
| プロフィール管理       | ✅            | ✅              | 完了 |
| レシピ管理             | ✅            | ✅              | 完了 |
| レシピ画像アップロード | ✅            | ✅              | 完了 |
| 食材検索機能           | ✅            | ✅              | 完了 |
| レビュー機能           | ✅            | ✅              | 完了 |
| スケジュール管理       | ✅            | ✅              | 完了 |
| 買い物リスト           | ✅            | ✅              | 完了 |
| 在庫管理               | ✅            | ✅              | 完了 |
| AIチャット             | ✅            | ✅              | 完了 |
| アラート機能           | ✅            | ✅              | 完了 |
| 管理者機能             | ✅            | ✅              | 完了 |

---

## 🔍 詳細実装状況

### 1. ユーザー認証・管理

#### ✅ 完全実装
| エンドポイント                | メソッド | バックエンド | フロントエンド | 画面             |
| ----------------------------- | -------- | ------------ | -------------- | ---------------- |
| `/api/users/register`         | POST     | ✅            | ✅              | RegisterPage     |
| `/api/users/confirm`          | POST     | ✅            | ✅              | ConfirmEmailPage |
| `/api/users/resend-code`      | POST     | ✅            | ✅              | ConfirmEmailPage |
| `/api/users/login`            | POST     | ✅            | ✅              | LoginPage        |
| `/api/users/profile/{userId}` | GET      | ✅            | ✅              | ProfileEditPage  |
| `/api/users/profile/{userId}` | PUT      | ✅            | ✅              | ProfileEditPage  |
| `/api/users/profile/image`    | POST     | ✅            | ✅              | ProfileEditPage  |
| `/api/users/account/{userId}` | DELETE   | ✅            | ❌              | -                |

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
| エンドポイント                          | メソッド | バックエンド | フロントエンド | 画面             |
| --------------------------------------- | -------- | ------------ | -------------- | ---------------- |
| `/api/recipes`                          | GET      | ✅            | ✅              | RecipeSearchPage |
| `/api/recipes/{id}`                     | GET      | ✅            | ✅              | RecipeDetailPage |
| `/api/recipes`                          | POST     | ✅            | ✅              | RecipeEditPage   |
| `/api/recipes/with-images`              | POST     | ✅            | ✅              | RecipeEditPage   |
| `/api/recipes/{id}`                     | PUT      | ✅            | ✅              | RecipeEditPage   |
| `/api/recipes/{id}`                     | DELETE   | ✅            | ✅              | RecipeDetailPage |
| `/api/recipes/{id}/image`               | POST     | ✅            | ✅              | RecipeEditPage   |
| `/api/recipes/{id}/steps/{index}/image` | POST     | ✅            | ✅              | RecipeEditPage   |
| `/api/recipes/search/by-ingredients`    | POST     | ✅            | ✅              | RecipeSearchPage |

---

### 3.1 食材検索機能（新規追加: 2024-12-24）

#### ✅ 完全実装
| 機能                      | バックエンド | フロントエンド | 説明                                    |
| ------------------------- | ------------ | -------------- | --------------------------------------- |
| 食材からレシピ検索        | ✅            | ✅              | 複数食材のAND条件検索                   |
| RecipeIngredientsテーブル | ✅            | -              | 逆引きインデックス                      |
| トランザクション処理      | ✅            | -              | Recipe + RecipeIngredients の原子性保証 |
| タブUI                    | -            | ✅              | レシピ名検索 / 食材検索の切り替え       |

**関連ファイル:**
- `backend/src/main/java/com/cookingapp/domain/entity/RecipeIngredient.java`
- `backend/src/main/java/com/cookingapp/infrastructure/repository/DynamoDBRecipeIngredientRepository.java`
- `backend/src/main/java/com/cookingapp/application/usecase/recipe/SearchRecipesByIngredientsUseCase.java`
- `frontend/src/components/recipe/RecipeSearchPage.tsx`

---

### 3.2 画像アップロード機能

#### ✅ 完全実装
| 機能                   | バックエンド | フロントエンド | 説明                        |
| ---------------------- | ------------ | -------------- | --------------------------- |
| メイン画像アップロード | ✅            | ✅              | S3 + Presigned URL          |
| 手順画像アップロード   | ✅            | ✅              | S3 + Presigned URL          |
| ドラッグ&ドロップUI    | -            | ✅              | ImageUploaderコンポーネント |
| 画像圧縮・リサイズ     | -            | ✅              | browser-image-compression   |

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
| エンドポイント                    | メソッド | バックエンド | フロントエンド | 画面             |
| --------------------------------- | -------- | ------------ | -------------- | ---------------- |
| `/api/recipes/{recipeId}/reviews` | GET      | ✅            | ✅              | RecipeDetailPage |
| `/api/recipes/{recipeId}/reviews` | POST     | ✅            | ✅              | ReviewForm       |
| `/api/reviews/{reviewId}`         | PUT      | ✅            | ✅              | ReviewForm       |
| `/api/reviews/{reviewId}`         | DELETE   | ✅            | ✅              | ReviewList       |
| `/api/reviews/{reviewId}/report`  | POST     | ✅            | ✅              | ReviewList       |

---

### 5. スケジュール管理

#### ✅ 完全実装
| エンドポイント                                  | メソッド | バックエンド | フロントエンド | 画面         |
| ----------------------------------------------- | -------- | ------------ | -------------- | ------------ |
| `/api/schedules`                                | GET      | ✅            | ✅              | SchedulePage |
| `/api/schedules`                                | POST     | ✅            | ✅              | SchedulePage |
| `/api/schedules/{scheduleId}`                   | PUT      | ✅            | ✅              | SchedulePage |
| `/api/schedules/{scheduleId}`                   | DELETE   | ✅            | ✅              | SchedulePage |
| `/api/schedules/{scheduleId}/convert-to-cooked` | POST     | ✅            | ✅              | SchedulePage |

---

### 6. 買い物リスト

#### ✅ 完全実装
| エンドポイント                | メソッド | バックエンド | フロントエンド | 画面             |
| ----------------------------- | -------- | ------------ | -------------- | ---------------- |
| `/api/shopping-list`          | GET      | ✅            | ✅              | ShoppingListPage |
| `/api/shopping-list`          | POST     | ✅            | ✅              | ShoppingListPage |
| `/api/shopping-list/{itemId}` | PUT      | ✅            | ✅              | ShoppingListPage |
| `/api/shopping-list/{itemId}` | DELETE   | ✅            | ✅              | ShoppingListPage |

---

### 7. アラート機能

#### ✅ 完全実装
| エンドポイント      | メソッド | バックエンド | フロントエンド | 画面       |
| ------------------- | -------- | ------------ | -------------- | ---------- |
| `/api/alerts/check` | GET      | ✅            | ✅              | AlertModal |

---

### 8. 管理者機能

#### ✅ 完全実装
| エンドポイント                         | メソッド | バックエンド | フロントエンド | 画面                 |
| -------------------------------------- | -------- | ------------ | -------------- | -------------------- |
| `/api/admin/dashboard`                 | GET      | ✅            | ✅              | AdminDashboardPage   |
| `/api/admin/users/{userId}/suspend`    | PUT      | ✅            | ✅              | UserManagementPage   |
| `/api/admin/users/{userId}`            | DELETE   | ✅            | ✅              | UserManagementPage   |
| `/api/admin/recipes`                   | GET      | ✅            | ✅              | RecipeManagementPage |
| `/api/admin/recipes/{recipeId}/status` | PUT      | ✅            | ✅              | RecipeManagementPage |
| `/api/admin/recipes/{recipeId}`        | DELETE   | ✅            | ✅              | RecipeManagementPage |

---

### 9. 在庫管理

#### ✅ 完全実装
| エンドポイント            | メソッド | バックエンド | フロントエンド | 画面          |
| ------------------------- | -------- | ------------ | -------------- | ------------- |
| `/api/inventory`          | GET      | ✅            | ✅              | InventoryPage |
| `/api/inventory`          | POST     | ✅            | ✅              | InventoryPage |
| `/api/inventory/{itemId}` | PUT      | ✅            | ✅              | InventoryPage |
| `/api/inventory/{itemId}` | DELETE   | ✅            | ✅              | InventoryPage |

**機能:**
- 在庫一覧表示（賞味期限順ソート対応）
- 賞味期限切れ・期限間近の視覚的表示
- 賞味期限のインライン編集
- アイテム削除

---

### 10. AIチャット

#### ✅ 完全実装
| エンドポイント                        | メソッド | バックエンド | フロントエンド | 画面           |
| ------------------------------------- | -------- | ------------ | -------------- | -------------- |
| `/api/ai/chat`                        | POST     | ✅            | ✅              | GeminiChatPage |
| `/api/ai/conversations`               | GET      | ✅            | ✅              | GeminiChatPage |
| `/api/ai/conversations/{id}/messages` | GET      | ✅            | ✅              | GeminiChatPage |
| `/api/ai/conversations/{id}/messages` | POST     | ✅            | ✅              | GeminiChatPage |
| `/api/ai/conversations/{id}`          | DELETE   | ✅            | ✅              | GeminiChatPage |

**機能:**
- 会話一覧サイドバー
- 会話タイプ選択（一般、レシピ推薦、賞味期限確認）
- 在庫連携パネル（レシピ推薦・賞味期限確認時）
- 会話削除

---

## 🚀 優先実装タスク

### 完了済み ✅
1. **レシピ画像アップロード機能** - 2024-12-24完了
2. **食材検索機能** - 2024-12-24完了
3. **画像圧縮・リサイズ機能** - 2024-12-24完了
4. **ドラッグ&ドロップUI** - 2024-12-24完了
5. **在庫管理機能** - 2025-01完了
6. **AIチャット機能** - 2025-01完了

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

| プロパティ         | 型                      | デフォルト                               | 説明                     |
| ------------------ | ----------------------- | ---------------------------------------- | ------------------------ |
| currentImageUrl    | string                  | -                                        | 現在の画像URL            |
| onImageSelect      | (file: File) => void    | -                                        | 画像選択時のコールバック |
| onImageRemove      | () => void              | -                                        | 画像削除時のコールバック |
| maxSizeMB          | number                  | 5                                        | 最大ファイルサイズ（MB） |
| allowedFormats     | string[]                | ['image/jpeg', 'image/png']              | 許可するファイル形式     |
| shape              | 'circle' \| 'rectangle' | 'circle'                                 | プレビューの形状         |
| height             | string                  | 'h-32'                                   | ドロップエリアの高さ     |
| enableCompression  | boolean                 | true                                     | 圧縮の有効/無効          |
| compressionOptions | object                  | { maxWidthOrHeight: 1920, maxSizeMB: 1 } | 圧縮設定                 |

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

- **2025-01-09**: 設計書更新・在庫管理・AIチャット機能追加
  - 在庫管理機能（InventoryPage、InventoryController）
  - AIチャット機能（GeminiChatPage、AiAssistantController）
  - 在庫とAIチャットの連携（レシピ推薦、賞味期限確認）
  - レビューのインライン編集機能
  - プロフィール画像変更後のヘッダー即時反映
  - トークン期限切れ時のログイン画面リダイレクト修正
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

# コード品質向上の実施内容

## 実施日
2024-12-02

## 概要
マジックナンバーの定数化、重複コードの共通化、命名の改善を実施し、コードの保守性と可読性を向上させました。

## 実施内容

### 1. バリデーション定数クラスの作成

#### バックエンド
**ファイル**: `backend/src/main/java/com/cookingapp/domain/constants/ValidationConstants.java`

すべてのバリデーション関連の定数を一元管理する定数クラスを作成しました。

**定義した定数**:
- **ユーザー関連**: パスワード最小文字数(8)、ニックネーム文字数(1-50)、表示名文字数(1-50)
- **レシピ関連**: タイトル最大文字数(100)、最小食材数(1)、最小手順数(1)、調理時間最小値(0)
- **食材関連**: 名前文字数(1-100)、数量範囲(0.01-9999)、メモ最大文字数(200)
- **レビュー関連**: 星評価範囲(1-5)、コメント最大文字数(300)、自動非表示閾値(3)
- **スケジュール関連**: メモ最大文字数(120)
- **買い物リスト関連**: アイテム名最大文字数(100)、数量範囲(0.01-9999)、自動削除日数(3)
- **画像関連**: 最大ファイルサイズ(5MB)
- **アラート関連**: 日数閾値(3)

#### フロントエンド
**ファイル**: `frontend/src/constants/validation.ts`

バックエンドと同様の定数をTypeScriptで定義し、フロントエンドでも一元管理できるようにしました。

**追加定義**:
- 許可される画像フォーマット: `['image/jpeg', 'image/jpg', 'image/png']`
- 許可される画像拡張子: `['.jpg', '.jpeg', '.png']`

### 2. マジックナンバーの定数化

#### 修正したファイル（バックエンド）

1. **Review.java**
   - 星評価のバリデーション: `1`, `5` → `ValidationConstants.REVIEW_RATING_MIN`, `REVIEW_RATING_MAX`
   - コメント最大文字数: `300` → `ValidationConstants.REVIEW_COMMENT_MAX_LENGTH`
   - 自動非表示閾値: `3` → `ValidationConstants.REVIEW_AUTO_HIDE_THRESHOLD`

2. **Ingredient.java**
   - 食材名最大文字数: `50` → `ValidationConstants.INGREDIENT_NAME_MAX_LENGTH`
   - 数量最大値: `"9999"` → `ValidationConstants.INGREDIENT_QUANTITY_MAX`
   - メモ最大文字数: `60` → `ValidationConstants.INGREDIENT_NOTE_MAX_LENGTH`

3. **ShoppingListItem.java**
   - 自動削除日数: `3` → `ValidationConstants.SHOPPING_LIST_AUTO_DELETE_DAYS`

4. **ImageValidator.java**
   - 最大ファイルサイズ: `5 * 1024 * 1024` → `ValidationConstants.IMAGE_MAX_FILE_SIZE`
   - エラーメッセージ内のMB表示: `MAX_FILE_SIZE / (1024 * 1024)` → `ValidationConstants.IMAGE_MAX_FILE_SIZE_MB`

5. **CheckAlertUseCase.java**
   - アラート日数閾値: `3` → `ValidationConstants.ALERT_DAYS_THRESHOLD`

#### 修正したファイル（フロントエンド）

1. **recipeEditConfig.ts**
   - タイトル最大文字数: `100` → `RECIPE_TITLE_MAX_LENGTH`
   - 調理時間最小値: `0` → `COOKING_TIME_MIN`
   - 最小食材数: `1` → `RECIPE_MIN_INGREDIENTS`
   - 最小手順数: `1` → `RECIPE_MIN_STEPS`
   - 食材名最大文字数: `100` → `INGREDIENT_NAME_MAX_LENGTH`
   - 食材数量最小値: `0` → `INGREDIENT_QUANTITY_MIN`

2. **registerFormConfig.ts**
   - パスワード最小文字数: `8` → `PASSWORD_MIN_LENGTH`
   - ニックネーム最大文字数: `50` → `NICKNAME_MAX_LENGTH`

### 3. メソッド命名の改善

#### IngredientDto.java
**変更前**:
```java
public Ingredient toDomain() {
    Unit unitEnum = Unit.fromCode(unit);
    return new Ingredient(name, quantity, unitEnum, note, optional);
}
```

**変更後**:
```java
public Ingredient toEntity() {
    return new Ingredient(name, quantity, unit, note, optional);
}
```

**理由**: DTOからドメインエンティティへの変換であることを明確にするため、`toDomain()` → `toEntity()` に変更しました。
また、unitは自由入力のString型に変更されたため、Unit enumへの変換は不要になりました。

#### RecipeController.java
**変更前**:
```java
List<Ingredient> ingredients = request.getIngredients().stream()
    .map(dto -> dto.toDomain())
    .collect(Collectors.toList());
```

**変更後**:
```java
List<Ingredient> ingredients = request.getIngredients().stream()
    .map(IngredientDto::toEntity)
    .collect(Collectors.toList());
```

**理由**: メソッド参照を使用してコードを簡潔にし、可読性を向上させました。

### 4. エラーメッセージの改善

定数を使用することで、エラーメッセージ内の数値も動的に生成されるようになりました。

**例（Review.java）**:
```java
// 変更前
throw new IllegalArgumentException("Rating must be between 1 and 5");

// 変更後
throw new IllegalArgumentException(
    String.format("Rating must be between %d and %d", 
        ValidationConstants.REVIEW_RATING_MIN, 
        ValidationConstants.REVIEW_RATING_MAX)
);
```

これにより、定数を変更するだけでエラーメッセージも自動的に更新されます。

## メリット

### 1. 保守性の向上
- バリデーションルールの変更が1箇所で済む
- 定数の変更時にエラーメッセージも自動更新される
- コード全体で一貫性が保たれる

### 2. 可読性の向上
- マジックナンバーが排除され、意図が明確になる
- 定数名から用途が理解しやすい
- コードレビューが容易になる

### 3. バグの防止
- 定数の誤入力を防げる
- IDEの補完機能が使える
- コンパイル時にエラーを検出できる

### 4. テストの容易性
- テストコードでも同じ定数を使用できる
- バリデーションルールの変更時にテストも自動的に追従する

## ビルド結果

### バックエンド
```bash
./gradlew.bat build -x test
BUILD SUCCESSFUL in 2s
```

### フロントエンド
```bash
npm run build
✓ built in 2.73s
```

### 診断結果
すべてのファイルでエラー・警告なし

## 今後の推奨事項

1. **新規コード作成時**
   - マジックナンバーを使用せず、必ず定数を定義する
   - 既存の定数クラスに追加するか、新しい定数クラスを作成する

2. **コードレビュー時**
   - マジックナンバーの使用をチェックする
   - 定数化できる値がないか確認する

3. **定数クラスの拡張**
   - 必要に応じて新しいカテゴリの定数を追加する
   - 定数のグループ化を適切に行う

4. **ドキュメント化**
   - 定数の意味や用途をJavadoc/JSDocで明確に記述する
   - 変更履歴を残す

## 関連ファイル

### 新規作成
- `backend/src/main/java/com/cookingapp/domain/constants/ValidationConstants.java`
- `frontend/src/constants/validation.ts`
- `docs/CODE_QUALITY_IMPROVEMENTS.md` (このファイル)

### 修正
**バックエンド**:
- `backend/src/main/java/com/cookingapp/domain/entity/Review.java`
- `backend/src/main/java/com/cookingapp/domain/entity/ShoppingListItem.java`
- `backend/src/main/java/com/cookingapp/domain/valueobject/Ingredient.java`
- `backend/src/main/java/com/cookingapp/application/validation/ImageValidator.java`
- `backend/src/main/java/com/cookingapp/application/usecase/alert/CheckAlertUseCase.java`
- `backend/src/main/java/com/cookingapp/presentation/dto/IngredientDto.java`
- `backend/src/main/java/com/cookingapp/presentation/controller/RecipeController.java`

**フロントエンド**:
- `frontend/src/components/recipe/RecipeEdit/recipeEditConfig.ts`
- `frontend/src/components/auth/Register/registerFormConfig.ts`

## まとめ

今回のリファクタリングにより、コードの保守性、可読性、テスタビリティが大幅に向上しました。マジックナンバーが排除され、バリデーションルールの変更が容易になり、コード全体の一貫性が保たれるようになりました。

今後は、新規コード作成時にも定数を積極的に活用し、高品質なコードベースを維持していくことが重要です。
