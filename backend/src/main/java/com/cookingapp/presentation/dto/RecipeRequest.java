package com.cookingapp.presentation.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    @Valid
    private List<StepDto> steps;

    @Min(value = 0, message = "Cooking time must be 0 or greater")
    private int cookingTime;
}
