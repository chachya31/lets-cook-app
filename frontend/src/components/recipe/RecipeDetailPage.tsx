import React, { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useNavigate, useParams } from 'react-router-dom';
import { AppDispatch, RootState } from '../../store/store';
import { fetchRecipe, deleteRecipe, clearCurrentRecipe } from '../../store/recipeSlice';
import { useTranslation } from 'react-i18next';
import { Button } from '../ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '../ui/card';

/**
 * レシピ詳細ページ
 */
const RecipeDetailPage: React.FC = () => {
  const { t } = useTranslation();
  const { id } = useParams<{ id: string }>();
  const dispatch = useDispatch<AppDispatch>();
  const navigate = useNavigate();
  const { currentRecipe, loading, error } = useSelector((state: RootState) => state.recipe);
  const { user } = useSelector((state: RootState) => state.auth);

  useEffect(() => {
    if (id) {
      dispatch(fetchRecipe(id));
    }
    return () => {
      dispatch(clearCurrentRecipe());
    };
  }, [dispatch, id]);

  const handleEdit = () => {
    navigate(`/recipes/${id}/edit`);
  };

  const handleDelete = async () => {
    if (!id || !user) return;
    if (window.confirm(t('recipe.delete.confirm'))) {
      await dispatch(deleteRecipe({ recipeId: id, userId: user.userId }));
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
    return (
      <div className="container mx-auto px-4 py-8 text-center">
        {t('recipe.notFound')}
      </div>
    );
  }

  const isAuthor = user && currentRecipe.authorId === user.userId;

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
                <span className="mx-2">-</span>
                <span>
                  {ingredient.quantity} {ingredient.unit}
                </span>
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
            {currentRecipe.steps.map((step, index) => (
              <li key={index} className="flex">
                <span className="font-bold mr-3">{index + 1}.</span>
                <span>{step}</span>
              </li>
            ))}
          </ol>
        </CardContent>
      </Card>
    </div>
  );
};

export default RecipeDetailPage;
