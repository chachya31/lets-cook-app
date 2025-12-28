import { apiPost } from '../utils/apiClient';

export interface GeminiChatResponse {
  response: string;
}

/**
 * Gemini チャット
 */
export const chatWithGemini = async (message: string): Promise<GeminiChatResponse> => {
  return apiPost<GeminiChatResponse>('/api/ai/chat', { message });
};
