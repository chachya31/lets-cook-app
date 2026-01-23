package com.cookingapp.infrastructure.repository;

import com.cookingapp.domain.model.Ingredient;
import com.cookingapp.domain.model.Recipe;
import com.cookingapp.domain.model.RecipeIngredient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.TransactWriteItemsEnhancedRequest;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("DynamoDBRecipeRepository")
class DynamoDBRecipeRepositoryTest {

    @Mock
    private DynamoDbEnhancedClient enhancedClient;

    @Mock
    private DynamoDbTable<Recipe> recipeTable;

    @Mock
    private DynamoDbTable<RecipeIngredient> recipeIngredientTable;

    @Mock
    private DynamoDbIndex<Recipe> authorIndex;

    private DynamoDBRecipeRepository repository;

    private static final String RECIPES_TABLE_NAME = "Recipes";
    private static final String RECIPE_INGREDIENTS_TABLE_NAME = "RecipeIngredients";
    private static final String TEST_RECIPE_ID = "recipe-123";
    private static final String TEST_AUTHOR_ID = "author-456";

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        when(enhancedClient.table(eq(RECIPES_TABLE_NAME), any(TableSchema.class))).thenReturn(recipeTable);
        when(enhancedClient.table(eq(RECIPE_INGREDIENTS_TABLE_NAME), any(TableSchema.class))).thenReturn(recipeIngredientTable);
        when(recipeTable.index("GSI_Author")).thenReturn(authorIndex);

        // Mock tableSchema() to return actual schemas for TransactWriteItems
        when(recipeTable.tableSchema()).thenReturn(TableSchema.fromBean(Recipe.class));
        when(recipeIngredientTable.tableSchema()).thenReturn(TableSchema.fromBean(RecipeIngredient.class));

        repository = new DynamoDBRecipeRepository(enhancedClient, RECIPES_TABLE_NAME, RECIPE_INGREDIENTS_TABLE_NAME);
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("transactWriteItemsが呼ばれ、レシピと材料が保存される")
        void shouldCallTransactWriteItemsToSaveRecipeAndIngredients() {
            // Arrange
            Recipe recipe = createRecipeWithIngredients();

            // Act
            Recipe result = repository.save(recipe);

            // Assert
            ArgumentCaptor<TransactWriteItemsEnhancedRequest> requestCaptor =
                    ArgumentCaptor.forClass(TransactWriteItemsEnhancedRequest.class);
            verify(enhancedClient).transactWriteItems(requestCaptor.capture());

            assertThat(result).isEqualTo(recipe);
            assertThat(result.getRecipeId()).isEqualTo(TEST_RECIPE_ID);
        }

        @Test
        @DisplayName("材料がない場合でもレシピは保存される")
        void shouldSaveRecipeWithoutIngredients() {
            // Arrange
            Recipe recipe = createRecipeWithoutIngredients();

            // Act
            Recipe result = repository.save(recipe);

            // Assert
            verify(enhancedClient).transactWriteItems(any(TransactWriteItemsEnhancedRequest.class));
            assertThat(result).isEqualTo(recipe);
        }

        @Test
        @DisplayName("材料がnullの場合でもレシピは保存される")
        void shouldSaveRecipeWithNullIngredients() {
            // Arrange
            Recipe recipe = Recipe.builder()
                    .recipeId(TEST_RECIPE_ID)
                    .title("Test Recipe")
                    .authorId(TEST_AUTHOR_ID)
                    .ingredients(null)
                    .cookingTime(30)
                    .isPublic(true)
                    .isDeleted(false)
                    .createdAt("2024-01-01T00:00:00Z")
                    .updatedAt("2024-01-01T00:00:00Z")
                    .build();

            // Act
            Recipe result = repository.save(recipe);

            // Assert
            verify(enhancedClient).transactWriteItems(any(TransactWriteItemsEnhancedRequest.class));
            assertThat(result).isEqualTo(recipe);
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("IDに一致するレシピが存在する場合、そのレシピを返す")
        void shouldReturnRecipeWhenIdExists() {
            // Arrange
            Recipe expectedRecipe = createRecipeWithIngredients();
            when(recipeTable.getItem(any(Key.class))).thenReturn(expectedRecipe);

            // Act
            Optional<Recipe> result = repository.findById(TEST_RECIPE_ID);

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get().getRecipeId()).isEqualTo(TEST_RECIPE_ID);
            assertThat(result.get().getTitle()).isEqualTo("Test Recipe");

            verify(recipeTable).getItem(any(Key.class));
        }

        @Test
        @DisplayName("IDに一致するレシピが存在しない場合、空のOptionalを返す")
        void shouldReturnEmptyOptionalWhenIdNotFound() {
            // Arrange
            when(recipeTable.getItem(any(Key.class))).thenReturn(null);

            // Act
            Optional<Recipe> result = repository.findById("nonexistent-id");

            // Assert
            assertThat(result).isEmpty();
            verify(recipeTable).getItem(any(Key.class));
        }

