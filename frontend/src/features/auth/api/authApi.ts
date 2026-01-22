import { apiClient } from '@/lib/axios'
import type { LoginRequest, LoginResponse } from '../types'

// POST /api/auth/login
export const loginApi = async (request: LoginRequest): Promise<LoginResponse> => {
  const response = await apiClient.post<LoginResponse>('/auth/login', request)
  return response.data
}
