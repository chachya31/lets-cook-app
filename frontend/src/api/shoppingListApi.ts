import { ShoppingListItem, AddShoppingListItemRequest, UpdateShoppingListItemRequest } from '../types/shoppingList';
import { apiGet, apiPost, apiPut, apiDelete } from '../utils/apiClient';

/**
 * 買い物リスト取得
 */
export const getShoppingList = async (userId: string): Promise<ShoppingListItem[]> => {
  return apiGet<ShoppingListItem[]>('/api/shopping-lists', userId);
};

/**
 * 買い物リストアイテム追加
 */
export const addShoppingListItem = async (
  userId: string,
  request: AddShoppingListItemRequest
): Promise<ShoppingListItem> => {
  return apiPost<ShoppingListItem>('/api/shopping-lists', request, userId);
};

/**
 * 買い物リストアイテム更新
 */
export const updateShoppingListItem = async (
  userId: string,
  itemId: string,
  request: UpdateShoppingListItemRequest
): Promise<ShoppingListItem> => {
  return apiPut<ShoppingListItem>(`/api/shopping-lists/${itemId}`, request, userId);
};

/**
 * 買い物リストアイテム削除
 */
export const deleteShoppingListItem = async (userId: string, itemId: string): Promise<void> => {
  return apiDelete<void>(`/api/shopping-lists/${itemId}`, userId);
};
