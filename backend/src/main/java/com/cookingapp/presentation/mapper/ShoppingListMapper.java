package com.cookingapp.presentation.mapper;

import com.cookingapp.domain.entity.ShoppingListItem;
import com.cookingapp.presentation.dto.response.ShoppingListItemResponse;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ShoppingListMapper
 * ShoppingListItem エンティティと DTO の変換を担当
 */
public class ShoppingListMapper {

    private ShoppingListMapper() {
        // ユーティリティクラスのため、インスタンス化を防ぐ
    }

    /**
     * ShoppingListItem エンティティから ShoppingListItemResponse に変換
     */
    public static ShoppingListItemResponse toResponse(ShoppingListItem item) {
        return ShoppingListItemResponse.from(item);
    }

    /**
     * ShoppingListItem エンティティのリストから ShoppingListItemResponse のリストに変換
     */
    public static List<ShoppingListItemResponse> toResponseList(List<ShoppingListItem> items) {
        return items.stream()
                .map(ShoppingListMapper::toResponse)
                .collect(Collectors.toList());
    }
}
