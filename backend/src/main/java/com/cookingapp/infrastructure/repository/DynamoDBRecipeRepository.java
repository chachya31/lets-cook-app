package com.cookingapp.infrastructure.repository;

import com.cookingapp.domain.entity.Recipe;
import com.cookingapp.domain.repository.RecipeRepository;
import com.cookingapp.domain.valueobject.Ingredient;
import com.cookingapp.domain.valueobject.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

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
                "RecipeId", AttributeValue.builder().s(recipeId).build()
        );

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
        // GSI_Authorを使用して検索
        QueryRequest request = QueryRequest.builder()
                .tableName(tableName)
                .indexName("GSI_Author")
                .keyConditionExpression("AuthorId = :authorId")
                .expressionAttributeValues(Map.of(
                        ":authorId", AttributeValue.builder().s(authorId).build()
                ))
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
                        ":isDeleted", AttributeValue.builder().bool(false).build()
                ))
                .build();

        ScanResponse response = dynamoDbClient.scan(request);

        return response.items().stream()
                .map(this::fromAttributeMap)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(String recipeId) {
        Map<String, AttributeValue> key = Map.of(
                "RecipeId", AttributeValue.builder().s(recipeId).build()
        );

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

    /**
     * RecipeをDynamoDB AttributeMapに変換
     */
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

        // 食材リストを変換
        List<AttributeValue> ingredientsList = recipe.getIngredients().stream()
                .map(this::ingredientToAttributeValue)
                .collect(Collectors.toList());
        item.put("Ingredients", AttributeValue.builder().l(ingredientsList).build());

        // 手順リストを変換
        List<AttributeValue> stepsList = recipe.getSteps().stream()
                .map(step -> AttributeValue.builder().s(step).build())
                .collect(Collectors.toList());
        item.put("Steps", AttributeValue.builder().l(stepsList).build());

        return item;
    }

    /**
     * DynamoDB AttributeMapからRecipeに変換
     */
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

        List<String> steps = item.get("Steps").l().stream()
                .map(AttributeValue::s)
                .collect(Collectors.toList());

        return new Recipe(recipeId, authorId, title, ingredients, steps, cookingTime,
                imageUrl, isPublic, isDeleted, createdAt, updatedAt);
    }

    /**
     * IngredientをAttributeValueに変換
     */
    private AttributeValue ingredientToAttributeValue(Ingredient ingredient) {
        Map<String, AttributeValue> map = new HashMap<>();
        map.put("name", AttributeValue.builder().s(ingredient.getName()).build());
        map.put("quantity", AttributeValue.builder().n(ingredient.getQuantity().toString()).build());
        map.put("unit", AttributeValue.builder().s(ingredient.getUnit().getCode()).build());
        map.put("optional", AttributeValue.builder().bool(ingredient.isOptional()).build());

        if (ingredient.getNote() != null) {
            map.put("note", AttributeValue.builder().s(ingredient.getNote()).build());
        }

        return AttributeValue.builder().m(map).build();
    }

    /**
     * AttributeValueからIngredientに変換
     */
    private Ingredient attributeValueToIngredient(AttributeValue attributeValue) {
        Map<String, AttributeValue> map = attributeValue.m();

        String name = map.get("name").s();
        BigDecimal quantity = new BigDecimal(map.get("quantity").n());
        Unit unit = Unit.fromCode(map.get("unit").s());
        boolean optional = map.get("optional").bool();
        String note = map.containsKey("note") ? map.get("note").s() : null;

        return new Ingredient(name, quantity, unit, note, optional);
    }
}
