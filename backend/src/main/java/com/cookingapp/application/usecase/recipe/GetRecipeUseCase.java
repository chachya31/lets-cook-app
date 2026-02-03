package com.cookingapp.application.usecase.recipe;

import org.springframework.stereotype.Service;

import com.cookingapp.domain.entity.Recipe;
import com.cookingapp.domain.exception.RecipeNotFoundException;
import com.cookingapp.domain.repository.RecipeRepository;
import com.cookingapp.domain.service.ImageStorageService;

/**
 * レシピ取得ユースケース
 */
@Service
public class GetRecipeUseCase {

    private final RecipeRepository recipeRepository;
    private final ImageStorageService imageStorageService;

    public GetRecipeUseCase(RecipeRepository recipeRepository, ImageStorageService imageStorageService) {
        this.recipeRepository = recipeRepository;
        this.imageStorageService = imageStorageService;
    }

    /**
     * レシピを取得
     * 画像URLがある場合は新しいPresignedURLを生成して返す
     * 
     * @param recipeId レシピID
     * @return レシピ（画像URLは有効なPresignedURL）
     * @throws RecipeNotFoundException レシピが見つからない場合
     */
    public Recipe execute(String recipeId) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new RecipeNotFoundException("Recipe not found: " + recipeId));

        // メイン画像URLがある場合は新しいPresignedURLを生成
        if (recipe.getImageUrl() != null && !recipe.getImageUrl().isEmpty()) {
            String presignedUrl = imageStorageService.generatePresignedUrl(recipe.getImageUrl());
            recipe.updateImageUrl(presignedUrl);
        }

        // 手順画像のPresignedURLを生成
        for (int i = 0; i < recipe.getSteps().size(); i++) {
            var step = recipe.getSteps().get(i);
            if (step.hasImage()) {
                String presignedUrl = imageStorageService.generatePresignedUrl(step.getImageUrl());
                recipe.updateStepImageUrl(i, presignedUrl);
            }
        }

        return recipe;
    }
}
