import { useEffect, useState } from 'react'
import { useNavigate, useParams, Link } from 'react-router-dom'
import { useForm, useFieldArray, type Resolver } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { ArrowLeft, GripVertical, Plus, Trash2, ArrowUp, ArrowDown, ImagePlus } from 'lucide-react'
import { Layout } from '@/components/Layout'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import { Switch } from '@/components/ui/switch'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { useAuthStore } from '@/features/auth/stores/useAuthStore'
import { createRecipe, getRecipe, updateRecipe } from '../api/recipeApi'
import type { RecipeFormData } from '../types'

// バリデーションスキーマ
const ingredientSchema = z.object({
  name: z.string().min(1, '材料名は必須です'),
  quantity: z.string().optional(),
  unit: z.string().optional(),
})

const stepSchema = z.object({
  description: z.string().min(1, '手順の説明は必須です'),
  imageUrl: z.string().optional(),
  videoUrl: z.string().optional(),
})

const recipeFormSchema = z.object({
  title: z.string().min(1, 'タイトルは必須です'),
  cookingTime: z.coerce.number().min(1, '調理時間は1分以上で入力してください'),
  isPublic: z.boolean(),
  ingredients: z.array(ingredientSchema).min(1, '材料は1つ以上追加してください'),
  steps: z.array(stepSchema).min(1, '手順は1つ以上追加してください'),
})

type RecipeFormValues = z.infer<typeof recipeFormSchema>

