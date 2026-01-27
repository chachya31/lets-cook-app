import { Layout } from '@/components/Layout'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Separator } from '@/components/ui/separator'
import { useAuthStore } from '@/features/auth/stores/useAuthStore'
import { ArrowLeft, Clock, Edit, Trash2, User } from 'lucide-react'
import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { deleteRecipe, getRecipe } from '../api/recipeApi'
import type { Recipe } from '../types'

export const RecipeDetailPage = () => {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const { user, isAuthenticated } = useAuthStore()
  const [recipe, setRecipe] = useState<Recipe | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [isDeleting, setIsDeleting] = useState(false)

  const isAuthor = isAuthenticated && user?.userId === recipe?.authorId

  useEffect(() => {
    const fetchRecipe = async () => {
      if (!id) return

      setIsLoading(true)
      setError(null)
      try {
        const data = await getRecipe(id)
        setRecipe(data)
      } catch (err) {
        setError('レシピの取得に失敗しました')
        console.error('Failed to fetch recipe:', err)
      } finally {
        setIsLoading(false)
      }
    }

    fetchRecipe()
  }, [id])

  const handleDelete = async () => {
    if (!id || !recipe) return

    const confirmed = window.confirm(`「${recipe.title}」を削除してもよろしいですか？`)
    if (!confirmed) return

    setIsDeleting(true)
    try {
      await deleteRecipe(id)
      navigate('/recipes')
    } catch (err) {
      setError('レシピの削除に失敗しました')
      console.error('Failed to delete recipe:', err)
    } finally {
      setIsDeleting(false)
    }
  }

  if (isLoading) {
    return (
      <Layout>
        <div className="py-12 text-center text-muted-foreground">読み込み中...</div>
      </Layout>
    )
  }

  if (error || !recipe) {
    return (
      <Layout>
        <div className="space-y-4">
          <div className="rounded-md bg-destructive/10 p-4 text-destructive">
            {error || 'レシピが見つかりませんでした'}
          </div>
          <Button variant="outline" asChild>
            <Link to="/recipes">
              <ArrowLeft className="mr-2 h-4 w-4" />
              一覧に戻る
            </Link>
          </Button>
        </div>
      </Layout>
    )
  }
  console.log(user?.userId)
  console.log(recipe?.authorId)

  return (
    <Layout>
      <div className="space-y-6">
        {/* 戻るボタン */}
        <Button variant="ghost" size="sm" asChild>
          <Link to="/recipes">
            <ArrowLeft className="mr-2 h-4 w-4" />
            レシピ一覧に戻る
          </Link>
        </Button>

        {/* ヘッダー */}
        <div className="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
          <div className="space-y-2">
            <div className="flex flex-wrap items-center gap-2">
              <h1 className="text-3xl font-bold tracking-tight">{recipe.title}</h1>
              {!recipe.isPublic && <Badge variant="secondary">非公開</Badge>}
            </div>
            <div className="flex flex-wrap items-center gap-4 text-muted-foreground">
              <div className="flex items-center gap-1">
                <User className="h-4 w-4" />
                <span>{recipe.authorId}</span>
              </div>
              <div className="flex items-center gap-1">
                <Clock className="h-4 w-4" />
                <span>{recipe.cookingTime}分</span>
              </div>
            </div>
          </div>

          {/* 編集・削除ボタン（作成者のみ） */}
          {isAuthor && (
            <div className="flex gap-2">
              <Button variant="outline" asChild>
                <Link to={`/recipes/${id}/edit`}>
                  <Edit className="mr-2 h-4 w-4" />
                  編集
                </Link>
              </Button>
              <Button variant="destructive" onClick={handleDelete} disabled={isDeleting}>
                <Trash2 className="mr-2 h-4 w-4" />
                {isDeleting ? '削除中...' : '削除'}
              </Button>
            </div>
          )}
        </div>

        <div className="grid gap-6 lg:grid-cols-3">
          {/* 材料 */}
          <Card>
            <CardHeader>
              <CardTitle className="text-lg">材料</CardTitle>
              <CardDescription>{recipe.ingredients?.length || 0}品</CardDescription>
            </CardHeader>
            <CardContent>
              <ul className="space-y-2">
                {recipe.ingredients?.map((ingredient, index) => (
                  <li
                    key={index}
                    className="flex items-center justify-between border-b pb-2 last:border-0"
                  >
                    <span className="font-medium">{ingredient.name}</span>
                    <span className="text-muted-foreground">
                      {ingredient.quantity}
                      {ingredient.unit}
                    </span>
                  </li>
                ))}
              </ul>
            </CardContent>
          </Card>

          {/* 手順 */}
          <Card className="lg:col-span-2">
            <CardHeader>
              <CardTitle className="text-lg">作り方</CardTitle>
              <CardDescription>{recipe.steps?.length || 0}ステップ</CardDescription>
            </CardHeader>
            <CardContent>
              <ol className="space-y-4">
                {recipe.steps
                  ?.sort((a, b) => a.stepNumber - b.stepNumber)
                  .map((step) => (
                    <li key={step.stepNumber} className="flex gap-4">
                      <div className="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-emerald-100 text-sm font-bold text-emerald-700">
                        {step.stepNumber}
                      </div>
                      <div className="flex-1 pt-1">
                        <p>{step.description}</p>
                      </div>
                    </li>
                  ))}
              </ol>
            </CardContent>
          </Card>
        </div>

        <Separator />

        {/* メタ情報 */}
        <div className="text-sm text-muted-foreground">
          <p>作成日: {new Date(recipe.createdAt).toLocaleString('ja-JP')}</p>
          <p>更新日: {new Date(recipe.updatedAt).toLocaleString('ja-JP')}</p>
        </div>
      </div>
    </Layout>
  )
}
