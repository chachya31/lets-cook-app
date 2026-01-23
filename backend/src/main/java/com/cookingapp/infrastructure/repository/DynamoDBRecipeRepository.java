package com.cookingapp.infrastructure.repository;

import com.cookingapp.domain.model.Ingredient;
import com.cookingapp.domain.model.Recipe;
import com.cookingapp.domain.model.RecipeIngredient;
import com.cookingapp.domain.repository.RecipeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.TransactWriteItemsEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class DynamoDBRecipeRepository implements RecipeRepository {

    private static final Logger logger = LoggerFactory.getLogger(DynamoDBRecipeRepository.class);

    private final DynamoDbEnhancedClient enhancedClient;
    private final DynamoDbTable<Recipe> recipeTable;
    private final DynamoDbTable<RecipeIngredient> recipeIngredientTable;
    private final DynamoDbIndex<Recipe> authorIndex;

    public DynamoDBRecipeRepository(
            DynamoDbEnhancedClient enhancedClient,
            @Value("${aws.dynamodb.table.recipes:Recipes}") String recipesTableName,
            @Value("${aws.dynamodb.table.recipe-ingredients:RecipeIngredients}") String recipeIngredientsTableName) {
        this.enhancedClient = enhancedClient;
        this.recipeTable = enhancedClient.table(recipesTableName, TableSchema.fromBean(Recipe.class));
        this.recipeIngredientTable = enhancedClient.table(recipeIngredientsTableName, TableSchema.fromBean(RecipeIngredient.class));
        this.authorIndex = recipeTable.index("GSI_Author");
    }

    @Override
    public Recipe save(Recipe recipe) {
        logger.debug("Saving recipe with ID: {}", recipe.getRecipeId());

        TransactWriteItemsEnhancedRequest.Builder transactionBuilder = TransactWriteItemsEnhancedRequest.builder();

        transactionBuilder.addPutItem(recipeTable, recipe);

        if (recipe.getIngredients() != null) {
            for (Ingredient ingredient : recipe.getIngredients()) {
                RecipeIngredient recipeIngredient = RecipeIngredient.builder()
                        .ingredientName(ingredient.getName())
                        .recipeId(recipe.getRecipeId())
                        .recipeTitle(recipe.getTitle())
                        .build();
                transactionBuilder.addPutItem(recipeIngredientTable, recipeIngredient);
            }
        }

        enhancedClient.transactWriteItems(transactionBuilder.build());
        logger.info("Recipe saved successfully with ID: {}", recipe.getRecipeId());

        return recipe;
    }

    @Override
    public Optional<Recipe> findById(String recipeId) {
        logger.debug("Finding recipe by ID: {}", recipeId);

        Key key = Key.builder()
                .partitionValue(recipeId)
                .build();

        Recipe recipe = recipeTable.getItem(key);

        if (recipe != null && Boolean.TRUE.equals(recipe.getIsDeleted())) {
            logger.debug("Recipe with ID {} is deleted", recipeId);
            return Optional.empty();
        }

        return Optional.ofNullable(recipe);
    }

    @Override
    public List<Recipe> findByAuthor(String authorId) {
        logger.debug("Finding recipes by author ID: {}", authorId);

        QueryConditional queryConditional = QueryConditional
                .keyEqualTo(Key.builder().partitionValue(authorId).build());

        Expression filterExpression = Expression.builder()
                .expression("IsDeleted = :isDeleted OR attribute_not_exists(IsDeleted)")
                .expressionValues(Map.of(":isDeleted", AttributeValue.builder().bool(false).build()))
                .build();

        QueryEnhancedRequest queryRequest = QueryEnhancedRequest.builder()
                .queryConditional(queryConditional)
                .filterExpression(filterExpression)
                .build();

        List<Recipe> recipes = new ArrayList<>();
        authorIndex.query(queryRequest)
                .stream()
                .flatMap(page -> page.items().stream())
                .forEach(recipes::add);

        logger.debug("Found {} recipes for author: {}", recipes.size(), authorId);
        return recipes;
    }

    @Override
    public List<Recipe> findAllPublic() {
        logger.debug("Finding all public recipes");

        Expression filterExpression = Expression.builder()
                .expression("IsPublic = :isPublic AND (IsDeleted = :isDeleted OR attribute_not_exists(IsDeleted))")
                .expressionValues(Map.of(
                        ":isPublic", AttributeValue.builder().bool(true).build(),
                        ":isDeleted", AttributeValue.builder().bool(false).build()
                ))
                .build();

        ScanEnhancedRequest scanRequest = ScanEnhancedRequest.builder()
                .filterExpression(filterExpression)
                .build();

        List<Recipe> recipes = new ArrayList<>();
        recipeTable.scan(scanRequest)
                .items()
                .forEach(recipes::add);

        logger.debug("Found {} public recipes", recipes.size());
        return recipes;
    }

    @Override
    public void delete(String recipeId, List<String> ingredientNames) {
        logger.debug("Deleting recipe with ID: {} and {} ingredients", recipeId, ingredientNames.size());

        TransactWriteItemsEnhancedRequest.Builder transactionBuilder = TransactWriteItemsEnhancedRequest.builder();

        Key recipeKey = Key.builder()
                .partitionValue(recipeId)
                .build();
        transactionBuilder.addDeleteItem(recipeTable, recipeKey);

        for (String ingredientName : ingredientNames) {
            Key ingredientKey = Key.builder()
                    .partitionValue(ingredientName)
                    .sortValue(recipeId)
                    .build();
            transactionBuilder.addDeleteItem(recipeIngredientTable, ingredientKey);
        }

        enhancedClient.transactWriteItems(transactionBuilder.build());
        logger.info("Recipe deleted successfully with ID: {}", recipeId);
    }
}
