package com.cookingapp.unit.usecase.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cookingapp.application.usecase.admin.GetAllUsersUseCase;
import com.cookingapp.domain.entity.User;
import com.cookingapp.domain.repository.UserRepository;
import com.cookingapp.domain.service.ImageStorageService;
import com.cookingapp.domain.valueobject.Language;
import com.cookingapp.infrastructure.external.cognito.CognitoAuthService;
import com.cookingapp.presentation.dto.UserResponse;

/**
 * GetAllUsersUseCaseのユニットテスト
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GetAllUsersUseCase ユニットテスト")
class GetAllUsersUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CognitoAuthService cognitoAuthService;

    @Mock
    private ImageStorageService imageStorageService;

    private GetAllUsersUseCase getAllUsersUseCase;

    private static final String TEST_USER_ID_1 = "user-123";
    private static final String TEST_USER_ID_2 = "user-456";
    private static final String TEST_EMAIL_1 = "test1@example.com";
    private static final String TEST_EMAIL_2 = "test2@example.com";
    private static final String TEST_IMAGE_URL = "images/profile/user-123.jpg";
    private static final String TEST_PRESIGNED_URL = "https://bucket.s3.amazonaws.com/images/profile/user-123.jpg?signature=xxx";

    @BeforeEach
    void setUp() {
        getAllUsersUseCase = new GetAllUsersUseCase(userRepository, cognitoAuthService, imageStorageService);
    }

    @Test
    @DisplayName("全ユーザーを正常に取得する")
    void testExecute_ReturnsAllUsers() {
        // Arrange
        User user1 = new User(TEST_USER_ID_1, TEST_EMAIL_1, "user1", Language.JA);
        User user2 = new User(TEST_USER_ID_2, TEST_EMAIL_2, "user2", Language.KO);
        List<User> users = List.of(user1, user2);

        when(userRepository.findAll()).thenReturn(users);
        when(cognitoAuthService.getUserGroups(TEST_EMAIL_1)).thenReturn(List.of("Users"));
        when(cognitoAuthService.getUserGroups(TEST_EMAIL_2)).thenReturn(List.of("Users", "ADMINS"));

        // Act
        List<UserResponse> result = getAllUsersUseCase.execute();

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getUserId()).isEqualTo(TEST_USER_ID_1);
        assertThat(result.get(0).getEmail()).isEqualTo(TEST_EMAIL_1);
        assertThat(result.get(0).getRoles()).containsExactly("Users");
        assertThat(result.get(1).getUserId()).isEqualTo(TEST_USER_ID_2);
        assertThat(result.get(1).getRoles()).containsExactly("Users", "ADMINS");

        verify(userRepository).findAll();
        verify(cognitoAuthService).getUserGroups(TEST_EMAIL_1);
        verify(cognitoAuthService).getUserGroups(TEST_EMAIL_2);
    }

    @Test
    @DisplayName("ユーザーが存在しない場合、空のリストを返す")
    void testExecute_ReturnsEmptyListWhenNoUsers() {
        // Arrange
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<UserResponse> result = getAllUsersUseCase.execute();

        // Assert
        assertThat(result).isEmpty();

        verify(userRepository).findAll();
        verify(cognitoAuthService, never()).getUserGroups(anyString());
    }

    @Test
    @DisplayName("プロフィール画像がある場合、Presigned URLに変換される")
    void testExecute_ConvertsProfileImageToPresignedUrl() {
        // Arrange
        User user = new User(TEST_USER_ID_1, TEST_EMAIL_1, "user1", Language.JA);
        user.updateProfileImageUrl(TEST_IMAGE_URL);

        when(userRepository.findAll()).thenReturn(List.of(user));
        when(imageStorageService.generatePresignedUrl(TEST_IMAGE_URL)).thenReturn(TEST_PRESIGNED_URL);
        when(cognitoAuthService.getUserGroups(TEST_EMAIL_1)).thenReturn(List.of("Users"));

        // Act
        List<UserResponse> result = getAllUsersUseCase.execute();

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProfileImageUrl()).isEqualTo(TEST_PRESIGNED_URL);

        verify(imageStorageService).generatePresignedUrl(TEST_IMAGE_URL);
    }

    @Test
    @DisplayName("プロフィール画像がない場合、Presigned URL生成はスキップされる")
    void testExecute_SkipsPresignedUrlGenerationWhenNoImage() {
        // Arrange
        User user = new User(TEST_USER_ID_1, TEST_EMAIL_1, "user1", Language.JA);

        when(userRepository.findAll()).thenReturn(List.of(user));
        when(cognitoAuthService.getUserGroups(TEST_EMAIL_1)).thenReturn(List.of("Users"));

        // Act
        List<UserResponse> result = getAllUsersUseCase.execute();

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProfileImageUrl()).isNull();

        verify(imageStorageService, never()).generatePresignedUrl(anyString());
    }

    @Test
    @DisplayName("グループ取得に失敗した場合、空のロールリストが設定される")
    void testExecute_SetsEmptyRolesWhenGroupFetchFails() {
        // Arrange
        User user = new User(TEST_USER_ID_1, TEST_EMAIL_1, "user1", Language.JA);

        when(userRepository.findAll()).thenReturn(List.of(user));
        when(cognitoAuthService.getUserGroups(TEST_EMAIL_1))
                .thenThrow(new RuntimeException("Cognito error"));

        // Act
        List<UserResponse> result = getAllUsersUseCase.execute();

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(TEST_USER_ID_1);
        assertThat(result.get(0).getRoles()).isEmpty();

        verify(cognitoAuthService).getUserGroups(TEST_EMAIL_1);
    }

    @Test
    @DisplayName("複数ユーザーで一部のグループ取得が失敗しても他は正常に処理される")
    void testExecute_HandlesPartialGroupFetchFailure() {
        // Arrange
        User user1 = new User(TEST_USER_ID_1, TEST_EMAIL_1, "user1", Language.JA);
        User user2 = new User(TEST_USER_ID_2, TEST_EMAIL_2, "user2", Language.KO);

        when(userRepository.findAll()).thenReturn(List.of(user1, user2));
        when(cognitoAuthService.getUserGroups(TEST_EMAIL_1))
                .thenThrow(new RuntimeException("Cognito error"));
        when(cognitoAuthService.getUserGroups(TEST_EMAIL_2))
                .thenReturn(List.of("Users", "ADMINS"));

        // Act
        List<UserResponse> result = getAllUsersUseCase.execute();

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getRoles()).isEmpty();
        assertThat(result.get(1).getRoles()).containsExactly("Users", "ADMINS");
    }
}
