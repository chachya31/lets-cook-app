package com.cookingapp.unit.controller;

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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.cookingapp.application.usecase.ai.ChatUseCase;
import com.cookingapp.domain.entity.ChatConversation;
import com.cookingapp.domain.entity.ChatMessage;
import com.cookingapp.presentation.controller.AiAssistantController;
import com.cookingapp.presentation.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * AiAssistantControllerのユニットテスト
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AiAssistantController ユニットテスト")
class AiAssistantControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private ChatUseCase chatUseCase;

    @Mock
    private MessageSource messageSource;

    private static final String AI_ENDPOINT = "/api/ai";
    private static final String TEST_USER_ID = "user-123";
    private static final String TEST_CONVERSATION_ID = "conv-456";

    @BeforeEach
    void setUp() {
        AiAssistantController controller = new AiAssistantController(chatUseCase);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler(messageSource))
                .build();
        objectMapper = new ObjectMapper();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void setAuthenticatedUser(String userId) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Nested
    @DisplayName("POST /api/ai/chat - 新しい会話を開始")
    class Chat {

        @Nested
        @DisplayName("正常系")
        class Success {

            @Test
            @DisplayName("新しい会話が正常に作成される")
            void shouldReturn201WhenChatSuccessful() throws Exception {
                // Arrange
                setAuthenticatedUser(TEST_USER_ID);
                String requestBody = """
                    {"message": "こんにちは", "conversationType": "general"}
                    """;
                ChatUseCase.ChatResponse chatResponse =
                        new ChatUseCase.ChatResponse(TEST_CONVERSATION_ID, "こんにちは！何かお手伝いできますか？");

                when(chatUseCase.chat(eq(TEST_USER_ID), eq("こんにちは"), eq("general")))
                        .thenReturn(chatResponse);

                // Act & Assert
                mockMvc.perform(post(AI_ENDPOINT + "/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.conversationId").value(TEST_CONVERSATION_ID))
                        .andExpect(jsonPath("$.response").value("こんにちは！何かお手伝いできますか？"));

                verify(chatUseCase).chat(TEST_USER_ID, "こんにちは", "general");
            }

            @Test
            @DisplayName("conversationTypeがnullの場合、generalがデフォルトで使用される")
            void shouldUseGeneralAsDefaultConversationType() throws Exception {
                // Arrange
                setAuthenticatedUser(TEST_USER_ID);
                String requestBody = """
                    {"message": "こんにちは"}
                    """;
                ChatUseCase.ChatResponse chatResponse =
                        new ChatUseCase.ChatResponse(TEST_CONVERSATION_ID, "応答");

                when(chatUseCase.chat(eq(TEST_USER_ID), eq("こんにちは"), eq("general")))
                        .thenReturn(chatResponse);

                // Act & Assert
                mockMvc.perform(post(AI_ENDPOINT + "/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                        .andExpect(status().isCreated());

                verify(chatUseCase).chat(TEST_USER_ID, "こんにちは", "general");
            }
        }

        @Nested
        @DisplayName("異常系")
        class Failure {

            @Test
            @DisplayName("未認証の場合、403 Forbiddenを返す")
            void shouldReturn403WhenNotAuthenticated() throws Exception {
                // Arrange - SecurityContextに認証情報を設定しない
                String requestBody = """
                    {"message": "こんにちは", "conversationType": "general"}
                    """;

                // Act & Assert
                mockMvc.perform(post(AI_ENDPOINT + "/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                        .andExpect(status().isForbidden());

                verify(chatUseCase, never()).chat(anyString(), anyString(), anyString());
            }
        }
    }

    @Nested
    @DisplayName("GET /api/ai/conversations - 会話一覧取得")
    class GetConversations {

        @Nested
        @DisplayName("正常系")
        class Success {

            @Test
            @DisplayName("会話一覧が正常に取得される")
            void shouldReturn200WithConversations() throws Exception {
                // Arrange
                setAuthenticatedUser(TEST_USER_ID);
                List<ChatConversation> conversations = List.of(
                        createTestConversation("conv-1", "会話1"),
                        createTestConversation("conv-2", "会話2"));

                when(chatUseCase.getConversations(TEST_USER_ID)).thenReturn(conversations);

                // Act & Assert
                mockMvc.perform(get(AI_ENDPOINT + "/conversations"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$[0].conversationId").value("conv-1"))
                        .andExpect(jsonPath("$[0].title").value("会話1"))
                        .andExpect(jsonPath("$[1].conversationId").value("conv-2"))
                        .andExpect(jsonPath("$[1].title").value("会話2"));

                verify(chatUseCase).getConversations(TEST_USER_ID);
            }

            @Test
            @DisplayName("会話がない場合、空のリストを返す")
            void shouldReturnEmptyListWhenNoConversations() throws Exception {
                // Arrange
                setAuthenticatedUser(TEST_USER_ID);
                when(chatUseCase.getConversations(TEST_USER_ID)).thenReturn(Collections.emptyList());

                // Act & Assert
                mockMvc.perform(get(AI_ENDPOINT + "/conversations"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$").isArray())
                        .andExpect(jsonPath("$").isEmpty());

                verify(chatUseCase).getConversations(TEST_USER_ID);
            }
        }
    }

    @Nested
    @DisplayName("GET /api/ai/conversations/{conversationId}/messages - メッセージ一覧取得")
    class GetMessages {

        @Nested
        @DisplayName("正常系")
        class Success {

            @Test
            @DisplayName("メッセージ一覧が正常に取得される")
            void shouldReturn200WithMessages() throws Exception {
                // Arrange
                setAuthenticatedUser(TEST_USER_ID);
                List<ChatMessage> messages = List.of(
                        createTestMessage("msg-1", "user", "こんにちは"),
                        createTestMessage("msg-2", "assistant", "こんにちは！"));

                when(chatUseCase.getMessages(TEST_USER_ID, TEST_CONVERSATION_ID)).thenReturn(messages);

                // Act & Assert
                mockMvc.perform(get(AI_ENDPOINT + "/conversations/" + TEST_CONVERSATION_ID + "/messages"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$[0].messageId").value("msg-1"))
                        .andExpect(jsonPath("$[0].role").value("user"))
                        .andExpect(jsonPath("$[0].content").value("こんにちは"))
                        .andExpect(jsonPath("$[1].messageId").value("msg-2"))
                        .andExpect(jsonPath("$[1].role").value("assistant"));

                verify(chatUseCase).getMessages(TEST_USER_ID, TEST_CONVERSATION_ID);
            }
        }

        @Nested
        @DisplayName("異常系")
        class Failure {

            @Test
            @DisplayName("会話が存在しない場合、400 Bad Requestを返す")
            void shouldReturn400WhenConversationNotFound() throws Exception {
                // Arrange
                setAuthenticatedUser(TEST_USER_ID);
                when(chatUseCase.getMessages(TEST_USER_ID, TEST_CONVERSATION_ID))
                        .thenThrow(new IllegalArgumentException("Conversation not found"));

                // Act & Assert
                mockMvc.perform(get(AI_ENDPOINT + "/conversations/" + TEST_CONVERSATION_ID + "/messages"))
                        .andExpect(status().isBadRequest());

                verify(chatUseCase).getMessages(TEST_USER_ID, TEST_CONVERSATION_ID);
            }
        }
    }

    @Nested
    @DisplayName("POST /api/ai/conversations/{conversationId}/messages - メッセージ送信")
    class SendMessage {

        @Nested
        @DisplayName("正常系")
        class Success {

            @Test
            @DisplayName("メッセージが正常に送信される")
            void shouldReturn201WhenMessageSent() throws Exception {
                // Arrange
                setAuthenticatedUser(TEST_USER_ID);
                String requestBody = """
                    {"message": "続きの質問です"}
                    """;
                ChatUseCase.ChatResponse chatResponse =
                        new ChatUseCase.ChatResponse(TEST_CONVERSATION_ID, "続きの応答です");

                when(chatUseCase.chatInConversation(TEST_USER_ID, TEST_CONVERSATION_ID, "続きの質問です"))
                        .thenReturn(chatResponse);

                // Act & Assert
                mockMvc.perform(post(AI_ENDPOINT + "/conversations/" + TEST_CONVERSATION_ID + "/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.conversationId").value(TEST_CONVERSATION_ID))
                        .andExpect(jsonPath("$.response").value("続きの応答です"));

                verify(chatUseCase).chatInConversation(TEST_USER_ID, TEST_CONVERSATION_ID, "続きの質問です");
            }
        }

        @Nested
        @DisplayName("異常系")
        class Failure {

            @Test
            @DisplayName("会話が存在しない場合、400 Bad Requestを返す")
            void shouldReturn400WhenConversationNotFound() throws Exception {
                // Arrange
                setAuthenticatedUser(TEST_USER_ID);
                String requestBody = """
                    {"message": "テスト"}
                    """;

                when(chatUseCase.chatInConversation(TEST_USER_ID, TEST_CONVERSATION_ID, "テスト"))
                        .thenThrow(new IllegalArgumentException("Conversation not found"));

                // Act & Assert
                mockMvc.perform(post(AI_ENDPOINT + "/conversations/" + TEST_CONVERSATION_ID + "/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                        .andExpect(status().isBadRequest());
            }

            @Test
            @DisplayName("アクセス権がない場合、400 Bad Requestを返す")
            void shouldReturn400WhenAccessDenied() throws Exception {
                // Arrange
                setAuthenticatedUser(TEST_USER_ID);
                String requestBody = """
                    {"message": "テスト"}
                    """;

                when(chatUseCase.chatInConversation(TEST_USER_ID, TEST_CONVERSATION_ID, "テスト"))
                        .thenThrow(new IllegalArgumentException("Access denied"));

                // Act & Assert
                mockMvc.perform(post(AI_ENDPOINT + "/conversations/" + TEST_CONVERSATION_ID + "/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                        .andExpect(status().isBadRequest());
            }
        }
    }

    @Nested
    @DisplayName("DELETE /api/ai/conversations/{conversationId} - 会話削除")
    class DeleteConversation {

        @Nested
        @DisplayName("正常系")
        class Success {

            @Test
            @DisplayName("会話が正常に削除される")
            void shouldReturn204WhenConversationDeleted() throws Exception {
                // Arrange
                setAuthenticatedUser(TEST_USER_ID);
                doNothing().when(chatUseCase).deleteConversation(TEST_USER_ID, TEST_CONVERSATION_ID);

                // Act & Assert
                mockMvc.perform(delete(AI_ENDPOINT + "/conversations/" + TEST_CONVERSATION_ID))
                        .andExpect(status().isNoContent());

                verify(chatUseCase).deleteConversation(TEST_USER_ID, TEST_CONVERSATION_ID);
            }
        }

        @Nested
        @DisplayName("異常系")
        class Failure {

            @Test
            @DisplayName("会話が存在しない場合、400 Bad Requestを返す")
            void shouldReturn400WhenConversationNotFound() throws Exception {
                // Arrange
                setAuthenticatedUser(TEST_USER_ID);
                doThrow(new IllegalArgumentException("Conversation not found"))
                        .when(chatUseCase).deleteConversation(TEST_USER_ID, TEST_CONVERSATION_ID);

                // Act & Assert
                mockMvc.perform(delete(AI_ENDPOINT + "/conversations/" + TEST_CONVERSATION_ID))
                        .andExpect(status().isBadRequest());

                verify(chatUseCase).deleteConversation(TEST_USER_ID, TEST_CONVERSATION_ID);
            }

            @Test
            @DisplayName("アクセス権がない場合、400 Bad Requestを返す")
            void shouldReturn400WhenAccessDenied() throws Exception {
                // Arrange
                setAuthenticatedUser(TEST_USER_ID);
                doThrow(new IllegalArgumentException("Access denied"))
                        .when(chatUseCase).deleteConversation(TEST_USER_ID, TEST_CONVERSATION_ID);

                // Act & Assert
                mockMvc.perform(delete(AI_ENDPOINT + "/conversations/" + TEST_CONVERSATION_ID))
                        .andExpect(status().isBadRequest());
            }
        }
    }

    // ヘルパーメソッド

    private ChatConversation createTestConversation(String conversationId, String title) {
        return ChatConversation.builder()
                .conversationId(conversationId)
                .userId(TEST_USER_ID)
                .title(title)
                .conversationType("general")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    private ChatMessage createTestMessage(String messageId, String role, String content) {
        return ChatMessage.builder()
                .messageId(messageId)
                .conversationId(TEST_CONVERSATION_ID)
                .role(role)
                .content(content)
                .createdAt(Instant.now())
                .build();
    }
}
