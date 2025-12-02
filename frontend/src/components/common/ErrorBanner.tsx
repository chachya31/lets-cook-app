import React from 'react';
import { useTranslation } from 'react-i18next';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { Button } from '@/components/ui/button';

interface ErrorBannerProps {
  error: string | null;
  onRetry?: () => void;
  onDismiss?: () => void;
}

/**
 * エラーバナーコンポーネント
 * 画面上部にエラーメッセージを表示し、再試行ボタンを提供
 */
export const ErrorBanner: React.FC<ErrorBannerProps> = ({ error, onRetry, onDismiss }) => {
  const { t } = useTranslation();

  if (!error) {
    return null;
  }

  return (
    <div className="fixed top-0 left-0 right-0 z-50 p-4">
      <Alert variant="destructive" className="shadow-lg">
        <AlertTitle className="flex items-center justify-between">
          <span>{t('error.title')}</span>
          {onDismiss && (
            <button
              onClick={onDismiss}
              className="text-sm hover:opacity-70 transition-opacity"
              aria-label={t('error.dismiss')}
            >
              ✕
            </button>
          )}
        </AlertTitle>
        <AlertDescription className="mt-2">
          <p className="mb-3">{error}</p>
          {onRetry && (
            <Button
              onClick={onRetry}
              variant="outline"
              size="sm"
              className="bg-white hover:bg-gray-100"
            >
              {t('error.retry')}
            </Button>
          )}
        </AlertDescription>
      </Alert>
    </div>
  );
};
