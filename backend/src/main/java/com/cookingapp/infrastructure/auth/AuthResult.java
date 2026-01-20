package com.cookingapp.infrastructure.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AuthResult {

    private final String accessToken;
    private final String idToken;
    private final String refreshToken;
    private final Integer expiresIn;
}
