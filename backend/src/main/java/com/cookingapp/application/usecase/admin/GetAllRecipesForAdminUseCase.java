package com.cookingapp.application.usecase.admin;

import com.cookingapp.domain.entity.Recipe;
import com.cookingapp.domain.repository.RecipeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * すべてのレシピ取得ユースケース（管理者機能）
 * 審査待ちを含むすべてのレシピを表示
 */
@Service
public class GetAllRecipesForAdminUseCase {
    
    private static final Logger log = LoggerFactory.getLogger(GetAllRecipesForAdminUseCase.class);
    
    private final RecipeRepository recipeRepository;
    
    public GetAllRecipesForAdminUseCase(RecipeRepository recipeRepository) {
        this.recipeRepository = recipeRepository;
    }
    
    /**
     * すべてのレシピを取得（管理者権限）
     * 公開・非公開・削除済みを含むすべてのレシピを返す
     * 
     * @return レシピリスト
     */
    public List<Recipe> execute() {
        log.info("管理者用にすべてのレシピを取得中");
        
        // 現時点では公開レシピのみ取得
        // 将来的にはすべてのレシピ（非公開・削除済み含む）を取得するメソッドを追加
        List<Recipe> recipes = recipeRepository.findAllPublic();
        
        log.info("管理者用にレシピを取得しました: count={}", recipes.size());
        return recipes;
    }
}
