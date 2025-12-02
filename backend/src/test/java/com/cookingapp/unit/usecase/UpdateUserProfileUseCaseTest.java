package com.cookingapp.unit.usecase;

import com.cookingapp.application.usecase.user.UpdateUserProfileUseCase;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * UpdateUserProfileUseCaseのユニットテスト
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateUserProfileUseCase ユニットテスト")
class UpdateUserProfileUseCaseTest {

    @Mock
    private UserRepository userRepository;

    private UpdateUserProfileUseCase updateUserProfileUseCase;

    @BeforeEach
    void setUp() {
        updateUserProfileUseCase = new UpdateUserProfileUseCase(userRepository);
    }

    @Test
    @DisplayName("プロフィール情報が正しく更新される")
    void testExecute_UpdatesProfile_Success() {
        // Arrange
        String userId = "user-123";
        String nickname = "newnickname";
        String displayName = "New Display Name";
        Language language = Language.KO;
        String timezone = "Asia/Seoul";
        boolean marketingOptOut = true;

        User existingUser = new User("test@example.com", "oldnickname", Language.JA);
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User result = updateUserProfileUseCase.execute(
                userId, nickname, displayName, language, timezone, marketingOptOut
        );

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getNickname()).isEqualTo(nickname);
        assertThat(result.getDisplayName()).isEqualTo(displayName);
        assertThat(result.getPreferredLanguage()).isEqualTo(language);
        assertThat(result.getTimezone()).isEqualTo(timezone);
        assertThat(result.isMarketingOptOut()).isTrue();

        verify(userRepository).findById(userId);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("存在しないユーザーで更新が失敗する")
    void testExecute_WithNonExistentUser_ThrowsException() {
        // Arrange
        String userId = "non-existent-user";
        
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> updateUserProfileUseCase.execute(
                userId, "nickname", "displayName", Language.JA, "Asia/Tokyo", false
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");

        verify(userRepository).findById(userId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("プロフィール画像URLが正しく更新される")
    void testUpdateProfileImage_Success() {
        // Arrange
        String userId = "user-123";
        String profileImageUrl = "https://example.com/image.jpg";

        User existingUser = new User("test@example.com", "testuser", Language.JA);
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User result = updateUserProfileUseCase.updateProfileImage(userId, profileImageUrl);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getProfileImageUrl()).isEqualTo(profileImageUrl);

        verify(userRepository).findById(userId);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("存在しないユーザーでプロフィール画像更新が失敗する")
    void testUpdateProfileImage_WithNonExistentUser_ThrowsException() {
        // Arrange
        String userId = "non-existent-user";
        String profileImageUrl = "https://example.com/image.jpg";
        
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> updateUserProfileUseCase.updateProfileImage(userId, profileImageUrl))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");

        verify(userRepository).findById(userId);
        verify(userRepository, never()).save(any(User.class));
    }
}
