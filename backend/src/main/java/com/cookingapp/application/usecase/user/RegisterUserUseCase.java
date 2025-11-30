package com.cookingapp.application.usecase.user;

import com.cookingapp.domain.entity.User;
import com.cookingapp.domain.exception.UserAlreadyExistsException;
import com.cookingapp.domain.repository.UserRepository;
import com.cookingapp.domain.valueobject.Language;
import com.cookingapp.infrastructure.external.cognito.CognitoAuthService;
import org.springframework.stereotype.Service;

/**
 * 繝ｦ繝ｼ繧ｶ繝ｼ逋ｻ骭ｲ繝ｦ繝ｼ繧ｹ繧ｱ繝ｼ繧ｹ
 */
@Service
public class RegisterUserUseCase {
    
    private final CognitoAuthService cognitoAuthService;
    private final UserRepository userRepository;

    public RegisterUserUseCase(CognitoAuthService cognitoAuthService, UserRepository userRepository) {
        this.cognitoAuthService = cognitoAuthService;
        this.userRepository = userRepository;
    }

    /**
     * 繝ｦ繝ｼ繧ｶ繝ｼ繧堤匳骭ｲ
     * 
     * @param email 繝｡繝ｼ繝ｫ繧｢繝峨Ξ繧ｹ
     * @param password 繝代せ繝ｯ繝ｼ繝・
     * @param nickname 繝九ャ繧ｯ繝阪・繝
     * @param preferredLanguage 蜆ｪ蜈郁ｨ隱・
     * @return 逋ｻ骭ｲ縺輔ｌ縺溘Θ繝ｼ繧ｶ繝ｼ
     * @throws UserAlreadyExistsException 繝ｦ繝ｼ繧ｶ繝ｼ縺梧里縺ｫ蟄伜惠縺吶ｋ蝣ｴ蜷・
     */
    public User execute(String email, String password, String nickname, Language preferredLanguage) {
        // 繝代せ繝ｯ繝ｼ繝峨ヰ繝ｪ繝・・繧ｷ繝ｧ繝ｳ
        if (!User.validatePassword(password)) {
            throw new IllegalArgumentException(
                "Password must be at least 8 characters long and contain uppercase, lowercase, numbers, and special characters"
            );
        }

        // 繝｡繝ｼ繝ｫ繧｢繝峨Ξ繧ｹ縺ｮ驥崎､・メ繧ｧ繝・け
        if (userRepository.findByEmail(email).isPresent()) {
            throw new UserAlreadyExistsException("User with email " + email + " already exists");
        }

        // Cognito縺ｫ繝ｦ繝ｼ繧ｶ繝ｼ逋ｻ骭ｲ
        String cognitoUserId = cognitoAuthService.signUp(email, password, nickname);

        // 繝ｦ繝ｼ繧ｶ繝ｼ繧ｨ繝ｳ繝・ぅ繝・ぅ菴懈・
        User user = new User(email, nickname, preferredLanguage);

        // DynamoDB縺ｫ菫晏ｭ・
        return userRepository.save(user);
    }
}
