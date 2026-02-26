import {
  AddShoppingListItemRequest,
  ShoppingListItem,
  UpdateShoppingListItemRequest,
} from '../types/shoppingList';
import { apiDelete, apiGet, apiPost, apiPut } from '../utils/apiClient';

/**
 * 買い物リスト取得
 */
export const getShoppingList = async (): Promise<ShoppingListItem[]> => {
  return apiGet<ShoppingListItem[]>('/api/shopping-lists');
};

/**
 * 買い物リストアイテム追加
 */
export const addShoppingListItem = async (
  request: AddShoppingListItemRequest
): Promise<ShoppingListItem> => {
  return apiPost<ShoppingListItem>('/api/shopping-lists', request);
};

/**
 * 買い物リストアイテム更新
 */
export const updateShoppingListItem = async (
  itemId: string,
  request: UpdateShoppingListItemRequest
): Promise<ShoppingListItem> => {
  return apiPut<ShoppingListItem>(`/api/shopping-lists/${itemId}`, request);
};

/**
 * 買い物リストアイテム削除
 */
export const deleteShoppingListItem = async (itemId: string): Promise<void> => {
  return apiDelete<void>(`/api/shopping-lists/${itemId}`);
};
