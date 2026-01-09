package com.cookingapp.application.usecase.user;

import org.springframework.stereotype.Service;

import com.cookingapp.domain.entity.User;
import com.cookingapp.domain.repository.UserRepository;
import com.cookingapp.domain.service.ImageStorageService;
import com.cookingapp.domain.valueobject.Language;

/**
 * プロフィール更新ユースケース
 */
@Service
public class UpdateUserProfileUseCase {

    private final UserRepository userRepository;
    private final ImageStorageService imageStorageService;

    public UpdateUserProfileUseCase(UserRepository userRepository, ImageStorageService imageStorageService) {
        this.userRepository = userRepository;
        this.imageStorageService = imageStorageService;
    }

    /**
     * ユーザープロフィールを更新
     * 
     * @param userId            ユーザーID
     * @param nickname          ニックネーム
     * @param displayName       表示名
     * @param preferredLanguage 優先言語
     * @param timezone          タイムゾーン
     * @param marketingOptOut   マーケティングオプトアウト
     * @return 更新されたユーザー
     */
    public User execute(String userId, String nickname, String displayName,
            Language preferredLanguage, String timezone, boolean marketingOptOut) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        user.updateProfile(nickname, displayName, preferredLanguage, timezone, marketingOptOut);

        User savedUser = userRepository.save(user);

        // プロフィール画像がある場合は新しいPresigned URLを生成
        if (savedUser.getProfileImageUrl() != null && !savedUser.getProfileImageUrl().isEmpty()) {
            String presignedUrl = imageStorageService.generatePresignedUrl(savedUser.getProfileImageUrl());
            savedUser.updateProfileImageUrl(presignedUrl);
        }

        return savedUser;
    }

    /**
     * プロフィール画像URLを更新
     * 
     * @param userId          ユーザーID
     * @param profileImageUrl プロフィール画像URL
     * @return 更新されたユーザー
     */
    public User updateProfileImage(String userId, String profileImageUrl) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        user.updateProfileImageUrl(profileImageUrl);

        return userRepository.save(user);
    }
}
