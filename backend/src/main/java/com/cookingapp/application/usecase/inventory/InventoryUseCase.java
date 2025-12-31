package com.cookingapp.application.usecase.inventory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.cookingapp.domain.entity.InventoryItem;
import com.cookingapp.domain.entity.ShoppingListItem;
import com.cookingapp.domain.repository.InventoryRepository;

/**
 * 食材在庫ユースケース
 */
@Service
public class InventoryUseCase {

    private static final Logger log = LoggerFactory.getLogger(InventoryUseCase.class);

    private final InventoryRepository inventoryRepository;

    public InventoryUseCase(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    /**
     * 在庫一覧を取得
     */
    public List<InventoryItem> getInventory(String userId) {
        return inventoryRepository.findByUserId(userId);
    }

    /**
     * 在庫一覧を賞味期限順で取得
     */
    public List<InventoryItem> getInventoryByExpiryDate(String userId) {
        return inventoryRepository.findByUserIdOrderByExpiryDate(userId);
    }

    /**
     * 在庫アイテムを追加
     */
    public InventoryItem addItem(String userId, String name, BigDecimal quantity, String unit, LocalDate expiryDate) {
        // 同じ名前と単位の在庫があれば数量を合算
        Optional<InventoryItem> existing = inventoryRepository.findByUserIdAndNameAndUnit(userId, name, unit);

        if (existing.isPresent()) {
            InventoryItem item = existing.get();
            item.addQuantity(quantity);
            if (expiryDate != null) {
                item.updateExpiryDate(expiryDate);
            }
            inventoryRepository.save(item);
            log.info("Updated existing inventory item: userId={}, name={}, newQuantity={}", userId, name,
                    item.getQuantity());
            return item;
        }

        InventoryItem newItem = InventoryItem.create(userId, name, quantity, unit, expiryDate);
        inventoryRepository.save(newItem);
        log.info("Added new inventory item: userId={}, name={}", userId, name);
        return newItem;
    }

    /**
     * 買い物リストアイテムから在庫に追加
     */
    public InventoryItem addFromShoppingList(ShoppingListItem shoppingItem) {
        // 同じ名前と単位の在庫があれば数量を合算
        Optional<InventoryItem> existing = inventoryRepository.findByUserIdAndNameAndUnit(
                shoppingItem.getUserId(),
                shoppingItem.getName(),
                shoppingItem.getUnit());

        if (existing.isPresent()) {
            InventoryItem item = existing.get();
            item.addQuantity(shoppingItem.getQuantity());
            inventoryRepository.save(item);
            log.info("Added quantity to existing inventory: userId={}, name={}, addedQuantity={}",
                    shoppingItem.getUserId(), shoppingItem.getName(), shoppingItem.getQuantity());
            return item;
        }

        InventoryItem newItem = InventoryItem.fromShoppingListItem(shoppingItem);
        inventoryRepository.save(newItem);
        log.info("Created inventory from shopping list: userId={}, name={}",
                shoppingItem.getUserId(), shoppingItem.getName());
        return newItem;
    }

    /**
     * 在庫アイテムを更新
     */
    public InventoryItem updateItem(String userId, String itemId, BigDecimal quantity, LocalDate expiryDate) {
        InventoryItem item = inventoryRepository.findById(userId, itemId)
                .orElseThrow(() -> new IllegalArgumentException("Inventory item not found"));

        if (quantity != null) {
            item.updateQuantity(quantity);
        }
        if (expiryDate != null) {
            item.updateExpiryDate(expiryDate);
        }

        inventoryRepository.save(item);
        log.info("Updated inventory item: userId={}, itemId={}", userId, itemId);
        return item;
    }

    /**
     * 在庫アイテムを削除
     */
    public void deleteItem(String userId, String itemId) {
        inventoryRepository.findById(userId, itemId)
                .orElseThrow(() -> new IllegalArgumentException("Inventory item not found"));

        inventoryRepository.delete(userId, itemId);
        log.info("Deleted inventory item: userId={}, itemId={}", userId, itemId);
    }
}
