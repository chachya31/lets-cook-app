package com.cookingapp.unit.usecase;

import com.cookingapp.application.usecase.user.RegisterUserUseCase;
import com.cookingapp.domain.entity.User;
import com.cookingapp.domain.exception.UserAlreadyExistsException;
import com.cookingapp.domain.repository.UserRepository;
import com.cookingapp.domain.valueobject.Language;
import com.cookingapp.infrastructure.external.cognito.CognitoAuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * RegisterUserUseCaseのユニットテスト
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RegisterUserUseCase ユニットテスト")
class RegisterUserUseCaseTest {

    @Mock
    private CognitoAuthService cognitoAuthService;

    @Mock
    private UserRepository userRepository;

    private RegisterUserUseCase registerUserUseCase;

    @BeforeEach
    void setUp() {
        registerUserUseCase = new RegisterUserUseCase(cognitoAuthService, userRepository);
    }

    @Test
    @DisplayName("有効な入力でユーザー登録が成功する")
    void testExecute_WithValidInput_Success() {
        // Arrange
        String email = "test@example.com";
        String password = "Password123!";
        String nickname = "testuser";
        Language language = Language.JA;

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(cognitoAuthService.signUp(email, password, nickname)).thenReturn("cognito-user-id");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User result = registerUserUseCase.execute(email, password, nickname, language);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(email);
        assertThat(result.getNickname()).isEqualTo(nickname);
        assertThat(result.getPreferredLanguage()).isEqualTo(language);
        assertThat(result.getUserId()).isNotNull();

        verify(userRepository).findByEmail(email);
        verify(cognitoAuthService).signUp(email, password, nickname);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("無効なパスワードで登録が失敗する")
    void testExecute_WithInvalidPassword_ThrowsException() {
        // Arrange
        String email = "test@example.com";
        String password = "weak"; // 8文字未満、大文字・数字・特殊文字なし
        String nickname = "testuser";
        Language language = Language.JA;

        // Act & Assert
        assertThatThrownBy(() -> registerUserUseCase.execute(email, password, nickname, language))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Password must be at least 8 characters");

        verify(userRepository, never()).findByEmail(anyString());
        verify(cognitoAuthService, never()).signUp(anyString(), anyString(), anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("重複メールアドレスで登録が失敗する")
    void testExecute_WithDuplicateEmail_ThrowsUserAlreadyExistsException() {
        // Arrange
        String email = "existing@example.com";
        String password = "Password123!";
        String nickname = "testuser";
        Language language = Language.JA;

        User existingUser = new User(email, "existinguser", Language.JA);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));

        // Act & Assert
        assertThatThrownBy(() -> registerUserUseCase.execute(email, password, nickname, language))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("User with email " + email + " already exists");

        verify(userRepository).findByEmail(email);
        verify(cognitoAuthService, never()).signUp(anyString(), anyString(), anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Cognitoへの登録が成功する")
    void testExecute_CognitoSignUpCalled() {
        // Arrange
        String email = "test@example.com";
        String password = "Password123!";
        String nickname = "testuser";
        Language language = Language.JA;

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(cognitoAuthService.signUp(email, password, nickname)).thenReturn("cognito-user-id");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        registerUserUseCase.execute(email, password, nickname, language);

        // Assert
        verify(cognitoAuthService).signUp(email, password, nickname);
    }

    @Test
    @DisplayName("DynamoDBへの保存が成功する")
    void testExecute_UserSavedToRepository() {
        // Arrange
        String email = "test@example.com";
        String password = "Password123!";
        String nickname = "testuser";
        Language language = Language.JA;

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(cognitoAuthService.signUp(email, password, nickname)).thenReturn("cognito-user-id");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        registerUserUseCase.execute(email, password, nickname, language);

        // Assert
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("韓国語の優先言語でユーザー登録が成功する")
    void testExecute_WithKoreanLanguage_Success() {
        // Arrange
        String email = "test@example.com";
        String password = "Password123!";
        String nickname = "testuser";
        Language language = Language.KO;

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(cognitoAuthService.signUp(email, password, nickname)).thenReturn("cognito-user-id");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User result = registerUserUseCase.execute(email, password, nickname, language);

        // Assert
        assertThat(result.getPreferredLanguage()).isEqualTo(Language.KO);
    }
}
