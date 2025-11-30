package com.cookingapp.application.usecase.user;

import com.cookingapp.domain.entity.User;
import com.cookingapp.domain.exception.AuthenticationException;
import com.cookingapp.domain.repository.UserRepository;
import com.cookingapp.domain.valueobject.Language;
import com.cookingapp.infrastructure.external.cognito.CognitoAuthService;
import com.cookingapp.infrastructure.external.cognito.dto.AuthTokens;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 繝ｭ繧ｰ繧､繝ｳ繝ｦ繝ｼ繧ｹ繧ｱ繝ｼ繧ｹ
 */
@Service
public class LoginUserUseCase {
    
    private static final Logger logger = LoggerFactory.getLogger(LoginUserUseCase.class);
    
    private final CognitoAuthService cognitoAuthService;
    private final UserRepository userRepository;

    public LoginUserUseCase(CognitoAuthService cognitoAuthService, UserRepository userRepository) {
        this.cognitoAuthService = cognitoAuthService;
        this.userRepository = userRepository;
    }

    /**
     * 繝ｦ繝ｼ繧ｶ繝ｼ繧偵Ο繧ｰ繧､繝ｳ
     * 
     * @param email 繝｡繝ｼ繝ｫ繧｢繝峨Ξ繧ｹ
     * @param password 繝代せ繝ｯ繝ｼ繝・
     * @return 隱崎ｨｼ繝医・繧ｯ繝ｳ縺ｨ繝ｦ繝ｼ繧ｶ繝ｼ諠・ｱ
     * @throws AuthenticationException 隱崎ｨｼ縺ｫ螟ｱ謨励＠縺溷ｴ蜷・
     */
    public LoginResult execute(String email, String password) {
        // Cognito縺ｧ隱崎ｨｼ
        AuthTokens tokens = cognitoAuthService.signIn(email, password);

        // 繝ｦ繝ｼ繧ｶ繝ｼ諠・ｱ繧貞叙蠕励∝ｭ伜惠縺励↑縺・ｴ蜷医・菴懈・
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    logger.info("User not found in database, creating new user from Cognito: {}", email);
                    return createUserFromCognito(email, tokens.getAccessToken());
                });

        // 譛邨ゅΟ繧ｰ繧､繝ｳ譌･譎ゅｒ譖ｴ譁ｰ
        user.updateLastLoginDate(LocalDateTime.now());
        userRepository.save(user);

        return new LoginResult(tokens, user);
    }
    
    /**
     * Cognito縺ｮ繝ｦ繝ｼ繧ｶ繝ｼ諠・ｱ縺九ｉDynamoDB縺ｫ繝ｦ繝ｼ繧ｶ繝ｼ繧剃ｽ懈・
     */
    private User createUserFromCognito(String email, String accessToken) {
        try {
            // Cognito縺九ｉ繝ｦ繝ｼ繧ｶ繝ｼ螻樊ｧ繧貞叙蠕・
            Map<String, String> attributes = cognitoAuthService.getUserAttributes(accessToken);
            
            String nickname = attributes.getOrDefault("nickname", email.split("@")[0]);
            String preferredLanguageCode = attributes.getOrDefault("locale", "ja");
            Language preferredLanguage = Language.fromCode(preferredLanguageCode);
            
            // 譁ｰ縺励＞繝ｦ繝ｼ繧ｶ繝ｼ繧剃ｽ懈・
            User newUser = new User(email, nickname, preferredLanguage);
            
            logger.info("Creating new user: email={}, nickname={}, language={}", 
                    email, nickname, preferredLanguage.getCode());
            
            return userRepository.save(newUser);
            
        } catch (Exception e) {
            logger.error("Failed to create user from Cognito: {}", email, e);
            throw new AuthenticationException("Failed to create user: " + e.getMessage(), e);
        }
    }

    /**
     * 繝ｭ繧ｰ繧､繝ｳ邨先棡
     */
    @lombok.Getter
    @lombok.AllArgsConstructor
    public static class LoginResult {
        private final AuthTokens tokens;
        private final User user;
    }
}
