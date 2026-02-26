package com.cookingapp.unit.usecase.admin;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
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

import com.cookingapp.application.usecase.admin.DeleteRecipeByAdminUseCase;
import com.cookingapp.domain.entity.Recipe;
import com.cookingapp.domain.exception.RecipeNotFoundException;
import com.cookingapp.domain.repository.RecipeRepository;
import com.cookingapp.domain.service.ImageStorageService;
import com.cookingapp.domain.valueobject.Ingredient;
import com.cookingapp.domain.valueobject.Step;

/**
 * DeleteRecipeByAdminUseCaseのユニットテスト
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DeleteRecipeByAdminUseCase ユニットテスト")
class DeleteRecipeByAdminUseCaseTest {

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private ImageStorageService imageStorageService;

    private DeleteRecipeByAdminUseCase deleteRecipeByAdminUseCase;

    private static final String TEST_RECIPE_ID = "recipe-123";
    private static final String TEST_AUTHOR_ID = "author-456";
    private static final String TEST_IMAGE_URL = "images/recipe/recipe-123.jpg";

    @BeforeEach
    void setUp() {
        deleteRecipeByAdminUseCase = new DeleteRecipeByAdminUseCase(recipeRepository, imageStorageService);
    }

    @Test
    @DisplayName("レシピの論理削除が成功する")
    void testExecute_DeletesRecipeSuccessfully() {
        // Arrange
        Recipe recipe = createTestRecipe(null);

        when(recipeRepository.findById(TEST_RECIPE_ID)).thenReturn(Optional.of(recipe));
        when(recipeRepository.save(any(Recipe.class))).thenReturn(recipe);

        // Act
        deleteRecipeByAdminUseCase.execute(TEST_RECIPE_ID);

        // Assert
        verify(recipeRepository).findById(TEST_RECIPE_ID);
        verify(recipeRepository).save(any(Recipe.class));
    }

    @Test
    @DisplayName("レシピ画像がある場合、画像も削除される")
    void testExecute_DeletesImageWhenExists() {
        // Arrange
        Recipe recipe = createTestRecipe(TEST_IMAGE_URL);

        when(recipeRepository.findById(TEST_RECIPE_ID)).thenReturn(Optional.of(recipe));
        doNothing().when(imageStorageService).deleteImage(TEST_IMAGE_URL);
        when(recipeRepository.save(any(Recipe.class))).thenReturn(recipe);

        // Act
        deleteRecipeByAdminUseCase.execute(TEST_RECIPE_ID);

        // Assert
        verify(imageStorageService).deleteImage(TEST_IMAGE_URL);
        verify(recipeRepository).save(any(Recipe.class));
    }

    @Test
    @DisplayName("レシピ画像がない場合、画像削除はスキップされる")
    void testExecute_SkipsImageDeletionWhenNoImage() {
        // Arrange
        Recipe recipe = createTestRecipe(null);

        when(recipeRepository.findById(TEST_RECIPE_ID)).thenReturn(Optional.of(recipe));
        when(recipeRepository.save(any(Recipe.class))).thenReturn(recipe);

        // Act
        deleteRecipeByAdminUseCase.execute(TEST_RECIPE_ID);

        // Assert
        verify(imageStorageService, never()).deleteImage(anyString());
        verify(recipeRepository).save(any(Recipe.class));
    }

    @Test
    @DisplayName("存在しないレシピで削除が失敗する")
    void testExecute_WithNonExistentRecipe_ThrowsRecipeNotFoundException() {
        // Arrange
        when(recipeRepository.findById(TEST_RECIPE_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> deleteRecipeByAdminUseCase.execute(TEST_RECIPE_ID))
                .isInstanceOf(RecipeNotFoundException.class)
                .hasMessageContaining("レシピが見つかりません");

        verify(recipeRepository).findById(TEST_RECIPE_ID);
        verify(imageStorageService, never()).deleteImage(anyString());
        verify(recipeRepository, never()).save(any(Recipe.class));
    }

    @Test
    @DisplayName("画像削除に失敗しても論理削除は実行される")
    void testExecute_ContinuesWhenImageDeletionFails() {
        // Arrange
        Recipe recipe = createTestRecipe(TEST_IMAGE_URL);

        when(recipeRepository.findById(TEST_RECIPE_ID)).thenReturn(Optional.of(recipe));
        doThrow(new RuntimeException("S3 error")).when(imageStorageService).deleteImage(TEST_IMAGE_URL);
        when(recipeRepository.save(any(Recipe.class))).thenReturn(recipe);

        // Act
        deleteRecipeByAdminUseCase.execute(TEST_RECIPE_ID);

        // Assert
        verify(imageStorageService).deleteImage(TEST_IMAGE_URL);
        verify(recipeRepository).save(any(Recipe.class));
    }

    @Test
    @DisplayName("削除順序が正しい（画像→論理削除）")
    void testExecute_DeletesInCorrectOrder() {
        // Arrange
        Recipe recipe = createTestRecipe(TEST_IMAGE_URL);

        when(recipeRepository.findById(TEST_RECIPE_ID)).thenReturn(Optional.of(recipe));
        doNothing().when(imageStorageService).deleteImage(TEST_IMAGE_URL);
        when(recipeRepository.save(any(Recipe.class))).thenReturn(recipe);

        // Act
        deleteRecipeByAdminUseCase.execute(TEST_RECIPE_ID);

        // Assert
        var inOrder = inOrder(imageStorageService, recipeRepository);
        inOrder.verify(imageStorageService).deleteImage(TEST_IMAGE_URL);
        inOrder.verify(recipeRepository).save(any(Recipe.class));
    }

    // ヘルパーメソッド

    private Recipe createTestRecipe(String imageUrl) {
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
                imageUrl,
                true,
                false,
                LocalDateTime.now(),
                LocalDateTime.now());
    }
}
