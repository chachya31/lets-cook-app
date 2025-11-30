package com.cookingapp.application.usecase.recipe;

import com.cookingapp.domain.entity.Recipe;
import com.cookingapp.domain.exception.RecipeNotFoundException;
import com.cookingapp.domain.exception.UnauthorizedException;
import com.cookingapp.domain.repository.RecipeRepository;
import com.cookingapp.domain.valueobject.Ingredient;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 繝ｬ繧ｷ繝疲峩譁ｰ繝ｦ繝ｼ繧ｹ繧ｱ繝ｼ繧ｹ
 */
@Service
public class UpdateRecipeUseCase {

    private final RecipeRepository recipeRepository;

    public UpdateRecipeUseCase(RecipeRepository recipeRepository) {
        this.recipeRepository = recipeRepository;
    }

    /**
     * 繝ｬ繧ｷ繝斐ｒ譖ｴ譁ｰ
     * 
     * @param recipeId 繝ｬ繧ｷ繝祢D
     * @param userId 繝ｦ繝ｼ繧ｶ繝ｼID
     * @param title 繧ｿ繧､繝医Ν
     * @param ingredients 鬟滓攝繝ｪ繧ｹ繝・
     * @param steps 謇矩・Μ繧ｹ繝・
     * @param cookingTime 隱ｿ逅・凾髢難ｼ亥・・・
     * @return 譖ｴ譁ｰ縺輔ｌ縺溘Ξ繧ｷ繝・
     * @throws RecipeNotFoundException 繝ｬ繧ｷ繝斐′隕九▽縺九ｉ縺ｪ縺・ｴ蜷・
     * @throws UnauthorizedException 邱ｨ髮・ｨｩ髯舌′縺ｪ縺・ｴ蜷・
     */
    public Recipe execute(String recipeId, String userId, String title, List<Ingredient> ingredients,
                          List<String> steps, int cookingTime) {
        // 繝ｬ繧ｷ繝斐ｒ蜿門ｾ・
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new RecipeNotFoundException("Recipe not found: " + recipeId));

        // 邱ｨ髮・ｨｩ髯舌メ繧ｧ繝・け
        if (!recipe.canEdit(userId)) {
            throw new UnauthorizedException("User does not have permission to edit this recipe");
        }

        // 繝ｬ繧ｷ繝斐ｒ譖ｴ譁ｰ
        recipe.update(title, ingredients, steps, cookingTime);

        // 繝ｪ繝昴ず繝医Μ縺ｫ菫晏ｭ・
        return recipeRepository.save(recipe);
    }
}
