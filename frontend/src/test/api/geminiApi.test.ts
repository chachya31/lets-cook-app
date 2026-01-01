import { beforeEach, describe, expect, it, vi } from 'vitest';
import {
    deleteConversation,
    getConversations,
    getMessages,
    sendMessage,
    startChat,
} from '../../api/geminiApi';
import * as apiClient from '../../utils/apiClient';

// apiClientをモック
vi.mock('../../utils/apiClient');

describe('geminiApi', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('startChat', () => {
    it('should call apiPost with message and default conversation type', async () => {
      const mockResponse = {
        conversationId: 'conv-123',
        response: 'AIからの応答です',
      };
      vi.mocked(apiClient.apiPost).mockResolvedValue(mockResponse);

      const result = await startChat('こんにちは');

      expect(apiClient.apiPost).toHaveBeenCalledWith('/api/ai/chat', {
        message: 'こんにちは',
        conversationType: 'general',
      });
      expect(result).toEqual(mockResponse);
    });

    it('should call apiPost with specified conversation type', async () => {
      const mockResponse = {
        conversationId: 'conv-456',
        response: 'レシピを提案します',
      };
      vi.mocked(apiClient.apiPost).mockResolvedValue(mockResponse);

      const result = await startChat('冷蔵庫にある食材でレシピを教えて', 'recipe_recommendation');

      expect(apiClient.apiPost).toHaveBeenCalledWith('/api/ai/chat', {
        message: '冷蔵庫にある食材でレシピを教えて',
        conversationType: 'recipe_recommendation',
      });
      expect(result).toEqual(mockResponse);
    });

    it('should handle expiry_check conversation type', async () => {
      const mockResponse = {
        conversationId: 'conv-789',
        response: '賞味期限をチェックします',
      };
      vi.mocked(apiClient.apiPost).mockResolvedValue(mockResponse);

      const result = await startChat('賞味期限が近い食材は？', 'expiry_check');

      expect(apiClient.apiPost).toHaveBeenCalledWith('/api/ai/chat', {
        message: '賞味期限が近い食材は？',
        conversationType: 'expiry_check',
      });
      expect(result).toEqual(mockResponse);
    });

    it('should handle errors from apiPost', async () => {
      const error = new Error('API error');
      vi.mocked(apiClient.apiPost).mockRejectedValue(error);

      await expect(startChat('テスト')).rejects.toThrow('API error');
    });
  });

  describe('getConversations', () => {
    it('should call apiGet and return conversations list', async () => {
      const mockConversations = [
        {
          conversationId: 'conv-1',
          title: '料理の相談',
          conversationType: 'general' as const,
          createdAt: '2025-01-01T10:00:00Z',
          updatedAt: '2025-01-01T11:00:00Z',
        },
        {
          conversationId: 'conv-2',
          title: 'レシピ推薦',
          conversationType: 'recipe_recommendation' as const,
          createdAt: '2025-01-02T10:00:00Z',
          updatedAt: '2025-01-02T12:00:00Z',
        },
      ];
      vi.mocked(apiClient.apiGet).mockResolvedValue(mockConversations);

      const result = await getConversations();

      expect(apiClient.apiGet).toHaveBeenCalledWith('/api/ai/conversations');
      expect(result).toEqual(mockConversations);
      expect(result).toHaveLength(2);
    });

    it('should return empty array when no conversations', async () => {
      vi.mocked(apiClient.apiGet).mockResolvedValue([]);

      const result = await getConversations();

      expect(result).toEqual([]);
    });

    it('should handle errors from apiGet', async () => {
      const error = new Error('Failed to fetch conversations');
      vi.mocked(apiClient.apiGet).mockRejectedValue(error);

      await expect(getConversations()).rejects.toThrow('Failed to fetch conversations');
    });
  });

  describe('getMessages', () => {
    it('should call apiGet with conversation ID and return messages', async () => {
      const conversationId = 'conv-123';
      const mockMessages = [
        {
          messageId: 'msg-1',
          role: 'user' as const,
          content: 'こんにちは',
          createdAt: '2025-01-01T10:00:00Z',
        },
        {
          messageId: 'msg-2',
          role: 'assistant' as const,
          content: 'こんにちは！何かお手伝いできますか？',
          createdAt: '2025-01-01T10:00:05Z',
        },
      ];
      vi.mocked(apiClient.apiGet).mockResolvedValue(mockMessages);

      const result = await getMessages(conversationId);

      expect(apiClient.apiGet).toHaveBeenCalledWith(`/api/ai/conversations/${conversationId}/messages`);
      expect(result).toEqual(mockMessages);
    });

    it('should return empty array for new conversation', async () => {
      vi.mocked(apiClient.apiGet).mockResolvedValue([]);

      const result = await getMessages('new-conv');

      expect(result).toEqual([]);
    });

    it('should handle errors from apiGet', async () => {
      const error = new Error('Conversation not found');
      vi.mocked(apiClient.apiGet).mockRejectedValue(error);

      await expect(getMessages('invalid-id')).rejects.toThrow('Conversation not found');
    });
  });

  describe('sendMessage', () => {
    it('should call apiPost with conversation ID and message', async () => {
      const conversationId = 'conv-123';
      const message = '次の質問です';
      const mockResponse = {
        conversationId: 'conv-123',
        response: 'AIの応答です',
      };
      vi.mocked(apiClient.apiPost).mockResolvedValue(mockResponse);

      const result = await sendMessage(conversationId, message);

      expect(apiClient.apiPost).toHaveBeenCalledWith(
        `/api/ai/conversations/${conversationId}/messages`,
        { message }
      );
      expect(result).toEqual(mockResponse);
    });

    it('should handle errors from apiPost', async () => {
      const error = new Error('Failed to send message');
      vi.mocked(apiClient.apiPost).mockRejectedValue(error);

      await expect(sendMessage('conv-123', 'テスト')).rejects.toThrow('Failed to send message');
    });
  });

  describe('deleteConversation', () => {
    it('should call apiDelete with conversation ID', async () => {
      const conversationId = 'conv-123';
      vi.mocked(apiClient.apiDelete).mockResolvedValue(undefined);

      await deleteConversation(conversationId);

      expect(apiClient.apiDelete).toHaveBeenCalledWith(`/api/ai/conversations/${conversationId}`);
    });

    it('should handle errors from apiDelete', async () => {
      const error = new Error('Failed to delete conversation');
      vi.mocked(apiClient.apiDelete).mockRejectedValue(error);

      await expect(deleteConversation('conv-123')).rejects.toThrow('Failed to delete conversation');
    });
  });
});
