package com.cookingapp.infrastructure.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.cookingapp.domain.entity.RecipeIngredient;
import com.cookingapp.domain.repository.RecipeIngredientRepository;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.Delete;
import software.amazon.awssdk.services.dynamodb.model.Put;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;
import software.amazon.awssdk.services.dynamodb.model.TransactWriteItem;
import software.amazon.awssdk.services.dynamodb.model.TransactWriteItemsRequest;

/**
 * DynamoDB レシピ食材リポジトリ実装（逆引きインデックス用）
 */
@Repository
public class DynamoDBRecipeIngredientRepository implements RecipeIngredientRepository {

    private static final Logger logger = LoggerFactory.getLogger(DynamoDBRecipeIngredientRepository.class);

    private final DynamoDbClient dynamoDbClient;
    private final String tableName;

    public DynamoDBRecipeIngredientRepository(
            DynamoDbClient dynamoDbClient,
            @Value("${aws.dynamodb.table.recipe-ingredients:RecipeIngredients}") String tableName) {
        this.dynamoDbClient = dynamoDbClient;
        this.tableName = tableName;
    }

    @Override
    public List<RecipeIngredient> findByIngredientName(String ingredientName) {
        String normalizedName = RecipeIngredient.normalizeIngredientName(ingredientName);

        QueryRequest request = QueryRequest.builder()
                .tableName(tableName)
                .keyConditionExpression("IngredientName = :ingredientName")
                .expressionAttributeValues(Map.of(
                        ":ingredientName", AttributeValue.builder().s(normalizedName).build()))
                .build();

        QueryResponse response = dynamoDbClient.query(request);

        return response.items().stream()
                .map(this::fromAttributeMap)
                .collect(Collectors.toList());
    }

    @Override
    public List<RecipeIngredient> findByIngredientNames(List<String> ingredientNames) {
        if (ingredientNames == null || ingredientNames.isEmpty()) {
            return new ArrayList<>();
        }

        // 各食材で検索してレシピIDを収集
        Map<String, Set<String>> recipeIdsByIngredient = new HashMap<>();
        Map<String, RecipeIngredient> recipeIngredientMap = new HashMap<>();

        for (String ingredientName : ingredientNames) {
            List<RecipeIngredient> results = findByIngredientName(ingredientName);
            Set<String> recipeIds = new HashSet<>();

            for (RecipeIngredient ri : results) {
                recipeIds.add(ri.getRecipeId());
                // 最新の情報を保持（どの食材からでも同じレシピ情報）
                recipeIngredientMap.put(ri.getRecipeId(), ri);
            }

            recipeIdsByIngredient.put(ingredientName, recipeIds);
        }

        // AND検索: すべての食材を含むレシピIDを抽出
        Set<String> commonRecipeIds = null;
        for (Set<String> recipeIds : recipeIdsByIngredient.values()) {
            if (commonRecipeIds == null) {
                commonRecipeIds = new HashSet<>(recipeIds);
            } else {
                commonRecipeIds.retainAll(recipeIds);
            }
        }

        if (commonRecipeIds == null || commonRecipeIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 共通のレシピIDに対応するRecipeIngredientを返す
        return commonRecipeIds.stream()
                .map(recipeIngredientMap::get)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteByRecipeId(String recipeId) {
        // まずレシピIDに紐づくすべての食材インデックスを検索
        ScanRequest scanRequest = ScanRequest.builder()
                .tableName(tableName)
                .filterExpression("RecipeId = :recipeId")
                .expressionAttributeValues(Map.of(
                        ":recipeId", AttributeValue.builder().s(recipeId).build()))
                .build();

        ScanResponse scanResponse = dynamoDbClient.scan(scanRequest);

        if (scanResponse.items().isEmpty()) {
            logger.info("No recipe ingredients found for recipeId: {}", recipeId);
            return;
        }

        // トランザクションで一括削除（25件制限に注意）
        List<TransactWriteItem> deleteItems = scanResponse.items().stream()
                .map(item -> TransactWriteItem.builder()
                        .delete(Delete.builder()
                                .tableName(tableName)
                                .key(Map.of(
                                        "IngredientName", item.get("IngredientName"),
                                        "RecipeId", item.get("RecipeId")))
                                .build())
                        .build())
                .collect(Collectors.toList());

        // DynamoDBトランザクションは25件まで
        for (int i = 0; i < deleteItems.size(); i += 25) {
            int endIndex = Math.min(i + 25, deleteItems.size());
            List<TransactWriteItem> batch = deleteItems.subList(i, endIndex);

            TransactWriteItemsRequest request = TransactWriteItemsRequest.builder()
                    .transactItems(batch)
                    .build();

            dynamoDbClient.transactWriteItems(request);
        }

        logger.info("Deleted {} recipe ingredient indexes for recipeId: {}",
                deleteItems.size(), recipeId);
    }

    @Override
    public void saveAll(List<RecipeIngredient> recipeIngredients) {
        if (recipeIngredients == null || recipeIngredients.isEmpty()) {
            return;
        }

        List<TransactWriteItem> putItems = recipeIngredients.stream()
                .map(ri -> TransactWriteItem.builder()
                        .put(Put.builder()
                                .tableName(tableName)
                                .item(toAttributeMap(ri))
                                .build())
                        .build())
                .collect(Collectors.toList());

        // DynamoDBトランザクションは25件まで
        for (int i = 0; i < putItems.size(); i += 25) {
            int endIndex = Math.min(i + 25, putItems.size());
            List<TransactWriteItem> batch = putItems.subList(i, endIndex);

            TransactWriteItemsRequest request = TransactWriteItemsRequest.builder()
                    .transactItems(batch)
                    .build();

            dynamoDbClient.transactWriteItems(request);
        }

        logger.info("Saved {} recipe ingredient indexes", recipeIngredients.size());
    }

    private Map<String, AttributeValue> toAttributeMap(RecipeIngredient recipeIngredient) {
        Map<String, AttributeValue> item = new HashMap<>();

        item.put("IngredientName",
                AttributeValue.builder().s(recipeIngredient.getIngredientName()).build());
        item.put("RecipeId",
                AttributeValue.builder().s(recipeIngredient.getRecipeId()).build());
        item.put("RecipeTitle",
                AttributeValue.builder().s(recipeIngredient.getRecipeTitle()).build());

        if (recipeIngredient.getRecipeImageUrl() != null) {
            item.put("RecipeImageUrl",
                    AttributeValue.builder().s(recipeIngredient.getRecipeImageUrl()).build());
        }

        return item;
    }

    private RecipeIngredient fromAttributeMap(Map<String, AttributeValue> item) {
        String ingredientName = item.get("IngredientName").s();
        String recipeId = item.get("RecipeId").s();
        String recipeTitle = item.get("RecipeTitle").s();
        String recipeImageUrl = item.containsKey("RecipeImageUrl")
                ? item.get("RecipeImageUrl").s()
                : null;

        return new RecipeIngredient(ingredientName, recipeId, recipeTitle, recipeImageUrl);
    }
}
