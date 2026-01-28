import { describe, it, expect, vi, beforeEach } from 'vitest'
import {
  getAllRecipes,
  getRecipe,
  getRecipesByAuthor,
  createRecipe,
  updateRecipe,
  deleteRecipe,
  uploadImage,
} from './recipeApi'
import type { Recipe, RecipeFormData } from '../types'

// apiClientとapiPostMultipartをモック
vi.mock('@/lib/axios', () => ({
  apiClient: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
  },
  apiPostMultipart: vi.fn(),
}))

import { apiClient, apiPostMultipart } from '@/lib/axios'

const mockRecipe: Recipe = {
  recipeId: 'recipe-123',
  title: 'テストレシピ',
  authorId: 'user-123',
  ingredients: [{ name: 'トマト', quantity: '2', unit: '個' }],
  steps: [{ stepNumber: 1, description: 'トマトを切る' }],
  cookingTime: 30,
  isPublic: true,
  imageUrl: 'https://example.com/image.jpg',
  createdAt: '2026-01-01T00:00:00Z',
  updatedAt: '2026-01-01T00:00:00Z',
}

const mockFormData: RecipeFormData = {
  title: 'テストレシピ',
  cookingTime: 30,
  isPublic: true,
  ingredients: [{ name: 'トマト', quantity: '2', unit: '個' }],
  steps: [{ description: 'トマトを切る' }],
  imageKey: 'images/test.jpg',
}

