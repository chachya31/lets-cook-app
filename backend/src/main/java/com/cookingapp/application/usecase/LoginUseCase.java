package com.cookingapp.application.usecase;

import com.cookingapp.domain.model.User;
import com.cookingapp.domain.repository.UserRepository;
import com.cookingapp.infrastructure.auth.AuthResult;
import com.cookingapp.infrastructure.auth.CognitoAuthService;
import com.cookingapp.presentation.dto.LoginResponse;
import com.cookingapp.presentation.dto.UserProfileResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
public class LoginUseCase {

    private static final Logger logger = LoggerFactory.getLogger(LoginUseCase.class);

    private final CognitoAuthService cognitoAuthService;
    private final UserRepository userRepository;

    public LoginUseCase(CognitoAuthService cognitoAuthService, UserRepository userRepository) {
        this.cognitoAuthService = cognitoAuthService;
        this.userRepository = userRepository;
    }

    public LoginResponse execute(String email, String password) {
        // 1. Authenticate with Cognito
        AuthResult authResult = cognitoAuthService.signIn(email, password);

        // 2. Get user details from DynamoDB
        Optional<User> userOptional = userRepository.findByEmail(email);

        UserProfileResponse userProfile = null;

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            // 3. Update last login date
            user.setLastLoginDate(Instant.now().toString());
            userRepository.save(user);

            logger.info("User logged in and LastLoginDate updated: {}", email);

            userProfile = UserProfileResponse.from(user);
        } else {
            // User authenticated via Cognito but not found in DynamoDB
            // This could happen if user registration in DynamoDB failed previously
            logger.warn("User authenticated but not found in DynamoDB: {}", email);
        }

        // 4. Build and return response
        return LoginResponse.builder()
                .accessToken(authResult.getAccessToken())
                .idToken(authResult.getIdToken())
                .refreshToken(authResult.getRefreshToken())
                .expiresIn(authResult.getExpiresIn())
                .user(userProfile)
                .build();
    }
}
