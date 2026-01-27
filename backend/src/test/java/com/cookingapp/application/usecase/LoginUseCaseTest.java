package com.cookingapp.application.usecase;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.cookingapp.domain.model.User;
import com.cookingapp.domain.repository.UserRepository;
import com.cookingapp.infrastructure.auth.AuthResult;
import com.cookingapp.infrastructure.auth.AuthenticationException;
import com.cookingapp.infrastructure.auth.CognitoAuthService;
import com.cookingapp.presentation.dto.LoginResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoginUseCase")
class LoginUseCaseTest {

    @Mock
    private CognitoAuthService cognitoAuthService;

    @Mock
    private UserRepository userRepository;

    private LoginUseCase loginUseCase;

    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_USER_ID = "user-123";
    private static final String TEST_COGNITO_SUB = "cognito-sub-12345";
    private static final String TEST_ACCESS_TOKEN = "access-token-xyz";
    private static final String TEST_REFRESH_TOKEN = "refresh-token-xyz";
    private static final Integer TEST_EXPIRES_IN = 3600;

    // テスト用の有効なJWTを生成
    private static final String TEST_ID_TOKEN = JWT.create()
            .withSubject(TEST_COGNITO_SUB)
            .withClaim("email", TEST_EMAIL)
            .sign(Algorithm.none());

    @BeforeEach
    void setUp() {
        loginUseCase = new LoginUseCase(cognitoAuthService, userRepository);
    }

    @Nested
    @DisplayName("execute")
    class Execute {

        @Nested
        @DisplayName("正常系")
        class Success {

            @Test
            @DisplayName("ユーザーがDynamoDBに存在する場合、ログイン成功しユーザー情報を含むレスポンスを返す")
            void shouldReturnLoginResponseWithUserWhenUserExistsInDynamoDB() {
                // Arrange
                AuthResult authResult = createAuthResult();
                User user = createUser();

                when(cognitoAuthService.signIn(TEST_EMAIL, TEST_PASSWORD)).thenReturn(authResult);
                when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(user));
                when(userRepository.save(any(User.class))).thenReturn(user);

                // Act
                LoginResponse response = loginUseCase.execute(TEST_EMAIL, TEST_PASSWORD);

                // Assert
                assertThat(response).isNotNull();
                assertThat(response.getAccessToken()).isEqualTo(TEST_ACCESS_TOKEN);
                assertThat(response.getIdToken()).isEqualTo(TEST_ID_TOKEN);
                assertThat(response.getRefreshToken()).isEqualTo(TEST_REFRESH_TOKEN);
                assertThat(response.getExpiresIn()).isEqualTo(TEST_EXPIRES_IN);
                assertThat(response.getUser()).isNotNull();
                assertThat(response.getUser().getUserId()).isEqualTo(TEST_COGNITO_SUB);
                assertThat(response.getUser().getEmail()).isEqualTo(TEST_EMAIL);
                assertThat(response.getUser().getNickname()).isEqualTo("testuser");

                verify(cognitoAuthService).signIn(TEST_EMAIL, TEST_PASSWORD);
                verify(userRepository).findByEmail(TEST_EMAIL);
                verify(userRepository).save(any(User.class));
            }

            @Test
            @DisplayName("ユーザーがDynamoDBに存在しない場合、ログイン成功しユーザー情報なしのレスポンスを返す")
            void shouldReturnLoginResponseWithoutUserWhenUserNotInDynamoDB() {
                // Arrange
                AuthResult authResult = createAuthResult();

                when(cognitoAuthService.signIn(TEST_EMAIL, TEST_PASSWORD)).thenReturn(authResult);
                when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());

                // Act
                LoginResponse response = loginUseCase.execute(TEST_EMAIL, TEST_PASSWORD);

                // Assert
                assertThat(response).isNotNull();
                assertThat(response.getAccessToken()).isEqualTo(TEST_ACCESS_TOKEN);
                assertThat(response.getIdToken()).isEqualTo(TEST_ID_TOKEN);
                assertThat(response.getRefreshToken()).isEqualTo(TEST_REFRESH_TOKEN);
                assertThat(response.getExpiresIn()).isEqualTo(TEST_EXPIRES_IN);
                assertThat(response.getUser()).isNull();

                verify(cognitoAuthService).signIn(TEST_EMAIL, TEST_PASSWORD);
                verify(userRepository).findByEmail(TEST_EMAIL);
                verify(userRepository, never()).save(any(User.class));
            }

            @Test
            @DisplayName("ログイン成功時にLastLoginDateが更新される")
            void shouldUpdateLastLoginDateOnSuccessfulLogin() {
                // Arrange
                AuthResult authResult = createAuthResult();
                User user = createUser();

                when(cognitoAuthService.signIn(TEST_EMAIL, TEST_PASSWORD)).thenReturn(authResult);
                when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(user));
                when(userRepository.save(any(User.class))).thenReturn(user);

                // Act
                loginUseCase.execute(TEST_EMAIL, TEST_PASSWORD);

                // Assert
                verify(userRepository).save(any(User.class));
                assertThat(user.getLastLoginDate()).isNotNull();
            }
        }

        @Nested
        @DisplayName("異常系")
        class Failure {

            @Test
            @DisplayName("Cognito認証に失敗した場合、AuthenticationExceptionがスローされる")
            void shouldThrowAuthenticationExceptionWhenCognitoAuthFails() {
                // Arrange
                when(cognitoAuthService.signIn(TEST_EMAIL, TEST_PASSWORD))
                        .thenThrow(new AuthenticationException("Invalid email or password"));

                // Act & Assert
                assertThatThrownBy(() -> loginUseCase.execute(TEST_EMAIL, TEST_PASSWORD))
                        .isInstanceOf(AuthenticationException.class)
                        .hasMessage("Invalid email or password");

                verify(cognitoAuthService).signIn(TEST_EMAIL, TEST_PASSWORD);
                verify(userRepository, never()).findByEmail(anyString());
                verify(userRepository, never()).save(any(User.class));
            }

            @Test
            @DisplayName("ユーザーがCognitoに存在しない場合、AuthenticationExceptionがスローされる")
            void shouldThrowAuthenticationExceptionWhenUserNotFound() {
                // Arrange
                when(cognitoAuthService.signIn(TEST_EMAIL, TEST_PASSWORD))
                        .thenThrow(new AuthenticationException("Invalid email or password"));

                // Act & Assert
                assertThatThrownBy(() -> loginUseCase.execute(TEST_EMAIL, TEST_PASSWORD))
                        .isInstanceOf(AuthenticationException.class)
                        .hasMessage("Invalid email or password");

                verify(cognitoAuthService).signIn(TEST_EMAIL, TEST_PASSWORD);
            }
        }
    }

    private AuthResult createAuthResult() {
        return AuthResult.builder()
                .accessToken(TEST_ACCESS_TOKEN)
                .idToken(TEST_ID_TOKEN)
                .refreshToken(TEST_REFRESH_TOKEN)
                .expiresIn(TEST_EXPIRES_IN)
                .build();
    }

    private User createUser() {
        return User.builder()
                .userId(TEST_USER_ID)
                .email(TEST_EMAIL)
                .nickname("testuser")
                .displayName("Test User")
                .profileImageUrl("https://example.com/image.jpg")
                .preferredLanguage("ja")
                .timezone("Asia/Tokyo")
                .createdAt("2024-01-01T00:00:00Z")
                .marketingOptOut(false)
                .build();
    }
}
