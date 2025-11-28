import React from 'react';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';

/**
 * パスワードリセットページ（プレースホルダー）
 * TODO: 実装予定
 */
export const PasswordResetPage: React.FC = () => {
  const { t } = useTranslation();
  const navigate = useNavigate();

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-md w-full space-y-8">
        <div>
          <h2 className="mt-6 text-center text-3xl font-extrabold text-gray-900">
            {t('auth.passwordReset')}
          </h2>
          <p className="mt-2 text-center text-sm text-gray-600">
            {t('auth.passwordResetDescription')}
          </p>
        </div>
        <div className="mt-8 space-y-6">
          <p className="text-center text-gray-500">
            {t('common.comingSoon')}
          </p>
          <div className="text-center">
            <button
              type="button"
              onClick={() => navigate('/login')}
              className="text-sm text-green-600 hover:text-green-500"
            >
              {t('auth.backToLogin')}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
