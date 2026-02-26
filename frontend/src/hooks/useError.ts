import { useState, useCallback } from 'react';
import { useTranslation } from 'react-i18next';

interface UseErrorReturn {
  error: string | null;
  setError: (error: string | null) => void;
  clearError: () => void;
  handleError: (error: unknown) => void;
}

/**
 * エラーハンドリングのためのカスタムフック
 */
export const useError = (): UseErrorReturn => {
  const { t } = useTranslation();
  const [error, setError] = useState<string | null>(null);

  const clearError = useCallback(() => {
    setError(null);
  }, []);

  const handleError = useCallback((error: unknown) => {
    if (error instanceof Error) {
      // ネットワークエラー
      if (error.message.includes('Failed to fetch') || error.message.includes('Network')) {
        setError(t('error.network'));
        return;
      }

      // その他のエラー
      setError(error.message || t('error.unknown'));
    } else if (typeof error === 'string') {
      setError(error);
    } else {
      setError(t('error.unknown'));
    }
  }, [t]);

  return {
    error,
    setError,
    clearError,
    handleError,
  };
};
