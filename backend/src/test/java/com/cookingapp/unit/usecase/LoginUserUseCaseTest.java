package com.cookingapp.unit.usecase;

import com.cookingapp.application.usecase.user.LoginUserUseCase;
import com.cookingapp.domain.entity.User;
import com.cookingapp.domain.exception.AuthenticationException;
import com.cookingapp.domain.repository.UserRepository;
import com.cookingapp.domain.valueobject.Language;
import com.cookingapp.infrastructure.external.cognito.CognitoAuthService;
import com.cookingapp.infrastructure.external.cognito.dto.AuthTokens;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * LoginUserUseCaseのユニットテスト
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LoginUserUseCase ユニットテスト")
class LoginUserUseCaseTest {

    @Mock
    private CognitoAuthService cognitoAuthService;

    @Mock
    private UserRepository userRepository;

    private LoginUserUseCase loginUserUseCase;

    @BeforeEach
    void setUp() {
        loginUserUseCase = new LoginUserUseCase(cognitoAuthService, userRepository);
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
                3600
        );
        
        User existingUser = new User(email, "testuser", Language.JA);
        
        when(cognitoAuthService.signIn(email, password)).thenReturn(tokens);
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

        verify(cognitoAuthService).signIn(email, password);
        verify(userRepository).findByEmail(email);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("無効な認証情報でログインが失敗する")
    void testExecute_WithInvalidCredentials_ThrowsAuthenticationException() {
        // Arrange
        String email = "test@example.com";
        String password = "wrongpassword";

        when(cognitoAuthService.signIn(email, password))
                .thenThrow(new AuthenticationException("Invalid credentials"));

        // Act & Assert
        assertThatThrownBy(() -> loginUserUseCase.execute(email, password))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Invalid credentials");

        verify(cognitoAuthService).signIn(email, password);
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
        User existingUser = new User(email, "testuser", Language.JA);
        LocalDateTime beforeLogin = LocalDateTime.now();
        
        when(cognitoAuthService.signIn(email, password)).thenReturn(tokens);
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
        
        AuthTokens tokens = new AuthTokens("access-token", "refresh-token", "id-token", 3600);
        
        Map<String, String> attributes = new HashMap<>();
        attributes.put("nickname", "newuser");
        attributes.put("locale", "ja");
        
        when(cognitoAuthService.signIn(email, password)).thenReturn(tokens);
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(cognitoAuthService.getUserAttributes("access-token")).thenReturn(attributes);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        LoginUserUseCase.LoginResult result = loginUserUseCase.execute(email, password);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUser()).isNotNull();
        assertThat(result.getUser().getEmail()).isEqualTo(email);
        assertThat(result.getUser().getNickname()).isEqualTo("newuser");
        
        verify(cognitoAuthService).getUserAttributes("access-token");
        verify(userRepository, times(2)).save(any(User.class)); // 作成時と最終ログイン更新時
    }

    @Test
    @DisplayName("Cognitoからのユーザー作成に失敗した場合、AuthenticationExceptionがスローされる")
    void testExecute_ThrowsAuthenticationException_WhenUserCreationFails() {
        // Arrange
        String email = "newuser@example.com";
        String password = "Password123!";
        
        AuthTokens tokens = new AuthTokens("access-token", "refresh-token", "id-token", 3600);
        
        when(cognitoAuthService.signIn(email, password)).thenReturn(tokens);
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(cognitoAuthService.getUserAttributes("access-token"))
                .thenThrow(new RuntimeException("Cognito error"));

        // Act & Assert
        assertThatThrownBy(() -> loginUserUseCase.execute(email, password))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Failed to create user");

        verify(cognitoAuthService).getUserAttributes("access-token");
    }
}
