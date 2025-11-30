package com.cookingapp.presentation.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * レシピ作成・更新リクエストDTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecipeRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title must be 100 characters or less")
    private String title;

    @NotNull(message = "Ingredients are required")
    @Size(min = 1, message = "At least one ingredient is required")
    @Valid
    private List<IngredientDto> ingredients;

    @NotNull(message = "Steps are required")
    @Size(min = 1, message = "At least one step is required")
    private List<@NotBlank(message = "Step cannot be empty") String> steps;

    @Min(value = 0, message = "Cooking time must be 0 or greater")
    private int cookingTime;
}
