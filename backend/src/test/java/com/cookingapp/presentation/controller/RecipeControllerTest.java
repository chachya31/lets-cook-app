package com.cookingapp.presentation.controller;

import com.cookingapp.application.dto.CreateRecipeInput;
import com.cookingapp.application.dto.RecipeOutput;
import com.cookingapp.application.exception.RecipeNotFoundException;
import com.cookingapp.application.exception.UnauthorizedRecipeAccessException;
import com.cookingapp.application.usecase.RecipeUseCase;
import com.cookingapp.presentation.dto.IngredientDto;
import com.cookingapp.presentation.dto.RecipeRequest;
import com.cookingapp.presentation.dto.StepDto;
import com.cookingapp.presentation.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
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

@ExtendWith(MockitoExtension.class)
@DisplayName("RecipeController")
class RecipeControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private RecipeUseCase recipeUseCase;

    private static final String RECIPES_ENDPOINT = "/api/recipes";
    private static final String TEST_RECIPE_ID = "recipe-123";
    private static final String TEST_AUTHOR_ID = "author-456";
    private static final String TEST_USER_ID = "user-789";
    private static final String TEST_PRESIGNED_URL = "https://s3.example.com/presigned-url";

    @BeforeEach
    void setUp() {
        RecipeController recipeController = new RecipeController(recipeUseCase);
        mockMvc = MockMvcBuilders.standaloneSetup(recipeController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Nested
    @DisplayName("GET /api/recipes")
    class GetRecipes {

        @Nested
        @DisplayName("正常系")
        class Success {

            @Test
            @DisplayName("authorIdパラメータがある場合、getRecipesByAuthorが呼ばれる")
            void shouldCallGetRecipesByAuthorWhenAuthorIdProvided() throws Exception {
                // Arrange
                RecipeOutput output = createRecipeOutput();
                when(recipeUseCase.getRecipesByAuthor(eq(TEST_AUTHOR_ID), eq(TEST_USER_ID))).thenReturn(List.of(output));

                // Act & Assert
                mockMvc.perform(get(RECIPES_ENDPOINT)
                                .param("authorId", TEST_AUTHOR_ID)
                                .principal(createPrincipal()))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$[0].recipeId").value(TEST_RECIPE_ID))
                        .andExpect(jsonPath("$[0].title").value("Test Recipe"))
                        .andExpect(jsonPath("$[0].authorId").value(TEST_AUTHOR_ID));

                verify(recipeUseCase).getRecipesByAuthor(eq(TEST_AUTHOR_ID), eq(TEST_USER_ID));
                verify(recipeUseCase, never()).getPublicRecipes();
            }

            @Test
            @DisplayName("authorIdパラメータがない場合、getPublicRecipesが呼ばれる")
            void shouldCallGetPublicRecipesWhenAuthorIdNotProvided() throws Exception {
                // Arrange
                RecipeOutput output = createRecipeOutput();
                when(recipeUseCase.getPublicRecipes()).thenReturn(List.of(output));

                // Act & Assert
                mockMvc.perform(get(RECIPES_ENDPOINT)
                                .principal(createPrincipal()))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$[0].recipeId").value(TEST_RECIPE_ID));

                verify(recipeUseCase).getPublicRecipes();
                verify(recipeUseCase, never()).getRecipesByAuthor(anyString(), anyString());
            }

            @Test
            @DisplayName("authorIdが空文字の場合、getPublicRecipesが呼ばれる")
            void shouldCallGetPublicRecipesWhenAuthorIdIsEmpty() throws Exception {
                // Arrange
                RecipeOutput output = createRecipeOutput();
                when(recipeUseCase.getPublicRecipes()).thenReturn(List.of(output));

                // Act & Assert
                mockMvc.perform(get(RECIPES_ENDPOINT)
                                .param("authorId", "")
                                .principal(createPrincipal()))
                        .andExpect(status().isOk());

                verify(recipeUseCase).getPublicRecipes();
                verify(recipeUseCase, never()).getRecipesByAuthor(anyString(), anyString());
            }

            @Test
            @DisplayName("レシピが存在しない場合、空のリストを返す")
            void shouldReturnEmptyListWhenNoRecipesFound() throws Exception {
                // Arrange
                when(recipeUseCase.getPublicRecipes()).thenReturn(Collections.emptyList());

                // Act & Assert
                mockMvc.perform(get(RECIPES_ENDPOINT)
                                .principal(createPrincipal()))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$").isArray())
                        .andExpect(jsonPath("$").isEmpty());
            }
        }
    }

    @Nested
    @DisplayName("GET /api/recipes/{id}")
    class GetRecipeById {

        @Nested
        @DisplayName("正常系")
        class Success {

            @Test
            @DisplayName("レシピが存在する場合、200 OKとレシピ情報を返す")
            void shouldReturn200WithRecipeWhenRecipeExists() throws Exception {
                // Arrange
                RecipeOutput output = createRecipeOutput();
                when(recipeUseCase.getRecipeById(eq(TEST_RECIPE_ID), any())).thenReturn(output);

                // Act & Assert
                mockMvc.perform(get(RECIPES_ENDPOINT + "/" + TEST_RECIPE_ID))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.recipeId").value(TEST_RECIPE_ID))
                        .andExpect(jsonPath("$.title").value("Test Recipe"))
                        .andExpect(jsonPath("$.authorId").value(TEST_AUTHOR_ID))
                        .andExpect(jsonPath("$.cookingTime").value(30))
                        .andExpect(jsonPath("$.isPublic").value(true))
                        .andExpect(jsonPath("$.imageUrl").value(TEST_PRESIGNED_URL));

                verify(recipeUseCase).getRecipeById(eq(TEST_RECIPE_ID), any());
            }
        }

        @Nested
        @DisplayName("異常系")
        class Failure {

            @Test
            @DisplayName("レシピが存在しない場合、404 Not Foundを返す")
            void shouldReturn404WhenRecipeNotFound() throws Exception {
                // Arrange
                when(recipeUseCase.getRecipeById(eq(TEST_RECIPE_ID), any()))
                        .thenThrow(new RecipeNotFoundException(TEST_RECIPE_ID));

                // Act & Assert
                mockMvc.perform(get(RECIPES_ENDPOINT + "/" + TEST_RECIPE_ID))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.code").value("RECIPE_NOT_FOUND"));

                verify(recipeUseCase).getRecipeById(eq(TEST_RECIPE_ID), any());
            }
        }
    }

    @Nested
    @DisplayName("POST /api/recipes")
    class CreateRecipe {

        @Nested
        @DisplayName("正常系")
        class Success {

            @Test
            @DisplayName("有効なリクエストでレシピ作成時、201 Createdを返す")
            void shouldReturn201WhenRecipeCreatedSuccessfully() throws Exception {
                // Arrange
                RecipeRequest request = createRecipeRequest();
                RecipeOutput output = createRecipeOutput();
                when(recipeUseCase.createRecipe(any(CreateRecipeInput.class), eq(TEST_USER_ID)))
                        .thenReturn(output);

                // Act & Assert
                mockMvc.perform(post(RECIPES_ENDPOINT)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .principal(createPrincipal()))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.recipeId").value(TEST_RECIPE_ID))
                        .andExpect(jsonPath("$.title").value("Test Recipe"));

                verify(recipeUseCase).createRecipe(any(CreateRecipeInput.class), eq(TEST_USER_ID));
            }
        }

        @Nested
        @DisplayName("異常系")
        class Failure {

            @Test
            @DisplayName("認証されていない場合、401 Unauthorizedを返す")
            void shouldReturn401WhenNotAuthenticated() throws Exception {
                // Arrange
                RecipeRequest request = createRecipeRequest();

                // Act & Assert
                mockMvc.perform(post(RECIPES_ENDPOINT)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isUnauthorized())
                        .andExpect(jsonPath("$.code").value("USER_NOT_AUTHENTICATED"));

                verify(recipeUseCase, never()).createRecipe(any(), anyString());
            }
        }
    }

    @Nested
    @DisplayName("PUT /api/recipes/{id}")
    class UpdateRecipe {

        @Nested
        @DisplayName("正常系")
        class Success {

            @Test
            @DisplayName("有効なリクエストでレシピ更新時、200 OKを返す")
            void shouldReturn200WhenRecipeUpdatedSuccessfully() throws Exception {
                // Arrange
                RecipeRequest request = createRecipeRequest();
                RecipeOutput output = createRecipeOutput();
                when(recipeUseCase.updateRecipe(eq(TEST_RECIPE_ID), any(CreateRecipeInput.class), eq(TEST_USER_ID)))
                        .thenReturn(output);

                // Act & Assert
                mockMvc.perform(put(RECIPES_ENDPOINT + "/" + TEST_RECIPE_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .principal(createPrincipal()))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.recipeId").value(TEST_RECIPE_ID));

                verify(recipeUseCase).updateRecipe(eq(TEST_RECIPE_ID), any(CreateRecipeInput.class), eq(TEST_USER_ID));
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
                when(recipeUseCase.updateRecipe(eq(TEST_RECIPE_ID), any(CreateRecipeInput.class), eq(TEST_USER_ID)))
                        .thenThrow(new RecipeNotFoundException(TEST_RECIPE_ID));

                // Act & Assert
                mockMvc.perform(put(RECIPES_ENDPOINT + "/" + TEST_RECIPE_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .principal(createPrincipal()))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.code").value("RECIPE_NOT_FOUND"));
            }

            @Test
            @DisplayName("著者以外が更新しようとした場合、403 Forbiddenを返す")
            void shouldReturn403WhenUserIsNotAuthor() throws Exception {
                // Arrange
                RecipeRequest request = createRecipeRequest();
                when(recipeUseCase.updateRecipe(eq(TEST_RECIPE_ID), any(CreateRecipeInput.class), eq(TEST_USER_ID)))
                        .thenThrow(new UnauthorizedRecipeAccessException(TEST_RECIPE_ID));

                // Act & Assert
                mockMvc.perform(put(RECIPES_ENDPOINT + "/" + TEST_RECIPE_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .principal(createPrincipal()))
                        .andExpect(status().isForbidden())
                        .andExpect(jsonPath("$.code").value("UNAUTHORIZED_RECIPE_ACCESS"));
            }
        }
    }

    @Nested
    @DisplayName("DELETE /api/recipes/{id}")
    class DeleteRecipe {

        @Nested
        @DisplayName("正常系")
        class Success {

            @Test
            @DisplayName("レシピ削除成功時、204 No Contentを返す")
            void shouldReturn204WhenRecipeDeletedSuccessfully() throws Exception {
                // Arrange
                doNothing().when(recipeUseCase).deleteRecipe(TEST_RECIPE_ID, TEST_USER_ID);

                // Act & Assert
                mockMvc.perform(delete(RECIPES_ENDPOINT + "/" + TEST_RECIPE_ID)
                                .principal(createPrincipal()))
                        .andExpect(status().isNoContent());

                verify(recipeUseCase).deleteRecipe(TEST_RECIPE_ID, TEST_USER_ID);
            }
        }

        @Nested
        @DisplayName("異常系")
        class Failure {

            @Test
            @DisplayName("レシピが存在しない場合、404 Not Foundを返す")
            void shouldReturn404WhenRecipeNotFound() throws Exception {
                // Arrange
                doThrow(new RecipeNotFoundException(TEST_RECIPE_ID))
                        .when(recipeUseCase).deleteRecipe(TEST_RECIPE_ID, TEST_USER_ID);

                // Act & Assert
                mockMvc.perform(delete(RECIPES_ENDPOINT + "/" + TEST_RECIPE_ID)
                                .principal(createPrincipal()))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.code").value("RECIPE_NOT_FOUND"));
            }

            @Test
            @DisplayName("著者以外が削除しようとした場合、403 Forbiddenを返す")
            void shouldReturn403WhenUserIsNotAuthor() throws Exception {
                // Arrange
                doThrow(new UnauthorizedRecipeAccessException(TEST_RECIPE_ID))
                        .when(recipeUseCase).deleteRecipe(TEST_RECIPE_ID, TEST_USER_ID);

                // Act & Assert
                mockMvc.perform(delete(RECIPES_ENDPOINT + "/" + TEST_RECIPE_ID)
                                .principal(createPrincipal()))
                        .andExpect(status().isForbidden())
                        .andExpect(jsonPath("$.code").value("UNAUTHORIZED_RECIPE_ACCESS"));
            }
        }
    }

    // Helper methods

    private Principal createPrincipal() {
        return () -> TEST_USER_ID;
    }

    private RecipeRequest createRecipeRequest() {
        return RecipeRequest.builder()
                .title("Test Recipe")
                .ingredients(createIngredients())
                .steps(createSteps())
                .cookingTime(30)
                .isPublic(true)
                .imageKey("images/recipe/test-image.jpg")
                .build();
    }

    private List<IngredientDto> createIngredients() {
        return List.of(
                IngredientDto.builder().name("Flour").quantity("200").unit("g").build(),
                IngredientDto.builder().name("Sugar").quantity("100").unit("g").build()
        );
    }

    private List<StepDto> createSteps() {
        return List.of(
                StepDto.builder().stepNumber(1).description("Mix ingredients").build(),
                StepDto.builder().stepNumber(2).description("Bake for 30 minutes").build()
        );
    }

    private RecipeOutput createRecipeOutput() {
        List<RecipeOutput.IngredientOutput> ingredients = List.of(
                RecipeOutput.IngredientOutput.builder().name("Flour").quantity("200").unit("g").build(),
                RecipeOutput.IngredientOutput.builder().name("Sugar").quantity("100").unit("g").build()
        );
        List<RecipeOutput.StepOutput> steps = List.of(
                RecipeOutput.StepOutput.builder().stepNumber(1).description("Mix ingredients").build(),
                RecipeOutput.StepOutput.builder().stepNumber(2).description("Bake for 30 minutes").build()
        );

        return RecipeOutput.builder()
                .recipeId(TEST_RECIPE_ID)
                .title("Test Recipe")
                .authorId(TEST_AUTHOR_ID)
                .ingredients(ingredients)
                .steps(steps)
                .cookingTime(30)
                .isPublic(true)
                .imageUrl(TEST_PRESIGNED_URL)
                .createdAt("2024-01-01T00:00:00Z")
                .updatedAt("2024-01-01T00:00:00Z")
                .build();
    }
}
