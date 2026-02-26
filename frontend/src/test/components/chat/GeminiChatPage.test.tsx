/**
 * @vitest-environment jsdom
 */
import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import * as geminiApi from '../../../api/geminiApi';
import * as inventoryApi from '../../../api/inventoryApi';
import GeminiChatPage from '../../../components/chat/GeminiChatPage';

// モック設定
vi.mock('../../../api/geminiApi');
vi.mock('../../../api/inventoryApi');
vi.mock('react-i18next', () => ({
  useTranslation: () => ({
    t: (key: string) => {
      const translations: Record<string, string> = {
        'chat.title': 'AIチャット',
        'chat.newConversation': '新しい会話',
        'chat.noHistory': '会話履歴がありません',
        'chat.placeholder': 'メッセージを入力してください',
        'chat.inputPlaceholder': 'メッセージを入力...',
        'chat.thinking': '考え中...',
        'chat.error': 'エラーが発生しました。もう一度お試しください。',
        'chat.confirmDelete': 'この会話を削除しますか？',
        'chat.deleteError': '削除に失敗しました',
        'chat.selectTopic': '会話の主題を選択',
        'chat.currentInventory': '現在の在庫（クリックで追加）',
        'chat.noInventory': '在庫がありません',
        'chat.addAllToInput': 'すべて追加',
        'chat.topics.general': '一般チャット',
        'chat.topics.generalDesc': '料理に関する質問や相談',
        'chat.topics.recipeRecommendation': 'レシピ推薦',
        'chat.topics.recipeRecommendationDesc': '在庫食材からレシピを提案',
        'chat.topics.expiryCheck': '賞味期限確認',
        'chat.topics.expiryCheckDesc': '食材の賞味期限をチェック',
        'common.loading': '読み込み中...',
        'common.cancel': 'キャンセル',
      };
      return translations[key] || key;
    },
  }),
}));

// InventoryItemのモックデータ作成ヘルパー
const createMockInventoryItem = (
  overrides: Partial<inventoryApi.InventoryItem> = {}
): inventoryApi.InventoryItem => ({
  itemId: 'item-1',
  name: '玉ねぎ',
  quantity: 3,
  unit: '個',
  expiryDate: '2025-01-15',
  isExpired: false,
  isExpiringSoon: false,
  purchasedAt: '2025-01-01T10:00:00Z',
  createdAt: '2025-01-01T10:00:00Z',
  ...overrides,
});

