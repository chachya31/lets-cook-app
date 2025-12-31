package com.cookingapp.infrastructure.repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.cookingapp.domain.entity.InventoryItem;
import com.cookingapp.domain.repository.InventoryRepository;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

/**
 * DynamoDB食材在庫リポジトリ実装
 */
@Repository
public class DynamoDBInventoryRepository implements InventoryRepository {

    private static final Logger log = LoggerFactory.getLogger(DynamoDBInventoryRepository.class);

    private final DynamoDbClient dynamoDbClient;
    private final String tableName;

    public DynamoDBInventoryRepository(
            DynamoDbClient dynamoDbClient,
            @Value("${aws.dynamodb.table.inventory:Inventory}") String tableName) {
        this.dynamoDbClient = dynamoDbClient;
        this.tableName = tableName;
    }

    @Override
    public InventoryItem save(InventoryItem item) {
        Map<String, AttributeValue> itemMap = new HashMap<>();
        itemMap.put("UserId", AttributeValue.builder().s(item.getUserId()).build());
        itemMap.put("ItemId", AttributeValue.builder().s(item.getItemId()).build());
        itemMap.put("Name", AttributeValue.builder().s(item.getName()).build());
        itemMap.put("Quantity", AttributeValue.builder().n(item.getQuantity().toString()).build());
        itemMap.put("Unit", AttributeValue.builder().s(item.getUnit() != null ? item.getUnit() : "").build());

        if (item.getExpiryDate() != null) {
            itemMap.put("ExpiryDate", AttributeValue.builder().s(item.getExpiryDate().toString()).build());
        }

        itemMap.put("PurchasedAt", AttributeValue.builder().s(item.getPurchasedAt().toString()).build());
        itemMap.put("CreatedAt", AttributeValue.builder().s(item.getCreatedAt().toString()).build());

        PutItemRequest request = PutItemRequest.builder()
                .tableName(tableName)
                .item(itemMap)
                .build();

        dynamoDbClient.putItem(request);
        log.info("Saved inventory item: userId={}, itemId={}, name={}", item.getUserId(), item.getItemId(),
                item.getName());
        return item;
    }

    @Override
    public List<InventoryItem> findByUserId(String userId) {
        Map<String, AttributeValue> expressionValues = new HashMap<>();
        expressionValues.put(":userId", AttributeValue.builder().s(userId).build());

        QueryRequest request = QueryRequest.builder()
                .tableName(tableName)
                .keyConditionExpression("UserId = :userId")
                .expressionAttributeValues(expressionValues)
                .build();

        QueryResponse response = dynamoDbClient.query(request);

        return response.items().stream()
                .map(this::mapToEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<InventoryItem> findByUserIdOrderByExpiryDate(String userId) {
        // GSIではExpiryDateがnullのアイテムが含まれないため、
        // 通常のクエリで全件取得してアプリケーション側でソート
        List<InventoryItem> items = findByUserId(userId);

        // ExpiryDateでソート（nullは最後に）
        return items.stream()
                .sorted((a, b) -> {
                    if (a.getExpiryDate() == null && b.getExpiryDate() == null) {
                        return 0;
                    }
                    if (a.getExpiryDate() == null) {
                        return 1; // nullは最後
                    }
                    if (b.getExpiryDate() == null) {
                        return -1;
                    }
                    return a.getExpiryDate().compareTo(b.getExpiryDate());
                })
                .collect(Collectors.toList());
    }

    @Override
    public Optional<InventoryItem> findById(String userId, String itemId) {
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("UserId", AttributeValue.builder().s(userId).build());
        key.put("ItemId", AttributeValue.builder().s(itemId).build());

        GetItemRequest request = GetItemRequest.builder()
                .tableName(tableName)
                .key(key)
                .build();

        GetItemResponse response = dynamoDbClient.getItem(request);

        if (!response.hasItem() || response.item().isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(mapToEntity(response.item()));
    }

    @Override
    public void delete(String userId, String itemId) {
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("UserId", AttributeValue.builder().s(userId).build());
        key.put("ItemId", AttributeValue.builder().s(itemId).build());

        DeleteItemRequest request = DeleteItemRequest.builder()
                .tableName(tableName)
                .key(key)
                .build();

        dynamoDbClient.deleteItem(request);
        log.info("Deleted inventory item: userId={}, itemId={}", userId, itemId);
    }

    @Override
    public Optional<InventoryItem> findByUserIdAndNameAndUnit(String userId, String name, String unit) {
        List<InventoryItem> items = findByUserId(userId);
        return items.stream()
                .filter(item -> item.getName().equalsIgnoreCase(name))
                .filter(item -> {
                    String itemUnit = item.getUnit() != null ? item.getUnit() : "";
                    String targetUnit = unit != null ? unit : "";
                    return itemUnit.equalsIgnoreCase(targetUnit);
                })
                .findFirst();
    }

    private InventoryItem mapToEntity(Map<String, AttributeValue> item) {
        return InventoryItem.builder()
                .itemId(item.get("ItemId").s())
                .userId(item.get("UserId").s())
                .name(item.get("Name").s())
                .quantity(new BigDecimal(item.get("Quantity").n()))
                .unit(item.get("Unit").s())
                .expiryDate(item.containsKey("ExpiryDate") && item.get("ExpiryDate").s() != null
                        ? LocalDate.parse(item.get("ExpiryDate").s())
                        : null)
                .purchasedAt(Instant.parse(item.get("PurchasedAt").s()))
                .createdAt(Instant.parse(item.get("CreatedAt").s()))
                .build();
    }
}
