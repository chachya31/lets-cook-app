import { LogIn, UserPlus } from 'lucide-react';
import React from 'react';
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../../hooks/useAuth';
import { useForm } from '../../../hooks/useForm';
import { FormField } from '../../common/FormField';
import {
  getRegisterValidationRules,
  languageOptions,
  registerFormFields,
  registerFormInitialValues,
} from './registerFormConfig';

/**
 * ユーザー登録ページ
 */
export const RegisterPage: React.FC = () => {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { register, error, clearError } = useAuth();

  const { values, errors, isSubmitting, handleChange, handleSubmit } = useForm({
    initialValues: registerFormInitialValues,
    validationRules: getRegisterValidationRules(t),
    onSubmit: async (formData) => {
      clearError();
      try {
        await register({
          email: formData.email,
          password: formData.password,
          nickname: formData.nickname,
          preferredLanguage: formData.preferredLanguage,
        });
        navigate('/confirm-email', {
          state: { email: formData.email, password: formData.password },
        });
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
            <UserPlus size={48} className="text-green-600" />
          </div>
          <h2 className="mt-6 text-center text-3xl font-extrabold text-gray-900">
            {t('auth.register')}
          </h2>
        </div>
        <form className="mt-8 space-y-6" onSubmit={handleSubmit}>
          {error && (
            <div className="rounded-md bg-red-50 p-4">
              <p className="text-sm text-red-800">{error}</p>
            </div>
          )}

          <div className="space-y-4">
            {registerFormFields.map((field) => (
              <FormField
                key={field.id}
                id={field.id}
                name={field.name}
                type={field.type}
                label={t(field.labelKey)}
                value={values[field.name as keyof typeof values]}
                error={errors[field.name]}
                autoComplete={field.autoComplete}
                onChange={handleChange}
              />
            ))}

            <div>
              <label
                htmlFor="preferredLanguage"
                className="block text-sm font-medium text-gray-700"
              >
                {t('auth.preferredLanguage')}
              </label>
              <select
                id="preferredLanguage"
                name="preferredLanguage"
                className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-green-500 focus:border-green-500 sm:text-sm"
                value={values.preferredLanguage}
                onChange={handleChange}
              >
                {languageOptions.map((option) => (
                  <option key={option.value} value={option.value}>
                    {t(option.labelKey)}
                  </option>
                ))}
              </select>
            </div>
          </div>

          <div>
            <button
              type="submit"
              disabled={isSubmitting}
              className="group relative w-full flex justify-center items-center space-x-2 py-2 px-4 border border-transparent text-sm font-medium rounded-md text-white bg-green-600 hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-green-500 disabled:opacity-50"
            >
              <UserPlus size={18} />
              <span>{isSubmitting ? t('common.loading') : t('auth.register')}</span>
            </button>
          </div>

          <div className="text-center">
            <button
              type="button"
              onClick={() => navigate('/login')}
              className="flex items-center justify-center space-x-1 text-sm text-green-600 hover:text-green-500 mx-auto"
            >
              <LogIn size={16} />
              <span>{t('auth.hasAccount')}</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