        @Test
        @DisplayName("削除済みのレシピの場合、空のOptionalを返す")
        void shouldReturnEmptyOptionalWhenRecipeIsDeleted() {
            // Arrange
            Recipe deletedRecipe = Recipe.builder()
                    .recipeId(TEST_RECIPE_ID)
                    .title("Deleted Recipe")
                    .authorId(TEST_AUTHOR_ID)
                    .isDeleted(true)
                    .build();
            when(recipeTable.getItem(any(Key.class))).thenReturn(deletedRecipe);

            // Act
            Optional<Recipe> result = repository.findById(TEST_RECIPE_ID);

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("正しいキーでDynamoDBにアクセスする")
        void shouldUseCorrectKey() {
            // Arrange
            when(recipeTable.getItem(any(Key.class))).thenReturn(null);

            // Act
            repository.findById(TEST_RECIPE_ID);

            // Assert
            ArgumentCaptor<Key> keyCaptor = ArgumentCaptor.forClass(Key.class);
            verify(recipeTable).getItem(keyCaptor.capture());

            Key capturedKey = keyCaptor.getValue();
            assertThat(capturedKey.partitionKeyValue().s()).isEqualTo(TEST_RECIPE_ID);
        }
    }

    @Nested
    @DisplayName("findByAuthor")
    class FindByAuthor {

        @Test
        @DisplayName("著者IDに一致するレシピ一覧を返す")
        @SuppressWarnings("unchecked")
        void shouldReturnRecipesForAuthor() {
            // Arrange
            Recipe recipe = createRecipeWithIngredients();
            Page<Recipe> page = mock(Page.class);
            when(page.items()).thenReturn(List.of(recipe));

            SdkIterable<Page<Recipe>> sdkIterable = createPageSdkIterable(List.of(page));
            when(authorIndex.query(any(QueryEnhancedRequest.class))).thenReturn(sdkIterable);

            // Act
            List<Recipe> result = repository.findByAuthor(TEST_AUTHOR_ID);

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getRecipeId()).isEqualTo(TEST_RECIPE_ID);
            assertThat(result.get(0).getAuthorId()).isEqualTo(TEST_AUTHOR_ID);

            verify(authorIndex).query(any(QueryEnhancedRequest.class));
        }

        @Test
        @DisplayName("著者IDに一致するレシピがない場合、空のリストを返す")
        @SuppressWarnings("unchecked")
        void shouldReturnEmptyListWhenNoRecipesFound() {
            // Arrange
            Page<Recipe> page = mock(Page.class);
            when(page.items()).thenReturn(Collections.emptyList());

            SdkIterable<Page<Recipe>> sdkIterable = createPageSdkIterable(List.of(page));
            when(authorIndex.query(any(QueryEnhancedRequest.class))).thenReturn(sdkIterable);

            // Act
            List<Recipe> result = repository.findByAuthor(TEST_AUTHOR_ID);

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("クエリリクエストに正しいフィルタ式が含まれている")
        @SuppressWarnings("unchecked")
        void shouldUseCorrectFilterExpression() {
            // Arrange
            Page<Recipe> page = mock(Page.class);
            when(page.items()).thenReturn(Collections.emptyList());

            SdkIterable<Page<Recipe>> sdkIterable = createPageSdkIterable(List.of(page));
            when(authorIndex.query(any(QueryEnhancedRequest.class))).thenReturn(sdkIterable);

            // Act
            repository.findByAuthor(TEST_AUTHOR_ID);

            // Assert
            ArgumentCaptor<QueryEnhancedRequest> requestCaptor = ArgumentCaptor.forClass(QueryEnhancedRequest.class);
            verify(authorIndex).query(requestCaptor.capture());

            QueryEnhancedRequest capturedRequest = requestCaptor.getValue();
            assertThat(capturedRequest.filterExpression()).isNotNull();
            assertThat(capturedRequest.filterExpression().expression())
                    .contains("IsDeleted");
        }
    }

    @Nested
    @DisplayName("findAllPublic")
    class FindAllPublic {

        @Test
        @DisplayName("公開レシピ一覧を返す")
        @SuppressWarnings("unchecked")
        void shouldReturnPublicRecipes() {
            // Arrange
            Recipe recipe = createRecipeWithIngredients();
            PageIterable<Recipe> pageIterable = mock(PageIterable.class);
            SdkIterable<Recipe> sdkIterable = createSdkIterable(List.of(recipe));

            when(pageIterable.items()).thenReturn(sdkIterable);
            when(recipeTable.scan(any(ScanEnhancedRequest.class))).thenReturn(pageIterable);

            // Act
            List<Recipe> result = repository.findAllPublic();

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getIsPublic()).isTrue();

