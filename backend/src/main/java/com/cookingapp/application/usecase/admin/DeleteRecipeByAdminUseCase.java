package com.cookingapp.application.usecase.admin;

import com.cookingapp.domain.entity.Recipe;
import com.cookingapp.domain.exception.RecipeNotFoundException;
import com.cookingapp.domain.repository.RecipeRepository;
import com.cookingapp.domain.service.ImageStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * レシピ削除ユースケース（管理者機能）
 * スケジュールと買い物リストの参照を保持するため、論理削除を実行
 */
@Service
public class DeleteRecipeByAdminUseCase {
    
    private static final Logger log = LoggerFactory.getLogger(DeleteRecipeByAdminUseCase.class);
    
    private final RecipeRepository recipeRepository;
    private final ImageStorageService imageStorageService;
    
    public DeleteRecipeByAdminUseCase(RecipeRepository recipeRepository, ImageStorageService imageStorageService) {
        this.recipeRepository = recipeRepository;
        this.imageStorageService = imageStorageService;
    }
    
    /**
     * レシピを削除（管理者権限）
     * スケジュールと買い物リストの参照を保持するため、論理削除を実行
     * 
     * @param recipeId レシピID
     */
    public void execute(String recipeId) {
        log.info("管理者によるレシピ削除を開始: recipeId={}", recipeId);
        
        // レシピの存在確認
        Recipe recipe = recipeRepository.findById(recipeId)
            .orElseThrow(() -> new RecipeNotFoundException("レシピが見つかりません: " + recipeId));
        
        // 画像を削除（オプション）
        if (recipe.getImageUrl() != null && !recipe.getImageUrl().isEmpty()) {
            try {
                imageStorageService.deleteImage(recipe.getImageUrl());
                log.info("レシピ画像を削除しました: recipeId={}", recipeId);
            } catch (Exception e) {
                log.warn("レシピ画像の削除に失敗しました: recipeId={}, error={}", recipeId, e.getMessage());
            }
        }
        
        // 論理削除
        recipe.markAsDeleted();
        recipeRepository.save(recipe);
        
        log.info("管理者によるレシピ削除が完了しました（論理削除）: recipeId={}", recipeId);
    }
}
