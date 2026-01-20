import { Search } from 'lucide-react';
import React, { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useDispatch, useSelector } from 'react-redux';
import { useAuthStore } from '../../store/authStore';
import { searchRecipes } from '../../store/recipeSlice';
import { AppDispatch, RootState } from '../../store/store';
import { Recipe } from '../../types/recipe';
import { Button } from '../ui/button';
import { Card } from '../ui/card';
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '../ui/dialog';
import { Input } from '../ui/input';

interface RecipeSelectModalProps {
  open: boolean;
  onClose: () => void;
  onSelect: (recipe: Recipe) => void;
}

/**
 * レシピ選択モーダル
 */
const RecipeSelectModal: React.FC<RecipeSelectModalProps> = ({ open, onClose, onSelect }) => {
  const { t } = useTranslation();
  const dispatch = useDispatch<AppDispatch>();
  const { recipes, loading } = useSelector((state: RootState) => state.recipe);
  const user = useAuthStore((state) => state.user);

  const [searchKeyword, setSearchKeyword] = useState('');
  const [filteredRecipes, setFilteredRecipes] = useState<Recipe[]>([]);

  // モーダルが開いたときにレシピを取得
  useEffect(() => {
    if (open && user?.userId) {
      dispatch(searchRecipes({ authorId: user.userId }));
    }
  }, [open, user, dispatch]);

  // レシピが更新されたらフィルタリング
  useEffect(() => {
    if (searchKeyword.trim() === '') {
      setFilteredRecipes(recipes);
    } else {
      const keyword = searchKeyword.toLowerCase();
      setFilteredRecipes(recipes.filter((recipe) => recipe.title.toLowerCase().includes(keyword)));
    }
  }, [recipes, searchKeyword]);

  const handleSearch = (e: React.ChangeEvent<HTMLInputElement>) => {
    setSearchKeyword(e.target.value);
  };

  const handleSelect = (recipe: Recipe) => {
    onSelect(recipe);
    onClose();
    setSearchKeyword('');
  };

  return (
    <Dialog open={open} onOpenChange={(isOpen) => !isOpen && onClose()}>
      <DialogContent className="max-w-2xl max-h-[80vh] overflow-hidden flex flex-col">
        <DialogHeader>
          <DialogTitle>{t('schedule.selectRecipe')}</DialogTitle>
        </DialogHeader>

        {/* 検索バー */}
        <div className="relative mb-4">
          <Search
            className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400"
            size={20}
          />
          <Input
            type="text"
            placeholder={t('schedule.searchRecipePlaceholder')}
            value={searchKeyword}
            onChange={handleSearch}
            className="pl-10"
          />
        </div>

        {/* レシピ一覧 */}
        <div className="flex-1 overflow-y-auto space-y-2">
          {loading && <p className="text-center py-4">{t('common.loading')}</p>}

          {!loading && filteredRecipes.length === 0 && (
            <p className="text-center py-4 text-gray-500">{t('schedule.noRecipesFound')}</p>
          )}

          {filteredRecipes.map((recipe) => (
            <Card
              key={recipe.recipeId}
              className="p-3 cursor-pointer hover:bg-gray-50 transition-colors"
              onClick={() => handleSelect(recipe)}
            >
              <div className="flex items-center gap-3">
                {recipe.imageUrl ? (
                  <img
                    src={recipe.imageUrl}
                    alt={recipe.title}
                    className="w-16 h-16 object-cover rounded"
                  />
                ) : (
                  <div className="w-16 h-16 bg-gray-200 rounded flex items-center justify-center">
                    <span className="text-gray-400 text-xs">{t('recipe.noImage')}</span>
                  </div>
                )}
                <div className="flex-1">
                  <h3 className="font-medium">{recipe.title}</h3>
                  <p className="text-sm text-gray-500">
                    {t('recipe.cookingTime')}: {recipe.cookingTime}
                    {t('recipe.minutes')}
                  </p>
                </div>
                <Button variant="outline" size="sm">
                  {t('common.select')}
                </Button>
              </div>
            </Card>
          ))}
        </div>
      </DialogContent>
    </Dialog>
  );
};

export default RecipeSelectModal;
