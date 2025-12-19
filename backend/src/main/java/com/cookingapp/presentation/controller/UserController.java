package com.cookingapp.presentation.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.cookingapp.application.usecase.user.ConfirmSignUpUseCase;
import com.cookingapp.application.usecase.user.DeleteUserAccountUseCase;
import com.cookingapp.application.usecase.user.GetUserProfileUseCase;
import com.cookingapp.application.usecase.user.LoginUserUseCase;
import com.cookingapp.application.usecase.user.RegisterUserUseCase;
import com.cookingapp.application.usecase.user.ResendConfirmationCodeUseCase;
import com.cookingapp.application.usecase.user.UpdateUserProfileUseCase;
import com.cookingapp.application.usecase.user.UploadProfileImageUseCase;
import com.cookingapp.domain.entity.User;
import com.cookingapp.domain.valueobject.Language;
import com.cookingapp.presentation.dto.ConfirmSignUpRequest;
import com.cookingapp.presentation.dto.LoginRequest;
import com.cookingapp.presentation.dto.LoginResponse;
import com.cookingapp.presentation.dto.RegisterUserRequest;
import com.cookingapp.presentation.dto.ResendConfirmationCodeRequest;
import com.cookingapp.presentation.dto.UpdateProfileRequest;
import com.cookingapp.presentation.dto.UserResponse;
import com.cookingapp.presentation.mapper.UserMapper;

import jakarta.validation.Valid;

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
    private final UploadProfileImageUseCase uploadProfileImageUseCase;
    private final ConfirmSignUpUseCase confirmSignUpUseCase;
    private final ResendConfirmationCodeUseCase resendConfirmationCodeUseCase;

    public UserController(
            RegisterUserUseCase registerUserUseCase,
            LoginUserUseCase loginUserUseCase,
            GetUserProfileUseCase getUserProfileUseCase,
            UpdateUserProfileUseCase updateUserProfileUseCase,
            DeleteUserAccountUseCase deleteUserAccountUseCase,
            UploadProfileImageUseCase uploadProfileImageUseCase,
            ConfirmSignUpUseCase confirmSignUpUseCase,
            ResendConfirmationCodeUseCase resendConfirmationCodeUseCase) {
        this.registerUserUseCase = registerUserUseCase;
        this.loginUserUseCase = loginUserUseCase;
        this.getUserProfileUseCase = getUserProfileUseCase;
        this.updateUserProfileUseCase = updateUserProfileUseCase;
        this.deleteUserAccountUseCase = deleteUserAccountUseCase;
        this.uploadProfileImageUseCase = uploadProfileImageUseCase;
        this.confirmSignUpUseCase = confirmSignUpUseCase;
        this.resendConfirmationCodeUseCase = resendConfirmationCodeUseCase;
    }

    /**
     * ユーザー登録
     * POST /api/users/register
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterUserRequest request) {
        Language language = UserMapper.toLanguage(request);

        User user = registerUserUseCase.execute(
                request.getEmail(),
                request.getPassword(),
                request.getNickname(),
                language);

        return ResponseEntity.status(HttpStatus.CREATED).body(UserMapper.toResponse(user));
    }

    /**
     * メール確認コード検証
     * POST /api/users/confirm
     */
    @PostMapping("/confirm")
    public ResponseEntity<Void> confirmSignUp(@Valid @RequestBody ConfirmSignUpRequest request) {
        confirmSignUpUseCase.execute(request.getEmail(), request.getConfirmationCode());
        return ResponseEntity.ok().build();
    }

    /**
     * 確認コード再送信
     * POST /api/users/resend-code
     */
    @PostMapping("/resend-code")
    public ResponseEntity<Void> resendConfirmationCode(@Valid @RequestBody ResendConfirmationCodeRequest request) {
        resendConfirmationCodeUseCase.execute(request.getEmail());
        return ResponseEntity.ok().build();
    }

    /**
     * ログイン
     * POST /api/users/login
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginUserUseCase.LoginResult result = loginUserUseCase.execute(
                request.getEmail(),
                request.getPassword());

        LoginResponse response = new LoginResponse(
                result.getTokens().getAccessToken(),
                result.getTokens().getRefreshToken(),
                result.getTokens().getIdToken(),
                result.getTokens().getExpiresIn(),
                UserMapper.toResponse(result.getUser()));

        return ResponseEntity.ok(response);
    }

    /**
     * プロフィール取得
     * GET /api/users/profile/{userId}
     */
    @GetMapping("/profile/{userId}")
    public ResponseEntity<UserResponse> getProfile(@PathVariable("userId") String userId) {
        User user = getUserProfileUseCase.execute(userId);
        return ResponseEntity.ok(UserMapper.toResponse(user));
    }

    /**
     * プロフィール更新
     * PUT /api/users/profile/{userId}
     */
    @PutMapping("/profile/{userId}")
    public ResponseEntity<UserResponse> updateProfile(
            @PathVariable("userId") String userId,
            @Valid @RequestBody UpdateProfileRequest request) {

        Language language = UserMapper.toLanguageOrNull(request);

        User user = updateUserProfileUseCase.execute(
                userId,
                request.getNickname(),
                request.getDisplayName(),
                language,
                request.getTimezone(),
                request.getMarketingOptOut() != null ? request.getMarketingOptOut() : false);

        return ResponseEntity.ok(UserMapper.toResponse(user));
    }

    /**
     * アカウント削除
     * DELETE /api/users/account/{userId}
     */
    @DeleteMapping("/account/{userId}")
    public ResponseEntity<Void> deleteAccount(@PathVariable("userId") String userId) {
        deleteUserAccountUseCase.execute(userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * プロフィール画像アップロード
     * POST /api/users/profile/{userId}/image
     */
    @PostMapping(value = "/profile/{userId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserResponse> uploadProfileImage(
            @PathVariable("userId") String userId,
            @RequestParam("file") MultipartFile file) {
        User user = uploadProfileImageUseCase.execute(userId, file);
        return ResponseEntity.ok(UserMapper.toResponse(user));
    }
}
