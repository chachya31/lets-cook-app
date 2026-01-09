package com.cookingapp.presentation.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cookingapp.application.usecase.shoppinglist.AddShoppingListItemUseCase;
import com.cookingapp.application.usecase.shoppinglist.CleanupExpiredItemsUseCase;
import com.cookingapp.application.usecase.shoppinglist.DeleteShoppingListItemUseCase;
import com.cookingapp.application.usecase.shoppinglist.GetShoppingListUseCase;
import com.cookingapp.application.usecase.shoppinglist.UpdateShoppingListItemUseCase;
import com.cookingapp.domain.entity.ShoppingListItem;
import com.cookingapp.presentation.dto.request.AddShoppingListItemRequest;
import com.cookingapp.presentation.dto.request.UpdateShoppingListItemRequest;
import com.cookingapp.presentation.dto.response.ShoppingListItemResponse;
import com.cookingapp.presentation.mapper.ShoppingListMapper;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 買い物リストコントローラー
 */
@RestController
@RequestMapping("/api/shopping-lists")
@RequiredArgsConstructor
public class ShoppingListController {
    private static final Logger log = LoggerFactory.getLogger(ShoppingListController.class);
    private final AddShoppingListItemUseCase addShoppingListItemUseCase;
    private final UpdateShoppingListItemUseCase updateShoppingListItemUseCase;
    private final DeleteShoppingListItemUseCase deleteShoppingListItemUseCase;
    private final GetShoppingListUseCase getShoppingListUseCase;
    private final CleanupExpiredItemsUseCase cleanupExpiredItemsUseCase;

    /**
     * 買い物リスト取得
     */
    @GetMapping
    public ResponseEntity<List<ShoppingListItemResponse>> getShoppingList(
            @RequestHeader("X-User-Id") String userId) {
        log.info("GET /api/shopping-lists - userId={}", userId);

        // 期限切れアイテムをクリーンアップ
        cleanupExpiredItemsUseCase.execute(userId);

        List<ShoppingListItem> items = getShoppingListUseCase.execute(userId);

        return ResponseEntity.ok(ShoppingListMapper.toResponseList(items));
    }

    /**
     * 買い物リストアイテム追加
     */
    @PostMapping
    public ResponseEntity<ShoppingListItemResponse> addItem(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody AddShoppingListItemRequest request) {
        log.info("POST /api/shopping-lists - userId={}, name={}", userId, request.getName());

        ShoppingListItem item = addShoppingListItemUseCase.execute(
                userId,
                request.getName(),
                request.getQuantity(),
                request.getUnit(),
                request.getSourceRecipeId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ShoppingListMapper.toResponse(item));
    }

    /**
     * 買い物リストアイテム更新（チェック状態）
     */
    @PutMapping("/{itemId}")
    public ResponseEntity<ShoppingListItemResponse> updateItem(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable("itemId") String itemId,
            @Valid @RequestBody UpdateShoppingListItemRequest request) {
        log.info("PUT /api/shopping-lists/{} - userId={}, isChecked={}",
                itemId, userId, request.getIsChecked());

        ShoppingListItem item = updateShoppingListItemUseCase.execute(
                userId,
                itemId,
                request.getIsChecked());

        return ResponseEntity.ok(ShoppingListMapper.toResponse(item));
    }

    /**
     * 買い物リストアイテム削除
     */
    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> deleteItem(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable("itemId") String itemId) {
        log.info("DELETE /api/shopping-lists/{} - userId={}", itemId, userId);

        deleteShoppingListItemUseCase.execute(userId, itemId);

        return ResponseEntity.noContent().build();
    }
}
