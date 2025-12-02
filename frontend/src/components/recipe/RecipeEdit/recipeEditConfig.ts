import { Ingredient } from '../../../types/recipe';
import {
  RECIPE_TITLE_MAX_LENGTH,
  COOKING_TIME_MIN,
  RECIPE_MIN_INGREDIENTS,
  RECIPE_MIN_STEPS,
  INGREDIENT_NAME_MAX_LENGTH,
  INGREDIENT_QUANTITY_MIN,
} from '../../../constants/validation';

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
    maxLength: RECIPE_TITLE_MAX_LENGTH,
  },
  cookingTime: {
    required: true,
    min: COOKING_TIME_MIN,
  },
  ingredients: {
    minItems: RECIPE_MIN_INGREDIENTS,
    name: {
      required: true,
      maxLength: INGREDIENT_NAME_MAX_LENGTH,
    },
    quantity: {
      required: true,
      min: INGREDIENT_QUANTITY_MIN,
    },
    unit: {
      required: true,
    },
  },
  steps: {
    minItems: RECIPE_MIN_STEPS,
    required: true,
  },
};
