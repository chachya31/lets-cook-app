import { AdminDashboardStats, SetRecipeStatusRequest } from '../types/admin';
import { Recipe } from '../types/recipe';
import { apiGet, apiPut, apiDelete } from '../utils/apiClient';

/**
 * 管理者ダッシュボード統計を取得
 */
export const getAdminDashboardStats = async (): Promise<AdminDashboardStats> => {
  return apiGet<AdminDashboardStats>('/api/admin/dashboard');
};

/**
 * ユーザーを停止
 */
export const suspendUser = async (userId: string): Promise<void> => {
  return apiPut<void>(`/api/admin/users/${userId}/suspend`, {});
};

/**
 * ユーザーを削除
 */
export const deleteUserByAdmin = async (userId: string): Promise<void> => {
  return apiDelete<void>(`/api/admin/users/${userId}`);
};

/**
 * すべてのレシピを取得（管理者用）
 */
export const getAllRecipesForAdmin = async (): Promise<Recipe[]> => {
  return apiGet<Recipe[]>('/api/admin/recipes');
};

/**
 * レシピのステータスを設定
 */
export const setRecipeStatus = async (
  recipeId: string,
  request: SetRecipeStatusRequest
): Promise<Recipe> => {
  return apiPut<Recipe>(`/api/admin/recipes/${recipeId}/status`, request);
};

/**
 * レシピを削除（管理者用）
 */
export const deleteRecipeByAdmin = async (recipeId: string): Promise<void> => {
  return apiDelete<void>(`/api/admin/recipes/${recipeId}`);
};
