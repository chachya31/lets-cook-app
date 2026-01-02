import React, { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router-dom';
import { searchRecipes } from '../../api/recipeApi';
import { getSchedules } from '../../api/scheduleApi';
import { getShoppingList } from '../../api/shoppingListApi';
import { Recipe } from '../../types/recipe';
import { Schedule } from '../../types/schedule';
import { ShoppingListItem } from '../../types/shoppingList';
import LoadingSkeleton from '../common/LoadingSkeleton';
import { Button } from '../ui/button';
import { Card } from '../ui/card';

/**
 * ダッシュボードページコンポーネント
 * ホーム画面として、最近のレシピ、スケジュール概要、買い物リスト概要を表示
 */
const DashboardPage: React.FC = () => {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const [recentRecipes, setRecentRecipes] = useState<Recipe[]>([]);
  const [upcomingSchedules, setUpcomingSchedules] = useState<Schedule[]>([]);
  const [shoppingItems, setShoppingItems] = useState<ShoppingListItem[]>([]);
  const [loading, setLoading] = useState(true);

  // ユーザーIDを取得（実際の実装では認証状態から取得）
  const userId = localStorage.getItem('userId') || '';

  useEffect(() => {
    const fetchDashboardData = async () => {
      try {
        setLoading(true);

        // 最近のレシピを取得（最大3件）
        const recipes = await searchRecipes();
        setRecentRecipes(recipes.slice(0, 3));

        // 今日から7日間のスケジュールを取得
        if (userId) {
          const today = new Date();
          const nextWeek = new Date();
          nextWeek.setDate(today.getDate() + 7);

          const schedules = await getSchedules(userId, {
            startDate: today.toISOString().split('T')[0],
            endDate: nextWeek.toISOString().split('T')[0],
          });
          setUpcomingSchedules(schedules.slice(0, 5));

          // 買い物リストを取得（未チェックのみ、最大5件）
          const items = await getShoppingList(userId);
          const uncheckedItems = items.filter((item) => !item.isChecked);
          setShoppingItems(uncheckedItems.slice(0, 5));
        }
      } catch (error) {
        console.error('Failed to fetch dashboard data:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchDashboardData();
  }, [userId]);

  if (loading) {
    return (
      <div className="container mx-auto p-6">
        <h1 className="text-3xl font-bold mb-6">{t('dashboard.title')}</h1>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          <Card className="p-6">
            <LoadingSkeleton type="list" lines={3} />
          </Card>
          <Card className="p-6">
            <LoadingSkeleton type="list" lines={3} />
          </Card>
          <Card className="p-6">
            <LoadingSkeleton type="list" lines={3} />
          </Card>
        </div>
      </div>
    );
  }

  return (
    <div className="container mx-auto p-6">
      <h1 className="text-3xl font-bold mb-6">{t('dashboard.title')}</h1>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {/* 最近のレシピセクション */}
        <Card className="p-6">
          <div className="flex justify-between items-center mb-4">
            <h2 className="text-xl font-semibold">{t('dashboard.recentRecipes')}</h2>
            <Button variant="ghost" size="sm" onClick={() => navigate('/recipes')}>
              {t('dashboard.viewAll')}
            </Button>
          </div>
          {recentRecipes.length === 0 ? (
            <p className="text-gray-500">{t('dashboard.noRecentRecipes')}</p>
          ) : (
            <ul className="space-y-3">
              {recentRecipes.map((recipe) => (
                <li
                  key={recipe.recipeId}
                  className="cursor-pointer hover:bg-gray-50 p-2 rounded"
                  onClick={() => navigate(`/recipes/${recipe.recipeId}`)}
                >
                  <div className="font-medium">{recipe.title}</div>
                  <div className="text-sm text-gray-500">
                    {t('recipe.cookingTime')}: {recipe.cookingTime} {t('recipe.minutes')}
                  </div>
                </li>
              ))}
            </ul>
          )}
          <Button className="w-full mt-4" onClick={() => navigate('/recipes/new')}>
            {t('recipe.create.button')}
          </Button>
        </Card>

        {/* スケジュール概要セクション */}
        <Card className="p-6">
          <div className="flex justify-between items-center mb-4">
            <h2 className="text-xl font-semibold">{t('dashboard.upcomingSchedules')}</h2>
            <Button variant="ghost" size="sm" onClick={() => navigate('/schedules')}>
              {t('dashboard.viewAll')}
            </Button>
          </div>
          {upcomingSchedules.length === 0 ? (
            <p className="text-gray-500">{t('dashboard.noUpcomingSchedules')}</p>
          ) : (
            <ul className="space-y-3">
              {upcomingSchedules.map((schedule) => (
                <li key={schedule.scheduleId} className="p-2 border-l-4 border-green-500">
                  <div className="font-medium">{schedule.recipeTitle}</div>
                  <div className="text-sm text-gray-500">
                    {schedule.date} - {t(schedule.isDone ? 'schedule.cooked' : 'schedule.planned')}
                  </div>
                </li>
              ))}
            </ul>
          )}
          <Button className="w-full mt-4" onClick={() => navigate('/schedules')}>
            {t('dashboard.manageSchedules')}
          </Button>
        </Card>

        {/* 買い物リスト概要セクション */}
        <Card className="p-6">
          <div className="flex justify-between items-center mb-4">
            <h2 className="text-xl font-semibold">{t('dashboard.shoppingList')}</h2>
            <Button variant="ghost" size="sm" onClick={() => navigate('/shopping-list')}>
              {t('dashboard.viewAll')}
            </Button>
          </div>
          {shoppingItems.length === 0 ? (
            <p className="text-gray-500">{t('dashboard.noShoppingItems')}</p>
          ) : (
            <ul className="space-y-2">
              {shoppingItems.map((item) => (
                <li key={item.itemId} className="flex items-center">
                  <span className="w-2 h-2 bg-green-500 rounded-full mr-2"></span>
                  <span>
                    {item.name} - {item.quantity} {item.unit}
                  </span>
                </li>
              ))}
            </ul>
          )}
          <Button className="w-full mt-4" onClick={() => navigate('/shopping-list')}>
            {t('dashboard.manageShoppingList')}
          </Button>
        </Card>
      </div>

      {/* AIチャット・食材在庫セクション */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mt-6">
        {/* AIチャットセクション */}
        <Card className="p-6">
          <div className="flex justify-between items-center mb-4">
            <h2 className="text-xl font-semibold">{t('dashboard.aiChat')}</h2>
          </div>
          <p className="text-gray-500 mb-4">{t('dashboard.aiChatDescription')}</p>
          <Button className="w-full" onClick={() => navigate('/chat')}>
            {t('dashboard.startChat')}
          </Button>
        </Card>

        {/* 食材在庫セクション */}
        <Card className="p-6">
          <div className="flex justify-between items-center mb-4">
            <h2 className="text-xl font-semibold">{t('dashboard.inventory')}</h2>
          </div>
          <p className="text-gray-500 mb-4">{t('dashboard.inventoryDescription')}</p>
          <Button className="w-full" onClick={() => navigate('/inventory')}>
            {t('dashboard.manageInventory')}
          </Button>
        </Card>
      </div>
    </div>
  );
};

export default DashboardPage;
