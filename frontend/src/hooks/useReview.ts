/**
 * useReview Hook
 * レビュー管理カスタムフック
 */

import { useDispatch, useSelector } from 'react-redux';
import { AppDispatch, RootState } from '../store/store';
import {
  fetchReviewsByRecipe,
  createReview as createReviewAction,
  updateReview as updateReviewAction,
  deleteReview as deleteReviewAction,
  reportReview as reportReviewAction,
  clearReviews,
  clearError,
} from '../store/slices/reviewSlice';
import { CreateReviewRequest, UpdateReviewRequest } from '../types/review';

export const useReview = () => {
  const dispatch = useDispatch<AppDispatch>();
  const { reviews, loading, error } = useSelector((state: RootState) => state.review);

  const fetchReviews = async (recipeId: string) => {
    await dispatch(fetchReviewsByRecipe(recipeId));
  };

  const createReview = async (
    recipeId: string,
    userId: string,
    request: CreateReviewRequest
  ) => {
    await dispatch(createReviewAction({ recipeId, userId, request }));
  };

  const updateReview = async (
    reviewId: string,
    userId: string,
    request: UpdateReviewRequest
  ) => {
    await dispatch(updateReviewAction({ reviewId, userId, request }));
  };

  const deleteReview = async (reviewId: string, userId: string) => {
    await dispatch(deleteReviewAction({ reviewId, userId }));
  };

  const reportReview = async (reviewId: string) => {
    await dispatch(reportReviewAction(reviewId));
  };

  const clear = () => {
    dispatch(clearReviews());
  };

  const clearErrorMessage = () => {
    dispatch(clearError());
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
