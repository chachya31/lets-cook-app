package com.cookingapp.unit.usecase.alert;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cookingapp.application.usecase.alert.AlertResponse;
import com.cookingapp.application.usecase.alert.CheckAlertUseCase;
import com.cookingapp.domain.constants.ValidationConstants;
import com.cookingapp.domain.entity.User;
import com.cookingapp.domain.repository.UserRepository;
import com.cookingapp.domain.valueobject.Language;

/**
 * CheckAlertUseCaseのユニットテスト
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CheckAlertUseCase ユニットテスト")
class CheckAlertUseCaseTest {

    @Mock
    private UserRepository userRepository;

    private CheckAlertUseCase checkAlertUseCase;

    private static final String TEST_USER_ID = "user-123";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_NICKNAME = "testuser";

    @BeforeEach
    void setUp() {
        checkAlertUseCase = new CheckAlertUseCase(userRepository);
    }

    @Nested
    @DisplayName("checkAlert - アラート判定")
    class CheckAlert {

        @Nested
        @DisplayName("正常系 - アラート表示")
        class ShouldShowAlert {

            @Test
            @DisplayName("最終料理日がnullの場合、アラートを表示する")
            void shouldShowAlertWhenLastCookingDateIsNull() {
                // Arrange
                User user = createTestUserWithLastCookingDate(null);
                when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(user));

                // Act
                AlertResponse response = checkAlertUseCase.checkAlert(TEST_USER_ID);

                // Assert
                assertThat(response.isShouldShow()).isTrue();
                assertThat(response.getMessage()).isNotNull();
                assertThat(response.getMessage()).isNotEmpty();
                verify(userRepository).findById(TEST_USER_ID);
            }

            @Test
            @DisplayName("最終料理日から3日経過した場合、アラートを表示する")
            void shouldShowAlertWhenThreeDaysPassed() {
                // Arrange
                User user = createTestUserWithLastCookingDate(
                        LocalDate.now().minusDays(ValidationConstants.ALERT_DAYS_THRESHOLD));
                when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(user));

                // Act
                AlertResponse response = checkAlertUseCase.checkAlert(TEST_USER_ID);

                // Assert
                assertThat(response.isShouldShow()).isTrue();
                assertThat(response.getMessage()).isNotNull();
                verify(userRepository).findById(TEST_USER_ID);
            }

            @Test
            @DisplayName("最終料理日から5日経過した場合、アラートを表示する")
            void shouldShowAlertWhenFiveDaysPassed() {
                // Arrange
                User user = createTestUserWithLastCookingDate(LocalDate.now().minusDays(5));
                when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(user));

                // Act
                AlertResponse response = checkAlertUseCase.checkAlert(TEST_USER_ID);

                // Assert
                assertThat(response.isShouldShow()).isTrue();
                assertThat(response.getMessage()).isNotNull();
                verify(userRepository).findById(TEST_USER_ID);
            }
        }

        @Nested
        @DisplayName("正常系 - アラート非表示")
        class ShouldNotShowAlert {

            @Test
            @DisplayName("最終料理日が今日の場合、アラートを表示しない")
            void shouldNotShowAlertWhenCookedToday() {
                // Arrange
                User user = createTestUserWithLastCookingDate(LocalDate.now());
                when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(user));

                // Act
                AlertResponse response = checkAlertUseCase.checkAlert(TEST_USER_ID);

                // Assert
                assertThat(response.isShouldShow()).isFalse();
                assertThat(response.getMessage()).isNull();
                verify(userRepository).findById(TEST_USER_ID);
            }

            @Test
            @DisplayName("最終料理日が昨日の場合、アラートを表示しない")
            void shouldNotShowAlertWhenCookedYesterday() {
                // Arrange
                User user = createTestUserWithLastCookingDate(LocalDate.now().minusDays(1));
                when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(user));

                // Act
                AlertResponse response = checkAlertUseCase.checkAlert(TEST_USER_ID);

                // Assert
                assertThat(response.isShouldShow()).isFalse();
                assertThat(response.getMessage()).isNull();
                verify(userRepository).findById(TEST_USER_ID);
            }

            @Test
            @DisplayName("最終料理日が2日前の場合、アラートを表示しない")
            void shouldNotShowAlertWhenCookedTwoDaysAgo() {
                // Arrange
                User user = createTestUserWithLastCookingDate(LocalDate.now().minusDays(2));
                when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(user));

                // Act
                AlertResponse response = checkAlertUseCase.checkAlert(TEST_USER_ID);

                // Assert
                assertThat(response.isShouldShow()).isFalse();
                assertThat(response.getMessage()).isNull();
                verify(userRepository).findById(TEST_USER_ID);
            }
        }

        @Nested
        @DisplayName("異常系")
        class Failure {

            @Test
            @DisplayName("ユーザーが存在しない場合、IllegalArgumentExceptionをスローする")
            void shouldThrowExceptionWhenUserNotFound() {
                // Arrange
                when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.empty());

                // Act & Assert
                assertThatThrownBy(() -> checkAlertUseCase.checkAlert(TEST_USER_ID))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessageContaining("User not found: " + TEST_USER_ID);

                verify(userRepository).findById(TEST_USER_ID);
            }
        }
    }

    @Nested
    @DisplayName("メッセージ選択")
    class MessageSelection {

        @Test
        @DisplayName("アラート表示時にメッセージが返される")
        void shouldReturnMessageWhenAlertShown() {
            // Arrange
            User user = createTestUserWithLastCookingDate(null);
            when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(user));

            // Act - 複数回実行してメッセージが返されることを確認
            for (int i = 0; i < 10; i++) {
                AlertResponse response = checkAlertUseCase.checkAlert(TEST_USER_ID);
                assertThat(response.getMessage()).isNotNull();
                assertThat(response.getMessage()).isNotEmpty();
            }
        }
    }

    // ヘルパーメソッド

    private User createTestUserWithLastCookingDate(LocalDate lastCookingDate) {
        return new User(
                TEST_USER_ID,
                TEST_EMAIL,
                TEST_NICKNAME,
                TEST_NICKNAME,
                null,
                Language.JA,
                lastCookingDate,
                null,
                LocalDateTime.now(),
                "Asia/Tokyo",
                false);
    }
}
