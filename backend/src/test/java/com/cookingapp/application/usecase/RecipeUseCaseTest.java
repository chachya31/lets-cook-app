package com.cookingapp.application.usecase;

import com.cookingapp.application.dto.CreateRecipeInput;
import com.cookingapp.application.dto.IngredientInput;
import com.cookingapp.application.dto.RecipeOutput;
import com.cookingapp.application.dto.StepInput;
import com.cookingapp.application.exception.RecipeNotFoundException;
import com.cookingapp.application.exception.UnauthorizedRecipeAccessException;
import com.cookingapp.application.port.ImageStoragePort;
import com.cookingapp.domain.model.Ingredient;
import com.cookingapp.domain.model.Recipe;
import com.cookingapp.domain.model.Step;
import com.cookingapp.domain.repository.RecipeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RecipeUseCase")
class RecipeUseCaseTest {

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private ImageStoragePort imageStoragePort;

    private RecipeUseCase recipeUseCase;

    private static final String TEST_RECIPE_ID = "recipe-123";
    private static final String TEST_AUTHOR_ID = "author-456";
    private static final String TEST_IMAGE_KEY = "images/recipe/test-image.jpg";
    private static final String TEST_PRESIGNED_URL = "https://s3.example.com/presigned-url";

    @BeforeEach
    void setUp() {
        recipeUseCase = new RecipeUseCase(recipeRepository, imageStoragePort);
    }

    @Nested
    @DisplayName("createRecipe")
    class CreateRecipe {

        @Nested
        @DisplayName("正常系")
        class Success {

            @Test
            @DisplayName("画像キーがある場合、レシピが正しく保存されpresigned URLが生成される")
            void shouldCreateRecipeWithImageKeyAndGeneratePresignedUrl() {
                // Arrange
                CreateRecipeInput input = createRecipeInputWithImageKey();
                when(recipeRepository.save(any(Recipe.class))).thenAnswer(invocation -> invocation.getArgument(0));
                when(imageStoragePort.generatePresignedUrl(TEST_IMAGE_KEY)).thenReturn(TEST_PRESIGNED_URL);

                // Act
                RecipeOutput output = recipeUseCase.createRecipe(input, TEST_AUTHOR_ID);

                // Assert
                assertThat(output).isNotNull();
                assertThat(output.getTitle()).isEqualTo("Test Recipe");
                assertThat(output.getAuthorId()).isEqualTo(TEST_AUTHOR_ID);
                assertThat(output.getImageUrl()).isEqualTo(TEST_PRESIGNED_URL);
                assertThat(output.getCookingTime()).isEqualTo(30);
                assertThat(output.getIsPublic()).isTrue();

                ArgumentCaptor<Recipe> recipeCaptor = ArgumentCaptor.forClass(Recipe.class);
                verify(recipeRepository).save(recipeCaptor.capture());
                Recipe savedRecipe = recipeCaptor.getValue();
                assertThat(savedRecipe.getImageUrl()).isEqualTo(TEST_IMAGE_KEY);
                assertThat(savedRecipe.getIsDeleted()).isFalse();

                verify(imageStoragePort).generatePresignedUrl(TEST_IMAGE_KEY);
            }

            @Test
            @DisplayName("画像キーがない場合、presigned URL生成がスキップされる")
            void shouldCreateRecipeWithoutImageKey() {
                // Arrange
                CreateRecipeInput input = createRecipeInputWithoutImageKey();
                when(recipeRepository.save(any(Recipe.class))).thenAnswer(invocation -> invocation.getArgument(0));

                // Act
                RecipeOutput output = recipeUseCase.createRecipe(input, TEST_AUTHOR_ID);

                // Assert
                assertThat(output).isNotNull();
                assertThat(output.getTitle()).isEqualTo("Test Recipe");
                assertThat(output.getImageUrl()).isNull();

                verify(recipeRepository).save(any(Recipe.class));
                verify(imageStoragePort, never()).generatePresignedUrl(anyString());
            }

