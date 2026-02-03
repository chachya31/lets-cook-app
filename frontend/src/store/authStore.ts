import { create } from 'zustand';
import { loginUser, registerUser } from '../api/userApi';
import { LoginRequest, RegisterRequest, User } from '../types/user';

interface AuthState {
  user: User | null;
  cognitoSub: string | null;
  accessToken: string | null;
  refreshToken: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;
}

interface AuthActions {
  login: (data: LoginRequest) => Promise<void>;
  register: (data: RegisterRequest) => Promise<User>;
  logout: () => void;
  clearError: () => void;
  updateUser: (userData: Partial<User>) => void;
}

type AuthStore = AuthState & AuthActions;

/**
 * JWTトークンからペイロードを抽出
 */
const decodeJwtPayload = (token: string): Record<string, unknown> | null => {
  try {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    );
    return JSON.parse(jsonPayload);
  } catch {
    return null;
  }
};

/**
 * JWTトークンからCognitoグループを抽出
 */
const extractRolesFromToken = (idToken: string): string[] => {
  const payload = decodeJwtPayload(idToken);
  if (payload && Array.isArray(payload['cognito:groups'])) {
    return payload['cognito:groups'] as string[];
  }
  return [];
};

/**
 * JWTトークンからCognito sub（ユーザーID）を抽出
 */
const extractCognitoSubFromToken = (idToken: string): string | null => {
  const payload = decodeJwtPayload(idToken);
  if (payload && typeof payload['sub'] === 'string') {
    return payload['sub'];
  }
  return null;
};

/**
 * localStorageからユーザー情報を復元
 */
const loadUserFromStorage = (): User | null => {
  const userJson = localStorage.getItem('user');
  if (userJson) {
    try {
      return JSON.parse(userJson);
    } catch {
      return null;
    }
  }
  return null;
};

/**
 * localStorageからCognito subを復元
 */
const loadCognitoSubFromStorage = (): string | null => {
  return localStorage.getItem('cognitoSub');
};

/**
 * Zustand認証ストア
 */
export const useAuthStore = create<AuthStore>((set) => ({
  // 初期状態
  user: loadUserFromStorage(),
  cognitoSub: loadCognitoSubFromStorage(),
  accessToken: localStorage.getItem('accessToken'),
  refreshToken: localStorage.getItem('refreshToken'),
  isAuthenticated: !!localStorage.getItem('accessToken'),
  isLoading: false,
  error: null,

  // ログイン
  login: async (data: LoginRequest) => {
    set({ isLoading: true, error: null });
    try {
      const response = await loginUser(data);

      // JWTからロール（グループ）とCognito subを抽出
      const roles = extractRolesFromToken(response.idToken);
      const cognitoSub = extractCognitoSubFromToken(response.idToken);
      const userWithRoles = { ...response.user, roles };

      // トークンとユーザー情報をlocalStorageに保存
      localStorage.setItem('accessToken', response.accessToken);
      localStorage.setItem('refreshToken', response.refreshToken);
      localStorage.setItem('idToken', response.idToken);
      localStorage.setItem('userId', response.user.userId);
      localStorage.setItem('user', JSON.stringify(userWithRoles));
      if (cognitoSub) {
        localStorage.setItem('cognitoSub', cognitoSub);
      }

      set({
        user: userWithRoles,
        cognitoSub,
        accessToken: response.accessToken,
        refreshToken: response.refreshToken,
        isAuthenticated: true,
        isLoading: false,
        error: null,
      });
    } catch (error) {
      const errorMessage = (error as Error).message;
      set({ isLoading: false, error: errorMessage });
      throw error;
    }
  },

  // ユーザー登録
  register: async (data: RegisterRequest) => {
    set({ isLoading: true, error: null });
    try {
      const user = await registerUser(data);
      set({ isLoading: false, error: null });
      return user;
    } catch (error) {
      const errorMessage = (error as Error).message;
      set({ isLoading: false, error: errorMessage });
      throw error;
    }
  },

  // ログアウト
  logout: () => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('idToken');
    localStorage.removeItem('userId');
    localStorage.removeItem('user');
    localStorage.removeItem('cognitoSub');
    localStorage.removeItem('alert_dismissed_date');

    set({
      user: null,
      cognitoSub: null,
      accessToken: null,
      refreshToken: null,
      isAuthenticated: false,
      error: null,
    });
  },

  // エラークリア
  clearError: () => {
    set({ error: null });
  },

  // ユーザー情報更新
  updateUser: (userData: Partial<User>) => {
    set((state) => {
      if (state.user) {
        const updatedUser = { ...state.user, ...userData };
        localStorage.setItem('user', JSON.stringify(updatedUser));
        return { user: updatedUser };
      }
      return state;
    });
  },
}));
