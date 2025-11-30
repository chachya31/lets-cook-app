package com.cookingapp.infrastructure.repository;

import com.cookingapp.domain.entity.Review;
import com.cookingapp.domain.repository.ReviewRepository;
import com.cookingapp.domain.valueobject.ReviewStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * DynamoDB ReviewRepository Implementation
 */
@Repository
public class DynamoDBReviewRepository implements ReviewRepository {
    private static final Logger log = LoggerFactory.getLogger(DynamoDBReviewRepository.class);
    private static final String GSI_USER_INDEX = "GSI_User";

    private final DynamoDbClient dynamoDbClient;
    private final String tableName;

    public DynamoDBReviewRepository(
            DynamoDbClient dynamoDbClient,
            @org.springframework.beans.factory.annotation.Value("${aws.dynamodb.table.reviews:Reviews}") String tableName) {
        this.dynamoDbClient = dynamoDbClient;
        this.tableName = tableName;
    }

    @Override
    public Review save(Review review) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("RecipeId", AttributeValue.builder().s(review.getRecipeId()).build());
        item.put("ReviewId", AttributeValue.builder().s(review.getReviewId()).build());
        item.put("UserId", AttributeValue.builder().s(review.getUserId()).build());
        item.put("Rating", AttributeValue.builder().n(String.valueOf(review.getRating())).build());
        
        if (review.getComment() != null) {
            item.put("Comment", AttributeValue.builder().s(review.getComment()).build());
        }
        
        item.put("Status", AttributeValue.builder().s(review.getStatus().getCode()).build());
        item.put("ReportedCount", AttributeValue.builder().n(String.valueOf(review.getReportedCount())).build());
        item.put("CreatedAt", AttributeValue.builder().s(review.getCreatedAt().toString()).build());
        item.put("UpdatedAt", AttributeValue.builder().s(review.getUpdatedAt().toString()).build());

        PutItemRequest request = PutItemRequest.builder()
                .tableName(tableName)
                .item(item)
                .build();

        dynamoDbClient.putItem(request);
        log.info("Review saved: reviewId={}", review.getReviewId());
        return review;
    }

    @Override
    public Optional<Review> findById(String reviewId) {
        // ReviewIdのみでは検索できないため、全レビューをスキャンして検索
        // 本番環境では、RecipeIdとReviewIdの組み合わせで検索することを推奨
        ScanRequest scanRequest = ScanRequest.builder()
                .tableName(tableName)
                .filterExpression("ReviewId = :reviewId")
                .expressionAttributeValues(Map.of(
                        ":reviewId", AttributeValue.builder().s(reviewId).build()
                ))
                .build();

        ScanResponse response = dynamoDbClient.scan(scanRequest);
        
        if (response.items().isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(mapToReview(response.items().get(0)));
    }

    @Override
    public List<Review> findByRecipeId(String recipeId) {
        QueryRequest request = QueryRequest.builder()
                .tableName(tableName)
                .keyConditionExpression("RecipeId = :recipeId")
                .expressionAttributeValues(Map.of(
                        ":recipeId", AttributeValue.builder().s(recipeId).build()
                ))
                .build();

        QueryResponse response = dynamoDbClient.query(request);
        
        return response.items().stream()
                .map(this::mapToReview)
                .collect(Collectors.toList());
    }

    @Override
    public List<Review> findByUserId(String userId) {
        QueryRequest request = QueryRequest.builder()
                .tableName(tableName)
                .indexName(GSI_USER_INDEX)
                .keyConditionExpression("UserId = :userId")
                .expressionAttributeValues(Map.of(
                        ":userId", AttributeValue.builder().s(userId).build()
                ))
                .build();

        QueryResponse response = dynamoDbClient.query(request);
        
        return response.items().stream()
                .map(this::mapToReview)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(String reviewId) {
        // ReviewIdのみでは削除できないため、まず検索してRecipeIdを取得
        Optional<Review> review = findById(reviewId);
        if (review.isEmpty()) {
            log.warn("Review not found for deletion: reviewId={}", reviewId);
            return;
        }

        DeleteItemRequest request = DeleteItemRequest.builder()
                .tableName(tableName)
                .key(Map.of(
                        "RecipeId", AttributeValue.builder().s(review.get().getRecipeId()).build(),
                        "ReviewId", AttributeValue.builder().s(reviewId).build()
                ))
                .build();

        dynamoDbClient.deleteItem(request);
        log.info("Review deleted: reviewId={}", reviewId);
    }

    @Override
    public boolean existsById(String reviewId) {
        return findById(reviewId).isPresent();
    }

    private Review mapToReview(Map<String, AttributeValue> item) {
        return Review.builder()
                .reviewId(item.get("ReviewId").s())
                .recipeId(item.get("RecipeId").s())
                .userId(item.get("UserId").s())
                .rating(Integer.parseInt(item.get("Rating").n()))
                .comment(item.containsKey("Comment") ? item.get("Comment").s() : null)
                .status(ReviewStatus.fromCode(item.get("Status").s()))
                .reportedCount(Integer.parseInt(item.get("ReportedCount").n()))
                .createdAt(Instant.parse(item.get("CreatedAt").s()))
                .updatedAt(Instant.parse(item.get("UpdatedAt").s()))
                .build();
    }
}
