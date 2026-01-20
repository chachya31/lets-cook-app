package com.cookingapp.infrastructure.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthFlowType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthenticationResultType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.NotAuthorizedException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserNotFoundException;

import java.util.Map;

@Service
public class CognitoAuthService {

    private static final Logger logger = LoggerFactory.getLogger(CognitoAuthService.class);

    private final CognitoIdentityProviderClient cognitoClient;
    private final String userPoolClientId;

    public CognitoAuthService(
            CognitoIdentityProviderClient cognitoClient,
            @Value("${aws.cognito.user-pool-client-id}") String userPoolClientId) {
        this.cognitoClient = cognitoClient;
        this.userPoolClientId = userPoolClientId;
    }

    public AuthResult signIn(String email, String password) {
        try {
            InitiateAuthRequest authRequest = InitiateAuthRequest.builder()
                    .authFlow(AuthFlowType.USER_PASSWORD_AUTH)
                    .clientId(userPoolClientId)
                    .authParameters(Map.of(
                            "USERNAME", email,
                            "PASSWORD", password
                    ))
                    .build();

            InitiateAuthResponse authResponse = cognitoClient.initiateAuth(authRequest);
            AuthenticationResultType authResult = authResponse.authenticationResult();

            logger.info("User signed in successfully: {}", email);

            return AuthResult.builder()
                    .accessToken(authResult.accessToken())
                    .idToken(authResult.idToken())
                    .refreshToken(authResult.refreshToken())
                    .expiresIn(authResult.expiresIn())
                    .build();

        } catch (NotAuthorizedException e) {
            logger.warn("Authentication failed for user: {}", email);
            throw new AuthenticationException("Invalid email or password");
        } catch (UserNotFoundException e) {
            logger.warn("User not found: {}", email);
            throw new AuthenticationException("Invalid email or password");
        }
    }
}
