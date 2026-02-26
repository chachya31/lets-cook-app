package com.cookingapp.unit.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.cookingapp.application.usecase.alert.AlertResponse;
import com.cookingapp.application.usecase.alert.CheckAlertUseCase;
import com.cookingapp.domain.exception.UnauthorizedException;
import com.cookingapp.infrastructure.security.SecurityUtils;
import com.cookingapp.presentation.controller.AlertController;
import com.cookingapp.presentation.exception.GlobalExceptionHandler;

/**
 * AlertControllerのユニットテスト
 * MockMvcBuilders.standaloneSetupを使用してAWS SDKモック問題を回避
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AlertController ユニットテスト")
class AlertControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CheckAlertUseCase checkAlertUseCase;

    @Mock
    private MessageSource messageSource;

    private MockedStatic<SecurityUtils> securityUtilsMock;

    private static final String ALERTS_ENDPOINT = "/api/alerts";
    private static final String TEST_USER_ID = "user-123";

    @BeforeEach
    void setUp() {
        AlertController alertController = new AlertController(checkAlertUseCase);

        mockMvc = MockMvcBuilders.standaloneSetup(alertController)
                .setControllerAdvice(new GlobalExceptionHandler(messageSource))
                .build();

        securityUtilsMock = Mockito.mockStatic(SecurityUtils.class);
    }

    @AfterEach
    void tearDown() {
        securityUtilsMock.close();
    }

    @Nested
    @DisplayName("GET /api/alerts/check - アラート判定")
    class CheckAlert {

        @Nested
        @DisplayName("正常系")
        class Success {

            @Test
            @DisplayName("アラート表示が必要な場合、shouldShow=trueとメッセージを返す")
            void shouldReturn200WithAlertWhenShouldShow() throws Exception {
                // Arrange
                securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(TEST_USER_ID);

                AlertResponse response = new AlertResponse(true, "もう3日も料理していませんよ！");
                when(checkAlertUseCase.checkAlert(TEST_USER_ID)).thenReturn(response);

                // Act & Assert
                mockMvc.perform(get(ALERTS_ENDPOINT + "/check"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.shouldShow").value(true))
                        .andExpect(jsonPath("$.message").value("もう3日も料理していませんよ！"));

                verify(checkAlertUseCase).checkAlert(TEST_USER_ID);
            }

            @Test
            @DisplayName("アラート表示が不要な場合、shouldShow=falseを返す")
            void shouldReturn200WithNoAlertWhenShouldNotShow() throws Exception {
                // Arrange
                securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(TEST_USER_ID);

                AlertResponse response = new AlertResponse(false, null);
                when(checkAlertUseCase.checkAlert(TEST_USER_ID)).thenReturn(response);

                // Act & Assert
                mockMvc.perform(get(ALERTS_ENDPOINT + "/check"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.shouldShow").value(false))
                        .andExpect(jsonPath("$.message").isEmpty());

                verify(checkAlertUseCase).checkAlert(TEST_USER_ID);
            }
        }

        @Nested
        @DisplayName("異常系")
        class Failure {

            @Test
            @DisplayName("未認証の場合、403 Forbiddenを返す")
            void shouldReturn403WhenNotAuthenticated() throws Exception {
                // Arrange
                securityUtilsMock.when(SecurityUtils::getCurrentUserId)
                        .thenThrow(new UnauthorizedException("User is not authenticated"));

                // Act & Assert
                mockMvc.perform(get(ALERTS_ENDPOINT + "/check"))
                        .andExpect(status().isForbidden())
                        .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
            }

            @Test
            @DisplayName("ユーザーが存在しない場合、400 Bad Requestを返す")
            void shouldReturn400WhenUserNotFound() throws Exception {
                // Arrange
                securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(TEST_USER_ID);

                when(checkAlertUseCase.checkAlert(TEST_USER_ID))
                        .thenThrow(new IllegalArgumentException("User not found: " + TEST_USER_ID));

                // Act & Assert
                mockMvc.perform(get(ALERTS_ENDPOINT + "/check"))
                        .andExpect(status().isBadRequest());

                verify(checkAlertUseCase).checkAlert(TEST_USER_ID);
            }
        }
    }
}
