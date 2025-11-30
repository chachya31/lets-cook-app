/**
 * Review Slice
 * レビュー状態管理
 */

import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import { ReviewState, Review, CreateReviewRequest, UpdateReviewRequest } from '../../types/review';
import * as reviewApi from '../../api/reviewApi';

const initialState: ReviewState = {
  reviews: [],
  loading: false,
  error: null,
};

/**
 * レシピのレビュー一覧を取得
 */
export const fetchReviewsByRecipe = createAsyncThunk(
  'review/fetchByRecipe',
  async (recipeId: string) => {
    return await reviewApi.getReviewsByRecipe(recipeId);
  }
);

/**
 * レビューを作成
 */
export const createReview = createAsyncThunk(
  'review/create',
  async ({ recipeId, userId, request }: { recipeId: string; userId: string; request: CreateReviewRequest }) => {
    return await reviewApi.createReview(recipeId, userId, request);
  }
);

/**
 * レビューを更新
 */
export const updateReview = createAsyncThunk(
  'review/update',
  async ({ reviewId, userId, request }: { reviewId: string; userId: string; request: UpdateReviewRequest }) => {
    return await reviewApi.updateReview(reviewId, userId, request);
  }
);

/**
 * レビューを削除
 */
export const deleteReview = createAsyncThunk(
  'review/delete',
  async ({ reviewId, userId }: { reviewId: string; userId: string }) => {
    await reviewApi.deleteReview(reviewId, userId);
    return reviewId;
  }
);

/**
 * レビューを通報
 */
export const reportReview = createAsyncThunk(
  'review/report',
  async (reviewId: string) => {
    return await reviewApi.reportReview(reviewId);
  }
);

const reviewSlice = createSlice({
  name: 'review',
  initialState,
  reducers: {
    clearReviews: (state) => {
      state.reviews = [];
      state.error = null;
    },
    clearError: (state) => {
      state.error = null;
    },
  },
  extraReducers: (builder) => {
    // レビュー一覧取得
    builder
      .addCase(fetchReviewsByRecipe.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchReviewsByRecipe.fulfilled, (state, action: PayloadAction<Review[]>) => {
        state.loading = false;
        state.reviews = action.payload;
      })
      .addCase(fetchReviewsByRecipe.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to fetch reviews';
      });

    // レビュー作成
    builder
      .addCase(createReview.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(createReview.fulfilled, (state, action: PayloadAction<Review>) => {
        state.loading = false;
        state.reviews.push(action.payload);
      })
      .addCase(createReview.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to create review';
      });

    // レビュー更新
    builder
      .addCase(updateReview.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(updateReview.fulfilled, (state, action: PayloadAction<Review>) => {
        state.loading = false;
        const index = state.reviews.findIndex((r) => r.reviewId === action.payload.reviewId);
        if (index !== -1) {
          state.reviews[index] = action.payload;
        }
      })
      .addCase(updateReview.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to update review';
      });

    // レビュー削除
    builder
      .addCase(deleteReview.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(deleteReview.fulfilled, (state, action: PayloadAction<string>) => {
        state.loading = false;
        state.reviews = state.reviews.filter((r) => r.reviewId !== action.payload);
      })
      .addCase(deleteReview.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to delete review';
      });

    // レビュー通報
    builder
      .addCase(reportReview.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(reportReview.fulfilled, (state, action: PayloadAction<Review>) => {
        state.loading = false;
        const index = state.reviews.findIndex((r) => r.reviewId === action.payload.reviewId);
        if (index !== -1) {
          state.reviews[index] = action.payload;
        }
      })
      .addCase(reportReview.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to report review';
      });
  },
});

export const { clearReviews, clearError } = reviewSlice.actions;
export default reviewSlice.reducer;
