package com.cookingapp.presentation.dto.response;

import com.cookingapp.domain.entity.ShoppingListItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 買い物リストアイテムレスポンス
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShoppingListItemResponse {
    private String itemId;
    private String userId;
    private String name;
    private BigDecimal quantity;
    private String unit;
    private boolean isChecked;
    private String isCheckedAt;
    private String addedAt;
    private String sourceRecipeId;

    public static ShoppingListItemResponse from(ShoppingListItem item) {
        return ShoppingListItemResponse.builder()
                .itemId(item.getItemId())
                .userId(item.getUserId())
                .name(item.getName())
                .quantity(item.getQuantity())
                .unit(item.getUnit().getCode())
                .isChecked(item.isChecked())
                .isCheckedAt(item.getIsCheckedAt() != null ? item.getIsCheckedAt().toString() : null)
                .addedAt(item.getAddedAt().toString())
                .sourceRecipeId(item.getSourceRecipeId())
                .build();
    }
}
