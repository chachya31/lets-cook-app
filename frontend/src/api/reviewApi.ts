/**
 * Review API
 * レビューAPI呼び出し関数
 */

import { Review, CreateReviewRequest, UpdateReviewRequest } from '../types/review';
import { apiGet, apiPost, apiPut, apiDelete } from '../utils/apiClient';

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
  userId: string,
  request: CreateReviewRequest
): Promise<Review> => {
  return apiPost<Review>(`/api/recipes/${recipeId}/reviews`, request, userId);
};

/**
 * レビューを更新
 */
export const updateReview = async (
  reviewId: string,
  userId: string,
  request: UpdateReviewRequest
): Promise<Review> => {
  return apiPut<Review>(`/api/reviews/${reviewId}`, request, userId);
};

/**
 * レビューを削除
 */
export const deleteReview = async (reviewId: string, userId: string): Promise<void> => {
  return apiDelete<void>(`/api/reviews/${reviewId}`, userId);
};

/**
 * レビューを通報
 */
export const reportReview = async (reviewId: string): Promise<Review> => {
  return apiPost<Review>(`/api/reviews/${reviewId}/report`, {});
};
