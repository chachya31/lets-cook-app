package com.cookingapp.application.usecase.recipe;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.cookingapp.domain.entity.Recipe;
import com.cookingapp.domain.repository.RecipeRepository;
import com.cookingapp.domain.service.ImageStorageService;

/**
 * レシピ検索ユースケース
 */
@Service
public class SearchRecipesUseCase {

    private final RecipeRepository recipeRepository;
    private final ImageStorageService imageStorageService;

    public SearchRecipesUseCase(RecipeRepository recipeRepository, ImageStorageService imageStorageService) {
        this.recipeRepository = recipeRepository;
        this.imageStorageService = imageStorageService;
    }

    /**
     * すべての公開レシピを検索
     * 
     * @return 公開レシピリスト（画像URLは有効なPresignedURL）
     */
    public List<Recipe> executePublic() {
        return recipeRepository.findAllPublic().stream()
                .map(this::refreshImageUrl)
                .collect(Collectors.toList());
    }

    /**
     * 作成者IDでレシピを検索
     * 
     * @param authorId 作成者ID
     * @return レシピリスト（画像URLは有効なPresignedURL）
     */
    public List<Recipe> executeByAuthor(String authorId) {
        return recipeRepository.findByAuthorId(authorId).stream()
                .map(this::refreshImageUrl)
                .collect(Collectors.toList());
    }

    /**
     * キーワードでレシピを検索（タイトルに含まれるもの）
     * 
     * @param keyword キーワード
     * @return レシピリスト（画像URLは有効なPresignedURL）
     */
    public List<Recipe> executeByKeyword(String keyword) {
        List<Recipe> allPublicRecipes = recipeRepository.findAllPublic();

        if (keyword == null || keyword.trim().isEmpty()) {
            return allPublicRecipes.stream()
                    .map(this::refreshImageUrl)
                    .collect(Collectors.toList());
        }

        String lowerKeyword = keyword.toLowerCase();
        return allPublicRecipes.stream()
                .filter(recipe -> recipe.getTitle().toLowerCase().contains(lowerKeyword))
                .map(this::refreshImageUrl)
                .collect(Collectors.toList());
    }

    /**
     * レシピの画像URLを新しいPresignedURLに更新
     */
    private Recipe refreshImageUrl(Recipe recipe) {
        if (recipe.getImageUrl() != null && !recipe.getImageUrl().isEmpty()) {
            String presignedUrl = imageStorageService.generatePresignedUrl(recipe.getImageUrl());
            recipe.updateImageUrl(presignedUrl);
        }
        return recipe;
    }
}
