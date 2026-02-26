package com.cookingapp.unit.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cookingapp.domain.entity.ChatMessage;

/**
 * ChatMessageエンティティのユニットテスト
 */
@DisplayName("ChatMessage ユニットテスト")
class ChatMessageTest {

    private static final String TEST_CONVERSATION_ID = "conv-123";

    @Nested
    @DisplayName("create - 新しいメッセージを作成")
    class Create {

        @Test
        @DisplayName("新しいメッセージが正常に作成される")
        void shouldCreateNewMessage() {
            // Arrange
            String role = "user";
            String content = "こんにちは";
            String generatedRecipe = null;

            // Act
            ChatMessage message = ChatMessage.create(TEST_CONVERSATION_ID, role, content, generatedRecipe);

            // Assert
            assertThat(message).isNotNull();
            assertThat(message.getMessageId()).isNotNull().isNotEmpty();
            assertThat(message.getConversationId()).isEqualTo(TEST_CONVERSATION_ID);
            assertThat(message.getRole()).isEqualTo(role);
            assertThat(message.getContent()).isEqualTo(content);
            assertThat(message.getGeneratedRecipe()).isNull();
            assertThat(message.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("generatedRecipeが設定される")
        void shouldSetGeneratedRecipe() {
            // Arrange
            String generatedRecipe = "{\"title\": \"テストレシピ\"}";

            // Act
            ChatMessage message = ChatMessage.create(
                    TEST_CONVERSATION_ID, "assistant", "レシピを生成しました", generatedRecipe);

            // Assert
            assertThat(message.getGeneratedRecipe()).isEqualTo(generatedRecipe);
        }

        @Test
        @DisplayName("ULIDが毎回異なる値で生成される")
        void shouldGenerateUniqueMessageId() {
            // Act
            ChatMessage message1 = ChatMessage.create(TEST_CONVERSATION_ID, "user", "メッセージ1", null);
            ChatMessage message2 = ChatMessage.create(TEST_CONVERSATION_ID, "user", "メッセージ2", null);

            // Assert
            assertThat(message1.getMessageId()).isNotEqualTo(message2.getMessageId());
        }
    }

    @Nested
    @DisplayName("createUserMessage - ユーザーメッセージを作成")
    class CreateUserMessage {

        @Test
        @DisplayName("ユーザーメッセージが正常に作成される")
        void shouldCreateUserMessage() {
            // Arrange
            String content = "質問です";

            // Act
            ChatMessage message = ChatMessage.createUserMessage(TEST_CONVERSATION_ID, content);

            // Assert
            assertThat(message).isNotNull();
            assertThat(message.getMessageId()).isNotNull().isNotEmpty();
            assertThat(message.getConversationId()).isEqualTo(TEST_CONVERSATION_ID);
            assertThat(message.getRole()).isEqualTo("user");
            assertThat(message.getContent()).isEqualTo(content);
            assertThat(message.getGeneratedRecipe()).isNull();
            assertThat(message.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("roleが'user'に設定される")
        void shouldSetRoleToUser() {
            // Act
            ChatMessage message = ChatMessage.createUserMessage(TEST_CONVERSATION_ID, "テスト");

            // Assert
            assertThat(message.getRole()).isEqualTo("user");
        }
    }

    @Nested
    @DisplayName("createAssistantMessage - アシスタントメッセージを作成")
    class CreateAssistantMessage {

        @Test
        @DisplayName("アシスタントメッセージが正常に作成される")
        void shouldCreateAssistantMessage() {
            // Arrange
            String content = "応答です";
            String generatedRecipe = null;

            // Act
            ChatMessage message = ChatMessage.createAssistantMessage(
                    TEST_CONVERSATION_ID, content, generatedRecipe);

            // Assert
            assertThat(message).isNotNull();
            assertThat(message.getMessageId()).isNotNull().isNotEmpty();
            assertThat(message.getConversationId()).isEqualTo(TEST_CONVERSATION_ID);
            assertThat(message.getRole()).isEqualTo("assistant");
            assertThat(message.getContent()).isEqualTo(content);
            assertThat(message.getGeneratedRecipe()).isNull();
            assertThat(message.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("roleが'assistant'に設定される")
        void shouldSetRoleToAssistant() {
            // Act
            ChatMessage message = ChatMessage.createAssistantMessage(TEST_CONVERSATION_ID, "テスト", null);

            // Assert
            assertThat(message.getRole()).isEqualTo("assistant");
        }

        @Test
        @DisplayName("generatedRecipeが設定される")
        void shouldSetGeneratedRecipe() {
            // Arrange
            String generatedRecipe = "{\"title\": \"カレーライス\", \"ingredients\": []}";

            // Act
            ChatMessage message = ChatMessage.createAssistantMessage(
                    TEST_CONVERSATION_ID, "レシピを生成しました", generatedRecipe);

            // Assert
            assertThat(message.getGeneratedRecipe()).isEqualTo(generatedRecipe);
        }
    }

    @Nested
    @DisplayName("Builder - ビルダーパターン")
    class Builder {

        @Test
        @DisplayName("ビルダーで全フィールドを設定できる")
        void shouldBuildWithAllFields() {
            // Arrange
            Instant now = Instant.now();

            // Act
            ChatMessage message = ChatMessage.builder()
                    .messageId("msg-123")
                    .conversationId(TEST_CONVERSATION_ID)
                    .role("assistant")
                    .content("応答テスト")
                    .generatedRecipe("{\"title\": \"test\"}")
                    .createdAt(now)
                    .build();

            // Assert
            assertThat(message.getMessageId()).isEqualTo("msg-123");
            assertThat(message.getConversationId()).isEqualTo(TEST_CONVERSATION_ID);
            assertThat(message.getRole()).isEqualTo("assistant");
            assertThat(message.getContent()).isEqualTo("応答テスト");
            assertThat(message.getGeneratedRecipe()).isEqualTo("{\"title\": \"test\"}");
            assertThat(message.getCreatedAt()).isEqualTo(now);
        }

        @Test
        @DisplayName("generatedRecipeなしでビルドできる")
        void shouldBuildWithoutGeneratedRecipe() {
            // Act
            ChatMessage message = ChatMessage.builder()
                    .messageId("msg-123")
                    .conversationId(TEST_CONVERSATION_ID)
                    .role("user")
                    .content("質問")
                    .createdAt(Instant.now())
                    .build();

            // Assert
            assertThat(message.getGeneratedRecipe()).isNull();
        }
    }

    @Nested
    @DisplayName("MessageId - ULID形式")
    class MessageIdFormat {

        @Test
        @DisplayName("messageIdがULID形式で生成される（26文字）")
        void shouldGenerateUlidFormat() {
            // Act
            ChatMessage message = ChatMessage.createUserMessage(TEST_CONVERSATION_ID, "テスト");

            // Assert
            assertThat(message.getMessageId()).hasSize(26);
            assertThat(message.getMessageId()).matches("[0-9A-Z]+");
        }
    }
}
