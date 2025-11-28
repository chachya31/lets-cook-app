/**
 * バリデーションユーティリティ
 */

export interface ValidationRule {
  validate: (value: any, formData?: any) => boolean;
  message: string;
}

export interface ValidationRules {
  [key: string]: ValidationRule[];
}

/**
 * バリデーションを実行
 */
export const validateForm = (
  formData: Record<string, any>,
  rules: ValidationRules
): Record<string, string> => {
  const errors: Record<string, string> = {};

  Object.keys(rules).forEach((field) => {
    const fieldRules = rules[field];
    const value = formData[field];

    for (const rule of fieldRules) {
      if (!rule.validate(value, formData)) {
        errors[field] = rule.message;
        break; // 最初のエラーのみ表示
      }
    }
  });

  return errors;
};

/**
 * 共通バリデーションルール
 */
export const validationRules = {
  required: (message: string): ValidationRule => ({
    validate: (value) => value !== null && value !== undefined && value.trim() !== '',
    message,
  }),

  email: (message: string): ValidationRule => ({
    validate: (value) => /\S+@\S+\.\S+/.test(value),
    message,
  }),

  minLength: (length: number, message: string): ValidationRule => ({
    validate: (value) => value && value.length >= length,
    message,
  }),

  maxLength: (length: number, message: string): ValidationRule => ({
    validate: (value) => !value || value.length <= length,
    message,
  }),

  pattern: (regex: RegExp, message: string): ValidationRule => ({
    validate: (value) => regex.test(value),
    message,
  }),

  matchField: (fieldName: string, message: string): ValidationRule => ({
    validate: (value, formData) => value === formData?.[fieldName],
    message,
  }),
};
