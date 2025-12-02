package com.cookingapp.presentation.dto;

import com.cookingapp.domain.valueobject.Ingredient;
import com.cookingapp.domain.valueobject.Unit;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

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

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Quantity must be greater than 0")
    private BigDecimal quantity;

    @NotBlank(message = "Unit is required")
    private String unit;

    @Size(max = 200, message = "Note must be 200 characters or less")
    private String note;

    private boolean optional;

    /**
     * DTOからドメインエンティティに変換
     */
    public Ingredient toEntity() {
        Unit unitEnum = Unit.fromCode(unit);
        return new Ingredient(name, quantity, unitEnum, note, optional);
    }

    /**
     * ドメインオブジェクトからDTOに変換
     */
    public static IngredientDto from(Ingredient ingredient) {
        return new IngredientDto(
                ingredient.getName(),
                ingredient.getQuantity(),
                ingredient.getUnit().getCode(),
                ingredient.getNote(),
                ingredient.isOptional()
        );
    }
}
