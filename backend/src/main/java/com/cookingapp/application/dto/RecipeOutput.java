package com.cookingapp.application.dto;

import com.cookingapp.domain.model.Ingredient;
import com.cookingapp.domain.model.Recipe;
import com.cookingapp.domain.model.Step;
import lombok.Builder;
import lombok.Getter;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class RecipeOutput {
    private final String recipeId;
    private final String title;
    private final String authorId;
    private final List<IngredientOutput> ingredients;
    private final List<StepOutput> steps;
    private final Integer cookingTime;
    private final Boolean isPublic;
    private final String imageUrl;
    private final String createdAt;
    private final String updatedAt;

    public static RecipeOutput from(Recipe recipe, String presignedImageUrl) {
        List<IngredientOutput> ingredientOutputs = recipe.getIngredients() != null
                ? recipe.getIngredients().stream()
                    .map(IngredientOutput::from)
                    .collect(Collectors.toList())
                : Collections.emptyList();

        List<StepOutput> stepOutputs = recipe.getSteps() != null
                ? recipe.getSteps().stream()
                    .map(StepOutput::from)
                    .collect(Collectors.toList())
                : Collections.emptyList();

        return RecipeOutput.builder()
                .recipeId(recipe.getRecipeId())
                .title(recipe.getTitle())
                .authorId(recipe.getAuthorId())
                .ingredients(ingredientOutputs)
                .steps(stepOutputs)
                .cookingTime(recipe.getCookingTime())
                .isPublic(recipe.getIsPublic())
                .imageUrl(presignedImageUrl)
                .createdAt(recipe.getCreatedAt())
                .updatedAt(recipe.getUpdatedAt())
                .build();
    }

    @Getter
    @Builder
    public static class IngredientOutput {
        private final String name;
        private final String quantity;
        private final String unit;

        public static IngredientOutput from(Ingredient ingredient) {
            return IngredientOutput.builder()
                    .name(ingredient.getName())
                    .quantity(ingredient.getQuantity())
                    .unit(ingredient.getUnit())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class StepOutput {
        private final Integer stepNumber;
        private final String description;
        private final String imageUrl;
        private final String videoUrl;

        public static StepOutput from(Step step) {
            return StepOutput.builder()
                    .stepNumber(step.getStepNumber())
                    .description(step.getDescription())
                    .imageUrl(step.getImageUrl())
                    .videoUrl(step.getVideoUrl())
                    .build();
        }
    }
}
