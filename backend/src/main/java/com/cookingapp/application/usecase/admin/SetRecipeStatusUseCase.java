package com.cookingapp.application.usecase.admin;

import com.cookingapp.domain.entity.Recipe;
import com.cookingapp.domain.exception.RecipeNotFoundException;
import com.cookingapp.domain.repository.RecipeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * レシピステータス設定ユースケース（管理者機能）
 */
@Service
public class SetRecipeStatusUseCase {
    
    private static final Logger log = LoggerFactory.getLogger(SetRecipeStatusUseCase.class);
    
    private final RecipeRepository recipeRepository;
    
    public SetRecipeStatusUseCase(RecipeRepository recipeRepository) {
        this.recipeRepository = recipeRepository;
    }
    
    /**
     * レシピの公開ステータスを設定（管理者権限）
     * 
     * @param recipeId レシピID
     * @param isPublic 公開フラグ（true: 公開、false: 非公開）
     * @return 更新されたレシピ
     */
    public Recipe execute(String recipeId, boolean isPublic) {
        log.info("管理者によるレシピステータス設定: recipeId={}, isPublic={}", recipeId, isPublic);
        
        // レシピの存在確認
        Recipe recipe = recipeRepository.findById(recipeId)
            .orElseThrow(() -> new RecipeNotFoundException("レシピが見つかりません: " + recipeId));
        
        // ステータスを設定
        recipe.setPublic(isPublic);
        
        // 保存
        Recipe updatedRecipe = recipeRepository.save(recipe);
        
        log.info("管理者によるレシピステータス設定が完了しました: recipeId={}, isPublic={}", recipeId, isPublic);
        return updatedRecipe;
    }
}
