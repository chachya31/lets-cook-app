package com.cookingapp.application.usecase;

import com.cookingapp.application.validation.ImageValidationException;
import com.cookingapp.application.validation.ImageValidator;
import com.cookingapp.domain.entity.User;
import com.cookingapp.domain.exception.UserNotFoundException;
import com.cookingapp.domain.repository.UserRepository;
import com.cookingapp.infrastructure.external.s3.S3ImageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * プロフィール画像アップロードユースケース
 */
@Service
public class UploadProfileImageUseCase {

    private static final Logger logger = LoggerFactory.getLogger(UploadProfileImageUseCase.class);

    private final UserRepository userRepository;
    private final S3ImageService s3ImageService;
    private final ImageValidator imageValidator;

    public UploadProfileImageUseCase(
            UserRepository userRepository,
            S3ImageService s3ImageService,
            ImageValidator imageValidator) {
        this.userRepository = userRepository;
        this.s3ImageService = s3ImageService;
        this.imageValidator = imageValidator;
    }

    /**
     * プロフィール画像をアップロード
     * 
     * @param userId ユーザーID
     * @param file 画像ファイル
     * @return 更新されたユーザー
     * @throws UserNotFoundException ユーザーが見つからない
     * @throws ImageValidationException 画像バリデーションエラー
     */
    public User execute(String userId, MultipartFile file) {
        logger.info("Uploading profile image for user: {}", userId);

        // ユーザーの存在確認
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

        // 画像バリデーション
        imageValidator.validate(file);

        try {
            // 既存の画像を削除
            if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
                try {
                    s3ImageService.deleteImage(user.getProfileImageUrl());
                    logger.info("Deleted old profile image for user: {}", userId);
                } catch (Exception e) {
                    logger.warn("Failed to delete old profile image: {}", e.getMessage());
                    // 削除失敗しても続行
                }
            }

            // 新しい画像をアップロード
            String imageUrl = s3ImageService.uploadImage(
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getInputStream(),
                    file.getSize()
            );

            // ユーザーのプロフィール画像URLを更新
            user.updateProfileImageUrl(imageUrl);
            userRepository.save(user);

            logger.info("Successfully uploaded profile image for user: {}", userId);
            return user;

        } catch (IOException e) {
            logger.error("Failed to read image file: {}", e.getMessage(), e);
            throw new ImageValidationException("Failed to read image file", e);
        }
    }
}
