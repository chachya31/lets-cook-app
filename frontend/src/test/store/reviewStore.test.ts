import { act } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import * as reviewApi from '../../api/reviewApi';
import { useReviewStore } from '../../store/reviewStore';

vi.mock('../../api/reviewApi');

describe('reviewStore', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    // ストアをリセット
    useReviewStore.setState({
      reviews: [],
      loading: false,
      error: null,
    });
  });

  const mockReview = {
    reviewId: 'review-1',
    recipeId: 'recipe-1',
    userId: 'user-1',
    rating: 5,
    comment: 'おいしかった！',
    status: 'visible' as const,
    reportedCount: 0,
    createdAt: '2025-01-01T10:00:00Z',
    updatedAt: '2025-01-01T10:00:00Z',
  };

  describe('fetchReviews', () => {
    it('should fetch reviews successfully', async () => {
      const mockReviews = [mockReview];
      vi.mocked(reviewApi.getReviewsByRecipe).mockResolvedValue(mockReviews);

      await act(async () => {
        await useReviewStore.getState().fetchReviews('recipe-1');
      });

      const state = useReviewStore.getState();
      expect(state.reviews).toEqual(mockReviews);
      expect(state.loading).toBe(false);
      expect(state.error).toBeNull();
    });

    it('should set error on fetch failure', async () => {
      vi.mocked(reviewApi.getReviewsByRecipe).mockRejectedValue(new Error('Failed to fetch'));

      await act(async () => {
        await useReviewStore.getState().fetchReviews('recipe-1');
      });

      const state = useReviewStore.getState();
      expect(state.reviews).toEqual([]);
      expect(state.loading).toBe(false);
      expect(state.error).toBe('Failed to fetch');
    });
  });

  describe('createReview', () => {
    it('should create review successfully', async () => {
      vi.mocked(reviewApi.createReview).mockResolvedValue(mockReview);

      await act(async () => {
        await useReviewStore.getState().createReview('recipe-1', {
          rating: 5,
          comment: 'おいしかった！',
        });
      });

      const state = useReviewStore.getState();
      expect(state.reviews).toContainEqual(mockReview);
      expect(state.loading).toBe(false);
    });

    it('should set error on create failure', async () => {
      vi.mocked(reviewApi.createReview).mockRejectedValue(new Error('Failed to create'));

      await expect(
        useReviewStore.getState().createReview('recipe-1', {
          rating: 5,
        })
      ).rejects.toThrow('Failed to create');

      const state = useReviewStore.getState();
      expect(state.error).toBe('Failed to create');
    });
  });

  describe('updateReview', () => {
    it('should update review successfully', async () => {
      // 初期状態にレビューを設定
      useReviewStore.setState({ reviews: [mockReview] });

      const updatedReview = { ...mockReview, rating: 4, comment: '更新しました' };
      vi.mocked(reviewApi.updateReview).mockResolvedValue(updatedReview);

      await act(async () => {
        await useReviewStore.getState().updateReview('review-1', {
          rating: 4,
          comment: '更新しました',
        });
      });

      const state = useReviewStore.getState();
      expect(state.reviews[0].rating).toBe(4);
      expect(state.reviews[0].comment).toBe('更新しました');
    });
  });

  describe('deleteReview', () => {
    it('should delete review successfully', async () => {
      useReviewStore.setState({ reviews: [mockReview] });
      vi.mocked(reviewApi.deleteReview).mockResolvedValue(undefined);

      await act(async () => {
        await useReviewStore.getState().deleteReview('review-1');
      });

      const state = useReviewStore.getState();
      expect(state.reviews).toHaveLength(0);
    });
  });

  describe('reportReview', () => {
    it('should report review successfully', async () => {
      useReviewStore.setState({ reviews: [mockReview] });
      const reportedReview = { ...mockReview, reportedCount: 1 };
      vi.mocked(reviewApi.reportReview).mockResolvedValue(reportedReview);

      await act(async () => {
        await useReviewStore.getState().reportReview('review-1');
      });

      const state = useReviewStore.getState();
      expect(state.reviews[0].reportedCount).toBe(1);
    });
  });

  describe('clearReviews', () => {
    it('should clear all reviews', () => {
      useReviewStore.setState({ reviews: [mockReview], error: 'some error' });

      act(() => {
        useReviewStore.getState().clearReviews();
      });

      const state = useReviewStore.getState();
      expect(state.reviews).toEqual([]);
      expect(state.error).toBeNull();
    });
  });

  describe('clearError', () => {
    it('should clear error', () => {
      useReviewStore.setState({ error: 'some error' });

      act(() => {
        useReviewStore.getState().clearError();
      });

      expect(useReviewStore.getState().error).toBeNull();
    });
  });
});
