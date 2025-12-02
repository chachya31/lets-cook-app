import React, { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router-dom';
import { AppDispatch, RootState } from '../../store/store';
import { fetchAdminDashboardStats } from '../../store/slices/adminSlice';
import { Card } from '../ui/card';
import { Button } from '../ui/button';
import LoadingSkeleton from '../common/LoadingSkeleton';

/**
 * 管理者ダッシュボードページ
 */
const AdminDashboardPage: React.FC = () => {
  const { t } = useTranslation();
  const dispatch = useDispatch<AppDispatch>();
  const navigate = useNavigate();
  const { stats, loading, error } = useSelector((state: RootState) => state.admin);

  useEffect(() => {
    dispatch(fetchAdminDashboardStats());
  }, [dispatch]);

  if (loading) {
    return (
      <div className="container mx-auto px-4 py-8">
        <LoadingSkeleton type="card" />
      </div>
    );
  }

  if (error) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="text-red-500">{error}</div>
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-3xl font-bold mb-8">{t('admin.dashboard.title')}</h1>

      {/* 統計カード */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-8">
        <Card className="p-6">
          <h2 className="text-xl font-semibold mb-2">{t('admin.dashboard.totalUsers')}</h2>
          <p className="text-4xl font-bold text-green-600">{stats?.totalUsers || 0}</p>
        </Card>

        <Card className="p-6">
          <h2 className="text-xl font-semibold mb-2">{t('admin.dashboard.totalRecipes')}</h2>
          <p className="text-4xl font-bold text-green-600">{stats?.totalRecipes || 0}</p>
        </Card>
      </div>

      {/* メッセージ */}
      {stats?.message && (
        <Card className="p-6 mb-8">
          <p className="text-gray-700">{stats.message}</p>
        </Card>
      )}

      {/* 管理機能へのリンク */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <Card className="p-6">
          <h2 className="text-xl font-semibold mb-4">{t('admin.dashboard.userManagement')}</h2>
          <p className="text-gray-600 mb-4">{t('admin.dashboard.userManagementDesc')}</p>
          <Button onClick={() => navigate('/admin/users')} className="w-full">
            {t('admin.dashboard.goToUserManagement')}
          </Button>
        </Card>

        <Card className="p-6">
          <h2 className="text-xl font-semibold mb-4">{t('admin.dashboard.recipeManagement')}</h2>
          <p className="text-gray-600 mb-4">{t('admin.dashboard.recipeManagementDesc')}</p>
          <Button onClick={() => navigate('/admin/recipes')} className="w-full">
            {t('admin.dashboard.goToRecipeManagement')}
          </Button>
        </Card>
      </div>
    </div>
  );
};

export default AdminDashboardPage;
