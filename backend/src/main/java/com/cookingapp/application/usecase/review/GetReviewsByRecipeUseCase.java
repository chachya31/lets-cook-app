package com.cookingapp.application.usecase.review;

import com.cookingapp.domain.entity.Review;
import com.cookingapp.domain.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * GetReviewsByRecipeUseCase
 * レシピIDでレビュー一覧を取得するユースケース
 */
@Service
@RequiredArgsConstructor
public class GetReviewsByRecipeUseCase {
    private static final Logger log = LoggerFactory.getLogger(GetReviewsByRecipeUseCase.class);
    private final ReviewRepository reviewRepository;

    public List<Review> execute(String recipeId) {
        log.info("Getting reviews for recipe: recipeId={}", recipeId);
        
        List<Review> reviews = reviewRepository.findByRecipeId(recipeId);
        
        // 表示可能なレビューのみをフィルタリング
        List<Review> visibleReviews = reviews.stream()
                .filter(Review::isVisible)
                .collect(Collectors.toList());
        
        log.info("Found {} visible reviews for recipe: recipeId={}", visibleReviews.size(), recipeId);
        return visibleReviews;
    }
}
