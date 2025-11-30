package com.cookingapp.application.usecase.shoppinglist;

import com.cookingapp.domain.repository.ShoppingListRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 期限切れアイテムクリーンアップユースケース
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CleanupExpiredItemsUseCase {
    private final ShoppingListRepository shoppingListRepository;

    /**
     * チェック済みで3日経過したアイテムを削除
     */
    public void execute(String userId) {
        log.info("Cleaning up expired items for userId={}", userId);
        shoppingListRepository.deleteExpiredCheckedItems(userId);
    }
}
