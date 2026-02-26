/**
 * ユーザー型定義
 */

export interface User {
  userId: string;
  email: string;
  nickname: string;
  displayName: string;
  profileImageUrl?: string;
  preferredLanguage: 'ja' | 'ko';
  lastCookingDate?: string;
  lastLoginDate?: string;
  createdAt: string;
  timezone: string;
  marketingOptOut: boolean;
  roles?: string[];
}

export interface RegisterRequest {
  email: string;
  password: string;
  nickname: string;
  preferredLanguage?: 'ja' | 'ko';
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  idToken: string;
  expiresIn: number;
  user: User;
}

export interface AuthState {
  user: User | null;
  accessToken: string | null;
  refreshToken: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;
}
