import { describe, it, expect, vi, beforeEach } from 'vitest'
import { useAuthStore } from './useAuthStore'
import type { LoginResponse } from '../types'

// loginApiをモック
vi.mock('../api/authApi', () => ({
  loginApi: vi.fn(),
}))

import { loginApi } from '../api/authApi'

const mockLoginResponse: LoginResponse = {
  accessToken: 'mock-access-token',
  idToken: 'mock-id-token',
  refreshToken: 'mock-refresh-token',
  expiresIn: 3600,
  user: {
    userId: 'user-123',
    email: 'test@example.com',
    nickname: 'testuser',
    displayName: 'Test User',
    profileImageUrl: null,
    preferredLanguage: 'ja',
    timezone: 'Asia/Tokyo',
    roles: [],
  },
}

describe('useAuthStore', () => {
  beforeEach(() => {
    // 各テスト前にストアをリセット
    useAuthStore.setState({
      user: null,
      accessToken: null,
      idToken: null,
      refreshToken: null,
      expiresIn: null,
      isAuthenticated: false,
      isLoading: false,
      error: null,
    })
    vi.clearAllMocks()
  })

  describe('初期状態', () => {
    it('初期状態が未認証であること', () => {
      const state = useAuthStore.getState()

      expect(state.user).toBeNull()
      expect(state.accessToken).toBeNull()
      expect(state.idToken).toBeNull()
      expect(state.refreshToken).toBeNull()
      expect(state.expiresIn).toBeNull()
      expect(state.isAuthenticated).toBe(false)
      expect(state.isLoading).toBe(false)
      expect(state.error).toBeNull()
    })
  })

  describe('login アクション', () => {
    it('ログイン成功時にuserとtokenがセットされ、isAuthenticatedがtrueになること', async () => {
      vi.mocked(loginApi).mockResolvedValue(mockLoginResponse)

      await useAuthStore.getState().login('test@example.com', 'password123')

      const state = useAuthStore.getState()
      expect(state.user).toEqual(mockLoginResponse.user)
      expect(state.accessToken).toBe('mock-access-token')
      expect(state.idToken).toBe('mock-id-token')
      expect(state.refreshToken).toBe('mock-refresh-token')
      expect(state.expiresIn).toBe(3600)
      expect(state.isAuthenticated).toBe(true)
      expect(state.isLoading).toBe(false)
      expect(state.error).toBeNull()
    })

    it('ログイン中はisLoadingがtrueになること', async () => {
      let resolveLogin: (value: LoginResponse) => void
      vi.mocked(loginApi).mockReturnValue(
        new Promise((resolve) => {
          resolveLogin = resolve
        })
      )

      const loginPromise = useAuthStore.getState().login('test@example.com', 'password123')

      // ログイン処理中の状態を確認
      expect(useAuthStore.getState().isLoading).toBe(true)

      // ログイン処理を完了
      resolveLogin!(mockLoginResponse)
      await loginPromise

      expect(useAuthStore.getState().isLoading).toBe(false)
    })

    it('ログイン失敗時にエラーがセットされ、状態がクリアされること', async () => {
      const errorMessage = '認証に失敗しました'
      vi.mocked(loginApi).mockRejectedValue(new Error(errorMessage))

      await expect(
        useAuthStore.getState().login('test@example.com', 'wrongpassword')
      ).rejects.toThrow(errorMessage)

      const state = useAuthStore.getState()
      expect(state.user).toBeNull()
      expect(state.accessToken).toBeNull()
      expect(state.isAuthenticated).toBe(false)
      expect(state.isLoading).toBe(false)
      expect(state.error).toBe(errorMessage)
    })

    it('ログイン失敗時にError以外の例外の場合はデフォルトメッセージがセットされること', async () => {
      vi.mocked(loginApi).mockRejectedValue('Unknown error')

      await expect(useAuthStore.getState().login('test@example.com', 'wrongpassword')).rejects.toBe(
        'Unknown error'
      )

      const state = useAuthStore.getState()
      expect(state.error).toBe(
        'ログインに失敗しました。メールアドレスとパスワードを確認してください。'
      )
    })
  })

  describe('logout アクション', () => {
    it('ログアウト実行時に状態がクリアされること', async () => {
      // まずログイン状態にする
      vi.mocked(loginApi).mockResolvedValue(mockLoginResponse)
      await useAuthStore.getState().login('test@example.com', 'password123')

      // ログイン状態を確認
      expect(useAuthStore.getState().isAuthenticated).toBe(true)

      // ログアウト実行
      useAuthStore.getState().logout()

      const state = useAuthStore.getState()
      expect(state.user).toBeNull()
      expect(state.accessToken).toBeNull()
      expect(state.idToken).toBeNull()
      expect(state.refreshToken).toBeNull()
      expect(state.expiresIn).toBeNull()
      expect(state.isAuthenticated).toBe(false)
      expect(state.isLoading).toBe(false)
      expect(state.error).toBeNull()
    })
  })

  describe('clearError アクション', () => {
    it('エラーがクリアされること', async () => {
      // エラー状態を作る
      vi.mocked(loginApi).mockRejectedValue(new Error('テストエラー'))
      try {
        await useAuthStore.getState().login('test@example.com', 'wrongpassword')
      } catch {
        // エラーは無視
      }

      // エラーが設定されていることを確認
      expect(useAuthStore.getState().error).toBe('テストエラー')

      // clearErrorを実行
      useAuthStore.getState().clearError()

      expect(useAuthStore.getState().error).toBeNull()
    })
  })
})
