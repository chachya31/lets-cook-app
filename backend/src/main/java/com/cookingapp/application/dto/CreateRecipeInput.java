package com.cookingapp.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CreateRecipeInput {
    private final String title;
    private final List<IngredientInput> ingredients;
    private final List<StepInput> steps;
    private final Integer cookingTime;
    private final Boolean isPublic;
    private final String imageKey;
}
