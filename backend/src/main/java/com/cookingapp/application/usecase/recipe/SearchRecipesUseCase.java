package com.cookingapp.application.usecase.recipe;

import com.cookingapp.domain.entity.Recipe;
import com.cookingapp.domain.repository.RecipeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * レシピ検索ユースケース
 */
@Service
public class SearchRecipesUseCase {

    private final RecipeRepository recipeRepository;

    public SearchRecipesUseCase(RecipeRepository recipeRepository) {
        this.recipeRepository = recipeRepository;
    }

    /**
     * すべての公開レシピを検索
     * 
     * @return 公開レシピリスチE
     */
    public List<Recipe> executePublic() {
        return recipeRepository.findAllPublic();
    }

    /**
     * 作�E老EDでレシピを検索
     * 
     * @param authorId 作�E老ED
     * @return レシピリスチE
     */
    public List<Recipe> executeByAuthor(String authorId) {
        return recipeRepository.findByAuthorId(authorId);
    }

    /**
     * キーワードでレシピを検索�E�タイトルに含まれるも�E�E�E
     * 
     * @param keyword キーワーチE
     * @return レシピリスチE
     */
    public List<Recipe> executeByKeyword(String keyword) {
        List<Recipe> allPublicRecipes = recipeRepository.findAllPublic();
        
        if (keyword == null || keyword.trim().isEmpty()) {
            return allPublicRecipes;
        }

        String lowerKeyword = keyword.toLowerCase();
        return allPublicRecipes.stream()
                .filter(recipe -> recipe.getTitle().toLowerCase().contains(lowerKeyword))
                .collect(Collectors.toList());
    }
}
