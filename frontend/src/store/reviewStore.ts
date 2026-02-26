/**
 * Review Store (Zustand)
 * レビュー状態管理
 */

import { create } from 'zustand';
import * as reviewApi from '../api/reviewApi';
import { CreateReviewRequest, Review, UpdateReviewRequest } from '../types/review';

interface ReviewState {
  reviews: Review[];
  loading: boolean;
  error: string | null;
}

interface ReviewActions {
  fetchReviews: (recipeId: string) => Promise<void>;
  createReview: (recipeId: string, request: CreateReviewRequest) => Promise<void>;
  updateReview: (reviewId: string, request: UpdateReviewRequest) => Promise<void>;
  deleteReview: (reviewId: string) => Promise<void>;
  reportReview: (reviewId: string) => Promise<void>;
  clearReviews: () => void;
  clearError: () => void;
}

type ReviewStore = ReviewState & ReviewActions;

export const useReviewStore = create<ReviewStore>((set) => ({
  // 初期状態
  reviews: [],
  loading: false,
  error: null,

  // レシピのレビュー一覧を取得
  fetchReviews: async (recipeId: string) => {
    set({ loading: true, error: null });
    try {
      const reviews = await reviewApi.getReviewsByRecipe(recipeId);
      set({ reviews, loading: false });
    } catch (error) {
      set({
        loading: false,
        error: (error as Error).message || 'Failed to fetch reviews',
      });
    }
  },

  // レビューを作成
  createReview: async (recipeId: string, request: CreateReviewRequest) => {
    set({ loading: true, error: null });
    try {
      const review = await reviewApi.createReview(recipeId, request);
      set((state) => ({
        reviews: [...state.reviews, review],
        loading: false,
      }));
    } catch (error) {
      set({
        loading: false,
        error: (error as Error).message || 'Failed to create review',
      });
      throw error;
    }
  },

  // レビューを更新
  updateReview: async (reviewId: string, request: UpdateReviewRequest) => {
    set({ loading: true, error: null });
    try {
      const updatedReview = await reviewApi.updateReview(reviewId, request);
      set((state) => ({
        reviews: state.reviews.map((r) => (r.reviewId === reviewId ? updatedReview : r)),
        loading: false,
      }));
    } catch (error) {
      set({
        loading: false,
        error: (error as Error).message || 'Failed to update review',
      });
      throw error;
    }
  },

  // レビューを削除
  deleteReview: async (reviewId: string) => {
    set({ loading: true, error: null });
    try {
      await reviewApi.deleteReview(reviewId);
      set((state) => ({
        reviews: state.reviews.filter((r) => r.reviewId !== reviewId),
        loading: false,
      }));
    } catch (error) {
      set({
        loading: false,
        error: (error as Error).message || 'Failed to delete review',
      });
      throw error;
    }
  },

  // レビューを通報
  reportReview: async (reviewId: string) => {
    set({ loading: true, error: null });
    try {
      const reportedReview = await reviewApi.reportReview(reviewId);
      set((state) => ({
        reviews: state.reviews.map((r) => (r.reviewId === reviewId ? reportedReview : r)),
        loading: false,
      }));
    } catch (error) {
      set({
        loading: false,
        error: (error as Error).message || 'Failed to report review',
      });
      throw error;
    }
  },

  // レビューをクリア
  clearReviews: () => {
    set({ reviews: [], error: null });
  },

  // エラーをクリア
  clearError: () => {
    set({ error: null });
  },
}));
