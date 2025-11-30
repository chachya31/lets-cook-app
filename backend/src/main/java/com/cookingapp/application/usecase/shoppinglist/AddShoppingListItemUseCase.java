package com.cookingapp.application.usecase.shoppinglist;

import com.cookingapp.domain.entity.ShoppingListItem;
import com.cookingapp.domain.repository.ShoppingListRepository;
import com.cookingapp.domain.valueobject.Unit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * 買い物リストアイテム追加ユースケース
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AddShoppingListItemUseCase {
    private final ShoppingListRepository shoppingListRepository;

    /**
     * 買い物リストにアイテムを追加
     * 同じ正規化キー（名前+単位）のアイテムが存在する場合は数量を合算
     */
    public ShoppingListItem execute(
            String userId,
            String name,
            BigDecimal quantity,
            Unit unit,
            String sourceRecipeId
    ) {
        String normalizedKey = ShoppingListItem.generateNormalizedKey(name, unit);
        
        // 既存のアイテムを検索
        Optional<ShoppingListItem> existingItem = shoppingListRepository.findByNormalizedKey(userId, normalizedKey);
        
        if (existingItem.isPresent()) {
            // 既存アイテムがある場合は数量を合算
            ShoppingListItem updatedItem = existingItem.get().updateQuantity(quantity);
            log.info("Updating existing shopping list item: userId={}, itemId={}, newQuantity={}",
                    userId, updatedItem.getItemId(), updatedItem.getQuantity());
            return shoppingListRepository.save(updatedItem);
        } else {
            // 新規アイテムを作成
            ShoppingListItem newItem = ShoppingListItem.create(userId, name, quantity, unit, sourceRecipeId);
            log.info("Creating new shopping list item: userId={}, itemId={}", userId, newItem.getItemId());
            return shoppingListRepository.save(newItem);
        }
    }
}
