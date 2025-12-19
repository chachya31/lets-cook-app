import { UpdateProfileRequest } from '../types/profile';
import { User } from '../types/user';
import { apiPostFile, apiPut } from '../utils/apiClient';

/**
 * プロフィール更新
 */
export const updateProfile = async (userId: string, data: UpdateProfileRequest): Promise<User> => {
  return apiPut<User>(`/api/users/profile/${userId}`, data, userId);
};

/**
 * プロフィール画像アップロード
 */
export const uploadProfileImage = async (userId: string, file: File): Promise<User> => {
  const formData = new FormData();
  formData.append('file', file);
  return apiPostFile<User>(`/api/users/profile/${userId}/image`, formData, userId);
};
