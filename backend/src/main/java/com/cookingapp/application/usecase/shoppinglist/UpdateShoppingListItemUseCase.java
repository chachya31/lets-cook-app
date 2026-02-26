package com.cookingapp.application.usecase.shoppinglist;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.cookingapp.application.usecase.inventory.InventoryUseCase;
import com.cookingapp.domain.entity.ShoppingListItem;
import com.cookingapp.domain.exception.ShoppingListItemNotFoundException;
import com.cookingapp.domain.exception.UnauthorizedException;
import com.cookingapp.domain.repository.ShoppingListRepository;

/**
 * 買い物リストアイテム更新ユースケース
 */
@Service
public class UpdateShoppingListItemUseCase {

    private static final Logger log = LoggerFactory.getLogger(UpdateShoppingListItemUseCase.class);

    private final ShoppingListRepository shoppingListRepository;
    private final InventoryUseCase inventoryUseCase;

    public UpdateShoppingListItemUseCase(
            ShoppingListRepository shoppingListRepository,
            InventoryUseCase inventoryUseCase) {
        this.shoppingListRepository = shoppingListRepository;
        this.inventoryUseCase = inventoryUseCase;
    }

    /**
     * 買い物リストアイテムのチェック状態を更新
     * チェック時は自動的に在庫に追加
     */
    public ShoppingListItem execute(String userId, String itemId, boolean isChecked) {
        ShoppingListItem item = shoppingListRepository.findById(userId, itemId)
                .orElseThrow(() -> new ShoppingListItemNotFoundException(
                        "Shopping list item not found: itemId=" + itemId));

        if (!item.canEdit(userId)) {
            throw new UnauthorizedException("You are not authorized to update this item");
        }

        ShoppingListItem updatedItem = isChecked ? item.markAsChecked() : item.uncheck();

        // チェック時は在庫に追加
        if (isChecked && !item.isChecked()) {
            inventoryUseCase.addFromShoppingList(item);
            log.info("Added to inventory from shopping list: userId={}, name={}", userId, item.getName());
        }

        log.info("Updated shopping list item: userId={}, itemId={}, isChecked={}",
                userId, itemId, isChecked);
        return shoppingListRepository.save(updatedItem);
    }
}
