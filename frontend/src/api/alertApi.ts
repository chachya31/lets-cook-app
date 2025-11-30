import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

export interface AlertResponse {
  shouldShow: boolean;
  message: string | null;
}

/**
 * アラート表示判定を取得
 */
export const checkAlert = async (userId: string): Promise<AlertResponse> => {
  const response = await axios.get<AlertResponse>(`${API_BASE_URL}/api/alerts/check`, {
    headers: {
      'X-User-Id': userId,
    },
  });
  return response.data;
};
