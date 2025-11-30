/**
 * レシピ型定義
 */

export interface Ingredient {
  name: string;
  quantity: number;
  unit: string;
  note?: string;
  optional: boolean;
}

export interface Recipe {
  recipeId: string;
  title: string;
  authorId: string;
  ingredients: Ingredient[];
  steps: string[];
  cookingTime: number;
  imageUrl?: string;
  isPublic: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface RecipeRequest {
  title: string;
  ingredients: Ingredient[];
  steps: string[];
  cookingTime: number;
}

export interface RecipeSearchParams {
  keyword?: string;
  authorId?: string;
}
