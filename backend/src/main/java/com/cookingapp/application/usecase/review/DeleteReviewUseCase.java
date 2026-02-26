package com.cookingapp.application.usecase.review;

import com.cookingapp.domain.entity.Review;
import com.cookingapp.domain.exception.ReviewNotFoundException;
import com.cookingapp.domain.exception.UnauthorizedException;
import com.cookingapp.domain.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * DeleteReviewUseCase
 * レビュー削除ユースケース
 */
@Service
@RequiredArgsConstructor
public class DeleteReviewUseCase {
    private static final Logger log = LoggerFactory.getLogger(DeleteReviewUseCase.class);
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
