import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import { Recipe, RecipeRequest, RecipeSearchParams } from '../types/recipe';
import * as recipeApi from '../api/recipeApi';

interface RecipeState {
  recipes: Recipe[];
  currentRecipe: Recipe | null;
  loading: boolean;
  error: string | null;
}

const initialState: RecipeState = {
  recipes: [],
  currentRecipe: null,
  loading: false,
  error: null,
};

/**
 * レシピ検索
 */
export const searchRecipes = createAsyncThunk(
  'recipe/search',
  async (params: RecipeSearchParams | undefined) => {
    return await recipeApi.searchRecipes(params);
  }
);

/**
 * レシピ詳細取得
 */
export const fetchRecipe = createAsyncThunk(
  'recipe/fetch',
  async (recipeId: string) => {
    return await recipeApi.getRecipe(recipeId);
  }
);

/**
 * レシピ作成
 */
export const createRecipe = createAsyncThunk(
  'recipe/create',
  async ({ userId, recipe }: { userId: string; recipe: RecipeRequest }) => {
    return await recipeApi.createRecipe(userId, recipe);
  }
);

/**
 * レシピ更新
 */
export const updateRecipe = createAsyncThunk(
  'recipe/update',
  async ({ recipeId, userId, recipe }: { recipeId: string; userId: string; recipe: RecipeRequest }) => {
    return await recipeApi.updateRecipe(recipeId, userId, recipe);
  }
);

/**
 * レシピ削除
 */
export const deleteRecipe = createAsyncThunk(
  'recipe/delete',
  async ({ recipeId, userId }: { recipeId: string; userId: string }) => {
    await recipeApi.deleteRecipe(recipeId, userId);
    return recipeId;
  }
);

/**
 * レシピ画像アップロード
 */
export const uploadRecipeImage = createAsyncThunk(
  'recipe/uploadImage',
  async ({ recipeId, userId, file }: { recipeId: string; userId: string; file: File }) => {
    return await recipeApi.uploadRecipeImage(recipeId, userId, file);
  }
);

const recipeSlice = createSlice({
  name: 'recipe',
  initialState,
  reducers: {
    clearError: (state) => {
      state.error = null;
    },
    clearCurrentRecipe: (state) => {
      state.currentRecipe = null;
    },
  },
  extraReducers: (builder) => {
    // レシピ検索
    builder
      .addCase(searchRecipes.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(searchRecipes.fulfilled, (state, action: PayloadAction<Recipe[]>) => {
        state.loading = false;
        state.recipes = action.payload;
      })
      .addCase(searchRecipes.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to search recipes';
      });

    // レシピ詳細取得
    builder
      .addCase(fetchRecipe.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchRecipe.fulfilled, (state, action: PayloadAction<Recipe>) => {
        state.loading = false;
        state.currentRecipe = action.payload;
      })
      .addCase(fetchRecipe.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to fetch recipe';
      });

    // レシピ作成
    builder
      .addCase(createRecipe.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(createRecipe.fulfilled, (state, action: PayloadAction<Recipe>) => {
        state.loading = false;
        state.recipes.push(action.payload);
        state.currentRecipe = action.payload;
      })
      .addCase(createRecipe.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to create recipe';
      });

    // レシピ更新
    builder
      .addCase(updateRecipe.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(updateRecipe.fulfilled, (state, action: PayloadAction<Recipe>) => {
        state.loading = false;
        const index = state.recipes.findIndex((r) => r.recipeId === action.payload.recipeId);
        if (index !== -1) {
          state.recipes[index] = action.payload;
        }
        state.currentRecipe = action.payload;
      })
      .addCase(updateRecipe.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to update recipe';
      });

    // レシピ削除
    builder
      .addCase(deleteRecipe.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(deleteRecipe.fulfilled, (state, action: PayloadAction<string>) => {
        state.loading = false;
        state.recipes = state.recipes.filter((r) => r.recipeId !== action.payload);
        if (state.currentRecipe?.recipeId === action.payload) {
          state.currentRecipe = null;
        }
      })
      .addCase(deleteRecipe.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to delete recipe';
      });

    // レシピ画像アップロード
    builder
      .addCase(uploadRecipeImage.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(uploadRecipeImage.fulfilled, (state, action: PayloadAction<Recipe>) => {
        state.loading = false;
        const index = state.recipes.findIndex((r) => r.recipeId === action.payload.recipeId);
        if (index !== -1) {
          state.recipes[index] = action.payload;
        }
        state.currentRecipe = action.payload;
      })
      .addCase(uploadRecipeImage.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to upload recipe image';
      });
  },
});

export const { clearError, clearCurrentRecipe } = recipeSlice.actions;
export default recipeSlice.reducer;