            @Test
            @DisplayName("材料と手順が正しくエンティティに変換される")
            void shouldConvertIngredientsAndStepsCorrectly() {
                // Arrange
                CreateRecipeInput input = createRecipeInputWithImageKey();
                when(recipeRepository.save(any(Recipe.class))).thenAnswer(invocation -> invocation.getArgument(0));
                when(imageStoragePort.generatePresignedUrl(anyString())).thenReturn(TEST_PRESIGNED_URL);

                // Act
                RecipeOutput output = recipeUseCase.createRecipe(input, TEST_AUTHOR_ID);

                // Assert
                assertThat(output.getIngredients()).hasSize(2);
                assertThat(output.getIngredients().get(0).getName()).isEqualTo("Flour");
                assertThat(output.getIngredients().get(0).getQuantity()).isEqualTo("200");
                assertThat(output.getIngredients().get(0).getUnit()).isEqualTo("g");

                assertThat(output.getSteps()).hasSize(2);
                assertThat(output.getSteps().get(0).getStepNumber()).isEqualTo(1);
                assertThat(output.getSteps().get(0).getDescription()).isEqualTo("Mix ingredients");
            }
        }
    }

    @Nested
    @DisplayName("getRecipeById")
    class GetRecipeById {

        @Nested
        @DisplayName("正常系")
        class Success {

            @Test
            @DisplayName("公開レシピの場合、誰でもアクセスできる")
            void shouldReturnPublicRecipeForAnyUser() {
                // Arrange
                Recipe recipe = createRecipeWithImage();
                when(recipeRepository.findById(TEST_RECIPE_ID)).thenReturn(Optional.of(recipe));
                when(imageStoragePort.generatePresignedUrl(TEST_IMAGE_KEY)).thenReturn(TEST_PRESIGNED_URL);

                // Act
                RecipeOutput output = recipeUseCase.getRecipeById(TEST_RECIPE_ID, "other-user");

                // Assert
                assertThat(output).isNotNull();
                assertThat(output.getRecipeId()).isEqualTo(TEST_RECIPE_ID);
                assertThat(output.getImageUrl()).isEqualTo(TEST_PRESIGNED_URL);

                verify(recipeRepository).findById(TEST_RECIPE_ID);
                verify(imageStoragePort).generatePresignedUrl(TEST_IMAGE_KEY);
            }

            @Test
            @DisplayName("非公開レシピの場合、作者はアクセスできる")
            void shouldReturnPrivateRecipeForAuthor() {
                // Arrange
                Recipe recipe = createPrivateRecipeWithImage();
                when(recipeRepository.findById(TEST_RECIPE_ID)).thenReturn(Optional.of(recipe));
                when(imageStoragePort.generatePresignedUrl(TEST_IMAGE_KEY)).thenReturn(TEST_PRESIGNED_URL);

                // Act
                RecipeOutput output = recipeUseCase.getRecipeById(TEST_RECIPE_ID, TEST_AUTHOR_ID);

                // Assert
                assertThat(output).isNotNull();
                assertThat(output.getRecipeId()).isEqualTo(TEST_RECIPE_ID);
            }

            @Test
            @DisplayName("画像がないレシピの場合、presigned URL生成がスキップされる")
            void shouldReturnRecipeWithoutPresignedUrlWhenNoImage() {
                // Arrange
                Recipe recipe = createRecipeWithoutImage();
                when(recipeRepository.findById(TEST_RECIPE_ID)).thenReturn(Optional.of(recipe));

                // Act
                RecipeOutput output = recipeUseCase.getRecipeById(TEST_RECIPE_ID, TEST_AUTHOR_ID);

                // Assert
                assertThat(output).isNotNull();
                assertThat(output.getRecipeId()).isEqualTo(TEST_RECIPE_ID);
                assertThat(output.getImageUrl()).isNull();

                verify(recipeRepository).findById(TEST_RECIPE_ID);
                verify(imageStoragePort, never()).generatePresignedUrl(anyString());
            }
        }

        @Nested
        @DisplayName("異常系")
        class Failure {

            @Test
            @DisplayName("レシピが存在しない場合、RecipeNotFoundExceptionがスローされる")
            void shouldThrowRecipeNotFoundExceptionWhenRecipeNotFound() {
                // Arrange
                when(recipeRepository.findById(TEST_RECIPE_ID)).thenReturn(Optional.empty());

                // Act & Assert
                assertThatThrownBy(() -> recipeUseCase.getRecipeById(TEST_RECIPE_ID, TEST_AUTHOR_ID))
                        .isInstanceOf(RecipeNotFoundException.class)
                        .hasMessageContaining(TEST_RECIPE_ID);

                verify(recipeRepository).findById(TEST_RECIPE_ID);
                verify(imageStoragePort, never()).generatePresignedUrl(anyString());
            }

