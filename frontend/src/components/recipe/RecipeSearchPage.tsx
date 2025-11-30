import React, { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import { AppDispatch, RootState } from '../../store/store';
import { searchRecipes } from '../../store/recipeSlice';
import { useTranslation } from 'react-i18next';
import { Button } from '../ui/button';
import { Input } from '../ui/input';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../ui/card';

/**
 * レシピ検索ページ
 */
const RecipeSearchPage: React.FC = () => {
  const { t } = useTranslation();
  const dispatch = useDispatch<AppDispatch>();
  const navigate = useNavigate();
  const { recipes, loading, error } = useSelector((state: RootState) => state.recipe);
  const [keyword, setKeyword] = useState('');

  useEffect(() => {
    dispatch(searchRecipes({}));
  }, [dispatch]);

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    dispatch(searchRecipes({ keyword }));
  };

  const handleRecipeClick = (recipeId: string) => {
    navigate(`/recipes/${recipeId}`);
  };

  const handleCreateRecipe = () => {
    navigate('/recipes/new');
  };

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold">{t('recipe.search.title')}</h1>
        <Button onClick={handleCreateRecipe}>{t('recipe.create.button')}</Button>
      </div>

      <form onSubmit={handleSearch} className="mb-6">
        <div className="flex gap-2">
          <Input
            type="text"
            placeholder={t('recipe.search.placeholder')}
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            className="flex-1"
          />
          <Button type="submit">{t('recipe.search.button')}</Button>
        </div>
      </form>

      {error && (
        <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
          {error}
        </div>
      )}

      {loading ? (
        <div className="text-center py-8">{t('common.loading')}</div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {recipes.map((recipe) => (
            <Card
              key={recipe.recipeId}
              className="cursor-pointer hover:shadow-lg transition-shadow"
              onClick={() => handleRecipeClick(recipe.recipeId)}
            >
              {recipe.imageUrl && (
                <img
                  src={recipe.imageUrl}
                  alt={recipe.title}
                  className="w-full h-48 object-cover rounded-t-lg"
                />
              )}
              <CardHeader>
                <CardTitle>{recipe.title}</CardTitle>
                <CardDescription>
                  {t('recipe.cookingTime')}: {recipe.cookingTime} {t('recipe.minutes')}
                </CardDescription>
              </CardHeader>
              <CardContent>
                <p className="text-sm text-gray-600">
                  {t('recipe.ingredients')}: {recipe.ingredients.length}
                </p>
              </CardContent>
            </Card>
          ))}
        </div>
      )}

      {!loading && recipes.length === 0 && (
        <div className="text-center py-8 text-gray-500">{t('recipe.search.noResults')}</div>
      )}
    </div>
  );
};

export default RecipeSearchPage;
