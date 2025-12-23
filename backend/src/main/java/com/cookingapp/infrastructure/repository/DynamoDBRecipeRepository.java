package com.cookingapp.infrastructure.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.cookingapp.domain.entity.Recipe;
import com.cookingapp.domain.repository.RecipeRepository;
import com.cookingapp.domain.valueobject.Ingredient;
import com.cookingapp.domain.valueobject.Step;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;

/**
 * DynamoDB レシピリポジトリ実装
 */
@Repository
public class DynamoDBRecipeRepository implements RecipeRepository {

        private static final Logger logger = LoggerFactory.getLogger(DynamoDBRecipeRepository.class);
        private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        private final DynamoDbClient dynamoDbClient;
        private final String tableName;

        public DynamoDBRecipeRepository(
                        DynamoDbClient dynamoDbClient,
                        @Value("${aws.dynamodb.table.recipes:Recipes}") String tableName) {
                this.dynamoDbClient = dynamoDbClient;
                this.tableName = tableName;
        }

        @Override
        public Recipe save(Recipe recipe) {
                Map<String, AttributeValue> item = toAttributeMap(recipe);

                PutItemRequest request = PutItemRequest.builder()
                                .tableName(tableName)
                                .item(item)
                                .build();

                dynamoDbClient.putItem(request);
                logger.info("Saved recipe: {}", recipe.getRecipeId());

                return recipe;
        }

        @Override
        public Optional<Recipe> findById(String recipeId) {
                Map<String, AttributeValue> key = Map.of(
                                "RecipeId", AttributeValue.builder().s(recipeId).build());

                GetItemRequest request = GetItemRequest.builder()
                                .tableName(tableName)
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
                                .tableName(tableName)
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
                                .tableName(tableName)
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
                Map<String, AttributeValue> key = Map.of(
                                "RecipeId", AttributeValue.builder().s(recipeId).build());

                DeleteItemRequest request = DeleteItemRequest.builder()
                                .tableName(tableName)
                                .key(key)
                                .build();

                dynamoDbClient.deleteItem(request);
                logger.info("Deleted recipe: {}", recipeId);
        }

        @Override
        public boolean existsById(String recipeId) {
                return findById(recipeId).isPresent();
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
                return AttributeValue.builder().m(map).build();
        }

        private Step attributeValueToStep(AttributeValue attributeValue) {
                // 新形式: マップ {description: "...", imageUrl: "..."}
                if (attributeValue.m() != null && !attributeValue.m().isEmpty()) {
                        Map<String, AttributeValue> map = attributeValue.m();
                        String description = map.get("description").s();
                        String imageUrl = map.containsKey("imageUrl") ? map.get("imageUrl").s() : null;
                        return new Step(description, imageUrl);
                }
                // 旧形式: 文字列 "手順の説明"
                if (attributeValue.s() != null) {
                        return new Step(attributeValue.s(), null);
                }
                throw new IllegalArgumentException("Invalid step format in DynamoDB");
        }
}
