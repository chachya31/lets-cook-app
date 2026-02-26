package com.cookingapp.unit.usecase.inventory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cookingapp.application.usecase.inventory.InventoryUseCase;
import com.cookingapp.domain.entity.InventoryItem;
import com.cookingapp.domain.entity.ShoppingListItem;
import com.cookingapp.domain.repository.InventoryRepository;

/**
 * InventoryUseCaseのユニットテスト
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("InventoryUseCase ユニットテスト")
class InventoryUseCaseTest {

    @Mock
    private InventoryRepository inventoryRepository;

    private InventoryUseCase inventoryUseCase;

    private static final String TEST_USER_ID = "user-123";
    private static final String TEST_ITEM_ID = "item-456";

    @BeforeEach
    void setUp() {
        inventoryUseCase = new InventoryUseCase(inventoryRepository);
    }

    /**
     * テスト用のInventoryItemを作成
     */
    private InventoryItem createTestInventoryItem(String itemId, String name, BigDecimal quantity,
            String unit, LocalDate expiryDate) {
        return InventoryItem.builder()
                .itemId(itemId)
                .userId(TEST_USER_ID)
                .name(name)
                .quantity(quantity)
                .unit(unit)
                .expiryDate(expiryDate)
                .purchasedAt(Instant.now())
                .createdAt(Instant.now())
                .build();
    }

    @Nested
    @DisplayName("getInventory - 在庫一覧取得")
    class GetInventory {

        @Test
        @DisplayName("ユーザーの在庫一覧を取得する")
        void shouldReturnInventoryList() {
            // Arrange
            List<InventoryItem> items = List.of(
                    createTestInventoryItem("item-1", "にんじん", new BigDecimal("3"), "本", LocalDate.now().plusDays(5)),
                    createTestInventoryItem("item-2", "牛乳", new BigDecimal("1"), "L", LocalDate.now().plusDays(2)));
            when(inventoryRepository.findByUserId(TEST_USER_ID)).thenReturn(items);

            // Act
            List<InventoryItem> result = inventoryUseCase.getInventory(TEST_USER_ID);

            // Assert
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getName()).isEqualTo("にんじん");
            assertThat(result.get(1).getName()).isEqualTo("牛乳");
            verify(inventoryRepository).findByUserId(TEST_USER_ID);
        }

        @Test
        @DisplayName("在庫がない場合は空のリストを返す")
        void shouldReturnEmptyListWhenNoInventory() {
            // Arrange
            when(inventoryRepository.findByUserId(TEST_USER_ID)).thenReturn(List.of());

            // Act
            List<InventoryItem> result = inventoryUseCase.getInventory(TEST_USER_ID);

            // Assert
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getInventoryByExpiryDate - 賞味期限順で在庫一覧取得")
    class GetInventoryByExpiryDate {

        @Test
        @DisplayName("賞味期限順でソートされた在庫一覧を取得する")
        void shouldReturnInventoryListSortedByExpiryDate() {
            // Arrange
            List<InventoryItem> items = List.of(
                    createTestInventoryItem("item-1", "牛乳", new BigDecimal("1"), "L", LocalDate.now().plusDays(2)),
                    createTestInventoryItem("item-2", "にんじん", new BigDecimal("3"), "本", LocalDate.now().plusDays(5)));
            when(inventoryRepository.findByUserIdOrderByExpiryDate(TEST_USER_ID)).thenReturn(items);

            // Act
            List<InventoryItem> result = inventoryUseCase.getInventoryByExpiryDate(TEST_USER_ID);

            // Assert
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getName()).isEqualTo("牛乳");
            verify(inventoryRepository).findByUserIdOrderByExpiryDate(TEST_USER_ID);
        }
    }

    @Nested
    @DisplayName("addItem - 在庫アイテム追加")
    class AddItem {

        @Nested
        @DisplayName("新規アイテム追加")
        class NewItem {

            @Test
            @DisplayName("新しい在庫アイテムを追加する")
            void shouldAddNewInventoryItem() {
                // Arrange
                LocalDate expiryDate = LocalDate.now().plusDays(7);
                when(inventoryRepository.findByUserIdAndNameAndUnit(TEST_USER_ID, "たまご", "個"))
                        .thenReturn(Optional.empty());
                when(inventoryRepository.save(any(InventoryItem.class)))
                        .thenAnswer(invocation -> invocation.getArgument(0));

                // Act
                InventoryItem result = inventoryUseCase.addItem(TEST_USER_ID, "たまご", new BigDecimal("10"), "個",
                        expiryDate);

                // Assert
                assertThat(result.getName()).isEqualTo("たまご");
                assertThat(result.getQuantity()).isEqualByComparingTo(new BigDecimal("10"));
                assertThat(result.getUnit()).isEqualTo("個");
                assertThat(result.getExpiryDate()).isEqualTo(expiryDate);

                ArgumentCaptor<InventoryItem> captor = ArgumentCaptor.forClass(InventoryItem.class);
                verify(inventoryRepository).save(captor.capture());
                assertThat(captor.getValue().getUserId()).isEqualTo(TEST_USER_ID);
            }

            @Test
            @DisplayName("賞味期限なしで新しい在庫アイテムを追加する")
            void shouldAddNewInventoryItemWithoutExpiryDate() {
                // Arrange
                when(inventoryRepository.findByUserIdAndNameAndUnit(TEST_USER_ID, "塩", "袋"))
                        .thenReturn(Optional.empty());
                when(inventoryRepository.save(any(InventoryItem.class)))
                        .thenAnswer(invocation -> invocation.getArgument(0));

                // Act
                InventoryItem result = inventoryUseCase.addItem(TEST_USER_ID, "塩", new BigDecimal("1"), "袋", null);

                // Assert
                assertThat(result.getName()).isEqualTo("塩");
                assertThat(result.getExpiryDate()).isNull();
            }
        }

        @Nested
        @DisplayName("既存アイテムへの追加")
        class ExistingItem {

            @Test
            @DisplayName("同じ名前と単位の在庫がある場合は数量を合算する")
            void shouldAddQuantityToExistingItem() {
                // Arrange
                InventoryItem existingItem = createTestInventoryItem(TEST_ITEM_ID, "たまご", new BigDecimal("5"), "個",
                        LocalDate.now().plusDays(3));
                when(inventoryRepository.findByUserIdAndNameAndUnit(TEST_USER_ID, "たまご", "個"))
                        .thenReturn(Optional.of(existingItem));
                when(inventoryRepository.save(any(InventoryItem.class)))
                        .thenAnswer(invocation -> invocation.getArgument(0));

                // Act
                InventoryItem result = inventoryUseCase.addItem(TEST_USER_ID, "たまご", new BigDecimal("10"), "個",
                        LocalDate.now().plusDays(7));

                // Assert
                assertThat(result.getQuantity()).isEqualByComparingTo(new BigDecimal("15")); // 5 + 10
                verify(inventoryRepository).save(existingItem);
            }

            @Test
            @DisplayName("新しい賞味期限が指定された場合は賞味期限も更新する")
            void shouldUpdateExpiryDateWhenProvided() {
                // Arrange
                LocalDate oldExpiryDate = LocalDate.now().plusDays(3);
                LocalDate newExpiryDate = LocalDate.now().plusDays(10);
                InventoryItem existingItem = createTestInventoryItem(TEST_ITEM_ID, "たまご", new BigDecimal("5"), "個",
                        oldExpiryDate);
                when(inventoryRepository.findByUserIdAndNameAndUnit(TEST_USER_ID, "たまご", "個"))
                        .thenReturn(Optional.of(existingItem));
                when(inventoryRepository.save(any(InventoryItem.class)))
                        .thenAnswer(invocation -> invocation.getArgument(0));

                // Act
                InventoryItem result = inventoryUseCase.addItem(TEST_USER_ID, "たまご", new BigDecimal("10"), "個",
                        newExpiryDate);

                // Assert
                assertThat(result.getExpiryDate()).isEqualTo(newExpiryDate);
            }

            @Test
            @DisplayName("新しい賞味期限がnullの場合は既存の賞味期限を維持する")
            void shouldKeepExistingExpiryDateWhenNewIsNull() {
                // Arrange
                LocalDate existingExpiryDate = LocalDate.now().plusDays(3);
                InventoryItem existingItem = createTestInventoryItem(TEST_ITEM_ID, "たまご", new BigDecimal("5"), "個",
                        existingExpiryDate);
                when(inventoryRepository.findByUserIdAndNameAndUnit(TEST_USER_ID, "たまご", "個"))
                        .thenReturn(Optional.of(existingItem));
                when(inventoryRepository.save(any(InventoryItem.class)))
                        .thenAnswer(invocation -> invocation.getArgument(0));

                // Act
                InventoryItem result = inventoryUseCase.addItem(TEST_USER_ID, "たまご", new BigDecimal("10"), "個", null);

                // Assert
                assertThat(result.getExpiryDate()).isEqualTo(existingExpiryDate);
            }
        }
    }

    @Nested
    @DisplayName("addFromShoppingList - 買い物リストから在庫追加")
    class AddFromShoppingList {

        private ShoppingListItem createTestShoppingListItem(String name, BigDecimal quantity, String unit) {
            return ShoppingListItem.builder()
                    .itemId("shopping-item-1")
                    .userId(TEST_USER_ID)
                    .name(name)
                    .quantity(quantity)
                    .unit(unit)
                    .isChecked(true)
                    .isCheckedAt(Instant.now())
                    .addedAt(Instant.now())
                    .sourceRecipeId(null)
                    .normalizedKey(name.toLowerCase() + "#" + (unit != null ? unit.toLowerCase() : ""))
                    .build();
        }

        @Test
        @DisplayName("買い物リストアイテムから新しい在庫を作成する")
        void shouldCreateNewInventoryFromShoppingList() {
            // Arrange
            ShoppingListItem shoppingItem = createTestShoppingListItem("りんご", new BigDecimal("3"), "個");
            when(inventoryRepository.findByUserIdAndNameAndUnit(TEST_USER_ID, "りんご", "個"))
                    .thenReturn(Optional.empty());
            when(inventoryRepository.save(any(InventoryItem.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            InventoryItem result = inventoryUseCase.addFromShoppingList(shoppingItem);

            // Assert
            assertThat(result.getName()).isEqualTo("りんご");
            assertThat(result.getQuantity()).isEqualByComparingTo(new BigDecimal("3"));
            assertThat(result.getUnit()).isEqualTo("個");
            assertThat(result.getExpiryDate()).isNull();
            verify(inventoryRepository).save(any(InventoryItem.class));
        }

        @Test
        @DisplayName("同じ名前と単位の在庫がある場合は数量を合算する")
        void shouldAddQuantityToExistingInventory() {
            // Arrange
            ShoppingListItem shoppingItem = createTestShoppingListItem("りんご", new BigDecimal("3"), "個");
            InventoryItem existingItem = createTestInventoryItem(TEST_ITEM_ID, "りんご", new BigDecimal("2"), "個",
                    LocalDate.now().plusDays(5));
            when(inventoryRepository.findByUserIdAndNameAndUnit(TEST_USER_ID, "りんご", "個"))
                    .thenReturn(Optional.of(existingItem));
            when(inventoryRepository.save(any(InventoryItem.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            InventoryItem result = inventoryUseCase.addFromShoppingList(shoppingItem);

            // Assert
            assertThat(result.getQuantity()).isEqualByComparingTo(new BigDecimal("5")); // 2 + 3
            verify(inventoryRepository).save(existingItem);
        }
    }

    @Nested
    @DisplayName("updateItem - 在庫アイテム更新")
    class UpdateItem {

        @Test
        @DisplayName("在庫アイテムの数量を更新する")
        void shouldUpdateQuantity() {
            // Arrange
            InventoryItem existingItem = createTestInventoryItem(TEST_ITEM_ID, "たまご", new BigDecimal("10"), "個",
                    LocalDate.now().plusDays(5));
            when(inventoryRepository.findById(TEST_USER_ID, TEST_ITEM_ID)).thenReturn(Optional.of(existingItem));
            when(inventoryRepository.save(any(InventoryItem.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            InventoryItem result = inventoryUseCase.updateItem(TEST_USER_ID, TEST_ITEM_ID, new BigDecimal("5"), null);

            // Assert
            assertThat(result.getQuantity()).isEqualByComparingTo(new BigDecimal("5"));
            verify(inventoryRepository).save(existingItem);
        }

        @Test
        @DisplayName("在庫アイテムの賞味期限を更新する")
        void shouldUpdateExpiryDate() {
            // Arrange
            LocalDate newExpiryDate = LocalDate.now().plusDays(14);
            InventoryItem existingItem = createTestInventoryItem(TEST_ITEM_ID, "たまご", new BigDecimal("10"), "個",
                    LocalDate.now().plusDays(5));
            when(inventoryRepository.findById(TEST_USER_ID, TEST_ITEM_ID)).thenReturn(Optional.of(existingItem));
            when(inventoryRepository.save(any(InventoryItem.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            InventoryItem result = inventoryUseCase.updateItem(TEST_USER_ID, TEST_ITEM_ID, null, newExpiryDate);

            // Assert
            assertThat(result.getExpiryDate()).isEqualTo(newExpiryDate);
        }

        @Test
        @DisplayName("在庫アイテムの数量と賞味期限を同時に更新する")
        void shouldUpdateQuantityAndExpiryDate() {
            // Arrange
            LocalDate newExpiryDate = LocalDate.now().plusDays(14);
            InventoryItem existingItem = createTestInventoryItem(TEST_ITEM_ID, "たまご", new BigDecimal("10"), "個",
                    LocalDate.now().plusDays(5));
            when(inventoryRepository.findById(TEST_USER_ID, TEST_ITEM_ID)).thenReturn(Optional.of(existingItem));
            when(inventoryRepository.save(any(InventoryItem.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            InventoryItem result = inventoryUseCase.updateItem(TEST_USER_ID, TEST_ITEM_ID, new BigDecimal("3"),
                    newExpiryDate);

            // Assert
            assertThat(result.getQuantity()).isEqualByComparingTo(new BigDecimal("3"));
            assertThat(result.getExpiryDate()).isEqualTo(newExpiryDate);
        }

        @Test
        @DisplayName("存在しない在庫アイテムの場合は例外をスローする")
        void shouldThrowExceptionWhenItemNotFound() {
            // Arrange
            when(inventoryRepository.findById(TEST_USER_ID, "non-existent")).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> inventoryUseCase.updateItem(TEST_USER_ID, "non-existent", new BigDecimal("5"), null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Inventory item not found");

            verify(inventoryRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deleteItem - 在庫アイテム削除")
    class DeleteItem {

        @Test
        @DisplayName("在庫アイテムを削除する")
        void shouldDeleteInventoryItem() {
            // Arrange
            InventoryItem existingItem = createTestInventoryItem(TEST_ITEM_ID, "たまご", new BigDecimal("10"), "個",
                    LocalDate.now().plusDays(5));
            when(inventoryRepository.findById(TEST_USER_ID, TEST_ITEM_ID)).thenReturn(Optional.of(existingItem));

            // Act
            inventoryUseCase.deleteItem(TEST_USER_ID, TEST_ITEM_ID);

            // Assert
            verify(inventoryRepository).delete(TEST_USER_ID, TEST_ITEM_ID);
        }

        @Test
        @DisplayName("存在しない在庫アイテムの場合は例外をスローする")
        void shouldThrowExceptionWhenItemNotFound() {
            // Arrange
            when(inventoryRepository.findById(TEST_USER_ID, "non-existent")).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> inventoryUseCase.deleteItem(TEST_USER_ID, "non-existent"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Inventory item not found");

            verify(inventoryRepository, never()).delete(any(), any());
        }
    }
}
