/**
 * Admin Store Tests
 */

import { beforeEach, describe, expect, it, vi } from 'vitest';
import * as adminApi from '../../api/adminApi';
import { useAdminStore } from '../../store/adminStore';
import { AdminDashboardStats } from '../../types/admin';
import { Recipe } from '../../types/recipe';
import { User } from '../../types/user';

// adminApiをモック
vi.mock('../../api/adminApi');

const mockStats: AdminDashboardStats = {
  message: 'Success',
  totalUsers: 100,
  totalRecipes: 500,
};

const mockUsers: User[] = [
  {
    userId: 'user-1',
    email: 'user1@example.com',
    nickname: 'User1',
    displayName: 'User One',
    preferredLanguage: 'ja',
    createdAt: '2024-01-01T00:00:00Z',
    timezone: 'Asia/Tokyo',
    marketingOptOut: false,
  },
  {
    userId: 'user-2',
    email: 'user2@example.com',
    nickname: 'User2',
    displayName: 'User Two',
    preferredLanguage: 'ko',
    createdAt: '2024-01-02T00:00:00Z',
    timezone: 'Asia/Seoul',
    marketingOptOut: false,
  },
];

const mockRecipes: Recipe[] = [
  {
    recipeId: 'recipe-1',
    authorId: 'user-1',
    title: 'Test Recipe 1',
    ingredients: [],
    steps: [],
    cookingTime: 20,
    isPublic: true,
    createdAt: '2024-01-01T00:00:00Z',
    updatedAt: '2024-01-01T00:00:00Z',
  },
  {
    recipeId: 'recipe-2',
    authorId: 'user-2',
    title: 'Test Recipe 2',
    ingredients: [],
    steps: [],
    cookingTime: 30,
    isPublic: true,
    createdAt: '2024-01-02T00:00:00Z',
    updatedAt: '2024-01-02T00:00:00Z',
  },
];

