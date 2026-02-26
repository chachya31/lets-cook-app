import { beforeEach, describe, expect, it, vi } from 'vitest';
import { updateProfile, uploadProfileImage } from '../../api/profileApi';
import { UpdateProfileRequest } from '../../types/profile';
import { User } from '../../types/user';
import * as apiClient from '../../utils/apiClient';

// apiClientをモック
vi.mock('../../utils/apiClient');

describe('profileApi', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('updateProfile', () => {
    it('should call apiPut with correct parameters', async () => {
      // Arrange
      const userId = 'user-123';
      const updateData: UpdateProfileRequest = {
        nickname: 'newnickname',
        displayName: 'New Display Name',
        preferredLanguage: 'ko',
        timezone: 'Asia/Seoul',
        marketingOptOut: true,
      };

      const expectedUser: User = {
        userId: 'user-123',
        email: 'test@example.com',
        nickname: 'newnickname',
        displayName: 'New Display Name',
        preferredLanguage: 'ko',
        createdAt: '2024-01-01T00:00:00Z',
        timezone: 'Asia/Seoul',
        marketingOptOut: true,
      };

      vi.mocked(apiClient.apiPut).mockResolvedValue(expectedUser);

      // Act
      const result = await updateProfile(userId, updateData);

      // Assert
      expect(apiClient.apiPut).toHaveBeenCalledWith(`/api/users/profile/${userId}`, updateData);
      expect(result).toEqual(expectedUser);
    });

    it('should handle errors from apiPut', async () => {
      // Arrange
      const userId = 'user-123';
      const updateData: UpdateProfileRequest = {
        nickname: 'newnickname',
        displayName: 'New Display Name',
        preferredLanguage: 'ja',
        timezone: 'Asia/Tokyo',
        marketingOptOut: false,
      };

      const error = new Error('Update failed');
      vi.mocked(apiClient.apiPut).mockRejectedValue(error);

      // Act & Assert
      await expect(updateProfile(userId, updateData)).rejects.toThrow('Update failed');
    });
  });

  describe('uploadProfileImage', () => {
    it('should call apiPostFile with correct parameters', async () => {
      // Arrange
      const userId = 'user-123';
      const file = new File(['image content'], 'profile.jpg', { type: 'image/jpeg' });

      const expectedUser: User = {
        userId: 'user-123',
        email: 'test@example.com',
        nickname: 'testuser',
        displayName: 'Test User',
        preferredLanguage: 'ja',
        profileImageUrl: 'https://example.com/profile.jpg',
        createdAt: '2024-01-01T00:00:00Z',
        timezone: 'Asia/Tokyo',
        marketingOptOut: false,
      };

      vi.mocked(apiClient.apiPostFile).mockResolvedValue(expectedUser);

      // Act
      const result = await uploadProfileImage(userId, file);

      // Assert
      expect(apiClient.apiPostFile).toHaveBeenCalledWith(
        `/api/users/profile/${userId}/image`,
        expect.any(FormData)
      );

      // FormDataの内容を検証
      const callArgs = vi.mocked(apiClient.apiPostFile).mock.calls[0];
      const formData = callArgs[1] as FormData;
      expect(formData.get('file')).toBe(file);

      expect(result).toEqual(expectedUser);
    });

    it('should handle errors from apiPostFile', async () => {
      // Arrange
      const userId = 'user-123';
      const file = new File(['image content'], 'profile.jpg', { type: 'image/jpeg' });

      const error = new Error('Upload failed');
      vi.mocked(apiClient.apiPostFile).mockRejectedValue(error);

      // Act & Assert
      await expect(uploadProfileImage(userId, file)).rejects.toThrow('Upload failed');
    });
  });
});
