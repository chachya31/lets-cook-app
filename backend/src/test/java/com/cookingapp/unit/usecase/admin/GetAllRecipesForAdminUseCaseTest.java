package com.cookingapp.unit.usecase.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cookingapp.application.usecase.admin.GetAllRecipesForAdminUseCase;
import com.cookingapp.domain.entity.Recipe;
import com.cookingapp.domain.repository.RecipeRepository;
import com.cookingapp.domain.valueobject.Ingredient;
import com.cookingapp.domain.valueobject.Step;

/**
 * GetAllRecipesForAdminUseCaseのユニットテスト
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GetAllRecipesForAdminUseCase ユニットテスト")
class GetAllRecipesForAdminUseCaseTest {

    @Mock
    private RecipeRepository recipeRepository;

    private GetAllRecipesForAdminUseCase getAllRecipesForAdminUseCase;

    private static final String TEST_RECIPE_ID_1 = "recipe-123";
    private static final String TEST_RECIPE_ID_2 = "recipe-456";
    private static final String TEST_AUTHOR_ID = "author-789";

    @BeforeEach
    void setUp() {
        getAllRecipesForAdminUseCase = new GetAllRecipesForAdminUseCase(recipeRepository);
    }

    @Test
    @DisplayName("すべてのレシピを正常に取得する")
    void testExecute_ReturnsAllRecipes() {
        // Arrange
        Recipe recipe1 = createTestRecipe(TEST_RECIPE_ID_1, "レシピ1");
        Recipe recipe2 = createTestRecipe(TEST_RECIPE_ID_2, "レシピ2");
        List<Recipe> recipes = List.of(recipe1, recipe2);

        when(recipeRepository.findAllPublic()).thenReturn(recipes);

        // Act
        List<Recipe> result = getAllRecipesForAdminUseCase.execute();

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getRecipeId()).isEqualTo(TEST_RECIPE_ID_1);
        assertThat(result.get(0).getTitle()).isEqualTo("レシピ1");
        assertThat(result.get(1).getRecipeId()).isEqualTo(TEST_RECIPE_ID_2);
        assertThat(result.get(1).getTitle()).isEqualTo("レシピ2");

        verify(recipeRepository).findAllPublic();
    }

    @Test
    @DisplayName("レシピが存在しない場合、空のリストを返す")
    void testExecute_ReturnsEmptyListWhenNoRecipes() {
        // Arrange
        when(recipeRepository.findAllPublic()).thenReturn(Collections.emptyList());

        // Act
        List<Recipe> result = getAllRecipesForAdminUseCase.execute();

        // Assert
        assertThat(result).isEmpty();

        verify(recipeRepository).findAllPublic();
    }

    @Test
    @DisplayName("1件のレシピを正常に取得する")
    void testExecute_ReturnsSingleRecipe() {
        // Arrange
        Recipe recipe = createTestRecipe(TEST_RECIPE_ID_1, "単一レシピ");

        when(recipeRepository.findAllPublic()).thenReturn(List.of(recipe));

        // Act
        List<Recipe> result = getAllRecipesForAdminUseCase.execute();

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRecipeId()).isEqualTo(TEST_RECIPE_ID_1);
        assertThat(result.get(0).getTitle()).isEqualTo("単一レシピ");

        verify(recipeRepository).findAllPublic();
    }

    // ヘルパーメソッド

    private Recipe createTestRecipe(String recipeId, String title) {
        List<Ingredient> ingredients = List.of(
                new Ingredient("小麦粉", new BigDecimal("200"), "g", null, false));

        List<Step> steps = List.of(
                new Step("材料を混ぜる", null, null));

        return new Recipe(
                recipeId,
                TEST_AUTHOR_ID,
                title,
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
