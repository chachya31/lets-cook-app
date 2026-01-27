import { act } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import * as shoppingListApi from '../../api/shoppingListApi';
import { useShoppingListStore } from '../../store/shoppingListStore';

vi.mock('../../api/shoppingListApi');

describe('shoppingListStore', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    useShoppingListStore.setState({
      items: [],
      loading: false,
      error: null,
    });
  });

  const mockItem = {
    itemId: 'item-1',
    userId: 'user-1',
    name: '牛乳',
    quantity: 1,
    unit: 'L',
    isChecked: false,
    isCheckedAt: null,
    addedAt: '2025-01-01T10:00:00Z',
    sourceRecipeId: null,
  };

  describe('fetchShoppingList', () => {
    it('should fetch shopping list successfully', async () => {
      const mockItems = [mockItem];
      vi.mocked(shoppingListApi.getShoppingList).mockResolvedValue(mockItems);

      await act(async () => {
        await useShoppingListStore.getState().fetchShoppingList('user-1');
      });

      const state = useShoppingListStore.getState();
      expect(state.items).toEqual(mockItems);
      expect(state.loading).toBe(false);
      expect(state.error).toBeNull();
    });

    it('should set error on fetch failure', async () => {
      vi.mocked(shoppingListApi.getShoppingList).mockRejectedValue(new Error('Failed to fetch'));

      await act(async () => {
        await useShoppingListStore.getState().fetchShoppingList('user-1');
      });

      const state = useShoppingListStore.getState();
      expect(state.error).toBe('Failed to fetch');
    });
  });

  describe('addItem', () => {
    it('should add item successfully', async () => {
      vi.mocked(shoppingListApi.addShoppingListItem).mockResolvedValue(mockItem);

      await act(async () => {
        await useShoppingListStore.getState().addItem('user-1', {
          name: '牛乳',
          quantity: 1,
          unit: 'L',
        });
      });

      const state = useShoppingListStore.getState();
      expect(state.items).toContainEqual(mockItem);
      expect(state.loading).toBe(false);
    });

    it('should update existing item if same itemId', async () => {
      useShoppingListStore.setState({ items: [mockItem] });
      const updatedItem = { ...mockItem, quantity: 2 };
      vi.mocked(shoppingListApi.addShoppingListItem).mockResolvedValue(updatedItem);

      await act(async () => {
        await useShoppingListStore.getState().addItem('user-1', {
          name: '牛乳',
          quantity: 2,
          unit: 'L',
        });
      });

      const state = useShoppingListStore.getState();
      expect(state.items).toHaveLength(1);
      expect(state.items[0].quantity).toBe(2);
    });
  });

  describe('updateItem', () => {
    it('should update item successfully', async () => {
      useShoppingListStore.setState({ items: [mockItem] });
      const checkedItem = { ...mockItem, isChecked: true };
      vi.mocked(shoppingListApi.updateShoppingListItem).mockResolvedValue(checkedItem);

      await act(async () => {
        await useShoppingListStore.getState().updateItem('user-1', 'item-1', {
          isChecked: true,
        });
      });

      const state = useShoppingListStore.getState();
      expect(state.items[0].isChecked).toBe(true);
    });
  });

  describe('deleteItem', () => {
    it('should delete item successfully', async () => {
      useShoppingListStore.setState({ items: [mockItem] });
      vi.mocked(shoppingListApi.deleteShoppingListItem).mockResolvedValue(undefined);

      await act(async () => {
        await useShoppingListStore.getState().deleteItem('user-1', 'item-1');
      });

      const state = useShoppingListStore.getState();
      expect(state.items).toHaveLength(0);
    });
  });

  describe('clearError', () => {
    it('should clear error', () => {
      useShoppingListStore.setState({ error: 'some error' });

      act(() => {
        useShoppingListStore.getState().clearError();
      });

      expect(useShoppingListStore.getState().error).toBeNull();
    });
  });
});
