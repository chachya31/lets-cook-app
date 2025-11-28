package com.cookingapp.application.usecase;

import com.cookingapp.domain.entity.User;
import com.cookingapp.domain.repository.UserRepository;
import com.cookingapp.domain.valueobject.Language;
import org.springframework.stereotype.Service;

/**
 * プロフィール更新ユースケース
 */
@Service
public class UpdateUserProfileUseCase {
    
    private final UserRepository userRepository;

    public UpdateUserProfileUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * ユーザープロフィールを更新
     * 
     * @param userId ユーザーID
     * @param nickname ニックネーム
     * @param displayName 表示名
     * @param preferredLanguage 優先言語
     * @param timezone タイムゾーン
     * @param marketingOptOut マーケティングオプトアウト
     * @return 更新されたユーザー
     */
    public User execute(String userId, String nickname, String displayName, 
                       Language preferredLanguage, String timezone, boolean marketingOptOut) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        user.updateProfile(nickname, displayName, preferredLanguage, timezone, marketingOptOut);

        return userRepository.save(user);
    }

    /**
     * プロフィール画像URLを更新
     * 
     * @param userId ユーザーID
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
