package com.cookingapp.presentation.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cookingapp.application.usecase.review.CreateReviewUseCase;
import com.cookingapp.application.usecase.review.DeleteReviewUseCase;
import com.cookingapp.application.usecase.review.GetReviewsByRecipeUseCase;
import com.cookingapp.application.usecase.review.ReportReviewUseCase;
import com.cookingapp.application.usecase.review.UpdateReviewUseCase;
import com.cookingapp.domain.entity.Review;
import com.cookingapp.infrastructure.security.SecurityUtils;
import com.cookingapp.presentation.dto.request.CreateReviewRequest;
import com.cookingapp.presentation.dto.request.UpdateReviewRequest;
import com.cookingapp.presentation.dto.response.ReviewResponse;
import com.cookingapp.presentation.mapper.ReviewMapper;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * ReviewController
 * レビュー管理REST APIコントローラー
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReviewController {
    private static final Logger log = LoggerFactory.getLogger(ReviewController.class);
    private final CreateReviewUseCase createReviewUseCase;
    private final UpdateReviewUseCase updateReviewUseCase;
    private final DeleteReviewUseCase deleteReviewUseCase;
    private final GetReviewsByRecipeUseCase getReviewsByRecipeUseCase;
    private final ReportReviewUseCase reportReviewUseCase;

    /**
     * レシピのレビュー一覧を取得
     */
    @GetMapping("/recipes/{recipeId}/reviews")
    public ResponseEntity<List<ReviewResponse>> getReviewsByRecipe(@PathVariable("recipeId") String recipeId) {
        log.info("GET /api/recipes/{}/reviews", recipeId);

        List<Review> reviews = getReviewsByRecipeUseCase.execute(recipeId);

        return ResponseEntity.ok(ReviewMapper.toResponseList(reviews));
    }

    /**
     * レビューを作成
     */
    @PostMapping("/recipes/{recipeId}/reviews")
    public ResponseEntity<ReviewResponse> createReview(
            @PathVariable("recipeId") String recipeId,
            @Valid @RequestBody CreateReviewRequest request) {
        String userId = SecurityUtils.getCurrentUserId();
        log.info("POST /api/recipes/{}/reviews by userId={}", recipeId, userId);

        Review review = createReviewUseCase.execute(
                recipeId,
                userId,
                request.getRating(),
                request.getComment());

        return ResponseEntity.status(HttpStatus.CREATED).body(ReviewMapper.toResponse(review));
    }

    /**
     * レビューを更新
     */
    @PutMapping("/reviews/{reviewId}")
    public ResponseEntity<ReviewResponse> updateReview(
            @PathVariable("reviewId") String reviewId,
            @Valid @RequestBody UpdateReviewRequest request) {
        String userId = SecurityUtils.getCurrentUserId();
        log.info("PUT /api/reviews/{} by userId={}", reviewId, userId);

        Review review = updateReviewUseCase.execute(
                reviewId,
                userId,
                request.getRating(),
                request.getComment());

        return ResponseEntity.ok(ReviewMapper.toResponse(review));
    }

    /**
     * レビューを削除
     */
    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable("reviewId") String reviewId) {
        String userId = SecurityUtils.getCurrentUserId();
        log.info("DELETE /api/reviews/{} by userId={}", reviewId, userId);

        deleteReviewUseCase.execute(reviewId, userId);

        return ResponseEntity.noContent().build();
    }

    /**
     * レビューを通報
     */
    @PostMapping("/reviews/{reviewId}/report")
    public ResponseEntity<ReviewResponse> reportReview(@PathVariable("reviewId") String reviewId) {
        log.info("POST /api/reviews/{}/report", reviewId);

        Review review = reportReviewUseCase.execute(reviewId);

        return ResponseEntity.ok(ReviewMapper.toResponse(review));
    }
}
