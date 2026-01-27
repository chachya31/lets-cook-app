// 材料の型定義
export interface Ingredient {
  name: string
  quantity?: string
  unit?: string
}

// 手順の型定義
export interface Step {
  stepNumber: number
  description: string
  imageUrl?: string
  videoUrl?: string
}

// レシピレスポンスの型定義（Backend RecipeResponseに対応）
export interface Recipe {
  recipeId: string
  title: string
  authorId: string
  ingredients: Ingredient[]
  steps: Step[]
  cookingTime: number
  isPublic: boolean
  imageUrl?: string
  createdAt: string
  updatedAt: string
}

// レシピ作成/更新リクエストの型定義（Backend RecipeRequestに対応）
export interface RecipeRequest {
  title: string
  ingredients: Ingredient[]
  steps: Step[]
  cookingTime: number
  isPublic: boolean
  imageKey?: string
}

// フォーム用の型定義
export interface RecipeFormData {
  title: string
  cookingTime: number
  isPublic: boolean
  ingredients: Ingredient[]
  steps: Omit<Step, 'stepNumber'>[]
}
