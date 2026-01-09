import {
  Bot,
  ChefHat,
  Clock,
  Menu,
  MessageCircle,
  MessageSquarePlus,
  Package,
  Send,
  Trash2,
  User,
  X,
} from 'lucide-react';
import React, { useCallback, useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import {
  ConversationResponse,
  ConversationType,
  deleteConversation,
  getConversations,
  getMessages,
  MessageResponse,
  sendMessage,
  startChat,
} from '../../api/geminiApi';
import { getInventory, InventoryItem } from '../../api/inventoryApi';
import { Button } from '../ui/button';
import { Card } from '../ui/card';

interface Message {
  role: 'user' | 'assistant';
  content: string;
}

const GeminiChatPage: React.FC = () => {
  const { t } = useTranslation();
  const [conversations, setConversations] = useState<ConversationResponse[]>([]);
  const [currentConversationId, setCurrentConversationId] = useState<string | null>(null);
  const [currentConversationType, setCurrentConversationType] =
    useState<ConversationType>('general');
  const [messages, setMessages] = useState<Message[]>([]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const [loadingConversations, setLoadingConversations] = useState(true);

  // 主題選択モーダル
  const [showTopicModal, setShowTopicModal] = useState(false);

  // モバイル用サイドバー表示状態
  const [isSidebarOpen, setIsSidebarOpen] = useState(false);

  // 在庫データ
  const [inventory, setInventory] = useState<InventoryItem[]>([]);
  const [loadingInventory, setLoadingInventory] = useState(false);

  // 会話一覧を取得
  const loadConversations = useCallback(async () => {
    try {
      const data = await getConversations();
      const sorted = data.sort(
        (a, b) => new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime()
      );
      setConversations(sorted);
    } catch {
      setConversations([]);
    } finally {
      setLoadingConversations(false);
    }
  }, []);

  // 会話のメッセージを取得
  const loadMessages = useCallback(async (conversationId: string) => {
    try {
      const data = await getMessages(conversationId);
      const msgs: Message[] = data.map((m: MessageResponse) => ({
        role: m.role,
        content: m.content,
      }));
      setMessages(msgs);
    } catch {
      setMessages([]);
    }
  }, []);

  // 在庫を取得
  const loadInventory = useCallback(async () => {
    setLoadingInventory(true);
    try {
      const data = await getInventory(true);
      setInventory(data);
    } catch {
      setInventory([]);
    } finally {
      setLoadingInventory(false);
    }
  }, []);

  useEffect(() => {
    loadConversations();
  }, [loadConversations]);

  // 会話を選択
  const handleSelectConversation = async (conv: ConversationResponse) => {
    setCurrentConversationId(conv.conversationId);
    setCurrentConversationType(conv.conversationType);
    setMessages([]);
    await loadMessages(conv.conversationId);

    // 在庫関連の会話タイプなら在庫を読み込む
    if (
      conv.conversationType === 'recipe_recommendation' ||
      conv.conversationType === 'expiry_check'
    ) {
      loadInventory();
    }

    // モバイルではサイドバーを閉じる
    setIsSidebarOpen(false);
  };

  // 新しい会話ボタンクリック
  const handleNewConversationClick = () => {
    setShowTopicModal(true);
  };

  // 主題を選択して新しい会話を開始
  const handleSelectTopic = async (type: ConversationType) => {
    setShowTopicModal(false);
    setCurrentConversationId(null);
    setCurrentConversationType(type);
    setMessages([]);

    // 在庫関連の会話タイプなら在庫を読み込む
    if (type === 'recipe_recommendation' || type === 'expiry_check') {
      loadInventory();
    } else {
      setInventory([]);
    }
  };

  // 会話を削除
  const handleDeleteConversation = async (conversationId: string, e: React.MouseEvent) => {
    e.stopPropagation();
    if (!confirm(t('chat.confirmDelete'))) return;

    try {
      await deleteConversation(conversationId);
      if (currentConversationId === conversationId) {
        setCurrentConversationId(null);
        setMessages([]);
      }
      await loadConversations();
    } catch {
      alert(t('chat.deleteError'));
    }
  };

  // 在庫アイテムをクリックして入力欄に追加
  const handleInventoryClick = (item: InventoryItem) => {
    const itemText = `${item.name} ${item.quantity}${item.unit}`;
    setInput((prev) => (prev ? `${prev}, ${itemText}` : itemText));
  };

  // 全在庫を入力欄に追加
  const handleAddAllInventory = () => {
    if (inventory.length === 0) return;

    const itemsText = inventory
      .map((item) => `${item.name} ${item.quantity}${item.unit}`)
      .join(', ');
    setInput(itemsText);
  };

  // メッセージ送信
  const handleSend = async () => {
    if (!input.trim() || loading) return;

    const userMessage = input.trim();
    setInput('');
    setMessages((prev) => [...prev, { role: 'user', content: userMessage }]);
    setLoading(true);

    try {
      let result;
      if (currentConversationId) {
        result = await sendMessage(currentConversationId, userMessage);
      } else {
        result = await startChat(userMessage, currentConversationType);
        setCurrentConversationId(result.conversationId);
        await loadConversations();
      }
      setMessages((prev) => [...prev, { role: 'assistant', content: result.response }]);
    } catch {
      setMessages((prev) => [...prev, { role: 'assistant', content: t('chat.error') }]);
    } finally {
      setLoading(false);
    }
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  };

  const formatDate = (dateStr: string) => {
    const date = new Date(dateStr);
    return date.toLocaleDateString('ja-JP', {
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const getTopicIcon = (type: ConversationType) => {
    switch (type) {
      case 'recipe_recommendation':
        return <ChefHat size={16} className="text-orange-500" />;
      case 'expiry_check':
        return <Clock size={16} className="text-yellow-500" />;
      default:
        return <MessageCircle size={16} className="text-purple-500" />;
    }
  };

  const getTopicLabel = (type: ConversationType) => {
    switch (type) {
      case 'recipe_recommendation':
        return t('chat.topics.recipeRecommendation');
      case 'expiry_check':
        return t('chat.topics.expiryCheck');
      default:
        return t('chat.topics.general');
    }
  };

  const showInventoryPanel =
    currentConversationType === 'recipe_recommendation' ||
    currentConversationType === 'expiry_check';

  // サイドバーコンテンツ（PC・モバイル共通）
  const sidebarContent = (
    <>
      <div className="p-3 border-b">
        <Button onClick={handleNewConversationClick} className="w-full flex items-center gap-2">
          <MessageSquarePlus size={18} />
          {t('chat.newConversation')}
        </Button>
      </div>
      <div className="flex-1 overflow-y-auto">
        {loadingConversations ? (
          <p className="text-gray-400 text-center mt-4">{t('common.loading')}</p>
        ) : conversations.length === 0 ? (
          <p className="text-gray-400 text-center mt-4 text-sm">{t('chat.noHistory')}</p>
        ) : (
          conversations.map((conv) => (
            <div
              key={conv.conversationId}
              onClick={() => handleSelectConversation(conv)}
              className={`p-3 border-b cursor-pointer hover:bg-gray-50 flex items-center justify-between group ${
                currentConversationId === conv.conversationId ? 'bg-purple-50' : ''
              }`}
            >
              <div className="flex-1 min-w-0">
                <div className="flex items-center gap-1 mb-1">
                  {getTopicIcon(conv.conversationType)}
                  <p className="text-sm font-medium truncate">{conv.title}</p>
                </div>
                <p className="text-xs text-gray-400">{formatDate(conv.updatedAt)}</p>
              </div>
              <button
                onClick={(e) => handleDeleteConversation(conv.conversationId, e)}
                className="opacity-0 group-hover:opacity-100 p-1 hover:bg-red-100 rounded"
              >
                <Trash2 size={16} className="text-red-500" />
              </button>
            </div>
          ))
        )}
      </div>
    </>
  );

  return (
    <div className="container mx-auto px-4 py-8">
      {/* ヘッダー */}
      <div className="flex items-center space-x-3 mb-6">
        {/* モバイル用ハンバーガーメニュー */}
        <button
          onClick={() => setIsSidebarOpen(true)}
          className="md:hidden p-2 hover:bg-gray-100 rounded-lg"
          aria-label="メニューを開く"
        >
          <Menu size={24} className="text-gray-600" />
        </button>
        <Bot size={32} className="text-purple-600" />
        <h1 className="text-2xl font-bold">{t('chat.title')}</h1>
      </div>

      <div className="flex gap-4 h-[600px]">
        {/* モバイル用ドロワーオーバーレイ */}
        {isSidebarOpen && (
          <div
            className="fixed inset-0 bg-black bg-opacity-50 z-40 md:hidden"
            onClick={() => setIsSidebarOpen(false)}
          />
        )}

        {/* 会話一覧サイドバー（PC: 常時表示、モバイル: ドロワー） */}
        <Card
          className={`
            w-72 flex flex-col
            fixed md:relative
            inset-y-0 left-0
            z-50 md:z-auto
            transform transition-transform duration-300 ease-in-out
            ${isSidebarOpen ? 'translate-x-0' : '-translate-x-full md:translate-x-0'}
            md:transform-none
            h-full md:h-auto
            rounded-none md:rounded-lg
          `}
        >
          {/* モバイル用閉じるボタン */}
          <div className="md:hidden flex items-center justify-between p-3 border-b">
            <span className="font-medium">{t('chat.conversationHistory')}</span>
            <button
              onClick={() => setIsSidebarOpen(false)}
              className="p-1 hover:bg-gray-100 rounded"
              aria-label="メニューを閉じる"
            >
              <X size={20} className="text-gray-600" />
            </button>
          </div>
          {sidebarContent}
        </Card>

        {/* チャットエリア */}
        <Card className="flex-1 flex flex-col">
          {/* 現在の主題表示 */}
          {!currentConversationId && (
            <div className="px-4 py-2 border-b bg-gray-50 flex items-center gap-2">
              {getTopicIcon(currentConversationType)}
              <span className="text-sm font-medium">{getTopicLabel(currentConversationType)}</span>
            </div>
          )}

          {/* 在庫パネル */}
          {showInventoryPanel && (
            <div className="px-4 py-3 border-b bg-blue-50">
              <div className="flex items-center justify-between mb-2">
                <div className="flex items-center gap-2">
                  <Package size={16} className="text-blue-600" />
                  <span className="text-sm font-medium text-blue-800">
                    {t('chat.currentInventory')}
                  </span>
                </div>
                {inventory.length > 0 && (
                  <button
                    onClick={handleAddAllInventory}
                    className="text-xs text-blue-600 hover:underline"
                  >
                    {t('chat.addAllToInput')}
                  </button>
                )}
              </div>
              {loadingInventory ? (
                <p className="text-sm text-gray-500">{t('common.loading')}</p>
              ) : inventory.length === 0 ? (
                <p className="text-sm text-gray-500">{t('chat.noInventory')}</p>
              ) : (
                <div className="flex flex-wrap gap-2">
                  {inventory.map((item) => (
                    <button
                      key={item.itemId}
                      onClick={() => handleInventoryClick(item)}
                      className={`px-2 py-1 text-xs rounded-full border transition-colors ${
                        item.isExpired
                          ? 'bg-red-100 border-red-300 text-red-700 hover:bg-red-200'
                          : item.isExpiringSoon
                          ? 'bg-yellow-100 border-yellow-300 text-yellow-700 hover:bg-yellow-200'
                          : 'bg-white border-gray-300 text-gray-700 hover:bg-gray-100'
                      }`}
                    >
                      {item.name} {item.quantity}
                      {item.unit}
                    </button>
                  ))}
                </div>
              )}
            </div>
          )}

          <div className="flex-1 overflow-y-auto p-4 space-y-4">
            {messages.length === 0 && (
              <p className="text-gray-400 text-center mt-20">{t('chat.placeholder')}</p>
            )}
            {messages.map((msg, idx) => (
              <div
                key={idx}
                className={`flex items-start gap-3 ${
                  msg.role === 'user' ? 'flex-row-reverse' : ''
                }`}
              >
                <div
                  className={`p-2 rounded-full ${
                    msg.role === 'user' ? 'bg-blue-100' : 'bg-purple-100'
                  }`}
                >
                  {msg.role === 'user' ? <User size={20} /> : <Bot size={20} />}
                </div>
                <div
                  className={`max-w-[80%] p-3 rounded-lg ${
                    msg.role === 'user' ? 'bg-blue-500 text-white' : 'bg-gray-100'
                  }`}
                >
                  <p className="whitespace-pre-wrap">{msg.content}</p>
                </div>
              </div>
            ))}
            {loading && (
              <div className="flex items-center gap-3">
                <div className="p-2 rounded-full bg-purple-100">
                  <Bot size={20} />
                </div>
                <div className="bg-gray-100 p-3 rounded-lg">
                  <span className="animate-pulse">{t('chat.thinking')}</span>
                </div>
              </div>
            )}
          </div>

          <div className="border-t p-4">
            <div className="flex gap-2">
              <textarea
                value={input}
                onChange={(e) => setInput(e.target.value)}
                onKeyDown={handleKeyDown}
                placeholder={t('chat.inputPlaceholder')}
                className="flex-1 resize-none border rounded-lg p-3 focus:outline-none focus:ring-2 focus:ring-purple-500"
                rows={2}
                disabled={loading}
              />
              <Button onClick={handleSend} disabled={loading || !input.trim()} className="self-end">
                <Send size={20} />
              </Button>
            </div>
          </div>
        </Card>
      </div>

      {/* 主題選択モーダル */}
      {showTopicModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <Card className="w-96 p-6">
            <h2 className="text-lg font-bold mb-4">{t('chat.selectTopic')}</h2>
            <div className="space-y-3">
              <button
                onClick={() => handleSelectTopic('general')}
                className="w-full p-4 border rounded-lg hover:bg-gray-50 flex items-center gap-3 text-left"
              >
                <MessageCircle size={24} className="text-purple-500" />
                <div>
                  <p className="font-medium">{t('chat.topics.general')}</p>
                  <p className="text-sm text-gray-500">{t('chat.topics.generalDesc')}</p>
                </div>
              </button>
              <button
                onClick={() => handleSelectTopic('recipe_recommendation')}
                className="w-full p-4 border rounded-lg hover:bg-gray-50 flex items-center gap-3 text-left"
              >
                <ChefHat size={24} className="text-orange-500" />
                <div>
                  <p className="font-medium">{t('chat.topics.recipeRecommendation')}</p>
                  <p className="text-sm text-gray-500">
                    {t('chat.topics.recipeRecommendationDesc')}
                  </p>
                </div>
              </button>
              <button
                onClick={() => handleSelectTopic('expiry_check')}
                className="w-full p-4 border rounded-lg hover:bg-gray-50 flex items-center gap-3 text-left"
              >
                <Clock size={24} className="text-yellow-500" />
                <div>
                  <p className="font-medium">{t('chat.topics.expiryCheck')}</p>
                  <p className="text-sm text-gray-500">{t('chat.topics.expiryCheckDesc')}</p>
                </div>
              </button>
            </div>
            <Button
              variant="outline"
              className="w-full mt-4"
              onClick={() => setShowTopicModal(false)}
            >
              {t('common.cancel')}
            </Button>
          </Card>
        </div>
      )}
    </div>
  );
};

export default GeminiChatPage;
