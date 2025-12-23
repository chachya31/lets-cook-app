import {
  COOKING_TIME_MIN,
  INGREDIENT_NAME_MAX_LENGTH,
  RECIPE_MIN_INGREDIENTS,
  RECIPE_MIN_STEPS,
  RECIPE_TITLE_MAX_LENGTH,
} from '../../../constants/validation';
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
    { name: '', quantity: undefined, unit: '', note: '', optional: false },
  ] as Ingredient[],
  steps: [''],
};

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
      required: false,
    },
    unit: {
      required: false,
    },
  },
  steps: {
    minItems: RECIPE_MIN_STEPS,
    required: true,
  },
};
