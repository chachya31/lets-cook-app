package com.cookingapp.application.usecase.user;

import org.springframework.stereotype.Service;

import com.cookingapp.domain.entity.User;
import com.cookingapp.domain.repository.UserRepository;
import com.cookingapp.domain.service.ImageStorageService;

/**
 * ユーザープロフィール取得ユースケース
 */
@Service
public class GetUserProfileUseCase {

    private final UserRepository userRepository;
    private final ImageStorageService imageStorageService;

    public GetUserProfileUseCase(UserRepository userRepository, ImageStorageService imageStorageService) {
        this.userRepository = userRepository;
        this.imageStorageService = imageStorageService;
    }

    /**
     * ユーザープロフィールを取得
     * プロフィール画像がある場合は新しいPresigned URLを生成
     * 
     * @param userId ユーザーID
     * @return ユーザー（画像URLは有効なPresigned URL）
     */
    public User execute(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        // プロフィール画像がある場合は新しいPresigned URLを生成
        if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
            String presignedUrl = imageStorageService.generatePresignedUrl(user.getProfileImageUrl());
            user.updateProfileImageUrl(presignedUrl);
        }

        return user;
    }
}
