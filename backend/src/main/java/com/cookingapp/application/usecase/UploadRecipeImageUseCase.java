package com.cookingapp.application.usecase;

import com.cookingapp.application.validation.ImageValidator;
import com.cookingapp.domain.entity.Recipe;
import com.cookingapp.domain.exception.RecipeNotFoundException;
import com.cookingapp.domain.exception.UnauthorizedException;
import com.cookingapp.domain.repository.RecipeRepository;
import com.cookingapp.infrastructure.external.s3.S3ImageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * レシピ画像アップロードユースケース
 */
@Service
public class UploadRecipeImageUseCase {

    private final RecipeRepository recipeRepository;
    private final S3ImageService s3ImageService;
    private final ImageValidator imageValidator;

    public UploadRecipeImageUseCase(RecipeRepository recipeRepository,
                                    S3ImageService s3ImageService,
                                    ImageValidator imageValidator) {
        this.recipeRepository = recipeRepository;
        this.s3ImageService = s3ImageService;
        this.imageValidator = imageValidator;
    }

    /**
     * レシピ画像をアップロード
     * 
     * @param recipeId レシピID
     * @param userId ユーザーID
     * @param file 画像ファイル
     * @return 更新されたレシピ
     * @throws RecipeNotFoundException レシピが見つからない場合
     * @throws UnauthorizedException 編集権限がない場合
     * @throws IOException ファイル読み込みエラー
     */
    public Recipe execute(String recipeId, String userId, MultipartFile file) throws IOException {
        // レシピを取得
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new RecipeNotFoundException("Recipe not found: " + recipeId));

        // 編集権限チェック
        if (!recipe.canEdit(userId)) {
            throw new UnauthorizedException("User does not have permission to edit this recipe");
        }

        // 画像バリデーション
        imageValidator.validate(file);

        // S3にアップロード
        String fileName = "recipes/" + recipeId + "/" + file.getOriginalFilename();
        String imageUrl = s3ImageService.uploadImage(
                fileName,
                file.getContentType(),
                file.getInputStream(),
                file.getSize()
        );

        // レシピの画像URLを更新
        recipe.updateImageUrl(imageUrl);

        // リポジトリに保存
        return recipeRepository.save(recipe);
    }
}
