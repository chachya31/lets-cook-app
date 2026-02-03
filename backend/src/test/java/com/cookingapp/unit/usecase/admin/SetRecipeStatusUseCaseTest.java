package com.cookingapp.unit.usecase.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cookingapp.application.usecase.admin.SetRecipeStatusUseCase;
import com.cookingapp.domain.entity.Recipe;
import com.cookingapp.domain.exception.RecipeNotFoundException;
import com.cookingapp.domain.repository.RecipeRepository;
import com.cookingapp.domain.valueobject.Ingredient;
import com.cookingapp.domain.valueobject.Step;

/**
 * SetRecipeStatusUseCaseのユニットテスト
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SetRecipeStatusUseCase ユニットテスト")
class SetRecipeStatusUseCaseTest {

    @Mock
    private RecipeRepository recipeRepository;

    private SetRecipeStatusUseCase setRecipeStatusUseCase;

    private static final String TEST_RECIPE_ID = "recipe-123";
    private static final String TEST_AUTHOR_ID = "author-456";

    @BeforeEach
    void setUp() {
        setRecipeStatusUseCase = new SetRecipeStatusUseCase(recipeRepository);
    }

    @Test
    @DisplayName("レシピを公開状態に設定する")
    void testExecute_SetsRecipeToPublic() {
        // Arrange
        Recipe recipe = createTestRecipe(false);
        Recipe savedRecipe = createTestRecipe(true);

        when(recipeRepository.findById(TEST_RECIPE_ID)).thenReturn(Optional.of(recipe));
        when(recipeRepository.save(any(Recipe.class))).thenReturn(savedRecipe);

        // Act
        Recipe result = setRecipeStatusUseCase.execute(TEST_RECIPE_ID, true);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.isPublic()).isTrue();

        verify(recipeRepository).findById(TEST_RECIPE_ID);
        verify(recipeRepository).save(any(Recipe.class));
    }

    @Test
    @DisplayName("レシピを非公開状態に設定する")
    void testExecute_SetsRecipeToPrivate() {
        // Arrange
        Recipe recipe = createTestRecipe(true);
        Recipe savedRecipe = createTestRecipe(false);

        when(recipeRepository.findById(TEST_RECIPE_ID)).thenReturn(Optional.of(recipe));
        when(recipeRepository.save(any(Recipe.class))).thenReturn(savedRecipe);

        // Act
        Recipe result = setRecipeStatusUseCase.execute(TEST_RECIPE_ID, false);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.isPublic()).isFalse();

        verify(recipeRepository).findById(TEST_RECIPE_ID);
        verify(recipeRepository).save(any(Recipe.class));
    }

    @Test
    @DisplayName("存在しないレシピでステータス変更が失敗する")
    void testExecute_WithNonExistentRecipe_ThrowsRecipeNotFoundException() {
        // Arrange
        when(recipeRepository.findById(TEST_RECIPE_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> setRecipeStatusUseCase.execute(TEST_RECIPE_ID, true))
                .isInstanceOf(RecipeNotFoundException.class)
                .hasMessageContaining("レシピが見つかりません");

        verify(recipeRepository).findById(TEST_RECIPE_ID);
        verify(recipeRepository, never()).save(any(Recipe.class));
    }

    @Test
    @DisplayName("同じステータスを設定しても正常に完了する")
    void testExecute_SetsStatusToSameValue() {
        // Arrange
        Recipe recipe = createTestRecipe(true);
        Recipe savedRecipe = createTestRecipe(true);

        when(recipeRepository.findById(TEST_RECIPE_ID)).thenReturn(Optional.of(recipe));
        when(recipeRepository.save(any(Recipe.class))).thenReturn(savedRecipe);

        // Act
        Recipe result = setRecipeStatusUseCase.execute(TEST_RECIPE_ID, true);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.isPublic()).isTrue();

        verify(recipeRepository).findById(TEST_RECIPE_ID);
        verify(recipeRepository).save(any(Recipe.class));
    }

    // ヘルパーメソッド

    private Recipe createTestRecipe(boolean isPublic) {
        List<Ingredient> ingredients = List.of(
                new Ingredient("小麦粉", new BigDecimal("200"), "g", null, false));

        List<Step> steps = List.of(
                new Step("材料を混ぜる", null, null));

        return new Recipe(
                TEST_RECIPE_ID,
                TEST_AUTHOR_ID,
                "テストレシピ",
                ingredients,
                steps,
                30,
                null,
                isPublic,
                false,
                LocalDateTime.now(),
                LocalDateTime.now());
    }
}
