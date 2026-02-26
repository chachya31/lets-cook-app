package com.cookingapp.domain.entity;

import lombok.Getter;

/**
 * レシピ食材エンティティ（逆引きインデックス用）
 * 食材名からレシピを検索するためのルックアップテーブル
 */
@Getter
public class RecipeIngredient {
    private final String ingredientName;
    private final String recipeId;
    private final String recipeTitle;
    private String recipeImageUrl;

    /**
     * レシピ食材を作成
     *
     * @param ingredientName 食材名（正規化済み）
     * @param recipeId       レシピID
     * @param recipeTitle    レシピタイトル
     * @param recipeImageUrl レシピ画像URL（nullable）
     */
    public RecipeIngredient(String ingredientName, String recipeId,
            String recipeTitle, String recipeImageUrl) {
        validateIngredientName(ingredientName);
        validateRecipeId(recipeId);
        validateRecipeTitle(recipeTitle);

        this.ingredientName = normalizeIngredientName(ingredientName);
        this.recipeId = recipeId;
        this.recipeTitle = recipeTitle;
        this.recipeImageUrl = recipeImageUrl;
    }

    private void validateIngredientName(String ingredientName) {
        if (ingredientName == null || ingredientName.trim().isEmpty()) {
            throw new IllegalArgumentException("食材名は必須です");
        }
    }

    private void validateRecipeId(String recipeId) {
        if (recipeId == null || recipeId.trim().isEmpty()) {
            throw new IllegalArgumentException("レシピIDは必須です");
        }
    }

    private void validateRecipeTitle(String recipeTitle) {
        if (recipeTitle == null || recipeTitle.trim().isEmpty()) {
            throw new IllegalArgumentException("レシピタイトルは必須です");
        }
    }

    /**
     * 食材名を正規化
     * Phase 1: trim のみ
     * Phase 2: カタカナ/ひらがな統一などを追加予定
     */
    public static String normalizeIngredientName(String ingredientName) {
        if (ingredientName == null) {
            return null;
        }
        return ingredientName.trim();
    }

    /**
     * レシピ画像URLを更新（PresignedURL生成用）
     */
    public void updateRecipeImageUrl(String recipeImageUrl) {
        this.recipeImageUrl = recipeImageUrl;
    }
}
