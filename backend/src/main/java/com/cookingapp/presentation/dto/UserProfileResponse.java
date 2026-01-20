package com.cookingapp.presentation.dto;

import com.cookingapp.domain.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class UserProfileResponse {

    private final String userId;
    private final String email;
    private final String nickname;
    private final String displayName;
    private final String profileImageUrl;
    private final String preferredLanguage;
    private final String timezone;

    public static UserProfileResponse from(User user) {
        return UserProfileResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .displayName(user.getDisplayName())
                .profileImageUrl(user.getProfileImageUrl())
                .preferredLanguage(user.getPreferredLanguage())
                .timezone(user.getTimezone())
                .build();
    }
}
