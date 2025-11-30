import { apiGet } from '../utils/apiClient';

export interface AlertResponse {
  shouldShow: boolean;
  message: string | null;
}

/**
 * アラート表示判定を取得
 */
export const checkAlert = async (userId: string): Promise<AlertResponse> => {
  return apiGet<AlertResponse>('/api/alerts/check', userId);
};
