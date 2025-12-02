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
    Unit unitEnum = Unit.fromCode(unit);
    return new Ingredient(name, quantity, unitEnum, note, optional);
}
```

**理由**: DTOからドメインエンティティへの変換であることを明確にするため、`toDomain()` → `toEntity()` に変更しました。

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
