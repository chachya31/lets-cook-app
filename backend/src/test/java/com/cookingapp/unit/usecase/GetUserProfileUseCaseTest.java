package com.cookingapp.unit.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cookingapp.application.usecase.user.GetUserProfileUseCase;
import com.cookingapp.domain.entity.User;
import com.cookingapp.domain.repository.UserRepository;
import com.cookingapp.domain.service.ImageStorageService;
import com.cookingapp.domain.valueobject.Language;

/**
 * GetUserProfileUseCaseのユニットテスト
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GetUserProfileUseCase ユニットテスト")
class GetUserProfileUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ImageStorageService imageStorageService;

    private GetUserProfileUseCase getUserProfileUseCase;

    @BeforeEach
    void setUp() {
        getUserProfileUseCase = new GetUserProfileUseCase(userRepository, imageStorageService);
    }

    @Test
    @DisplayName("ユーザー情報が正しく取得される")
    void testExecute_ReturnsUser_Success() {
        // Arrange
        String userId = "user-123";
        String email = "test@example.com";
        String nickname = "testuser";

        User existingUser = new User(email, nickname, Language.JA);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

        // Act
        User result = getUserProfileUseCase.execute(userId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(email);
        assertThat(result.getNickname()).isEqualTo(nickname);
        assertThat(result.getPreferredLanguage()).isEqualTo(Language.JA);

        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("存在しないユーザーで取得が失敗する")
    void testExecute_WithNonExistentUser_ThrowsException() {
        // Arrange
        String userId = "non-existent-user";

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> getUserProfileUseCase.execute(userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");

        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("プロフィール画像URLがPresigned URLに変換される")
    void testExecute_ReturnsUserWithPresignedProfileImage() {
        // Arrange
        String userId = "user-123";
        String email = "test@example.com";
        String imageKey = "images/uuid-123.jpg";
        String presignedUrl = "https://bucket.s3.amazonaws.com/images/uuid-123.jpg?signature=xxx";

        User existingUser = new User(email, "testuser", Language.JA);
        existingUser.updateProfileImageUrl(imageKey);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(imageStorageService.generatePresignedUrl(imageKey)).thenReturn(presignedUrl);

        // Act
        User result = getUserProfileUseCase.execute(userId);

        // Assert
        assertThat(result.getProfileImageUrl()).isEqualTo(presignedUrl);
        verify(imageStorageService).generatePresignedUrl(imageKey);
    }
}
