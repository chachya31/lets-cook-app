package com.cookingapp.application.usecase.shoppinglist;

import com.cookingapp.domain.entity.ShoppingListItem;
import com.cookingapp.domain.exception.ShoppingListItemNotFoundException;
import com.cookingapp.domain.exception.UnauthorizedException;
import com.cookingapp.domain.repository.ShoppingListRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 買い物リストアイテム削除ユースケース
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeleteShoppingListItemUseCase {
    private final ShoppingListRepository shoppingListRepository;

    /**
     * 買い物リストアイテムを削除
     */
    public void execute(String userId, String itemId) {
        ShoppingListItem item = shoppingListRepository.findById(userId, itemId)
                .orElseThrow(() -> new ShoppingListItemNotFoundException(
                        "Shopping list item not found: itemId=" + itemId));

        if (!item.canEdit(userId)) {
            throw new UnauthorizedException("You are not authorized to delete this item");
        }

        shoppingListRepository.delete(userId, itemId);
        log.info("Deleted shopping list item: userId={}, itemId={}", userId, itemId);
    }
}
