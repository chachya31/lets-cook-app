import React from 'react';
import { useTranslation } from 'react-i18next';
import { useSelector } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import { RootState } from '../../store/store';
import { Button } from '../ui/button';

/**
 * ヘッダーコンポーネント
 * アプリケーション全体のナビゲーションバー
 */
const Header: React.FC = () => {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const currentUser = useSelector((state: RootState) => state.auth.user);
  const isLoggedIn = !!currentUser;
  const isAdmin = currentUser?.roles?.includes('Admins') ?? false;

  const handleLogout = () => {
    localStorage.removeItem('userId');
    localStorage.removeItem('accessToken');
    navigate('/login');
  };

  return (
    <header className="bg-white border-b border-gray-200 sticky top-0 z-50">
      <div className="container mx-auto px-4">
        <div className="flex items-center justify-between h-16">
          {/* ロゴ */}
          <div className="flex items-center space-x-3 cursor-pointer" onClick={() => navigate('/')}>
            <img src="/logo.svg" alt="Logo" className="h-20 w-20" />
            <span className="text-xl font-bold text-orange-500">{t('app.title')}</span>
          </div>

          {/* ナビゲーションメニュー */}
          <nav className="hidden md:flex items-center space-x-6">
            <button
              onClick={() => navigate('/recipes')}
              className="text-gray-700 hover:text-green-600 transition-colors"
            >
              {t('recipe.search.title')}
            </button>
            <button
              onClick={() => navigate('/schedules')}
              className="text-gray-700 hover:text-green-600 transition-colors"
            >
              {t('schedule.title')}
            </button>
            <button
              onClick={() => navigate('/shopping-list')}
              className="text-gray-700 hover:text-green-600 transition-colors"
            >
              {t('shoppingList.title')}
            </button>
            {isAdmin && (
              <button
                onClick={() => navigate('/admin')}
                className="text-gray-700 hover:text-green-600 transition-colors"
              >
                {t('admin.dashboard.title')}
              </button>
            )}
          </nav>

          {/* ユーザーアクション */}
          <div className="flex items-center space-x-4">
            {isLoggedIn ? (
              <>
                <Button variant="outline" size="sm" onClick={() => navigate('/recipes/new')}>
                  {t('recipe.create.button')}
                </Button>
                <Button variant="ghost" size="sm" onClick={() => navigate('/profile/edit')}>
                  {t('profile.title')}
                </Button>
                <Button variant="ghost" size="sm" onClick={handleLogout}>
                  {t('auth.logout')}
                </Button>
              </>
            ) : (
              <>
                <Button variant="ghost" size="sm" onClick={() => navigate('/login')}>
                  {t('auth.login')}
                </Button>
                <Button variant="default" size="sm" onClick={() => navigate('/register')}>
                  {t('auth.register')}
                </Button>
              </>
            )}
          </div>
        </div>
      </div>
    </header>
  );
};

export default Header;
