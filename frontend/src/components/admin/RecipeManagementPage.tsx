import React, { useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router-dom';
import { useScrollToMessage } from '../../hooks/useScrollToMessage';
import { useAdminStore } from '../../store/adminStore';
import LoadingSkeleton from '../common/LoadingSkeleton';
import { MessageDisplay } from '../common/MessageDisplay';
import { Button } from '../ui/button';
import { Card } from '../ui/card';

/**
 * レシピ管理ページ
 */
const RecipeManagementPage: React.FC = () => {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { messageRef, scrollToMessage } = useScrollToMessage();
  const { recipes, loading, error, fetchAllRecipes, setRecipeStatus, deleteRecipe } =
    useAdminStore();

  useEffect(() => {
    fetchAllRecipes();
  }, [fetchAllRecipes]);

  // エラー発生時にスクロール
  useEffect(() => {
    if (error) {
      scrollToMessage();
    }
  }, [error, scrollToMessage]);

  const handleToggleStatus = async (recipeId: string, currentStatus: boolean) => {
    try {
      await setRecipeStatus(recipeId, { isPublic: !currentStatus });
    } catch {
      // エラーはstoreで管理
    }
  };

  const handleDeleteRecipe = async (recipeId: string) => {
    if (!window.confirm(t('admin.recipes.confirm.delete'))) {
      return;
    }

    try {
      await deleteRecipe(recipeId);
    } catch {
      // エラーはstoreで管理
    }
  };

  if (loading && recipes.length === 0) {
    return (
      <div className="container mx-auto px-4 py-8">
        <LoadingSkeleton type="list" />
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-3xl font-bold mb-8">{t('admin.recipes.title')}</h1>

      <MessageDisplay ref={messageRef} error={error} />

      {/* レシピ一覧 */}
      {recipes.length === 0 ? (
        <Card className="p-6">
          <p className="text-gray-500 text-center">{t('admin.recipes.noRecipes')}</p>
        </Card>
      ) : (
        <div className="space-y-4">
          {recipes.map((recipe) => (
            <Card key={recipe.recipeId} className="p-6">
              <div className="flex items-start justify-between">
                <div className="flex-1">
                  <h2 className="text-xl font-semibold mb-2">{recipe.title}</h2>
                  <div className="text-sm text-gray-600 space-y-1">
                    <p>
                      <strong>{t('admin.recipes.recipeId')}:</strong> {recipe.recipeId}
                    </p>
                    <p>
                      <strong>{t('admin.recipes.authorId')}:</strong> {recipe.authorId}
                    </p>
                    <p>
                      <strong>{t('admin.recipes.cookingTime')}:</strong> {recipe.cookingTime}{' '}
                      {t('recipe.minutes')}
                    </p>
                    <p>
                      <strong>{t('admin.recipes.status')}:</strong>{' '}
                      <span
                        className={
                          recipe.isPublic
                            ? 'text-green-600 font-semibold'
                            : 'text-red-600 font-semibold'
                        }
                      >
                        {recipe.isPublic ? t('admin.recipes.public') : t('admin.recipes.private')}
                      </span>
                    </p>
                    {recipe.isDeleted && (
                      <p className="text-red-600 font-semibold">{t('admin.recipes.deleted')}</p>
                    )}
                  </div>
                </div>

                <div className="flex flex-col gap-2 ml-4">
                  <Button
                    onClick={() => navigate(`/recipes/${recipe.recipeId}`)}
                    variant="outline"
                    size="sm"
                  >
                    {t('admin.recipes.view')}
                  </Button>

                  <Button
                    onClick={() => handleToggleStatus(recipe.recipeId, recipe.isPublic)}
                    disabled={loading || recipe.isDeleted}
                    variant={recipe.isPublic ? 'destructive' : 'default'}
                    size="sm"
                  >
                    {recipe.isPublic ? t('admin.recipes.setPrivate') : t('admin.recipes.setPublic')}
                  </Button>

                  <Button
                    onClick={() => handleDeleteRecipe(recipe.recipeId)}
                    disabled={loading || recipe.isDeleted}
                    variant="destructive"
                    size="sm"
                  >
                    {t('admin.recipes.delete')}
                  </Button>
                </div>
              </div>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
};

export default RecipeManagementPage;
