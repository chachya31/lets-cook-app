package com.cookingapp.application.usecase.recipe;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cookingapp.application.validation.ImageValidator;
import com.cookingapp.domain.entity.Recipe;
import com.cookingapp.domain.repository.RecipeRepository;
import com.cookingapp.domain.service.ImageStorageService;
import com.cookingapp.domain.valueobject.Ingredient;
import com.cookingapp.domain.valueobject.Step;

/**
 * レシピ作成ユースケース
 */
@Service
public class CreateRecipeUseCase {

    private static final Logger logger = LoggerFactory.getLogger(CreateRecipeUseCase.class);

    private final RecipeRepository recipeRepository;
    private final ImageStorageService imageStorageService;
    private final ImageValidator imageValidator;

    public CreateRecipeUseCase(RecipeRepository recipeRepository,
            ImageStorageService imageStorageService,
            ImageValidator imageValidator) {
        this.recipeRepository = recipeRepository;
        this.imageStorageService = imageStorageService;
        this.imageValidator = imageValidator;
    }

    /**
     * レシピを作成（画像なし）
     */
    public Recipe execute(String authorId, String title, List<Ingredient> ingredients,
            List<Step> steps, int cookingTime) {
        Recipe recipe = new Recipe(authorId, title, ingredients, steps, cookingTime);
        return recipeRepository.save(recipe);
    }

    /**
     * レシピを作成（画像あり）
     * 
     * @param authorId    作成者ID
     * @param title       タイトル
     * @param ingredients 食材リスト
     * @param steps       手順リスト
     * @param cookingTime 調理時間（分）
     * @param mainImage   メイン画像（任意）
     * @param stepImages  手順画像リスト（任意、インデックスに対応）
     * @return 作成されたレシピ
     */
    public Recipe executeWithImages(String authorId, String title, List<Ingredient> ingredients,
            List<Step> steps, int cookingTime,
            MultipartFile mainImage, List<MultipartFile> stepImages) throws IOException {

        Recipe recipe = new Recipe(authorId, title, ingredients, steps, cookingTime);
        String recipeId = recipe.getRecipeId();

        // メイン画像をアップロード
        if (mainImage != null && !mainImage.isEmpty()) {
            imageValidator.validate(mainImage);
            String fileName = "recipes/" + recipeId + "/main/" + mainImage.getOriginalFilename();
            String imageUrl = imageStorageService.uploadImage(
                    fileName,
                    mainImage.getContentType(),
                    mainImage.getInputStream(),
                    mainImage.getSize());
            recipe.updateImageUrl(imageUrl);
            logger.info("Uploaded main image for recipe: {}", recipeId);
        }

        // 手順画像をアップロード
        if (stepImages != null) {
            for (int i = 0; i < stepImages.size(); i++) {
                MultipartFile stepImage = stepImages.get(i);
                if (stepImage != null && !stepImage.isEmpty()) {
                    imageValidator.validate(stepImage);
                    String fileName = "recipes/" + recipeId + "/steps/" + i + "/" + stepImage.getOriginalFilename();
                    String imageUrl = imageStorageService.uploadImage(
                            fileName,
                            stepImage.getContentType(),
                            stepImage.getInputStream(),
                            stepImage.getSize());
                    recipe.updateStepImageUrl(i, imageUrl);
                    logger.info("Uploaded step {} image for recipe: {}", i, recipeId);
                }
            }
        }

        return recipeRepository.save(recipe);
    }
}
