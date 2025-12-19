import { Clock, RefreshCw } from 'lucide-react';
import React, { useCallback, useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useDispatch } from 'react-redux';
import { useLocation, useNavigate } from 'react-router-dom';
import { confirmSignUp, resendConfirmationCode } from '../../../api/userApi';
import { useForm } from '../../../hooks/useForm';
import { login } from '../../../store/slices/authSlice';
import { AppDispatch } from '../../../store/store';
import { FormField } from '../../common/FormField';
import { Button } from '../../ui/button';
import { Card } from '../../ui/card';
import {
  confirmEmailFormFields,
  confirmEmailFormInitialValues,
  getConfirmEmailValidationRules,
} from './confirmEmailFormConfig';

// 確認コードの有効期間（秒）
const CODE_VALIDITY_SECONDS = 5 * 60; // 5分

export const ConfirmEmailPage: React.FC = () => {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const location = useLocation();
  const dispatch = useDispatch<AppDispatch>();
  const email = location.state?.email || '';
  const password = location.state?.password || '';

  const [error, setError] = useState('');
  const [isResending, setIsResending] = useState(false);
  const [resendMessage, setResendMessage] = useState('');
  const [remainingSeconds, setRemainingSeconds] = useState(CODE_VALIDITY_SECONDS);
  const [isCodeExpired, setIsCodeExpired] = useState(false);
  const [hasResent, setHasResent] = useState(false);

  // タイマーをリセット
  const resetTimer = useCallback(() => {
    setRemainingSeconds(CODE_VALIDITY_SECONDS);
    setIsCodeExpired(false);
    setHasResent(true);
  }, []);

  // カウントダウンタイマー
  useEffect(() => {
    if (remainingSeconds <= 0) {
      setIsCodeExpired(true);
      return;
    }

    const timer = setInterval(() => {
      setRemainingSeconds((prev) => {
        if (prev <= 1) {
          setIsCodeExpired(true);
          return 0;
        }
        return prev - 1;
      });
    }, 1000);

    return () => clearInterval(timer);
  }, [remainingSeconds]);

  // 残り時間をフォーマット（MM:SS）
  const formatTime = (seconds: number): string => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
  };

  const handleFormSubmit = async (values: typeof confirmEmailFormInitialValues) => {
    setError('');

    // 有効期限切れで再発行していない場合はブロック
    if (isCodeExpired && !hasResent) {
      setError(t('auth.confirmEmail.codeExpiredError'));
      return;
    }

    try {
      // 確認コードを検証
      await confirmSignUp(email, values.confirmationCode);

      // パスワードがある場合は自動ログイン
      if (password) {
        await dispatch(login({ email, password })).unwrap();
        navigate('/dashboard');
      } else {
        // パスワードがない場合はログイン画面へ
        navigate('/login', {
          state: { message: t('auth.confirmEmail.success') },
        });
      }
    } catch (err: any) {
      setError(err.message || t('auth.confirmEmail.error'));
    }
  };

  const { values, errors, isSubmitting, handleChange, handleSubmit } = useForm({
    initialValues: confirmEmailFormInitialValues,
    validationRules: getConfirmEmailValidationRules(t),
    onSubmit: handleFormSubmit,
  });

  const handleResendCode = async () => {
    setError('');
    setResendMessage('');
    setIsResending(true);

    try {
      await resendConfirmationCode(email);
      setResendMessage(t('auth.confirmEmail.resendSuccess'));
      resetTimer();
    } catch (err: any) {
      setError(err.response?.data?.message || t('auth.confirmEmail.resendError'));
    } finally {
      setIsResending(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
      <Card className="max-w-md w-full space-y-8 p-8">
        <div>
          <h2 className="mt-6 text-center text-3xl font-extrabold text-gray-900">
            {t('auth.confirmEmail.title')}
          </h2>
          <p className="mt-2 text-center text-sm text-gray-600">
            {t('auth.confirmEmail.description', { email })}
          </p>
        </div>

        {/* カウントダウンタイマー */}
        <div
          className={`flex items-center justify-center space-x-2 p-3 rounded-md ${
            isCodeExpired ? 'bg-red-50 text-red-700' : 'bg-blue-50 text-blue-700'
          }`}
        >
          <Clock size={20} />
          {isCodeExpired ? (
            <span className="font-medium">{t('auth.confirmEmail.codeExpired')}</span>
          ) : (
            <span className="font-medium">
              {t('auth.confirmEmail.remainingTime', { time: formatTime(remainingSeconds) })}
            </span>
          )}
        </div>

        <form className="mt-8 space-y-6" onSubmit={handleSubmit}>
          {error && (
            <div className="rounded-md bg-red-50 p-4">
              <p className="text-sm text-red-800">{error}</p>
            </div>
          )}

          {resendMessage && (
            <div className="rounded-md bg-green-50 p-4">
              <p className="text-sm text-green-800">{resendMessage}</p>
            </div>
          )}

          <div className="space-y-4">
            {confirmEmailFormFields.map((field) => (
              <FormField
                key={field.id}
                id={field.id}
                name={field.name}
                type={field.type}
                label={t(field.labelKey)}
                placeholder={t(field.placeholderKey)}
                value={values[field.name as keyof typeof values]}
                error={errors[field.name]}
                onChange={handleChange}
                maxLength={field.maxLength}
                disabled={isCodeExpired && !hasResent}
              />
            ))}
          </div>

          <div className="flex flex-col space-y-4">
            <Button
              type="submit"
              disabled={isSubmitting || !values.confirmationCode || (isCodeExpired && !hasResent)}
              className="w-full"
            >
              {isSubmitting ? t('common.loading') : t('auth.confirmEmail.submit')}
            </Button>

            <Button
              type="button"
              variant={isCodeExpired ? 'default' : 'outline'}
              onClick={handleResendCode}
              disabled={isResending}
              className="w-full flex items-center justify-center space-x-2"
            >
              <RefreshCw size={16} className={isResending ? 'animate-spin' : ''} />
              <span>{isResending ? t('common.loading') : t('auth.confirmEmail.resendCode')}</span>
            </Button>
          </div>
        </form>
      </Card>
    </div>
  );
};