describe('adminStore', () => {
  beforeEach(() => {
    // ストアをリセット
    useAdminStore.setState({
      stats: null,
      recipes: [],
      users: [],
      loading: false,
      error: null,
    });
    vi.clearAllMocks();
  });

  describe('fetchDashboardStats', () => {
    it('統計を正常に取得できる', async () => {
      vi.mocked(adminApi.getAdminDashboardStats).mockResolvedValue(mockStats);

      await useAdminStore.getState().fetchDashboardStats();

      const state = useAdminStore.getState();
      expect(state.stats).toEqual(mockStats);
      expect(state.loading).toBe(false);
      expect(state.error).toBeNull();
    });

    it('統計取得失敗時にエラーを設定する', async () => {
      vi.mocked(adminApi.getAdminDashboardStats).mockRejectedValue(new Error('API Error'));

      await useAdminStore.getState().fetchDashboardStats();

      const state = useAdminStore.getState();
      expect(state.stats).toBeNull();
      expect(state.loading).toBe(false);
      expect(state.error).toBe('API Error');
    });
  });

  describe('fetchAllUsers', () => {
    it('ユーザー一覧を正常に取得できる', async () => {
      vi.mocked(adminApi.getAllUsers).mockResolvedValue(mockUsers);

      await useAdminStore.getState().fetchAllUsers();

      const state = useAdminStore.getState();
      expect(state.users).toEqual(mockUsers);
      expect(state.loading).toBe(false);
      expect(state.error).toBeNull();
    });

    it('ユーザー一覧取得失敗時にエラーを設定する', async () => {
      vi.mocked(adminApi.getAllUsers).mockRejectedValue(new Error('Fetch failed'));

      await useAdminStore.getState().fetchAllUsers();

      const state = useAdminStore.getState();
      expect(state.users).toEqual([]);
      expect(state.error).toBe('Fetch failed');
    });
  });

  describe('suspendUser', () => {
    it('ユーザーを正常に停止できる', async () => {
      vi.mocked(adminApi.suspendUser).mockResolvedValue(undefined);

      await useAdminStore.getState().suspendUser('user-1');

      expect(adminApi.suspendUser).toHaveBeenCalledWith('user-1');
      expect(useAdminStore.getState().loading).toBe(false);
    });

    it('ユーザー停止失敗時にエラーをスローする', async () => {
      vi.mocked(adminApi.suspendUser).mockRejectedValue(new Error('Suspend failed'));

      await expect(useAdminStore.getState().suspendUser('user-1')).rejects.toThrow(
        'Suspend failed'
      );
      expect(useAdminStore.getState().error).toBe('Suspend failed');
    });
  });

  describe('deleteUser', () => {
    it('ユーザーを正常に削除できる', async () => {
      vi.mocked(adminApi.deleteUserByAdmin).mockResolvedValue(undefined);

      await useAdminStore.getState().deleteUser('user-1');

      expect(adminApi.deleteUserByAdmin).toHaveBeenCalledWith('user-1');
      expect(useAdminStore.getState().loading).toBe(false);
    });

    it('ユーザー削除失敗時にエラーをスローする', async () => {
      vi.mocked(adminApi.deleteUserByAdmin).mockRejectedValue(new Error('Delete failed'));

      await expect(useAdminStore.getState().deleteUser('user-1')).rejects.toThrow('Delete failed');
      expect(useAdminStore.getState().error).toBe('Delete failed');
    });
  });

  describe('fetchAllRecipes', () => {
    it('レシピ一覧を正常に取得できる', async () => {
      vi.mocked(adminApi.getAllRecipesForAdmin).mockResolvedValue(mockRecipes);

      await useAdminStore.getState().fetchAllRecipes();

      const state = useAdminStore.getState();
      expect(state.recipes).toEqual(mockRecipes);
      expect(state.loading).toBe(false);
      expect(state.error).toBeNull();
    });

    it('レシピ一覧取得失敗時にエラーを設定する', async () => {
      vi.mocked(adminApi.getAllRecipesForAdmin).mockRejectedValue(new Error('Fetch failed'));

      await useAdminStore.getState().fetchAllRecipes();

      const state = useAdminStore.getState();
      expect(state.recipes).toEqual([]);
      expect(state.error).toBe('Fetch failed');
    });
  });

  describe('setRecipeStatus', () => {
    it('レシピステータスを正常に設定できる', async () => {
      const updatedRecipe = { ...mockRecipes[0], isPublic: false };
      vi.mocked(adminApi.setRecipeStatus).mockResolvedValue(updatedRecipe);

      useAdminStore.setState({ recipes: mockRecipes });

      await useAdminStore.getState().setRecipeStatus('recipe-1', { isPublic: false });

      const state = useAdminStore.getState();
      expect(state.recipes[0]).toEqual(updatedRecipe);
      expect(state.loading).toBe(false);
    });

    it('レシピステータス設定失敗時にエラーをスローする', async () => {
      vi.mocked(adminApi.setRecipeStatus).mockRejectedValue(new Error('Status update failed'));

      await expect(
        useAdminStore.getState().setRecipeStatus('recipe-1', { isPublic: false })
      ).rejects.toThrow('Status update failed');
      expect(useAdminStore.getState().error).toBe('Status update failed');
    });
  });

  describe('deleteRecipe', () => {
    it('レシピを正常に削除できる', async () => {
      vi.mocked(adminApi.deleteRecipeByAdmin).mockResolvedValue(undefined);

      useAdminStore.setState({ recipes: mockRecipes });

      await useAdminStore.getState().deleteRecipe('recipe-1');

      const state = useAdminStore.getState();
      expect(state.recipes).toHaveLength(1);
      expect(state.recipes[0].recipeId).toBe('recipe-2');
    });

    it('レシピ削除失敗時にエラーをスローする', async () => {
      vi.mocked(adminApi.deleteRecipeByAdmin).mockRejectedValue(new Error('Delete failed'));

      await expect(useAdminStore.getState().deleteRecipe('recipe-1')).rejects.toThrow(
        'Delete failed'
      );
      expect(useAdminStore.getState().error).toBe('Delete failed');
    });
  });

  describe('clearError', () => {
    it('エラーをクリアできる', () => {
      useAdminStore.setState({ error: 'Some error' });

      useAdminStore.getState().clearError();

      expect(useAdminStore.getState().error).toBeNull();
    });
  });
});
