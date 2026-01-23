package com.cookingapp.presentation.dto;

import com.cookingapp.domain.model.Ingredient;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class IngredientDto {

    @NotBlank(message = "Ingredient name is required")
    private final String name;

    private final String quantity;

    private final String unit;

    public IngredientDto() {
        this.name = null;
        this.quantity = null;
        this.unit = null;
    }

    public Ingredient toEntity() {
        return Ingredient.builder()
                .name(name)
                .quantity(quantity)
                .unit(unit)
                .build();
    }

    public static IngredientDto from(Ingredient ingredient) {
        return IngredientDto.builder()
                .name(ingredient.getName())
                .quantity(ingredient.getQuantity())
                .unit(ingredient.getUnit())
                .build();
    }
}
