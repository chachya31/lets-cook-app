package com.cookingapp.presentation.controller;

import com.cookingapp.application.usecase.review.*;
import com.cookingapp.domain.entity.Review;
import com.cookingapp.presentation.dto.request.CreateReviewRequest;
import com.cookingapp.presentation.dto.request.UpdateReviewRequest;
import com.cookingapp.presentation.dto.response.ReviewResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ReviewController
 * レビュー管琁EEST APIコントローラー
 */
@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReviewController {
    private final CreateReviewUseCase createReviewUseCase;
    private final UpdateReviewUseCase updateReviewUseCase;
    private final DeleteReviewUseCase deleteReviewUseCase;
    private final GetReviewsByRecipeUseCase getReviewsByRecipeUseCase;
    private final ReportReviewUseCase reportReviewUseCase;

    /**
     * レシピ�Eレビュー一覧を取征E
     */
    @GetMapping("/recipes/{recipeId}/reviews")
    public ResponseEntity<List<ReviewResponse>> getReviewsByRecipe(@PathVariable String recipeId) {
        log.info("GET /api/recipes/{}/reviews", recipeId);
        
        List<Review> reviews = getReviewsByRecipeUseCase.execute(recipeId);
        List<ReviewResponse> response = reviews.stream()
                .map(ReviewResponse::from)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    /**
     * レビューを作�E
     */
    @PostMapping("/recipes/{recipeId}/reviews")
    public ResponseEntity<ReviewResponse> createReview(
            @PathVariable String recipeId,
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody CreateReviewRequest request) {
        log.info("POST /api/recipes/{}/reviews by userId={}", recipeId, userId);
        
        Review review = createReviewUseCase.execute(
                recipeId,
                userId,
                request.getRating(),
                request.getComment()
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(ReviewResponse.from(review));
    }

    /**
     * レビューを更新
     */
    @PutMapping("/reviews/{reviewId}")
    public ResponseEntity<ReviewResponse> updateReview(
            @PathVariable String reviewId,
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody UpdateReviewRequest request) {
        log.info("PUT /api/reviews/{} by userId={}", reviewId, userId);
        
        Review review = updateReviewUseCase.execute(
                reviewId,
                userId,
                request.getRating(),
                request.getComment()
        );
        
        return ResponseEntity.ok(ReviewResponse.from(review));
    }

    /**
     * レビューを削除
     */
    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable String reviewId,
            @RequestHeader("X-User-Id") String userId) {
        log.info("DELETE /api/reviews/{} by userId={}", reviewId, userId);
        
        deleteReviewUseCase.execute(reviewId, userId);
        
        return ResponseEntity.noContent().build();
    }

    /**
     * レビューを通報
     */
    @PostMapping("/reviews/{reviewId}/report")
    public ResponseEntity<ReviewResponse> reportReview(@PathVariable String reviewId) {
        log.info("POST /api/reviews/{}/report", reviewId);
        
        Review review = reportReviewUseCase.execute(reviewId);
        
        return ResponseEntity.ok(ReviewResponse.from(review));
    }
}
