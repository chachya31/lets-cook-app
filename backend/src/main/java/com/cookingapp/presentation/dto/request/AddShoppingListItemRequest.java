package com.cookingapp.presentation.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 買い物リストアイテム追加リクエスト
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddShoppingListItemRequest {
    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must be 100 characters or less")
    private String name;

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.01", message = "Quantity must be greater than 0")
    @DecimalMax(value = "9999", message = "Quantity must be 9999 or less")
    private BigDecimal quantity;

    @NotBlank(message = "Unit is required")
    private String unit;

    private String sourceRecipeId;
}
