package com.cookingapp.application.usecase.recipe;

import com.cookingapp.domain.entity.Recipe;
import com.cookingapp.domain.repository.RecipeRepository;
import com.cookingapp.domain.valueobject.Ingredient;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 繝ｬ繧ｷ繝比ｽ懈・繝ｦ繝ｼ繧ｹ繧ｱ繝ｼ繧ｹ
 */
@Service
public class CreateRecipeUseCase {

    private final RecipeRepository recipeRepository;

    public CreateRecipeUseCase(RecipeRepository recipeRepository) {
        this.recipeRepository = recipeRepository;
    }

    /**
     * 繝ｬ繧ｷ繝斐ｒ菴懈・
     * 
     * @param authorId 菴懈・閠・D
     * @param title 繧ｿ繧､繝医Ν
     * @param ingredients 鬟滓攝繝ｪ繧ｹ繝・
     * @param steps 謇矩・Μ繧ｹ繝・
     * @param cookingTime 隱ｿ逅・凾髢難ｼ亥・・・
     * @return 菴懈・縺輔ｌ縺溘Ξ繧ｷ繝・
     */
    public Recipe execute(String authorId, String title, List<Ingredient> ingredients,
                          List<String> steps, int cookingTime) {
        // 繝ｬ繧ｷ繝斐お繝ｳ繝・ぅ繝・ぅ菴懈・・医ヰ繝ｪ繝・・繧ｷ繝ｧ繝ｳ縺ｯ繧ｨ繝ｳ繝・ぅ繝・ぅ蜀・〒螳滓命・・
        Recipe recipe = new Recipe(authorId, title, ingredients, steps, cookingTime);

        // 繝ｪ繝昴ず繝医Μ縺ｫ菫晏ｭ・
        return recipeRepository.save(recipe);
    }
}
