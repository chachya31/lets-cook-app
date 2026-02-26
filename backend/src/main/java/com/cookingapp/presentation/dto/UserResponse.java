package com.cookingapp.presentation.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.cookingapp.domain.entity.User;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * ユーザーレスポンス
 */
@Getter
@AllArgsConstructor
public class UserResponse {
    private String userId;
    private String email;
    private String nickname;
    private String displayName;
    private String profileImageUrl;
    private String preferredLanguage;
    private LocalDate lastCookingDate;
    private LocalDateTime lastLoginDate;
    private LocalDateTime createdAt;
    private String timezone;
    private boolean marketingOptOut;
    private List<String> roles;

    /**
     * UserエンティティからUserResponseを生成（rolesなし）
     */
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getUserId(),
                user.getEmail(),
                user.getNickname(),
                user.getDisplayName(),
                user.getProfileImageUrl(),
                user.getPreferredLanguage().getCode(),
                user.getLastCookingDate(),
                user.getLastLoginDate(),
                user.getCreatedAt(),
                user.getTimezone(),
                user.isMarketingOptOut(),
                null);
    }

    /**
     * UserエンティティとrolesからUserResponseを生成
     */
    public static UserResponse from(User user, List<String> roles) {
        return new UserResponse(
                user.getUserId(),
                user.getEmail(),
                user.getNickname(),
                user.getDisplayName(),
                user.getProfileImageUrl(),
                user.getPreferredLanguage().getCode(),
                user.getLastCookingDate(),
                user.getLastLoginDate(),
                user.getCreatedAt(),
                user.getTimezone(),
                user.isMarketingOptOut(),
                roles);
    }
}
