import React from 'react';
import { Routes, Route, useLocation } from 'react-router-dom';
import { LoginPage } from './components/auth/Login/LoginPage';
import { RegisterPage } from './components/auth/Register/RegisterPage';
import { ConfirmEmailPage } from './components/auth/ConfirmEmail/ConfirmEmailPage';
import { PasswordResetPage } from './components/auth/PasswordReset/PasswordResetPage';
import DashboardPage from './components/dashboard/DashboardPage';
import RecipeSearchPage from './components/recipe/RecipeSearchPage';
import RecipeDetailPage from './components/recipe/RecipeDetailPage';
import RecipeEditPage from './components/recipe/RecipeEdit/RecipeEditPage';
import SchedulePage from './components/schedule/SchedulePage';
import ShoppingListPage from './components/shopping/ShoppingListPage';
import { AlertModal } from './components/alert/AlertModal';
import Header from './components/common/Header';
import Footer from './components/common/Footer';

const App: React.FC = () => {
  const location = useLocation();
  
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
          <Route path="/schedules" element={<SchedulePage />} />
          <Route path="/shopping-list" element={<ShoppingListPage />} />
        </Routes>
      </main>
      {!isAuthPage && <Footer />}
    </div>
  );
};

export default App;
