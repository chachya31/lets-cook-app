package com.cookingapp.application.usecase;

import com.cookingapp.domain.entity.Review;
import com.cookingapp.domain.exception.ReviewNotFoundException;
import com.cookingapp.domain.exception.UnauthorizedException;
import com.cookingapp.domain.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * DeleteReviewUseCase
 * レビュー削除ユースケース
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeleteReviewUseCase {
    private final ReviewRepository reviewRepository;

    public void execute(String reviewId, String userId) {
        log.info("Deleting review: reviewId={}, userId={}", reviewId, userId);
        
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("Review not found: " + reviewId));

        if (!review.canEdit(userId)) {
            throw new UnauthorizedException("User is not authorized to delete this review");
        }

        reviewRepository.delete(reviewId);
        log.info("Review deleted successfully: reviewId={}", reviewId);
    }
}
