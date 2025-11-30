package com.cookingapp.application.usecase;

import com.cookingapp.domain.entity.Recipe;
import com.cookingapp.domain.exception.RecipeNotFoundException;
import com.cookingapp.domain.repository.RecipeRepository;
import org.springframework.stereotype.Service;

/**
 * レシピ取得ユースケース
 */
@Service
public class GetRecipeUseCase {

    private final RecipeRepository recipeRepository;

    public GetRecipeUseCase(RecipeRepository recipeRepository) {
        this.recipeRepository = recipeRepository;
    }

    /**
     * レシピを取得
     * 
     * @param recipeId レシピID
     * @return レシピ
     * @throws RecipeNotFoundException レシピが見つからない場合
     */
    public Recipe execute(String recipeId) {
        return recipeRepository.findById(recipeId)
                .orElseThrow(() -> new RecipeNotFoundException("Recipe not found: " + recipeId));
    }
}
