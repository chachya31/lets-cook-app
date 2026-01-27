/**
 * Admin Store (Zustand)
 * 管理者状態管理
 */

import { create } from 'zustand';
import * as adminApi from '../api/adminApi';
import { AdminDashboardStats, SetRecipeStatusRequest } from '../types/admin';
import { Recipe } from '../types/recipe';
import { User } from '../types/user';

interface AdminState {
  stats: AdminDashboardStats | null;
  recipes: Recipe[];
  users: User[];
  loading: boolean;
  error: string | null;
}

interface AdminActions {
  fetchDashboardStats: () => Promise<void>;
  fetchAllUsers: () => Promise<void>;
  suspendUser: (userId: string) => Promise<void>;
  deleteUser: (userId: string) => Promise<void>;
  fetchAllRecipes: () => Promise<void>;
  setRecipeStatus: (recipeId: string, request: SetRecipeStatusRequest) => Promise<void>;
  deleteRecipe: (recipeId: string) => Promise<void>;
  clearError: () => void;
}

type AdminStore = AdminState & AdminActions;

export const useAdminStore = create<AdminStore>((set) => ({
  // 初期状態
  stats: null,
  recipes: [],
  users: [],
  loading: false,
  error: null,

  // 管理者ダッシュボード統計を取得
  fetchDashboardStats: async () => {
    set({ loading: true, error: null });
    try {
      const stats = await adminApi.getAdminDashboardStats();
      set({ stats, loading: false });
    } catch (error) {
      set({
        loading: false,
        error: (error as Error).message || '統計の取得に失敗しました',
      });
    }
  },

  // すべてのユーザーを取得
  fetchAllUsers: async () => {
    set({ loading: true, error: null });
    try {
      const users = await adminApi.getAllUsers();
      set({ users, loading: false });
    } catch (error) {
      set({
        loading: false,
        error: (error as Error).message || 'ユーザー一覧の取得に失敗しました',
      });
    }
  },

  // ユーザーを停止
  suspendUser: async (userId: string) => {
    set({ loading: true, error: null });
    try {
      await adminApi.suspendUser(userId);
      set({ loading: false });
    } catch (error) {
      set({
        loading: false,
        error: (error as Error).message || 'ユーザーの停止に失敗しました',
      });
      throw error;
    }
  },

  // ユーザーを削除
  deleteUser: async (userId: string) => {
    set({ loading: true, error: null });
    try {
      await adminApi.deleteUserByAdmin(userId);
      set({ loading: false });
    } catch (error) {
      set({
        loading: false,
        error: (error as Error).message || 'ユーザーの削除に失敗しました',
      });
      throw error;
    }
  },

  // すべてのレシピを取得
  fetchAllRecipes: async () => {
    set({ loading: true, error: null });
    try {
      const recipes = await adminApi.getAllRecipesForAdmin();
      set({ recipes, loading: false });
    } catch (error) {
      set({
        loading: false,
        error: (error as Error).message || 'レシピの取得に失敗しました',
      });
    }
  },

  // レシピのステータスを設定
  setRecipeStatus: async (recipeId: string, request: SetRecipeStatusRequest) => {
    set({ loading: true, error: null });
    try {
      const updatedRecipe = await adminApi.setRecipeStatus(recipeId, request);
      set((state) => ({
        recipes: state.recipes.map((r) => (r.recipeId === recipeId ? updatedRecipe : r)),
        loading: false,
      }));
    } catch (error) {
      set({
        loading: false,
        error: (error as Error).message || 'レシピステータスの設定に失敗しました',
      });
      throw error;
    }
  },

  // レシピを削除
  deleteRecipe: async (recipeId: string) => {
    set({ loading: true, error: null });
    try {
      await adminApi.deleteRecipeByAdmin(recipeId);
      set((state) => ({
        recipes: state.recipes.filter((r) => r.recipeId !== recipeId),
        loading: false,
      }));
    } catch (error) {
      set({
        loading: false,
        error: (error as Error).message || 'レシピの削除に失敗しました',
      });
      throw error;
    }
  },

  // エラーをクリア
  clearError: () => {
    set({ error: null });
  },
}));
