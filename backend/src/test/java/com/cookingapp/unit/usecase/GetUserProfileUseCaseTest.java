package com.cookingapp.unit.usecase;

import com.cookingapp.application.usecase.user.GetUserProfileUseCase;
import com.cookingapp.domain.entity.User;
import com.cookingapp.domain.repository.UserRepository;
import com.cookingapp.domain.valueobject.Language;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * GetUserProfileUseCaseのユニットテスト
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GetUserProfileUseCase ユニットテスト")
class GetUserProfileUseCaseTest {

    @Mock
    private UserRepository userRepository;

    private GetUserProfileUseCase getUserProfileUseCase;

    @BeforeEach
    void setUp() {
        getUserProfileUseCase = new GetUserProfileUseCase(userRepository);
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
    @DisplayName("プロフィール画像URLが含まれる")
    void testExecute_ReturnsUserWithProfileImage() {
        // Arrange
        String userId = "user-123";
        String email = "test@example.com";
        String profileImageUrl = "https://example.com/image.jpg";
        
        User existingUser = new User(email, "testuser", Language.JA);
        existingUser.updateProfileImageUrl(profileImageUrl);
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

        // Act
        User result = getUserProfileUseCase.execute(userId);

        // Assert
        assertThat(result.getProfileImageUrl()).isEqualTo(profileImageUrl);
    }
}
