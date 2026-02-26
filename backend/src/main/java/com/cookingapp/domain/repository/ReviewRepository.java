package com.cookingapp.domain.repository;

import com.cookingapp.domain.entity.Review;

import java.util.List;
import java.util.Optional;

/**
 * ReviewRepository Interface
 * レビューリポジトリインターフェース
 */
public interface ReviewRepository {
    /**
     * レビューを保存
     */
    Review save(Review review);

    /**
     * レビューIDでレビューを取得
     */
    Optional<Review> findById(String reviewId);

    /**
     * レシピIDでレビュー一覧を取得
     */
    List<Review> findByRecipeId(String recipeId);

    /**
     * ユーザーIDでレビュー一覧を取得
     */
    List<Review> findByUserId(String userId);

    /**
     * レビューを削除
     */
    void delete(String reviewId);

    /**
     * レビューが存在するかチェック
     */
    boolean existsById(String reviewId);
}
