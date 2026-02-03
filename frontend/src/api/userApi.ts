import { LoginRequest, LoginResponse, RegisterRequest, User } from '../types/user';
import { apiGet, apiPost } from '../utils/apiClient';

/**
 * ユーザー登録
 */
export const registerUser = async (data: RegisterRequest): Promise<User> => {
  return apiPost<User>('/api/users/register', data);
};

/**
 * ログイン
 */
export const loginUser = async (data: LoginRequest): Promise<LoginResponse> => {
  return apiPost<LoginResponse>('/api/users/login', data);
};

/**
 * メール確認コード検証
 */
export const confirmSignUp = async (email: string, confirmationCode: string): Promise<void> => {
  return apiPost<void>('/api/users/confirm', { email, confirmationCode });
};

/**
 * 確認コード再送信
 */
export const resendConfirmationCode = async (email: string): Promise<void> => {
  return apiPost<void>('/api/users/resend-code', { email });
};

/**
 * プロフィール取得
 */
export const getUserProfile = async (userId: string): Promise<User> => {
  return apiGet<User>(`/api/users/profile/${userId}`);
};