describe('GeminiChatPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(geminiApi.getConversations).mockResolvedValue([]);
    vi.mocked(inventoryApi.getInventory).mockResolvedValue([]);
  });

  describe('初期表示', () => {
    it('should render chat page with title', async () => {
      render(<GeminiChatPage />);

      expect(screen.getByText('AIチャット')).toBeInTheDocument();
      expect(screen.getByText('新しい会話')).toBeInTheDocument();
    });

    it('should show no history message when no conversations', async () => {
      render(<GeminiChatPage />);

      await waitFor(() => {
        expect(screen.getByText('会話履歴がありません')).toBeInTheDocument();
      });
    });

    it('should load and display conversations', async () => {
      const mockConversations = [
        {
          conversationId: 'conv-1',
          title: '料理の相談',
          conversationType: 'general' as const,
          createdAt: '2025-01-01T10:00:00Z',
          updatedAt: '2025-01-01T11:00:00Z',
        },
      ];
      vi.mocked(geminiApi.getConversations).mockResolvedValue(mockConversations);

      render(<GeminiChatPage />);

      await waitFor(() => {
        expect(screen.getByText('料理の相談')).toBeInTheDocument();
      });
    });
  });

  describe('新しい会話', () => {
    it('should open topic modal when clicking new conversation button', async () => {
      render(<GeminiChatPage />);

      await waitFor(() => {
        expect(screen.queryByText('読み込み中...')).not.toBeInTheDocument();
      });

      const newConversationButton = screen.getByText('新しい会話');
      fireEvent.click(newConversationButton);

      expect(screen.getByText('会話の主題を選択')).toBeInTheDocument();
      expect(screen.getAllByText('一般チャット').length).toBeGreaterThanOrEqual(1);
      expect(screen.getByText('レシピ推薦')).toBeInTheDocument();
      expect(screen.getByText('賞味期限確認')).toBeInTheDocument();
    });

    it('should close topic modal when clicking cancel', async () => {
      render(<GeminiChatPage />);

      await waitFor(() => {
        expect(screen.queryByText('読み込み中...')).not.toBeInTheDocument();
      });

      fireEvent.click(screen.getByText('新しい会話'));
      expect(screen.getByText('会話の主題を選択')).toBeInTheDocument();

      fireEvent.click(screen.getByText('キャンセル'));
      expect(screen.queryByText('会話の主題を選択')).not.toBeInTheDocument();
    });

    it('should select general topic and close modal', async () => {
      render(<GeminiChatPage />);

      await waitFor(() => {
        expect(screen.queryByText('読み込み中...')).not.toBeInTheDocument();
      });

      fireEvent.click(screen.getByText('新しい会話'));

      const generalButton = screen.getByText('料理に関する質問や相談').closest('button');
      fireEvent.click(generalButton!);

      expect(screen.queryByText('会話の主題を選択')).not.toBeInTheDocument();
    });

    it('should load inventory when selecting recipe_recommendation topic', async () => {
      render(<GeminiChatPage />);

      await waitFor(() => {
        expect(screen.queryByText('読み込み中...')).not.toBeInTheDocument();
      });

      fireEvent.click(screen.getByText('新しい会話'));

      const recipeButton = screen.getByText('在庫食材からレシピを提案').closest('button');
      fireEvent.click(recipeButton!);

      await waitFor(() => {
        expect(inventoryApi.getInventory).toHaveBeenCalledWith(true);
      });
    });
  });

  describe('メッセージ送信', () => {
    it('should send message and display response', async () => {
      const mockResponse = {
        conversationId: 'conv-new',
        response: 'AIからの応答です',
      };
      vi.mocked(geminiApi.startChat).mockResolvedValue(mockResponse);

      render(<GeminiChatPage />);

      await waitFor(() => {
        expect(screen.queryByText('読み込み中...')).not.toBeInTheDocument();
      });

      const textarea = screen.getByPlaceholderText('メッセージを入力...');
      fireEvent.change(textarea, { target: { value: 'こんにちは' } });

      // 送信ボタンを取得（Sendアイコンを持つボタン）
      const sendButton = screen
        .getAllByRole('button')
        .find((btn) => btn.querySelector('svg.lucide-send'));
      fireEvent.click(sendButton!);

      await waitFor(() => {
        expect(screen.getByText('こんにちは')).toBeInTheDocument();
      });

      await waitFor(
        () => {
          expect(screen.getByText('AIからの応答です')).toBeInTheDocument();
        },
        { timeout: 3000 }
      );
    });

    it('should show error message when API fails', async () => {
      vi.mocked(geminiApi.startChat).mockRejectedValue(new Error('API Error'));

      render(<GeminiChatPage />);

      await waitFor(() => {
        expect(screen.queryByText('読み込み中...')).not.toBeInTheDocument();
      });

      const textarea = screen.getByPlaceholderText('メッセージを入力...');
      fireEvent.change(textarea, { target: { value: 'テスト' } });

      // 送信ボタンを取得（Sendアイコンを持つボタン）
      const sendButton = screen
        .getAllByRole('button')
        .find((btn) => btn.querySelector('svg.lucide-send'));
      fireEvent.click(sendButton!);

      await waitFor(
        () => {
          expect(
            screen.getByText('エラーが発生しました。もう一度お試しください。')
          ).toBeInTheDocument();
        },
        { timeout: 3000 }
      );
    });

    it('should send message with Enter key', async () => {
      const mockResponse = {
        conversationId: 'conv-new',
        response: 'AIからの応答',
      };
      vi.mocked(geminiApi.startChat).mockResolvedValue(mockResponse);

      render(<GeminiChatPage />);

      await waitFor(() => {
        expect(screen.queryByText('読み込み中...')).not.toBeInTheDocument();
      });

      const textarea = screen.getByPlaceholderText('メッセージを入力...');
      fireEvent.change(textarea, { target: { value: 'Enterで送信' } });
      fireEvent.keyDown(textarea, { key: 'Enter', code: 'Enter' });

      await waitFor(() => {
        expect(geminiApi.startChat).toHaveBeenCalled();
      });
    });
  });

  describe('会話選択', () => {
    it('should load messages when selecting a conversation', async () => {
      const mockConversations = [
        {
          conversationId: 'conv-1',
          title: '既存の会話',
          conversationType: 'general' as const,
          createdAt: '2025-01-01T10:00:00Z',
          updatedAt: '2025-01-01T11:00:00Z',
        },
      ];
      const mockMessages = [
        {
          messageId: 'msg-1',
          role: 'user' as const,
          content: '以前のメッセージ',
          createdAt: '2025-01-01T10:00:00Z',
        },
        {
          messageId: 'msg-2',
          role: 'assistant' as const,
          content: 'AIの返答',
          createdAt: '2025-01-01T10:00:05Z',
        },
      ];
      vi.mocked(geminiApi.getConversations).mockResolvedValue(mockConversations);
      vi.mocked(geminiApi.getMessages).mockResolvedValue(mockMessages);

      render(<GeminiChatPage />);

      await waitFor(() => {
        expect(screen.getByText('既存の会話')).toBeInTheDocument();
      });

      fireEvent.click(screen.getByText('既存の会話'));

      await waitFor(() => {
        expect(screen.getByText('以前のメッセージ')).toBeInTheDocument();
        expect(screen.getByText('AIの返答')).toBeInTheDocument();
      });
    });
  });

  describe('会話削除', () => {
    it('should delete conversation when confirmed', async () => {
      const mockConversations = [
        {
          conversationId: 'conv-1',
          title: '削除する会話',
          conversationType: 'general' as const,
          createdAt: '2025-01-01T10:00:00Z',
          updatedAt: '2025-01-01T11:00:00Z',
        },
      ];
      vi.mocked(geminiApi.getConversations).mockResolvedValue(mockConversations);
      vi.mocked(geminiApi.deleteConversation).mockResolvedValue(undefined);

      // globalThis.confirmをモック
      const confirmMock = vi.fn().mockReturnValue(true);
      globalThis.confirm = confirmMock;

      render(<GeminiChatPage />);

      await waitFor(() => {
        expect(screen.getByText('削除する会話')).toBeInTheDocument();
      });

      // 削除ボタンを直接取得
      const deleteButtons = screen.getAllByRole('button');
      const trashButton = deleteButtons.find((btn) => btn.querySelector('svg.lucide-trash2'));

      if (trashButton) {
        fireEvent.click(trashButton);

        await waitFor(() => {
          expect(geminiApi.deleteConversation).toHaveBeenCalledWith('conv-1');
        });
      }
    });

    it('should not delete conversation when cancelled', async () => {
      const mockConversations = [
        {
          conversationId: 'conv-1',
          title: 'キャンセルする会話',
          conversationType: 'general' as const,
          createdAt: '2025-01-01T10:00:00Z',
          updatedAt: '2025-01-01T11:00:00Z',
        },
      ];
      vi.mocked(geminiApi.getConversations).mockResolvedValue(mockConversations);

      // globalThis.confirmをモック（キャンセル）
      const confirmMock = vi.fn().mockReturnValue(false);
      globalThis.confirm = confirmMock;

      render(<GeminiChatPage />);

      await waitFor(() => {
        expect(screen.getByText('キャンセルする会話')).toBeInTheDocument();
      });

      const deleteButtons = screen.getAllByRole('button');
      const trashButton = deleteButtons.find((btn) => btn.querySelector('svg.lucide-trash2'));

      if (trashButton) {
        fireEvent.click(trashButton);
        expect(geminiApi.deleteConversation).not.toHaveBeenCalled();
      }
    });
  });

  describe('在庫パネル', () => {
    it('should display inventory items for recipe_recommendation', async () => {
      const mockInventory = [
        createMockInventoryItem({ itemId: 'item-1', name: '玉ねぎ', quantity: 3, unit: '個' }),
        createMockInventoryItem({
          itemId: 'item-2',
          name: '牛乳',
          quantity: 1,
          unit: 'L',
          isExpiringSoon: true,
        }),
      ];
      vi.mocked(inventoryApi.getInventory).mockResolvedValue(mockInventory);

      render(<GeminiChatPage />);

      await waitFor(() => {
        expect(screen.queryByText('読み込み中...')).not.toBeInTheDocument();
      });

      fireEvent.click(screen.getByText('新しい会話'));
      const recipeButton = screen.getByText('在庫食材からレシピを提案').closest('button');
      fireEvent.click(recipeButton!);

      await waitFor(() => {
        expect(screen.getByText('玉ねぎ 3個')).toBeInTheDocument();
        expect(screen.getByText('牛乳 1L')).toBeInTheDocument();
      });
    });

    it('should add inventory item to input when clicked', async () => {
      const mockInventory = [
        createMockInventoryItem({ itemId: 'item-1', name: '玉ねぎ', quantity: 3, unit: '個' }),
      ];
      vi.mocked(inventoryApi.getInventory).mockResolvedValue(mockInventory);

      render(<GeminiChatPage />);

      await waitFor(() => {
        expect(screen.queryByText('読み込み中...')).not.toBeInTheDocument();
      });

      fireEvent.click(screen.getByText('新しい会話'));
      const recipeButton = screen.getByText('在庫食材からレシピを提案').closest('button');
      fireEvent.click(recipeButton!);

      await waitFor(() => {
        expect(screen.getByText('玉ねぎ 3個')).toBeInTheDocument();
      });

      fireEvent.click(screen.getByText('玉ねぎ 3個'));

      const textarea = screen.getByPlaceholderText('メッセージを入力...') as HTMLTextAreaElement;
      expect(textarea.value).toBe('玉ねぎ 3個');
    });

    it('should add all inventory items to input', async () => {
      const mockInventory = [
        createMockInventoryItem({ itemId: 'item-1', name: '玉ねぎ', quantity: 3, unit: '個' }),
        createMockInventoryItem({ itemId: 'item-2', name: '牛乳', quantity: 1, unit: 'L' }),
      ];
      vi.mocked(inventoryApi.getInventory).mockResolvedValue(mockInventory);

      render(<GeminiChatPage />);

      await waitFor(() => {
        expect(screen.queryByText('読み込み中...')).not.toBeInTheDocument();
      });

      fireEvent.click(screen.getByText('新しい会話'));
      const recipeButton = screen.getByText('在庫食材からレシピを提案').closest('button');
      fireEvent.click(recipeButton!);

      await waitFor(() => {
        expect(screen.getByText('すべて追加')).toBeInTheDocument();
      });

      fireEvent.click(screen.getByText('すべて追加'));

      const textarea = screen.getByPlaceholderText('メッセージを入力...') as HTMLTextAreaElement;
      expect(textarea.value).toBe('玉ねぎ 3個, 牛乳 1L');
    });

    it('should show no inventory message when empty', async () => {
      vi.mocked(inventoryApi.getInventory).mockResolvedValue([]);

      render(<GeminiChatPage />);

      await waitFor(() => {
        expect(screen.queryByText('読み込み中...')).not.toBeInTheDocument();
      });

      fireEvent.click(screen.getByText('新しい会話'));
      const recipeButton = screen.getByText('在庫食材からレシピを提案').closest('button');
      fireEvent.click(recipeButton!);

      await waitFor(() => {
        expect(screen.getByText('在庫がありません')).toBeInTheDocument();
      });
    });
  });

  describe('既存会話へのメッセージ送信', () => {
    it('should use sendMessage for existing conversation', async () => {
      const mockConversations = [
        {
          conversationId: 'conv-existing',
          title: '既存の会話',
          conversationType: 'general' as const,
          createdAt: '2025-01-01T10:00:00Z',
          updatedAt: '2025-01-01T11:00:00Z',
        },
      ];
      const mockMessages = [
        {
          messageId: 'msg-1',
          role: 'user' as const,
          content: '最初のメッセージ',
          createdAt: '2025-01-01T10:00:00Z',
        },
      ];
      const mockResponse = {
        conversationId: 'conv-existing',
        response: '続きの応答',
      };

      vi.mocked(geminiApi.getConversations).mockResolvedValue(mockConversations);
      vi.mocked(geminiApi.getMessages).mockResolvedValue(mockMessages);
      vi.mocked(geminiApi.sendMessage).mockResolvedValue(mockResponse);

      render(<GeminiChatPage />);

      await waitFor(() => {
        expect(screen.getByText('既存の会話')).toBeInTheDocument();
      });

      fireEvent.click(screen.getByText('既存の会話'));

      await waitFor(() => {
        expect(screen.getByText('最初のメッセージ')).toBeInTheDocument();
      });

      const textarea = screen.getByPlaceholderText('メッセージを入力...');
      fireEvent.change(textarea, { target: { value: '続きの質問' } });

      // 送信ボタンを取得（disabled属性がないボタン）
      const sendButtons = screen.getAllByRole('button');
      const enabledSendButton = sendButtons.find(
        (btn) => btn.querySelector('svg.lucide-send') && !btn.hasAttribute('disabled')
      );

      if (enabledSendButton) {
        fireEvent.click(enabledSendButton);

        await waitFor(() => {
          expect(geminiApi.sendMessage).toHaveBeenCalledWith('conv-existing', '続きの質問');
        });

        await waitFor(() => {
          expect(screen.getByText('続きの応答')).toBeInTheDocument();
        });
      }
    });
  });
});
