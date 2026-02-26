package com.cookingapp.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * ログインレスポンス
 */
@Getter
@AllArgsConstructor
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private String idToken;
    private int expiresIn;
    private UserResponse user;
}
