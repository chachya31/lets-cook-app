package com.cookingapp.infrastructure.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.cookingapp.domain.entity.Recipe;
import com.cookingapp.domain.entity.RecipeIngredient;
import com.cookingapp.domain.repository.RecipeRepository;
import com.cookingapp.domain.valueobject.Ingredient;
import com.cookingapp.domain.valueobject.Step;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.Delete;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.Put;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;
import software.amazon.awssdk.services.dynamodb.model.TransactWriteItem;
import software.amazon.awssdk.services.dynamodb.model.TransactWriteItemsRequest;

/**
 * DynamoDB レシピリポジトリ実装
 */
@Repository
public class DynamoDBRecipeRepository implements RecipeRepository {

        private static final Logger logger = LoggerFactory.getLogger(DynamoDBRecipeRepository.class);
        private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        private static final int TRANSACTION_BATCH_SIZE = 25;

        private final DynamoDbClient dynamoDbClient;
        private final String recipesTableName;
        private final String recipeIngredientsTableName;

        public DynamoDBRecipeRepository(
                        DynamoDbClient dynamoDbClient,
                        @Value("${aws.dynamodb.table.recipes:Recipes}") String recipesTableName,
                        @Value("${aws.dynamodb.table.recipe-ingredients:RecipeIngredients}") String recipeIngredientsTableName) {
                this.dynamoDbClient = dynamoDbClient;
                this.recipesTableName = recipesTableName;
                this.recipeIngredientsTableName = recipeIngredientsTableName;
        }

        @Override
        public Recipe save(Recipe recipe) {
                // 既存の食材インデックスを取得（更新時の差分削除用）
                Set<String> existingIngredientNames = getExistingIngredientNames(recipe.getRecipeId());

                // 新しい食材名セット
                Set<String> newIngredientNames = recipe.getIngredients().stream()
                                .map(ing -> RecipeIngredient.normalizeIngredientName(ing.getName()))
                                .collect(Collectors.toSet());

                // 削除対象: 既存にあって新規にない食材
                Set<String> toDelete = new HashSet<>(existingIngredientNames);
                toDelete.removeAll(newIngredientNames);

                // 追加対象: 新規にあって既存にない食材（または全て上書き）
                // シンプルに全食材を上書きする方式を採用
                List<RecipeIngredient> toAdd = recipe.getIngredients().stream()
                                .map(ing -> new RecipeIngredient(
                                                ing.getName(),
                                                recipe.getRecipeId(),
                                                recipe.getTitle(),
                                                recipe.getImageUrl()))
                                .collect(Collectors.toList());

                // トランザクションアイテムを構築
                List<TransactWriteItem> transactItems = new ArrayList<>();

                // 1. レシピ本体のPut
                transactItems.add(TransactWriteItem.builder()
                                .put(Put.builder()
                                                .tableName(recipesTableName)
                                                .item(toAttributeMap(recipe))
                                                .build())
                                .build());

                // 2. 削除対象の食材インデックスをDelete
                for (String ingredientName : toDelete) {
                        transactItems.add(TransactWriteItem.builder()
                                        .delete(Delete.builder()
                                                        .tableName(recipeIngredientsTableName)
                                                        .key(Map.of(
                                                                        "IngredientName",
                                                                        AttributeValue.builder().s(ingredientName)
                                                                                        .build(),
                                                                        "RecipeId",
                                                                        AttributeValue.builder().s(recipe.getRecipeId())
                                                                                        .build()))
                                                        .build())
                                        .build());
                }

                // 3. 新しい食材インデックスをPut
                for (RecipeIngredient ri : toAdd) {
                        transactItems.add(TransactWriteItem.builder()
                                        .put(Put.builder()
                                                        .tableName(recipeIngredientsTableName)
                                                        .item(recipeIngredientToAttributeMap(ri))
                                                        .build())
                                        .build());
                }

                // トランザクション実行（25件制限対応）
                executeTransactionInBatches(transactItems);

                logger.info("Saved recipe with ingredients: recipeId={}, added={}, deleted={}",
                                recipe.getRecipeId(), toAdd.size(), toDelete.size());

                return recipe;
        }

        @Override
        public Optional<Recipe> findById(String recipeId) {
                Map<String, AttributeValue> key = Map.of(
                                "RecipeId", AttributeValue.builder().s(recipeId).build());

                GetItemRequest request = GetItemRequest.builder()
                                .tableName(recipesTableName)
                                .key(key)
                                .build();

                GetItemResponse response = dynamoDbClient.getItem(request);

                if (!response.hasItem()) {
                        return Optional.empty();
                }

                return Optional.of(fromAttributeMap(response.item()));
        }

