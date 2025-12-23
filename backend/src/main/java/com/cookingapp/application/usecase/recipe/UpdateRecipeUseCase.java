package com.cookingapp.application.usecase.recipe;

import java.util.List;

import org.springframework.stereotype.Service;

import com.cookingapp.domain.entity.Recipe;
import com.cookingapp.domain.exception.RecipeNotFoundException;
import com.cookingapp.domain.exception.UnauthorizedException;
import com.cookingapp.domain.repository.RecipeRepository;
import com.cookingapp.domain.valueobject.Ingredient;
import com.cookingapp.domain.valueobject.Step;

/**
 * レシピ更新ユースケース
 */
@Service
public class UpdateRecipeUseCase {

    private final RecipeRepository recipeRepository;

    public UpdateRecipeUseCase(RecipeRepository recipeRepository) {
        this.recipeRepository = recipeRepository;
    }

    /**
     * レシピを更新
     * 
     * @param recipeId    レシピID
     * @param userId      ユーザーID
     * @param title       タイトル
     * @param ingredients 食材リスト
     * @param steps       手順リスト
     * @param cookingTime 調理時間（分）
     * @return 更新されたレシピ
     * @throws RecipeNotFoundException レシピが見つからない場合
     * @throws UnauthorizedException   編集権限がない場合
     */
    public Recipe execute(String recipeId, String userId, String title, List<Ingredient> ingredients,
            List<Step> steps, int cookingTime) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new RecipeNotFoundException("Recipe not found: " + recipeId));

        if (!recipe.canEdit(userId)) {
            throw new UnauthorizedException("User does not have permission to edit this recipe");
        }

        recipe.update(title, ingredients, steps, cookingTime);

        return recipeRepository.save(recipe);
    }
}