export const RecipeFormPage = () => {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const { isAuthenticated } = useAuthStore()
  const isEditMode = Boolean(id)

  const [isLoading, setIsLoading] = useState(false)
  const [isFetching, setIsFetching] = useState(isEditMode)
  const [error, setError] = useState<string | null>(null)

  const {
    register,
    control,
    handleSubmit,
    reset,
    watch,
    setValue,
    formState: { errors, isSubmitting },
  } = useForm<RecipeFormValues>({
    resolver: zodResolver(recipeFormSchema) as Resolver<RecipeFormValues>,
    defaultValues: {
      title: '',
      cookingTime: 30,
      isPublic: true,
      ingredients: [{ name: '', quantity: '', unit: '' }],
      steps: [{ description: '' }],
    },
  })

  const {
    fields: ingredientFields,
    append: appendIngredient,
    remove: removeIngredient,
    move: moveIngredient,
  } = useFieldArray({
    control,
    name: 'ingredients',
  })

  const {
    fields: stepFields,
    append: appendStep,
    remove: removeStep,
    move: moveStep,
  } = useFieldArray({
    control,
    name: 'steps',
  })

  const isPublic = watch('isPublic')

  // 認証チェック
  useEffect(() => {
    if (!isAuthenticated) {
      navigate('/login')
    }
  }, [isAuthenticated, navigate])

  // 編集モード時にレシピデータを取得
  useEffect(() => {
    const fetchRecipe = async () => {
      if (!id) return

      setIsFetching(true)
      try {
        const recipe = await getRecipe(id)
        reset({
          title: recipe.title,
          cookingTime: recipe.cookingTime,
          isPublic: recipe.isPublic,
          ingredients: recipe.ingredients.map((ing) => ({
            name: ing.name,
            quantity: ing.quantity || '',
            unit: ing.unit || '',
          })),
          steps: recipe.steps
            .sort((a, b) => a.stepNumber - b.stepNumber)
            .map((step) => ({
              description: step.description,
              imageUrl: step.imageUrl || '',
              videoUrl: step.videoUrl || '',
            })),
        })
      } catch (err) {
        setError('レシピの取得に失敗しました')
        console.error('Failed to fetch recipe:', err)
      } finally {
        setIsFetching(false)
      }
    }

    fetchRecipe()
  }, [id, reset])

  const onSubmit = async (data: RecipeFormValues) => {
    setIsLoading(true)
    setError(null)

    const formData: RecipeFormData = {
      title: data.title,
      cookingTime: data.cookingTime,
      isPublic: data.isPublic,
      ingredients: data.ingredients.filter((ing) => ing.name.trim() !== ''),
      steps: data.steps.filter((step) => step.description.trim() !== ''),
    }

    try {
      if (isEditMode && id) {
        await updateRecipe(id, formData)
        navigate(`/recipes/${id}`)
      } else {
        const created = await createRecipe(formData)
        navigate(`/recipes/${created.recipeId}`)
      }
    } catch (err) {
      setError(isEditMode ? 'レシピの更新に失敗しました' : 'レシピの作成に失敗しました')
      console.error('Failed to save recipe:', err)
    } finally {
      setIsLoading(false)
    }
  }

  if (isFetching) {
    return (
      <Layout>
        <div className="py-12 text-center text-muted-foreground">読み込み中...</div>
      </Layout>
    )
  }

  return (
    <Layout>
      <div className="mx-auto max-w-3xl space-y-6">
        {/* 戻るボタン */}
        <Button variant="ghost" size="sm" asChild>
          <Link to="/recipes">
            <ArrowLeft className="mr-2 h-4 w-4" />
            レシピ一覧に戻る
          </Link>
        </Button>

        {/* ページタイトル */}
        <div>
          <h1 className="text-2xl font-bold tracking-tight">
            {isEditMode ? 'レシピを編集' : '新しいレシピを作成'}
          </h1>
          <p className="text-muted-foreground">
            {isEditMode ? 'レシピの内容を編集できます' : 'あなたのオリジナルレシピを登録しましょう'}
          </p>
        </div>

        {/* エラー表示 */}
        {error && <div className="rounded-md bg-destructive/10 p-4 text-destructive">{error}</div>}

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
          {/* 基本情報 */}
          <Card>
            <CardHeader>
              <CardTitle>基本情報</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              {/* タイトル */}
              <div className="space-y-2">
                <Label htmlFor="title">
                  タイトル <span className="text-destructive">*</span>
                </Label>
                <Input id="title" placeholder="例: 簡単トマトパスタ" {...register('title')} />
                {errors.title && <p className="text-sm text-destructive">{errors.title.message}</p>}
              </div>

              {/* 調理時間 */}
              <div className="space-y-2">
                <Label htmlFor="cookingTime">
                  調理時間（分） <span className="text-destructive">*</span>
                </Label>
                <Input
                  id="cookingTime"
                  type="number"
                  min={1}
                  placeholder="30"
                  className="w-32"
                  {...register('cookingTime')}
                />
                {errors.cookingTime && (
                  <p className="text-sm text-destructive">{errors.cookingTime.message}</p>
                )}
              </div>

              {/* 公開設定 */}
              <div className="flex items-center gap-3">
                <Switch
                  id="isPublic"
                  checked={isPublic}
                  onCheckedChange={(checked) => setValue('isPublic', checked)}
                />
                <Label htmlFor="isPublic" className="cursor-pointer">
                  {isPublic ? '公開する' : '非公開にする'}
                </Label>
              </div>

              {/* 画像アップロード（UIのみ） */}
              <div className="space-y-2">
                <Label>レシピ画像</Label>
                <div className="flex h-32 w-full cursor-pointer items-center justify-center rounded-md border-2 border-dashed border-muted-foreground/25 transition-colors hover:border-muted-foreground/50">
                  <div className="flex flex-col items-center gap-2 text-muted-foreground">
                    <ImagePlus className="h-8 w-8" />
                    <span className="text-sm">画像をアップロード（後日実装）</span>
                  </div>
                </div>
              </div>
            </CardContent>
          </Card>

          {/* 材料 */}
          <Card>
            <CardHeader>
              <CardTitle>材料</CardTitle>
              <CardDescription>材料を追加してください（1つ以上必須）</CardDescription>
            </CardHeader>
            <CardContent className="space-y-3">
              {ingredientFields.map((field, index) => (
                <div
                  key={field.id}
                  className="flex items-start gap-2 rounded-md border bg-muted/30 p-3"
                >
                  <div className="flex flex-col gap-1 pt-2">
                    <button
                      type="button"
                      onClick={() => index > 0 && moveIngredient(index, index - 1)}
                      disabled={index === 0}
                      className="text-muted-foreground hover:text-foreground disabled:opacity-30"
                    >
                      <ArrowUp className="h-4 w-4" />
                    </button>
                    <GripVertical className="h-4 w-4 text-muted-foreground/50" />
                    <button
                      type="button"
                      onClick={() =>
                        index < ingredientFields.length - 1 && moveIngredient(index, index + 1)
                      }
                      disabled={index === ingredientFields.length - 1}
                      className="text-muted-foreground hover:text-foreground disabled:opacity-30"
                    >
                      <ArrowDown className="h-4 w-4" />
                    </button>
                  </div>

                  <div className="grid flex-1 gap-2 sm:grid-cols-3">
                    <Input placeholder="材料名" {...register(`ingredients.${index}.name`)} />
                    <Input placeholder="分量" {...register(`ingredients.${index}.quantity`)} />
                    <Input placeholder="単位" {...register(`ingredients.${index}.unit`)} />
                  </div>

                  <Button
                    type="button"
                    variant="ghost"
                    size="icon"
                    onClick={() => removeIngredient(index)}
                    disabled={ingredientFields.length === 1}
                    className="shrink-0 text-muted-foreground hover:text-destructive"
                  >
                    <Trash2 className="h-4 w-4" />
                  </Button>
                </div>
              ))}

              {errors.ingredients && (
                <p className="text-sm text-destructive">
                  {errors.ingredients.message || errors.ingredients.root?.message}
                </p>
              )}

              <Button
                type="button"
                variant="outline"
                onClick={() => appendIngredient({ name: '', quantity: '', unit: '' })}
              >
                <Plus className="mr-2 h-4 w-4" />
                材料を追加
              </Button>
            </CardContent>
          </Card>

          {/* 手順 */}
          <Card>
            <CardHeader>
              <CardTitle>作り方</CardTitle>
              <CardDescription>手順を追加してください（1つ以上必須）</CardDescription>
            </CardHeader>
            <CardContent className="space-y-3">
              {stepFields.map((field, index) => (
                <div
                  key={field.id}
                  className="flex items-start gap-2 rounded-md border bg-muted/30 p-3"
                >
                  <div className="flex flex-col items-center gap-1 pt-2">
                    <button
                      type="button"
                      onClick={() => index > 0 && moveStep(index, index - 1)}
                      disabled={index === 0}
                      className="text-muted-foreground hover:text-foreground disabled:opacity-30"
                    >
                      <ArrowUp className="h-4 w-4" />
                    </button>
                    <div className="flex h-6 w-6 items-center justify-center rounded-full bg-emerald-100 text-xs font-bold text-emerald-700">
                      {index + 1}
                    </div>
                    <button
                      type="button"
                      onClick={() => index < stepFields.length - 1 && moveStep(index, index + 1)}
                      disabled={index === stepFields.length - 1}
                      className="text-muted-foreground hover:text-foreground disabled:opacity-30"
                    >
                      <ArrowDown className="h-4 w-4" />
                    </button>
                  </div>

                  <div className="flex-1">
                    <Textarea
                      placeholder={`ステップ ${index + 1} の説明`}
                      rows={2}
                      {...register(`steps.${index}.description`)}
                    />
                  </div>

                  <Button
                    type="button"
                    variant="ghost"
                    size="icon"
                    onClick={() => removeStep(index)}
                    disabled={stepFields.length === 1}
                    className="shrink-0 text-muted-foreground hover:text-destructive"
                  >
                    <Trash2 className="h-4 w-4" />
                  </Button>
                </div>
              ))}

              {errors.steps && (
                <p className="text-sm text-destructive">
                  {errors.steps.message || errors.steps.root?.message}
                </p>
              )}

              <Button
                type="button"
                variant="outline"
                onClick={() => appendStep({ description: '' })}
              >
                <Plus className="mr-2 h-4 w-4" />
                手順を追加
              </Button>
            </CardContent>
          </Card>

          {/* 送信ボタン */}
          <div className="flex gap-3">
            <Button
              type="submit"
              disabled={isSubmitting || isLoading}
              className="bg-emerald-600 hover:bg-emerald-700"
            >
              {isSubmitting || isLoading ? '保存中...' : isEditMode ? '更新する' : '作成する'}
            </Button>
            <Button type="button" variant="outline" asChild>
              <Link to="/recipes">キャンセル</Link>
            </Button>
          </div>
        </form>
      </div>
    </Layout>
  )
}
