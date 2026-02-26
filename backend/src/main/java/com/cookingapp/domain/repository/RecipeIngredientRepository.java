package com.cookingapp.domain.repository;

import java.util.List;

import com.cookingapp.domain.entity.RecipeIngredient;

/**
 * レシピ食材リポジトリインターフェース（逆引きインデックス用）
 */
public interface RecipeIngredientRepository {

    /**
     * 食材名でレシピを検索
     *
     * @param ingredientName 食材名
     * @return レシピ食材リスト
     */
    List<RecipeIngredient> findByIngredientName(String ingredientName);

    /**
     * 複数の食材名でレシピを検索（AND検索）
     * すべての食材を含むレシピのみ返す
     *
     * @param ingredientNames 食材名リスト
     * @return レシピ食材リスト
     */
    List<RecipeIngredient> findByIngredientNames(List<String> ingredientNames);

    /**
     * レシピIDに紐づく食材インデックスを削除
     *
     * @param recipeId レシピID
     */
    void deleteByRecipeId(String recipeId);

    /**
     * レシピ食材インデックスを一括保存
     *
     * @param recipeIngredients レシピ食材リスト
     */
    void saveAll(List<RecipeIngredient> recipeIngredients);
}
