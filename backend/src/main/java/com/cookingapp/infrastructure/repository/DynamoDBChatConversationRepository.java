package com.cookingapp.infrastructure.repository;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.cookingapp.domain.entity.ChatConversation;
import com.cookingapp.domain.repository.ChatConversationRepository;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

/**
 * DynamoDB ChatConversationRepository実装
 */
@Repository
public class DynamoDBChatConversationRepository implements ChatConversationRepository {

    private static final Logger log = LoggerFactory.getLogger(DynamoDBChatConversationRepository.class);

    private final DynamoDbClient dynamoDbClient;
    private final String tableName;

    public DynamoDBChatConversationRepository(
            DynamoDbClient dynamoDbClient,
            @Value("${aws.dynamodb.table.chatConversations:ChatConversations}") String tableName) {
        this.dynamoDbClient = dynamoDbClient;
        this.tableName = tableName;
    }

    @Override
    public ChatConversation save(ChatConversation conversation) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("UserId", AttributeValue.builder().s(conversation.getUserId()).build());
        item.put("ConversationId", AttributeValue.builder().s(conversation.getConversationId()).build());
        item.put("Title", AttributeValue.builder().s(conversation.getTitle()).build());
        item.put("ConversationType", AttributeValue.builder().s(conversation.getConversationType()).build());
        item.put("CreatedAt", AttributeValue.builder().s(conversation.getCreatedAt().toString()).build());
        item.put("UpdatedAt", AttributeValue.builder().s(conversation.getUpdatedAt().toString()).build());

        PutItemRequest request = PutItemRequest.builder()
                .tableName(tableName)
                .item(item)
                .build();

        dynamoDbClient.putItem(request);
        log.info("ChatConversation saved: conversationId={}", conversation.getConversationId());
        return conversation;
    }

    @Override
    public Optional<ChatConversation> findById(String userId, String conversationId) {
        GetItemRequest request = GetItemRequest.builder()
                .tableName(tableName)
                .key(Map.of(
                        "UserId", AttributeValue.builder().s(userId).build(),
                        "ConversationId", AttributeValue.builder().s(conversationId).build()))
                .build();

        GetItemResponse response = dynamoDbClient.getItem(request);

        if (!response.hasItem() || response.item().isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(mapToConversation(response.item()));
    }

    @Override
    public List<ChatConversation> findByUserId(String userId) {
        QueryRequest request = QueryRequest.builder()
                .tableName(tableName)
                .keyConditionExpression("UserId = :userId")
                .expressionAttributeValues(Map.of(
                        ":userId", AttributeValue.builder().s(userId).build()))
                .scanIndexForward(false)
                .build();

        QueryResponse response = dynamoDbClient.query(request);

        return response.items().stream()
                .map(this::mapToConversation)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(String userId, String conversationId) {
        DeleteItemRequest request = DeleteItemRequest.builder()
                .tableName(tableName)
                .key(Map.of(
                        "UserId", AttributeValue.builder().s(userId).build(),
                        "ConversationId", AttributeValue.builder().s(conversationId).build()))
                .build();

        dynamoDbClient.deleteItem(request);
        log.info("ChatConversation deleted: conversationId={}", conversationId);
    }

    private ChatConversation mapToConversation(Map<String, AttributeValue> item) {
        return ChatConversation.builder()
                .conversationId(item.get("ConversationId").s())
                .userId(item.get("UserId").s())
                .title(item.get("Title").s())
                .conversationType(item.get("ConversationType").s())
                .createdAt(Instant.parse(item.get("CreatedAt").s()))
                .updatedAt(Instant.parse(item.get("UpdatedAt").s()))
                .build();
    }
}
