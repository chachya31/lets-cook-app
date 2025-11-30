package com.cookingapp.presentation.controller;

import com.cookingapp.application.usecase.shoppinglist.*;
import com.cookingapp.domain.entity.ShoppingListItem;
import com.cookingapp.domain.valueobject.Unit;
import com.cookingapp.presentation.dto.request.AddShoppingListItemRequest;
import com.cookingapp.presentation.dto.request.UpdateShoppingListItemRequest;
import com.cookingapp.presentation.dto.response.ShoppingListItemResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 買い物リストコントローラー
 */
@Slf4j
@RestController
@RequestMapping("/api/shopping-lists")
@RequiredArgsConstructor
public class ShoppingListController {
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
            @RequestHeader("X-User-Id") String userId
    ) {
        log.info("GET /api/shopping-lists - userId={}", userId);
        
        // 期限切れアイテムをクリーンアップ
        cleanupExpiredItemsUseCase.execute(userId);
        
        List<ShoppingListItem> items = getShoppingListUseCase.execute(userId);
        List<ShoppingListItemResponse> response = items.stream()
                .map(ShoppingListItemResponse::from)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    /**
     * 買い物リストアイテム追加
     */
    @PostMapping
    public ResponseEntity<ShoppingListItemResponse> addItem(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody AddShoppingListItemRequest request
    ) {
        log.info("POST /api/shopping-lists - userId={}, name={}", userId, request.getName());
        
        Unit unit = Unit.fromCode(request.getUnit());
        ShoppingListItem item = addShoppingListItemUseCase.execute(
                userId,
                request.getName(),
                request.getQuantity(),
                unit,
                request.getSourceRecipeId()
        );
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ShoppingListItemResponse.from(item));
    }

    /**
     * 買い物リストアイテム更新（チェック状態）
     */
    @PutMapping("/{itemId}")
    public ResponseEntity<ShoppingListItemResponse> updateItem(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String itemId,
            @Valid @RequestBody UpdateShoppingListItemRequest request
    ) {
        log.info("PUT /api/shopping-lists/{} - userId={}, isChecked={}",
                itemId, userId, request.getIsChecked());
        
        ShoppingListItem item = updateShoppingListItemUseCase.execute(
                userId,
                itemId,
                request.getIsChecked()
        );
        
        return ResponseEntity.ok(ShoppingListItemResponse.from(item));
    }

    /**
     * 買い物リストアイテム削除
     */
    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> deleteItem(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String itemId
    ) {
        log.info("DELETE /api/shopping-lists/{} - userId={}", itemId, userId);
        
        deleteShoppingListItemUseCase.execute(userId, itemId);
        
        return ResponseEntity.noContent().build();
    }
}