            @Test
            @DisplayName("非公開レシピに作者以外がアクセスした場合、RecipeNotFoundExceptionがスローされる")
            void shouldThrowRecipeNotFoundExceptionWhenPrivateRecipeAccessedByNonAuthor() {
                // Arrange
                Recipe recipe = createPrivateRecipeWithImage();
                when(recipeRepository.findById(TEST_RECIPE_ID)).thenReturn(Optional.of(recipe));

                // Act & Assert
                assertThatThrownBy(() -> recipeUseCase.getRecipeById(TEST_RECIPE_ID, "other-user"))
                        .isInstanceOf(RecipeNotFoundException.class)
                        .hasMessageContaining(TEST_RECIPE_ID);

                verify(imageStoragePort, never()).generatePresignedUrl(anyString());
            }
        }
    }

    @Nested
    @DisplayName("getRecipesByAuthor")
    class GetRecipesByAuthor {

        @Nested
        @DisplayName("正常系")
        class Success {

            @Test
            @DisplayName("自分のレシピを取得する場合、全てのレシピが返される")
            void shouldReturnAllRecipesWhenRequestingOwnRecipes() {
                // Arrange
                Recipe publicRecipe = createRecipeWithImage();
                Recipe privateRecipe = createPrivateRecipeWithImage();
                when(recipeRepository.findByAuthor(TEST_AUTHOR_ID)).thenReturn(List.of(publicRecipe, privateRecipe));
                when(imageStoragePort.generatePresignedUrl(TEST_IMAGE_KEY)).thenReturn(TEST_PRESIGNED_URL);

                // Act
                List<RecipeOutput> outputs = recipeUseCase.getRecipesByAuthor(TEST_AUTHOR_ID, TEST_AUTHOR_ID);

                // Assert
                assertThat(outputs).hasSize(2);

                verify(recipeRepository).findByAuthor(TEST_AUTHOR_ID);
            }

            @Test
            @DisplayName("他人のレシピを取得する場合、公開レシピのみが返される")
            void shouldReturnOnlyPublicRecipesWhenRequestingOthersRecipes() {
                // Arrange
                Recipe publicRecipe = createRecipeWithImage();
                Recipe privateRecipe = createPrivateRecipeWithImage();
                when(recipeRepository.findByAuthor(TEST_AUTHOR_ID)).thenReturn(List.of(publicRecipe, privateRecipe));
                when(imageStoragePort.generatePresignedUrl(TEST_IMAGE_KEY)).thenReturn(TEST_PRESIGNED_URL);

                // Act
                List<RecipeOutput> outputs = recipeUseCase.getRecipesByAuthor(TEST_AUTHOR_ID, "other-user");

                // Assert
                assertThat(outputs).hasSize(1);
                assertThat(outputs.get(0).getIsPublic()).isTrue();

                verify(recipeRepository).findByAuthor(TEST_AUTHOR_ID);
            }

            @Test
            @DisplayName("レシピが存在しない場合、空のリストを返す")
            void shouldReturnEmptyListWhenNoRecipesFound() {
                // Arrange
                when(recipeRepository.findByAuthor(TEST_AUTHOR_ID)).thenReturn(Collections.emptyList());

                // Act
                List<RecipeOutput> outputs = recipeUseCase.getRecipesByAuthor(TEST_AUTHOR_ID, TEST_AUTHOR_ID);

                // Assert
                assertThat(outputs).isEmpty();

                verify(recipeRepository).findByAuthor(TEST_AUTHOR_ID);
                verify(imageStoragePort, never()).generatePresignedUrl(anyString());
            }
        }
    }

    @Nested
    @DisplayName("getPublicRecipes")
    class GetPublicRecipes {

        @Nested
        @DisplayName("正常系")
        class Success {

