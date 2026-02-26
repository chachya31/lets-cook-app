/**
 * Recipe Store (Zustand)
 * レシピ状態管理
 */

import { create } from 'zustand';
import * as recipeApi from '../api/recipeApi';
import { Recipe, RecipeRequest, RecipeSearchParams } from '../types/recipe';

interface RecipeState {
  recipes: Recipe[];
  currentRecipe: Recipe | null;
  loading: boolean;
  error: string | null;
}

interface RecipeActions {
  searchRecipes: (params?: RecipeSearchParams) => Promise<void>;
  fetchRecipe: (recipeId: string) => Promise<void>;
  createRecipe: (recipe: RecipeRequest) => Promise<Recipe>;
  updateRecipe: (recipeId: string, recipe: RecipeRequest) => Promise<Recipe>;
  deleteRecipe: (recipeId: string) => Promise<void>;
  uploadRecipeImage: (recipeId: string, file: File) => Promise<Recipe>;
  clearError: () => void;
  clearCurrentRecipe: () => void;
}

type RecipeStore = RecipeState & RecipeActions;

export const useRecipeStore = create<RecipeStore>((set, get) => ({
  // 初期状態
  recipes: [],
  currentRecipe: null,
  loading: false,
  error: null,

  // レシピ検索
  searchRecipes: async (params?: RecipeSearchParams) => {
    set({ loading: true, error: null });
    try {
      const recipes = await recipeApi.searchRecipes(params);
      set({ recipes, loading: false });
    } catch (error) {
      set({
        loading: false,
        error: (error as Error).message || 'Failed to search recipes',
      });
    }
  },

  // レシピ詳細取得
  fetchRecipe: async (recipeId: string) => {
    set({ loading: true, error: null });
    try {
      const recipe = await recipeApi.getRecipe(recipeId);
      set({ currentRecipe: recipe, loading: false });
    } catch (error) {
      set({
        loading: false,
        error: (error as Error).message || 'Failed to fetch recipe',
      });
    }
  },

  // レシピ作成
  createRecipe: async (recipe: RecipeRequest) => {
    set({ loading: true, error: null });
    try {
      const newRecipe = await recipeApi.createRecipe(recipe);
      set((state) => ({
        recipes: [...state.recipes, newRecipe],
        currentRecipe: newRecipe,
        loading: false,
      }));
      return newRecipe;
    } catch (error) {
      set({
        loading: false,
        error: (error as Error).message || 'Failed to create recipe',
      });
      throw error;
    }
  },

  // レシピ更新
  updateRecipe: async (recipeId: string, recipe: RecipeRequest) => {
    set({ loading: true, error: null });
    try {
      const updatedRecipe = await recipeApi.updateRecipe(recipeId, recipe);
      set((state) => ({
        recipes: state.recipes.map((r) => (r.recipeId === recipeId ? updatedRecipe : r)),
        currentRecipe: updatedRecipe,
        loading: false,
      }));
      return updatedRecipe;
    } catch (error) {
      set({
        loading: false,
        error: (error as Error).message || 'Failed to update recipe',
      });
      throw error;
    }
  },

  // レシピ削除
  deleteRecipe: async (recipeId: string) => {
    set({ loading: true, error: null });
    try {
      await recipeApi.deleteRecipe(recipeId);
      const state = get();
      set({
        recipes: state.recipes.filter((r) => r.recipeId !== recipeId),
        currentRecipe: state.currentRecipe?.recipeId === recipeId ? null : state.currentRecipe,
        loading: false,
      });
    } catch (error) {
      set({
        loading: false,
        error: (error as Error).message || 'Failed to delete recipe',
      });
      throw error;
    }
  },

  // レシピ画像アップロード
  uploadRecipeImage: async (recipeId: string, file: File) => {
    set({ loading: true, error: null });
    try {
      const updatedRecipe = await recipeApi.uploadRecipeImage(recipeId, file);
      set((state) => ({
        recipes: state.recipes.map((r) => (r.recipeId === recipeId ? updatedRecipe : r)),
        currentRecipe: updatedRecipe,
        loading: false,
      }));
      return updatedRecipe;
    } catch (error) {
      set({
        loading: false,
        error: (error as Error).message || 'Failed to upload recipe image',
      });
      throw error;
    }
  },

  // エラーをクリア
  clearError: () => {
    set({ error: null });
  },

  // 現在のレシピをクリア
  clearCurrentRecipe: () => {
    set({ currentRecipe: null });
  },
}));
