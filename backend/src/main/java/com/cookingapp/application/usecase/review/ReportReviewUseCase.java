package com.cookingapp.application.usecase.review;

import com.cookingapp.domain.entity.Review;
import com.cookingapp.domain.exception.ReviewNotFoundException;
import com.cookingapp.domain.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * ReportReviewUseCase
 * レビュー通報ユースケース
 */
@Service
@RequiredArgsConstructor
public class ReportReviewUseCase {
    private static final Logger log = LoggerFactory.getLogger(ReportReviewUseCase.class);
    private final ReviewRepository reviewRepository;

    public Review execute(String reviewId) {
        log.info("Reporting review: reviewId={}", reviewId);
        
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("Review not found: " + reviewId));

        review.incrementReportCount();
        Review updatedReview = reviewRepository.save(review);
        
        if (updatedReview.shouldHide()) {
            log.warn("Review auto-hidden due to reports: reviewId={}, reportCount={}", 
                    reviewId, updatedReview.getReportedCount());
        }
        
        log.info("Review reported successfully: reviewId={}, reportCount={}", 
                reviewId, updatedReview.getReportedCount());
        return updatedReview;
    }
}
