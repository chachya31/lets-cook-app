import { describe, it, expect, vi, beforeEach } from 'vitest'
import { loginApi } from './authApi'
import type { LoginRequest, LoginResponse } from '../types'

// apiClientをモック
vi.mock('@/lib/axios', () => ({
  apiClient: {
    post: vi.fn(),
  },
}))

import { apiClient } from '@/lib/axios'

const mockLoginRequest: LoginRequest = {
  email: 'test@example.com',
  password: 'password123',
}

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

describe('authApi', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('loginApi', () => {
    it('正しいエンドポイント（/auth/login）にPOSTリクエストを送ること', async () => {
      vi.mocked(apiClient.post).mockResolvedValue({ data: mockLoginResponse })

      await loginApi(mockLoginRequest)

      expect(apiClient.post).toHaveBeenCalledTimes(1)
      expect(apiClient.post).toHaveBeenCalledWith('/auth/login', mockLoginRequest)
    })

    it('APIレスポンスのdataを返すこと', async () => {
      vi.mocked(apiClient.post).mockResolvedValue({ data: mockLoginResponse })

      const result = await loginApi(mockLoginRequest)

      expect(result).toEqual(mockLoginResponse)
    })

    it('APIエラー時に例外がスローされること', async () => {
      const errorResponse = {
        response: {
          status: 401,
          data: { message: 'Invalid credentials' },
        },
      }
      vi.mocked(apiClient.post).mockRejectedValue(errorResponse)

      await expect(loginApi(mockLoginRequest)).rejects.toEqual(errorResponse)
    })

    it('ネットワークエラー時に例外がスローされること', async () => {
      const networkError = new Error('Network Error')
      vi.mocked(apiClient.post).mockRejectedValue(networkError)

      await expect(loginApi(mockLoginRequest)).rejects.toThrow('Network Error')
    })

    it('タイムアウトエラー時に例外がスローされること', async () => {
      const timeoutError = new Error('timeout of 10000ms exceeded')
      vi.mocked(apiClient.post).mockRejectedValue(timeoutError)

      await expect(loginApi(mockLoginRequest)).rejects.toThrow('timeout of 10000ms exceeded')
    })
  })
})
