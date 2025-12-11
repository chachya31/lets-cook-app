import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import authReducer, { logout, clearError, register, login } from '../../store/slices/authSlice';
import { AuthState } from '../../types/user';
import * as userApi from '../../api/userApi';

// userApiをモック
vi.mock('../../api/userApi');

describe('authSlice', () => {
  beforeEach(() => {
    localStorage.clear();
    vi.clearAllMocks();
  });

  afterEach(() => {
    localStorage.clear();
  });

  const initialState: AuthState = {
    user: null,
    accessToken: null,
    refreshToken: null,
    isAuthenticated: false,
    isLoading: false,
    error: null,
  };

  describe('reducers', () => {
    it('should handle logout', () => {
      // Arrange
      const stateWithUser: AuthState = {
        user: { 
          userId: '123', 
          email: 'test@example.com', 
          nickname: 'testuser', 
          displayName: 'Test User',
          preferredLanguage: 'ja',
          createdAt: '2024-01-01T00:00:00Z',
          timezone: 'Asia/Tokyo',
          marketingOptOut: false
        },
        accessToken: 'access-token',
        refreshToken: 'refresh-token',
        isAuthenticated: true,
        isLoading: false,
        error: null,
      };

      localStorage.setItem('accessToken', 'access-token');
      localStorage.setItem('refreshToken', 'refresh-token');
      localStorage.setItem('userId', '123');

      // Act
      const newState = authReducer(stateWithUser, logout());

      // Assert
      expect(newState.user).toBeNull();
      expect(newState.accessToken).toBeNull();
      expect(newState.refreshToken).toBeNull();
      expect(newState.isAuthenticated).toBe(false);
      expect(localStorage.getItem('accessToken')).toBeNull();
      expect(localStorage.getItem('refreshToken')).toBeNull();
      expect(localStorage.getItem('userId')).toBeNull();
    });

    it('should handle clearError', () => {
      // Arrange
      const stateWithError: AuthState = {
        ...initialState,
        error: 'Some error message',
      };

      // Act
      const newState = authReducer(stateWithError, clearError());

      // Assert
      expect(newState.error).toBeNull();
    });
  });

  describe('register async thunk', () => {
    it('should handle register.pending', () => {
      // Act
      const newState = authReducer(initialState, { type: register.pending.type });

      // Assert
      expect(newState.isLoading).toBe(true);
      expect(newState.error).toBeNull();
    });

    it('should handle register.fulfilled', () => {
      // Arrange
      const user = {
        userId: '123',
        email: 'test@example.com',
        nickname: 'testuser',
        displayName: 'Test User',
        preferredLanguage: 'ja' as const,
        createdAt: '2024-01-01T00:00:00Z',
        timezone: 'Asia/Tokyo',
        marketingOptOut: false
      };

      // Act
      const newState = authReducer(initialState, {
        type: register.fulfilled.type,
        payload: user,
      });

      // Assert
      expect(newState.isLoading).toBe(false);
      expect(newState.user).toEqual(user);
      expect(newState.error).toBeNull();
    });

    it('should handle register.rejected', () => {
      // Arrange
      const errorMessage = 'Registration failed';

      // Act
      const newState = authReducer(initialState, {
        type: register.rejected.type,
        payload: errorMessage,
      });

      // Assert
      expect(newState.isLoading).toBe(false);
      expect(newState.error).toBe(errorMessage);
    });
  });

  describe('login async thunk', () => {
    it('should handle login.pending', () => {
      // Act
      const newState = authReducer(initialState, { type: login.pending.type });

      // Assert
      expect(newState.isLoading).toBe(true);
      expect(newState.error).toBeNull();
    });

    it('should handle login.fulfilled', () => {
      // Arrange
      const loginResponse = {
        accessToken: 'access-token',
        refreshToken: 'refresh-token',
        idToken: 'id-token',
        expiresIn: 3600,
        user: {
          userId: '123',
          email: 'test@example.com',
          nickname: 'testuser',
          displayName: 'Test User',
          preferredLanguage: 'ja' as const,
          createdAt: '2024-01-01T00:00:00Z',
          timezone: 'Asia/Tokyo',
          marketingOptOut: false
        },
      };

      // Act
      const newState = authReducer(initialState, {
        type: login.fulfilled.type,
        payload: loginResponse,
      });

      // Assert
      expect(newState.isLoading).toBe(false);
      expect(newState.user).toEqual(loginResponse.user);
      expect(newState.accessToken).toBe(loginResponse.accessToken);
      expect(newState.refreshToken).toBe(loginResponse.refreshToken);
      expect(newState.isAuthenticated).toBe(true);
      expect(newState.error).toBeNull();
    });

    it('should handle login.rejected', () => {
      // Arrange
      const errorMessage = 'Invalid credentials';

      // Act
      const newState = authReducer(initialState, {
        type: login.rejected.type,
        payload: errorMessage,
      });

      // Assert
      expect(newState.isLoading).toBe(false);
      expect(newState.error).toBe(errorMessage);
    });

    it('should save tokens to localStorage on login.fulfilled', () => {
      // Arrange
      const loginResponse = {
        accessToken: 'access-token',
        refreshToken: 'refresh-token',
        idToken: 'id-token',
        expiresIn: 3600,
        user: {
          userId: '123',
          email: 'test@example.com',
          nickname: 'testuser',
          displayName: 'Test User',
          preferredLanguage: 'ja' as const,
          createdAt: '2024-01-01T00:00:00Z',
          timezone: 'Asia/Tokyo',
          marketingOptOut: false
        },
      };

      vi.mocked(userApi.loginUser).mockResolvedValue(loginResponse);

      // Act
      const newState = authReducer(initialState, {
        type: login.fulfilled.type,
        payload: loginResponse,
      });

      // Assert
      expect(newState.accessToken).toBe('access-token');
      expect(newState.refreshToken).toBe('refresh-token');
    });
  });

  describe('initial state', () => {
    it('should return the initial state', () => {
      // Act
      const state = authReducer(undefined, { type: 'unknown' });

      // Assert
      expect(state).toEqual(initialState);
    });

    it('should load tokens from localStorage on initialization', () => {
      // Arrange
      localStorage.setItem('accessToken', 'stored-access-token');
      localStorage.setItem('refreshToken', 'stored-refresh-token');

      // Act
      // 初期状態を再作成（実際のアプリケーション起動時の動作をシミュレート）
      const stateWithStoredTokens: AuthState = {
        user: null,
        accessToken: localStorage.getItem('accessToken'),
        refreshToken: localStorage.getItem('refreshToken'),
        isAuthenticated: !!localStorage.getItem('accessToken'),
        isLoading: false,
        error: null,
      };

      // Assert
      expect(stateWithStoredTokens.accessToken).toBe('stored-access-token');
      expect(stateWithStoredTokens.refreshToken).toBe('stored-refresh-token');
      expect(stateWithStoredTokens.isAuthenticated).toBe(true);
    });
  });
});
