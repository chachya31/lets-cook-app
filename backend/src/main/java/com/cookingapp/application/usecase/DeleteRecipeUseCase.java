package com.cookingapp.application.usecase;

import com.cookingapp.domain.entity.Recipe;
import com.cookingapp.domain.exception.RecipeNotFoundException;
import com.cookingapp.domain.exception.UnauthorizedException;
import com.cookingapp.domain.repository.RecipeRepository;
import org.springframework.stereotype.Service;

/**
 * レシピ削除ユースケース
 * スケジュールと買い物リストの参照を保持するため、論理削除を実施
 */
@Service
public class DeleteRecipeUseCase {

    private final RecipeRepository recipeRepository;

    public DeleteRecipeUseCase(RecipeRepository recipeRepository) {
        this.recipeRepository = recipeRepository;
    }

    /**
     * レシピを削除（論理削除）
     * 
     * @param recipeId レシピID
     * @param userId ユーザーID
     * @throws RecipeNotFoundException レシピが見つからない場合
     * @throws UnauthorizedException 削除権限がない場合
     */
    public void execute(String recipeId, String userId) {
        // レシピを取得
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new RecipeNotFoundException("Recipe not found: " + recipeId));

        // 削除権限チェック
        if (!recipe.canDelete(userId)) {
            throw new UnauthorizedException("User does not have permission to delete this recipe");
        }

        // 論理削除
        recipe.markAsDeleted();

        // リポジトリに保存
        recipeRepository.save(recipe);
    }
}
