import {
    Bot,
    Calendar,
    LogOut,
    Search,
    Shield,
    ShoppingCart,
    User
} from 'lucide-react';
import React, { useEffect, useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useDispatch, useSelector } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import { logout } from '../../store/slices/authSlice';
import { AppDispatch, RootState } from '../../store/store';
import { Button } from '../ui/button';

/**
 * ヘッダーコンポーネント
 * アプリケーション全体のナビゲーションバー
 */
const Header: React.FC = () => {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const dispatch = useDispatch<AppDispatch>();
  const currentUser = useSelector((state: RootState) => state.auth.user);
  const isLoggedIn = !!currentUser;
  const isAdmin = currentUser?.roles?.includes('Admins') ?? false;
  const [isProfileMenuOpen, setIsProfileMenuOpen] = useState(false);
  const profileMenuRef = useRef<HTMLDivElement>(null);

  const handleLogout = () => {
    dispatch(logout());
    navigate('/login');
  };

  // メニュー外クリックで閉じる
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (profileMenuRef.current && !profileMenuRef.current.contains(event.target as Node)) {
        setIsProfileMenuOpen(false);
      }
    };

    if (isProfileMenuOpen) {
      document.addEventListener('mousedown', handleClickOutside);
    }

    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [isProfileMenuOpen]);

  return (
    <header className="bg-white border-b border-gray-200 sticky top-0 z-50">
      <div className="container mx-auto px-4">
        <div className="flex items-center justify-between h-16">
          {/* ロゴ */}
          <div className="flex items-center space-x-3 cursor-pointer" onClick={() => navigate('/')}>
            <img src="/logo.svg" alt="Logo" className="h-12 w-auto" />
            <span className="text-xl font-bold text-orange-500">{t('app.title')}</span>
          </div>

          {/* ナビゲーションメニュー */}
          <nav className="hidden md:flex items-center space-x-6">
            <button
              onClick={() => navigate('/recipes')}
              className="flex items-center space-x-1 text-gray-700 hover:text-green-600 transition-colors"
            >
              <Search size={18} />
              <span>{t('recipe.search.title')}</span>
            </button>
            <button
              onClick={() => navigate('/schedules')}
              className="flex items-center space-x-1 text-gray-700 hover:text-green-600 transition-colors"
            >
              <Calendar size={18} />
              <span>{t('schedule.title')}</span>
            </button>
            <button
              onClick={() => navigate('/shopping-list')}
              className="flex items-center space-x-1 text-gray-700 hover:text-green-600 transition-colors"
            >
              <ShoppingCart size={18} />
              <span>{t('shoppingList.title')}</span>
            </button>
            {isLoggedIn && (
              <button
                onClick={() => navigate('/chat')}
                className="flex items-center space-x-1 text-gray-700 hover:text-purple-600 transition-colors"
              >
                <Bot size={18} />
                <span>AI Chat</span>
              </button>
            )}
            {isAdmin && (
              <button
                onClick={() => navigate('/admin')}
                className="flex items-center space-x-1 text-gray-700 hover:text-green-600 transition-colors"
              >
                <Shield size={18} />
                <span>{t('admin.dashboard.title')}</span>
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
                {/* プロフィールメニュー */}
                <div className="relative" ref={profileMenuRef}>
                  <button
                    onClick={() => setIsProfileMenuOpen(!isProfileMenuOpen)}
                    className="flex items-center space-x-2 focus:outline-none"
                  >
                    {currentUser.profileImageUrl ? (
                      <img
                        src={currentUser.profileImageUrl}
                        alt={currentUser.nickname}
                        className="h-10 w-10 rounded-full object-cover border-2 border-gray-200 hover:border-orange-500 transition-colors"
                      />
                    ) : (
                      <div className="h-10 w-10 rounded-full bg-orange-500 flex items-center justify-center border-2 border-gray-200 hover:border-orange-600 transition-colors">
                        <span className="text-white font-semibold text-lg">
                          {currentUser.nickname.charAt(0).toUpperCase()}
                        </span>
                      </div>
                    )}
                  </button>
                  {isProfileMenuOpen && (
                    <div className="absolute right-0 mt-2 w-48 bg-white rounded-md shadow-lg py-1 z-50 border border-gray-200">
                      <div className="px-4 py-2 border-b border-gray-200">
                        <p className="text-sm font-medium text-gray-900">{currentUser.nickname}</p>
                        <p className="text-xs text-gray-500">{currentUser.email}</p>
                      </div>
                      <button
                        onClick={() => {
                          setIsProfileMenuOpen(false);
                          navigate('/profile/edit');
                        }}
                        className="flex items-center space-x-2 w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
                      >
                        <User size={16} />
                        <span>{t('profile.title')}</span>
                      </button>
                      <button
                        onClick={() => {
                          setIsProfileMenuOpen(false);
                          handleLogout();
                        }}
                        className="flex items-center space-x-2 w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
                      >
                        <LogOut size={16} />
                        <span>{t('auth.logout')}</span>
                      </button>
                    </div>
                  )}
                </div>
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
