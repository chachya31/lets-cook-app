package com.cookingapp.presentation.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class RecipeRequest {

    @NotBlank(message = "Title is required")
    private final String title;

    @NotEmpty(message = "At least one ingredient is required")
    @Valid
    private final List<IngredientDto> ingredients;

    @NotEmpty(message = "At least one step is required")
    @Valid
    private final List<StepDto> steps;

    @NotNull(message = "Cooking time is required")
    @Min(value = 1, message = "Cooking time must be at least 1 minute")
    private final Integer cookingTime;

    @NotNull(message = "IsPublic is required")
    private final Boolean isPublic;

    private final String imageKey;

    public RecipeRequest() {
        this.title = null;
        this.ingredients = null;
        this.steps = null;
        this.cookingTime = null;
        this.isPublic = null;
        this.imageKey = null;
    }
}
