package com.cookingapp.application.dto;

import com.cookingapp.domain.model.Ingredient;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class IngredientInput {
    private final String name;
    private final String quantity;
    private final String unit;

    public Ingredient toEntity() {
        return Ingredient.builder()
                .name(name)
                .quantity(quantity)
                .unit(unit)
                .build();
    }
}
