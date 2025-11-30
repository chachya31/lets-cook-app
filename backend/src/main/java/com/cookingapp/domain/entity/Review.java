package com.cookingapp.domain.entity;

import com.cookingapp.domain.valueobject.ReviewStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

/**
 * Review Entity
 * レビュードメインエンティティ
 */
@Getter
@Builder
@AllArgsConstructor
public class Review {
    private final String reviewId;
    private final String recipeId;
    private final String userId;
    private final int rating;
    private final String comment;
    private ReviewStatus status;
    private int reportedCount;
    private final Instant createdAt;
    private Instant updatedAt;

    /**
     * 新しいレビューを作成
     */
    public static Review create(String recipeId, String userId, int rating, String comment) {
        validateRating(rating);
        validateComment(comment);

        Instant now = Instant.now();
        return Review.builder()
                .reviewId(UUID.randomUUID().toString())
                .recipeId(recipeId)
                .userId(userId)
                .rating(rating)
                .comment(comment)
                .status(ReviewStatus.VISIBLE)
                .reportedCount(0)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    /**
     * レビューを更新
     */
    public void update(int rating, String comment) {
        validateRating(rating);
        validateComment(comment);
        this.updatedAt = Instant.now();
    }

    /**
     * 通報カウントを増加
     */
    public void incrementReportCount() {
        this.reportedCount++;
        if (shouldHide()) {
            this.status = ReviewStatus.HIDDEN;
        }
        this.updatedAt = Instant.now();
    }

    /**
     * 自動非表示判定
     */
    public boolean shouldHide() {
        return this.reportedCount >= 3;
    }

    /**
     * 編集・削除権限チェック
     */
    public boolean canEdit(String requestUserId) {
        return this.userId.equals(requestUserId);
    }

    /**
     * 表示可能かチェック
     */
    public boolean isVisible() {
        return this.status == ReviewStatus.VISIBLE;
    }

    /**
     * 星評価のバリデーション
     */
    private static void validateRating(int rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
    }

    /**
     * コメントのバリデーション
     */
    private static void validateComment(String comment) {
        if (comment != null && comment.length() > 300) {
            throw new IllegalArgumentException("Comment must be 300 characters or less");
        }
    }
}
