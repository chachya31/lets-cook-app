package com.cookingapp.presentation.controller;

import com.cookingapp.application.usecase.*;
import com.cookingapp.domain.entity.User;
import com.cookingapp.domain.valueobject.Language;
import com.cookingapp.presentation.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * ユーザー管理コントローラー
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUserUseCase loginUserUseCase;
    private final GetUserProfileUseCase getUserProfileUseCase;
    private final UpdateUserProfileUseCase updateUserProfileUseCase;
    private final DeleteUserAccountUseCase deleteUserAccountUseCase;

    public UserController(
            RegisterUserUseCase registerUserUseCase,
            LoginUserUseCase loginUserUseCase,
            GetUserProfileUseCase getUserProfileUseCase,
            UpdateUserProfileUseCase updateUserProfileUseCase,
            DeleteUserAccountUseCase deleteUserAccountUseCase) {
        this.registerUserUseCase = registerUserUseCase;
        this.loginUserUseCase = loginUserUseCase;
        this.getUserProfileUseCase = getUserProfileUseCase;
        this.updateUserProfileUseCase = updateUserProfileUseCase;
        this.deleteUserAccountUseCase = deleteUserAccountUseCase;
    }

    /**
     * ユーザー登録
     * POST /api/users/register
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterUserRequest request) {
        Language language = request.getPreferredLanguage() != null
                ? Language.fromCode(request.getPreferredLanguage())
                : Language.JA;

        User user = registerUserUseCase.execute(
                request.getEmail(),
                request.getPassword(),
                request.getNickname(),
                language
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(user));
    }

    /**
     * ログイン
     * POST /api/users/login
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginUserUseCase.LoginResult result = loginUserUseCase.execute(
                request.getEmail(),
                request.getPassword()
        );

        LoginResponse response = new LoginResponse(
                result.getTokens().getAccessToken(),
                result.getTokens().getRefreshToken(),
                result.getTokens().getIdToken(),
                result.getTokens().getExpiresIn(),
                UserResponse.from(result.getUser())
        );

        return ResponseEntity.ok(response);
    }

    /**
     * プロフィール取得
     * GET /api/users/profile/{userId}
     */
    @GetMapping("/profile/{userId}")
    public ResponseEntity<UserResponse> getProfile(@PathVariable String userId) {
        User user = getUserProfileUseCase.execute(userId);
        return ResponseEntity.ok(UserResponse.from(user));
    }

    /**
     * プロフィール更新
     * PUT /api/users/profile/{userId}
     */
    @PutMapping("/profile/{userId}")
    public ResponseEntity<UserResponse> updateProfile(
            @PathVariable String userId,
            @Valid @RequestBody UpdateProfileRequest request) {

        Language language = request.getPreferredLanguage() != null
                ? Language.fromCode(request.getPreferredLanguage())
                : null;

        User user = updateUserProfileUseCase.execute(
                userId,
                request.getNickname(),
                request.getDisplayName(),
                language,
                request.getTimezone(),
                request.getMarketingOptOut() != null ? request.getMarketingOptOut() : false
        );

        return ResponseEntity.ok(UserResponse.from(user));
    }

    /**
     * アカウント削除
     * DELETE /api/users/account/{userId}
     */
    @DeleteMapping("/account/{userId}")
    public ResponseEntity<Void> deleteAccount(@PathVariable String userId) {
        deleteUserAccountUseCase.execute(userId);
        return ResponseEntity.noContent().build();
    }
}
