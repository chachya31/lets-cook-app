import { act } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import * as userApi from '../../api/userApi';
import { useAuthStore } from '../../store/authStore';

vi.mock('../../api/userApi');

describe('authStore', () => {
  beforeEach(() => {
    localStorage.clear();
    vi.clearAllMocks();
    // ストアをリセット
    useAuthStore.setState({
      user: null,
      accessToken: null,
      refreshToken: null,
      isAuthenticated: false,
      isLoading: false,
      error: null,
    });
  });

  afterEach(() => {
    localStorage.clear();
  });

  describe('logout', () => {
    it('should clear user state and localStorage', () => {
      // Arrange
      const user = {
        userId: '123',
        email: 'test@example.com',
        nickname: 'testuser',
        displayName: 'Test User',
        preferredLanguage: 'ja' as const,
        createdAt: '2024-01-01T00:00:00Z',
        timezone: 'Asia/Tokyo',
        marketingOptOut: false,
      };
      useAuthStore.setState({
        user,
        accessToken: 'access-token',
        refreshToken: 'refresh-token',
        isAuthenticated: true,
      });
      localStorage.setItem('accessToken', 'access-token');
      localStorage.setItem('refreshToken', 'refresh-token');
      localStorage.setItem('userId', '123');

      // Act
      act(() => {
        useAuthStore.getState().logout();
      });

      // Assert
      const state = useAuthStore.getState();
      expect(state.user).toBeNull();
      expect(state.accessToken).toBeNull();
      expect(state.refreshToken).toBeNull();
      expect(state.isAuthenticated).toBe(false);
      expect(localStorage.getItem('accessToken')).toBeNull();
      expect(localStorage.getItem('refreshToken')).toBeNull();
      expect(localStorage.getItem('userId')).toBeNull();
    });
  });

  describe('clearError', () => {
    it('should clear error state', () => {
      // Arrange
      useAuthStore.setState({ error: 'Some error message' });

      // Act
      act(() => {
        useAuthStore.getState().clearError();
      });

      // Assert
      expect(useAuthStore.getState().error).toBeNull();
    });
  });

  describe('register', () => {
    it('should set isLoading to true during registration', async () => {
      // Arrange
      const user = {
        userId: '123',
        email: 'test@example.com',
        nickname: 'testuser',
        displayName: 'Test User',
        preferredLanguage: 'ja' as const,
        createdAt: '2024-01-01T00:00:00Z',
        timezone: 'Asia/Tokyo',
        marketingOptOut: false,
      };
      vi.mocked(userApi.registerUser).mockResolvedValue(user);

      // Act
      const registerPromise = useAuthStore.getState().register({
        email: 'test@example.com',
        password: 'password123',
        nickname: 'testuser',
        preferredLanguage: 'ja',
      });

      // Assert - isLoading should be true during the call
      expect(useAuthStore.getState().isLoading).toBe(true);

      await registerPromise;

      // After completion
      expect(useAuthStore.getState().isLoading).toBe(false);
    });

    it('should return user on successful registration', async () => {
      // Arrange
      const user = {
        userId: '123',
        email: 'test@example.com',
        nickname: 'testuser',
        displayName: 'Test User',
        preferredLanguage: 'ja' as const,
        createdAt: '2024-01-01T00:00:00Z',
        timezone: 'Asia/Tokyo',
        marketingOptOut: false,
      };
      vi.mocked(userApi.registerUser).mockResolvedValue(user);

      // Act
      const result = await useAuthStore.getState().register({
        email: 'test@example.com',
        password: 'password123',
        nickname: 'testuser',
        preferredLanguage: 'ja',
      });

      // Assert
      expect(result).toEqual(user);
      expect(useAuthStore.getState().error).toBeNull();
    });

    it('should set error on registration failure', async () => {
      // Arrange
      vi.mocked(userApi.registerUser).mockRejectedValue(new Error('Registration failed'));

      // Act & Assert
      await expect(
        useAuthStore.getState().register({
          email: 'test@example.com',
          password: 'password123',
          nickname: 'testuser',
          preferredLanguage: 'ja',
        })
      ).rejects.toThrow('Registration failed');

      expect(useAuthStore.getState().error).toBe('Registration failed');
      expect(useAuthStore.getState().isLoading).toBe(false);
    });
  });

  describe('login', () => {
    it('should set user and tokens on successful login', async () => {
      // Arrange
      // JWT payload: {"sub": "cognito-sub-123", "cognito:groups": ["users"]}
      const loginResponse = {
        accessToken: 'access-token',
        refreshToken: 'refresh-token',
        idToken:
          'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJjb2duaXRvLXN1Yi0xMjMiLCJjb2duaXRvOmdyb3VwcyI6WyJ1c2VycyJdfQ.test',
        expiresIn: 3600,
        user: {
          userId: '123',
          email: 'test@example.com',
          nickname: 'testuser',
          displayName: 'Test User',
          preferredLanguage: 'ja' as const,
          createdAt: '2024-01-01T00:00:00Z',
          timezone: 'Asia/Tokyo',
          marketingOptOut: false,
        },
      };
      vi.mocked(userApi.loginUser).mockResolvedValue(loginResponse);

      // Act
      await useAuthStore.getState().login({
        email: 'test@example.com',
        password: 'password123',
      });

      // Assert
      const state = useAuthStore.getState();
      expect(state.user?.email).toBe('test@example.com');
      expect(state.accessToken).toBe('access-token');
      expect(state.refreshToken).toBe('refresh-token');
      expect(state.isAuthenticated).toBe(true);
      expect(state.isLoading).toBe(false);
      expect(state.error).toBeNull();
    });

    it('should save tokens to localStorage on successful login', async () => {
      // Arrange
      // JWT payload: {"sub": "cognito-sub-456", "cognito:groups": []}
      const loginResponse = {
        accessToken: 'access-token',
        refreshToken: 'refresh-token',
        idToken:
          'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJjb2duaXRvLXN1Yi00NTYiLCJjb2duaXRvOmdyb3VwcyI6W119.test',
        expiresIn: 3600,
        user: {
          userId: '123',
          email: 'test@example.com',
          nickname: 'testuser',
          displayName: 'Test User',
          preferredLanguage: 'ja' as const,
          createdAt: '2024-01-01T00:00:00Z',
          timezone: 'Asia/Tokyo',
          marketingOptOut: false,
        },
      };
      vi.mocked(userApi.loginUser).mockResolvedValue(loginResponse);

      // Act
      await useAuthStore.getState().login({
        email: 'test@example.com',
        password: 'password123',
      });

      // Assert
      expect(localStorage.getItem('accessToken')).toBe('access-token');
      expect(localStorage.getItem('refreshToken')).toBe('refresh-token');
      expect(localStorage.getItem('userId')).toBe('123');
    });

    it('should set error on login failure', async () => {
      // Arrange
      vi.mocked(userApi.loginUser).mockRejectedValue(new Error('Invalid credentials'));

      // Act & Assert
      await expect(
        useAuthStore.getState().login({
          email: 'test@example.com',
          password: 'wrongpassword',
        })
      ).rejects.toThrow('Invalid credentials');

      expect(useAuthStore.getState().error).toBe('Invalid credentials');
      expect(useAuthStore.getState().isAuthenticated).toBe(false);
    });
  });

  describe('updateUser', () => {
    it('should update user data', () => {
      // Arrange
      const user = {
        userId: '123',
        email: 'test@example.com',
        nickname: 'testuser',
        displayName: 'Test User',
        preferredLanguage: 'ja' as const,
        createdAt: '2024-01-01T00:00:00Z',
        timezone: 'Asia/Tokyo',
        marketingOptOut: false,
      };
      useAuthStore.setState({ user });

      // Act
      act(() => {
        useAuthStore.getState().updateUser({ nickname: 'newname' });
      });

      // Assert
      expect(useAuthStore.getState().user?.nickname).toBe('newname');
      expect(useAuthStore.getState().user?.email).toBe('test@example.com');
    });

    it('should update localStorage when user is updated', () => {
      // Arrange
      const user = {
        userId: '123',
        email: 'test@example.com',
        nickname: 'testuser',
        displayName: 'Test User',
        preferredLanguage: 'ja' as const,
        createdAt: '2024-01-01T00:00:00Z',
        timezone: 'Asia/Tokyo',
        marketingOptOut: false,
      };
      useAuthStore.setState({ user });

      // Act
      act(() => {
        useAuthStore.getState().updateUser({ nickname: 'newname' });
      });

      // Assert
      const storedUser = JSON.parse(localStorage.getItem('user') || '{}');
      expect(storedUser.nickname).toBe('newname');
    });
  });
});
