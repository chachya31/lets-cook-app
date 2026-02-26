package com.cookingapp.presentation.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cookingapp.application.usecase.inventory.InventoryUseCase;
import com.cookingapp.domain.entity.InventoryItem;
import com.cookingapp.infrastructure.security.SecurityUtils;

/**
 * 食材在庫コントローラー
 */
@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

        private static final Logger log = LoggerFactory.getLogger(InventoryController.class);

        private final InventoryUseCase inventoryUseCase;

        public InventoryController(InventoryUseCase inventoryUseCase) {
                this.inventoryUseCase = inventoryUseCase;
        }

        /**
         * 在庫一覧を取得
         */
        @GetMapping
        public ResponseEntity<List<InventoryItemResponse>> getInventory(
                        @RequestParam(value = "sortByExpiry", defaultValue = "false") boolean sortByExpiry) {
                String userId = SecurityUtils.getCurrentUserId();
                log.info("GET /api/inventory - userId={}, sortByExpiry={}", userId, sortByExpiry);

                List<InventoryItem> items = sortByExpiry
                                ? inventoryUseCase.getInventoryByExpiryDate(userId)
                                : inventoryUseCase.getInventory(userId);

                List<InventoryItemResponse> response = items.stream()
                                .map(InventoryItemResponse::from)
                                .toList();

                return ResponseEntity.ok(response);
        }

        /**
         * 在庫アイテムを追加
         */
        @PostMapping
        public ResponseEntity<InventoryItemResponse> addItem(@RequestBody AddInventoryItemRequest request) {
                String userId = SecurityUtils.getCurrentUserId();
                log.info("POST /api/inventory - userId={}, name={}", userId, request.name());

                InventoryItem item = inventoryUseCase.addItem(
                                userId,
                                request.name(),
                                request.quantity(),
                                request.unit(),
                                request.expiryDate());

                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(InventoryItemResponse.from(item));
        }

        /**
         * 在庫アイテムを更新
         */
        @PutMapping("/{itemId}")
        public ResponseEntity<InventoryItemResponse> updateItem(
                        @PathVariable String itemId,
                        @RequestBody UpdateInventoryItemRequest request) {
                String userId = SecurityUtils.getCurrentUserId();
                log.info("PUT /api/inventory/{} - userId={}", itemId, userId);

                InventoryItem item = inventoryUseCase.updateItem(
                                userId,
                                itemId,
                                request.quantity(),
                                request.expiryDate());

                return ResponseEntity.ok(InventoryItemResponse.from(item));
        }

        /**
         * 在庫アイテムを削除
         */
        @DeleteMapping("/{itemId}")
        public ResponseEntity<Void> deleteItem(@PathVariable String itemId) {
                String userId = SecurityUtils.getCurrentUserId();
                log.info("DELETE /api/inventory/{} - userId={}", itemId, userId);

                inventoryUseCase.deleteItem(userId, itemId);

                return ResponseEntity.noContent().build();
        }

        // Request/Response DTOs
        public record AddInventoryItemRequest(
                        String name,
                        BigDecimal quantity,
                        String unit,
                        LocalDate expiryDate) {
        }

        public record UpdateInventoryItemRequest(
                        BigDecimal quantity,
                        LocalDate expiryDate) {
        }

        public record InventoryItemResponse(
                        String itemId,
                        String name,
                        BigDecimal quantity,
                        String unit,
                        String expiryDate,
                        boolean isExpired,
                        boolean isExpiringSoon,
                        String purchasedAt,
                        String createdAt) {

                public static InventoryItemResponse from(InventoryItem item) {
                        return new InventoryItemResponse(
                                        item.getItemId(),
                                        item.getName(),
                                        item.getQuantity(),
                                        item.getUnit(),
                                        item.getExpiryDate() != null ? item.getExpiryDate().toString() : null,
                                        item.isExpired(),
                                        item.isExpiringSoon(),
                                        item.getPurchasedAt().toString(),
                                        item.getCreatedAt().toString());
                }
        }
}
