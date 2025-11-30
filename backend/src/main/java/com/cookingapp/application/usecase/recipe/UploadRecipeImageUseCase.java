package com.cookingapp.application.usecase.recipe;

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
 * 繝ｬ繧ｷ繝皮判蜒上い繝・・繝ｭ繝ｼ繝峨Θ繝ｼ繧ｹ繧ｱ繝ｼ繧ｹ
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
     * 繝ｬ繧ｷ繝皮判蜒上ｒ繧｢繝・・繝ｭ繝ｼ繝・
     * 
     * @param recipeId 繝ｬ繧ｷ繝祢D
     * @param userId 繝ｦ繝ｼ繧ｶ繝ｼID
     * @param file 逕ｻ蜒上ヵ繧｡繧､繝ｫ
     * @return 譖ｴ譁ｰ縺輔ｌ縺溘Ξ繧ｷ繝・
     * @throws RecipeNotFoundException 繝ｬ繧ｷ繝斐′隕九▽縺九ｉ縺ｪ縺・ｴ蜷・
     * @throws UnauthorizedException 邱ｨ髮・ｨｩ髯舌′縺ｪ縺・ｴ蜷・
     * @throws IOException 繝輔ぃ繧､繝ｫ隱ｭ縺ｿ霎ｼ縺ｿ繧ｨ繝ｩ繝ｼ
     */
    public Recipe execute(String recipeId, String userId, MultipartFile file) throws IOException {
        // 繝ｬ繧ｷ繝斐ｒ蜿門ｾ・
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new RecipeNotFoundException("Recipe not found: " + recipeId));

        // 邱ｨ髮・ｨｩ髯舌メ繧ｧ繝・け
        if (!recipe.canEdit(userId)) {
            throw new UnauthorizedException("User does not have permission to edit this recipe");
        }

        // 逕ｻ蜒上ヰ繝ｪ繝・・繧ｷ繝ｧ繝ｳ
        imageValidator.validate(file);

        // S3縺ｫ繧｢繝・・繝ｭ繝ｼ繝・
        String fileName = "recipes/" + recipeId + "/" + file.getOriginalFilename();
        String imageUrl = s3ImageService.uploadImage(
                fileName,
                file.getContentType(),
                file.getInputStream(),
                file.getSize()
        );

        // 繝ｬ繧ｷ繝斐・逕ｻ蜒酋RL繧呈峩譁ｰ
        recipe.updateImageUrl(imageUrl);

        // 繝ｪ繝昴ず繝医Μ縺ｫ菫晏ｭ・
        return recipeRepository.save(recipe);
    }
}
