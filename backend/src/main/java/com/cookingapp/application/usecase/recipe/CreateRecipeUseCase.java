package com.cookingapp.application.usecase.recipe;

import com.cookingapp.domain.entity.Recipe;
import com.cookingapp.domain.repository.RecipeRepository;
import com.cookingapp.domain.valueobject.Ingredient;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * レシピ作成ユースケース
 */
@Service
public class CreateRecipeUseCase {

    private final RecipeRepository recipeRepository;

    public CreateRecipeUseCase(RecipeRepository recipeRepository) {
        this.recipeRepository = recipeRepository;
    }

    /**
     * レシピを作成
     * 
     * @param authorId 作成者ID
     * @param title タイトル
     * @param ingredients 食材リスト
     * @param steps 手順リスト
     * @param cookingTime 調理時間（分）
     * @return 作成されたレシピ
     */
    public Recipe execute(String authorId, String title, List<Ingredient> ingredients,
                          List<String> steps, int cookingTime) {
        // レシピエンティティ作成（バリデーションはエンティティ側で実施）
        Recipe recipe = new Recipe(authorId, title, ingredients, steps, cookingTime);

        // リポジトリに保存
        return recipeRepository.save(recipe);
    }
}
