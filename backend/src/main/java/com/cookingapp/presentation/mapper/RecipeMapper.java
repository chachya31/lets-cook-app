package com.cookingapp.presentation.mapper;

import com.cookingapp.domain.entity.Recipe;
import com.cookingapp.domain.valueobject.Ingredient;
import com.cookingapp.presentation.dto.IngredientDto;
import com.cookingapp.presentation.dto.RecipeRequest;
import com.cookingapp.presentation.dto.RecipeResponse;

import java.util.List;
import java.util.stream.Collectors;

/**
 * RecipeMapper
 * Recipe エンティティと DTO の変換を担当
 */
public class RecipeMapper {

    private RecipeMapper() {
        // ユーティリティクラスのため、インスタンス化を防ぐ
    }

    /**
     * RecipeRequest から Ingredient リストに変換
     */
    public static List<Ingredient> toIngredients(RecipeRequest request) {
        return request.getIngredients().stream()
                .map(IngredientDto::toEntity)
                .collect(Collectors.toList());
    }

    /**
     * Recipe エンティティから RecipeResponse に変換
     */
    public static RecipeResponse toResponse(Recipe recipe) {
        List<IngredientDto> ingredientDtos = recipe.getIngredients().stream()
                .map(IngredientDto::from)
                .collect(Collectors.toList());

        return new RecipeResponse(
                recipe.getRecipeId(),
                recipe.getTitle(),
                recipe.getAuthorId(),
                ingredientDtos,
                recipe.getSteps(),
                recipe.getCookingTime(),
                recipe.getImageUrl(),
                recipe.isPublic(),
                recipe.getCreatedAt(),
                recipe.getUpdatedAt()
        );
    }

    /**
     * Recipe エンティティのリストから RecipeResponse のリストに変換
     */
    public static List<RecipeResponse> toResponseList(List<Recipe> recipes) {
        return recipes.stream()
                .map(RecipeMapper::toResponse)
                .collect(Collectors.toList());
    }
}