            @Test
            @DisplayName("findAllPublicが呼ばれ、公開レシピ一覧が返される")
            void shouldCallFindAllPublicAndReturnRecipes() {
                // Arrange
                Recipe recipe = createRecipeWithImage();
                when(recipeRepository.findAllPublic()).thenReturn(List.of(recipe));
                when(imageStoragePort.generatePresignedUrl(TEST_IMAGE_KEY)).thenReturn(TEST_PRESIGNED_URL);

                // Act
                List<RecipeOutput> outputs = recipeUseCase.getPublicRecipes();

                // Assert
                assertThat(outputs).hasSize(1);
                assertThat(outputs.get(0).getImageUrl()).isEqualTo(TEST_PRESIGNED_URL);

                verify(recipeRepository).findAllPublic();
                verify(imageStoragePort).generatePresignedUrl(TEST_IMAGE_KEY);
            }
        }
    }

    @Nested
    @DisplayName("updateRecipe")
    class UpdateRecipe {

        @Nested
        @DisplayName("正常系")
        class Success {

            @Test
            @DisplayName("著者本人がレシピを更新できる")
            void shouldUpdateRecipeWhenUserIsAuthor() {
                // Arrange
                Recipe existingRecipe = createRecipeWithImage();
                CreateRecipeInput input = createRecipeInputWithImageKey();
                when(recipeRepository.findById(TEST_RECIPE_ID)).thenReturn(Optional.of(existingRecipe));
                when(recipeRepository.save(any(Recipe.class))).thenAnswer(invocation -> invocation.getArgument(0));
                when(imageStoragePort.generatePresignedUrl(anyString())).thenReturn(TEST_PRESIGNED_URL);

                // Act
                RecipeOutput output = recipeUseCase.updateRecipe(TEST_RECIPE_ID, input, TEST_AUTHOR_ID);

                // Assert
                assertThat(output).isNotNull();
                assertThat(output.getRecipeId()).isEqualTo(TEST_RECIPE_ID);

                verify(recipeRepository).findById(TEST_RECIPE_ID);
                verify(recipeRepository).save(any(Recipe.class));
            }

            @Test
            @DisplayName("画像キーが未指定の場合、既存の画像キーが保持される")
            void shouldKeepExistingImageKeyWhenNewKeyNotProvided() {
                // Arrange
                Recipe existingRecipe = createRecipeWithImage();
                CreateRecipeInput input = createRecipeInputWithoutImageKey();
                when(recipeRepository.findById(TEST_RECIPE_ID)).thenReturn(Optional.of(existingRecipe));
                when(recipeRepository.save(any(Recipe.class))).thenAnswer(invocation -> invocation.getArgument(0));
                when(imageStoragePort.generatePresignedUrl(TEST_IMAGE_KEY)).thenReturn(TEST_PRESIGNED_URL);

                // Act
                RecipeOutput output = recipeUseCase.updateRecipe(TEST_RECIPE_ID, input, TEST_AUTHOR_ID);

                // Assert
                ArgumentCaptor<Recipe> recipeCaptor = ArgumentCaptor.forClass(Recipe.class);
                verify(recipeRepository).save(recipeCaptor.capture());
                Recipe savedRecipe = recipeCaptor.getValue();
                assertThat(savedRecipe.getImageUrl()).isEqualTo(TEST_IMAGE_KEY);
            }
        }

        @Nested
        @DisplayName("異常系")
        class Failure {

            @Test
            @DisplayName("レシピが存在しない場合、RecipeNotFoundExceptionがスローされる")
            void shouldThrowRecipeNotFoundExceptionWhenRecipeNotFound() {
                // Arrange
                CreateRecipeInput input = createRecipeInputWithImageKey();
                when(recipeRepository.findById(TEST_RECIPE_ID)).thenReturn(Optional.empty());

                // Act & Assert
                assertThatThrownBy(() -> recipeUseCase.updateRecipe(TEST_RECIPE_ID, input, TEST_AUTHOR_ID))
                        .isInstanceOf(RecipeNotFoundException.class)
                        .hasMessageContaining(TEST_RECIPE_ID);

                verify(recipeRepository, never()).save(any(Recipe.class));
            }

