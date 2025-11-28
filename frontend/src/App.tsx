import React from 'react';
import { Routes, Route } from 'react-router-dom';
import { LoginPage } from './components/auth/Login/LoginPage';
import { RegisterPage } from './components/auth/Register/RegisterPage';
import { PasswordResetPage } from './components/auth/PasswordReset/PasswordResetPage';

const App: React.FC = () => {
  return (
    <div>
      <Routes>
        <Route path="/" element={<div>ホーム画面</div>} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/password-reset" element={<PasswordResetPage />} />
        <Route path="/dashboard" element={<div>ダッシュボード</div>} />
      </Routes>
    </div>
  );
};

export default App;
