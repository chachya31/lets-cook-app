import React, { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { Card } from '../../ui/card';
import { Button } from '../../ui/button';
import { useForm } from '../../../hooks/useForm';
import { FormField } from '../../common/FormField';
import { confirmSignUp, resendConfirmationCode } from '../../../api/userApi';
import {
  confirmEmailFormInitialValues,
  getConfirmEmailValidationRules,
  confirmEmailFormFields,
} from './confirmEmailFormConfig';

export const ConfirmEmailPage: React.FC = () => {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const location = useLocation();
  const email = location.state?.email || '';

  const [error, setError] = useState('');
  const [isResending, setIsResending] = useState(false);
  const [resendMessage, setResendMessage] = useState('');

  const handleFormSubmit = async (values: typeof confirmEmailFormInitialValues) => {
    setError('');

    try {
      await confirmSignUp(email, values.confirmationCode);
      navigate('/login', {
        state: { message: t('auth.confirmEmail.success') }
      });
    } catch (err: any) {
      setError(err.response?.data?.message || t('auth.confirmEmail.error'));
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
              />
            ))}
          </div>

          <div className="flex flex-col space-y-4">
            <Button
              type="submit"
              disabled={isSubmitting || !values.confirmationCode}
              className="w-full"
            >
              {isSubmitting ? t('common.loading') : t('auth.confirmEmail.submit')}
            </Button>

            <Button
              type="button"
              variant="outline"
              onClick={handleResendCode}
              disabled={isResending}
              className="w-full"
            >
              {isResending ? t('common.loading') : t('auth.confirmEmail.resendCode')}
            </Button>
          </div>
        </form>
      </Card>
    </div>
  );
};
