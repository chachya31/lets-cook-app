package com.cookingapp.application.usecase.shoppinglist;

import com.cookingapp.domain.entity.ShoppingListItem;
import com.cookingapp.domain.exception.ShoppingListItemNotFoundException;
import com.cookingapp.domain.exception.UnauthorizedException;
import com.cookingapp.domain.repository.ShoppingListRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 買い物リストアイテム更新ユースケース
 */
@Service
@RequiredArgsConstructor
public class UpdateShoppingListItemUseCase {
    private static final Logger log = LoggerFactory.getLogger(UpdateShoppingListItemUseCase.class);
    private final ShoppingListRepository shoppingListRepository;

    /**
     * 買い物リストアイテムのチェック状態を更新
     */
    public ShoppingListItem execute(String userId, String itemId, boolean isChecked) {
        ShoppingListItem item = shoppingListRepository.findById(userId, itemId)
                .orElseThrow(() -> new ShoppingListItemNotFoundException(
                        "Shopping list item not found: itemId=" + itemId));

        if (!item.canEdit(userId)) {
            throw new UnauthorizedException("You are not authorized to update this item");
        }

        ShoppingListItem updatedItem = isChecked ? item.markAsChecked() : item.uncheck();
        log.info("Updated shopping list item: userId={}, itemId={}, isChecked={}",
                userId, itemId, isChecked);
        return shoppingListRepository.save(updatedItem);
    }
}
