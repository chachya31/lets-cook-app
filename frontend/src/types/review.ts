/**
 * Review Types
 * レビュー型定義
 */

export interface Review {
  reviewId: string;
  recipeId: string;
  userId: string;
  rating: number;
  comment?: string;
  status: 'visible' | 'hidden';
  reportedCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface CreateReviewRequest {
  rating: number;
  comment?: string;
}

export interface UpdateReviewRequest {
  rating: number;
  comment?: string;
}

export interface ReviewState {
  reviews: Review[];
  loading: boolean;
  error: string | null;
}
