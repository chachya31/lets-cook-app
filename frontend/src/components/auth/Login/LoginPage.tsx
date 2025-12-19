import { UserPlus } from 'lucide-react';
import React from 'react';
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../../hooks/useAuth';
import { useForm } from '../../../hooks/useForm';
import { FormField } from '../../common/FormField';
import {
  getLoginValidationRules,
  loginFormFields,
  loginFormInitialValues,
} from './loginFormConfig';

/**
 * ログインページ
 */
export const LoginPage: React.FC = () => {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { login, error, clearError } = useAuth();

  const { values, errors, isSubmitting, handleChange, handleSubmit } = useForm({
    initialValues: loginFormInitialValues,
    validationRules: getLoginValidationRules(t),
    onSubmit: async (formData) => {
      clearError();
      try {
        await login(formData);
        navigate('/dashboard');
      } catch (err) {
        // Error is handled by Redux
      }
    },
  });

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-md w-full space-y-8">
        <div>
          <div className="flex justify-center mb-4">
            <img src="/logo.svg" alt="Logo" className="h-16 w-auto" />
          </div>
          <h2 className="mt-6 text-center text-3xl font-extrabold text-gray-900">
            {t('auth.login')}
          </h2>
        </div>
        <form className="mt-8 space-y-6" onSubmit={handleSubmit}>
          {error && (
            <div className="rounded-md bg-red-50 p-4">
              <p className="text-sm text-red-800">{error}</p>
            </div>
          )}

          <div className="space-y-4">
            {loginFormFields.map((field) => (
              <FormField
                key={field.id}
                id={field.id}
                name={field.name}
                type={field.type}
                label={t(field.labelKey)}
                placeholder={t(field.placeholderKey)}
                value={values[field.name as keyof typeof values]}
                error={errors[field.name]}
                autoComplete={field.autoComplete}
                onChange={handleChange}
              />
            ))}
          </div>

          <div>
            <button
              type="submit"
              disabled={isSubmitting}
              className="group relative w-full flex justify-center items-center py-2 px-4 border border-transparent text-sm font-medium rounded-md text-white bg-green-600 hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-green-500 disabled:opacity-50"
            >
              <span>{isSubmitting ? t('common.loading') : t('auth.login')}</span>
            </button>
          </div>

          <div className="text-center">
            <button
              type="button"
              onClick={() => navigate('/register')}
              className="flex items-center justify-center space-x-1 text-sm text-green-600 hover:text-green-500 mx-auto"
            >
              <UserPlus size={16} />
              <span>{t('auth.noAccount')}</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