            @Test
            @DisplayName("著者以外がレシピを更新しようとした場合、UnauthorizedRecipeAccessExceptionがスローされる")
            void shouldThrowUnauthorizedExceptionWhenUserIsNotAuthor() {
                // Arrange
                Recipe existingRecipe = createRecipeWithImage();
                CreateRecipeInput input = createRecipeInputWithImageKey();
                when(recipeRepository.findById(TEST_RECIPE_ID)).thenReturn(Optional.of(existingRecipe));

                String otherUserId = "other-user-789";

                // Act & Assert
                assertThatThrownBy(() -> recipeUseCase.updateRecipe(TEST_RECIPE_ID, input, otherUserId))
                        .isInstanceOf(UnauthorizedRecipeAccessException.class)
                        .hasMessageContaining(TEST_RECIPE_ID);

                verify(recipeRepository, never()).save(any(Recipe.class));
            }
        }
    }

    @Nested
    @DisplayName("deleteRecipe")
    class DeleteRecipe {

        @Nested
        @DisplayName("正常系")
        class Success {

            @Test
            @DisplayName("著者本人がレシピを削除できる")
            void shouldDeleteRecipeWhenUserIsAuthor() {
                // Arrange
                Recipe existingRecipe = createRecipeWithImage();
                when(recipeRepository.findById(TEST_RECIPE_ID)).thenReturn(Optional.of(existingRecipe));

                // Act
                recipeUseCase.deleteRecipe(TEST_RECIPE_ID, TEST_AUTHOR_ID);

                // Assert
                verify(recipeRepository).findById(TEST_RECIPE_ID);
                verify(recipeRepository).delete(TEST_RECIPE_ID, List.of("Flour", "Sugar"));
            }

            @Test
            @DisplayName("材料がnullのレシピも正常に削除できる")
            void shouldDeleteRecipeWithNullIngredients() {
                // Arrange
                Recipe existingRecipe = createRecipeWithNullIngredients();
                when(recipeRepository.findById(TEST_RECIPE_ID)).thenReturn(Optional.of(existingRecipe));

                // Act
                recipeUseCase.deleteRecipe(TEST_RECIPE_ID, TEST_AUTHOR_ID);

                // Assert
                verify(recipeRepository).findById(TEST_RECIPE_ID);
                verify(recipeRepository).delete(TEST_RECIPE_ID, Collections.emptyList());
            }
        }

        @Nested
        @DisplayName("異常系")
        class Failure {

            @Test
            @DisplayName("レシピが存在しない場合、RecipeNotFoundExceptionがスローされる")
            void shouldThrowRecipeNotFoundExceptionWhenRecipeNotFound() {
                // Arrange
                when(recipeRepository.findById(TEST_RECIPE_ID)).thenReturn(Optional.empty());

                // Act & Assert
                assertThatThrownBy(() -> recipeUseCase.deleteRecipe(TEST_RECIPE_ID, TEST_AUTHOR_ID))
                        .isInstanceOf(RecipeNotFoundException.class)
                        .hasMessageContaining(TEST_RECIPE_ID);

                verify(recipeRepository, never()).delete(anyString(), any());
            }

            @Test
            @DisplayName("著者以外がレシピを削除しようとした場合、UnauthorizedRecipeAccessExceptionがスローされる")
            void shouldThrowUnauthorizedExceptionWhenUserIsNotAuthor() {
                // Arrange
                Recipe existingRecipe = createRecipeWithImage();
                when(recipeRepository.findById(TEST_RECIPE_ID)).thenReturn(Optional.of(existingRecipe));

                String otherUserId = "other-user-789";

                // Act & Assert
                assertThatThrownBy(() -> recipeUseCase.deleteRecipe(TEST_RECIPE_ID, otherUserId))
                        .isInstanceOf(UnauthorizedRecipeAccessException.class)
                        .hasMessageContaining(TEST_RECIPE_ID);

                verify(recipeRepository, never()).delete(anyString(), any());
            }
        }
    }

    // Helper methods

    private CreateRecipeInput createRecipeInputWithImageKey() {
        List<IngredientInput> ingredients = List.of(
                IngredientInput.builder().name("Flour").quantity("200").unit("g").build(),
                IngredientInput.builder().name("Sugar").quantity("100").unit("g").build()
        );
        List<StepInput> steps = List.of(
                StepInput.builder().stepNumber(1).description("Mix ingredients").build(),
                StepInput.builder().stepNumber(2).description("Bake for 30 minutes").build()
        );

        return CreateRecipeInput.builder()
                .title("Test Recipe")
                .ingredients(ingredients)
                .steps(steps)
                .cookingTime(30)
                .isPublic(true)
                .imageKey(TEST_IMAGE_KEY)
                .build();
    }

