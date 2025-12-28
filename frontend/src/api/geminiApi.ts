import { apiDelete, apiGet, apiPost } from '../utils/apiClient';

// Response types
export interface ChatResponse {
  conversationId: string;
  response: string;
}

export interface ConversationResponse {
  conversationId: string;
  title: string;
  conversationType: string;
  createdAt: string;
  updatedAt: string;
}

export interface MessageResponse {
  messageId: string;
  role: 'user' | 'assistant';
  content: string;
  createdAt: string;
}

/**
 * 新しい会話を開始してチャット
 */
export const startChat = async (message: string): Promise<ChatResponse> => {
  return apiPost<ChatResponse>('/api/ai/chat', { message });
};

/**
 * 会話一覧を取得
 */
export const getConversations = async (): Promise<ConversationResponse[]> => {
  return apiGet<ConversationResponse[]>('/api/ai/conversations');
};

/**
 * 会話のメッセージ一覧を取得
 */
export const getMessages = async (conversationId: string): Promise<MessageResponse[]> => {
  return apiGet<MessageResponse[]>(`/api/ai/conversations/${conversationId}/messages`);
};

/**
 * 既存の会話にメッセージを送信
 */
export const sendMessage = async (
  conversationId: string,
  message: string
): Promise<ChatResponse> => {
  return apiPost<ChatResponse>(`/api/ai/conversations/${conversationId}/messages`, { message });
};

/**
 * 会話を削除
 */
export const deleteConversation = async (conversationId: string): Promise<void> => {
  return apiDelete<void>(`/api/ai/conversations/${conversationId}`);
};

// Legacy support - 後方互換性のため
export const chatWithGemini = startChat;
