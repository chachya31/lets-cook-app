import { ShoppingListItem, AddShoppingListItemRequest, UpdateShoppingListItemRequest } from '../types/shoppingList';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

/**
 * 買い物リスト取得
 */
export const getShoppingList = async (userId: string): Promise<ShoppingListItem[]> => {
  const response = await fetch(`${API_BASE_URL}/api/shopping-lists`, {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
      'X-User-Id': userId,
    },
  });

  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message || 'Failed to fetch shopping list');
  }

  return response.json();
};

/**
 * 買い物リストアイテム追加
 */
export const addShoppingListItem = async (
  userId: string,
  request: AddShoppingListItemRequest
): Promise<ShoppingListItem> => {
  const response = await fetch(`${API_BASE_URL}/api/shopping-lists`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'X-User-Id': userId,
    },
    body: JSON.stringify(request),
  });

  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message || 'Failed to add item');
  }

  return response.json();
};

/**
 * 買い物リストアイテム更新
 */
export const updateShoppingListItem = async (
  userId: string,
  itemId: string,
  request: UpdateShoppingListItemRequest
): Promise<ShoppingListItem> => {
  const response = await fetch(`${API_BASE_URL}/api/shopping-lists/${itemId}`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
      'X-User-Id': userId,
    },
    body: JSON.stringify(request),
  });

  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message || 'Failed to update item');
  }

  return response.json();
};

/**
 * 買い物リストアイテム削除
 */
export const deleteShoppingListItem = async (userId: string, itemId: string): Promise<void> => {
  const response = await fetch(`${API_BASE_URL}/api/shopping-lists/${itemId}`, {
    method: 'DELETE',
    headers: {
      'Content-Type': 'application/json',
      'X-User-Id': userId,
    },
  });

  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message || 'Failed to delete item');
  }
};
