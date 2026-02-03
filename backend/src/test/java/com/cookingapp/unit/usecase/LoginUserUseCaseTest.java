package com.cookingapp.unit.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cookingapp.application.usecase.user.LoginUserUseCase;
import com.cookingapp.domain.entity.User;
import com.cookingapp.domain.exception.AuthenticationException;
import com.cookingapp.domain.repository.UserRepository;
import com.cookingapp.domain.service.AuthService;
import com.cookingapp.domain.service.ImageStorageService;
import com.cookingapp.domain.valueobject.Language;
import com.cookingapp.infrastructure.external.cognito.dto.AuthTokens;

/**
 * LoginUserUseCaseのユニットテスト
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LoginUserUseCase ユニットテスト")
class LoginUserUseCaseTest {

    @Mock
    private AuthService authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ImageStorageService imageStorageService;

    private LoginUserUseCase loginUserUseCase;

    @BeforeEach
    void setUp() {
        loginUserUseCase = new LoginUserUseCase(authService, userRepository, imageStorageService);
    }

    @Test
    @DisplayName("有効な認証情報でログインが成功する")
    void testExecute_WithValidCredentials_Success() {
        // Arrange
        String email = "test@example.com";
        String password = "Password123!";

        AuthTokens tokens = new AuthTokens(
                "access-token",
                "refresh-token",
                "id-token",
                3600);

        User existingUser = new User("user-id-123", email, "testuser", Language.JA);

        when(authService.signIn(email, password)).thenReturn(tokens);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        LoginUserUseCase.LoginResult result = loginUserUseCase.execute(email, password);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getTokens()).isEqualTo(tokens);
        assertThat(result.getUser()).isNotNull();
        assertThat(result.getUser().getEmail()).isEqualTo(email);
        assertThat(result.getUser().getLastLoginDate()).isNotNull();

        verify(authService).signIn(email, password);
        verify(userRepository).findByEmail(email);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("無効な認証情報でログインが失敗する")
    void testExecute_WithInvalidCredentials_ThrowsAuthenticationException() {
        // Arrange
        String email = "test@example.com";
        String password = "wrongpassword";

        when(authService.signIn(email, password))
                .thenThrow(new AuthenticationException("Invalid credentials"));

        // Act & Assert
        assertThatThrownBy(() -> loginUserUseCase.execute(email, password))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Invalid credentials");

        verify(authService).signIn(email, password);
        verify(userRepository, never()).findByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("最終ログイン日時が更新される")
    void testExecute_UpdatesLastLoginDate() {
        // Arrange
        String email = "test@example.com";
        String password = "Password123!";

        AuthTokens tokens = new AuthTokens("access-token", "refresh-token", "id-token", 3600);
        User existingUser = new User("user-id-123", email, "testuser", Language.JA);
        LocalDateTime beforeLogin = LocalDateTime.now();

        when(authService.signIn(email, password)).thenReturn(tokens);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        LoginUserUseCase.LoginResult result = loginUserUseCase.execute(email, password);

        // Assert
        assertThat(result.getUser().getLastLoginDate()).isNotNull();
        assertThat(result.getUser().getLastLoginDate()).isAfterOrEqualTo(beforeLogin);

        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("DBにユーザーが存在しない場合、Cognitoから作成される")
    void testExecute_CreatesUserFromCognito_WhenNotInDatabase() {
        // Arrange
        String email = "newuser@example.com";
        String password = "Password123!";
        String cognitoSub = "cognito-sub-12345";

        AuthTokens tokens = new AuthTokens("access-token", "refresh-token", "id-token", 3600);

        Map<String, String> attributes = new HashMap<>();
        attributes.put("sub", cognitoSub);
        attributes.put("nickname", "newuser");
        attributes.put("locale", "ja");

        when(authService.signIn(email, password)).thenReturn(tokens);
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(authService.getUserAttributes("access-token")).thenReturn(attributes);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        LoginUserUseCase.LoginResult result = loginUserUseCase.execute(email, password);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUser()).isNotNull();
        assertThat(result.getUser().getUserId()).isEqualTo(cognitoSub);
        assertThat(result.getUser().getEmail()).isEqualTo(email);
        assertThat(result.getUser().getNickname()).isEqualTo("newuser");

        verify(authService).getUserAttributes("access-token");
        verify(userRepository, times(2)).save(any(User.class)); // 作成時と最終ログイン更新時
    }

    @Test
    @DisplayName("Cognitoからのユーザー作成に失敗した場合、AuthenticationExceptionがスローされる")
    void testExecute_ThrowsAuthenticationException_WhenUserCreationFails() {
        // Arrange
        String email = "newuser@example.com";
        String password = "Password123!";

        AuthTokens tokens = new AuthTokens("access-token", "refresh-token", "id-token", 3600);

        when(authService.signIn(email, password)).thenReturn(tokens);
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(authService.getUserAttributes("access-token"))
                .thenThrow(new RuntimeException("Cognito error"));

        // Act & Assert
        assertThatThrownBy(() -> loginUserUseCase.execute(email, password))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Failed to create user");

        verify(authService).getUserAttributes("access-token");
    }
}
