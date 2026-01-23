package com.cookingapp.presentation.dto;

import com.cookingapp.domain.model.Recipe;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@AllArgsConstructor
public class RecipeResponse {

    private final String recipeId;
    private final String title;
    private final String authorId;
    private final List<IngredientDto> ingredients;
    private final List<StepDto> steps;
    private final Integer cookingTime;
    private final Boolean isPublic;
    private final String imageUrl;
    private final String createdAt;
    private final String updatedAt;

    public static RecipeResponse from(Recipe recipe) {
        return from(recipe, recipe.getImageUrl());
    }

    public static RecipeResponse from(Recipe recipe, String imageUrl) {
        List<IngredientDto> ingredientDtos = null;
        if (recipe.getIngredients() != null) {
            ingredientDtos = recipe.getIngredients().stream()
                    .map(IngredientDto::from)
                    .collect(Collectors.toList());
        }

        List<StepDto> stepDtos = null;
        if (recipe.getSteps() != null) {
            stepDtos = recipe.getSteps().stream()
                    .map(StepDto::from)
                    .collect(Collectors.toList());
        }

        return RecipeResponse.builder()
                .recipeId(recipe.getRecipeId())
                .title(recipe.getTitle())
                .authorId(recipe.getAuthorId())
                .ingredients(ingredientDtos)
                .steps(stepDtos)
                .cookingTime(recipe.getCookingTime())
                .isPublic(recipe.getIsPublic())
                .imageUrl(imageUrl)
                .createdAt(recipe.getCreatedAt())
                .updatedAt(recipe.getUpdatedAt())
                .build();
    }
}
