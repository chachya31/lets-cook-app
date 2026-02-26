import { ValidationRules, validationRules } from '../../../utils/validation';

/**
 * ログインフォームの初期値
 */
export const loginFormInitialValues = {
  email: '',
  password: '',
};

/**
 * ログインフォームのバリデーションルール
 */
export const getLoginValidationRules = (t: (key: string) => string): ValidationRules => ({
  email: [
    validationRules.required(t('validation.emailRequired')),
    validationRules.email(t('validation.emailInvalid')),
  ],
  password: [
    validationRules.required(t('validation.passwordRequired')),
  ],
});

/**
 * ログインフォームのフィールド設定の型
 */
export interface LoginFormField {
  id: string;
  name: string;
  type: string;
  autoComplete: string;
  labelKey: string;
  placeholderKey: string;
}

/**
 * ログインフォームのフィールド設定
 */
export const loginFormFields: LoginFormField[] = [
  {
    id: 'email',
    name: 'email',
    type: 'email',
    autoComplete: 'email',
    labelKey: 'auth.email',
    placeholderKey: 'auth.email',
  },
  {
    id: 'password',
    name: 'password',
    type: 'password',
    autoComplete: 'current-password',
    labelKey: 'auth.password',
    placeholderKey: 'auth.password',
  },
];
