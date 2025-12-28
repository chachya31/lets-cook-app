package com.cookingapp.infrastructure.repository;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.cookingapp.domain.entity.ChatMessage;
import com.cookingapp.domain.repository.ChatMessageRepository;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

/**
 * DynamoDB ChatMessageRepository実装
 */
@Repository
public class DynamoDBChatMessageRepository implements ChatMessageRepository {

    private static final Logger log = LoggerFactory.getLogger(DynamoDBChatMessageRepository.class);

    private final DynamoDbClient dynamoDbClient;
    private final String tableName;

    public DynamoDBChatMessageRepository(
            DynamoDbClient dynamoDbClient,
            @Value("${aws.dynamodb.table.chatMessages:ChatMessages}") String tableName) {
        this.dynamoDbClient = dynamoDbClient;
        this.tableName = tableName;
    }

    @Override
    public ChatMessage save(ChatMessage message) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("ConversationId", AttributeValue.builder().s(message.getConversationId()).build());
        item.put("MessageId", AttributeValue.builder().s(message.getMessageId()).build());
        item.put("Role", AttributeValue.builder().s(message.getRole()).build());
        item.put("Content", AttributeValue.builder().s(message.getContent()).build());
        item.put("CreatedAt", AttributeValue.builder().s(message.getCreatedAt().toString()).build());

        if (message.getGeneratedRecipe() != null) {
            item.put("GeneratedRecipe", AttributeValue.builder().s(message.getGeneratedRecipe()).build());
        }

        PutItemRequest request = PutItemRequest.builder()
                .tableName(tableName)
                .item(item)
                .build();

        dynamoDbClient.putItem(request);
        log.debug("ChatMessage saved: messageId={}", message.getMessageId());
        return message;
    }

    @Override
    public List<ChatMessage> findByConversationId(String conversationId) {
        QueryRequest request = QueryRequest.builder()
                .tableName(tableName)
                .keyConditionExpression("ConversationId = :conversationId")
                .expressionAttributeValues(Map.of(
                        ":conversationId", AttributeValue.builder().s(conversationId).build()))
                .scanIndexForward(true)
                .build();

        QueryResponse response = dynamoDbClient.query(request);

        return response.items().stream()
                .map(this::mapToMessage)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteByConversationId(String conversationId) {
        List<ChatMessage> messages = findByConversationId(conversationId);

        for (ChatMessage message : messages) {
            DeleteItemRequest request = DeleteItemRequest.builder()
                    .tableName(tableName)
                    .key(Map.of(
                            "ConversationId", AttributeValue.builder().s(conversationId).build(),
                            "MessageId", AttributeValue.builder().s(message.getMessageId()).build()))
                    .build();

            dynamoDbClient.deleteItem(request);
        }

        log.info("ChatMessages deleted for conversationId={}, count={}", conversationId, messages.size());
    }

    private ChatMessage mapToMessage(Map<String, AttributeValue> item) {
        return ChatMessage.builder()
                .messageId(item.get("MessageId").s())
                .conversationId(item.get("ConversationId").s())
                .role(item.get("Role").s())
                .content(item.get("Content").s())
                .generatedRecipe(item.containsKey("GeneratedRecipe") ? item.get("GeneratedRecipe").s() : null)
                .createdAt(Instant.parse(item.get("CreatedAt").s()))
                .build();
    }
}
