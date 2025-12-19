import React, { useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { useSelector } from 'react-redux';
import { Route, Routes, useLocation } from 'react-router-dom';
import AdminDashboardPage from './components/admin/AdminDashboardPage';
import RecipeManagementPage from './components/admin/RecipeManagementPage';
import UserManagementPage from './components/admin/UserManagementPage';
import { AlertModal } from './components/alert/AlertModal';
import { ConfirmEmailPage } from './components/auth/ConfirmEmail/ConfirmEmailPage';
import { LoginPage } from './components/auth/Login/LoginPage';
import { PasswordResetPage } from './components/auth/PasswordReset/PasswordResetPage';
import { RegisterPage } from './components/auth/Register/RegisterPage';
import Footer from './components/common/Footer';
import Header from './components/common/Header';
import DashboardPage from './components/dashboard/DashboardPage';
import ProfileEditPage from './components/profile/ProfileEditPage';
import RecipeDetailPage from './components/recipe/RecipeDetailPage';
import RecipeEditPage from './components/recipe/RecipeEdit/RecipeEditPage';
import RecipeSearchPage from './components/recipe/RecipeSearchPage';
import SchedulePage from './components/schedule/SchedulePage';
import ShoppingListPage from './components/shopping/ShoppingListPage';
import { RootState } from './store/store';

const App: React.FC = () => {
  const location = useLocation();
  const { i18n } = useTranslation();
  const currentUser = useSelector((state: RootState) => state.auth.user);

  // ログインユーザーの優先言語に切り替え
  useEffect(() => {
    if (currentUser?.preferredLanguage && i18n.language !== currentUser.preferredLanguage) {
      i18n.changeLanguage(currentUser.preferredLanguage);
    }
  }, [currentUser, i18n]);

  // 認証ページではヘッダーとフッターを非表示
  const isAuthPage = ['/login', '/register', '/confirm-email', '/password-reset'].includes(
    location.pathname
  );

  return (
    <div className="flex flex-col min-h-screen">
      <AlertModal />
      {!isAuthPage && <Header />}
      <main className="flex-1">
        <Routes>
          <Route path="/" element={<DashboardPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/confirm-email" element={<ConfirmEmailPage />} />
          <Route path="/password-reset" element={<PasswordResetPage />} />
          <Route path="/dashboard" element={<DashboardPage />} />
          <Route path="/recipes" element={<RecipeSearchPage />} />
          <Route path="/recipes/new" element={<RecipeEditPage />} />
          <Route path="/recipes/:id" element={<RecipeDetailPage />} />
          <Route path="/recipes/:id/edit" element={<RecipeEditPage />} />
          <Route path="/profile/edit" element={<ProfileEditPage />} />
          <Route path="/schedules" element={<SchedulePage />} />
          <Route path="/shopping-list" element={<ShoppingListPage />} />
          <Route path="/admin" element={<AdminDashboardPage />} />
          <Route path="/admin/users" element={<UserManagementPage />} />
          <Route path="/admin/recipes" element={<RecipeManagementPage />} />
        </Routes>
      </main>
      {!isAuthPage && <Footer />}
    </div>
  );
};

export default App;
