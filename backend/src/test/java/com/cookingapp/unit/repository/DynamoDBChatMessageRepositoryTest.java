package com.cookingapp.unit.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cookingapp.domain.entity.ChatMessage;
import com.cookingapp.infrastructure.repository.DynamoDBChatMessageRepository;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemResponse;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

/**
 * DynamoDBChatMessageRepositoryのユニットテスト
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DynamoDBChatMessageRepository ユニットテスト")
class DynamoDBChatMessageRepositoryTest {

    @Mock
    private DynamoDbClient dynamoDbClient;

    private DynamoDBChatMessageRepository repository;

    private static final String TABLE_NAME = "ChatMessages";
    private static final String TEST_CONVERSATION_ID = "conv-123";
    private static final String TEST_MESSAGE_ID = "msg-456";

    @BeforeEach
    void setUp() {
        repository = new DynamoDBChatMessageRepository(dynamoDbClient, TABLE_NAME);
    }

    @Nested
    @DisplayName("save - メッセージを保存")
    class Save {

        @Test
        @DisplayName("メッセージが正常に保存される")
        void shouldSaveMessage() {
            // Arrange
            ChatMessage message = createTestMessage("user", "こんにちは", null);
            when(dynamoDbClient.putItem(any(PutItemRequest.class)))
                    .thenReturn(PutItemResponse.builder().build());

            // Act
            ChatMessage result = repository.save(message);

            // Assert
            assertThat(result).isEqualTo(message);

            ArgumentCaptor<PutItemRequest> captor = ArgumentCaptor.forClass(PutItemRequest.class);
            verify(dynamoDbClient).putItem(captor.capture());

            PutItemRequest request = captor.getValue();
            assertThat(request.tableName()).isEqualTo(TABLE_NAME);
            assertThat(request.item().get("ConversationId").s()).isEqualTo(TEST_CONVERSATION_ID);
            assertThat(request.item().get("MessageId").s()).isEqualTo(TEST_MESSAGE_ID);
            assertThat(request.item().get("Role").s()).isEqualTo("user");
            assertThat(request.item().get("Content").s()).isEqualTo("こんにちは");
            assertThat(request.item().containsKey("GeneratedRecipe")).isFalse();
        }

        @Test
        @DisplayName("generatedRecipeがある場合、保存される")
        void shouldSaveMessageWithGeneratedRecipe() {
            // Arrange
            String generatedRecipe = "{\"title\": \"テストレシピ\"}";
            ChatMessage message = createTestMessage("assistant", "レシピを生成しました", generatedRecipe);
            when(dynamoDbClient.putItem(any(PutItemRequest.class)))
                    .thenReturn(PutItemResponse.builder().build());

            // Act
            repository.save(message);

            // Assert
            ArgumentCaptor<PutItemRequest> captor = ArgumentCaptor.forClass(PutItemRequest.class);
            verify(dynamoDbClient).putItem(captor.capture());

            PutItemRequest request = captor.getValue();
            assertThat(request.item().get("GeneratedRecipe").s()).isEqualTo(generatedRecipe);
        }
    }

    @Nested
    @DisplayName("findByConversationId - 会話IDでメッセージ一覧を取得")
    class FindByConversationId {

        @Test
        @DisplayName("メッセージ一覧を取得できる")
        void shouldFindMessagesByConversationId() {
            // Arrange
            Instant now = Instant.now();
            List<Map<String, AttributeValue>> items = List.of(
                    Map.of(
                            "ConversationId", AttributeValue.builder().s(TEST_CONVERSATION_ID).build(),
                            "MessageId", AttributeValue.builder().s("msg-1").build(),
                            "Role", AttributeValue.builder().s("user").build(),
                            "Content", AttributeValue.builder().s("こんにちは").build(),
                            "CreatedAt", AttributeValue.builder().s(now.toString()).build()),
                    Map.of(
                            "ConversationId", AttributeValue.builder().s(TEST_CONVERSATION_ID).build(),
                            "MessageId", AttributeValue.builder().s("msg-2").build(),
                            "Role", AttributeValue.builder().s("assistant").build(),
                            "Content", AttributeValue.builder().s("こんにちは！").build(),
                            "CreatedAt", AttributeValue.builder().s(now.toString()).build()));

            when(dynamoDbClient.query(any(QueryRequest.class)))
                    .thenReturn(QueryResponse.builder().items(items).build());

            // Act
            List<ChatMessage> result = repository.findByConversationId(TEST_CONVERSATION_ID);

            // Assert
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getMessageId()).isEqualTo("msg-1");
            assertThat(result.get(0).getRole()).isEqualTo("user");
            assertThat(result.get(0).getContent()).isEqualTo("こんにちは");
            assertThat(result.get(1).getMessageId()).isEqualTo("msg-2");
            assertThat(result.get(1).getRole()).isEqualTo("assistant");

            ArgumentCaptor<QueryRequest> captor = ArgumentCaptor.forClass(QueryRequest.class);
            verify(dynamoDbClient).query(captor.capture());

            QueryRequest request = captor.getValue();
            assertThat(request.tableName()).isEqualTo(TABLE_NAME);
            assertThat(request.keyConditionExpression()).isEqualTo("ConversationId = :conversationId");
            assertThat(request.scanIndexForward()).isTrue();
        }

        @Test
        @DisplayName("generatedRecipeがある場合、正しくマッピングされる")
        void shouldMapGeneratedRecipe() {
            // Arrange
            Instant now = Instant.now();
            String generatedRecipe = "{\"title\": \"テストレシピ\"}";
            List<Map<String, AttributeValue>> items = List.of(
                    Map.of(
                            "ConversationId", AttributeValue.builder().s(TEST_CONVERSATION_ID).build(),
                            "MessageId", AttributeValue.builder().s("msg-1").build(),
                            "Role", AttributeValue.builder().s("assistant").build(),
                            "Content", AttributeValue.builder().s("レシピを生成しました").build(),
                            "GeneratedRecipe", AttributeValue.builder().s(generatedRecipe).build(),
                            "CreatedAt", AttributeValue.builder().s(now.toString()).build()));

            when(dynamoDbClient.query(any(QueryRequest.class)))
                    .thenReturn(QueryResponse.builder().items(items).build());

            // Act
            List<ChatMessage> result = repository.findByConversationId(TEST_CONVERSATION_ID);

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getGeneratedRecipe()).isEqualTo(generatedRecipe);
        }

        @Test
        @DisplayName("メッセージがない場合、空のリストを返す")
        void shouldReturnEmptyListWhenNoMessages() {
            // Arrange
            when(dynamoDbClient.query(any(QueryRequest.class)))
                    .thenReturn(QueryResponse.builder().items(List.of()).build());

            // Act
            List<ChatMessage> result = repository.findByConversationId(TEST_CONVERSATION_ID);

            // Assert
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("deleteByConversationId - 会話IDでメッセージを削除")
    class DeleteByConversationId {

        @Test
        @DisplayName("会話のメッセージが全て削除される")
        void shouldDeleteAllMessagesByConversationId() {
            // Arrange
            Instant now = Instant.now();
            List<Map<String, AttributeValue>> items = List.of(
                    Map.of(
                            "ConversationId", AttributeValue.builder().s(TEST_CONVERSATION_ID).build(),
                            "MessageId", AttributeValue.builder().s("msg-1").build(),
                            "Role", AttributeValue.builder().s("user").build(),
                            "Content", AttributeValue.builder().s("メッセージ1").build(),
                            "CreatedAt", AttributeValue.builder().s(now.toString()).build()),
                    Map.of(
                            "ConversationId", AttributeValue.builder().s(TEST_CONVERSATION_ID).build(),
                            "MessageId", AttributeValue.builder().s("msg-2").build(),
                            "Role", AttributeValue.builder().s("assistant").build(),
                            "Content", AttributeValue.builder().s("メッセージ2").build(),
                            "CreatedAt", AttributeValue.builder().s(now.toString()).build()));

            when(dynamoDbClient.query(any(QueryRequest.class)))
                    .thenReturn(QueryResponse.builder().items(items).build());
            when(dynamoDbClient.deleteItem(any(DeleteItemRequest.class)))
                    .thenReturn(DeleteItemResponse.builder().build());

            // Act
            repository.deleteByConversationId(TEST_CONVERSATION_ID);

            // Assert
            verify(dynamoDbClient).query(any(QueryRequest.class));
            verify(dynamoDbClient, times(2)).deleteItem(any(DeleteItemRequest.class));

            ArgumentCaptor<DeleteItemRequest> captor = ArgumentCaptor.forClass(DeleteItemRequest.class);
            verify(dynamoDbClient, times(2)).deleteItem(captor.capture());

            List<DeleteItemRequest> requests = captor.getAllValues();
            assertThat(requests.get(0).key().get("ConversationId").s()).isEqualTo(TEST_CONVERSATION_ID);
            assertThat(requests.get(0).key().get("MessageId").s()).isEqualTo("msg-1");
            assertThat(requests.get(1).key().get("MessageId").s()).isEqualTo("msg-2");
        }

        @Test
        @DisplayName("メッセージがない場合、削除処理は行われない")
        void shouldNotDeleteWhenNoMessages() {
            // Arrange
            when(dynamoDbClient.query(any(QueryRequest.class)))
                    .thenReturn(QueryResponse.builder().items(List.of()).build());

            // Act
            repository.deleteByConversationId(TEST_CONVERSATION_ID);

            // Assert
            verify(dynamoDbClient).query(any(QueryRequest.class));
            verify(dynamoDbClient, times(0)).deleteItem(any(DeleteItemRequest.class));
        }
    }

    // ヘルパーメソッド

    private ChatMessage createTestMessage(String role, String content, String generatedRecipe) {
        return ChatMessage.builder()
                .messageId(TEST_MESSAGE_ID)
                .conversationId(TEST_CONVERSATION_ID)
                .role(role)
                .content(content)
                .generatedRecipe(generatedRecipe)
                .createdAt(Instant.now())
                .build();
    }
}
