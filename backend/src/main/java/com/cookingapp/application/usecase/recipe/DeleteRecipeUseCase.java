package com.cookingapp.application.usecase.recipe;

import com.cookingapp.domain.entity.Recipe;
import com.cookingapp.domain.exception.RecipeNotFoundException;
import com.cookingapp.domain.exception.UnauthorizedException;
import com.cookingapp.domain.repository.RecipeRepository;
import org.springframework.stereotype.Service;

/**
 * 繝ｬ繧ｷ繝泌炎髯､繝ｦ繝ｼ繧ｹ繧ｱ繝ｼ繧ｹ
 * 繧ｹ繧ｱ繧ｸ繝･繝ｼ繝ｫ縺ｨ雋ｷ縺・黄繝ｪ繧ｹ繝医・蜿ら・繧剃ｿ晄戟縺吶ｋ縺溘ａ縲∬ｫ也炊蜑企勁繧貞ｮ滓命
 */
@Service
public class DeleteRecipeUseCase {

    private final RecipeRepository recipeRepository;

    public DeleteRecipeUseCase(RecipeRepository recipeRepository) {
        this.recipeRepository = recipeRepository;
    }

    /**
     * 繝ｬ繧ｷ繝斐ｒ蜑企勁・郁ｫ也炊蜑企勁・・
     * 
     * @param recipeId 繝ｬ繧ｷ繝祢D
     * @param userId 繝ｦ繝ｼ繧ｶ繝ｼID
     * @throws RecipeNotFoundException 繝ｬ繧ｷ繝斐′隕九▽縺九ｉ縺ｪ縺・ｴ蜷・
     * @throws UnauthorizedException 蜑企勁讓ｩ髯舌′縺ｪ縺・ｴ蜷・
     */
    public void execute(String recipeId, String userId) {
        // 繝ｬ繧ｷ繝斐ｒ蜿門ｾ・
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new RecipeNotFoundException("Recipe not found: " + recipeId));

        // 蜑企勁讓ｩ髯舌メ繧ｧ繝・け
        if (!recipe.canDelete(userId)) {
            throw new UnauthorizedException("User does not have permission to delete this recipe");
        }

        // 隲也炊蜑企勁
        recipe.markAsDeleted();

        // 繝ｪ繝昴ず繝医Μ縺ｫ菫晏ｭ・
        recipeRepository.save(recipe);
    }
}
