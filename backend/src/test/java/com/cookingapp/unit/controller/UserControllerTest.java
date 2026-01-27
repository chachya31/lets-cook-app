package com.cookingapp.unit.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.cookingapp.application.usecase.user.ConfirmSignUpUseCase;
import com.cookingapp.application.usecase.user.DeleteUserAccountUseCase;
import com.cookingapp.application.usecase.user.GetUserProfileUseCase;
import com.cookingapp.application.usecase.user.LoginUserUseCase;
import com.cookingapp.application.usecase.user.RegisterUserUseCase;
import com.cookingapp.application.usecase.user.ResendConfirmationCodeUseCase;
import com.cookingapp.application.usecase.user.UpdateUserProfileUseCase;
import com.cookingapp.application.usecase.user.UploadProfileImageUseCase;
import com.cookingapp.domain.entity.User;
import com.cookingapp.domain.exception.AuthenticationException;
import com.cookingapp.domain.exception.UserAlreadyExistsException;
import com.cookingapp.domain.exception.UserNotFoundException;
import com.cookingapp.domain.valueobject.Language;
import com.cookingapp.infrastructure.external.cognito.dto.AuthTokens;
import com.cookingapp.presentation.controller.UserController;
import com.cookingapp.presentation.dto.ConfirmSignUpRequest;
import com.cookingapp.presentation.dto.LoginRequest;
import com.cookingapp.presentation.dto.RegisterUserRequest;
import com.cookingapp.presentation.dto.ResendConfirmationCodeRequest;
import com.cookingapp.presentation.dto.UpdateProfileRequest;
import com.cookingapp.presentation.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * UserControllerのユニットテスト
 * MockMvcBuilders.standaloneSetupを使用してAWS SDKモック問題を回避
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserController ユニットテスト")
class UserControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private RegisterUserUseCase registerUserUseCase;

    @Mock
    private LoginUserUseCase loginUserUseCase;

    @Mock
    private GetUserProfileUseCase getUserProfileUseCase;

    @Mock
    private UpdateUserProfileUseCase updateUserProfileUseCase;

    @Mock
    private DeleteUserAccountUseCase deleteUserAccountUseCase;

    @Mock
    private ConfirmSignUpUseCase confirmSignUpUseCase;

    @Mock
    private ResendConfirmationCodeUseCase resendConfirmationCodeUseCase;

    @Mock
    private UploadProfileImageUseCase uploadProfileImageUseCase;

    @Mock
    private MessageSource messageSource;

    private static final String USERS_ENDPOINT = "/api/users";
    private static final String TEST_USER_ID = "user-123";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "Password123!";
    private static final String TEST_NICKNAME = "testuser";

    @BeforeEach
    void setUp() {
        UserController userController = new UserController(
                registerUserUseCase,
                loginUserUseCase,
                getUserProfileUseCase,
                updateUserProfileUseCase,
                deleteUserAccountUseCase,
                uploadProfileImageUseCase,
                confirmSignUpUseCase,
                resendConfirmationCodeUseCase);

        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler(messageSource))
                .build();
        objectMapper = new ObjectMapper();
    }

    @Nested
    @DisplayName("POST /api/users/register - ユーザー登録")
    class Register {

        @Nested
        @DisplayName("正常系")
        class Success {

            @Test
            @DisplayName("有効なリクエストでユーザー登録が成功する")
            void shouldReturn201WhenRegistrationSuccessful() throws Exception {
                // Arrange
                RegisterUserRequest request = new RegisterUserRequest(
                        TEST_EMAIL, TEST_PASSWORD, TEST_NICKNAME, "ja");
                User user = createTestUser();

                when(registerUserUseCase.execute(
                        eq(TEST_EMAIL), eq(TEST_PASSWORD), eq(TEST_NICKNAME), eq(Language.JA)))
                        .thenReturn(user);

                // Act & Assert
                mockMvc.perform(post(USERS_ENDPOINT + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.email").value(TEST_EMAIL))
                        .andExpect(jsonPath("$.nickname").value(TEST_NICKNAME));

                verify(registerUserUseCase).execute(
                        eq(TEST_EMAIL), eq(TEST_PASSWORD), eq(TEST_NICKNAME), eq(Language.JA));
            }
        }

        @Nested
        @DisplayName("異常系")
        class Failure {

            @Test
            @DisplayName("既存ユーザーの場合、409 Conflictを返す")
            void shouldReturn409WhenUserAlreadyExists() throws Exception {
                // Arrange
                RegisterUserRequest request = new RegisterUserRequest(
                        TEST_EMAIL, TEST_PASSWORD, TEST_NICKNAME, "ja");

                when(registerUserUseCase.execute(anyString(), anyString(), anyString(), any()))
                        .thenThrow(new UserAlreadyExistsException("User already exists"));

                // Act & Assert
                mockMvc.perform(post(USERS_ENDPOINT + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isConflict())
                        .andExpect(jsonPath("$.code").value("USER_ALREADY_EXISTS"));
            }

            @Test
            @DisplayName("メールアドレスが空の場合、400 Bad Requestを返す")
            void shouldReturn400WhenEmailIsEmpty() throws Exception {
                // Arrange
                RegisterUserRequest request = new RegisterUserRequest(
                        "", TEST_PASSWORD, TEST_NICKNAME, "ja");

                // Act & Assert
                mockMvc.perform(post(USERS_ENDPOINT + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest());

                verify(registerUserUseCase, never()).execute(anyString(), anyString(), anyString(),
                        any());
            }
        }
    }

    @Nested
    @DisplayName("POST /api/users/login - ログイン")
    class Login {

        @Nested
        @DisplayName("正常系")
        class Success {

            @Test
            @DisplayName("有効な認証情報でログインが成功する")
            void shouldReturn200WhenLoginSuccessful() throws Exception {
                // Arrange
                LoginRequest request = new LoginRequest(TEST_EMAIL, TEST_PASSWORD);
                User user = createTestUser();
                AuthTokens tokens = new AuthTokens("access-token", "refresh-token", "id-token", 3600);
                LoginUserUseCase.LoginResult loginResult = new LoginUserUseCase.LoginResult(tokens,
                        user);

                when(loginUserUseCase.execute(TEST_EMAIL, TEST_PASSWORD)).thenReturn(loginResult);

                // Act & Assert
                mockMvc.perform(post(USERS_ENDPOINT + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.accessToken").value("access-token"))
                        .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
                        .andExpect(jsonPath("$.idToken").value("id-token"))
                        .andExpect(jsonPath("$.user.email").value(TEST_EMAIL));

                verify(loginUserUseCase).execute(TEST_EMAIL, TEST_PASSWORD);
            }
        }

        @Nested
        @DisplayName("異常系")
        class Failure {

            @Test
            @DisplayName("認証失敗の場合、401 Unauthorizedを返す")
            void shouldReturn401WhenAuthenticationFails() throws Exception {
                // Arrange
                LoginRequest request = new LoginRequest(TEST_EMAIL, "wrongpassword");

                when(loginUserUseCase.execute(anyString(), anyString()))
                        .thenThrow(new AuthenticationException("Invalid credentials"));

                // Act & Assert
                mockMvc.perform(post(USERS_ENDPOINT + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isUnauthorized())
                        .andExpect(jsonPath("$.code").value("AUTHENTICATION_ERROR"));
            }
        }
    }

    @Nested
    @DisplayName("POST /api/users/confirm - メール確認")
    class ConfirmSignUp {

        @Nested
        @DisplayName("正常系")
        class Success {

            @Test
            @DisplayName("有効な確認コードで確認が成功する")
            void shouldReturn200WhenConfirmationSuccessful() throws Exception {
                // Arrange
                ConfirmSignUpRequest request = new ConfirmSignUpRequest(TEST_EMAIL, "123456");
                doNothing().when(confirmSignUpUseCase).execute(TEST_EMAIL, "123456");

                // Act & Assert
                mockMvc.perform(post(USERS_ENDPOINT + "/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk());

                verify(confirmSignUpUseCase).execute(TEST_EMAIL, "123456");
            }
        }
    }

    @Nested
    @DisplayName("POST /api/users/resend-code - 確認コード再送信")
    class ResendCode {

        @Nested
        @DisplayName("正常系")
        class Success {

            @Test
            @DisplayName("確認コード再送信が成功する")
            void shouldReturn200WhenResendSuccessful() throws Exception {
                // Arrange
                ResendConfirmationCodeRequest request = new ResendConfirmationCodeRequest(TEST_EMAIL);
                doNothing().when(resendConfirmationCodeUseCase).execute(TEST_EMAIL);

                // Act & Assert
                mockMvc.perform(post(USERS_ENDPOINT + "/resend-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk());

                verify(resendConfirmationCodeUseCase).execute(TEST_EMAIL);
            }
        }
    }

    @Nested
    @DisplayName("GET /api/users/profile/{userId} - プロフィール取得")
    class GetProfile {

        @Nested
        @DisplayName("正常系")
        class Success {

            @Test
            @DisplayName("ユーザーが存在する場合、プロフィールを返す")
            void shouldReturn200WithProfileWhenUserExists() throws Exception {
                // Arrange
                User user = createTestUser();
                when(getUserProfileUseCase.execute(TEST_USER_ID)).thenReturn(user);

                // Act & Assert
                mockMvc.perform(get(USERS_ENDPOINT + "/profile/" + TEST_USER_ID))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.email").value(TEST_EMAIL))
                        .andExpect(jsonPath("$.nickname").value(TEST_NICKNAME));

                verify(getUserProfileUseCase).execute(TEST_USER_ID);
            }
        }

        @Nested
        @DisplayName("異常系")
        class Failure {

            @Test
            @DisplayName("ユーザーが存在しない場合、404 Not Foundを返す")
            void shouldReturn404WhenUserNotFound() throws Exception {
                // Arrange
                when(getUserProfileUseCase.execute(TEST_USER_ID))
                        .thenThrow(new UserNotFoundException("User not found"));

                // Act & Assert
                mockMvc.perform(get(USERS_ENDPOINT + "/profile/" + TEST_USER_ID))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"));
            }
        }
    }

    @Nested
    @DisplayName("PUT /api/users/profile/{userId} - プロフィール更新")
    class UpdateProfile {

        @Nested
        @DisplayName("正常系")
        class Success {

            @Test
            @DisplayName("有効なリクエストでプロフィール更新が成功する")
            void shouldReturn200WhenUpdateSuccessful() throws Exception {
                // Arrange
                UpdateProfileRequest request = new UpdateProfileRequest(
                        "newnickname", "New Display Name", "ja", "Asia/Tokyo", false);

                User updatedUser = createTestUser();
                when(updateUserProfileUseCase.execute(
                        eq(TEST_USER_ID), anyString(), anyString(), any(), any(), anyBoolean()))
                        .thenReturn(updatedUser);

                // Act & Assert
                mockMvc.perform(put(USERS_ENDPOINT + "/profile/" + TEST_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.email").value(TEST_EMAIL));

                verify(updateUserProfileUseCase).execute(
                        eq(TEST_USER_ID), anyString(), anyString(), any(), any(), anyBoolean());
            }
        }

        @Nested
        @DisplayName("異常系")
        class Failure {

            @Test
            @DisplayName("ユーザーが存在しない場合、404 Not Foundを返す")
            void shouldReturn404WhenUserNotFound() throws Exception {
                // Arrange
                UpdateProfileRequest request = new UpdateProfileRequest(
                        "newnickname", null, null, null, null);

                when(updateUserProfileUseCase.execute(
                        eq(TEST_USER_ID), anyString(), any(), any(), any(), anyBoolean()))
                        .thenThrow(new UserNotFoundException("User not found"));

                // Act & Assert
                mockMvc.perform(put(USERS_ENDPOINT + "/profile/" + TEST_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"));
            }
        }
    }

    @Nested
    @DisplayName("DELETE /api/users/account/{userId} - アカウント削除")
    class DeleteAccount {

        @Nested
        @DisplayName("正常系")
        class Success {

            @Test
            @DisplayName("アカウント削除が成功する")
            void shouldReturn204WhenDeleteSuccessful() throws Exception {
                // Arrange
                doNothing().when(deleteUserAccountUseCase).execute(TEST_USER_ID);

                // Act & Assert
                mockMvc.perform(delete(USERS_ENDPOINT + "/account/" + TEST_USER_ID))
                        .andExpect(status().isNoContent());

                verify(deleteUserAccountUseCase).execute(TEST_USER_ID);
            }
        }

        @Nested
        @DisplayName("異常系")
        class Failure {

            @Test
            @DisplayName("ユーザーが存在しない場合、404 Not Foundを返す")
            void shouldReturn404WhenUserNotFound() throws Exception {
                // Arrange
                doThrow(new UserNotFoundException("User not found"))
                        .when(deleteUserAccountUseCase).execute(TEST_USER_ID);

                // Act & Assert
                mockMvc.perform(delete(USERS_ENDPOINT + "/account/" + TEST_USER_ID))
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"));
            }
        }
    }

    // ヘルパーメソッド

    private User createTestUser() {
        return new User(TEST_EMAIL, TEST_NICKNAME, Language.JA);
    }
}
