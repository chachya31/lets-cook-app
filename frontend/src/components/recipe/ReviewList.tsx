/**
 * ReviewList Component
 * レビュー一覧コンポーネント
 */

import React, { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Review } from '../../types/review';
import { Button } from '../ui/button';
import { Card } from '../ui/card';
import { Label } from '../ui/label';
import { Textarea } from '../ui/textarea';

interface ReviewListProps {
  reviews: Review[];
  currentUserId?: string;
  onDelete: (reviewId: string) => void;
  onReport: (reviewId: string) => void;
  onUpdate?: (reviewId: string, rating: number, comment: string) => Promise<void>;
  loading?: boolean;
}

export const ReviewList: React.FC<ReviewListProps> = ({
  reviews,
  currentUserId,
  onDelete,
  onReport,
  onUpdate,
  loading = false,
}) => {
  const { t } = useTranslation();
  const [editingReviewId, setEditingReviewId] = useState<string | null>(null);
  const [editRating, setEditRating] = useState(5);
  const [editComment, setEditComment] = useState('');
  const [hoveredRating, setHoveredRating] = useState(0);

  const renderStars = (rating: number) => {
    return (
      <div className="flex gap-1">
        {[1, 2, 3, 4, 5].map((star) => (
          <span key={star} className={star <= rating ? 'text-yellow-400' : 'text-gray-300'}>
            ★
          </span>
        ))}
      </div>
    );
  };

  const renderEditableStars = () => {
    return (
      <div className="flex gap-2">
        {[1, 2, 3, 4, 5].map((star) => (
          <button
            key={star}
            type="button"
            className="text-3xl focus:outline-none transition-colors"
            onMouseEnter={() => setHoveredRating(star)}
            onMouseLeave={() => setHoveredRating(0)}
            onClick={() => setEditRating(star)}
          >
            <span
              className={
                star <= (hoveredRating || editRating) ? 'text-yellow-400' : 'text-gray-300'
              }
            >
              ★
            </span>
          </button>
        ))}
      </div>
    );
  };

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString();
  };

  const handleStartEdit = (review: Review) => {
    setEditingReviewId(review.reviewId);
    setEditRating(review.rating);
    setEditComment(review.comment || '');
    setHoveredRating(0);
  };

  const handleCancelEdit = () => {
    setEditingReviewId(null);
    setEditRating(5);
    setEditComment('');
    setHoveredRating(0);
  };

  const handleSaveEdit = async (reviewId: string) => {
    if (onUpdate) {
      await onUpdate(reviewId, editRating, editComment);
      handleCancelEdit();
    }
  };

  if (reviews.length === 0) {
    return <div className="text-center py-8 text-gray-500">{t('review.noReviews')}</div>;
  }

  return (
    <div className="space-y-4">
      {reviews.map((review) => (
        <Card key={review.reviewId} className="p-4">
          {editingReviewId === review.reviewId ? (
            // 編集モード
            <div className="space-y-4">
              <div>
                <Label>{t('review.rating')}</Label>
                {renderEditableStars()}
              </div>
              <div>
                <Label>{t('review.comment')}</Label>
                <Textarea
                  value={editComment}
                  onChange={(e) => setEditComment(e.target.value)}
                  placeholder={t('review.commentPlaceholder')}
                  rows={4}
                  maxLength={300}
                />
                <p className="text-sm text-gray-500 mt-1">{editComment.length} / 300</p>
              </div>
              <div className="flex gap-2">
                <Button
                  size="sm"
                  onClick={() => handleSaveEdit(review.reviewId)}
                  disabled={loading}
                >
                  {loading ? t('common.loading') : t('review.update')}
                </Button>
                <Button variant="outline" size="sm" onClick={handleCancelEdit}>
                  {t('common.cancel')}
                </Button>
              </div>
            </div>
          ) : (
            // 表示モード
            <>
              <div className="flex justify-between items-start mb-2">
                <div>
                  {renderStars(review.rating)}
                  <p className="text-sm text-gray-500 mt-1">{formatDate(review.createdAt)}</p>
                </div>
                <div className="flex gap-2">
                  {currentUserId === review.userId ? (
                    <>
                      <Button variant="outline" size="sm" onClick={() => handleStartEdit(review)}>
                        {t('review.edit')}
                      </Button>
                      <Button variant="outline" size="sm" onClick={() => onDelete(review.reviewId)}>
                        {t('review.delete')}
                      </Button>
                    </>
                  ) : (
                    <Button variant="outline" size="sm" onClick={() => onReport(review.reviewId)}>
                      {t('review.report')}
                    </Button>
                  )}
                </div>
              </div>
              {review.comment && <p className="text-gray-700 mt-2">{review.comment}</p>}
            </>
          )}
        </Card>
      ))}
    </div>
  );
};
