import { useState, useEffect } from 'react'
import type { FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { useAuthStore } from '../stores/useAuthStore'

export const LoginPage = () => {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [validationErrors, setValidationErrors] = useState<{
    email?: string
    password?: string
  }>({})

  const { login, isLoading, error, clearError, isAuthenticated } = useAuthStore()
  const navigate = useNavigate()

  useEffect(() => {
    if (isAuthenticated) {
      navigate('/')
    }
  }, [isAuthenticated, navigate])

  useEffect(() => {
    clearError()
  }, [clearError])

  const validateForm = (): boolean => {
    const errors: { email?: string; password?: string } = {}

    if (!email.trim()) {
      errors.email = 'メールアドレスを入力してください'
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
      errors.email = '有効なメールアドレスを入力してください'
    }

    if (!password) {
      errors.password = 'パスワードを入力してください'
    }

    setValidationErrors(errors)
    return Object.keys(errors).length === 0
  }

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault()
    clearError()

    if (!validateForm()) {
      return
    }

    try {
      await login(email, password)
      navigate('/')
    } catch {
      // エラーはストアで処理される
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-gray-50 px-4 dark:bg-slate-900">
      <Card className="w-full max-w-md shadow-lg">
        <CardHeader className="space-y-4 p-8 pb-0">
          <div className="flex justify-center">
            <img
              src="/logo.svg"
              alt="Let's Cook"
              className="mx-auto h-16 w-16"
            />
          </div>
          <div className="text-center">
            <CardTitle className="text-2xl font-bold">Let&apos;s Cook</CardTitle>
            <CardDescription className="mt-2">
              アカウントにログインしてください
            </CardDescription>
          </div>
        </CardHeader>

        <CardContent className="space-y-6 p-8">
          {error && (
            <Alert variant="destructive">
              <AlertDescription>{error}</AlertDescription>
            </Alert>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="email">メールアドレス</Label>
              <Input
                id="email"
                type="email"
                value={email}
                onChange={(e) => {
                  setEmail(e.target.value)
                  if (validationErrors.email) {
                    setValidationErrors((prev) => ({ ...prev, email: undefined }))
                  }
                }}
                placeholder="example@email.com"
                className={validationErrors.email ? 'border-red-500 focus-visible:ring-red-500' : ''}
                disabled={isLoading}
              />
              {validationErrors.email && (
                <p className="text-sm text-red-500">{validationErrors.email}</p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="password">パスワード</Label>
              <Input
                id="password"
                type="password"
                value={password}
                onChange={(e) => {
                  setPassword(e.target.value)
                  if (validationErrors.password) {
                    setValidationErrors((prev) => ({ ...prev, password: undefined }))
                  }
                }}
                placeholder="パスワードを入力"
                className={validationErrors.password ? 'border-red-500 focus-visible:ring-red-500' : ''}
                disabled={isLoading}
              />
              {validationErrors.password && (
                <p className="text-sm text-red-500">{validationErrors.password}</p>
              )}
            </div>

            <Button
              type="submit"
              className="w-full bg-emerald-600 hover:bg-emerald-700"
              disabled={isLoading}
            >
              {isLoading ? (
                <span className="flex items-center justify-center gap-2">
                  <svg
                    className="h-4 w-4 animate-spin"
                    fill="none"
                    viewBox="0 0 24 24"
                  >
                    <circle
                      className="opacity-25"
                      cx="12"
                      cy="12"
                      r="10"
                      stroke="currentColor"
                      strokeWidth="4"
                    />
                    <path
                      className="opacity-75"
                      fill="currentColor"
                      d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                    />
                  </svg>
                  ログイン中...
                </span>
              ) : (
                'ログイン'
              )}
            </Button>
          </form>

          <p className="text-center text-sm text-muted-foreground">
            アカウントをお持ちでない方は、新規登録してください
          </p>
        </CardContent>
      </Card>
    </div>
  )
}
