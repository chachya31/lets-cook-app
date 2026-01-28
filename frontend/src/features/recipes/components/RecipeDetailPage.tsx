import { Layout } from '@/components/Layout'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Separator } from '@/components/ui/separator'
import { useAuthStore } from '@/features/auth/stores/useAuthStore'
import { ArrowLeft, Clock, Edit, Trash2, User } from 'lucide-react'
import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { deleteRecipe, getRecipe } from '../api/recipeApi'
import type { Recipe } from '../types'

export const RecipeDetailPage = () => {
  const { t, i18n } = useTranslation()
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
        setError(t('recipe.detail.fetchError'))
        console.error('Failed to fetch recipe:', err)
      } finally {
        setIsLoading(false)
      }
    }

    fetchRecipe()
  }, [id, t])

  const handleDelete = async () => {
    if (!id || !recipe) return

    const confirmed = window.confirm(t('recipe.detail.confirmDelete', { title: recipe.title }))
    if (!confirmed) return

    setIsDeleting(true)
    try {
      await deleteRecipe(id)
      navigate('/recipes')
    } catch (err) {
      setError(t('recipe.detail.deleteError'))
      console.error('Failed to delete recipe:', err)
    } finally {
      setIsDeleting(false)
    }
  }

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleString(i18n.language === 'ko' ? 'ko-KR' : 'ja-JP')
  }

  if (isLoading) {
    return (
      <Layout>
        <div className="py-12 text-center text-muted-foreground">{t('common.loading')}</div>
      </Layout>
    )
  }

  if (error || !recipe) {
    return (
      <Layout>
        <div className="space-y-4">
          <div className="rounded-md bg-destructive/10 p-4 text-destructive">
            {error || t('recipe.detail.notFound')}
          </div>
          <Button variant="outline" asChild>
            <Link to="/recipes">
              <ArrowLeft className="mr-2 h-4 w-4" />
              {t('recipe.detail.backToListShort')}
            </Link>
          </Button>
        </div>
      </Layout>
    )
  }
  return (
    <Layout>
      <div className="space-y-6">
        {/* 戻るボタン */}
        <Button variant="ghost" size="sm" asChild>
          <Link to="/recipes">
            <ArrowLeft className="mr-2 h-4 w-4" />
            {t('recipe.detail.backToList')}
          </Link>
        </Button>

        {/* レシピ画像 */}
        <div className="aspect-video max-h-96 w-full overflow-hidden rounded-lg bg-muted">
          {recipe.imageUrl ? (
            <img
              src={recipe.imageUrl}
              alt={recipe.title}
              className="h-full w-full object-cover"
            />
          ) : (
            <div className="flex h-full w-full items-center justify-center text-muted-foreground">
              <div className="text-center">
                <div className="text-6xl">🍽️</div>
                <div className="mt-2 text-sm">{t('common.noImage')}</div>
              </div>
            </div>
          )}
        </div>

        {/* ヘッダー */}
        <div className="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
          <div className="space-y-2">
            <div className="flex flex-wrap items-center gap-2">
              <h1 className="text-3xl font-bold tracking-tight">{recipe.title}</h1>
              {!recipe.isPublic && <Badge variant="secondary">{t('recipe.list.private')}</Badge>}
            </div>
            <div className="flex flex-wrap items-center gap-4 text-muted-foreground">
              <div className="flex items-center gap-1">
                <User className="h-4 w-4" />
                <span>{recipe.authorId}</span>
              </div>
              <div className="flex items-center gap-1">
                <Clock className="h-4 w-4" />
                <span>
                  {recipe.cookingTime}
                  {t('recipe.list.minutes')}
                </span>
              </div>
            </div>
          </div>

          {/* 編集・削除ボタン（作成者のみ） */}
          {isAuthor && (
            <div className="flex gap-2">
              <Button variant="outline" asChild>
                <Link to={`/recipes/${id}/edit`}>
                  <Edit className="mr-2 h-4 w-4" />
                  {t('common.edit')}
                </Link>
              </Button>
              <Button variant="destructive" onClick={handleDelete} disabled={isDeleting}>
                <Trash2 className="mr-2 h-4 w-4" />
                {isDeleting ? t('recipe.detail.deleting') : t('common.delete')}
              </Button>
            </div>
          )}
        </div>

        <div className="grid gap-6 lg:grid-cols-3">
          {/* 材料 */}
          <Card>
            <CardHeader>
              <CardTitle className="text-lg">{t('recipe.detail.ingredients')}</CardTitle>
              <CardDescription>
                {recipe.ingredients?.length || 0}
                {t('recipe.list.items')}
              </CardDescription>
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
              <CardTitle className="text-lg">{t('recipe.detail.steps')}</CardTitle>
              <CardDescription>
                {recipe.steps?.length || 0}
                {t('recipe.detail.stepsCount')}
              </CardDescription>
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
          <p>
            {t('recipe.detail.createdAt')}: {formatDate(recipe.createdAt)}
          </p>
          <p>
            {t('recipe.detail.updatedAt')}: {formatDate(recipe.updatedAt)}
          </p>
        </div>
      </div>
    </Layout>
  )
}
