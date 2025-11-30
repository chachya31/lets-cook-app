import { Recipe, RecipeRequest, RecipeSearchParams } from '../types/recipe';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

/**
 * レシピを検索
 */
export const searchRecipes = async (params?: RecipeSearchParams): Promise<Recipe[]> => {
  const queryParams = new URLSearchParams();
  if (params?.keyword) queryParams.append('keyword', params.keyword);
  if (params?.authorId) queryParams.append('authorId', params.authorId);

  const url = `${API_BASE_URL}/api/recipes${queryParams.toString() ? `?${queryParams.toString()}` : ''}`;
  
  const response = await fetch(url, {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
    },
  });

  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message || 'Failed to search recipes');
  }

  return response.json();
};

/**
 * レシピ詳細を取得
 */
export const getRecipe = async (recipeId: string): Promise<Recipe> => {
  const response = await fetch(`${API_BASE_URL}/api/recipes/${recipeId}`, {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
    },
  });

  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message || 'Failed to get recipe');
  }

  return response.json();
};

/**
 * レシピを作成
 */
export const createRecipe = async (userId: string, recipe: RecipeRequest): Promise<Recipe> => {
  const response = await fetch(`${API_BASE_URL}/api/recipes`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'X-User-Id': userId,
    },
    body: JSON.stringify(recipe),
  });

  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message || 'Failed to create recipe');
  }

  return response.json();
};

/**
 * レシピを更新
 */
export const updateRecipe = async (
  recipeId: string,
  userId: string,
  recipe: RecipeRequest
): Promise<Recipe> => {
  const response = await fetch(`${API_BASE_URL}/api/recipes/${recipeId}`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
      'X-User-Id': userId,
    },
    body: JSON.stringify(recipe),
  });

  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message || 'Failed to update recipe');
  }

  return response.json();
};

/**
 * レシピを削除
 */
export const deleteRecipe = async (recipeId: string, userId: string): Promise<void> => {
  const response = await fetch(`${API_BASE_URL}/api/recipes/${recipeId}`, {
    method: 'DELETE',
    headers: {
      'X-User-Id': userId,
    },
  });

  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message || 'Failed to delete recipe');
  }
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

  const response = await fetch(`${API_BASE_URL}/api/recipes/${recipeId}/image`, {
    method: 'POST',
    headers: {
      'X-User-Id': userId,
    },
    body: formData,
  });

  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message || 'Failed to upload recipe image');
  }

  return response.json();
};
