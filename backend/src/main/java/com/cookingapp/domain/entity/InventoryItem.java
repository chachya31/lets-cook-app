package com.cookingapp.domain.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 食材在庫エンティティ
 */
@Getter
@Builder
@AllArgsConstructor
public class InventoryItem {
    private final String itemId;
    private final String userId;
    private String name;
    private BigDecimal quantity;
    private String unit;
    private LocalDate expiryDate;
    private final Instant purchasedAt;
    private final Instant createdAt;

    /**
     * 新しい在庫アイテムを作成
     */
    public static InventoryItem create(
            String userId,
            String name,
            BigDecimal quantity,
            String unit,
            LocalDate expiryDate) {
        Instant now = Instant.now();
        return InventoryItem.builder()
                .itemId(UUID.randomUUID().toString())
                .userId(userId)
                .name(name)
                .quantity(quantity)
                .unit(unit)
                .expiryDate(expiryDate)
                .purchasedAt(now)
                .createdAt(now)
                .build();
    }

    /**
     * 買い物リストアイテムから在庫を作成
     */
    public static InventoryItem fromShoppingListItem(ShoppingListItem item) {
        Instant now = Instant.now();
        return InventoryItem.builder()
                .itemId(UUID.randomUUID().toString())
                .userId(item.getUserId())
                .name(item.getName())
                .quantity(item.getQuantity())
                .unit(item.getUnit())
                .expiryDate(null)
                .purchasedAt(now)
                .createdAt(now)
                .build();
    }

    /**
     * 数量を更新
     */
    public void updateQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    /**
     * 賞味期限を更新
     */
    public void updateExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    /**
     * 数量を加算
     */
    public void addQuantity(BigDecimal amount) {
        this.quantity = this.quantity.add(amount);
    }

    /**
     * 賞味期限切れかどうか
     */
    public boolean isExpired() {
        if (expiryDate == null) {
            return false;
        }
        return LocalDate.now().isAfter(expiryDate);
    }

    /**
     * 賞味期限が近いかどうか（3日以内）
     */
    public boolean isExpiringSoon() {
        if (expiryDate == null) {
            return false;
        }
        LocalDate today = LocalDate.now();
        return !today.isAfter(expiryDate) && !today.plusDays(3).isBefore(expiryDate);
    }
}
