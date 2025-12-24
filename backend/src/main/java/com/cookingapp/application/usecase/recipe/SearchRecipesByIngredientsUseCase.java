package com.cookingapp.application.usecase.recipe;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.cookingapp.domain.entity.RecipeIngredient;
import com.cookingapp.domain.repository.RecipeIngredientRepository;

/**
 * 食材によるレシピ検索ユースケース
 */
@Service
public class SearchRecipesByIngredientsUseCase {

    private static final Logger logger = LoggerFactory.getLogger(SearchRecipesByIngredientsUseCase.class);

    private final RecipeIngredientRepository recipeIngredientRepository;

    public SearchRecipesByIngredientsUseCase(RecipeIngredientRepository recipeIngredientRepository) {
        this.recipeIngredientRepository = recipeIngredientRepository;
    }

    /**
     * 複数の食材でレシピを検索（AND条件）
     * 指定されたすべての食材を含むレシピのみを返す
     *
     * @param ingredientNames 食材名リスト
     * @return 検索結果（レシピの簡易情報を含む）
     */
    public List<RecipeIngredient> execute(List<String> ingredientNames) {
        if (ingredientNames == null || ingredientNames.isEmpty()) {
            logger.info("No ingredients provided for search");
            return new ArrayList<>();
        }

        // 空文字やnullを除去
        List<String> validIngredients = ingredientNames.stream()
                .filter(name -> name != null && !name.trim().isEmpty())
                .map(String::trim)
                .collect(Collectors.toList());

        if (validIngredients.isEmpty()) {
            logger.info("No valid ingredients after filtering");
            return new ArrayList<>();
        }

        logger.info("Searching recipes by ingredients: {}", validIngredients);

        // 各食材でレシピを検索
        Map<String, Set<String>> recipeIdsByIngredient = validIngredients.stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        ingredientName -> recipeIngredientRepository.findByIngredientName(ingredientName)
                                .stream()
                                .map(RecipeIngredient::getRecipeId)
                                .collect(Collectors.toSet())));

        // AND条件: すべての食材を含むレシピIDを抽出（交集合）
        Set<String> commonRecipeIds = null;
        for (Set<String> recipeIds : recipeIdsByIngredient.values()) {
            if (commonRecipeIds == null) {
                commonRecipeIds = new HashSet<>(recipeIds);
            } else {
                commonRecipeIds.retainAll(recipeIds);
            }
        }

        if (commonRecipeIds == null || commonRecipeIds.isEmpty()) {
            logger.info("No recipes found containing all ingredients: {}", validIngredients);
            return new ArrayList<>();
        }

        // 最初の食材の検索結果から、共通レシピIDに該当するものを返す
        // （RecipeTitle, RecipeImageUrl を含む情報を返すため）
        String firstIngredient = validIngredients.get(0);
        Set<String> finalCommonRecipeIds = commonRecipeIds;

        List<RecipeIngredient> results = recipeIngredientRepository.findByIngredientName(firstIngredient)
                .stream()
                .filter(ri -> finalCommonRecipeIds.contains(ri.getRecipeId()))
                .collect(Collectors.toList());

        logger.info("Found {} recipes containing all ingredients: {}", results.size(), validIngredients);

        return results;
    }

    /**
     * 単一の食材でレシピを検索
     *
     * @param ingredientName 食材名
     * @return 検索結果
     */
    public List<RecipeIngredient> executeByIngredient(String ingredientName) {
        if (ingredientName == null || ingredientName.trim().isEmpty()) {
            return new ArrayList<>();
        }

        logger.info("Searching recipes by ingredient: {}", ingredientName);
        return recipeIngredientRepository.findByIngredientName(ingredientName.trim());
    }
}
