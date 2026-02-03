import React, { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useNavigate, useParams } from 'react-router-dom';
import { useReview } from '../../hooks/useReview';
import { useAuthStore } from '../../store/authStore';
import { useRecipeStore } from '../../store/recipeStore';
import { Review } from '../../types/review';
import { formatUnit } from '../../utils/unitHelper';
import { getYouTubeEmbedUrl } from '../../utils/videoHelper';
import { Button } from '../ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '../ui/card';
import { ReviewForm } from './ReviewForm';
import { ReviewList } from './ReviewList';

/**
 * レシピ詳細ページ
 */
const RecipeDetailPage: React.FC = () => {
  const { t } = useTranslation();
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { currentRecipe, loading, error, fetchRecipe, deleteRecipe, clearCurrentRecipe } =
    useRecipeStore();
  const user = useAuthStore((state) => state.user);
  const cognitoSub = useAuthStore((state) => state.cognitoSub);
  const {
    reviews,
    loading: reviewLoading,
    fetchReviews,
    createReview,
    updateReview,
    deleteReview: deleteReviewAction,
    reportReview,
  } = useReview();
  const [editingReview, setEditingReview] = useState<Review | null>(null);
  const [showReviewForm, setShowReviewForm] = useState(false);

  useEffect(() => {
    if (id) {
      fetchRecipe(id);
      fetchReviews(id);
    }
    return () => {
      clearCurrentRecipe();
    };
  }, [id, fetchRecipe, clearCurrentRecipe]);

  const handleEdit = () => {
    navigate(`/recipes/${id}/edit`);
  };

  const handleDelete = async () => {
    if (!id || !user) return;
    if (window.confirm(t('recipe.delete.confirm'))) {
      await deleteRecipe(id);
      navigate('/recipes');
    }
  };

  const handleBack = () => {
    navigate('/recipes');
  };

  if (loading) {
    return <div className="container mx-auto px-4 py-8 text-center">{t('common.loading')}</div>;
  }

  if (error) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
          {error}
        </div>
        <Button onClick={handleBack} className="mt-4">
          {t('common.back')}
        </Button>
      </div>
    );
  }

  if (!currentRecipe) {
    return <div className="container mx-auto px-4 py-8 text-center">{t('recipe.notFound')}</div>;
  }

  const isAuthor = cognitoSub && currentRecipe.authorId === cognitoSub;

  const handleReviewSubmit = async (rating: number, comment: string) => {
    if (!id || !user) return;

    try {
      if (editingReview) {
        await updateReview(editingReview.reviewId, { rating, comment });
        setEditingReview(null);
      } else {
        await createReview(id, { rating, comment });
        setShowReviewForm(false);
      }
    } catch (err) {
      // Review submit failed
    }
  };

  const handleReviewUpdate = async (reviewId: string, rating: number, comment: string) => {
    if (!user) return;
    await updateReview(reviewId, { rating, comment });
  };

  const handleReviewDelete = async (reviewId: string) => {
    if (!user) return;
    if (window.confirm(t('review.deleteConfirm'))) {
      await deleteReviewAction(reviewId);
    }
  };

  const handleReviewReport = async (reviewId: string) => {
    if (window.confirm(t('review.reportConfirm'))) {
      await reportReview(reviewId);
      alert(t('review.reportSuccess'));
    }
  };

  const handleCancelReview = () => {
    setEditingReview(null);
    setShowReviewForm(false);
  };

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="flex justify-between items-center mb-6">
        <Button variant="outline" onClick={handleBack}>
          {t('common.back')}
        </Button>
        {isAuthor && (
          <div className="flex gap-2">
            <Button onClick={handleEdit}>{t('recipe.edit.button')}</Button>
            <Button variant="destructive" onClick={handleDelete}>
              {t('recipe.delete.button')}
            </Button>
          </div>
        )}
      </div>

      {currentRecipe.imageUrl && (
        <img
          src={currentRecipe.imageUrl}
          alt={currentRecipe.title}
          className="w-full h-64 object-cover rounded-lg mb-6"
        />
      )}

      <h1 className="text-3xl font-bold mb-4">{currentRecipe.title}</h1>
      <p className="text-gray-600 mb-6">
        {t('recipe.cookingTime')}: {currentRecipe.cookingTime} {t('recipe.minutes')}
      </p>

      <Card className="mb-6">
        <CardHeader>
          <CardTitle>{t('recipe.ingredients')}</CardTitle>
        </CardHeader>
        <CardContent>
          <ul className="space-y-2">
            {currentRecipe.ingredients.map((ingredient, index) => (
              <li key={index} className="flex items-center">
                <span className="font-medium">{ingredient.name}</span>
                {(ingredient.quantity || ingredient.unit) && (
                  <>
                    <span className="mx-2">-</span>
                    <span>
                      {ingredient.quantity} {ingredient.unit ? formatUnit(ingredient.unit, t) : ''}
                    </span>
                  </>
                )}
                {ingredient.optional && (
                  <span className="ml-2 text-sm text-gray-500">({t('recipe.optional')})</span>
                )}
                {ingredient.note && (
                  <span className="ml-2 text-sm text-gray-600">({ingredient.note})</span>
                )}
              </li>
            ))}
          </ul>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>{t('recipe.steps')}</CardTitle>
        </CardHeader>
        <CardContent>
          <ol className="space-y-4">
            {currentRecipe.steps.map((step, index) => {
              const embedUrl = step.videoUrl ? getYouTubeEmbedUrl(step.videoUrl) : null;
              return (
                <li key={index} className="border-b pb-4 last:border-b-0">
                  <div className="flex gap-4 items-start">
                    {/* 左側: テキスト (75%) */}
                    <div className="w-3/4">
                      <div className="flex">
                        <span className="font-bold mr-3">{index + 1}.</span>
                        <span>{step.description}</span>
                      </div>
                      {/* 動画埋め込み */}
                      {embedUrl && (
                        <div className="mt-3 ml-6">
                          <iframe
                            width="100%"
                            height="315"
                            src={embedUrl}
                            title={`${t('recipe.step')} ${index + 1} ${t('recipe.video')}`}
                            frameBorder="0"
                            allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                            allowFullScreen
                            className="rounded-lg max-w-lg"
                          />
                        </div>
                      )}
                    </div>
                    {/* 右側: 画像 (25%) */}
                    {step.imageUrl && (
                      <div className="w-1/4">
                        <img
                          src={step.imageUrl}
                          alt={`${t('recipe.step')} ${index + 1}`}
                          className="w-full rounded-lg"
                        />
                      </div>
                    )}
                  </div>
                </li>
              );
            })}
          </ol>
        </CardContent>
      </Card>

      {/* レビューセクション */}
      <Card className="mt-6">
        <CardHeader>
          <div className="flex justify-between items-center">
            <CardTitle>{t('review.title')}</CardTitle>
            {user && !showReviewForm && (
              <Button onClick={() => setShowReviewForm(true)}>{t('review.submit')}</Button>
            )}
          </div>
        </CardHeader>
        <CardContent>
          {showReviewForm && (
            <div className="mb-6">
              <ReviewForm
                editingReview={editingReview}
                onSubmit={handleReviewSubmit}
                onCancel={handleCancelReview}
                loading={reviewLoading}
              />
            </div>
          )}
          <ReviewList
            reviews={reviews}
            currentUserId={user?.userId}
            onDelete={handleReviewDelete}
            onReport={handleReviewReport}
            onUpdate={handleReviewUpdate}
            loading={reviewLoading}
          />
        </CardContent>
      </Card>
    </div>
  );
};

export default RecipeDetailPage;