        @Override
        public List<Recipe> findByAuthorId(String authorId) {
                QueryRequest request = QueryRequest.builder()
                                .tableName(recipesTableName)
                                .indexName("GSI_Author")
                                .keyConditionExpression("AuthorId = :authorId")
                                .expressionAttributeValues(Map.of(
                                                ":authorId", AttributeValue.builder().s(authorId).build()))
                                .build();

                QueryResponse response = dynamoDbClient.query(request);

                return response.items().stream()
                                .map(this::fromAttributeMap)
                                .collect(Collectors.toList());
        }

        @Override
        public List<Recipe> findAllPublic() {
                ScanRequest request = ScanRequest.builder()
                                .tableName(recipesTableName)
                                .filterExpression("IsPublic = :isPublic AND IsDeleted = :isDeleted")
                                .expressionAttributeValues(Map.of(
                                                ":isPublic", AttributeValue.builder().bool(true).build(),
                                                ":isDeleted", AttributeValue.builder().bool(false).build()))
                                .build();

                ScanResponse response = dynamoDbClient.scan(request);

                return response.items().stream()
                                .map(this::fromAttributeMap)
                                .collect(Collectors.toList());
        }

        @Override
        public void delete(String recipeId) {
                // 食材インデックスも一緒に削除
                Set<String> existingIngredientNames = getExistingIngredientNames(recipeId);

                List<TransactWriteItem> transactItems = new ArrayList<>();

                // 1. レシピ本体の削除
                transactItems.add(TransactWriteItem.builder()
                                .delete(Delete.builder()
                                                .tableName(recipesTableName)
                                                .key(Map.of("RecipeId", AttributeValue.builder().s(recipeId).build()))
                                                .build())
                                .build());

                // 2. 食材インデックスの削除
                for (String ingredientName : existingIngredientNames) {
                        transactItems.add(TransactWriteItem.builder()
                                        .delete(Delete.builder()
                                                        .tableName(recipeIngredientsTableName)
                                                        .key(Map.of(
                                                                        "IngredientName",
                                                                        AttributeValue.builder().s(ingredientName)
                                                                                        .build(),
                                                                        "RecipeId",
                                                                        AttributeValue.builder().s(recipeId).build()))
                                                        .build())
                                        .build());
                }

                executeTransactionInBatches(transactItems);

                logger.info("Deleted recipe and {} ingredient indexes: {}",
                                existingIngredientNames.size(), recipeId);
        }

        @Override
        public boolean existsById(String recipeId) {
                return findById(recipeId).isPresent();
        }

        /**
         * 既存の食材インデックスから食材名を取得
         */
        private Set<String> getExistingIngredientNames(String recipeId) {
                ScanRequest scanRequest = ScanRequest.builder()
                                .tableName(recipeIngredientsTableName)
                                .filterExpression("RecipeId = :recipeId")
                                .expressionAttributeValues(Map.of(
                                                ":recipeId", AttributeValue.builder().s(recipeId).build()))
                                .build();

                ScanResponse response = dynamoDbClient.scan(scanRequest);

                return response.items().stream()
                                .map(item -> item.get("IngredientName").s())
                                .collect(Collectors.toSet());
        }

        /**
         * トランザクションを25件ずつバッチ実行
         */
        private void executeTransactionInBatches(List<TransactWriteItem> transactItems) {
                if (transactItems.isEmpty()) {
                        return;
                }

                for (int i = 0; i < transactItems.size(); i += TRANSACTION_BATCH_SIZE) {
                        int endIndex = Math.min(i + TRANSACTION_BATCH_SIZE, transactItems.size());
                        List<TransactWriteItem> batch = transactItems.subList(i, endIndex);

                        TransactWriteItemsRequest request = TransactWriteItemsRequest.builder()
                                        .transactItems(batch)
                                        .build();

                        dynamoDbClient.transactWriteItems(request);
                }
        }

        private Map<String, AttributeValue> toAttributeMap(Recipe recipe) {
                Map<String, AttributeValue> item = new HashMap<>();

                item.put("RecipeId", AttributeValue.builder().s(recipe.getRecipeId()).build());
                item.put("Title", AttributeValue.builder().s(recipe.getTitle()).build());
                item.put("AuthorId", AttributeValue.builder().s(recipe.getAuthorId()).build());
                item.put("CookingTime", AttributeValue.builder().n(String.valueOf(recipe.getCookingTime())).build());
                item.put("IsPublic", AttributeValue.builder().bool(recipe.isPublic()).build());
                item.put("IsDeleted", AttributeValue.builder().bool(recipe.isDeleted()).build());
                item.put("CreatedAt", AttributeValue.builder().s(recipe.getCreatedAt().format(FORMATTER)).build());
                item.put("UpdatedAt", AttributeValue.builder().s(recipe.getUpdatedAt().format(FORMATTER)).build());

                if (recipe.getImageUrl() != null) {
                        item.put("ImageUrl", AttributeValue.builder().s(recipe.getImageUrl()).build());
                }

                List<AttributeValue> ingredientsList = recipe.getIngredients().stream()
                                .map(this::ingredientToAttributeValue)
                                .collect(Collectors.toList());
                item.put("Ingredients", AttributeValue.builder().l(ingredientsList).build());

                List<AttributeValue> stepsList = recipe.getSteps().stream()
                                .map(this::stepToAttributeValue)
                                .collect(Collectors.toList());
                item.put("Steps", AttributeValue.builder().l(stepsList).build());

                return item;
        }

