package com.cookingapp.application.usecase.shoppinglist;

import com.cookingapp.domain.repository.ShoppingListRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 期限切れアイテムクリーンアップユースケース
 */
@Service
@RequiredArgsConstructor
public class CleanupExpiredItemsUseCase {
    private static final Logger log = LoggerFactory.getLogger(CleanupExpiredItemsUseCase.class);
    private final ShoppingListRepository shoppingListRepository;

    /**
     * チェック済みで3日経過したアイテムを削除
     */
    public void execute(String userId) {
        log.info("Cleaning up expired items for userId={}", userId);
        shoppingListRepository.deleteExpiredCheckedItems(userId);
    }
}
