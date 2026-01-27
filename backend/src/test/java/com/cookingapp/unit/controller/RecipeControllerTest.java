package com.cookingapp.unit.controller;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.cookingapp.application.usecase.recipe.CreateRecipeUseCase;
import com.cookingapp.application.usecase.recipe.DeleteRecipeUseCase;
import com.cookingapp.application.usecase.recipe.GetRecipeUseCase;
import com.cookingapp.application.usecase.recipe.SearchRecipesByIngredientsUseCase;
import com.cookingapp.application.usecase.recipe.SearchRecipesUseCase;
import com.cookingapp.application.usecase.recipe.UpdateRecipeUseCase;
import com.cookingapp.application.usecase.recipe.UploadRecipeImageUseCase;
import com.cookingapp.domain.entity.Recipe;
import com.cookingapp.domain.exception.RecipeNotFoundException;
import com.cookingapp.domain.exception.UnauthorizedException;
import com.cookingapp.domain.valueobject.Ingredient;
import com.cookingapp.domain.valueobject.Step;
import com.cookingapp.presentation.controller.RecipeController;
import com.cookingapp.presentation.dto.IngredientDto;
import com.cookingapp.presentation.dto.RecipeRequest;
import com.cookingapp.presentation.dto.StepDto;
import com.cookingapp.presentation.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * RecipeControllerのユニットテスト
 * MockMvcBuilders.standaloneSetupを使用してAWS SDKモック問題を回避
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RecipeController ユニットテスト")
class RecipeControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private CreateRecipeUseCase createRecipeUseCase;

    @Mock
    private UpdateRecipeUseCase updateRecipeUseCase;

    @Mock
    private DeleteRecipeUseCase deleteRecipeUseCase;

    @Mock
    private GetRecipeUseCase getRecipeUseCase;

    @Mock
    private SearchRecipesUseCase searchRecipesUseCase;

    @Mock
    private SearchRecipesByIngredientsUseCase searchRecipesByIngredientsUseCase;

    @Mock
    private UploadRecipeImageUseCase uploadRecipeImageUseCase;

    @Mock
    private MessageSource messageSource;

    private static final String RECIPES_ENDPOINT = "/api/recipes";
    private static final String TEST_RECIPE_ID = "recipe-123";
    private static final String TEST_AUTHOR_ID = "author-456";
    private static final String TEST_USER_ID = "user-789";

    @BeforeEach
    void setUp() {
        RecipeController recipeController = new RecipeController(
                createRecipeUseCase,
                updateRecipeUseCase,
                deleteRecipeUseCase,
                getRecipeUseCase,
                searchRecipesUseCase,
                searchRecipesByIngredientsUseCase,
                uploadRecipeImageUseCase);

        mockMvc = MockMvcBuilders.standaloneSetup(recipeController)
                .setControllerAdvice(new GlobalExceptionHandler(messageSource))
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules(); // LocalDateTime対応
    }

    @Nested
    @DisplayName("GET /api/recipes - レシピ検索")
    class SearchRecipes {

        @Nested
        @DisplayName("正常系")
        class Success {

            @Test
            @DisplayName("authorIdパラメータがある場合、executeByAuthorが呼ばれる")
            void shouldCallExecuteByAuthorWhenAuthorIdProvided() throws Exception {
                // Arrange
                Recipe recipe = createTestRecipe();
                when(searchRecipesUseCase.executeByAuthor(TEST_AUTHOR_ID)).thenReturn(List.of(recipe));

                // Act & Assert
                mockMvc.perform(get(RECIPES_ENDPOINT)
                        .param("authorId", TEST_AUTHOR_ID))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$[0].recipeId").value(TEST_RECIPE_ID))
                        .andExpect(jsonPath("$[0].title").value("テストレシピ"))
                        .andExpect(jsonPath("$[0].authorId").value(TEST_AUTHOR_ID));

                verify(searchRecipesUseCase).executeByAuthor(TEST_AUTHOR_ID);
                verify(searchRecipesUseCase, never()).executePublic();
            }

            @Test
            @DisplayName("keywordパラメータがある場合、executeByKeywordが呼ばれる")
            void shouldCallExecuteByKeywordWhenKeywordProvided() throws Exception {
                // Arrange
                Recipe recipe = createTestRecipe();
                when(searchRecipesUseCase.executeByKeyword("パスタ")).thenReturn(List.of(recipe));

                // Act & Assert
                mockMvc.perform(get(RECIPES_ENDPOINT)
                        .param("keyword", "パスタ"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$[0].recipeId").value(TEST_RECIPE_ID));

                verify(searchRecipesUseCase).executeByKeyword("パスタ");
                verify(searchRecipesUseCase, never()).executePublic();
            }

            @Test
            @DisplayName("パラメータがない場合、executePublicが呼ばれる")
            void shouldCallExecutePublicWhenNoParams() throws Exception {
                // Arrange
                Recipe recipe = createTestRecipe();
                when(searchRecipesUseCase.executePublic()).thenReturn(List.of(recipe));

                // Act & Assert
                mockMvc.perform(get(RECIPES_ENDPOINT))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$[0].recipeId").value(TEST_RECIPE_ID));

                verify(searchRecipesUseCase).executePublic();
            }

            @Test
            @DisplayName("レシピが存在しない場合、空のリストを返す")
            void shouldReturnEmptyListWhenNoRecipesFound() throws Exception {
                // Arrange
                when(searchRecipesUseCase.executePublic()).thenReturn(Collections.emptyList());

                // Act & Assert
                mockMvc.perform(get(RECIPES_ENDPOINT))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$").isArray())
                        .andExpect(jsonPath("$").isEmpty());
            }
        }
    }

    @Nested
    @DisplayName("GET /api/recipes/{id} - レシピ詳細取得")
    class GetRecipeById {

        @Nested
        @DisplayName("正常系")
        class Success {

            @Test
            @DisplayName("レシピが存在する場合、200 OKとレシピ情報を返す")
            void shouldReturn200WithRecipeWhenRecipeExists() throws Exception {
                // Arrange
                Recipe recipe = createTestRecipe();
                when(getRecipeUseCase.execute(TEST_RECIPE_ID)).thenReturn(recipe);

                // Act & Assert
                mockMvc.perform(get(RECIPES_ENDPOINT + "/" + TEST_RECIPE_ID))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.recipeId").value(TEST_RECIPE_ID))
                        .andExpect(jsonPath("$.title").value("テストレシピ"))
                        .andExpect(jsonPath("$.authorId").value(TEST_AUTHOR_ID))
                        .andExpect(jsonPath("$.cookingTime").value(30))
                        .andExpect(jsonPath("$.public").value(true));

                verify(getRecipeUseCase).execute(TEST_RECIPE_ID);
            }
        }

        @Nested
        @DisplayName("異常系")
        class Failure {

            @Test
            @DisplayName("レシピが存在しない場合、404 Not Foundを返す")
            void shouldReturn404WhenRecipeNotFound() throws Exception {
                // Arrange
                when(getRecipeUseCase.execute(TEST_RECIPE_ID))
                        .thenThrow(new RecipeNotFoundException("Recipe not found: " + TEST_RECIPE_ID));

                // Act & Assert
                mockMvc.perform(get(RECIPES_ENDPOINT + "/" + TEST_RECIPE_ID))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.code").value("RECIPE_NOT_FOUND"));

                verify(getRecipeUseCase).execute(TEST_RECIPE_ID);
            }
        }
    }

    @Nested
    @DisplayName("POST /api/recipes - レシピ作成")
    class CreateRecipe {

        @Nested
        @DisplayName("正常系")
        class Success {

            @Test
            @DisplayName("有効なリクエストでレシピ作成時、201 Createdを返す")
            void shouldReturn201WhenRecipeCreatedSuccessfully() throws Exception {
                // Arrange
                RecipeRequest request = createRecipeRequest();
                Recipe recipe = createTestRecipe();
                when(createRecipeUseCase.execute(
                        eq(TEST_USER_ID),
                        eq("テストレシピ"),
                        anyList(),
                        anyList(),
                        eq(30)))
                        .thenReturn(recipe);

                // Act & Assert
                mockMvc.perform(post(RECIPES_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", TEST_USER_ID)
                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.recipeId").value(TEST_RECIPE_ID))
                        .andExpect(jsonPath("$.title").value("テストレシピ"));

                verify(createRecipeUseCase).execute(
                        eq(TEST_USER_ID),
                        eq("テストレシピ"),
                        anyList(),
                        anyList(),
                        eq(30));
            }
        }

        @Nested
        @DisplayName("異常系")
        class Failure {

            @Test
            @DisplayName("X-User-Idヘッダーがない場合、400 Bad Requestを返す")
            void shouldReturn400WhenUserIdHeaderMissing() throws Exception {
                // Arrange
                RecipeRequest request = createRecipeRequest();

                // Act & Assert
                mockMvc.perform(post(RECIPES_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest());

                verify(createRecipeUseCase, never()).execute(
                        anyString(), anyString(), anyList(), anyList(), anyInt());
            }

            @Test
            @DisplayName("タイトルが空の場合、400 Bad Requestを返す")
            void shouldReturn400WhenTitleIsEmpty() throws Exception {
                // Arrange
                RecipeRequest request = createRecipeRequest();
                request.setTitle("");

                // Act & Assert
                mockMvc.perform(post(RECIPES_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", TEST_USER_ID)
                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest());

                verify(createRecipeUseCase, never()).execute(
                        anyString(), anyString(), anyList(), anyList(), anyInt());
            }
        }
    }

    @Nested
    @DisplayName("PUT /api/recipes/{id} - レシピ更新")
    class UpdateRecipe {

        @Nested
        @DisplayName("正常系")
        class Success {

            @Test
            @DisplayName("有効なリクエストでレシピ更新時、200 OKを返す")
            void shouldReturn200WhenRecipeUpdatedSuccessfully() throws Exception {
                // Arrange
                RecipeRequest request = createRecipeRequest();
                Recipe recipe = createTestRecipe();
                when(updateRecipeUseCase.execute(
                        eq(TEST_RECIPE_ID),
                        eq(TEST_USER_ID),
                        eq("テストレシピ"),
                        anyList(),
                        anyList(),
                        eq(30)))
                        .thenReturn(recipe);

                // Act & Assert
                mockMvc.perform(put(RECIPES_ENDPOINT + "/" + TEST_RECIPE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", TEST_USER_ID)
                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.recipeId").value(TEST_RECIPE_ID));

                verify(updateRecipeUseCase).execute(
                        eq(TEST_RECIPE_ID),
                        eq(TEST_USER_ID),
                        eq("テストレシピ"),
                        anyList(),
                        anyList(),
                        eq(30));
            }
        }

        @Nested
        @DisplayName("異常系")
        class Failure {

            @Test
            @DisplayName("レシピが存在しない場合、404 Not Foundを返す")
            void shouldReturn404WhenRecipeNotFound() throws Exception {
                // Arrange
                RecipeRequest request = createRecipeRequest();
                when(updateRecipeUseCase.execute(
                        eq(TEST_RECIPE_ID),
                        eq(TEST_USER_ID),
                        anyString(),
                        anyList(),
                        anyList(),
                        anyInt()))
                        .thenThrow(new RecipeNotFoundException("Recipe not found: " + TEST_RECIPE_ID));

                // Act & Assert
                mockMvc.perform(put(RECIPES_ENDPOINT + "/" + TEST_RECIPE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", TEST_USER_ID)
                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.code").value("RECIPE_NOT_FOUND"));
            }

            @Test
            @DisplayName("著者以外が更新しようとした場合、403 Forbiddenを返す")
            void shouldReturn403WhenUserIsNotAuthor() throws Exception {
                // Arrange
                RecipeRequest request = createRecipeRequest();
                when(updateRecipeUseCase.execute(
                        eq(TEST_RECIPE_ID),
                        eq(TEST_USER_ID),
                        anyString(),
                        anyList(),
                        anyList(),
                        anyInt()))
                        .thenThrow(new UnauthorizedException("User does not have permission"));

                // Act & Assert
                mockMvc.perform(put(RECIPES_ENDPOINT + "/" + TEST_RECIPE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", TEST_USER_ID)
                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isForbidden())
                        .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
            }
        }
    }

    @Nested
    @DisplayName("DELETE /api/recipes/{id} - レシピ削除")
    class DeleteRecipe {

        @Nested
        @DisplayName("正常系")
        class Success {

            @Test
            @DisplayName("レシピ削除成功時、204 No Contentを返す")
            void shouldReturn204WhenRecipeDeletedSuccessfully() throws Exception {
                // Arrange
                doNothing().when(deleteRecipeUseCase).execute(TEST_RECIPE_ID, TEST_USER_ID);

                // Act & Assert
                mockMvc.perform(delete(RECIPES_ENDPOINT + "/" + TEST_RECIPE_ID)
                        .header("X-User-Id", TEST_USER_ID))
                        .andExpect(status().isNoContent());

                verify(deleteRecipeUseCase).execute(TEST_RECIPE_ID, TEST_USER_ID);
            }
        }

        @Nested
        @DisplayName("異常系")
        class Failure {

            @Test
            @DisplayName("レシピが存在しない場合、404 Not Foundを返す")
            void shouldReturn404WhenRecipeNotFound() throws Exception {
                // Arrange
                doThrow(new RecipeNotFoundException("Recipe not found: " + TEST_RECIPE_ID))
                        .when(deleteRecipeUseCase).execute(TEST_RECIPE_ID, TEST_USER_ID);

                // Act & Assert
                mockMvc.perform(delete(RECIPES_ENDPOINT + "/" + TEST_RECIPE_ID)
                        .header("X-User-Id", TEST_USER_ID))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.code").value("RECIPE_NOT_FOUND"));
            }

            @Test
            @DisplayName("著者以外が削除しようとした場合、403 Forbiddenを返す")
            void shouldReturn403WhenUserIsNotAuthor() throws Exception {
                // Arrange
                doThrow(new UnauthorizedException("User does not have permission"))
                        .when(deleteRecipeUseCase).execute(TEST_RECIPE_ID, TEST_USER_ID);

                // Act & Assert
                mockMvc.perform(delete(RECIPES_ENDPOINT + "/" + TEST_RECIPE_ID)
                        .header("X-User-Id", TEST_USER_ID))
                        .andExpect(status().isForbidden())
                        .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
            }
        }
    }

    // ヘルパーメソッド

    private RecipeRequest createRecipeRequest() {
        RecipeRequest request = new RecipeRequest();
        request.setTitle("テストレシピ");
        request.setIngredients(createIngredientDtos());
        request.setSteps(createStepDtos());
        request.setCookingTime(30);
        return request;
    }

    private List<IngredientDto> createIngredientDtos() {
        IngredientDto ingredient1 = new IngredientDto();
        ingredient1.setName("小麦粉");
        ingredient1.setQuantity(new BigDecimal("200"));
        ingredient1.setUnit("g");
        ingredient1.setOptional(false);

        IngredientDto ingredient2 = new IngredientDto();
        ingredient2.setName("砂糖");
        ingredient2.setQuantity(new BigDecimal("100"));
        ingredient2.setUnit("g");
        ingredient2.setOptional(false);

        return List.of(ingredient1, ingredient2);
    }

    private List<StepDto> createStepDtos() {
        StepDto step1 = new StepDto();
        step1.setDescription("材料を混ぜる");

        StepDto step2 = new StepDto();
        step2.setDescription("30分焼く");

        return List.of(step1, step2);
    }

    private Recipe createTestRecipe() {
        List<Ingredient> ingredients = List.of(
                new Ingredient("小麦粉", new BigDecimal("200"), "g", null, false),
                new Ingredient("砂糖", new BigDecimal("100"), "g", null, false));

        List<Step> steps = List.of(
                new Step("材料を混ぜる", null, null),
                new Step("30分焼く", null, null));

        return new Recipe(
                TEST_RECIPE_ID,
                TEST_AUTHOR_ID,
                "テストレシピ",
                ingredients,
                steps,
                30,
                null,
                true,
                false,
                LocalDateTime.now(),
                LocalDateTime.now());
    }
}
