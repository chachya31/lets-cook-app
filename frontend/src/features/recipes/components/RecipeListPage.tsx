import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
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
        setError('レシピの取得に失敗しました')
        console.error('Failed to fetch recipes:', err)
      } finally {
        setIsLoading(false)
      }
    }

    fetchRecipes()
  }, [isAuthenticated, user?.userId])

  const RecipeCard = ({ recipe }: { recipe: Recipe }) => (
    <Link to={`/recipes/${recipe.recipeId}`} className="block">
      <Card className="h-full transition-shadow hover:shadow-lg">
        <CardHeader className="pb-3">
          <div className="flex items-start justify-between">
            <CardTitle className="line-clamp-2 text-lg">{recipe.title}</CardTitle>
            {!recipe.isPublic && (
              <Badge variant="secondary" className="ml-2 shrink-0">
                非公開
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
            <span>{recipe.cookingTime}分</span>
          </div>
          <div className="mt-2 text-sm text-muted-foreground">
            材料: {recipe.ingredients?.length || 0}品
          </div>
        </CardContent>
        <CardFooter className="pt-0">
          <div className="text-xs text-muted-foreground">
            {new Date(recipe.createdAt).toLocaleDateString('ja-JP')}
          </div>
        </CardFooter>
      </Card>
    </Link>
  )

  const RecipeGrid = ({ recipes }: { recipes: Recipe[] }) => {
    if (recipes.length === 0) {
      return <div className="py-12 text-center text-muted-foreground">レシピがありません</div>
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
            <h1 className="text-2xl font-bold tracking-tight">レシピ一覧</h1>
            <p className="text-muted-foreground">みんなのレシピを探してみましょう</p>
          </div>
          {isAuthenticated && (
            <Button onClick={() => navigate('/recipes/new')}>
              <Plus className="mr-2 h-4 w-4" />
              新しいレシピを作成
            </Button>
          )}
        </div>

        {/* エラー表示 */}
        {error && <div className="rounded-md bg-destructive/10 p-4 text-destructive">{error}</div>}

        {/* ローディング */}
        {isLoading ? (
          <div className="py-12 text-center text-muted-foreground">読み込み中...</div>
        ) : (
          /* タブ切り替え */
          <Tabs value={activeTab} onValueChange={setActiveTab}>
            <TabsList>
              <TabsTrigger value="all">みんなのレシピ</TabsTrigger>
              {isAuthenticated && <TabsTrigger value="mine">自分のレシピ</TabsTrigger>}
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
