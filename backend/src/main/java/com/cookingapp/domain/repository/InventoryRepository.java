package com.cookingapp.domain.repository;

import java.util.List;
import java.util.Optional;

import com.cookingapp.domain.entity.InventoryItem;

/**
 * 食材在庫リポジトリインターフェース
 */
public interface InventoryRepository {

    /**
     * 在庫アイテムを保存
     */
    InventoryItem save(InventoryItem item);

    /**
     * ユーザーの在庫一覧を取得
     */
    List<InventoryItem> findByUserId(String userId);

    /**
     * ユーザーの在庫を賞味期限順で取得
     */
    List<InventoryItem> findByUserIdOrderByExpiryDate(String userId);

    /**
     * 特定の在庫アイテムを取得
     */
    Optional<InventoryItem> findById(String userId, String itemId);

    /**
     * 在庫アイテムを削除
     */
    void delete(String userId, String itemId);

    /**
     * 同じ名前と単位の在庫を検索
     */
    Optional<InventoryItem> findByUserIdAndNameAndUnit(String userId, String name, String unit);
}
