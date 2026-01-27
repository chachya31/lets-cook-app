import { createBrowserRouter } from 'react-router-dom'
import { LandingPage } from '../features/misc/components/LandingPage'
import { LoginPage } from '../features/auth/components/LoginPage'
import { RecipeListPage } from '../features/recipes/components/RecipeListPage'
import { RecipeDetailPage } from '../features/recipes/components/RecipeDetailPage'
import { RecipeFormPage } from '../features/recipes/components/RecipeFormPage'

export const router = createBrowserRouter([
  {
    path: '/',
    element: <LandingPage />,
  },
  {
    path: '/login',
    element: <LoginPage />,
  },
  {
    path: '/recipes',
    element: <RecipeListPage />,
  },
  {
    path: '/recipes/new',
    element: <RecipeFormPage />,
  },
  {
    path: '/recipes/:id',
    element: <RecipeDetailPage />,
  },
  {
    path: '/recipes/:id/edit',
    element: <RecipeFormPage />,
  },
])
