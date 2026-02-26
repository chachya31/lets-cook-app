package com.cookingapp.unit.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cookingapp.domain.entity.ChatConversation;
import com.cookingapp.infrastructure.repository.DynamoDBChatConversationRepository;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemResponse;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemResponse;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

/**
 * DynamoDBChatConversationRepositoryのユニットテスト
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DynamoDBChatConversationRepository ユニットテスト")
class DynamoDBChatConversationRepositoryTest {

    @Mock
    private DynamoDbClient dynamoDbClient;

    private DynamoDBChatConversationRepository repository;

    private static final String TABLE_NAME = "ChatConversations";
    private static final String TEST_USER_ID = "user-123";
    private static final String TEST_CONVERSATION_ID = "conv-456";

    @BeforeEach
    void setUp() {
        repository = new DynamoDBChatConversationRepository(dynamoDbClient, TABLE_NAME);
    }

    @Nested
    @DisplayName("save - 会話を保存")
    class Save {

        @Test
        @DisplayName("会話が正常に保存される")
        void shouldSaveConversation() {
            // Arrange
            ChatConversation conversation = createTestConversation();
            when(dynamoDbClient.putItem(any(PutItemRequest.class)))
                    .thenReturn(PutItemResponse.builder().build());

            // Act
            ChatConversation result = repository.save(conversation);

            // Assert
            assertThat(result).isEqualTo(conversation);

            ArgumentCaptor<PutItemRequest> captor = ArgumentCaptor.forClass(PutItemRequest.class);
            verify(dynamoDbClient).putItem(captor.capture());

            PutItemRequest request = captor.getValue();
            assertThat(request.tableName()).isEqualTo(TABLE_NAME);
            assertThat(request.item().get("UserId").s()).isEqualTo(TEST_USER_ID);
            assertThat(request.item().get("ConversationId").s()).isEqualTo(TEST_CONVERSATION_ID);
            assertThat(request.item().get("Title").s()).isEqualTo("テスト会話");
            assertThat(request.item().get("ConversationType").s()).isEqualTo("general");
        }
    }

    @Nested
    @DisplayName("findById - IDで会話を取得")
    class FindById {

        @Test
        @DisplayName("存在する会話を取得できる")
        void shouldFindConversationById() {
            // Arrange
            Instant now = Instant.now();
            Map<String, AttributeValue> item = Map.of(
                    "UserId", AttributeValue.builder().s(TEST_USER_ID).build(),
                    "ConversationId", AttributeValue.builder().s(TEST_CONVERSATION_ID).build(),
                    "Title", AttributeValue.builder().s("テスト会話").build(),
                    "ConversationType", AttributeValue.builder().s("general").build(),
                    "CreatedAt", AttributeValue.builder().s(now.toString()).build(),
                    "UpdatedAt", AttributeValue.builder().s(now.toString()).build());

            when(dynamoDbClient.getItem(any(GetItemRequest.class)))
                    .thenReturn(GetItemResponse.builder().item(item).build());

            // Act
            Optional<ChatConversation> result = repository.findById(TEST_USER_ID, TEST_CONVERSATION_ID);

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get().getConversationId()).isEqualTo(TEST_CONVERSATION_ID);
            assertThat(result.get().getUserId()).isEqualTo(TEST_USER_ID);
            assertThat(result.get().getTitle()).isEqualTo("テスト会話");
            assertThat(result.get().getConversationType()).isEqualTo("general");

            ArgumentCaptor<GetItemRequest> captor = ArgumentCaptor.forClass(GetItemRequest.class);
            verify(dynamoDbClient).getItem(captor.capture());

            GetItemRequest request = captor.getValue();
            assertThat(request.tableName()).isEqualTo(TABLE_NAME);
            assertThat(request.key().get("UserId").s()).isEqualTo(TEST_USER_ID);
            assertThat(request.key().get("ConversationId").s()).isEqualTo(TEST_CONVERSATION_ID);
        }

        @Test
        @DisplayName("存在しない会話の場合、空のOptionalを返す")
        void shouldReturnEmptyWhenNotFound() {
            // Arrange
            when(dynamoDbClient.getItem(any(GetItemRequest.class)))
                    .thenReturn(GetItemResponse.builder().item(Map.of()).build());

            // Act
            Optional<ChatConversation> result = repository.findById(TEST_USER_ID, TEST_CONVERSATION_ID);

            // Assert
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByUserId - ユーザーIDで会話一覧を取得")
    class FindByUserId {

        @Test
        @DisplayName("ユーザーの会話一覧を取得できる")
        void shouldFindConversationsByUserId() {
            // Arrange
            Instant now = Instant.now();
            List<Map<String, AttributeValue>> items = List.of(
                    Map.of(
                            "UserId", AttributeValue.builder().s(TEST_USER_ID).build(),
                            "ConversationId", AttributeValue.builder().s("conv-1").build(),
                            "Title", AttributeValue.builder().s("会話1").build(),
                            "ConversationType", AttributeValue.builder().s("general").build(),
                            "CreatedAt", AttributeValue.builder().s(now.toString()).build(),
                            "UpdatedAt", AttributeValue.builder().s(now.toString()).build()),
                    Map.of(
                            "UserId", AttributeValue.builder().s(TEST_USER_ID).build(),
                            "ConversationId", AttributeValue.builder().s("conv-2").build(),
                            "Title", AttributeValue.builder().s("会話2").build(),
                            "ConversationType", AttributeValue.builder().s("recipe_recommendation").build(),
                            "CreatedAt", AttributeValue.builder().s(now.toString()).build(),
                            "UpdatedAt", AttributeValue.builder().s(now.toString()).build()));

            when(dynamoDbClient.query(any(QueryRequest.class)))
                    .thenReturn(QueryResponse.builder().items(items).build());

            // Act
            List<ChatConversation> result = repository.findByUserId(TEST_USER_ID);

            // Assert
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getConversationId()).isEqualTo("conv-1");
            assertThat(result.get(0).getTitle()).isEqualTo("会話1");
            assertThat(result.get(1).getConversationId()).isEqualTo("conv-2");
            assertThat(result.get(1).getConversationType()).isEqualTo("recipe_recommendation");

            ArgumentCaptor<QueryRequest> captor = ArgumentCaptor.forClass(QueryRequest.class);
            verify(dynamoDbClient).query(captor.capture());

            QueryRequest request = captor.getValue();
            assertThat(request.tableName()).isEqualTo(TABLE_NAME);
            assertThat(request.keyConditionExpression()).isEqualTo("UserId = :userId");
        }

        @Test
        @DisplayName("会話がない場合、空のリストを返す")
        void shouldReturnEmptyListWhenNoConversations() {
            // Arrange
            when(dynamoDbClient.query(any(QueryRequest.class)))
                    .thenReturn(QueryResponse.builder().items(List.of()).build());

            // Act
            List<ChatConversation> result = repository.findByUserId(TEST_USER_ID);

            // Assert
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("delete - 会話を削除")
    class Delete {

        @Test
        @DisplayName("会話が正常に削除される")
        void shouldDeleteConversation() {
            // Arrange
            when(dynamoDbClient.deleteItem(any(DeleteItemRequest.class)))
                    .thenReturn(DeleteItemResponse.builder().build());

            // Act
            repository.delete(TEST_USER_ID, TEST_CONVERSATION_ID);

            // Assert
            ArgumentCaptor<DeleteItemRequest> captor = ArgumentCaptor.forClass(DeleteItemRequest.class);
            verify(dynamoDbClient).deleteItem(captor.capture());

            DeleteItemRequest request = captor.getValue();
            assertThat(request.tableName()).isEqualTo(TABLE_NAME);
            assertThat(request.key().get("UserId").s()).isEqualTo(TEST_USER_ID);
            assertThat(request.key().get("ConversationId").s()).isEqualTo(TEST_CONVERSATION_ID);
        }
    }

    // ヘルパーメソッド

    private ChatConversation createTestConversation() {
        return ChatConversation.builder()
                .conversationId(TEST_CONVERSATION_ID)
                .userId(TEST_USER_ID)
                .title("テスト会話")
                .conversationType("general")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}
