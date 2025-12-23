package com.cookingapp.presentation.dto;

import java.math.BigDecimal;

import com.cookingapp.domain.valueobject.Ingredient;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 食材DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IngredientDto {

    @NotBlank(message = "Ingredient name is required")
    @Size(max = 100, message = "Ingredient name must be 100 characters or less")
    private String name;

    @DecimalMin(value = "0.0", inclusive = false, message = "Quantity must be greater than 0")
    private BigDecimal quantity;

    @Size(max = 50, message = "Unit must be 50 characters or less")
    private String unit;

    @Size(max = 200, message = "Note must be 200 characters or less")
    private String note;

    private boolean optional;

    /**
     * DTOからドメインエンティティに変換
     */
    public Ingredient toEntity() {
        return new Ingredient(name, quantity, unit, note, optional);
    }

    /**
     * ドメインオブジェクトからDTOに変換
     */
    public static IngredientDto from(Ingredient ingredient) {
        return new IngredientDto(
                ingredient.getName(),
                ingredient.getQuantity(),
                ingredient.getUnit(),
                ingredient.getNote(),
                ingredient.isOptional());
    }
}
