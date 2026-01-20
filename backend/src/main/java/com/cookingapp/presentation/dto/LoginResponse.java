package com.cookingapp.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class LoginResponse {

    private final String accessToken;
    private final String idToken;
    private final String refreshToken;
    private final Integer expiresIn;
    private final UserProfileResponse user;
}
