/**
 * 買い物リスト型定義
 */

export interface ShoppingListItem {
  itemId: string;
  userId: string;
  name: string;
  quantity: number;
  unit: string;
  isChecked: boolean;
  isCheckedAt: string | null;
  addedAt: string;
  sourceRecipeId: string | null;
}

export interface AddShoppingListItemRequest {
  name: string;
  quantity: number;
  unit: string;
  sourceRecipeId?: string;
}

export interface UpdateShoppingListItemRequest {
  isChecked: boolean;
}

export interface ShoppingListState {
  items: ShoppingListItem[];
  loading: boolean;
  error: string | null;
}
