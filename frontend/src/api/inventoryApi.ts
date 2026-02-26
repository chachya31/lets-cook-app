import { apiDelete, apiGet, apiPost, apiPut } from '../utils/apiClient';

export interface InventoryItem {
  itemId: string;
  name: string;
  quantity: number;
  unit: string;
  expiryDate: string | null;
  isExpired: boolean;
  isExpiringSoon: boolean;
  purchasedAt: string;
  createdAt: string;
}

export interface AddInventoryItemRequest {
  name: string;
  quantity: number;
  unit: string;
  expiryDate?: string;
}

export interface UpdateInventoryItemRequest {
  quantity?: number;
  expiryDate?: string;
}

/**
 * 在庫一覧を取得
 */
export const getInventory = async (sortByExpiry = false): Promise<InventoryItem[]> => {
  const query = sortByExpiry ? '?sortByExpiry=true' : '';
  return apiGet<InventoryItem[]>(`/api/inventory${query}`);
};

/**
 * 在庫アイテムを追加
 */
export const addInventoryItem = async (request: AddInventoryItemRequest): Promise<InventoryItem> => {
  return apiPost<InventoryItem>('/api/inventory', request);
};

/**
 * 在庫アイテムを更新
 */
export const updateInventoryItem = async (
  itemId: string,
  request: UpdateInventoryItemRequest
): Promise<InventoryItem> => {
  return apiPut<InventoryItem>(`/api/inventory/${itemId}`, request);
};

/**
 * 在庫アイテムを削除
 */
export const deleteInventoryItem = async (itemId: string): Promise<void> => {
  return apiDelete<void>(`/api/inventory/${itemId}`);
};
