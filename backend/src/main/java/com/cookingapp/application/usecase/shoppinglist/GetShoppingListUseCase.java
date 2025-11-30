package com.cookingapp.application.usecase.shoppinglist;

import com.cookingapp.domain.entity.ShoppingListItem;
import com.cookingapp.domain.repository.ShoppingListRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 買い物リスト取得ユースケース
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GetShoppingListUseCase {
    private final ShoppingListRepository shoppingListRepository;

    /**
     * ユーザーの買い物リストを取得
     */
    public List<ShoppingListItem> execute(String userId) {
        log.info("Getting shopping list for userId={}", userId);
        return shoppingListRepository.findByUserId(userId);
    }
}
