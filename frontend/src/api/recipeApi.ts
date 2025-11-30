import { Recipe, RecipeRequest, RecipeSearchParams } from '../types/recipe';
import { apiGet, apiPost, apiPut, apiDelete, apiPostFile } from '../utils/apiClient';

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
