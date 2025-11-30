package com.cookingapp.application.usecase.review;

import com.cookingapp.domain.entity.Review;
import com.cookingapp.domain.exception.ReviewNotFoundException;
import com.cookingapp.domain.exception.UnauthorizedException;
import com.cookingapp.domain.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * UpdateReviewUseCase
 * レビュー更新ユースケース
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateReviewUseCase {
    private final ReviewRepository reviewRepository;

    public Review execute(String reviewId, String userId, int rating, String comment) {
        log.info("Updating review: reviewId={}, userId={}", reviewId, userId);
        
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("Review not found: " + reviewId));

        if (!review.canEdit(userId)) {
            throw new UnauthorizedException("User is not authorized to edit this review");
        }

        review.update(rating, comment);
        Review updatedReview = reviewRepository.save(review);
        
        log.info("Review updated successfully: reviewId={}", reviewId);
        return updatedReview;
    }
}
