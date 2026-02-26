package com.cookingapp.application.usecase.user;

import java.time.LocalDateTime;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.cookingapp.domain.entity.User;
import com.cookingapp.domain.exception.AuthenticationException;
import com.cookingapp.domain.repository.UserRepository;
import com.cookingapp.domain.service.AuthService;
import com.cookingapp.domain.service.ImageStorageService;
import com.cookingapp.domain.valueobject.Language;
import com.cookingapp.infrastructure.external.cognito.dto.AuthTokens;

/**
 * ログインユースケース
 */
@Service
public class LoginUserUseCase {

    private static final Logger logger = LoggerFactory.getLogger(LoginUserUseCase.class);

    private final AuthService authService;
    private final UserRepository userRepository;
    private final ImageStorageService imageStorageService;

    public LoginUserUseCase(AuthService authService, UserRepository userRepository,
            ImageStorageService imageStorageService) {
        this.authService = authService;
        this.userRepository = userRepository;
        this.imageStorageService = imageStorageService;
    }

    /**
     * ユーザーをログイン
     * 
     * @param email    メールアドレス
     * @param password パスワード
     * @return 認証トークンとユーザー情報
     * @throws AuthenticationException 認証に失敗した場合
     */
    public LoginResult execute(String email, String password) {
        // Cognitoで認証
        AuthTokens tokens = authService.signIn(email, password);

        // ユーザー情報を取得、存在しない場合は作成
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    logger.info("User not found in database, creating new user from Cognito: {}", email);
                    return createUserFromCognito(email, tokens.getAccessToken());
                });

        // 最終ログイン日時を更新
        user.updateLastLoginDate(LocalDateTime.now());
        userRepository.save(user);

        // プロフィール画像のPresigned URLを生成
        if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
            String presignedUrl = imageStorageService.generatePresignedUrl(user.getProfileImageUrl());
            user.updateProfileImageUrl(presignedUrl);
        }

        return new LoginResult(tokens, user);
    }

    /**
     * Cognitoのユーザー情報からDynamoDBにユーザーを作成
     */
    private User createUserFromCognito(String email, String accessToken) {
        try {
            // Cognitoからユーザー属性を取得
            Map<String, String> attributes = authService.getUserAttributes(accessToken);

            String nickname = attributes.getOrDefault("nickname", email.split("@")[0]);
            String preferredLanguageCode = attributes.getOrDefault("locale", "ja");
            Language preferredLanguage = Language.fromCode(preferredLanguageCode);

            // Cognito subを取得（userIdとして使用）
            String cognitoSub = attributes.get("sub");
            if (cognitoSub == null || cognitoSub.isEmpty()) {
                throw new AuthenticationException("Failed to get Cognito sub from user attributes", null);
            }

            // 新しいユーザーを作成（Cognito subをuserIdとして使用）
            User newUser = new User(cognitoSub, email, nickname, preferredLanguage);

            logger.info("Creating new user: userId={}, email={}, nickname={}, language={}",
                    cognitoSub, email, nickname, preferredLanguage.getCode());

            return userRepository.save(newUser);

        } catch (Exception e) {
            logger.error("Failed to create user from Cognito: {}", email, e);
            throw new AuthenticationException("Failed to create user: " + e.getMessage(), e);
        }
    }

    /**
     * ログイン結果
     */
    @lombok.Getter
    @lombok.AllArgsConstructor
    public static class LoginResult {
        private final AuthTokens tokens;
        private final User user;
    }
}
