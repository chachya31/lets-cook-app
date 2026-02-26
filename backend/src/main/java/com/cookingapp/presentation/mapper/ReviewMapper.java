package com.cookingapp.presentation.mapper;

import com.cookingapp.domain.entity.Review;
import com.cookingapp.presentation.dto.response.ReviewResponse;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ReviewMapper
 * Review エンティティと DTO の変換を担当
 */
public class ReviewMapper {

    private ReviewMapper() {
        // ユーティリティクラスのため、インスタンス化を防ぐ
    }

    /**
     * Review エンティティから ReviewResponse に変換
     */
    public static ReviewResponse toResponse(Review review) {
        return ReviewResponse.from(review);
    }

    /**
     * Review エンティティのリストから ReviewResponse のリストに変換
     */
    public static List<ReviewResponse> toResponseList(List<Review> reviews) {
        return reviews.stream()
                .map(ReviewMapper::toResponse)
                .collect(Collectors.toList());
    }
}
