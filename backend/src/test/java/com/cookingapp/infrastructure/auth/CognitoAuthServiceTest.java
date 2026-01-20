package com.cookingapp.infrastructure.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthFlowType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthenticationResultType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.NotAuthorizedException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CognitoAuthService")
class CognitoAuthServiceTest {

    @Mock
    private CognitoIdentityProviderClient cognitoClient;

    private CognitoAuthService cognitoAuthService;

    private static final String CLIENT_ID = "test-client-id";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_ACCESS_TOKEN = "access-token-xyz";
    private static final String TEST_ID_TOKEN = "id-token-xyz";
    private static final String TEST_REFRESH_TOKEN = "refresh-token-xyz";
    private static final Integer TEST_EXPIRES_IN = 3600;

    @BeforeEach
    void setUp() {
        cognitoAuthService = new CognitoAuthService(cognitoClient, CLIENT_ID);
    }

    @Nested
    @DisplayName("signIn")
    class SignIn {

        @Nested
        @DisplayName("正常系")
        class Success {

            @Test
            @DisplayName("有効な認証情報でサインイン成功時、AuthResultを返す")
            void shouldReturnAuthResultWhenCredentialsAreValid() {
                // Arrange
                AuthenticationResultType authResult = AuthenticationResultType.builder()
                        .accessToken(TEST_ACCESS_TOKEN)
                        .idToken(TEST_ID_TOKEN)
                        .refreshToken(TEST_REFRESH_TOKEN)
                        .expiresIn(TEST_EXPIRES_IN)
                        .build();

                InitiateAuthResponse response = InitiateAuthResponse.builder()
                        .authenticationResult(authResult)
                        .build();

                when(cognitoClient.initiateAuth(any(InitiateAuthRequest.class))).thenReturn(response);

                // Act
                AuthResult result = cognitoAuthService.signIn(TEST_EMAIL, TEST_PASSWORD);

                // Assert
                assertThat(result).isNotNull();
                assertThat(result.getAccessToken()).isEqualTo(TEST_ACCESS_TOKEN);
                assertThat(result.getIdToken()).isEqualTo(TEST_ID_TOKEN);
                assertThat(result.getRefreshToken()).isEqualTo(TEST_REFRESH_TOKEN);
                assertThat(result.getExpiresIn()).isEqualTo(TEST_EXPIRES_IN);
            }

            @Test
            @DisplayName("Cognitoに正しいリクエストパラメータを送信する")
            void shouldSendCorrectRequestParametersToCognito() {
                // Arrange
                AuthenticationResultType authResult = AuthenticationResultType.builder()
                        .accessToken(TEST_ACCESS_TOKEN)
                        .idToken(TEST_ID_TOKEN)
                        .refreshToken(TEST_REFRESH_TOKEN)
                        .expiresIn(TEST_EXPIRES_IN)
                        .build();

                InitiateAuthResponse response = InitiateAuthResponse.builder()
                        .authenticationResult(authResult)
                        .build();

                when(cognitoClient.initiateAuth(any(InitiateAuthRequest.class))).thenReturn(response);

                // Act
                cognitoAuthService.signIn(TEST_EMAIL, TEST_PASSWORD);

                // Assert
                ArgumentCaptor<InitiateAuthRequest> requestCaptor = ArgumentCaptor.forClass(InitiateAuthRequest.class);
                verify(cognitoClient).initiateAuth(requestCaptor.capture());

                InitiateAuthRequest capturedRequest = requestCaptor.getValue();
                assertThat(capturedRequest.authFlow()).isEqualTo(AuthFlowType.USER_PASSWORD_AUTH);
                assertThat(capturedRequest.clientId()).isEqualTo(CLIENT_ID);
                assertThat(capturedRequest.authParameters()).containsEntry("USERNAME", TEST_EMAIL);
                assertThat(capturedRequest.authParameters()).containsEntry("PASSWORD", TEST_PASSWORD);
            }
        }

        @Nested
        @DisplayName("異常系")
        class Failure {

            @Test
            @DisplayName("パスワードが間違っている場合、AuthenticationExceptionをスローする")
            void shouldThrowAuthenticationExceptionWhenPasswordIsIncorrect() {
                // Arrange
                when(cognitoClient.initiateAuth(any(InitiateAuthRequest.class)))
                        .thenThrow(NotAuthorizedException.builder()
                                .message("Incorrect username or password")
                                .build());

                // Act & Assert
                assertThatThrownBy(() -> cognitoAuthService.signIn(TEST_EMAIL, TEST_PASSWORD))
                        .isInstanceOf(AuthenticationException.class)
                        .hasMessage("Invalid email or password");

                verify(cognitoClient).initiateAuth(any(InitiateAuthRequest.class));
            }

            @Test
            @DisplayName("ユーザーが存在しない場合、AuthenticationExceptionをスローする")
            void shouldThrowAuthenticationExceptionWhenUserNotFound() {
                // Arrange
                when(cognitoClient.initiateAuth(any(InitiateAuthRequest.class)))
                        .thenThrow(UserNotFoundException.builder()
                                .message("User does not exist")
                                .build());

                // Act & Assert
                assertThatThrownBy(() -> cognitoAuthService.signIn(TEST_EMAIL, TEST_PASSWORD))
                        .isInstanceOf(AuthenticationException.class)
                        .hasMessage("Invalid email or password");

                verify(cognitoClient).initiateAuth(any(InitiateAuthRequest.class));
            }

            @Test
            @DisplayName("NotAuthorizedExceptionとUserNotFoundExceptionで同じエラーメッセージを返す（セキュリティ対策）")
            void shouldReturnSameErrorMessageForBothExceptions() {
                // Arrange - NotAuthorizedException
                when(cognitoClient.initiateAuth(any(InitiateAuthRequest.class)))
                        .thenThrow(NotAuthorizedException.builder()
                                .message("Incorrect username or password")
                                .build());

                // Act & Assert
                assertThatThrownBy(() -> cognitoAuthService.signIn(TEST_EMAIL, TEST_PASSWORD))
                        .isInstanceOf(AuthenticationException.class)
                        .hasMessage("Invalid email or password");

                // Reset and test UserNotFoundException
                when(cognitoClient.initiateAuth(any(InitiateAuthRequest.class)))
                        .thenThrow(UserNotFoundException.builder()
                                .message("User does not exist")
                                .build());

                assertThatThrownBy(() -> cognitoAuthService.signIn(TEST_EMAIL, "wrong-password"))
                        .isInstanceOf(AuthenticationException.class)
                        .hasMessage("Invalid email or password");
            }
        }
    }
}
