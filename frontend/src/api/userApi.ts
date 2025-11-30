import { RegisterRequest, LoginRequest, LoginResponse, User } from '../types/user';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

/**
 * ユーザー登録
 */
export const registerUser = async (data: RegisterRequest): Promise<User> => {
  const response = await fetch(`${API_BASE_URL}/api/users/register`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(data),
  });

  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message || 'Registration failed');
  }

  return response.json();
};

/**
 * ログイン
 */
export const loginUser = async (data: LoginRequest): Promise<LoginResponse> => {
  const response = await fetch(`${API_BASE_URL}/api/users/login`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(data),
  });

  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message || 'Login failed');
  }

  return response.json();
};

/**
 * メール確認コード検証
 */
export const confirmSignUp = async (email: string, confirmationCode: string): Promise<void> => {
  const response = await fetch(`${API_BASE_URL}/api/users/confirm`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ email, confirmationCode }),
  });

  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message || 'Confirmation failed');
  }
};

/**
 * 確認コード再送信
 */
export const resendConfirmationCode = async (email: string): Promise<void> => {
  const response = await fetch(`${API_BASE_URL}/api/users/resend-code`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ email }),
  });

  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message || 'Resend failed');
  }
};

/**
 * プロフィール取得
 */
export const getUserProfile = async (userId: string, token: string): Promise<User> => {
  const response = await fetch(`${API_BASE_URL}/api/users/profile/${userId}`, {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json',
    },
  });

  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message || 'Failed to fetch profile');
  }

  return response.json();
};
