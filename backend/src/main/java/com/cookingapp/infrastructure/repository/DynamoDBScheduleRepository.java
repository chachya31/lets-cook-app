package com.cookingapp.infrastructure.repository;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.cookingapp.domain.entity.Schedule;
import com.cookingapp.domain.repository.ScheduleRepository;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;

/**
 * DynamoDB実装のScheduleRepository
 */
@Repository
public class DynamoDBScheduleRepository implements ScheduleRepository {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private final DynamoDbClient dynamoDbClient;
    private final String tableName;

    public DynamoDBScheduleRepository(
            DynamoDbClient dynamoDbClient,
            @org.springframework.beans.factory.annotation.Value("${aws.dynamodb.table.schedules:Schedules}") String tableName) {
        this.dynamoDbClient = dynamoDbClient;
        this.tableName = tableName;
    }

    @Override
    public Schedule save(Schedule schedule) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("UserId", AttributeValue.builder().s(schedule.getUserId()).build());
        item.put("DateRecipeId", AttributeValue.builder()
                .s(buildSortKey(schedule.getDate(), schedule.getRecipeId()))
                .build());
        item.put("ScheduleId", AttributeValue.builder().s(schedule.getScheduleId()).build());
        item.put("Date", AttributeValue.builder().s(schedule.getDate().format(DATE_FORMATTER)).build());
        item.put("RecipeId", AttributeValue.builder().s(schedule.getRecipeId()).build());
        item.put("RecipeTitle", AttributeValue.builder().s(schedule.getRecipeTitle()).build());
        item.put("IsDone", AttributeValue.builder().bool(schedule.isDone()).build());
        if (schedule.getMemo() != null) {
            item.put("Memo", AttributeValue.builder().s(schedule.getMemo()).build());
        }
        item.put("CreatedAt", AttributeValue.builder().s(schedule.getCreatedAt().toString()).build());

        PutItemRequest request = PutItemRequest.builder()
                .tableName(tableName)
                .item(item)
                .build();

        dynamoDbClient.putItem(request);
        return schedule;
    }

    @Override
    public Optional<Schedule> findById(String scheduleId) {
        // ScheduleIdでスキャン（非効率だが、IDでの検索は稀なため許容）
        ScanRequest scanRequest = ScanRequest.builder()
                .tableName(tableName)
                .filterExpression("ScheduleId = :scheduleId")
                .expressionAttributeValues(Map.of(
                        ":scheduleId", AttributeValue.builder().s(scheduleId).build()))
                .build();

        ScanResponse response = dynamoDbClient.scan(scanRequest);
        if (response.items().isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(mapToSchedule(response.items().get(0)));
    }

    @Override
    public List<Schedule> findByUserIdAndDateRange(String userId, LocalDate startDate, LocalDate endDate) {
        // SortKeyは "Date#RecipeId" 形式なので、日付の範囲で前方一致検索
        String startSortKey = startDate.format(DATE_FORMATTER);
        // endDateの翌日を使って、endDate当日の全てのアイテムを含める
        String endSortKey = endDate.plusDays(1).format(DATE_FORMATTER);

        QueryRequest queryRequest = QueryRequest.builder()
                .tableName(tableName)
                .keyConditionExpression("UserId = :userId AND DateRecipeId BETWEEN :startKey AND :endKey")
                .expressionAttributeValues(Map.of(
                        ":userId", AttributeValue.builder().s(userId).build(),
                        ":startKey", AttributeValue.builder().s(startSortKey).build(),
                        ":endKey", AttributeValue.builder().s(endSortKey).build()))
                .build();

        QueryResponse response = dynamoDbClient.query(queryRequest);
        return response.items().stream()
                .map(this::mapToSchedule)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(String scheduleId) {
        // まずScheduleIdでアイテムを検索してキーを取得
        Optional<Schedule> schedule = findById(scheduleId);
        if (schedule.isEmpty()) {
            return;
        }

        Schedule s = schedule.get();
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("UserId", AttributeValue.builder().s(s.getUserId()).build());
        key.put("DateRecipeId", AttributeValue.builder()
                .s(buildSortKey(s.getDate(), s.getRecipeId()))
                .build());

        DeleteItemRequest request = DeleteItemRequest.builder()
                .tableName(tableName)
                .key(key)
                .build();

        dynamoDbClient.deleteItem(request);
    }

    @Override
    public boolean existsById(String scheduleId) {
        return findById(scheduleId).isPresent();
    }

    /**
     * SortKeyを構築（Date#RecipeId形式）
     */
    private String buildSortKey(LocalDate date, String recipeId) {
        return String.format("%s#%s", date.format(DATE_FORMATTER), recipeId);
    }

    /**
     * DynamoDBアイテムをScheduleエンティティにマッピング
     */
    private Schedule mapToSchedule(Map<String, AttributeValue> item) {
        return Schedule.builder()
                .scheduleId(item.get("ScheduleId").s())
                .userId(item.get("UserId").s())
                .date(LocalDate.parse(item.get("Date").s(), DATE_FORMATTER))
                .recipeId(item.get("RecipeId").s())
                .recipeTitle(item.get("RecipeTitle").s())
                .isDone(item.containsKey("IsDone") ? item.get("IsDone").bool() : false)
                .memo(item.containsKey("Memo") ? item.get("Memo").s() : null)
                .createdAt(Instant.parse(item.get("CreatedAt").s()))
                .build();
    }
}
