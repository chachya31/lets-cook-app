import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { Clock, Plus, User } from 'lucide-react'
import { Layout } from '@/components/Layout'
import { Button } from '@/components/ui/button'
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { Badge } from '@/components/ui/badge'
import { useAuthStore } from '@/features/auth/stores/useAuthStore'
import { getAllRecipes, getRecipesByAuthor } from '../api/recipeApi'
import type { Recipe } from '../types'

export const RecipeListPage = () => {
  const { t, i18n } = useTranslation()
  const navigate = useNavigate()
  const { user, isAuthenticated } = useAuthStore()
  const [publicRecipes, setPublicRecipes] = useState<Recipe[]>([])
  const [myRecipes, setMyRecipes] = useState<Recipe[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [activeTab, setActiveTab] = useState('all')

  useEffect(() => {
    const fetchRecipes = async () => {
      setIsLoading(true)
      setError(null)
      try {
        const recipes = await getAllRecipes()
        setPublicRecipes(recipes)

        if (isAuthenticated && user?.userId) {
          const userRecipes = await getRecipesByAuthor(user.userId)
          setMyRecipes(userRecipes)
        }
      } catch (err) {
        setError(t('recipe.detail.fetchError'))
        console.error('Failed to fetch recipes:', err)
      } finally {
        setIsLoading(false)
      }
    }

    fetchRecipes()
  }, [isAuthenticated, user?.userId, t])

  const RecipeCard = ({ recipe }: { recipe: Recipe }) => (
    <Link to={`/recipes/${recipe.recipeId}`} className="block">
      <Card className="h-full overflow-hidden transition-shadow hover:shadow-lg">
        {/* 画像表示 */}
        <div className="aspect-video w-full overflow-hidden bg-muted">
          {recipe.imageUrl ? (
            <img
              src={recipe.imageUrl}
              alt={recipe.title}
              className="h-full w-full object-cover"
            />
          ) : (
            <div className="flex h-full w-full items-center justify-center text-muted-foreground">
              <div className="text-center">
                <div className="text-4xl">🍽️</div>
                <div className="mt-1 text-xs">{t('common.noImage')}</div>
              </div>
            </div>
          )}
        </div>
        <CardHeader className="pb-3">
          <div className="flex items-start justify-between">
            <CardTitle className="line-clamp-2 text-lg">{recipe.title}</CardTitle>
            {!recipe.isPublic && (
              <Badge variant="secondary" className="ml-2 shrink-0">
                {t('recipe.list.private')}
              </Badge>
            )}
          </div>
          <CardDescription className="flex items-center gap-1">
            <User className="h-3 w-3" />
            <span className="truncate">{recipe.authorId}</span>
          </CardDescription>
        </CardHeader>
        <CardContent className="pb-3">
          <div className="flex items-center gap-1 text-sm text-muted-foreground">
            <Clock className="h-4 w-4" />
            <span>
              {recipe.cookingTime}
              {t('recipe.list.minutes')}
            </span>
          </div>
          <div className="mt-2 text-sm text-muted-foreground">
            {t('recipe.list.ingredients')}: {recipe.ingredients?.length || 0}
            {t('recipe.list.items')}
          </div>
        </CardContent>
        <CardFooter className="pt-0">
          <div className="text-xs text-muted-foreground">
            {new Date(recipe.createdAt).toLocaleDateString(
              i18n.language === 'ko' ? 'ko-KR' : 'ja-JP'
            )}
          </div>
        </CardFooter>
      </Card>
    </Link>
  )

  const RecipeGrid = ({ recipes }: { recipes: Recipe[] }) => {
    if (recipes.length === 0) {
      return (
        <div className="py-12 text-center text-muted-foreground">{t('recipe.list.noRecipes')}</div>
      )
    }

    return (
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
        {recipes.map((recipe) => (
          <RecipeCard key={recipe.recipeId} recipe={recipe} />
        ))}
      </div>
    )
  }

  return (
    <Layout>
      <div className="space-y-6">
        {/* ヘッダー */}
        <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <h1 className="text-2xl font-bold tracking-tight">{t('recipe.list.title')}</h1>
            <p className="text-muted-foreground">{t('recipe.list.description')}</p>
          </div>
          {isAuthenticated && (
            <Button onClick={() => navigate('/recipes/new')}>
              <Plus className="mr-2 h-4 w-4" />
              {t('recipe.list.createNew')}
            </Button>
          )}
        </div>

        {/* エラー表示 */}
        {error && <div className="rounded-md bg-destructive/10 p-4 text-destructive">{error}</div>}

        {/* ローディング */}
        {isLoading ? (
          <div className="py-12 text-center text-muted-foreground">{t('common.loading')}</div>
        ) : (
          /* タブ切り替え */
          <Tabs value={activeTab} onValueChange={setActiveTab}>
            <TabsList>
              <TabsTrigger value="all">{t('recipe.list.allRecipes')}</TabsTrigger>
              {isAuthenticated && <TabsTrigger value="mine">{t('recipe.list.myRecipes')}</TabsTrigger>}
            </TabsList>

            <TabsContent value="all" className="mt-6">
              <RecipeGrid recipes={publicRecipes} />
            </TabsContent>

            {isAuthenticated && (
              <TabsContent value="mine" className="mt-6">
                <RecipeGrid recipes={myRecipes} />
              </TabsContent>
            )}
          </Tabs>
        )}
      </div>
    </Layout>
  )
}
