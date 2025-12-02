package com.cookingapp.unit.usecase;

import com.cookingapp.application.usecase.user.DeleteUserAccountUseCase;
import com.cookingapp.domain.entity.User;
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * DeleteUserAccountUseCaseのユニットテスト
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DeleteUserAccountUseCase ユニットテスト")
class DeleteUserAccountUseCaseTest {

    @Mock
    private CognitoAuthService cognitoAuthService;

    @Mock
    private UserRepository userRepository;

    private DeleteUserAccountUseCase deleteUserAccountUseCase;

    @BeforeEach
    void setUp() {
        deleteUserAccountUseCase = new DeleteUserAccountUseCase(cognitoAuthService, userRepository);
    }

    @Test
    @DisplayName("Cognitoとリポジトリから削除される")
    void testExecute_DeletesFromCognitoAndRepository() {
        // Arrange
        String userId = "user-123";
        String email = "test@example.com";
        
        User existingUser = new User(email, "testuser", Language.JA);
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        doNothing().when(cognitoAuthService).deleteUser(email);
        doNothing().when(userRepository).delete(userId);

        // Act
        deleteUserAccountUseCase.execute(userId);

        // Assert
        verify(userRepository).findById(userId);
        verify(cognitoAuthService).deleteUser(email);
        verify(userRepository).delete(userId);
    }

    @Test
    @DisplayName("存在しないユーザーで削除が失敗する")
    void testExecute_WithNonExistentUser_ThrowsException() {
        // Arrange
        String userId = "non-existent-user";
        
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> deleteUserAccountUseCase.execute(userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");

        verify(userRepository).findById(userId);
        verify(cognitoAuthService, never()).deleteUser(anyString());
        verify(userRepository, never()).delete(anyString());
    }

    @Test
    @DisplayName("Cognito削除が先に実行される")
    void testExecute_DeletesCognitoFirst() {
        // Arrange
        String userId = "user-123";
        String email = "test@example.com";
        
        User existingUser = new User(email, "testuser", Language.JA);
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        doNothing().when(cognitoAuthService).deleteUser(email);
        doNothing().when(userRepository).delete(userId);

        // Act
        deleteUserAccountUseCase.execute(userId);

        // Assert
        // Cognitoの削除がリポジトリの削除より先に呼ばれることを確認
        var inOrder = inOrder(cognitoAuthService, userRepository);
        inOrder.verify(cognitoAuthService).deleteUser(email);
        inOrder.verify(userRepository).delete(userId);
    }
}
