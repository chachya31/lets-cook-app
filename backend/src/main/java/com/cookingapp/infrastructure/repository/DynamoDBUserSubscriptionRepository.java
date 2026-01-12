package com.cookingapp.infrastructure.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.cookingapp.domain.entity.UserSubscription;
import com.cookingapp.domain.exception.QuotaExceededException;
import com.cookingapp.domain.repository.UserSubscriptionRepository;
import com.cookingapp.domain.valueobject.PlanType;
import com.cookingapp.domain.valueobject.SubscriptionStatus;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ReturnValue;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemResponse;

/**
 * DynamoDBを使用したUserSubscriptionRepositoryの実装
 */
@Repository
public class DynamoDBUserSubscriptionRepository implements UserSubscriptionRepository {

    private static final Logger log = LoggerFactory.getLogger(DynamoDBUserSubscriptionRepository.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final DynamoDbClientWrapper clientWrapper;
    private final String tableName;

    public DynamoDBUserSubscriptionRepository(
            DynamoDbClientWrapper clientWrapper,
            @Value("${aws.dynamodb.table.user-subscription:UserSubscription}") String tableName) {
        this.clientWrapper = clientWrapper;
        this.tableName = tableName;
    }

    @Override
    public UserSubscription save(UserSubscription subscription) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("UserId", AttributeValue.builder().s(subscription.getUserId()).build());
        item.put("PlanType", AttributeValue.builder().s(subscription.getPlanType().getCode()).build());
        item.put("RemainingCredits",
                AttributeValue.builder().n(String.valueOf(subscription.getRemainingCredits())).build());
        item.put("Status", AttributeValue.builder().s(subscription.getStatus().getCode()).build());
        item.put("TotalCreditsUsed",
                AttributeValue.builder().n(String.valueOf(subscription.getTotalCreditsUsed())).build());
        item.put("CreatedAt",
                AttributeValue.builder().s(subscription.getCreatedAt().format(DATETIME_FORMATTER)).build());
        item.put("UpdatedAt",
                AttributeValue.builder().s(subscription.getUpdatedAt().format(DATETIME_FORMATTER)).build());

        if (subscription.getResetDate() != null) {
            item.put("ResetDate",
                    AttributeValue.builder().s(subscription.getResetDate().format(DATE_FORMATTER)).build());
        }
        if (subscription.getPlanStartDate() != null) {
            item.put("PlanStartDate",
                    AttributeValue.builder().s(subscription.getPlanStartDate().format(DATE_FORMATTER)).build());
        }

        PutItemRequest request = PutItemRequest.builder()
                .tableName(tableName)
                .item(item)
                .build();

        clientWrapper.putItem(request);
        return subscription;
    }

    @Override
    public Optional<UserSubscription> findByUserId(String userId) {
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

        return Optional.of(mapToSubscription(response.item()));
    }

    @Override
    public int decrementCreditsAtomic(String userId) {
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("UserId", AttributeValue.builder().s(userId).build());

        Map<String, AttributeValue> expressionValues = new HashMap<>();
        expressionValues.put(":one", AttributeValue.builder().n("1").build());
        expressionValues.put(":zero", AttributeValue.builder().n("0").build());
        expressionValues.put(":now",
                AttributeValue.builder().s(LocalDateTime.now().format(DATETIME_FORMATTER)).build());

        try {
            UpdateItemRequest request = UpdateItemRequest.builder()
                    .tableName(tableName)
                    .key(key)
                    .updateExpression(
                            "SET RemainingCredits = RemainingCredits - :one, TotalCreditsUsed = TotalCreditsUsed + :one, UpdatedAt = :now")
                    .conditionExpression("RemainingCredits > :zero")
                    .expressionAttributeValues(expressionValues)
                    .returnValues(ReturnValue.ALL_NEW)
                    .build();

            UpdateItemResponse response = clientWrapper.updateItem(request);
            return Integer.parseInt(response.attributes().get("RemainingCredits").n());

        } catch (ConditionalCheckFailedException e) {
            log.warn("Credit decrement failed for user {}: quota exceeded", userId);
            Optional<UserSubscription> subscription = findByUserId(userId);
            if (subscription.isPresent()) {
                UserSubscription sub = subscription.get();
                throw new QuotaExceededException(
                        userId,
                        sub.getPlanType(),
                        sub.getPlanType().getMonthlyCredits(),
                        sub.getResetDate());
            }
            throw new QuotaExceededException(userId, PlanType.FREE, 10, null);
        }
    }

    @Override
    public int addCredits(String userId, int amount) {
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("UserId", AttributeValue.builder().s(userId).build());

        Map<String, AttributeValue> expressionValues = new HashMap<>();
        expressionValues.put(":amount", AttributeValue.builder().n(String.valueOf(amount)).build());
        expressionValues.put(":now",
                AttributeValue.builder().s(LocalDateTime.now().format(DATETIME_FORMATTER)).build());

        UpdateItemRequest request = UpdateItemRequest.builder()
                .tableName(tableName)
                .key(key)
                .updateExpression("SET RemainingCredits = RemainingCredits + :amount, UpdatedAt = :now")
                .expressionAttributeValues(expressionValues)
                .returnValues(ReturnValue.ALL_NEW)
                .build();

        UpdateItemResponse response = clientWrapper.updateItem(request);
        return Integer.parseInt(response.attributes().get("RemainingCredits").n());
    }

    @Override
    public void incrementTotalCreditsUsed(String userId) {
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("UserId", AttributeValue.builder().s(userId).build());

        Map<String, AttributeValue> expressionValues = new HashMap<>();
        expressionValues.put(":one", AttributeValue.builder().n("1").build());
        expressionValues.put(":now",
                AttributeValue.builder().s(LocalDateTime.now().format(DATETIME_FORMATTER)).build());

        UpdateItemRequest request = UpdateItemRequest.builder()
                .tableName(tableName)
                .key(key)
                .updateExpression("SET TotalCreditsUsed = TotalCreditsUsed + :one, UpdatedAt = :now")
                .expressionAttributeValues(expressionValues)
                .build();

        clientWrapper.updateItem(request);
    }

    private UserSubscription mapToSubscription(Map<String, AttributeValue> item) {
        String oderId = item.get("UserId").s();
        PlanType planType = PlanType.fromCode(item.get("PlanType").s());
        int remainingCredits = Integer.parseInt(item.get("RemainingCredits").n());
        SubscriptionStatus status = SubscriptionStatus.fromCode(item.get("Status").s());
        long totalCreditsUsed = Long.parseLong(item.get("TotalCreditsUsed").n());
        LocalDateTime createdAt = LocalDateTime.parse(item.get("CreatedAt").s(), DATETIME_FORMATTER);
        LocalDateTime updatedAt = LocalDateTime.parse(item.get("UpdatedAt").s(), DATETIME_FORMATTER);

        LocalDate resetDate = item.containsKey("ResetDate")
                ? LocalDate.parse(item.get("ResetDate").s(), DATE_FORMATTER)
                : null;

        LocalDate planStartDate = item.containsKey("PlanStartDate")
                ? LocalDate.parse(item.get("PlanStartDate").s(), DATE_FORMATTER)
                : null;

        return new UserSubscription(
                oderId,
                planType,
                remainingCredits,
                resetDate,
                status,
                planStartDate,
                totalCreditsUsed,
                createdAt,
                updatedAt);
    }
}
