import { createAsyncThunk, createSlice, PayloadAction } from '@reduxjs/toolkit';
import * as adminApi from '../../api/adminApi';
import { AdminDashboardStats, AdminState, SetRecipeStatusRequest } from '../../types/admin';
import { Recipe } from '../../types/recipe';
import { User } from '../../types/user';

const initialState: AdminState = {
  stats: null,
  recipes: [],
  users: [],
  loading: false,
  error: null,
};

/**
 * 管理者ダッシュボード統計を取得
 */
export const fetchAdminDashboardStats = createAsyncThunk('admin/fetchDashboardStats', async () => {
  return await adminApi.getAdminDashboardStats();
});

/**
 * すべてのユーザーを取得（管理者用）
 */
export const fetchAllUsers = createAsyncThunk('admin/fetchAllUsers', async () => {
  return await adminApi.getAllUsers();
});

/**
 * ユーザーを停止
 */
export const suspendUser = createAsyncThunk('admin/suspendUser', async (userId: string) => {
  await adminApi.suspendUser(userId);
  return userId;
});

/**
 * ユーザーを削除
 */
export const deleteUserByAdmin = createAsyncThunk('admin/deleteUser', async (userId: string) => {
  await adminApi.deleteUserByAdmin(userId);
  return userId;
});

/**
 * すべてのレシピを取得（管理者用）
 */
export const fetchAllRecipesForAdmin = createAsyncThunk('admin/fetchAllRecipes', async () => {
  return await adminApi.getAllRecipesForAdmin();
});

/**
 * レシピのステータスを設定
 */
export const setRecipeStatus = createAsyncThunk(
  'admin/setRecipeStatus',
  async ({ recipeId, request }: { recipeId: string; request: SetRecipeStatusRequest }) => {
    return await adminApi.setRecipeStatus(recipeId, request);
  }
);

/**
 * レシピを削除（管理者用）
 */
export const deleteRecipeByAdmin = createAsyncThunk(
  'admin/deleteRecipe',
  async (recipeId: string) => {
    await adminApi.deleteRecipeByAdmin(recipeId);
    return recipeId;
  }
);

const adminSlice = createSlice({
  name: 'admin',
  initialState,
  reducers: {
    clearError: (state) => {
      state.error = null;
    },
  },
  extraReducers: (builder) => {
    builder
      // 管理者ダッシュボード統計取得
      .addCase(fetchAdminDashboardStats.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(
        fetchAdminDashboardStats.fulfilled,
        (state, action: PayloadAction<AdminDashboardStats>) => {
          state.loading = false;
          state.stats = action.payload;
        }
      )
      .addCase(fetchAdminDashboardStats.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || '統計の取得に失敗しました';
      })
      // ユーザー一覧取得
      .addCase(fetchAllUsers.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchAllUsers.fulfilled, (state, action: PayloadAction<User[]>) => {
        state.loading = false;
        state.users = action.payload;
      })
      .addCase(fetchAllUsers.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'ユーザー一覧の取得に失敗しました';
      })
      // ユーザー停止
      .addCase(suspendUser.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(suspendUser.fulfilled, (state) => {
        state.loading = false;
      })
      .addCase(suspendUser.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'ユーザーの停止に失敗しました';
      })
      // ユーザー削除
      .addCase(deleteUserByAdmin.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(deleteUserByAdmin.fulfilled, (state) => {
        state.loading = false;
      })
      .addCase(deleteUserByAdmin.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'ユーザーの削除に失敗しました';
      })
      // すべてのレシピ取得
      .addCase(fetchAllRecipesForAdmin.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchAllRecipesForAdmin.fulfilled, (state, action: PayloadAction<Recipe[]>) => {
        state.loading = false;
        state.recipes = action.payload;
      })
      .addCase(fetchAllRecipesForAdmin.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'レシピの取得に失敗しました';
      })
      // レシピステータス設定
      .addCase(setRecipeStatus.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(setRecipeStatus.fulfilled, (state, action: PayloadAction<Recipe>) => {
        state.loading = false;
        const index = state.recipes.findIndex((r) => r.recipeId === action.payload.recipeId);
        if (index !== -1) {
          state.recipes[index] = action.payload;
        }
      })
      .addCase(setRecipeStatus.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'レシピステータスの設定に失敗しました';
      })
      // レシピ削除
      .addCase(deleteRecipeByAdmin.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(deleteRecipeByAdmin.fulfilled, (state, action: PayloadAction<string>) => {
        state.loading = false;
        state.recipes = state.recipes.filter((r) => r.recipeId !== action.payload);
      })
      .addCase(deleteRecipeByAdmin.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'レシピの削除に失敗しました';
      });
  },
});

export const { clearError } = adminSlice.actions;
export default adminSlice.reducer;
