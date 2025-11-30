/**
 * ReviewForm Component
 * レビュー投稿フォームコンポーネント
 */

import React, { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { Review } from '../../types/review';
import { Button } from '../ui/button';
import { Textarea } from '../ui/textarea';
import { Label } from '../ui/label';

interface ReviewFormProps {
  editingReview?: Review | null;
  onSubmit: (rating: number, comment: string) => void;
  onCancel: () => void;
  loading?: boolean;
}

export const ReviewForm: React.FC<ReviewFormProps> = ({
  editingReview,
  onSubmit,
  onCancel,
  loading = false,
}) => {
  const { t } = useTranslation();
  const [rating, setRating] = useState(5);
  const [comment, setComment] = useState('');
  const [hoveredRating, setHoveredRating] = useState(0);
  const [errors, setErrors] = useState<{ rating?: string; comment?: string }>({});

  useEffect(() => {
    if (editingReview) {
      setRating(editingReview.rating);
      setComment(editingReview.comment || '');
    } else {
      setRating(5);
      setComment('');
    }
    setErrors({});
  }, [editingReview]);

  const validate = () => {
    const newErrors: { rating?: string; comment?: string } = {};

    if (rating < 1 || rating > 5) {
      newErrors.rating = t('review.validation.ratingRange');
    }

    if (comment && comment.length > 300) {
      newErrors.comment = t('review.validation.commentMaxLength');
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (validate()) {
      onSubmit(rating, comment);
    }
  };

  const renderStars = () => {
    return (
      <div className="flex gap-2">
        {[1, 2, 3, 4, 5].map((star) => (
          <button
            key={star}
            type="button"
            className="text-3xl focus:outline-none transition-colors"
            onMouseEnter={() => setHoveredRating(star)}
            onMouseLeave={() => setHoveredRating(0)}
            onClick={() => setRating(star)}
          >
            <span
              className={
                star <= (hoveredRating || rating)
                  ? 'text-yellow-400'
                  : 'text-gray-300'
              }
            >
              ★
            </span>
          </button>
        ))}
      </div>
    );
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div>
        <Label htmlFor="rating">{t('review.rating')}</Label>
        {renderStars()}
        {errors.rating && (
          <p className="text-sm text-red-500 mt-1">{errors.rating}</p>
        )}
      </div>

      <div>
        <Label htmlFor="comment">{t('review.comment')}</Label>
        <Textarea
          id="comment"
          value={comment}
          onChange={(e) => setComment(e.target.value)}
          placeholder={t('review.commentPlaceholder')}
          rows={4}
          maxLength={300}
        />
        <p className="text-sm text-gray-500 mt-1">
          {comment.length} / 300
        </p>
        {errors.comment && (
          <p className="text-sm text-red-500 mt-1">{errors.comment}</p>
        )}
      </div>

      <div className="flex gap-2">
        <Button type="submit" disabled={loading}>
          {loading
            ? t('common.loading')
            : editingReview
            ? t('review.update')
            : t('review.submit')}
        </Button>
        {editingReview && (
          <Button type="button" variant="outline" onClick={onCancel}>
            {t('common.cancel')}
          </Button>
        )}
      </div>
    </form>
  );
};
