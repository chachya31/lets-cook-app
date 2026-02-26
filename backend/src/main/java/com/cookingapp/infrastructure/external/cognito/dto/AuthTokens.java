package com.cookingapp.infrastructure.external.cognito.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 認証トークンを保持するDTO
 */
@Getter
@AllArgsConstructor
public class AuthTokens {
    private final String accessToken;
    private final String refreshToken;
    private final String idToken;
    private final int expiresIn;
}
