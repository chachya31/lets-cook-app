package com.cookingapp.presentation.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 食材検索レスポンス
 */
@Getter
@AllArgsConstructor
public class RecipeSearchByIngredientResponse {

    private final List<RecipeSummary> recipes;
    private final int totalCount;
    private final List<String> searchedIngredients;

    /**
     * レシピ概要（検索結果用）
     */
    @Getter
    @AllArgsConstructor
    public static class RecipeSummary {
        private final String recipeId;
        private final String title;
        private final String imageUrl;
    }
}
