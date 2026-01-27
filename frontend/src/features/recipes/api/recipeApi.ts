import { apiClient } from '@/lib/axios'
import type { Recipe, RecipeFormData, RecipeRequest } from '../types'

// フォームデータをAPIリクエスト形式に変換
const toRecipeRequest = (formData: RecipeFormData): RecipeRequest => {
  return {
    title: formData.title,
    cookingTime: formData.cookingTime,
    isPublic: formData.isPublic,
    ingredients: formData.ingredients,
    steps: formData.steps.map((step, index) => ({
      stepNumber: index + 1,
      description: step.description,
      imageUrl: step.imageUrl,
      videoUrl: step.videoUrl,
    })),
  }
}

// 全レシピ取得（公開レシピ）
export const getAllRecipes = async (): Promise<Recipe[]> => {
  const response = await apiClient.get<Recipe[]>('/recipes')
  return response.data
}

// 特定ユーザーのレシピ取得
export const getRecipesByAuthor = async (authorId: string): Promise<Recipe[]> => {
  const response = await apiClient.get<Recipe[]>('/recipes', {
    params: { authorId },
  })
  return response.data
}

// 単一レシピ取得
export const getRecipe = async (id: string): Promise<Recipe> => {
  const response = await apiClient.get<Recipe>(`/recipes/${id}`)
  return response.data
}

// レシピ作成
export const createRecipe = async (formData: RecipeFormData): Promise<Recipe> => {
  const request = toRecipeRequest(formData)
  console.log(request)
  const response = await apiClient.post<Recipe>('/recipes', request)
  return response.data
}

// レシピ更新
export const updateRecipe = async (id: string, formData: RecipeFormData): Promise<Recipe> => {
  const request = toRecipeRequest(formData)
  const response = await apiClient.put<Recipe>(`/recipes/${id}`, request)
  return response.data
}

// レシピ削除
export const deleteRecipe = async (id: string): Promise<void> => {
  await apiClient.delete(`/recipes/${id}`)
}
