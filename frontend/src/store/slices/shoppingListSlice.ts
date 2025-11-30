import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import {
  ShoppingListItem,
  ShoppingListState,
  AddShoppingListItemRequest,
  UpdateShoppingListItemRequest,
} from '../../types/shoppingList';
import * as shoppingListApi from '../../api/shoppingListApi';

const initialState: ShoppingListState = {
  items: [],
  loading: false,
  error: null,
};

/**
 * 買い物リスト取得
 */
export const fetchShoppingList = createAsyncThunk(
  'shoppingList/fetchShoppingList',
  async (userId: string) => {
    return await shoppingListApi.getShoppingList(userId);
  }
);

/**
 * 買い物リストアイテム追加
 */
export const addShoppingListItem = createAsyncThunk(
  'shoppingList/addItem',
  async ({ userId, request }: { userId: string; request: AddShoppingListItemRequest }) => {
    return await shoppingListApi.addShoppingListItem(userId, request);
  }
);

/**
 * 買い物リストアイテム更新
 */
export const updateShoppingListItem = createAsyncThunk(
  'shoppingList/updateItem',
  async ({
    userId,
    itemId,
    request,
  }: {
    userId: string;
    itemId: string;
    request: UpdateShoppingListItemRequest;
  }) => {
    return await shoppingListApi.updateShoppingListItem(userId, itemId, request);
  }
);

/**
 * 買い物リストアイテム削除
 */
export const deleteShoppingListItem = createAsyncThunk(
  'shoppingList/deleteItem',
  async ({ userId, itemId }: { userId: string; itemId: string }) => {
    await shoppingListApi.deleteShoppingListItem(userId, itemId);
    return itemId;
  }
);

const shoppingListSlice = createSlice({
  name: 'shoppingList',
  initialState,
  reducers: {
    clearError: (state) => {
      state.error = null;
    },
  },
  extraReducers: (builder) => {
    builder
      // 買い物リスト取得
      .addCase(fetchShoppingList.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchShoppingList.fulfilled, (state, action: PayloadAction<ShoppingListItem[]>) => {
        state.loading = false;
        state.items = action.payload;
      })
      .addCase(fetchShoppingList.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to fetch shopping list';
      })
      // アイテム追加
      .addCase(addShoppingListItem.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(addShoppingListItem.fulfilled, (state, action: PayloadAction<ShoppingListItem>) => {
        state.loading = false;
        // 既存アイテムを更新または新規追加
        const existingIndex = state.items.findIndex((item) => item.itemId === action.payload.itemId);
        if (existingIndex >= 0) {
          state.items[existingIndex] = action.payload;
        } else {
          state.items.push(action.payload);
        }
      })
      .addCase(addShoppingListItem.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to add item';
      })
      // アイテム更新
      .addCase(updateShoppingListItem.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(updateShoppingListItem.fulfilled, (state, action: PayloadAction<ShoppingListItem>) => {
        state.loading = false;
        const index = state.items.findIndex((item) => item.itemId === action.payload.itemId);
        if (index >= 0) {
          state.items[index] = action.payload;
        }
      })
      .addCase(updateShoppingListItem.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to update item';
      })
      // アイテム削除
      .addCase(deleteShoppingListItem.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(deleteShoppingListItem.fulfilled, (state, action: PayloadAction<string>) => {
        state.loading = false;
        state.items = state.items.filter((item) => item.itemId !== action.payload);
      })
      .addCase(deleteShoppingListItem.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to delete item';
      });
  },
});

export const { clearError } = shoppingListSlice.actions;
export default shoppingListSlice.reducer;
