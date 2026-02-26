package com.cookingapp.domain.repository;

import com.cookingapp.domain.entity.ShoppingListItem;

import java.util.List;
import java.util.Optional;

/**
 * 買い物リストリポジトリインターフェース
 */
public interface ShoppingListRepository {
    /**
     * アイテムを保存
     */
    ShoppingListItem save(ShoppingListItem item);

    /**
     * アイテムをIDで取得
     */
    Optional<ShoppingListItem> findById(String userId, String itemId);

    /**
     * ユーザーIDで全アイテムを取得
     */
    List<ShoppingListItem> findByUserId(String userId);

    /**
     * 正規化キーでアイテムを検索
     */
    Optional<ShoppingListItem> findByNormalizedKey(String userId, String normalizedKey);

    /**
     * アイテムを削除
     */
    void delete(String userId, String itemId);

    /**
     * アイテムが存在するかチェック
     */
    boolean existsById(String userId, String itemId);

    /**
     * チェック済みで3日経過したアイテムを削除
     */
    void deleteExpiredCheckedItems(String userId);
}
