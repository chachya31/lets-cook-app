import React from 'react';
import { Routes, Route } from 'react-router-dom';
import { LoginPage } from './components/auth/Login/LoginPage';
import { RegisterPage } from './components/auth/Register/RegisterPage';
import { ConfirmEmailPage } from './components/auth/ConfirmEmail/ConfirmEmailPage';
import { PasswordResetPage } from './components/auth/PasswordReset/PasswordResetPage';
import RecipeSearchPage from './components/recipe/RecipeSearchPage';
import RecipeDetailPage from './components/recipe/RecipeDetailPage';
import RecipeEditPage from './components/recipe/RecipeEdit/RecipeEditPage';
import SchedulePage from './components/schedule/SchedulePage';
import ShoppingListPage from './components/shopping/ShoppingListPage';
import { AlertModal } from './components/alert/AlertModal';

const App: React.FC = () => {
  return (
    <div>
      <AlertModal />
      <Routes>
        <Route path="/" element={<div>ホーム画面</div>} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/confirm-email" element={<ConfirmEmailPage />} />
        <Route path="/password-reset" element={<PasswordResetPage />} />
        <Route path="/dashboard" element={<div>ダッシュボード</div>} />
        <Route path="/recipes" element={<RecipeSearchPage />} />
        <Route path="/recipes/new" element={<RecipeEditPage />} />
        <Route path="/recipes/:id" element={<RecipeDetailPage />} />
        <Route path="/recipes/:id/edit" element={<RecipeEditPage />} />
        <Route path="/schedules" element={<SchedulePage />} />
        <Route path="/shopping-list" element={<ShoppingListPage />} />
      </Routes>
    </div>
  );
};

export default App;
