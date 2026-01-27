/**
 * useReview Hook
 * レビュー管理カスタムフック（Zustand版）
 */

import { useReviewStore } from '../store/reviewStore';
import { CreateReviewRequest, UpdateReviewRequest } from '../types/review';

export const useReview = () => {
  const {
    reviews,
    loading,
    error,
    fetchReviews: fetchReviewsAction,
    createReview: createReviewAction,
    updateReview: updateReviewAction,
    deleteReview: deleteReviewAction,
    reportReview: reportReviewAction,
    clearReviews,
    clearError,
  } = useReviewStore();

  const fetchReviews = async (recipeId: string) => {
    await fetchReviewsAction(recipeId);
  };

  const createReview = async (recipeId: string, userId: string, request: CreateReviewRequest) => {
    await createReviewAction(recipeId, userId, request);
  };

  const updateReview = async (reviewId: string, userId: string, request: UpdateReviewRequest) => {
    await updateReviewAction(reviewId, userId, request);
  };

  const deleteReview = async (reviewId: string, userId: string) => {
    await deleteReviewAction(reviewId, userId);
  };

  const reportReview = async (reviewId: string) => {
    await reportReviewAction(reviewId);
  };

  const clear = () => {
    clearReviews();
  };

  const clearErrorMessage = () => {
    clearError();
  };

  return {
    reviews,
    loading,
    error,
    fetchReviews,
    createReview,
    updateReview,
    deleteReview,
    reportReview,
    clear,
    clearErrorMessage,
  };
};
