package com.cookingapp.presentation.dto.response;

import com.cookingapp.domain.entity.Review;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ReviewResponse DTO
 * レビューレスポンス
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {
    private String reviewId;
    private String recipeId;
    private String userId;
    private int rating;
    private String comment;
    private String status;
    private int reportedCount;
    private String createdAt;
    private String updatedAt;

    public static ReviewResponse from(Review review) {
        return ReviewResponse.builder()
                .reviewId(review.getReviewId())
                .recipeId(review.getRecipeId())
                .userId(review.getUserId())
                .rating(review.getRating())
                .comment(review.getComment())
                .status(review.getStatus().getCode())
                .reportedCount(review.getReportedCount())
                .createdAt(review.getCreatedAt().toString())
                .updatedAt(review.getUpdatedAt().toString())
                .build();
    }
}
