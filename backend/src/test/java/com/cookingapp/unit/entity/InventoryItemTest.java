package com.cookingapp.unit.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cookingapp.domain.entity.InventoryItem;
import com.cookingapp.domain.entity.ShoppingListItem;

/**
 * InventoryItemエンティティのユニットテスト
 */
@DisplayName("InventoryItem ユニットテスト")
class InventoryItemTest {

    private static final String TEST_USER_ID = "user-123";

    @Nested
    @DisplayName("create - 新しい在庫アイテム作成")
    class Create {

        @Test
        @DisplayName("すべてのフィールドを持つ在庫アイテムを作成する")
        void shouldCreateInventoryItemWithAllFields() {
            // Arrange
            String name = "たまご";
            BigDecimal quantity = new BigDecimal("10");
            String unit = "個";
            LocalDate expiryDate = LocalDate.now().plusDays(7);

            // Act
            InventoryItem item = InventoryItem.create(TEST_USER_ID, name, quantity, unit, expiryDate);

            // Assert
            assertThat(item.getItemId()).isNotNull().isNotEmpty();
            assertThat(item.getUserId()).isEqualTo(TEST_USER_ID);
            assertThat(item.getName()).isEqualTo(name);
            assertThat(item.getQuantity()).isEqualByComparingTo(quantity);
            assertThat(item.getUnit()).isEqualTo(unit);
            assertThat(item.getExpiryDate()).isEqualTo(expiryDate);
            assertThat(item.getPurchasedAt()).isNotNull();
            assertThat(item.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("賞味期限なしで在庫アイテムを作成する")
        void shouldCreateInventoryItemWithoutExpiryDate() {
            // Act
            InventoryItem item = InventoryItem.create(TEST_USER_ID, "塩", new BigDecimal("1"), "袋", null);

            // Assert
            assertThat(item.getExpiryDate()).isNull();
        }

        @Test
        @DisplayName("UUIDが一意に生成される")
        void shouldGenerateUniqueItemIds() {
            // Act
            InventoryItem item1 = InventoryItem.create(TEST_USER_ID, "たまご", new BigDecimal("10"), "個", null);
            InventoryItem item2 = InventoryItem.create(TEST_USER_ID, "たまご", new BigDecimal("10"), "個", null);

            // Assert
            assertThat(item1.getItemId()).isNotEqualTo(item2.getItemId());
        }
    }

    @Nested
    @DisplayName("fromShoppingListItem - 買い物リストアイテムから在庫作成")
    class FromShoppingListItem {

        @Test
        @DisplayName("買い物リストアイテムから在庫アイテムを作成する")
        void shouldCreateInventoryFromShoppingListItem() {
            // Arrange
            ShoppingListItem shoppingItem = ShoppingListItem.builder()
                    .itemId("shopping-item-1")
                    .userId(TEST_USER_ID)
                    .name("りんご")
                    .quantity(new BigDecimal("3"))
                    .unit("個")
                    .isChecked(true)
                    .isCheckedAt(Instant.now())
                    .addedAt(Instant.now())
                    .sourceRecipeId(null)
                    .normalizedKey("りんご#個")
                    .build();

            // Act
            InventoryItem item = InventoryItem.fromShoppingListItem(shoppingItem);

            // Assert
            assertThat(item.getItemId()).isNotNull().isNotEmpty();
            assertThat(item.getUserId()).isEqualTo(TEST_USER_ID);
            assertThat(item.getName()).isEqualTo("りんご");
            assertThat(item.getQuantity()).isEqualByComparingTo(new BigDecimal("3"));
            assertThat(item.getUnit()).isEqualTo("個");
            assertThat(item.getExpiryDate()).isNull(); // 買い物リストからは賞味期限なし
            assertThat(item.getPurchasedAt()).isNotNull();
            assertThat(item.getCreatedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("updateQuantity - 数量更新")
    class UpdateQuantity {

        @Test
        @DisplayName("数量を更新する")
        void shouldUpdateQuantity() {
            // Arrange
            InventoryItem item = InventoryItem.create(TEST_USER_ID, "たまご", new BigDecimal("10"), "個", null);

            // Act
            item.updateQuantity(new BigDecimal("5"));

            // Assert
            assertThat(item.getQuantity()).isEqualByComparingTo(new BigDecimal("5"));
        }

        @Test
        @DisplayName("数量を0に更新できる")
        void shouldUpdateQuantityToZero() {
            // Arrange
            InventoryItem item = InventoryItem.create(TEST_USER_ID, "たまご", new BigDecimal("10"), "個", null);

            // Act
            item.updateQuantity(BigDecimal.ZERO);

            // Assert
            assertThat(item.getQuantity()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("updateExpiryDate - 賞味期限更新")
    class UpdateExpiryDate {

        @Test
        @DisplayName("賞味期限を更新する")
        void shouldUpdateExpiryDate() {
            // Arrange
            LocalDate oldDate = LocalDate.now().plusDays(3);
            LocalDate newDate = LocalDate.now().plusDays(10);
            InventoryItem item = InventoryItem.create(TEST_USER_ID, "たまご", new BigDecimal("10"), "個", oldDate);

            // Act
            item.updateExpiryDate(newDate);

            // Assert
            assertThat(item.getExpiryDate()).isEqualTo(newDate);
        }

        @Test
        @DisplayName("賞味期限をnullに更新できる")
        void shouldUpdateExpiryDateToNull() {
            // Arrange
            InventoryItem item = InventoryItem.create(TEST_USER_ID, "たまご", new BigDecimal("10"), "個",
                    LocalDate.now().plusDays(3));

            // Act
            item.updateExpiryDate(null);

            // Assert
            assertThat(item.getExpiryDate()).isNull();
        }
    }

    @Nested
    @DisplayName("addQuantity - 数量加算")
    class AddQuantity {

        @Test
        @DisplayName("数量を加算する")
        void shouldAddQuantity() {
            // Arrange
            InventoryItem item = InventoryItem.create(TEST_USER_ID, "たまご", new BigDecimal("10"), "個", null);

            // Act
            item.addQuantity(new BigDecimal("5"));

            // Assert
            assertThat(item.getQuantity()).isEqualByComparingTo(new BigDecimal("15"));
        }

        @Test
        @DisplayName("小数の数量を加算する")
        void shouldAddDecimalQuantity() {
            // Arrange
            InventoryItem item = InventoryItem.create(TEST_USER_ID, "牛乳", new BigDecimal("1.5"), "L", null);

            // Act
            item.addQuantity(new BigDecimal("0.5"));

            // Assert
            assertThat(item.getQuantity()).isEqualByComparingTo(new BigDecimal("2.0"));
        }
    }

    @Nested
    @DisplayName("isExpired - 賞味期限切れ判定")
    class IsExpired {

        @Test
        @DisplayName("賞味期限が過ぎている場合はtrueを返す")
        void shouldReturnTrueWhenExpired() {
            // Arrange
            InventoryItem item = InventoryItem.builder()
                    .itemId("item-1")
                    .userId(TEST_USER_ID)
                    .name("牛乳")
                    .quantity(new BigDecimal("1"))
                    .unit("L")
                    .expiryDate(LocalDate.now().minusDays(1)) // 昨日
                    .purchasedAt(Instant.now())
                    .createdAt(Instant.now())
                    .build();

            // Act & Assert
            assertThat(item.isExpired()).isTrue();
        }

        @Test
        @DisplayName("賞味期限が今日の場合はfalseを返す")
        void shouldReturnFalseWhenExpiryDateIsToday() {
            // Arrange
            InventoryItem item = InventoryItem.builder()
                    .itemId("item-1")
                    .userId(TEST_USER_ID)
                    .name("牛乳")
                    .quantity(new BigDecimal("1"))
                    .unit("L")
                    .expiryDate(LocalDate.now()) // 今日
                    .purchasedAt(Instant.now())
                    .createdAt(Instant.now())
                    .build();

            // Act & Assert
            assertThat(item.isExpired()).isFalse();
        }

        @Test
        @DisplayName("賞味期限が未来の場合はfalseを返す")
        void shouldReturnFalseWhenNotExpired() {
            // Arrange
            InventoryItem item = InventoryItem.create(TEST_USER_ID, "牛乳", new BigDecimal("1"), "L",
                    LocalDate.now().plusDays(5));

            // Act & Assert
            assertThat(item.isExpired()).isFalse();
        }

        @Test
        @DisplayName("賞味期限がnullの場合はfalseを返す")
        void shouldReturnFalseWhenExpiryDateIsNull() {
            // Arrange
            InventoryItem item = InventoryItem.create(TEST_USER_ID, "塩", new BigDecimal("1"), "袋", null);

            // Act & Assert
            assertThat(item.isExpired()).isFalse();
        }
    }

    @Nested
    @DisplayName("isExpiringSoon - 賞味期限間近判定（3日以内）")
    class IsExpiringSoon {

        @Test
        @DisplayName("賞味期限が今日の場合はtrueを返す")
        void shouldReturnTrueWhenExpiryDateIsToday() {
            // Arrange
            InventoryItem item = InventoryItem.builder()
                    .itemId("item-1")
                    .userId(TEST_USER_ID)
                    .name("牛乳")
                    .quantity(new BigDecimal("1"))
                    .unit("L")
                    .expiryDate(LocalDate.now())
                    .purchasedAt(Instant.now())
                    .createdAt(Instant.now())
                    .build();

            // Act & Assert
            assertThat(item.isExpiringSoon()).isTrue();
        }

        @Test
        @DisplayName("賞味期限が2日後の場合はtrueを返す")
        void shouldReturnTrueWhenExpiryDateIs2DaysAway() {
            // Arrange
            InventoryItem item = InventoryItem.builder()
                    .itemId("item-1")
                    .userId(TEST_USER_ID)
                    .name("牛乳")
                    .quantity(new BigDecimal("1"))
                    .unit("L")
                    .expiryDate(LocalDate.now().plusDays(2))
                    .purchasedAt(Instant.now())
                    .createdAt(Instant.now())
                    .build();

            // Act & Assert
            assertThat(item.isExpiringSoon()).isTrue();
        }

        @Test
        @DisplayName("賞味期限が3日後の場合はtrueを返す")
        void shouldReturnTrueWhenExpiryDateIs3DaysAway() {
            // Arrange
            InventoryItem item = InventoryItem.builder()
                    .itemId("item-1")
                    .userId(TEST_USER_ID)
                    .name("牛乳")
                    .quantity(new BigDecimal("1"))
                    .unit("L")
                    .expiryDate(LocalDate.now().plusDays(3))
                    .purchasedAt(Instant.now())
                    .createdAt(Instant.now())
                    .build();

            // Act & Assert
            assertThat(item.isExpiringSoon()).isTrue();
        }

        @Test
        @DisplayName("賞味期限が4日後の場合はfalseを返す")
        void shouldReturnFalseWhenExpiryDateIs4DaysAway() {
            // Arrange
            InventoryItem item = InventoryItem.builder()
                    .itemId("item-1")
                    .userId(TEST_USER_ID)
                    .name("牛乳")
                    .quantity(new BigDecimal("1"))
                    .unit("L")
                    .expiryDate(LocalDate.now().plusDays(4))
                    .purchasedAt(Instant.now())
                    .createdAt(Instant.now())
                    .build();

            // Act & Assert
            assertThat(item.isExpiringSoon()).isFalse();
        }

        @Test
        @DisplayName("賞味期限が過ぎている場合はfalseを返す")
        void shouldReturnFalseWhenAlreadyExpired() {
            // Arrange
            InventoryItem item = InventoryItem.builder()
                    .itemId("item-1")
                    .userId(TEST_USER_ID)
                    .name("牛乳")
                    .quantity(new BigDecimal("1"))
                    .unit("L")
                    .expiryDate(LocalDate.now().minusDays(1))
                    .purchasedAt(Instant.now())
                    .createdAt(Instant.now())
                    .build();

            // Act & Assert
            assertThat(item.isExpiringSoon()).isFalse();
        }

        @Test
        @DisplayName("賞味期限がnullの場合はfalseを返す")
        void shouldReturnFalseWhenExpiryDateIsNull() {
            // Arrange
            InventoryItem item = InventoryItem.create(TEST_USER_ID, "塩", new BigDecimal("1"), "袋", null);

            // Act & Assert
            assertThat(item.isExpiringSoon()).isFalse();
        }
    }
}
