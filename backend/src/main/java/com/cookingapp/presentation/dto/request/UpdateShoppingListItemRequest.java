package com.cookingapp.presentation.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 買い物リストアイテム更新リクエスト
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateShoppingListItemRequest {
    @NotNull(message = "IsChecked is required")
    private Boolean isChecked;
}
