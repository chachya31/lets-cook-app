package com.cookingapp.application.usecase.recipe;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cookingapp.application.validation.ImageValidator;
import com.cookingapp.domain.entity.Recipe;
import com.cookingapp.domain.exception.RecipeNotFoundException;
import com.cookingapp.domain.exception.UnauthorizedException;
import com.cookingapp.domain.repository.RecipeRepository;
import com.cookingapp.domain.service.ImageStorageService;

/**
 * レシピ画像アップロードユースケース
 */
@Service
public class UploadRecipeImageUseCase {

    private static final Logger logger = LoggerFactory.getLogger(UploadRecipeImageUseCase.class);

    private final RecipeRepository recipeRepository;
    private final ImageStorageService imageStorageService;
    private final ImageValidator imageValidator;

    public UploadRecipeImageUseCase(RecipeRepository recipeRepository,
            ImageStorageService imageStorageService,
            ImageValidator imageValidator) {
        this.recipeRepository = recipeRepository;
        this.imageStorageService = imageStorageService;
        this.imageValidator = imageValidator;
    }

    /**
     * レシピ画像をアップロード
     * 
     * @param recipeId レシピID
     * @param userId   ユーザーID
     * @param file     画像ファイル
     * @return 更新されたレシピ
     * @throws RecipeNotFoundException レシピが見つからない場合
     * @throws UnauthorizedException   編集権限がない場合
     * @throws IOException             ファイル読み込みエラー
     */
    public Recipe execute(String recipeId, String userId, MultipartFile file) throws IOException {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new RecipeNotFoundException("Recipe not found: " + recipeId));

        if (!recipe.canEdit(userId)) {
            throw new UnauthorizedException("User does not have permission to edit this recipe");
        }

        imageValidator.validate(file);

        String fileName = "recipes/" + recipeId + "/main/" + file.getOriginalFilename();
        String imageUrl = imageStorageService.uploadImage(
                fileName,
                file.getContentType(),
                file.getInputStream(),
                file.getSize());

        recipe.updateImageUrl(imageUrl);
        logger.info("Uploaded main image for recipe: {}", recipeId);

        return recipeRepository.save(recipe);
    }

    /**
     * 手順画像をアップロード
     * 
     * @param recipeId  レシピID
     * @param userId    ユーザーID
     * @param stepIndex 手順インデックス（0始まり）
     * @param file      画像ファイル
     * @return 更新されたレシピ
     * @throws RecipeNotFoundException レシピが見つからない場合
     * @throws UnauthorizedException   編集権限がない場合
     * @throws IOException             ファイル読み込みエラー
     */
    public Recipe executeStepImage(String recipeId, String userId, int stepIndex, MultipartFile file)
            throws IOException {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new RecipeNotFoundException("Recipe not found: " + recipeId));

        if (!recipe.canEdit(userId)) {
            throw new UnauthorizedException("User does not have permission to edit this recipe");
        }

        if (stepIndex < 0 || stepIndex >= recipe.getSteps().size()) {
            throw new IllegalArgumentException("Invalid step index: " + stepIndex);
        }

        imageValidator.validate(file);

        String fileName = "recipes/" + recipeId + "/steps/" + stepIndex + "/" + file.getOriginalFilename();
        String imageUrl = imageStorageService.uploadImage(
                fileName,
                file.getContentType(),
                file.getInputStream(),
                file.getSize());

        recipe.updateStepImageUrl(stepIndex, imageUrl);
        logger.info("Uploaded step {} image for recipe: {}", stepIndex, recipeId);

        return recipeRepository.save(recipe);
    }
}
