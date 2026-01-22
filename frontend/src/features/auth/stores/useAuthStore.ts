import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import { loginApi } from '../api/authApi'
import type { UserProfile } from '../types'

interface AuthState {
  // 状態
  user: UserProfile | null
  accessToken: string | null
  idToken: string | null
  refreshToken: string | null
  expiresIn: number | null
  isAuthenticated: boolean
  isLoading: boolean
  error: string | null

  // アクション
  login: (email: string, password: string) => Promise<void>
  logout: () => void
  clearError: () => void
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      // 初期状態
      user: null,
      accessToken: null,
      idToken: null,
      refreshToken: null,
      expiresIn: null,
      isAuthenticated: false,
      isLoading: false,
      error: null,

      login: async (email: string, password: string) => {
        set({ isLoading: true, error: null })

        try {
          const response = await loginApi({ email, password })

          set({
            user: response.user,
            accessToken: response.accessToken,
            idToken: response.idToken,
            refreshToken: response.refreshToken,
            expiresIn: response.expiresIn,
            isAuthenticated: true,
            isLoading: false,
            error: null,
          })
        } catch (error) {
          const errorMessage =
            error instanceof Error
              ? error.message
              : 'ログインに失敗しました。メールアドレスとパスワードを確認してください。'

          set({
            user: null,
            accessToken: null,
            idToken: null,
            refreshToken: null,
            expiresIn: null,
            isAuthenticated: false,
            isLoading: false,
            error: errorMessage,
          })

          throw error
        }
      },

      logout: () => {
        set({
          user: null,
          accessToken: null,
          idToken: null,
          refreshToken: null,
          expiresIn: null,
          isAuthenticated: false,
          isLoading: false,
          error: null,
        })
      },

      clearError: () => {
        set({ error: null })
      },
    }),
    {
      name: 'auth-storage',
      partialize: (state) => ({
        user: state.user,
        accessToken: state.accessToken,
        idToken: state.idToken,
        refreshToken: state.refreshToken,
        expiresIn: state.expiresIn,
        isAuthenticated: state.isAuthenticated,
      }),
    }
  )
)
