package com.cookingapp.unit.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cookingapp.domain.entity.Recipe;
import com.cookingapp.domain.valueobject.Ingredient;
import com.cookingapp.domain.valueobject.Step;
import com.cookingapp.presentation.dto.IngredientDto;
import com.cookingapp.presentation.dto.RecipeRequest;
import com.cookingapp.presentation.dto.RecipeResponse;
import com.cookingapp.presentation.dto.StepDto;
import com.cookingapp.presentation.mapper.RecipeMapper;

/**
 * RecipeMapperのユニットテスト
 */
@DisplayName("RecipeMapper ユニットテスト")
class RecipeMapperTest {

    private static final String TEST_RECIPE_ID = "recipe-123";
    private static final String TEST_AUTHOR_ID = "author-456";

    @Nested
    @DisplayName("toIngredients - RecipeRequestからIngredientリストに変換")
    class ToIngredients {

        @Test
        @DisplayName("正常にIngredientリストに変換される")
        void shouldConvertToIngredientList() {
            // Arrange
            RecipeRequest request = createRecipeRequest();

            // Act
            List<Ingredient> result = RecipeMapper.toIngredients(request);

            // Assert
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getName()).isEqualTo("小麦粉");
            assertThat(result.get(0).getQuantity()).isEqualByComparingTo(new BigDecimal("200"));
            assertThat(result.get(0).getUnit()).isEqualTo("g");
            assertThat(result.get(1).getName()).isEqualTo("砂糖");
        }
    }

    @Nested
    @DisplayName("toSteps - RecipeRequestからStepリストに変換")
    class ToSteps {

        @Test
        @DisplayName("正常にStepリストに変換される")
        void shouldConvertToStepList() {
            // Arrange
            RecipeRequest request = createRecipeRequest();

            // Act
            List<Step> result = RecipeMapper.toSteps(request);

            // Assert
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getDescription()).isEqualTo("材料を混ぜる");
            assertThat(result.get(1).getDescription()).isEqualTo("30分焼く");
        }
    }

    @Nested
    @DisplayName("parseIngredients - JSON文字列からIngredientリストに変換")
    class ParseIngredients {

        @Test
        @DisplayName("正常にJSON文字列からIngredientリストに変換される")
        void shouldParseValidJsonToIngredientList() {
            // Arrange
            String json = """
                [
                    {"name": "小麦粉", "quantity": 200, "unit": "g", "note": null, "optional": false},
                    {"name": "砂糖", "quantity": 100, "unit": "g", "note": "お好みで調整", "optional": true}
                ]
                """;

            // Act
            List<Ingredient> result = RecipeMapper.parseIngredients(json);

            // Assert
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getName()).isEqualTo("小麦粉");
            assertThat(result.get(0).getQuantity()).isEqualByComparingTo(new BigDecimal("200"));
            assertThat(result.get(0).isOptional()).isFalse();
            assertThat(result.get(1).getName()).isEqualTo("砂糖");
            assertThat(result.get(1).getNote()).isEqualTo("お好みで調整");
            assertThat(result.get(1).isOptional()).isTrue();
        }

        @Test
        @DisplayName("空の配列のJSONは空のリストを返す")
        void shouldReturnEmptyListForEmptyArray() {
            // Arrange
            String json = "[]";

            // Act
            List<Ingredient> result = RecipeMapper.parseIngredients(json);

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("不正なJSON形式でIllegalArgumentExceptionがスローされる")
        void shouldThrowExceptionForInvalidJson() {
            // Arrange
            String invalidJson = "invalid json";

            // Act & Assert
            assertThatThrownBy(() -> RecipeMapper.parseIngredients(invalidJson))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid ingredients JSON format");
        }

        @Test
        @DisplayName("JSONオブジェクト形式（配列でない）でIllegalArgumentExceptionがスローされる")
        void shouldThrowExceptionForJsonObject() {
            // Arrange
            String jsonObject = "{\"name\": \"小麦粉\"}";

            // Act & Assert
            assertThatThrownBy(() -> RecipeMapper.parseIngredients(jsonObject))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid ingredients JSON format");
        }
    }

    @Nested
    @DisplayName("parseSteps - JSON文字列からStepリストに変換")
    class ParseSteps {

        @Test
        @DisplayName("正常にJSON文字列からStepリストに変換される")
        void shouldParseValidJsonToStepList() {
            // Arrange
            String json = """
                [
                    {"description": "材料を混ぜる", "imageUrl": null, "videoUrl": null},
                    {"description": "30分焼く", "imageUrl": "https://example.com/step2.jpg", "videoUrl": null}
                ]
                """;

            // Act
            List<Step> result = RecipeMapper.parseSteps(json);

            // Assert
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getDescription()).isEqualTo("材料を混ぜる");
            assertThat(result.get(0).getImageUrl()).isNull();
            assertThat(result.get(1).getDescription()).isEqualTo("30分焼く");
            assertThat(result.get(1).getImageUrl()).isEqualTo("https://example.com/step2.jpg");
        }

        @Test
        @DisplayName("空の配列のJSONは空のリストを返す")
        void shouldReturnEmptyListForEmptyArray() {
            // Arrange
            String json = "[]";

            // Act
            List<Step> result = RecipeMapper.parseSteps(json);

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("不正なJSON形式でIllegalArgumentExceptionがスローされる")
        void shouldThrowExceptionForInvalidJson() {
            // Arrange
            String invalidJson = "not a valid json";

            // Act & Assert
            assertThatThrownBy(() -> RecipeMapper.parseSteps(invalidJson))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid steps JSON format");
        }
    }

    @Nested
    @DisplayName("toResponse - RecipeエンティティからRecipeResponseに変換")
    class ToResponse {

        @Test
        @DisplayName("正常にRecipeResponseに変換される")
        void shouldConvertToRecipeResponse() {
            // Arrange
            Recipe recipe = createTestRecipe();

            // Act
            RecipeResponse result = RecipeMapper.toResponse(recipe);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getRecipeId()).isEqualTo(TEST_RECIPE_ID);
            assertThat(result.getTitle()).isEqualTo("テストレシピ");
            assertThat(result.getAuthorId()).isEqualTo(TEST_AUTHOR_ID);
            assertThat(result.getCookingTime()).isEqualTo(30);
        }
    }

    @Nested
    @DisplayName("toResponseList - RecipeエンティティリストからRecipeResponseリストに変換")
    class ToResponseList {

        @Test
        @DisplayName("正常にRecipeResponseリストに変換される")
        void shouldConvertToRecipeResponseList() {
            // Arrange
            Recipe recipe1 = createTestRecipe();
            Recipe recipe2 = createTestRecipeWithId("recipe-456", "別のレシピ");
            List<Recipe> recipes = List.of(recipe1, recipe2);

            // Act
            List<RecipeResponse> result = RecipeMapper.toResponseList(recipes);

            // Assert
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getRecipeId()).isEqualTo(TEST_RECIPE_ID);
            assertThat(result.get(0).getTitle()).isEqualTo("テストレシピ");
            assertThat(result.get(1).getRecipeId()).isEqualTo("recipe-456");
            assertThat(result.get(1).getTitle()).isEqualTo("別のレシピ");
        }

        @Test
        @DisplayName("空のリストは空のレスポンスリストを返す")
        void shouldReturnEmptyListForEmptyInput() {
            // Arrange
            List<Recipe> recipes = Collections.emptyList();

            // Act
            List<RecipeResponse> result = RecipeMapper.toResponseList(recipes);

            // Assert
            assertThat(result).isEmpty();
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
        return createTestRecipeWithId(TEST_RECIPE_ID, "テストレシピ");
    }

    private Recipe createTestRecipeWithId(String recipeId, String title) {
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
