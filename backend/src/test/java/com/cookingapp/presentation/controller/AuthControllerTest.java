package com.cookingapp.presentation.controller;

import com.cookingapp.application.usecase.LoginUseCase;
import com.cookingapp.config.TestSecurityConfig;
import com.cookingapp.infrastructure.auth.AuthenticationException;
import com.cookingapp.presentation.dto.LoginResponse;
import com.cookingapp.presentation.dto.UserProfileResponse;
import com.cookingapp.presentation.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({GlobalExceptionHandler.class, TestSecurityConfig.class})
@DisplayName("AuthController")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LoginUseCase loginUseCase;

    private static final String LOGIN_ENDPOINT = "/api/auth/login";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_ACCESS_TOKEN = "access-token-xyz";
    private static final String TEST_ID_TOKEN = "id-token-xyz";
    private static final String TEST_REFRESH_TOKEN = "refresh-token-xyz";
    private static final Integer TEST_EXPIRES_IN = 3600;

    @Nested
    @DisplayName("POST /api/auth/login")
    class Login {

        @Nested
        @DisplayName("正常系")
        class Success {

            @Test
            @DisplayName("有効な認証情報でログイン成功時、200 OKとトークン情報を返す")
            void shouldReturn200WithTokensWhenValidCredentials() throws Exception {
                // Arrange
                LoginResponse loginResponse = createLoginResponseWithUser();
                when(loginUseCase.execute(TEST_EMAIL, TEST_PASSWORD)).thenReturn(loginResponse);

                Map<String, String> request = new HashMap<>();
                request.put("email", TEST_EMAIL);
                request.put("password", TEST_PASSWORD);

                // Act & Assert
                mockMvc.perform(post(LOGIN_ENDPOINT)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.accessToken").value(TEST_ACCESS_TOKEN))
                        .andExpect(jsonPath("$.idToken").value(TEST_ID_TOKEN))
                        .andExpect(jsonPath("$.refreshToken").value(TEST_REFRESH_TOKEN))
                        .andExpect(jsonPath("$.expiresIn").value(TEST_EXPIRES_IN))
                        .andExpect(jsonPath("$.user.userId").value("user-123"))
                        .andExpect(jsonPath("$.user.email").value(TEST_EMAIL));

                verify(loginUseCase).execute(TEST_EMAIL, TEST_PASSWORD);
            }

            @Test
            @DisplayName("ユーザー情報なしでもログイン成功時、200 OKを返す")
            void shouldReturn200WithoutUserWhenUserNotInDatabase() throws Exception {
                // Arrange
                LoginResponse loginResponse = createLoginResponseWithoutUser();
                when(loginUseCase.execute(TEST_EMAIL, TEST_PASSWORD)).thenReturn(loginResponse);

                Map<String, String> request = new HashMap<>();
                request.put("email", TEST_EMAIL);
                request.put("password", TEST_PASSWORD);

                // Act & Assert
                mockMvc.perform(post(LOGIN_ENDPOINT)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.accessToken").value(TEST_ACCESS_TOKEN))
                        .andExpect(jsonPath("$.user").doesNotExist());

                verify(loginUseCase).execute(TEST_EMAIL, TEST_PASSWORD);
            }
        }

        @Nested
        @DisplayName("異常系 - 認証エラー")
        class AuthenticationFailure {

            @Test
            @DisplayName("認証失敗時、401 Unauthorizedを返す")
            void shouldReturn401WhenAuthenticationFails() throws Exception {
                // Arrange
                when(loginUseCase.execute(TEST_EMAIL, TEST_PASSWORD))
                        .thenThrow(new AuthenticationException("Invalid email or password"));

                Map<String, String> request = new HashMap<>();
                request.put("email", TEST_EMAIL);
                request.put("password", TEST_PASSWORD);

                // Act & Assert
                mockMvc.perform(post(LOGIN_ENDPOINT)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isUnauthorized())
                        .andExpect(jsonPath("$.message").value("Invalid email or password"))
                        .andExpect(jsonPath("$.code").value("AUTHENTICATION_FAILED"));

                verify(loginUseCase).execute(TEST_EMAIL, TEST_PASSWORD);
            }
        }

        @Nested
        @DisplayName("異常系 - バリデーションエラー")
        class ValidationFailure {

            @Test
            @DisplayName("メールアドレスが空の場合、400 Bad Requestを返す")
            void shouldReturn400WhenEmailIsEmpty() throws Exception {
                // Arrange
                Map<String, String> request = new HashMap<>();
                request.put("email", "");
                request.put("password", TEST_PASSWORD);

                // Act & Assert
                mockMvc.perform(post(LOGIN_ENDPOINT)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

                verify(loginUseCase, never()).execute(anyString(), anyString());
            }

            @Test
            @DisplayName("メールアドレスが未指定の場合、400 Bad Requestを返す")
            void shouldReturn400WhenEmailIsMissing() throws Exception {
                // Arrange
                Map<String, String> request = new HashMap<>();
                request.put("password", TEST_PASSWORD);

                // Act & Assert
                mockMvc.perform(post(LOGIN_ENDPOINT)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                        .andExpect(jsonPath("$.message").value("email: Email is required"));

                verify(loginUseCase, never()).execute(anyString(), anyString());
            }

            @Test
            @DisplayName("パスワードが空の場合、400 Bad Requestを返す")
            void shouldReturn400WhenPasswordIsEmpty() throws Exception {
                // Arrange
                Map<String, String> request = new HashMap<>();
                request.put("email", TEST_EMAIL);
                request.put("password", "");

                // Act & Assert
                mockMvc.perform(post(LOGIN_ENDPOINT)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

                verify(loginUseCase, never()).execute(anyString(), anyString());
            }

            @Test
            @DisplayName("パスワードが未指定の場合、400 Bad Requestを返す")
            void shouldReturn400WhenPasswordIsMissing() throws Exception {
                // Arrange
                Map<String, String> request = new HashMap<>();
                request.put("email", TEST_EMAIL);

                // Act & Assert
                mockMvc.perform(post(LOGIN_ENDPOINT)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                        .andExpect(jsonPath("$.message").value("password: Password is required"));

                verify(loginUseCase, never()).execute(anyString(), anyString());
            }

            @Test
            @DisplayName("無効なメールアドレス形式の場合、400 Bad Requestを返す")
            void shouldReturn400WhenEmailFormatIsInvalid() throws Exception {
                // Arrange
                Map<String, String> request = new HashMap<>();
                request.put("email", "invalid-email");
                request.put("password", TEST_PASSWORD);

                // Act & Assert
                mockMvc.perform(post(LOGIN_ENDPOINT)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                        .andExpect(jsonPath("$.message").value("email: Invalid email format"));

                verify(loginUseCase, never()).execute(anyString(), anyString());
            }

            @Test
            @DisplayName("リクエストボディが空の場合、400 Bad Requestを返す")
            void shouldReturn400WhenRequestBodyIsEmpty() throws Exception {
                // Act & Assert
                mockMvc.perform(post(LOGIN_ENDPOINT)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}"))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

                verify(loginUseCase, never()).execute(anyString(), anyString());
            }
        }
    }

    private LoginResponse createLoginResponseWithUser() {
        UserProfileResponse userProfile = UserProfileResponse.builder()
                .userId("user-123")
                .email(TEST_EMAIL)
                .nickname("testuser")
                .displayName("Test User")
                .profileImageUrl("https://example.com/image.jpg")
                .preferredLanguage("ja")
                .timezone("Asia/Tokyo")
                .build();

        return LoginResponse.builder()
                .accessToken(TEST_ACCESS_TOKEN)
                .idToken(TEST_ID_TOKEN)
                .refreshToken(TEST_REFRESH_TOKEN)
                .expiresIn(TEST_EXPIRES_IN)
                .user(userProfile)
                .build();
    }

    private LoginResponse createLoginResponseWithoutUser() {
        return LoginResponse.builder()
                .accessToken(TEST_ACCESS_TOKEN)
                .idToken(TEST_ID_TOKEN)
                .refreshToken(TEST_REFRESH_TOKEN)
                .expiresIn(TEST_EXPIRES_IN)
                .user(null)
                .build();
    }
}