describe('recipeApi', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('getAllRecipes', () => {
    it('正しいエンドポイント（/recipes）にGETリクエストを送ること', async () => {
      vi.mocked(apiClient.get).mockResolvedValue({ data: [mockRecipe] })

      await getAllRecipes()

      expect(apiClient.get).toHaveBeenCalledTimes(1)
      expect(apiClient.get).toHaveBeenCalledWith('/recipes')
    })

    it('APIレスポンスのdataを返すこと', async () => {
      vi.mocked(apiClient.get).mockResolvedValue({ data: [mockRecipe] })

      const result = await getAllRecipes()

      expect(result).toEqual([mockRecipe])
    })

    it('APIエラー時に例外がスローされること', async () => {
      const error = new Error('Network Error')
      vi.mocked(apiClient.get).mockRejectedValue(error)

      await expect(getAllRecipes()).rejects.toThrow('Network Error')
    })
  })

  describe('getRecipe', () => {
    it('正しいエンドポイント（/recipes/:id）にGETリクエストを送ること', async () => {
      vi.mocked(apiClient.get).mockResolvedValue({ data: mockRecipe })

      await getRecipe('recipe-123')

      expect(apiClient.get).toHaveBeenCalledTimes(1)
      expect(apiClient.get).toHaveBeenCalledWith('/recipes/recipe-123')
    })

    it('APIレスポンスのdataを返すこと', async () => {
      vi.mocked(apiClient.get).mockResolvedValue({ data: mockRecipe })

      const result = await getRecipe('recipe-123')

      expect(result).toEqual(mockRecipe)
    })
  })

  describe('getRecipesByAuthor', () => {
    it('正しいエンドポイントとパラメータでGETリクエストを送ること', async () => {
      vi.mocked(apiClient.get).mockResolvedValue({ data: [mockRecipe] })

      await getRecipesByAuthor('user-123')

      expect(apiClient.get).toHaveBeenCalledTimes(1)
      expect(apiClient.get).toHaveBeenCalledWith('/recipes', {
        params: { authorId: 'user-123' },
      })
    })
  })

  describe('createRecipe', () => {
    it('正しいエンドポイント（/recipes）にPOSTリクエストを送ること', async () => {
      vi.mocked(apiClient.post).mockResolvedValue({ data: mockRecipe })

      await createRecipe(mockFormData)

      expect(apiClient.post).toHaveBeenCalledTimes(1)
      expect(apiClient.post).toHaveBeenCalledWith('/recipes', expect.any(Object))
    })

    it('フォームデータを正しいJSON形式に変換してPOSTすること', async () => {
      vi.mocked(apiClient.post).mockResolvedValue({ data: mockRecipe })

      await createRecipe(mockFormData)

      expect(apiClient.post).toHaveBeenCalledWith('/recipes', {
        title: 'テストレシピ',
        cookingTime: 30,
        isPublic: true,
        ingredients: [{ name: 'トマト', quantity: '2', unit: '個' }],
        steps: [{ stepNumber: 1, description: 'トマトを切る', imageUrl: undefined, videoUrl: undefined }],
        imageKey: 'images/test.jpg',
      })
    })

    it('APIレスポンスのdataを返すこと', async () => {
      vi.mocked(apiClient.post).mockResolvedValue({ data: mockRecipe })

      const result = await createRecipe(mockFormData)

      expect(result).toEqual(mockRecipe)
    })
  })

  describe('updateRecipe', () => {
    it('正しいエンドポイント（/recipes/:id）にPUTリクエストを送ること', async () => {
      vi.mocked(apiClient.put).mockResolvedValue({ data: mockRecipe })

      await updateRecipe('recipe-123', mockFormData)

      expect(apiClient.put).toHaveBeenCalledTimes(1)
      expect(apiClient.put).toHaveBeenCalledWith('/recipes/recipe-123', expect.any(Object))
    })

    it('フォームデータを正しいJSON形式に変換してPUTすること', async () => {
      vi.mocked(apiClient.put).mockResolvedValue({ data: mockRecipe })

      await updateRecipe('recipe-123', mockFormData)

      expect(apiClient.put).toHaveBeenCalledWith('/recipes/recipe-123', {
        title: 'テストレシピ',
        cookingTime: 30,
        isPublic: true,
        ingredients: [{ name: 'トマト', quantity: '2', unit: '個' }],
        steps: [{ stepNumber: 1, description: 'トマトを切る', imageUrl: undefined, videoUrl: undefined }],
        imageKey: 'images/test.jpg',
      })
    })
  })

  describe('deleteRecipe', () => {
    it('正しいエンドポイント（/recipes/:id）にDELETEリクエストを送ること', async () => {
      vi.mocked(apiClient.delete).mockResolvedValue({})

      await deleteRecipe('recipe-123')

      expect(apiClient.delete).toHaveBeenCalledTimes(1)
      expect(apiClient.delete).toHaveBeenCalledWith('/recipes/recipe-123')
    })
  })

  describe('uploadImage', () => {
    it('正しいエンドポイント（/recipes/images）にmultipart/form-dataでPOSTすること', async () => {
      vi.mocked(apiPostMultipart).mockResolvedValue({ imageKey: 'images/uploaded.jpg' })

      const file = new File(['test'], 'test.jpg', { type: 'image/jpeg' })
      await uploadImage(file)

      expect(apiPostMultipart).toHaveBeenCalledTimes(1)
      expect(apiPostMultipart).toHaveBeenCalledWith('/recipes/images', expect.any(FormData))
    })

    it('FormDataにファイルが含まれること', async () => {
      vi.mocked(apiPostMultipart).mockResolvedValue({ imageKey: 'images/uploaded.jpg' })

      const file = new File(['test'], 'test.jpg', { type: 'image/jpeg' })
      await uploadImage(file)

      const calledFormData = vi.mocked(apiPostMultipart).mock.calls[0][1] as FormData
      expect(calledFormData.get('file')).toBe(file)
    })

    it('レスポンスからimageKeyを返すこと', async () => {
      vi.mocked(apiPostMultipart).mockResolvedValue({ imageKey: 'images/uploaded.jpg' })

      const file = new File(['test'], 'test.jpg', { type: 'image/jpeg' })
      const result = await uploadImage(file)

      expect(result).toBe('images/uploaded.jpg')
    })

    it('APIエラー時に例外がスローされること', async () => {
      const error = new Error('Upload failed')
      vi.mocked(apiPostMultipart).mockRejectedValue(error)

      const file = new File(['test'], 'test.jpg', { type: 'image/jpeg' })

      await expect(uploadImage(file)).rejects.toThrow('Upload failed')
    })
  })
})
