/**
 * ShoppingList Store (Zustand)
 * 買い物リスト状態管理
 */

import { create } from 'zustand';
import * as shoppingListApi from '../api/shoppingListApi';
import {
  AddShoppingListItemRequest,
  ShoppingListItem,
  UpdateShoppingListItemRequest,
} from '../types/shoppingList';

interface ShoppingListState {
  items: ShoppingListItem[];
  loading: boolean;
  error: string | null;
}

interface ShoppingListActions {
  fetchShoppingList: () => Promise<void>;
  addItem: (request: AddShoppingListItemRequest) => Promise<void>;
  updateItem: (itemId: string, request: UpdateShoppingListItemRequest) => Promise<void>;
  deleteItem: (itemId: string) => Promise<void>;
  clearError: () => void;
}

type ShoppingListStore = ShoppingListState & ShoppingListActions;

export const useShoppingListStore = create<ShoppingListStore>((set) => ({
  // 初期状態
  items: [],
  loading: false,
  error: null,

  // 買い物リスト取得
  fetchShoppingList: async () => {
    set({ loading: true, error: null });
    try {
      const items = await shoppingListApi.getShoppingList();
      set({ items, loading: false });
    } catch (error) {
      set({
        loading: false,
        error: (error as Error).message || 'Failed to fetch shopping list',
      });
    }
  },

  // アイテム追加
  addItem: async (request: AddShoppingListItemRequest) => {
    set({ loading: true, error: null });
    try {
      const newItem = await shoppingListApi.addShoppingListItem(request);
      set((state) => {
        // 既存アイテムを更新または新規追加
        const existingIndex = state.items.findIndex((item) => item.itemId === newItem.itemId);
        if (existingIndex >= 0) {
          const updatedItems = [...state.items];
          updatedItems[existingIndex] = newItem;
          return { items: updatedItems, loading: false };
        }
        return { items: [...state.items, newItem], loading: false };
      });
    } catch (error) {
      set({
        loading: false,
        error: (error as Error).message || 'Failed to add item',
      });
      throw error;
    }
  },

  // アイテム更新
  updateItem: async (itemId: string, request: UpdateShoppingListItemRequest) => {
    set({ loading: true, error: null });
    try {
      const updatedItem = await shoppingListApi.updateShoppingListItem(itemId, request);
      set((state) => ({
        items: state.items.map((item) => (item.itemId === itemId ? updatedItem : item)),
        loading: false,
      }));
    } catch (error) {
      set({
        loading: false,
        error: (error as Error).message || 'Failed to update item',
      });
      throw error;
    }
  },

  // アイテム削除
  deleteItem: async (itemId: string) => {
    set({ loading: true, error: null });
    try {
      await shoppingListApi.deleteShoppingListItem(itemId);
      set((state) => ({
        items: state.items.filter((item) => item.itemId !== itemId),
        loading: false,
      }));
    } catch (error) {
      set({
        loading: false,
        error: (error as Error).message || 'Failed to delete item',
      });
      throw error;
    }
  },

  // エラーをクリア
  clearError: () => {
    set({ error: null });
  },
}));
