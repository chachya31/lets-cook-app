package com.cookingapp.domain.entity;

import com.cookingapp.domain.valueobject.Unit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * 買い物リストアイテムエンティティ
 */
@Getter
@Builder
@AllArgsConstructor
public class ShoppingListItem {
    private final String itemId;
    private final String userId;
    private final String name;
    private final BigDecimal quantity;
    private final Unit unit;
    private final boolean isChecked;
    private final Instant isCheckedAt;
    private final Instant addedAt;
    private final String sourceRecipeId;
    private final String normalizedKey;

    /**
     * 新しい買い物リストアイテムを作成
     */
    public static ShoppingListItem create(
            String userId,
            String name,
            BigDecimal quantity,
            Unit unit,
            String sourceRecipeId
    ) {
        String normalizedKey = generateNormalizedKey(name, unit);
        return ShoppingListItem.builder()
                .itemId(UUID.randomUUID().toString())
                .userId(userId)
                .name(name)
                .quantity(quantity)
                .unit(unit)
                .isChecked(false)
                .isCheckedAt(null)
                .addedAt(Instant.now())
                .sourceRecipeId(sourceRecipeId)
                .normalizedKey(normalizedKey)
                .build();
    }

    /**
     * 正規化キーを生成（名前と単位から）
     */
    public static String generateNormalizedKey(String name, Unit unit) {
        String normalizedName = name.trim().toLowerCase();
        return normalizedName + "#" + unit.getCode();
    }

    /**
     * 数量を更新（合算）
     */
    public ShoppingListItem updateQuantity(BigDecimal additionalQuantity) {
        return ShoppingListItem.builder()
                .itemId(this.itemId)
                .userId(this.userId)
                .name(this.name)
                .quantity(this.quantity.add(additionalQuantity))
                .unit(this.unit)
                .isChecked(this.isChecked)
                .isCheckedAt(this.isCheckedAt)
                .addedAt(this.addedAt)
                .sourceRecipeId(this.sourceRecipeId)
                .normalizedKey(this.normalizedKey)
                .build();
    }

    /**
     * チェック済みにする
     */
    public ShoppingListItem markAsChecked() {
        return ShoppingListItem.builder()
                .itemId(this.itemId)
                .userId(this.userId)
                .name(this.name)
                .quantity(this.quantity)
                .unit(this.unit)
                .isChecked(true)
                .isCheckedAt(Instant.now())
                .addedAt(this.addedAt)
                .sourceRecipeId(this.sourceRecipeId)
                .normalizedKey(this.normalizedKey)
                .build();
    }

    /**
     * チェックを外す
     */
    public ShoppingListItem uncheck() {
        return ShoppingListItem.builder()
                .itemId(this.itemId)
                .userId(this.userId)
                .name(this.name)
                .quantity(this.quantity)
                .unit(this.unit)
                .isChecked(false)
                .isCheckedAt(null)
                .addedAt(this.addedAt)
                .sourceRecipeId(this.sourceRecipeId)
                .normalizedKey(this.normalizedKey)
                .build();
    }

    /**
     * 自動削除すべきかどうかを判定（チェック済みから3日経過）
     */
    public boolean shouldAutoDelete() {
        if (!isChecked || isCheckedAt == null) {
            return false;
        }
        Instant threeDaysAgo = Instant.now().minus(3, ChronoUnit.DAYS);
        return isCheckedAt.isBefore(threeDaysAgo);
    }

    /**
     * 編集権限があるかどうかを確認
     */
    public boolean canEdit(String requestUserId) {
        return this.userId.equals(requestUserId);
    }
}
