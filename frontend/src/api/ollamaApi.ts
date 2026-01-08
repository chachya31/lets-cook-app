import { apiPost } from '../utils/apiClient';

// Response types
export interface OllamaGenerateResponse {
  response: string;
}

/**
 * Ollamaでテキスト生成
 */
export const generateWithOllama = async (prompt: string): Promise<OllamaGenerateResponse> => {
  return apiPost<OllamaGenerateResponse>('/api/ollama/generate', { prompt });
};
