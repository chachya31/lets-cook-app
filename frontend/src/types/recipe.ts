/**
 * レシピ型定義
 */

export interface Ingredient {
  name: string;
  quantity?: number;
  unit?: string;
  note?: string;
  optional: boolean;
}

export interface Step {
  description: string;
  imageUrl?: string;
  videoUrl?: string;
}

export interface Recipe {
  recipeId: string;
  title: string;
  authorId: string;
  ingredients: Ingredient[];
  steps: Step[];
  cookingTime: number;
  imageUrl?: string;
  isPublic: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface RecipeRequest {
  title: string;
  ingredients: Ingredient[];
  steps: Step[];
  cookingTime: number;
}

export interface RecipeSearchParams {
  keyword?: string;
  authorId?: string;
}

/**
 * 食材検索リクエスト
 */
export interface SearchByIngredientsRequest {
  ingredients: string[];
}

/**
 * 食材検索レスポンス
 */
export interface RecipeSearchByIngredientResponse {
  recipes: RecipeSummary[];
  totalCount: number;
  searchedIngredients: string[];
}

/**
 * レシピ概要（検索結果用）
 */
export interface RecipeSummary {
  recipeId: string;
  title: string;
  imageUrl?: string;
}
