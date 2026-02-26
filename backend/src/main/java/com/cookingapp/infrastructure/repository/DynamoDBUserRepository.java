package com.cookingapp.infrastructure.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.cookingapp.domain.entity.User;
import com.cookingapp.domain.repository.UserRepository;
import com.cookingapp.domain.valueobject.Language;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;

/**
 * DynamoDBを使用したUserRepositoryの実装
 */
@Repository
public class DynamoDBUserRepository implements UserRepository {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final DynamoDbClientWrapper clientWrapper;
    private final String tableName;

    public DynamoDBUserRepository(
            DynamoDbClientWrapper clientWrapper,
            @org.springframework.beans.factory.annotation.Value("${aws.dynamodb.table.users:Users}") String tableName) {
        this.clientWrapper = clientWrapper;
        this.tableName = tableName;
    }

    @Override
    public User save(User user) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("UserId", AttributeValue.builder().s(user.getUserId()).build());
        item.put("Email", AttributeValue.builder().s(user.getEmail()).build());
        item.put("Nickname", AttributeValue.builder().s(user.getNickname()).build());
        item.put("DisplayName", AttributeValue.builder().s(user.getDisplayName()).build());

        if (user.getProfileImageUrl() != null) {
            item.put("ProfileImageUrl", AttributeValue.builder().s(user.getProfileImageUrl()).build());
        }

        item.put("PreferredLanguage", AttributeValue.builder().s(user.getPreferredLanguage().getCode()).build());

        if (user.getLastCookingDate() != null) {
            item.put("LastCookingDate", AttributeValue.builder()
                    .s(user.getLastCookingDate().format(DATE_FORMATTER)).build());
        }

        if (user.getLastLoginDate() != null) {
            item.put("LastLoginDate", AttributeValue.builder()
                    .s(user.getLastLoginDate().format(DATETIME_FORMATTER)).build());
        }

        item.put("CreatedAt", AttributeValue.builder()
                .s(user.getCreatedAt().format(DATETIME_FORMATTER)).build());
        item.put("Timezone", AttributeValue.builder().s(user.getTimezone()).build());
        item.put("MarketingOptOut", AttributeValue.builder().bool(user.isMarketingOptOut()).build());

        PutItemRequest request = PutItemRequest.builder()
                .tableName(tableName)
                .item(item)
                .build();

        clientWrapper.putItem(request);
        return user;
    }

    @Override
    public Optional<User> findById(String userId) {
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("UserId", AttributeValue.builder().s(userId).build());

        GetItemRequest request = GetItemRequest.builder()
                .tableName(tableName)
                .key(key)
                .build();

        GetItemResponse response = clientWrapper.getItem(request);

        if (!response.hasItem() || response.item().isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(mapToUser(response.item()));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        // DynamoDBではemailでの検索にはScanが必要
        // 本番環境ではGSI（Global Secondary Index）を使用すべき
        Map<String, AttributeValue> expressionValues = new HashMap<>();
        expressionValues.put(":email", AttributeValue.builder().s(email).build());

        ScanRequest request = ScanRequest.builder()
                .tableName(tableName)
                .filterExpression("Email = :email")
                .expressionAttributeValues(expressionValues)
                .build();

        ScanResponse response = clientWrapper.scan(request);

        if (response.items().isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(mapToUser(response.items().get(0)));
    }

    @Override
    public void delete(String userId) {
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("UserId", AttributeValue.builder().s(userId).build());

        DeleteItemRequest request = DeleteItemRequest.builder()
                .tableName(tableName)
                .key(key)
                .build();

        clientWrapper.deleteItem(request);
    }

    @Override
    public boolean existsById(String userId) {
        return findById(userId).isPresent();
    }

    @Override
    public List<User> findAll() {
        ScanRequest request = ScanRequest.builder()
                .tableName(tableName)
                .build();

        ScanResponse response = clientWrapper.scan(request);

        return response.items().stream()
                .map(this::mapToUser)
                .collect(Collectors.toList());
    }

    /**
     * DynamoDBアイテムをUserエンティティにマッピング
     */
    private User mapToUser(Map<String, AttributeValue> item) {
        String userId = item.get("UserId").s();
        String email = item.get("Email").s();
        String nickname = item.get("Nickname").s();
        String displayName = item.get("DisplayName").s();

        String profileImageUrl = item.containsKey("ProfileImageUrl")
                ? item.get("ProfileImageUrl").s()
                : null;

        Language preferredLanguage = Language.fromCode(item.get("PreferredLanguage").s());

        LocalDate lastCookingDate = item.containsKey("LastCookingDate")
                ? LocalDate.parse(item.get("LastCookingDate").s(), DATE_FORMATTER)
                : null;

        LocalDateTime lastLoginDate = item.containsKey("LastLoginDate")
                ? LocalDateTime.parse(item.get("LastLoginDate").s(), DATETIME_FORMATTER)
                : null;

        LocalDateTime createdAt = LocalDateTime.parse(item.get("CreatedAt").s(), DATETIME_FORMATTER);

        String timezone = item.get("Timezone").s();
        boolean marketingOptOut = item.get("MarketingOptOut").bool();

        return new User(
                userId,
                email,
                nickname,
                displayName,
                profileImageUrl,
                preferredLanguage,
                lastCookingDate,
                lastLoginDate,
                createdAt,
                timezone,
                marketingOptOut);
    }
}
