package com.cookingapp.unit.usecase.admin;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cookingapp.application.usecase.admin.SuspendUserUseCase;
import com.cookingapp.domain.entity.User;
import com.cookingapp.domain.exception.UserNotFoundException;
import com.cookingapp.domain.repository.UserRepository;
import com.cookingapp.domain.valueobject.Language;
import com.cookingapp.infrastructure.external.cognito.CognitoAuthService;

/**
 * SuspendUserUseCaseのユニットテスト
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SuspendUserUseCase ユニットテスト")
class SuspendUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CognitoAuthService cognitoAuthService;

    private SuspendUserUseCase suspendUserUseCase;

    private static final String TEST_USER_ID = "user-123";
    private static final String TEST_EMAIL = "test@example.com";

    @BeforeEach
    void setUp() {
        suspendUserUseCase = new SuspendUserUseCase(userRepository, cognitoAuthService);
    }

    @Test
    @DisplayName("ユーザー停止が成功する")
    void testExecute_SuspendsUserSuccessfully() {
        // Arrange
        User existingUser = new User(TEST_USER_ID, TEST_EMAIL, "testuser", Language.JA);

        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(existingUser));
        doNothing().when(cognitoAuthService).disableUser(TEST_EMAIL);

        // Act
        suspendUserUseCase.execute(TEST_USER_ID);

        // Assert
        verify(userRepository).findById(TEST_USER_ID);
        verify(cognitoAuthService).disableUser(TEST_EMAIL);
    }

    @Test
    @DisplayName("存在しないユーザーで停止が失敗する")
    void testExecute_WithNonExistentUser_ThrowsUserNotFoundException() {
        // Arrange
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> suspendUserUseCase.execute(TEST_USER_ID))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("ユーザーが見つかりません");

        verify(userRepository).findById(TEST_USER_ID);
        verify(cognitoAuthService, never()).disableUser(TEST_EMAIL);
    }
}