        private Map<String, AttributeValue> recipeIngredientToAttributeMap(RecipeIngredient ri) {
                Map<String, AttributeValue> item = new HashMap<>();
                item.put("IngredientName", AttributeValue.builder().s(ri.getIngredientName()).build());
                item.put("RecipeId", AttributeValue.builder().s(ri.getRecipeId()).build());
                item.put("RecipeTitle", AttributeValue.builder().s(ri.getRecipeTitle()).build());

                if (ri.getRecipeImageUrl() != null) {
                        item.put("RecipeImageUrl", AttributeValue.builder().s(ri.getRecipeImageUrl()).build());
                }

                return item;
        }

        private Recipe fromAttributeMap(Map<String, AttributeValue> item) {
                String recipeId = item.get("RecipeId").s();
                String title = item.get("Title").s();
                String authorId = item.get("AuthorId").s();
                int cookingTime = Integer.parseInt(item.get("CookingTime").n());
                boolean isPublic = item.get("IsPublic").bool();
                boolean isDeleted = item.get("IsDeleted").bool();
                LocalDateTime createdAt = LocalDateTime.parse(item.get("CreatedAt").s(), FORMATTER);
                LocalDateTime updatedAt = LocalDateTime.parse(item.get("UpdatedAt").s(), FORMATTER);

                String imageUrl = item.containsKey("ImageUrl") ? item.get("ImageUrl").s() : null;

                List<Ingredient> ingredients = item.get("Ingredients").l().stream()
                                .map(this::attributeValueToIngredient)
                                .collect(Collectors.toList());

                List<Step> steps = item.get("Steps").l().stream()
                                .map(this::attributeValueToStep)
                                .collect(Collectors.toList());

                return new Recipe(recipeId, authorId, title, ingredients, steps, cookingTime,
                                imageUrl, isPublic, isDeleted, createdAt, updatedAt);
        }

        private AttributeValue ingredientToAttributeValue(Ingredient ingredient) {
                Map<String, AttributeValue> map = new HashMap<>();
                map.put("name", AttributeValue.builder().s(ingredient.getName()).build());
                map.put("optional", AttributeValue.builder().bool(ingredient.isOptional()).build());

                if (ingredient.getQuantity() != null) {
                        map.put("quantity", AttributeValue.builder().n(ingredient.getQuantity().toString()).build());
                }
                if (ingredient.getUnit() != null) {
                        map.put("unit", AttributeValue.builder().s(ingredient.getUnit()).build());
                }
                if (ingredient.getNote() != null) {
                        map.put("note", AttributeValue.builder().s(ingredient.getNote()).build());
                }

                return AttributeValue.builder().m(map).build();
        }

        private Ingredient attributeValueToIngredient(AttributeValue attributeValue) {
                Map<String, AttributeValue> map = attributeValue.m();

                String name = map.get("name").s();
                BigDecimal quantity = map.containsKey("quantity") ? new BigDecimal(map.get("quantity").n()) : null;
                String unit = map.containsKey("unit") ? map.get("unit").s() : null;
                boolean optional = map.get("optional").bool();
                String note = map.containsKey("note") ? map.get("note").s() : null;

                return new Ingredient(name, quantity, unit, note, optional);
        }

        private AttributeValue stepToAttributeValue(Step step) {
                Map<String, AttributeValue> map = new HashMap<>();
                map.put("description", AttributeValue.builder().s(step.getDescription()).build());
                if (step.getImageUrl() != null) {
                        map.put("imageUrl", AttributeValue.builder().s(step.getImageUrl()).build());
                }
                if (step.getVideoUrl() != null) {
                        map.put("videoUrl", AttributeValue.builder().s(step.getVideoUrl()).build());
                }
                return AttributeValue.builder().m(map).build();
        }

        private Step attributeValueToStep(AttributeValue attributeValue) {
                // 新形式: マップ {description: "...", imageUrl: "...", videoUrl: "..."}
                if (attributeValue.m() != null && !attributeValue.m().isEmpty()) {
                        Map<String, AttributeValue> map = attributeValue.m();
                        String description = map.get("description").s();
                        String imageUrl = map.containsKey("imageUrl") ? map.get("imageUrl").s() : null;
                        String videoUrl = map.containsKey("videoUrl") ? map.get("videoUrl").s() : null;
                        return new Step(description, imageUrl, videoUrl);
                }
                // 旧形式: 文字列 "手順の説明"
                if (attributeValue.s() != null) {
                        return new Step(attributeValue.s(), null, null);
                }
                throw new IllegalArgumentException("Invalid step format in DynamoDB");
        }
}
