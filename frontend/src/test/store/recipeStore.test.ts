/**
 * Recipe Store Tests
 */

import { beforeEach, describe, expect, it, vi } from 'vitest';
import * as recipeApi from '../../api/recipeApi';
import { useRecipeStore } from '../../store/recipeStore';
import { Recipe } from '../../types/recipe';

// recipeApiをモック
vi.mock('../../api/recipeApi');

const mockRecipes: Recipe[] = [
  {
    recipeId: 'recipe-1',
    authorId: 'user-1',
    title: 'Test Recipe 1',
    ingredients: [{ name: 'Ingredient 1', optional: false }],
    steps: [{ description: 'Step 1' }],
    cookingTime: 20,
    isPublic: true,
    createdAt: '2024-01-01T00:00:00Z',
    updatedAt: '2024-01-01T00:00:00Z',
  },
  {
    recipeId: 'recipe-2',
    authorId: 'user-2',
    title: 'Test Recipe 2',
    ingredients: [{ name: 'Ingredient 2', optional: false }],
    steps: [{ description: 'Step 2' }],
    cookingTime: 30,
    isPublic: true,
    createdAt: '2024-01-02T00:00:00Z',
    updatedAt: '2024-01-02T00:00:00Z',
  },
];

describe('recipeStore', () => {
  beforeEach(() => {
    // ストアをリセット
    useRecipeStore.setState({
      recipes: [],
      currentRecipe: null,
      loading: false,
      error: null,
    });
    vi.clearAllMocks();
  });

  describe('searchRecipes', () => {
    it('レシピを正常に検索できる', async () => {
      vi.mocked(recipeApi.searchRecipes).mockResolvedValue(mockRecipes);

      await useRecipeStore.getState().searchRecipes({});

      const state = useRecipeStore.getState();
      expect(state.recipes).toEqual(mockRecipes);
      expect(state.loading).toBe(false);
      expect(state.error).toBeNull();
    });

    it('キーワードでレシピを検索できる', async () => {
      vi.mocked(recipeApi.searchRecipes).mockResolvedValue([mockRecipes[0]]);

      await useRecipeStore.getState().searchRecipes({ keyword: 'Test' });

      expect(recipeApi.searchRecipes).toHaveBeenCalledWith({ keyword: 'Test' });
      expect(useRecipeStore.getState().recipes).toHaveLength(1);
    });

    it('検索失敗時にエラーを設定する', async () => {
      vi.mocked(recipeApi.searchRecipes).mockRejectedValue(new Error('Search failed'));

      await useRecipeStore.getState().searchRecipes({});

      const state = useRecipeStore.getState();
      expect(state.recipes).toEqual([]);
      expect(state.error).toBe('Search failed');
    });
  });

  describe('fetchRecipe', () => {
    it('レシピ詳細を正常に取得できる', async () => {
      vi.mocked(recipeApi.getRecipe).mockResolvedValue(mockRecipes[0]);

      await useRecipeStore.getState().fetchRecipe('recipe-1');

      const state = useRecipeStore.getState();
      expect(state.currentRecipe).toEqual(mockRecipes[0]);
      expect(state.loading).toBe(false);
    });

    it('レシピ取得失敗時にエラーを設定する', async () => {
      vi.mocked(recipeApi.getRecipe).mockRejectedValue(new Error('Not found'));

      await useRecipeStore.getState().fetchRecipe('invalid-id');

      const state = useRecipeStore.getState();
      expect(state.currentRecipe).toBeNull();
      expect(state.error).toBe('Not found');
    });
  });

  describe('createRecipe', () => {
    it('レシピを正常に作成できる', async () => {
      const newRecipe = mockRecipes[0];
      vi.mocked(recipeApi.createRecipe).mockResolvedValue(newRecipe);

      const result = await useRecipeStore.getState().createRecipe('user-1', {
        title: 'Test Recipe 1',
        ingredients: [{ name: 'Ingredient 1', optional: false }],
        steps: [{ description: 'Step 1' }],
        cookingTime: 20,
      });

      const state = useRecipeStore.getState();
      expect(result).toEqual(newRecipe);
      expect(state.recipes).toContainEqual(newRecipe);
      expect(state.currentRecipe).toEqual(newRecipe);
    });

    it('レシピ作成失敗時にエラーをスローする', async () => {
      vi.mocked(recipeApi.createRecipe).mockRejectedValue(new Error('Create failed'));

      await expect(
        useRecipeStore.getState().createRecipe('user-1', {
          title: 'Test',
          ingredients: [],
          steps: [],
          cookingTime: 10,
        })
      ).rejects.toThrow('Create failed');
      expect(useRecipeStore.getState().error).toBe('Create failed');
    });
  });

  describe('updateRecipe', () => {
    it('レシピを正常に更新できる', async () => {
      const updatedRecipe = { ...mockRecipes[0], title: 'Updated Title' };
      vi.mocked(recipeApi.updateRecipe).mockResolvedValue(updatedRecipe);

      useRecipeStore.setState({ recipes: mockRecipes });

      const result = await useRecipeStore.getState().updateRecipe('recipe-1', 'user-1', {
        title: 'Updated Title',
        ingredients: mockRecipes[0].ingredients,
        steps: mockRecipes[0].steps,
        cookingTime: 20,
      });

      const state = useRecipeStore.getState();
      expect(result).toEqual(updatedRecipe);
      expect(state.recipes[0].title).toBe('Updated Title');
      expect(state.currentRecipe).toEqual(updatedRecipe);
    });

    it('レシピ更新失敗時にエラーをスローする', async () => {
      vi.mocked(recipeApi.updateRecipe).mockRejectedValue(new Error('Update failed'));

      await expect(
        useRecipeStore.getState().updateRecipe('recipe-1', 'user-1', {
          title: 'Test',
          ingredients: [],
          steps: [],
          cookingTime: 10,
        })
      ).rejects.toThrow('Update failed');
    });
  });

  describe('deleteRecipe', () => {
    it('レシピを正常に削除できる', async () => {
      vi.mocked(recipeApi.deleteRecipe).mockResolvedValue(undefined);

      useRecipeStore.setState({ recipes: mockRecipes, currentRecipe: mockRecipes[0] });

      await useRecipeStore.getState().deleteRecipe('recipe-1', 'user-1');

      const state = useRecipeStore.getState();
      expect(state.recipes).toHaveLength(1);
      expect(state.recipes[0].recipeId).toBe('recipe-2');
      expect(state.currentRecipe).toBeNull();
    });

    it('レシピ削除失敗時にエラーをスローする', async () => {
      vi.mocked(recipeApi.deleteRecipe).mockRejectedValue(new Error('Delete failed'));

      await expect(useRecipeStore.getState().deleteRecipe('recipe-1', 'user-1')).rejects.toThrow(
        'Delete failed'
      );
    });
  });

  describe('uploadRecipeImage', () => {
    it('レシピ画像を正常にアップロードできる', async () => {
      const updatedRecipe = { ...mockRecipes[0], imageUrl: 'https://example.com/image.jpg' };
      vi.mocked(recipeApi.uploadRecipeImage).mockResolvedValue(updatedRecipe);

      useRecipeStore.setState({ recipes: mockRecipes });

      const file = new File(['test'], 'test.jpg', { type: 'image/jpeg' });
      const result = await useRecipeStore.getState().uploadRecipeImage('recipe-1', 'user-1', file);

      expect(result).toEqual(updatedRecipe);
      expect(useRecipeStore.getState().recipes[0].imageUrl).toBe('https://example.com/image.jpg');
    });
  });

  describe('clearError', () => {
    it('エラーをクリアできる', () => {
      useRecipeStore.setState({ error: 'Some error' });

      useRecipeStore.getState().clearError();

      expect(useRecipeStore.getState().error).toBeNull();
    });
  });

  describe('clearCurrentRecipe', () => {
    it('現在のレシピをクリアできる', () => {
      useRecipeStore.setState({ currentRecipe: mockRecipes[0] });

      useRecipeStore.getState().clearCurrentRecipe();

      expect(useRecipeStore.getState().currentRecipe).toBeNull();
    });
  });
});
