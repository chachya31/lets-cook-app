package com.cookingapp.unit.usecase.admin;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
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

import com.cookingapp.application.usecase.admin.DeleteUserByAdminUseCase;
import com.cookingapp.domain.entity.User;
import com.cookingapp.domain.exception.UserNotFoundException;
import com.cookingapp.domain.repository.UserRepository;
import com.cookingapp.domain.service.AuthService;
import com.cookingapp.domain.service.ImageStorageService;
import com.cookingapp.domain.valueobject.Language;

/**
 * DeleteUserByAdminUseCaseのユニットテスト
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DeleteUserByAdminUseCase ユニットテスト")
class DeleteUserByAdminUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthService authService;

    @Mock
    private ImageStorageService imageStorageService;

    private DeleteUserByAdminUseCase deleteUserByAdminUseCase;

    private static final String TEST_USER_ID = "user-123";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_IMAGE_URL = "images/profile/user-123.jpg";

    @BeforeEach
    void setUp() {
        deleteUserByAdminUseCase = new DeleteUserByAdminUseCase(
                userRepository, authService, imageStorageService);
    }

    @Test
    @DisplayName("ユーザー削除が成功する")
    void testExecute_DeletesUserSuccessfully() {
        // Arrange
        User existingUser = new User(TEST_USER_ID, TEST_EMAIL, "testuser", Language.JA);

        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(existingUser));
        doNothing().when(authService).deleteUser(TEST_EMAIL);
        doNothing().when(userRepository).delete(TEST_USER_ID);

        // Act
        deleteUserByAdminUseCase.execute(TEST_USER_ID);

        // Assert
        verify(userRepository).findById(TEST_USER_ID);
        verify(authService).deleteUser(TEST_EMAIL);
        verify(userRepository).delete(TEST_USER_ID);
    }

    @Test
    @DisplayName("プロフィール画像がある場合、画像も削除される")
    void testExecute_DeletesProfileImageWhenExists() {
        // Arrange
        User existingUser = new User(TEST_USER_ID, TEST_EMAIL, "testuser", Language.JA);
        existingUser.updateProfileImageUrl(TEST_IMAGE_URL);

        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(existingUser));
        doNothing().when(imageStorageService).deleteImage(TEST_IMAGE_URL);
        doNothing().when(authService).deleteUser(TEST_EMAIL);
        doNothing().when(userRepository).delete(TEST_USER_ID);

        // Act
        deleteUserByAdminUseCase.execute(TEST_USER_ID);

        // Assert
        verify(imageStorageService).deleteImage(TEST_IMAGE_URL);
        verify(authService).deleteUser(TEST_EMAIL);
        verify(userRepository).delete(TEST_USER_ID);
    }

    @Test
    @DisplayName("プロフィール画像がない場合、画像削除はスキップされる")
    void testExecute_SkipsImageDeletionWhenNoImage() {
        // Arrange
        User existingUser = new User(TEST_USER_ID, TEST_EMAIL, "testuser", Language.JA);

        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(existingUser));
        doNothing().when(authService).deleteUser(TEST_EMAIL);
        doNothing().when(userRepository).delete(TEST_USER_ID);

        // Act
        deleteUserByAdminUseCase.execute(TEST_USER_ID);

        // Assert
        verify(imageStorageService, never()).deleteImage(anyString());
        verify(authService).deleteUser(TEST_EMAIL);
        verify(userRepository).delete(TEST_USER_ID);
    }

    @Test
    @DisplayName("存在しないユーザーで削除が失敗する")
    void testExecute_WithNonExistentUser_ThrowsUserNotFoundException() {
        // Arrange
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> deleteUserByAdminUseCase.execute(TEST_USER_ID))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("ユーザーが見つかりません");

        verify(userRepository).findById(TEST_USER_ID);
        verify(imageStorageService, never()).deleteImage(anyString());
        verify(authService, never()).deleteUser(anyString());
        verify(userRepository, never()).delete(anyString());
    }

    @Test
    @DisplayName("画像削除に失敗しても処理は継続される")
    void testExecute_ContinuesWhenImageDeletionFails() {
        // Arrange
        User existingUser = new User(TEST_USER_ID, TEST_EMAIL, "testuser", Language.JA);
        existingUser.updateProfileImageUrl(TEST_IMAGE_URL);

        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(existingUser));
        doThrow(new RuntimeException("S3 error")).when(imageStorageService).deleteImage(TEST_IMAGE_URL);
        doNothing().when(authService).deleteUser(TEST_EMAIL);
        doNothing().when(userRepository).delete(TEST_USER_ID);

        // Act
        deleteUserByAdminUseCase.execute(TEST_USER_ID);

        // Assert
        verify(imageStorageService).deleteImage(TEST_IMAGE_URL);
        verify(authService).deleteUser(TEST_EMAIL);
        verify(userRepository).delete(TEST_USER_ID);
    }

    @Test
    @DisplayName("Cognito削除に失敗しても処理は継続される")
    void testExecute_ContinuesWhenCognitoDeletionFails() {
        // Arrange
        User existingUser = new User(TEST_USER_ID, TEST_EMAIL, "testuser", Language.JA);

        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(existingUser));
        doThrow(new RuntimeException("Cognito error")).when(authService).deleteUser(TEST_EMAIL);
        doNothing().when(userRepository).delete(TEST_USER_ID);

        // Act
        deleteUserByAdminUseCase.execute(TEST_USER_ID);

        // Assert
        verify(authService).deleteUser(TEST_EMAIL);
        verify(userRepository).delete(TEST_USER_ID);
    }

    @Test
    @DisplayName("削除順序が正しい（画像→Cognito→DB）")
    void testExecute_DeletesInCorrectOrder() {
        // Arrange
        User existingUser = new User(TEST_USER_ID, TEST_EMAIL, "testuser", Language.JA);
        existingUser.updateProfileImageUrl(TEST_IMAGE_URL);

        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(existingUser));
        doNothing().when(imageStorageService).deleteImage(TEST_IMAGE_URL);
        doNothing().when(authService).deleteUser(TEST_EMAIL);
        doNothing().when(userRepository).delete(TEST_USER_ID);

        // Act
        deleteUserByAdminUseCase.execute(TEST_USER_ID);

        // Assert
        var inOrder = inOrder(imageStorageService, authService, userRepository);
        inOrder.verify(imageStorageService).deleteImage(TEST_IMAGE_URL);
        inOrder.verify(authService).deleteUser(TEST_EMAIL);
        inOrder.verify(userRepository).delete(TEST_USER_ID);
    }
}
