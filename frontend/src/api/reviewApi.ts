/**
 * Review API
 * レビューAPI呼び出し関数
 */

import { CreateReviewRequest, Review, UpdateReviewRequest } from '../types/review';
import { apiDelete, apiGet, apiPost, apiPut } from '../utils/apiClient';

/**
 * レシピのレビュー一覧を取得
 */
export const getReviewsByRecipe = async (recipeId: string): Promise<Review[]> => {
  return apiGet<Review[]>(`/api/recipes/${recipeId}/reviews`);
};

/**
 * レビューを作成
 */
export const createReview = async (
  recipeId: string,
  request: CreateReviewRequest
): Promise<Review> => {
  return apiPost<Review>(`/api/recipes/${recipeId}/reviews`, request);
};

/**
 * レビューを更新
 */
export const updateReview = async (
  reviewId: string,
  request: UpdateReviewRequest
): Promise<Review> => {
  return apiPut<Review>(`/api/reviews/${reviewId}`, request);
};

/**
 * レビューを削除
 */
export const deleteReview = async (reviewId: string): Promise<void> => {
  return apiDelete<void>(`/api/reviews/${reviewId}`);
};

/**
 * レビューを通報
 */
export const reportReview = async (reviewId: string): Promise<Review> => {
  return apiPost<Review>(`/api/reviews/${reviewId}/report`, {});
};
