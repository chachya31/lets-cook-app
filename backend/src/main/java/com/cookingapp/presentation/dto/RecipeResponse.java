package com.cookingapp.presentation.dto;

import com.cookingapp.domain.entity.Recipe;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * レシピレスポンスDTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecipeResponse {

    private String recipeId;
    private String title;
    private String authorId;
    private List<IngredientDto> ingredients;
    private List<String> steps;
    private int cookingTime;
    private String imageUrl;
    private boolean isPublic;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * ドメインエンティティからDTOに変換
     */
    public static RecipeResponse from(Recipe recipe) {
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
}
