import { ValidationRules, validationRules } from '../../../utils/validation';
import { PASSWORD_MIN_LENGTH, NICKNAME_MAX_LENGTH } from '../../../constants/validation';

/**
 * 登録フォームの初期値
 */
export const registerFormInitialValues = {
  email: '',
  password: '',
  confirmPassword: '',
  nickname: '',
  preferredLanguage: 'ja' as 'ja' | 'ko',
};

/**
 * 登録フォームのバリデーションルール
 */
export const getRegisterValidationRules = (t: (key: string) => string): ValidationRules => ({
  email: [
    validationRules.required(t('validation.emailRequired')),
    validationRules.email(t('validation.emailInvalid')),
  ],
  password: [
    validationRules.required(t('validation.passwordRequired')),
    validationRules.minLength(PASSWORD_MIN_LENGTH, t('validation.passwordMinLength')),
    validationRules.pattern(
      /(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[$*.[\]{}()?\-"!@#%&/\\,><':;|_~`+=])/,
      t('validation.passwordComplexity')
    ),
  ],
  confirmPassword: [
    validationRules.matchField('password', t('validation.passwordMismatch')),
  ],
  nickname: [
    validationRules.required(t('validation.nicknameRequired')),
    validationRules.maxLength(NICKNAME_MAX_LENGTH, t('validation.nicknameMaxLength')),
  ],
});

/**
 * 登録フォームのフィールド設定の型
 */
export interface RegisterFormField {
  id: string;
  name: string;
  type: string;
  autoComplete: string;
  labelKey: string;
}

/**
 * 登録フォームのフィールド設定
 */
export const registerFormFields: RegisterFormField[] = [
  {
    id: 'email',
    name: 'email',
    type: 'email',
    autoComplete: 'email',
    labelKey: 'auth.email',
  },
  {
    id: 'nickname',
    name: 'nickname',
    type: 'text',
    autoComplete: 'off',
    labelKey: 'auth.nickname',
  },
  {
    id: 'password',
    name: 'password',
    type: 'password',
    autoComplete: 'new-password',
    labelKey: 'auth.password',
  },
  {
    id: 'confirmPassword',
    name: 'confirmPassword',
    type: 'password',
    autoComplete: 'new-password',
    labelKey: 'auth.confirmPassword',
  },
];

/**
 * 言語選択オプションの型
 */
export interface LanguageOption {
  value: string;
  labelKey: string;
}

/**
 * 言語選択オプション
 */
export const languageOptions: LanguageOption[] = [
  { value: 'ja', labelKey: 'language.japanese' },
  { value: 'ko', labelKey: 'language.korean' },
];
