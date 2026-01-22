// Backend DTOに対応する型定義

// POST /api/auth/login のリクエスト型
export interface LoginRequest {
  email: string
  password: string
}

// ユーザープロフィール（UserProfileResponseに対応）
export interface UserProfile {
  userId: string
  email: string
  nickname: string | null
  displayName: string | null
  profileImageUrl: string | null
  preferredLanguage: string | null
  timezone: string | null
}

// POST /api/auth/login のレスポンス型
export interface LoginResponse {
  accessToken: string
  idToken: string
  refreshToken: string
  expiresIn: number
  user: UserProfile
}

// 認証トークン情報
export interface AuthTokens {
  accessToken: string
  idToken: string
  refreshToken: string
  expiresIn: number
}
