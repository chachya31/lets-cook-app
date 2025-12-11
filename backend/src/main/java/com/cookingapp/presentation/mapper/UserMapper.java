package com.cookingapp.presentation.mapper;

import com.cookingapp.domain.entity.User;
import com.cookingapp.domain.valueobject.Language;
import com.cookingapp.presentation.dto.RegisterUserRequest;
import com.cookingapp.presentation.dto.UpdateProfileRequest;
import com.cookingapp.presentation.dto.UserResponse;

/**
 * UserMapper
 * User エンティティと DTO の変換を担当
 */
public class UserMapper {

    private UserMapper() {
        // ユーティリティクラスのため、インスタンス化を防ぐ
    }

    /**
     * RegisterUserRequest から Language に変換
     */
    public static Language toLanguage(RegisterUserRequest request) {
        return request.getPreferredLanguage() != null
                ? Language.fromCode(request.getPreferredLanguage())
                : Language.JA;
    }

    /**
     * UpdateProfileRequest から Language に変換（null許容）
     */
    public static Language toLanguageOrNull(UpdateProfileRequest request) {
        return request.getPreferredLanguage() != null
                ? Language.fromCode(request.getPreferredLanguage())
                : null;
    }

    /**
     * User エンティティから UserResponse に変換
     */
    public static UserResponse toResponse(User user) {
        return UserResponse.from(user);
    }
}
