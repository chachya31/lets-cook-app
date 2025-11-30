/**
 * ReviewList Component
 * レビュー一覧コンポーネント
 */

import React from 'react';
import { useTranslation } from 'react-i18next';
import { Review } from '../../types/review';
import { Button } from '../ui/button';
import { Card } from '../ui/card';

interface ReviewListProps {
  reviews: Review[];
  currentUserId?: string;
  onEdit: (review: Review) => void;
  onDelete: (reviewId: string) => void;
  onReport: (reviewId: string) => void;
}

export const ReviewList: React.FC<ReviewListProps> = ({
  reviews,
  currentUserId,
  onEdit,
  onDelete,
  onReport,
}) => {
  const { t } = useTranslation();

  const renderStars = (rating: number) => {
    return (
      <div className="flex gap-1">
        {[1, 2, 3, 4, 5].map((star) => (
          <span
            key={star}
            className={star <= rating ? 'text-yellow-400' : 'text-gray-300'}
          >
            ★
          </span>
        ))}
      </div>
    );
  };

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString();
  };

  if (reviews.length === 0) {
    return (
      <div className="text-center py-8 text-gray-500">
        {t('review.noReviews')}
      </div>
    );
  }

  return (
    <div className="space-y-4">
      {reviews.map((review) => (
        <Card key={review.reviewId} className="p-4">
          <div className="flex justify-between items-start mb-2">
            <div>
              {renderStars(review.rating)}
              <p className="text-sm text-gray-500 mt-1">
                {formatDate(review.createdAt)}
              </p>
            </div>
            <div className="flex gap-2">
              {currentUserId === review.userId ? (
                <>
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => onEdit(review)}
                  >
                    {t('review.edit')}
                  </Button>
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => onDelete(review.reviewId)}
                  >
                    {t('review.delete')}
                  </Button>
                </>
              ) : (
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => onReport(review.reviewId)}
                >
                  {t('review.report')}
                </Button>
              )}
            </div>
          </div>
          {review.comment && (
            <p className="text-gray-700 mt-2">{review.comment}</p>
          )}
        </Card>
      ))}
    </div>
  );
};