            verify(recipeTable).scan(any(ScanEnhancedRequest.class));
        }

        @Test
        @DisplayName("公開レシピがない場合、空のリストを返す")
        @SuppressWarnings("unchecked")
        void shouldReturnEmptyListWhenNoPublicRecipes() {
            // Arrange
            PageIterable<Recipe> pageIterable = mock(PageIterable.class);
            SdkIterable<Recipe> sdkIterable = createSdkIterable(Collections.emptyList());

            when(pageIterable.items()).thenReturn(sdkIterable);
            when(recipeTable.scan(any(ScanEnhancedRequest.class))).thenReturn(pageIterable);

            // Act
            List<Recipe> result = repository.findAllPublic();

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("スキャンリクエストに正しいフィルタ式が含まれている")
        @SuppressWarnings("unchecked")
        void shouldUseCorrectFilterExpression() {
            // Arrange
            PageIterable<Recipe> pageIterable = mock(PageIterable.class);
            SdkIterable<Recipe> sdkIterable = createSdkIterable(Collections.emptyList());

            when(pageIterable.items()).thenReturn(sdkIterable);
            when(recipeTable.scan(any(ScanEnhancedRequest.class))).thenReturn(pageIterable);

            // Act
            repository.findAllPublic();

            // Assert
            ArgumentCaptor<ScanEnhancedRequest> requestCaptor = ArgumentCaptor.forClass(ScanEnhancedRequest.class);
            verify(recipeTable).scan(requestCaptor.capture());

            ScanEnhancedRequest capturedRequest = requestCaptor.getValue();
            assertThat(capturedRequest.filterExpression()).isNotNull();
            assertThat(capturedRequest.filterExpression().expression())
                    .contains("IsPublic")
                    .contains("IsDeleted");
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("transactWriteItemsが呼ばれ、レシピと材料が削除される")
        void shouldCallTransactWriteItemsToDeleteRecipeAndIngredients() {
            // Arrange
            List<String> ingredientNames = List.of("Flour", "Sugar");

            // Act
            repository.delete(TEST_RECIPE_ID, ingredientNames);

            // Assert
            ArgumentCaptor<TransactWriteItemsEnhancedRequest> requestCaptor =
                    ArgumentCaptor.forClass(TransactWriteItemsEnhancedRequest.class);
            verify(enhancedClient).transactWriteItems(requestCaptor.capture());
        }

        @Test
        @DisplayName("材料がない場合でもレシピは削除される")
        void shouldDeleteRecipeWithoutIngredients() {
            // Arrange
            List<String> ingredientNames = Collections.emptyList();

            // Act
            repository.delete(TEST_RECIPE_ID, ingredientNames);

            // Assert
            verify(enhancedClient).transactWriteItems(any(TransactWriteItemsEnhancedRequest.class));
        }
    }

    // Helper methods

    private Recipe createRecipeWithIngredients() {
        List<Ingredient> ingredients = List.of(
                Ingredient.builder().name("Flour").quantity("200").unit("g").build(),
                Ingredient.builder().name("Sugar").quantity("100").unit("g").build()
        );

        return Recipe.builder()
                .recipeId(TEST_RECIPE_ID)
                .title("Test Recipe")
                .authorId(TEST_AUTHOR_ID)
                .ingredients(ingredients)
                .cookingTime(30)
                .isPublic(true)
                .isDeleted(false)
                .createdAt("2024-01-01T00:00:00Z")
                .updatedAt("2024-01-01T00:00:00Z")
                .build();
    }

    private Recipe createRecipeWithoutIngredients() {
        return Recipe.builder()
                .recipeId(TEST_RECIPE_ID)
                .title("Test Recipe")
                .authorId(TEST_AUTHOR_ID)
                .ingredients(Collections.emptyList())
                .cookingTime(30)
                .isPublic(true)
                .isDeleted(false)
                .createdAt("2024-01-01T00:00:00Z")
                .updatedAt("2024-01-01T00:00:00Z")
                .build();
    }

    private <T> SdkIterable<T> createSdkIterable(List<T> items) {
        return new SdkIterable<T>() {
            @Override
            public Iterator<T> iterator() {
                return items.iterator();
            }

            @Override
            public Stream<T> stream() {
                return items.stream();
            }
        };
    }

    private <T> SdkIterable<Page<T>> createPageSdkIterable(List<Page<T>> pages) {
        return new SdkIterable<Page<T>>() {
            @Override
            public Iterator<Page<T>> iterator() {
                return pages.iterator();
            }

            @Override
            public Stream<Page<T>> stream() {
                return pages.stream();
            }
        };
    }
}
