import { ValidationRules, validationRules } from '../../../utils/validation';

/**
 * メール確認フォームの初期値
 */
export const confirmEmailFormInitialValues = {
  confirmationCode: '',
};

/**
 * メール確認フォームのバリデーションルール
 */
export const getConfirmEmailValidationRules = (t: (key: string) => string): ValidationRules => ({
  confirmationCode: [
    validationRules.required(t('validation.confirmationCodeRequired')),
    validationRules.minLength(6, t('validation.confirmationCodeLength')),
    validationRules.maxLength(6, t('validation.confirmationCodeLength')),
  ],
});

/**
 * メール確認フォームのフィールド設定の型
 */
export interface ConfirmEmailFormField {
  id: string;
  name: string;
  type: string;
  maxLength: number;
  labelKey: string;
  placeholderKey: string;
}

/**
 * メール確認フォームのフィールド設定
 */
export const confirmEmailFormFields: ConfirmEmailFormField[] = [
  {
    id: 'confirmationCode',
    name: 'confirmationCode',
    type: 'text',
    maxLength: 6,
    labelKey: 'auth.confirmEmail.codeLabel',
    placeholderKey: 'auth.confirmEmail.codePlaceholder',
  },
];
