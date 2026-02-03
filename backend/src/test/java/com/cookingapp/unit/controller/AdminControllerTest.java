package com.cookingapp.unit.controller;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

import com.cookingapp.application.usecase.admin.DeleteRecipeByAdminUseCase;
import com.cookingapp.application.usecase.admin.DeleteUserByAdminUseCase;
import com.cookingapp.application.usecase.admin.GetAdminDashboardStatsUseCase;
import com.cookingapp.application.usecase.admin.GetAllRecipesForAdminUseCase;
import com.cookingapp.application.usecase.admin.GetAllUsersUseCase;
import com.cookingapp.application.usecase.admin.SetRecipeStatusUseCase;
import com.cookingapp.application.usecase.admin.SuspendUserUseCase;
import com.cookingapp.domain.entity.Recipe;
import com.cookingapp.domain.exception.RecipeNotFoundException;
import com.cookingapp.domain.exception.UserNotFoundException;
import com.cookingapp.domain.valueobject.Ingredient;
import com.cookingapp.domain.valueobject.Language;
import com.cookingapp.domain.valueobject.Step;
import com.cookingapp.presentation.controller.AdminController;
import com.cookingapp.presentation.dto.UserResponse;
import com.cookingapp.presentation.dto.request.SetRecipeStatusRequest;
import com.cookingapp.presentation.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * AdminControllerのユニットテスト
 * MockMvcBuilders.standaloneSetupを使用してAWS SDKモック問題を回避
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AdminController ユニットテスト")
class AdminControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private GetAdminDashboardStatsUseCase getAdminDashboardStatsUseCase;

    @Mock
    private SuspendUserUseCase suspendUserUseCase;

    @Mock
    private DeleteUserByAdminUseCase deleteUserByAdminUseCase;

    @Mock
    private GetAllRecipesForAdminUseCase getAllRecipesForAdminUseCase;

    @Mock
    private GetAllUsersUseCase getAllUsersUseCase;

    @Mock
    private SetRecipeStatusUseCase setRecipeStatusUseCase;

    @Mock
    private DeleteRecipeByAdminUseCase deleteRecipeByAdminUseCase;

    @Mock
    private MessageSource messageSource;

    private static final String ADMIN_ENDPOINT = "/api/admin";
    private static final String TEST_USER_ID = "user-123";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_NICKNAME = "testuser";
    private static final String TEST_RECIPE_ID = "recipe-123";
    private static final String TEST_AUTHOR_ID = "author-456";

    @BeforeEach
    void setUp() {
        AdminController adminController = new AdminController(
                getAdminDashboardStatsUseCase,
                suspendUserUseCase,
                deleteUserByAdminUseCase,
                getAllRecipesForAdminUseCase,
                getAllUsersUseCase,
                setRecipeStatusUseCase,
                deleteRecipeByAdminUseCase);

        mockMvc = MockMvcBuilders.standaloneSetup(adminController)
                .setControllerAdvice(new GlobalExceptionHandler(messageSource))
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    @Nested
    @DisplayName("GET /api/admin/dashboard - ダッシュボード統計取得")
    class GetDashboard {

        @Nested
        @DisplayName("正常系")
        class Success {

            @Test
            @DisplayName("ダッシュボード統計を正常に取得する")
            void shouldReturn200WithDashboardStats() throws Exception {
                // Arrange
                GetAdminDashboardStatsUseCase.AdminDashboardStats stats =
                        new GetAdminDashboardStatsUseCase.AdminDashboardStats(
                                "統計情報は現在開発中です", 10, 25);

                when(getAdminDashboardStatsUseCase.execute()).thenReturn(stats);

                // Act & Assert
                mockMvc.perform(get(ADMIN_ENDPOINT + "/dashboard"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.message").value("統計情報は現在開発中です"))
                        .andExpect(jsonPath("$.totalUsers").value(10))
                        .andExpect(jsonPath("$.totalRecipes").value(25));

                verify(getAdminDashboardStatsUseCase).execute();
            }
        }
    }

    @Nested
    @DisplayName("GET /api/admin/users - 全ユーザー取得")
    class GetAllUsers {

        @Nested
        @DisplayName("正常系")
        class Success {

            @Test
            @DisplayName("全ユーザーを正常に取得する")
            void shouldReturn200WithAllUsers() throws Exception {
                // Arrange
                UserResponse user1 = createTestUserResponse(TEST_USER_ID, TEST_EMAIL, TEST_NICKNAME);
                UserResponse user2 = createTestUserResponse("user-456", "user2@example.com", "user2");
                List<UserResponse> users = List.of(user1, user2);

                when(getAllUsersUseCase.execute()).thenReturn(users);

                // Act & Assert
                mockMvc.perform(get(ADMIN_ENDPOINT + "/users"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$[0].userId").value(TEST_USER_ID))
                        .andExpect(jsonPath("$[0].email").value(TEST_EMAIL))
                        .andExpect(jsonPath("$[1].userId").value("user-456"))
                        .andExpect(jsonPath("$[1].email").value("user2@example.com"));

                verify(getAllUsersUseCase).execute();
            }

            @Test
            @DisplayName("ユーザーが存在しない場合、空のリストを返す")
            void shouldReturnEmptyListWhenNoUsers() throws Exception {
                // Arrange
                when(getAllUsersUseCase.execute()).thenReturn(Collections.emptyList());

                // Act & Assert
                mockMvc.perform(get(ADMIN_ENDPOINT + "/users"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$").isArray())
                        .andExpect(jsonPath("$").isEmpty());

                verify(getAllUsersUseCase).execute();
            }
        }
    }

    @Nested
    @DisplayName("PUT /api/admin/users/{userId}/suspend - ユーザー停止")
    class SuspendUser {

        @Nested
        @DisplayName("正常系")
        class Success {

            @Test
            @DisplayName("ユーザー停止が成功する")
            void shouldReturn204WhenSuspendSuccessful() throws Exception {
                // Arrange
                doNothing().when(suspendUserUseCase).execute(TEST_USER_ID);

                // Act & Assert
                mockMvc.perform(put(ADMIN_ENDPOINT + "/users/" + TEST_USER_ID + "/suspend"))
                        .andExpect(status().isNoContent());

                verify(suspendUserUseCase).execute(TEST_USER_ID);
            }
        }

        @Nested
        @DisplayName("異常系")
        class Failure {

            @Test
            @DisplayName("ユーザーが存在しない場合、404 Not Foundを返す")
            void shouldReturn404WhenUserNotFound() throws Exception {
                // Arrange
                doThrow(new UserNotFoundException("ユーザーが見つかりません: " + TEST_USER_ID))
                        .when(suspendUserUseCase).execute(TEST_USER_ID);

                // Act & Assert
                mockMvc.perform(put(ADMIN_ENDPOINT + "/users/" + TEST_USER_ID + "/suspend"))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"));

                verify(suspendUserUseCase).execute(TEST_USER_ID);
            }
        }
    }

    @Nested
    @DisplayName("DELETE /api/admin/users/{userId} - ユーザー削除")
    class DeleteUser {

        @Nested
        @DisplayName("正常系")
        class Success {

            @Test
            @DisplayName("ユーザー削除が成功する")
            void shouldReturn204WhenDeleteSuccessful() throws Exception {
                // Arrange
                doNothing().when(deleteUserByAdminUseCase).execute(TEST_USER_ID);

                // Act & Assert
                mockMvc.perform(delete(ADMIN_ENDPOINT + "/users/" + TEST_USER_ID))
                        .andExpect(status().isNoContent());

                verify(deleteUserByAdminUseCase).execute(TEST_USER_ID);
            }
        }

        @Nested
        @DisplayName("異常系")
        class Failure {

            @Test
            @DisplayName("ユーザーが存在しない場合、404 Not Foundを返す")
            void shouldReturn404WhenUserNotFound() throws Exception {
                // Arrange
                doThrow(new UserNotFoundException("ユーザーが見つかりません: " + TEST_USER_ID))
                        .when(deleteUserByAdminUseCase).execute(TEST_USER_ID);

                // Act & Assert
                mockMvc.perform(delete(ADMIN_ENDPOINT + "/users/" + TEST_USER_ID))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"));

                verify(deleteUserByAdminUseCase).execute(TEST_USER_ID);
            }
        }
    }

    @Nested
    @DisplayName("GET /api/admin/recipes - 全レシピ取得")
    class GetAllRecipes {

        @Nested
        @DisplayName("正常系")
        class Success {

            @Test
            @DisplayName("全レシピを正常に取得する")
            void shouldReturn200WithAllRecipes() throws Exception {
                // Arrange
                Recipe recipe = createTestRecipe();
                when(getAllRecipesForAdminUseCase.execute()).thenReturn(List.of(recipe));

                // Act & Assert
                mockMvc.perform(get(ADMIN_ENDPOINT + "/recipes"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$[0].recipeId").value(TEST_RECIPE_ID))
                        .andExpect(jsonPath("$[0].title").value("テストレシピ"))
                        .andExpect(jsonPath("$[0].authorId").value(TEST_AUTHOR_ID));

                verify(getAllRecipesForAdminUseCase).execute();
            }

            @Test
            @DisplayName("レシピが存在しない場合、空のリストを返す")
            void shouldReturnEmptyListWhenNoRecipes() throws Exception {
                // Arrange
                when(getAllRecipesForAdminUseCase.execute()).thenReturn(Collections.emptyList());

                // Act & Assert
                mockMvc.perform(get(ADMIN_ENDPOINT + "/recipes"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$").isArray())
                        .andExpect(jsonPath("$").isEmpty());

                verify(getAllRecipesForAdminUseCase).execute();
            }
        }
    }

    @Nested
    @DisplayName("PUT /api/admin/recipes/{recipeId}/status - レシピステータス設定")
    class SetRecipeStatus {

        @Nested
        @DisplayName("正常系")
        class Success {

            @Test
            @DisplayName("レシピを公開状態に設定する")
            void shouldReturn200WhenSettingRecipeToPublic() throws Exception {
                // Arrange
                SetRecipeStatusRequest request = new SetRecipeStatusRequest(true);
                Recipe recipe = createTestRecipe();
                when(setRecipeStatusUseCase.execute(TEST_RECIPE_ID, true)).thenReturn(recipe);

                // Act & Assert
                mockMvc.perform(put(ADMIN_ENDPOINT + "/recipes/" + TEST_RECIPE_ID + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.recipeId").value(TEST_RECIPE_ID))
                        .andExpect(jsonPath("$.title").value("テストレシピ"));

                verify(setRecipeStatusUseCase).execute(TEST_RECIPE_ID, true);
            }

            @Test
            @DisplayName("レシピを非公開状態に設定する")
            void shouldReturn200WhenSettingRecipeToPrivate() throws Exception {
                // Arrange
                SetRecipeStatusRequest request = new SetRecipeStatusRequest(false);
                Recipe recipe = createTestRecipe();
                recipe.setPublic(false);
                when(setRecipeStatusUseCase.execute(TEST_RECIPE_ID, false)).thenReturn(recipe);

                // Act & Assert
                mockMvc.perform(put(ADMIN_ENDPOINT + "/recipes/" + TEST_RECIPE_ID + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.recipeId").value(TEST_RECIPE_ID));

                verify(setRecipeStatusUseCase).execute(TEST_RECIPE_ID, false);
            }
        }

        @Nested
        @DisplayName("異常系")
        class Failure {

            @Test
            @DisplayName("レシピが存在しない場合、404 Not Foundを返す")
            void shouldReturn404WhenRecipeNotFound() throws Exception {
                // Arrange
                SetRecipeStatusRequest request = new SetRecipeStatusRequest(true);
                when(setRecipeStatusUseCase.execute(eq(TEST_RECIPE_ID), anyBoolean()))
                        .thenThrow(new RecipeNotFoundException("レシピが見つかりません: " + TEST_RECIPE_ID));

                // Act & Assert
                mockMvc.perform(put(ADMIN_ENDPOINT + "/recipes/" + TEST_RECIPE_ID + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.code").value("RECIPE_NOT_FOUND"));

                verify(setRecipeStatusUseCase).execute(eq(TEST_RECIPE_ID), anyBoolean());
            }

            @Test
            @DisplayName("isPublicがnullの場合、400 Bad Requestを返す")
            void shouldReturn400WhenIsPublicIsNull() throws Exception {
                // Arrange - isPublicがnullのリクエスト
                String requestBody = "{}";

                // Act & Assert
                mockMvc.perform(put(ADMIN_ENDPOINT + "/recipes/" + TEST_RECIPE_ID + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                        .andExpect(status().isBadRequest());

                verify(setRecipeStatusUseCase, never()).execute(anyString(), anyBoolean());
            }
        }
    }

    @Nested
    @DisplayName("DELETE /api/admin/recipes/{recipeId} - レシピ削除")
    class DeleteRecipe {

        @Nested
        @DisplayName("正常系")
        class Success {

            @Test
            @DisplayName("レシピ削除が成功する")
            void shouldReturn204WhenDeleteSuccessful() throws Exception {
                // Arrange
                doNothing().when(deleteRecipeByAdminUseCase).execute(TEST_RECIPE_ID);

                // Act & Assert
                mockMvc.perform(delete(ADMIN_ENDPOINT + "/recipes/" + TEST_RECIPE_ID))
                        .andExpect(status().isNoContent());

                verify(deleteRecipeByAdminUseCase).execute(TEST_RECIPE_ID);
            }
        }

        @Nested
        @DisplayName("異常系")
        class Failure {

            @Test
            @DisplayName("レシピが存在しない場合、404 Not Foundを返す")
            void shouldReturn404WhenRecipeNotFound() throws Exception {
                // Arrange
                doThrow(new RecipeNotFoundException("レシピが見つかりません: " + TEST_RECIPE_ID))
                        .when(deleteRecipeByAdminUseCase).execute(TEST_RECIPE_ID);

                // Act & Assert
                mockMvc.perform(delete(ADMIN_ENDPOINT + "/recipes/" + TEST_RECIPE_ID))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.code").value("RECIPE_NOT_FOUND"));

                verify(deleteRecipeByAdminUseCase).execute(TEST_RECIPE_ID);
            }
        }
    }

    // ヘルパーメソッド

    private UserResponse createTestUserResponse(String userId, String email, String nickname) {
        return new UserResponse(
                userId,
                email,
                nickname,
                nickname,
                null,
                Language.JA.getCode(),
                null,
                null,
                LocalDateTime.now(),
                "Asia/Tokyo",
                false,
                List.of("Users"));
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
