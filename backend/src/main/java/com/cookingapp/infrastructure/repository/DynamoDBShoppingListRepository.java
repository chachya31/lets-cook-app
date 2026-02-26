package com.cookingapp.infrastructure.repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.cookingapp.domain.entity.ShoppingListItem;
import com.cookingapp.domain.repository.ShoppingListRepository;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

/**
 * DynamoDB買い物リストリポジトリ実装
 */
@Repository
public class DynamoDBShoppingListRepository implements ShoppingListRepository {
    private static final Logger log = LoggerFactory.getLogger(DynamoDBShoppingListRepository.class);
    private static final String GSI_NORMALIZED_KEY = "GSI_NormalizedKey";

    private final DynamoDbClient dynamoDbClient;
    private final String tableName;

    public DynamoDBShoppingListRepository(
            DynamoDbClient dynamoDbClient,
            @org.springframework.beans.factory.annotation.Value("${aws.dynamodb.table.shoppingLists:ShoppingLists}") String tableName) {
        this.dynamoDbClient = dynamoDbClient;
        this.tableName = tableName;
    }

    @Override
    public ShoppingListItem save(ShoppingListItem item) {
        Map<String, AttributeValue> itemMap = new HashMap<>();
        itemMap.put("UserId", AttributeValue.builder().s(item.getUserId()).build());
        itemMap.put("ItemId", AttributeValue.builder().s(item.getItemId()).build());
        itemMap.put("Name", AttributeValue.builder().s(item.getName()).build());
        itemMap.put("Quantity", AttributeValue.builder().n(item.getQuantity().toString()).build());
        itemMap.put("Unit", AttributeValue.builder().s(item.getUnit() != null ? item.getUnit() : "").build());
        itemMap.put("IsChecked", AttributeValue.builder().bool(item.isChecked()).build());

        if (item.getIsCheckedAt() != null) {
            itemMap.put("IsCheckedAt", AttributeValue.builder().s(item.getIsCheckedAt().toString()).build());
        }

        itemMap.put("AddedAt", AttributeValue.builder().s(item.getAddedAt().toString()).build());

        if (item.getSourceRecipeId() != null) {
            itemMap.put("SourceRecipeId", AttributeValue.builder().s(item.getSourceRecipeId()).build());
        }

        itemMap.put("NormalizedKey", AttributeValue.builder().s(item.getNormalizedKey()).build());

        PutItemRequest request = PutItemRequest.builder()
                .tableName(tableName)
                .item(itemMap)
                .build();

        dynamoDbClient.putItem(request);
        log.info("Saved shopping list item: userId={}, itemId={}", item.getUserId(), item.getItemId());
        return item;
    }

    @Override
    public Optional<ShoppingListItem> findById(String userId, String itemId) {
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
    public List<ShoppingListItem> findByUserId(String userId) {
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
    public Optional<ShoppingListItem> findByNormalizedKey(String userId, String normalizedKey) {
        Map<String, AttributeValue> expressionValues = new HashMap<>();
        expressionValues.put(":userId", AttributeValue.builder().s(userId).build());
        expressionValues.put(":normalizedKey", AttributeValue.builder().s(normalizedKey).build());

        QueryRequest request = QueryRequest.builder()
                .tableName(tableName)
                .indexName(GSI_NORMALIZED_KEY)
                .keyConditionExpression("UserId = :userId AND NormalizedKey = :normalizedKey")
                .expressionAttributeValues(expressionValues)
                .build();

        QueryResponse response = dynamoDbClient.query(request);

        if (response.items().isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(mapToEntity(response.items().get(0)));
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
        log.info("Deleted shopping list item: userId={}, itemId={}", userId, itemId);
    }

    @Override
    public boolean existsById(String userId, String itemId) {
        return findById(userId, itemId).isPresent();
    }

    @Override
    public void deleteExpiredCheckedItems(String userId) {
        List<ShoppingListItem> items = findByUserId(userId);
        Instant threeDaysAgo = Instant.now().minus(3, ChronoUnit.DAYS);

        items.stream()
                .filter(item -> item.isChecked() && item.getIsCheckedAt() != null)
                .filter(item -> item.getIsCheckedAt().isBefore(threeDaysAgo))
                .forEach(item -> delete(userId, item.getItemId()));

        log.info("Deleted expired checked items for userId={}", userId);
    }

    private ShoppingListItem mapToEntity(Map<String, AttributeValue> item) {
        return ShoppingListItem.builder()
                .itemId(item.get("ItemId").s())
                .userId(item.get("UserId").s())
                .name(item.get("Name").s())
                .quantity(new BigDecimal(item.get("Quantity").n()))
                .unit(item.get("Unit").s())
                .isChecked(item.get("IsChecked").bool())
                .isCheckedAt(item.containsKey("IsCheckedAt") && item.get("IsCheckedAt").s() != null
                        ? Instant.parse(item.get("IsCheckedAt").s())
                        : null)
                .addedAt(Instant.parse(item.get("AddedAt").s()))
                .sourceRecipeId(item.containsKey("SourceRecipeId") && item.get("SourceRecipeId").s() != null
                        ? item.get("SourceRecipeId").s()
                        : null)
                .normalizedKey(item.get("NormalizedKey").s())
                .build();
    }
}
