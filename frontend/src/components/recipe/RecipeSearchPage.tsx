import { Loader2, Plus, Search, UtensilsCrossed, X } from 'lucide-react';
import React, { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router-dom';
import { searchRecipesByIngredients } from '../../api/recipeApi';
import { useScrollToMessage } from '../../hooks/useScrollToMessage';
import { useRecipeStore } from '../../store/recipeStore';
import { RecipeSummary } from '../../types/recipe';
import { MessageDisplay } from '../common/MessageDisplay';
import { Badge } from '../ui/badge';
import { Button } from '../ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../ui/card';
import { Input } from '../ui/input';

type SearchMode = 'keyword' | 'ingredients';

/**
 * レシピ検索ページ（タブ切り替え式）
 */
const RecipeSearchPage: React.FC = () => {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { messageRef, scrollToMessage } = useScrollToMessage();
  const { recipes, loading: keywordLoading, error, searchRecipes } = useRecipeStore();

  // 検索モード
  const [searchMode, setSearchMode] = useState<SearchMode>('keyword');

  // キーワード検索用
  const [keyword, setKeyword] = useState('');

  // 食材検索用
  const [inputValue, setInputValue] = useState('');
  const [ingredients, setIngredients] = useState<string[]>([]);
  const [ingredientResults, setIngredientResults] = useState<RecipeSummary[]>([]);
  const [ingredientLoading, setIngredientLoading] = useState(false);
  const [ingredientError, setIngredientError] = useState<string | null>(null);
  const [warning, setWarning] = useState<string | null>(null);
  const [hasSearchedIngredients, setHasSearchedIngredients] = useState(false);

  // エラー発生時にスクロール
  useEffect(() => {
    if (error || ingredientError) {
      scrollToMessage();
    }
  }, [error, ingredientError, scrollToMessage]);

  // 初回ロード時にキーワード検索を実行
  useEffect(() => {
    searchRecipes({});
  }, [searchRecipes]);

  // キーワード検索
  const handleKeywordSearch = (e: React.FormEvent) => {
    e.preventDefault();
    searchRecipes({ keyword });
  };

  // 食材追加
  const handleAddIngredient = () => {
    const trimmed = inputValue.trim();
    if (!trimmed) return;

    if (ingredients.includes(trimmed)) {
      setWarning(t('recipe.searchByIngredients.duplicateIngredient', { ingredient: trimmed }));
      setTimeout(() => setWarning(null), 3000);
      return;
    }

    setIngredients([...ingredients, trimmed]);
    setInputValue('');
    setWarning(null);
  };

  const handleIngredientKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter') {
      e.preventDefault();
      handleAddIngredient();
    }
  };

  const handleRemoveIngredient = (ingredient: string) => {
    setIngredients(ingredients.filter((i) => i !== ingredient));
  };

  const handleClearIngredients = () => {
    setIngredients([]);
    setIngredientResults([]);
    setHasSearchedIngredients(false);
    setIngredientError(null);
  };

  // 食材検索
  const handleIngredientSearch = async () => {
    if (ingredients.length === 0) {
      setIngredientError(t('recipe.searchByIngredients.noIngredients'));
      return;
    }

    setIngredientLoading(true);
    setIngredientError(null);
    setHasSearchedIngredients(true);

    try {
      const response = await searchRecipesByIngredients(ingredients);
      setIngredientResults(response.recipes);
    } catch (err) {
      setIngredientError(err instanceof Error ? err.message : t('error.unknown'));
    } finally {
      setIngredientLoading(false);
    }
  };

  const handleRecipeClick = (recipeId: string) => {
    navigate(`/recipes/${recipeId}`);
  };

  const handleCreateRecipe = () => {
    navigate('/recipes/new');
  };

  return (
    <div className="container mx-auto px-4 py-8">
      {/* ヘッダー */}
      <div className="flex justify-between items-center mb-6">
        <div className="flex items-center space-x-3">
          <Search size={32} className="text-green-600" />
          <h1 className="text-3xl font-bold">{t('recipe.search.title')}</h1>
        </div>
        <Button onClick={handleCreateRecipe} className="flex items-center space-x-2">
          <Plus size={18} />
          <span>{t('recipe.create.button')}</span>
        </Button>
      </div>

      {/* タブ切り替え */}
      <div className="flex border-b border-gray-200 mb-6">
        <button
          onClick={() => setSearchMode('keyword')}
          className={`px-6 py-3 font-medium text-sm transition-colors ${
            searchMode === 'keyword'
              ? 'border-b-2 border-green-600 text-green-600'
              : 'text-gray-500 hover:text-gray-700'
          }`}
        >
          <div className="flex items-center gap-2">
            <Search size={18} />
            {t('recipe.search.tabKeyword')}
          </div>
        </button>
        <button
          onClick={() => setSearchMode('ingredients')}
          className={`px-6 py-3 font-medium text-sm transition-colors ${
            searchMode === 'ingredients'
              ? 'border-b-2 border-orange-500 text-orange-500'
              : 'text-gray-500 hover:text-gray-700'
          }`}
        >
          <div className="flex items-center gap-2">
            <UtensilsCrossed size={18} />
            {t('recipe.search.tabIngredients')}
          </div>
        </button>
      </div>

      {/* キーワード検索モード */}
      {searchMode === 'keyword' && (
        <>
          <form onSubmit={handleKeywordSearch} className="mb-6">
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

          <MessageDisplay ref={messageRef} error={error} />

          {keywordLoading ? (
            <div className="text-center py-8">{t('common.loading')}</div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
              {recipes.map((recipe) => (
                <Card
                  key={recipe.recipeId}
                  className="cursor-pointer hover:shadow-lg transition-shadow"
                  onClick={() => handleRecipeClick(recipe.recipeId)}
                >
                  {recipe.imageUrl ? (
                    <img
                      src={recipe.imageUrl}
                      alt={recipe.title}
                      className="w-full h-48 object-cover rounded-t-lg"
                    />
                  ) : (
                    <div className="w-full h-48 bg-gray-100 rounded-t-lg flex items-center justify-center">
                      <UtensilsCrossed size={48} className="text-gray-300" />
                    </div>
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

          {!keywordLoading && recipes.length === 0 && (
            <div className="text-center py-8 text-gray-500">{t('recipe.search.noResults')}</div>
          )}
        </>
      )}

      {/* 食材検索モード */}
      {searchMode === 'ingredients' && (
        <>
          <p className="text-gray-600 mb-4">{t('recipe.searchByIngredients.subtitle')}</p>

          {/* 食材入力エリア */}
          <div className="bg-white rounded-lg shadow-md p-6 mb-6">
            <div className="flex gap-2 mb-4">
              <Input
                type="text"
                placeholder={t('recipe.searchByIngredients.inputPlaceholder')}
                value={inputValue}
                onChange={(e) => setInputValue(e.target.value)}
                onKeyDown={handleIngredientKeyDown}
                className="flex-1"
              />
              <Button onClick={handleAddIngredient} variant="outline">
                {t('recipe.searchByIngredients.addButton')}
              </Button>
            </div>

            {/* 食材タグ */}
            {ingredients.length > 0 && (
              <div className="flex flex-wrap gap-2 mb-4">
                {ingredients.map((ingredient) => (
                  <Badge
                    key={ingredient}
                    variant="secondary"
                    className="px-3 py-1.5 text-sm flex items-center gap-1"
                  >
                    {ingredient}
                    <button
                      onClick={() => handleRemoveIngredient(ingredient)}
                      className="ml-1 hover:text-red-500 transition-colors"
                      aria-label={`Remove ${ingredient}`}
                    >
                      <X size={14} />
                    </button>
                  </Badge>
                ))}
              </div>
            )}

            {/* 検索ボタン */}
            <div className="flex gap-2">
              <Button
                onClick={handleIngredientSearch}
                disabled={ingredientLoading || ingredients.length === 0}
                className="flex items-center gap-2"
              >
                {ingredientLoading ? (
                  <Loader2 size={18} className="animate-spin" />
                ) : (
                  <Search size={18} />
                )}
                {t('recipe.searchByIngredients.searchButton')}
              </Button>
              {ingredients.length > 0 && (
                <Button variant="ghost" onClick={handleClearIngredients}>
                  {t('recipe.searchByIngredients.clearAll')}
                </Button>
              )}
            </div>
          </div>

          {/* 警告表示 */}
          {warning && (
            <div className="bg-yellow-50 border border-yellow-200 text-yellow-700 px-4 py-3 rounded-lg mb-6">
              {warning}
            </div>
          )}

          {/* エラー表示 */}
          {ingredientError && (
            <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg mb-6">
              {ingredientError}
            </div>
          )}

          {/* ローディング */}
          {ingredientLoading && (
            <div className="flex justify-center items-center py-12">
              <Loader2 size={48} className="animate-spin text-orange-500" />
            </div>
          )}

          {/* 検索結果 */}
          {!ingredientLoading && hasSearchedIngredients && (
            <>
              {ingredientResults.length > 0 ? (
                <>
                  <div className="mb-4">
                    <h2 className="text-xl font-semibold">
                      {t('recipe.searchByIngredients.resultsTitle', {
                        count: ingredientResults.length,
                      })}
                    </h2>
                    <p className="text-sm text-gray-500">
                      {t('recipe.searchByIngredients.searchedIngredients')}:{' '}
                      {ingredients.join(', ')}
                    </p>
                  </div>
                  <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                    {ingredientResults.map((recipe) => (
                      <Card
                        key={recipe.recipeId}
                        className="cursor-pointer hover:shadow-lg transition-shadow"
                        onClick={() => handleRecipeClick(recipe.recipeId)}
                      >
                        {recipe.imageUrl ? (
                          <img
                            src={recipe.imageUrl}
                            alt={recipe.title}
                            className="w-full h-48 object-cover rounded-t-lg"
                          />
                        ) : (
                          <div className="w-full h-48 bg-gray-100 rounded-t-lg flex items-center justify-center">
                            <UtensilsCrossed size={48} className="text-gray-300" />
                          </div>
                        )}
                        <CardHeader>
                          <CardTitle className="text-lg">{recipe.title}</CardTitle>
                        </CardHeader>
                        <CardContent>
                          <Button variant="outline" size="sm" className="w-full">
                            {t('admin.recipes.view')}
                          </Button>
                        </CardContent>
                      </Card>
                    ))}
                  </div>
                </>
              ) : (
                <div className="text-center py-12">
                  <UtensilsCrossed size={64} className="mx-auto text-gray-300 mb-4" />
                  <p className="text-xl text-gray-500">
                    {t('recipe.searchByIngredients.noResults')}
                  </p>
                </div>
              )}
            </>
          )}
        </>
      )}
    </div>
  );
};

export default RecipeSearchPage;