    private CreateRecipeInput createRecipeInputWithoutImageKey() {
        List<IngredientInput> ingredients = List.of(
                IngredientInput.builder().name("Flour").quantity("200").unit("g").build(),
                IngredientInput.builder().name("Sugar").quantity("100").unit("g").build()
        );
        List<StepInput> steps = List.of(
                StepInput.builder().stepNumber(1).description("Mix ingredients").build(),
                StepInput.builder().stepNumber(2).description("Bake for 30 minutes").build()
        );

        return CreateRecipeInput.builder()
                .title("Test Recipe")
                .ingredients(ingredients)
                .steps(steps)
                .cookingTime(30)
                .isPublic(true)
                .imageKey(null)
                .build();
    }

    private Recipe createRecipeWithImage() {
        List<Ingredient> ingredients = List.of(
                Ingredient.builder().name("Flour").quantity("200").unit("g").build(),
                Ingredient.builder().name("Sugar").quantity("100").unit("g").build()
        );
        List<Step> steps = List.of(
                Step.builder().stepNumber(1).description("Mix ingredients").build(),
                Step.builder().stepNumber(2).description("Bake for 30 minutes").build()
        );

        return Recipe.builder()
                .recipeId(TEST_RECIPE_ID)
                .title("Test Recipe")
                .authorId(TEST_AUTHOR_ID)
                .ingredients(ingredients)
                .steps(steps)
                .cookingTime(30)
                .isPublic(true)
                .isDeleted(false)
                .imageUrl(TEST_IMAGE_KEY)
                .createdAt("2024-01-01T00:00:00Z")
                .updatedAt("2024-01-01T00:00:00Z")
                .build();
    }

    private Recipe createRecipeWithoutImage() {
        List<Ingredient> ingredients = List.of(
                Ingredient.builder().name("Flour").quantity("200").unit("g").build()
        );
        List<Step> steps = List.of(
                Step.builder().stepNumber(1).description("Mix ingredients").build()
        );

        return Recipe.builder()
                .recipeId(TEST_RECIPE_ID)
                .title("Test Recipe")
                .authorId(TEST_AUTHOR_ID)
                .ingredients(ingredients)
                .steps(steps)
                .cookingTime(30)
                .isPublic(true)
                .isDeleted(false)
                .imageUrl(null)
                .createdAt("2024-01-01T00:00:00Z")
                .updatedAt("2024-01-01T00:00:00Z")
                .build();
    }

    private Recipe createPrivateRecipeWithImage() {
        List<Ingredient> ingredients = List.of(
                Ingredient.builder().name("Flour").quantity("200").unit("g").build()
        );
        List<Step> steps = List.of(
                Step.builder().stepNumber(1).description("Mix ingredients").build()
        );

        return Recipe.builder()
                .recipeId(TEST_RECIPE_ID)
                .title("Private Recipe")
                .authorId(TEST_AUTHOR_ID)
                .ingredients(ingredients)
                .steps(steps)
                .cookingTime(30)
                .isPublic(false)
                .isDeleted(false)
                .imageUrl(TEST_IMAGE_KEY)
                .createdAt("2024-01-01T00:00:00Z")
                .updatedAt("2024-01-01T00:00:00Z")
                .build();
    }

    private Recipe createRecipeWithNullIngredients() {
        List<Step> steps = List.of(
                Step.builder().stepNumber(1).description("Mix ingredients").build()
        );

        return Recipe.builder()
                .recipeId(TEST_RECIPE_ID)
                .title("Test Recipe")
                .authorId(TEST_AUTHOR_ID)
                .ingredients(null)
                .steps(steps)
                .cookingTime(30)
                .isPublic(true)
                .isDeleted(false)
                .imageUrl(TEST_IMAGE_KEY)
                .createdAt("2024-01-01T00:00:00Z")
                .updatedAt("2024-01-01T00:00:00Z")
                .build();
    }
}
