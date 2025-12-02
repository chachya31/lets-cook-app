package com.cookingapp.application.usecase.review;

import com.cookingapp.domain.entity.Review;
import com.cookingapp.domain.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * CreateReviewUseCase
 * レビュー作成ユースケース
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CreateReviewUseCase {
    private final ReviewRepository reviewRepository;

    public Review execute(String recipeId, String userId, int rating, String comment) {
        log.info("Creating review: recipeId={}, userId={}, rating={}", recipeId, userId, rating);
        
        Review review = Review.create(recipeId, userId, rating, comment);
        Review savedReview = reviewRepository.save(review);
        
        log.info("Review created successfully: reviewId={}", savedReview.getReviewId());
        return savedReview;
    }
}
