import { Ingredient, Recipe, RecipeRequest, RecipeSearchParams, Step } from '../types/recipe';
import { apiDelete, apiGet, apiPost, apiPostFile, apiPut } from '../utils/apiClient';

/**
 * レシピを検索
 */
export const searchRecipes = async (params?: RecipeSearchParams): Promise<Recipe[]> => {
  const queryParams = new URLSearchParams();
  if (params?.keyword) queryParams.append('keyword', params.keyword);
  if (params?.authorId) queryParams.append('authorId', params.authorId);

  const endpoint = `/api/recipes${queryParams.toString() ? `?${queryParams.toString()}` : ''}`;
  return apiGet<Recipe[]>(endpoint);
};

/**
 * レシピ詳細を取得
 */
export const getRecipe = async (recipeId: string): Promise<Recipe> => {
  return apiGet<Recipe>(`/api/recipes/${recipeId}`);
};

/**
 * レシピを作成
 */
export const createRecipe = async (userId: string, recipe: RecipeRequest): Promise<Recipe> => {
  return apiPost<Recipe>('/api/recipes', recipe, userId);
};

/**
 * レシピを作成（画像付き）
 */
export const createRecipeWithImages = async (
  userId: string,
  title: string,
  ingredients: Ingredient[],
  steps: Step[],
  cookingTime: number,
  mainImage?: File,
  stepImages?: (File | null)[]
): Promise<Recipe> => {
  const formData = new FormData();
  formData.append('title', title);
  formData.append('ingredients', JSON.stringify(ingredients));
  formData.append('steps', JSON.stringify(steps));
  formData.append('cookingTime', cookingTime.toString());

  if (mainImage) {
    formData.append('mainImage', mainImage);
  }

  if (stepImages) {
    stepImages.forEach((file) => {
      if (file) {
        formData.append('stepImages', file);
      }
    });
  }

  return apiPostFile<Recipe>('/api/recipes/with-images', formData, userId);
};

/**
 * レシピを更新
 */
export const updateRecipe = async (
  recipeId: string,
  userId: string,
  recipe: RecipeRequest
): Promise<Recipe> => {
  return apiPut<Recipe>(`/api/recipes/${recipeId}`, recipe, userId);
};

/**
 * レシピを削除
 */
export const deleteRecipe = async (recipeId: string, userId: string): Promise<void> => {
  return apiDelete<void>(`/api/recipes/${recipeId}`, userId);
};

/**
 * レシピ画像をアップロード
 */
export const uploadRecipeImage = async (
  recipeId: string,
  userId: string,
  file: File
): Promise<Recipe> => {
  const formData = new FormData();
  formData.append('file', file);
  return apiPostFile<Recipe>(`/api/recipes/${recipeId}/image`, formData, userId);
};

/**
 * 手順画像をアップロード
 */
export const uploadStepImage = async (
  recipeId: string,
  stepIndex: number,
  userId: string,
  file: File
): Promise<Recipe> => {
  const formData = new FormData();
  formData.append('file', file);
  return apiPostFile<Recipe>(`/api/recipes/${recipeId}/steps/${stepIndex}/image`, formData, userId);
};
