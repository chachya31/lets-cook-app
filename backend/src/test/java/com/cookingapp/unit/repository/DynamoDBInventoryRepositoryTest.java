package com.cookingapp.unit.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cookingapp.domain.entity.InventoryItem;
import com.cookingapp.infrastructure.repository.DynamoDBInventoryRepository;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemResponse;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemResponse;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

/**
 * DynamoDBInventoryRepositoryのユニットテスト
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DynamoDBInventoryRepository ユニットテスト")
class DynamoDBInventoryRepositoryTest {

    @Mock
    private DynamoDbClient dynamoDbClient;

    private DynamoDBInventoryRepository inventoryRepository;

    private static final String TABLE_NAME = "Inventory";
    private static final String TEST_USER_ID = "user-123";
    private static final String TEST_ITEM_ID = "item-456";

    @BeforeEach
    void setUp() {
        inventoryRepository = new DynamoDBInventoryRepository(dynamoDbClient, TABLE_NAME);
    }

    /**
     * テスト用のDynamoDBアイテムを作成するヘルパーメソッド
     */
    private Map<String, AttributeValue> createDynamoDbItem(String itemId, String userId, String name,
            BigDecimal quantity, String unit, LocalDate expiryDate) {
        Instant now = Instant.now();
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("ItemId", AttributeValue.builder().s(itemId).build());
        item.put("UserId", AttributeValue.builder().s(userId).build());
        item.put("Name", AttributeValue.builder().s(name).build());
        item.put("Quantity", AttributeValue.builder().n(quantity.toString()).build());
        item.put("Unit", AttributeValue.builder().s(unit != null ? unit : "").build());
        if (expiryDate != null) {
            item.put("ExpiryDate", AttributeValue.builder().s(expiryDate.toString()).build());
        }
        item.put("PurchasedAt", AttributeValue.builder().s(now.toString()).build());
        item.put("CreatedAt", AttributeValue.builder().s(now.toString()).build());
        return item;
    }

    /**
     * テスト用のInventoryItemを作成するヘルパーメソッド
     */
    private InventoryItem createTestInventoryItem(String itemId, String userId, String name,
            BigDecimal quantity, String unit, LocalDate expiryDate) {
        return InventoryItem.builder()
                .itemId(itemId)
                .userId(userId)
                .name(name)
                .quantity(quantity)
                .unit(unit)
                .expiryDate(expiryDate)
                .purchasedAt(Instant.now())
                .createdAt(Instant.now())
                .build();
    }

    @Nested
    @DisplayName("save - 在庫アイテム保存")
    class Save {

        @Test
        @DisplayName("在庫アイテムを正しく保存する")
        void shouldSaveInventoryItem() {
            // Arrange
            LocalDate expiryDate = LocalDate.now().plusDays(7);
            InventoryItem item = createTestInventoryItem(TEST_ITEM_ID, TEST_USER_ID, "たまご",
                    new BigDecimal("10"), "個", expiryDate);

            when(dynamoDbClient.putItem(any(PutItemRequest.class)))
                    .thenReturn(PutItemResponse.builder().build());

            // Act
            InventoryItem result = inventoryRepository.save(item);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getItemId()).isEqualTo(TEST_ITEM_ID);

            ArgumentCaptor<PutItemRequest> captor = ArgumentCaptor.forClass(PutItemRequest.class);
            verify(dynamoDbClient).putItem(captor.capture());

            PutItemRequest request = captor.getValue();
            assertThat(request.tableName()).isEqualTo(TABLE_NAME);
            assertThat(request.item().get("UserId").s()).isEqualTo(TEST_USER_ID);
            assertThat(request.item().get("ItemId").s()).isEqualTo(TEST_ITEM_ID);
            assertThat(request.item().get("Name").s()).isEqualTo("たまご");
            assertThat(request.item().get("Quantity").n()).isEqualTo("10");
            assertThat(request.item().get("Unit").s()).isEqualTo("個");
            assertThat(request.item().get("ExpiryDate").s()).isEqualTo(expiryDate.toString());
        }

        @Test
        @DisplayName("賞味期限なしの在庫アイテムを保存する")
        void shouldSaveInventoryItemWithoutExpiryDate() {
            // Arrange
            InventoryItem item = createTestInventoryItem(TEST_ITEM_ID, TEST_USER_ID, "塩",
                    new BigDecimal("1"), "袋", null);

            when(dynamoDbClient.putItem(any(PutItemRequest.class)))
                    .thenReturn(PutItemResponse.builder().build());

            // Act
            inventoryRepository.save(item);

            // Assert
            ArgumentCaptor<PutItemRequest> captor = ArgumentCaptor.forClass(PutItemRequest.class);
            verify(dynamoDbClient).putItem(captor.capture());

            PutItemRequest request = captor.getValue();
            assertThat(request.item().containsKey("ExpiryDate")).isFalse();
        }
    }

    @Nested
    @DisplayName("findByUserId - ユーザーIDで在庫検索")
    class FindByUserId {

        @Test
        @DisplayName("ユーザーの在庫一覧を取得する")
        void shouldReturnInventoryListForUser() {
            // Arrange
            Map<String, AttributeValue> item1 = createDynamoDbItem("item-1", TEST_USER_ID, "にんじん",
                    new BigDecimal("3"), "本", LocalDate.now().plusDays(5));
            Map<String, AttributeValue> item2 = createDynamoDbItem("item-2", TEST_USER_ID, "牛乳",
                    new BigDecimal("1"), "L", LocalDate.now().plusDays(2));

            QueryResponse response = QueryResponse.builder()
                    .items(List.of(item1, item2))
                    .build();

            when(dynamoDbClient.query(any(QueryRequest.class))).thenReturn(response);

            // Act
            List<InventoryItem> result = inventoryRepository.findByUserId(TEST_USER_ID);

            // Assert
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getName()).isEqualTo("にんじん");
            assertThat(result.get(1).getName()).isEqualTo("牛乳");

            ArgumentCaptor<QueryRequest> captor = ArgumentCaptor.forClass(QueryRequest.class);
            verify(dynamoDbClient).query(captor.capture());
            assertThat(captor.getValue().tableName()).isEqualTo(TABLE_NAME);
        }

        @Test
        @DisplayName("在庫がない場合は空のリストを返す")
        void shouldReturnEmptyListWhenNoInventory() {
            // Arrange
            QueryResponse response = QueryResponse.builder()
                    .items(List.of())
                    .build();

            when(dynamoDbClient.query(any(QueryRequest.class))).thenReturn(response);

            // Act
            List<InventoryItem> result = inventoryRepository.findByUserId(TEST_USER_ID);

            // Assert
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByUserIdOrderByExpiryDate - 賞味期限順で在庫検索")
    class FindByUserIdOrderByExpiryDate {

        @Test
        @DisplayName("賞味期限順でソートされた在庫一覧を取得する")
        void shouldReturnInventoryListSortedByExpiryDate() {
            // Arrange
            Map<String, AttributeValue> item1 = createDynamoDbItem("item-1", TEST_USER_ID, "にんじん",
                    new BigDecimal("3"), "本", LocalDate.now().plusDays(5));
            Map<String, AttributeValue> item2 = createDynamoDbItem("item-2", TEST_USER_ID, "牛乳",
                    new BigDecimal("1"), "L", LocalDate.now().plusDays(2));

            QueryResponse response = QueryResponse.builder()
                    .items(List.of(item1, item2))
                    .build();

            when(dynamoDbClient.query(any(QueryRequest.class))).thenReturn(response);

            // Act
            List<InventoryItem> result = inventoryRepository.findByUserIdOrderByExpiryDate(TEST_USER_ID);

            // Assert
            assertThat(result).hasSize(2);
            // 賞味期限が近い順（牛乳が先）
            assertThat(result.get(0).getName()).isEqualTo("牛乳");
            assertThat(result.get(1).getName()).isEqualTo("にんじん");
        }

        @Test
        @DisplayName("賞味期限がnullのアイテムは最後に配置される")
        void shouldPlaceNullExpiryDateItemsAtEnd() {
            // Arrange
            Map<String, AttributeValue> item1 = createDynamoDbItem("item-1", TEST_USER_ID, "塩",
                    new BigDecimal("1"), "袋", null);
            Map<String, AttributeValue> item2 = createDynamoDbItem("item-2", TEST_USER_ID, "牛乳",
                    new BigDecimal("1"), "L", LocalDate.now().plusDays(2));

            QueryResponse response = QueryResponse.builder()
                    .items(List.of(item1, item2))
                    .build();

            when(dynamoDbClient.query(any(QueryRequest.class))).thenReturn(response);

            // Act
            List<InventoryItem> result = inventoryRepository.findByUserIdOrderByExpiryDate(TEST_USER_ID);

            // Assert
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getName()).isEqualTo("牛乳"); // 賞味期限あり
            assertThat(result.get(1).getName()).isEqualTo("塩"); // 賞味期限なし（最後）
        }
    }

    @Nested
    @DisplayName("findById - IDで在庫検索")
    class FindById {

        @Test
        @DisplayName("IDで在庫アイテムを取得する")
        void shouldReturnInventoryItemById() {
            // Arrange
            Map<String, AttributeValue> item = createDynamoDbItem(TEST_ITEM_ID, TEST_USER_ID, "たまご",
                    new BigDecimal("10"), "個", LocalDate.now().plusDays(7));

            GetItemResponse response = GetItemResponse.builder()
                    .item(item)
                    .build();

            when(dynamoDbClient.getItem(any(GetItemRequest.class))).thenReturn(response);

            // Act
            Optional<InventoryItem> result = inventoryRepository.findById(TEST_USER_ID, TEST_ITEM_ID);

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get().getItemId()).isEqualTo(TEST_ITEM_ID);
            assertThat(result.get().getName()).isEqualTo("たまご");

            ArgumentCaptor<GetItemRequest> captor = ArgumentCaptor.forClass(GetItemRequest.class);
            verify(dynamoDbClient).getItem(captor.capture());
            assertThat(captor.getValue().tableName()).isEqualTo(TABLE_NAME);
        }

        @Test
        @DisplayName("存在しない場合はOptional.emptyを返す")
        void shouldReturnEmptyWhenNotFound() {
            // Arrange
            GetItemResponse response = GetItemResponse.builder()
                    .item(new HashMap<>())
                    .build();

            when(dynamoDbClient.getItem(any(GetItemRequest.class))).thenReturn(response);

            // Act
            Optional<InventoryItem> result = inventoryRepository.findById(TEST_USER_ID, "non-existent");

            // Assert
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("delete - 在庫アイテム削除")
    class Delete {

        @Test
        @DisplayName("在庫アイテムを削除する")
        void shouldDeleteInventoryItem() {
            // Arrange
            when(dynamoDbClient.deleteItem(any(DeleteItemRequest.class)))
                    .thenReturn(DeleteItemResponse.builder().build());

            // Act
            inventoryRepository.delete(TEST_USER_ID, TEST_ITEM_ID);

            // Assert
            ArgumentCaptor<DeleteItemRequest> captor = ArgumentCaptor.forClass(DeleteItemRequest.class);
            verify(dynamoDbClient).deleteItem(captor.capture());

            DeleteItemRequest request = captor.getValue();
            assertThat(request.tableName()).isEqualTo(TABLE_NAME);
            assertThat(request.key().get("UserId").s()).isEqualTo(TEST_USER_ID);
            assertThat(request.key().get("ItemId").s()).isEqualTo(TEST_ITEM_ID);
        }
    }

    @Nested
    @DisplayName("findByUserIdAndNameAndUnit - 名前と単位で在庫検索")
    class FindByUserIdAndNameAndUnit {

        @Test
        @DisplayName("名前と単位が一致する在庫アイテムを取得する")
        void shouldReturnInventoryItemByNameAndUnit() {
            // Arrange
            Map<String, AttributeValue> item = createDynamoDbItem(TEST_ITEM_ID, TEST_USER_ID, "たまご",
                    new BigDecimal("10"), "個", LocalDate.now().plusDays(7));

            QueryResponse response = QueryResponse.builder()
                    .items(List.of(item))
                    .build();

            when(dynamoDbClient.query(any(QueryRequest.class))).thenReturn(response);

            // Act
            Optional<InventoryItem> result = inventoryRepository.findByUserIdAndNameAndUnit(TEST_USER_ID, "たまご", "個");

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get().getName()).isEqualTo("たまご");
            assertThat(result.get().getUnit()).isEqualTo("個");
        }

        @Test
        @DisplayName("大文字小文字を区別せずに検索する")
        void shouldFindCaseInsensitive() {
            // Arrange
            Map<String, AttributeValue> item = createDynamoDbItem(TEST_ITEM_ID, TEST_USER_ID, "Milk",
                    new BigDecimal("1"), "L", LocalDate.now().plusDays(7));

            QueryResponse response = QueryResponse.builder()
                    .items(List.of(item))
                    .build();

            when(dynamoDbClient.query(any(QueryRequest.class))).thenReturn(response);

            // Act
            Optional<InventoryItem> result = inventoryRepository.findByUserIdAndNameAndUnit(TEST_USER_ID, "milk", "l");

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get().getName()).isEqualTo("Milk");
        }

        @Test
        @DisplayName("名前が一致しない場合はOptional.emptyを返す")
        void shouldReturnEmptyWhenNameDoesNotMatch() {
            // Arrange
            Map<String, AttributeValue> item = createDynamoDbItem(TEST_ITEM_ID, TEST_USER_ID, "にんじん",
                    new BigDecimal("3"), "本", LocalDate.now().plusDays(5));

            QueryResponse response = QueryResponse.builder()
                    .items(List.of(item))
                    .build();

            when(dynamoDbClient.query(any(QueryRequest.class))).thenReturn(response);

            // Act
            Optional<InventoryItem> result = inventoryRepository.findByUserIdAndNameAndUnit(TEST_USER_ID, "たまご", "本");

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("単位がnullの場合も検索できる")
        void shouldFindWithNullUnit() {
            // Arrange
            Map<String, AttributeValue> item = createDynamoDbItem(TEST_ITEM_ID, TEST_USER_ID, "塩",
                    new BigDecimal("1"), "", null);

            QueryResponse response = QueryResponse.builder()
                    .items(List.of(item))
                    .build();

            when(dynamoDbClient.query(any(QueryRequest.class))).thenReturn(response);

            // Act
            Optional<InventoryItem> result = inventoryRepository.findByUserIdAndNameAndUnit(TEST_USER_ID, "塩", null);

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get().getName()).isEqualTo("塩");
        }
    }
}
