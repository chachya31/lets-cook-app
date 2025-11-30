import { Ingredient } from '../../../types/recipe';

/**
 * レシピ編集フォームの設定
 */

/**
 * 初期値
 */
export const initialFormState = {
  title: '',
  cookingTime: 0,
  ingredients: [
    { name: '', quantity: 0, unit: 'g', note: '', optional: false },
  ] as Ingredient[],
  steps: [''],
};

/**
 * 単位オプション
 */
export const unitOptions = [
  { value: 'g', label: 'g' },
  { value: 'kg', label: 'kg' },
  { value: 'ml', label: 'ml' },
  { value: 'l', label: 'l' },
  { value: 'tbsp', label: '大さじ' },
  { value: 'tsp', label: '小さじ' },
  { value: 'cup', label: 'カップ' },
  { value: 'piece', label: '個' },
  { value: 'pack', label: 'パック' },
  { value: 'can', label: '缶' },
  { value: 'bottle', label: '本' },
  { value: 'slice', label: '枚' },
  { value: 'clove', label: '片' },
  { value: 'pinch', label: 'ひとつまみ' },
  { value: 'to_taste', label: '適量' },
  { value: 'as_needed', label: '必要に応じて' },
];

/**
 * バリデーションルール
 */
export const validationRules = {
  title: {
    required: true,
    maxLength: 100,
  },
  cookingTime: {
    required: true,
    min: 0,
  },
  ingredients: {
    minItems: 1,
    name: {
      required: true,
      maxLength: 100,
    },
    quantity: {
      required: true,
      min: 0,
    },
    unit: {
      required: true,
    },
  },
  steps: {
    minItems: 1,
    required: true,
  },
};
