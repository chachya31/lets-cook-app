package com.cookingapp.unit.usecase.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cookingapp.application.usecase.ai.ChatUseCase;
import com.cookingapp.domain.entity.ChatConversation;
import com.cookingapp.domain.entity.ChatMessage;
import com.cookingapp.domain.repository.ChatConversationRepository;
import com.cookingapp.domain.repository.ChatMessageRepository;
import com.cookingapp.infrastructure.external.gemini.GeminiService;

/**
 * ChatUseCaseのユニットテスト
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ChatUseCase ユニットテスト")
class ChatUseCaseTest {

        @Mock
        private GeminiService geminiService;

        @Mock
        private ChatConversationRepository conversationRepository;

        @Mock
        private ChatMessageRepository messageRepository;

        private ChatUseCase chatUseCase;

        @BeforeEach
        void setUp() {
                chatUseCase = new ChatUseCase(geminiService, conversationRepository, messageRepository);
        }

        @Nested
        @DisplayName("chat - 新しい会話を開始")
        class ChatTests {

                @Test
                @DisplayName("新しい会話が正常に作成される")
                void testChat_CreatesNewConversation_Success() {
                        // Arrange
                        String userId = "user-123";
                        String message = "こんにちは";
                        String conversationType = "general";
                        String aiResponse = "こんにちは！何かお手伝いできますか？";

                        when(geminiService.generateContent(message)).thenReturn(aiResponse);

                        // Act
                        ChatUseCase.ChatResponse result = chatUseCase.chat(userId, message, conversationType);

                        // Assert
                        assertThat(result).isNotNull();
                        assertThat(result.conversationId()).isNotNull();
                        assertThat(result.response()).isEqualTo(aiResponse);

                        // 会話が保存されたことを確認
                        ArgumentCaptor<ChatConversation> conversationCaptor = ArgumentCaptor
                                        .forClass(ChatConversation.class);
                        verify(conversationRepository, times(2)).save(conversationCaptor.capture());

                        ChatConversation savedConversation = conversationCaptor.getAllValues().get(0);
                        assertThat(savedConversation.getUserId()).isEqualTo(userId);
                        assertThat(savedConversation.getConversationType()).isEqualTo(conversationType);

                        // メッセージが保存されたことを確認（ユーザーメッセージとAI応答）
                        verify(messageRepository, times(2)).save(any(ChatMessage.class));
                }

                @Test
                @DisplayName("長いメッセージのタイトルが30文字で切り詰められる")
                void testChat_TruncatesLongTitle() {
                        // Arrange
                        String userId = "user-123";
                        String longMessage = "これは非常に長いメッセージで、タイトルとして使用する場合は30文字で切り詰められるべきです";
                        String conversationType = "general";

                        when(geminiService.generateContent(longMessage)).thenReturn("応答");

                        // Act
                        chatUseCase.chat(userId, longMessage, conversationType);

                        // Assert
                        ArgumentCaptor<ChatConversation> conversationCaptor = ArgumentCaptor
                                        .forClass(ChatConversation.class);
                        verify(conversationRepository, times(2)).save(conversationCaptor.capture());

                        ChatConversation savedConversation = conversationCaptor.getAllValues().get(0);
                        assertThat(savedConversation.getTitle()).hasSize(33); // 30文字 + "..."
                        assertThat(savedConversation.getTitle()).endsWith("...");
                }

                @Test
                @DisplayName("recipe_recommendation タイプで会話が作成される")
                void testChat_WithRecipeRecommendationType() {
                        // Arrange
                        String userId = "user-123";
                        String message = "冷蔵庫にある食材でレシピを教えて";
                        String conversationType = "recipe_recommendation";

                        when(geminiService.generateContent(message)).thenReturn("レシピを提案します");

                        // Act
                        ChatUseCase.ChatResponse result = chatUseCase.chat(userId, message, conversationType);

                        // Assert
                        assertThat(result).isNotNull();

                        ArgumentCaptor<ChatConversation> conversationCaptor = ArgumentCaptor
                                        .forClass(ChatConversation.class);
                        verify(conversationRepository, times(2)).save(conversationCaptor.capture());

                        ChatConversation savedConversation = conversationCaptor.getAllValues().get(0);
                        assertThat(savedConversation.getConversationType()).isEqualTo("recipe_recommendation");
                }
        }

        @Nested
        @DisplayName("chatInConversation - 既存会話でチャット")
        class ChatInConversationTests {

                @Test
                @DisplayName("既存の会話でメッセージが正常に送信される")
                void testChatInConversation_Success() {
                        // Arrange
                        String userId = "user-123";
                        String conversationId = "conv-123";
                        String message = "続きの質問です";
                        String aiResponse = "続きの応答です";

                        ChatConversation existingConversation = ChatConversation.builder()
                                        .conversationId(conversationId)
                                        .userId(userId)
                                        .title("テスト会話")
                                        .conversationType("general")
                                        .createdAt(Instant.now())
                                        .updatedAt(Instant.now())
                                        .build();

                        when(conversationRepository.findById(userId, conversationId))
                                        .thenReturn(Optional.of(existingConversation));
                        when(geminiService.generateContent(message)).thenReturn(aiResponse);

                        // Act
                        ChatUseCase.ChatResponse result = chatUseCase.chatInConversation(userId, conversationId,
                                        message);

                        // Assert
                        assertThat(result).isNotNull();
                        assertThat(result.conversationId()).isEqualTo(conversationId);
                        assertThat(result.response()).isEqualTo(aiResponse);

                        verify(messageRepository, times(2)).save(any(ChatMessage.class));
                }

                @Test
                @DisplayName("存在しない会話でエラーが発生する")
                void testChatInConversation_ConversationNotFound() {
                        // Arrange
                        String userId = "user-123";
                        String conversationId = "non-existent";
                        String message = "テスト";

                        when(conversationRepository.findById(userId, conversationId))
                                        .thenReturn(Optional.empty());

                        // Act & Assert
                        assertThatThrownBy(() -> chatUseCase.chatInConversation(userId, conversationId, message))
                                        .isInstanceOf(IllegalArgumentException.class)
                                        .hasMessageContaining("Conversation not found");

                        verify(geminiService, never()).generateContent(anyString());
                }

                @Test
                @DisplayName("他のユーザーの会話にアクセスするとエラーが発生する")
                void testChatInConversation_AccessDenied() {
                        // Arrange
                        String requestUserId = "user-456";
                        String ownerUserId = "user-123";
                        String conversationId = "conv-123";
                        String message = "テスト";

                        ChatConversation existingConversation = ChatConversation.builder()
                                        .conversationId(conversationId)
                                        .userId(ownerUserId)
                                        .title("テスト会話")
                                        .conversationType("general")
                                        .createdAt(Instant.now())
                                        .updatedAt(Instant.now())
                                        .build();

                        when(conversationRepository.findById(requestUserId, conversationId))
                                        .thenReturn(Optional.of(existingConversation));

                        // Act & Assert
                        assertThatThrownBy(() -> chatUseCase.chatInConversation(requestUserId, conversationId, message))
                                        .isInstanceOf(IllegalArgumentException.class)
                                        .hasMessageContaining("Access denied");

                        verify(geminiService, never()).generateContent(anyString());
                }
        }

        @Nested
        @DisplayName("getConversations - 会話一覧取得")
        class GetConversationsTests {

                @Test
                @DisplayName("ユーザーの会話一覧が正常に取得される")
                void testGetConversations_Success() {
                        // Arrange
                        String userId = "user-123";
                        List<ChatConversation> conversations = List.of(
                                        ChatConversation.builder()
                                                        .conversationId("conv-1")
                                                        .userId(userId)
                                                        .title("会話1")
                                                        .conversationType("general")
                                                        .createdAt(Instant.now())
                                                        .updatedAt(Instant.now())
                                                        .build(),
                                        ChatConversation.builder()
                                                        .conversationId("conv-2")
                                                        .userId(userId)
                                                        .title("会話2")
                                                        .conversationType("recipe_recommendation")
                                                        .createdAt(Instant.now())
                                                        .updatedAt(Instant.now())
                                                        .build());

                        when(conversationRepository.findByUserId(userId)).thenReturn(conversations);

                        // Act
                        List<ChatConversation> result = chatUseCase.getConversations(userId);

                        // Assert
                        assertThat(result).hasSize(2);
                        assertThat(result.get(0).getTitle()).isEqualTo("会話1");
                        assertThat(result.get(1).getTitle()).isEqualTo("会話2");

                        verify(conversationRepository).findByUserId(userId);
                }

                @Test
                @DisplayName("会話がない場合は空のリストが返される")
                void testGetConversations_EmptyList() {
                        // Arrange
                        String userId = "user-123";

                        when(conversationRepository.findByUserId(userId)).thenReturn(List.of());

                        // Act
                        List<ChatConversation> result = chatUseCase.getConversations(userId);

                        // Assert
                        assertThat(result).isEmpty();
                }
        }

        @Nested
        @DisplayName("getMessages - メッセージ一覧取得")
        class GetMessagesTests {

                @Test
                @DisplayName("会話のメッセージ一覧が正常に取得される")
                void testGetMessages_Success() {
                        // Arrange
                        String userId = "user-123";
                        String conversationId = "conv-123";

                        ChatConversation conversation = ChatConversation.builder()
                                        .conversationId(conversationId)
                                        .userId(userId)
                                        .title("テスト会話")
                                        .conversationType("general")
                                        .createdAt(Instant.now())
                                        .updatedAt(Instant.now())
                                        .build();

                        List<ChatMessage> messages = List.of(
                                        ChatMessage.builder()
                                                        .messageId("msg-1")
                                                        .conversationId(conversationId)
                                                        .role("user")
                                                        .content("こんにちは")
                                                        .createdAt(Instant.now())
                                                        .build(),
                                        ChatMessage.builder()
                                                        .messageId("msg-2")
                                                        .conversationId(conversationId)
                                                        .role("assistant")
                                                        .content("こんにちは！")
                                                        .createdAt(Instant.now())
                                                        .build());

                        when(conversationRepository.findById(userId, conversationId))
                                        .thenReturn(Optional.of(conversation));
                        when(messageRepository.findByConversationId(conversationId)).thenReturn(messages);

                        // Act
                        List<ChatMessage> result = chatUseCase.getMessages(userId, conversationId);

                        // Assert
                        assertThat(result).hasSize(2);
                        assertThat(result.get(0).getRole()).isEqualTo("user");
                        assertThat(result.get(1).getRole()).isEqualTo("assistant");
                }

                @Test
                @DisplayName("存在しない会話でエラーが発生する")
                void testGetMessages_ConversationNotFound() {
                        // Arrange
                        String userId = "user-123";
                        String conversationId = "non-existent";

                        when(conversationRepository.findById(userId, conversationId))
                                        .thenReturn(Optional.empty());

                        // Act & Assert
                        assertThatThrownBy(() -> chatUseCase.getMessages(userId, conversationId))
                                        .isInstanceOf(IllegalArgumentException.class)
                                        .hasMessageContaining("Conversation not found");
                }
        }

        @Nested
        @DisplayName("deleteConversation - 会話削除")
        class DeleteConversationTests {

                @Test
                @DisplayName("会話が正常に削除される")
                void testDeleteConversation_Success() {
                        // Arrange
                        String userId = "user-123";
                        String conversationId = "conv-123";

                        ChatConversation conversation = ChatConversation.builder()
                                        .conversationId(conversationId)
                                        .userId(userId)
                                        .title("削除する会話")
                                        .conversationType("general")
                                        .createdAt(Instant.now())
                                        .updatedAt(Instant.now())
                                        .build();

                        when(conversationRepository.findById(userId, conversationId))
                                        .thenReturn(Optional.of(conversation));

                        // Act
                        chatUseCase.deleteConversation(userId, conversationId);

                        // Assert
                        verify(messageRepository).deleteByConversationId(conversationId);
                        verify(conversationRepository).delete(userId, conversationId);
                }

                @Test
                @DisplayName("存在しない会話の削除でエラーが発生する")
                void testDeleteConversation_ConversationNotFound() {
                        // Arrange
                        String userId = "user-123";
                        String conversationId = "non-existent";

                        when(conversationRepository.findById(userId, conversationId))
                                        .thenReturn(Optional.empty());

                        // Act & Assert
                        assertThatThrownBy(() -> chatUseCase.deleteConversation(userId, conversationId))
                                        .isInstanceOf(IllegalArgumentException.class)
                                        .hasMessageContaining("Conversation not found");

                        verify(messageRepository, never()).deleteByConversationId(anyString());
                        verify(conversationRepository, never()).delete(anyString(), anyString());
                }

                @Test
                @DisplayName("他のユーザーの会話を削除しようとするとエラーが発生する")
                void testDeleteConversation_AccessDenied() {
                        // Arrange
                        String requestUserId = "user-456";
                        String ownerUserId = "user-123";
                        String conversationId = "conv-123";

                        ChatConversation conversation = ChatConversation.builder()
                                        .conversationId(conversationId)
                                        .userId(ownerUserId)
                                        .title("他人の会話")
                                        .conversationType("general")
                                        .createdAt(Instant.now())
                                        .updatedAt(Instant.now())
                                        .build();

                        when(conversationRepository.findById(requestUserId, conversationId))
                                        .thenReturn(Optional.of(conversation));

                        // Act & Assert
                        assertThatThrownBy(() -> chatUseCase.deleteConversation(requestUserId, conversationId))
                                        .isInstanceOf(IllegalArgumentException.class)
                                        .hasMessageContaining("Access denied");

                        verify(messageRepository, never()).deleteByConversationId(anyString());
                        verify(conversationRepository, never()).delete(anyString(), anyString());
                }
        }
}
