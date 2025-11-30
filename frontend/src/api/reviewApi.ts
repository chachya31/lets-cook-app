/**
 * Review API
 * レビューAPI呼び出し関数
 */

import { Review, CreateReviewRequest, UpdateReviewRequest } from '../types/review';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

/**
 * レシピのレビュー一覧を取得
 */
export const getReviewsByRecipe = async (recipeId: string): Promise<Review[]> => {
  const response = await fetch(`${API_BASE_URL}/api/recipes/${recipeId}/reviews`, {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
    },
  });

  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message || 'Failed to fetch reviews');
  }

  return response.json();
};

/**
 * レビューを作成
 */
export const createReview = async (
  recipeId: string,
  userId: string,
  request: CreateReviewRequest
): Promise<Review> => {
  const response = await fetch(`${API_BASE_URL}/api/recipes/${recipeId}/reviews`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'X-User-Id': userId,
    },
    body: JSON.stringify(request),
  });

  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message || 'Failed to create review');
  }

  return response.json();
};

/**
 * レビューを更新
 */
export const updateReview = async (
  reviewId: string,
  userId: string,
  request: UpdateReviewRequest
): Promise<Review> => {
  const response = await fetch(`${API_BASE_URL}/api/reviews/${reviewId}`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
      'X-User-Id': userId,
    },
    body: JSON.stringify(request),
  });

  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message || 'Failed to update review');
  }

  return response.json();
};

/**
 * レビューを削除
 */
export const deleteReview = async (reviewId: string, userId: string): Promise<void> => {
  const response = await fetch(`${API_BASE_URL}/api/reviews/${reviewId}`, {
    method: 'DELETE',
    headers: {
      'Content-Type': 'application/json',
      'X-User-Id': userId,
    },
  });

  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message || 'Failed to delete review');
  }
};

/**
 * レビューを通報
 */
export const reportReview = async (reviewId: string): Promise<Review> => {
  const response = await fetch(`${API_BASE_URL}/api/reviews/${reviewId}/report`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
  });

  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message || 'Failed to report review');
  }

  return response.json();
};
