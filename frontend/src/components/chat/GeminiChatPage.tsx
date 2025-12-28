import { Bot, MessageSquarePlus, Send, Trash2, User } from 'lucide-react';
import React, { useCallback, useEffect, useState } from 'react';
import {
  ConversationResponse,
  deleteConversation,
  getConversations,
  getMessages,
  MessageResponse,
  sendMessage,
  startChat,
} from '../../api/geminiApi';
import { Button } from '../ui/button';
import { Card } from '../ui/card';

interface Message {
  role: 'user' | 'assistant';
  content: string;
}

const GeminiChatPage: React.FC = () => {
  const [conversations, setConversations] = useState<ConversationResponse[]>([]);
  const [currentConversationId, setCurrentConversationId] = useState<string | null>(null);
  const [messages, setMessages] = useState<Message[]>([]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const [loadingConversations, setLoadingConversations] = useState(true);

  // 会話一覧を取得
  const loadConversations = useCallback(async () => {
    try {
      const data = await getConversations();
      // 更新日時で降順ソート
      const sorted = data.sort(
        (a, b) => new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime()
      );
      setConversations(sorted);
    } catch {
      // エラー時は空配列
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

  useEffect(() => {
    loadConversations();
  }, [loadConversations]);

  // 会話を選択
  const handleSelectConversation = async (conversationId: string) => {
    setCurrentConversationId(conversationId);
    setMessages([]);
    await loadMessages(conversationId);
  };

  // 新しい会話を開始
  const handleNewConversation = () => {
    setCurrentConversationId(null);
    setMessages([]);
  };

  // 会話を削除
  const handleDeleteConversation = async (conversationId: string, e: React.MouseEvent) => {
    e.stopPropagation();
    if (!confirm('この会話を削除しますか？')) return;

    try {
      await deleteConversation(conversationId);
      if (currentConversationId === conversationId) {
        setCurrentConversationId(null);
        setMessages([]);
      }
      await loadConversations();
    } catch {
      alert('削除に失敗しました');
    }
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
        // 既存の会話に送信
        result = await sendMessage(currentConversationId, userMessage);
      } else {
        // 新しい会話を開始
        result = await startChat(userMessage);
        setCurrentConversationId(result.conversationId);
        await loadConversations();
      }
      setMessages((prev) => [...prev, { role: 'assistant', content: result.response }]);
    } catch {
      setMessages((prev) => [
        ...prev,
        { role: 'assistant', content: 'エラーが発生しました。もう一度お試しください。' },
      ]);
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

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="flex items-center space-x-3 mb-6">
        <Bot size={32} className="text-purple-600" />
        <h1 className="text-2xl font-bold">AI Chat</h1>
      </div>

      <div className="flex gap-4 h-[600px]">
        {/* 会話一覧サイドバー */}
        <Card className="w-72 flex flex-col">
          <div className="p-3 border-b">
            <Button onClick={handleNewConversation} className="w-full flex items-center gap-2">
              <MessageSquarePlus size={18} />
              新しい会話
            </Button>
          </div>
          <div className="flex-1 overflow-y-auto">
            {loadingConversations ? (
              <p className="text-gray-400 text-center mt-4">読み込み中...</p>
            ) : conversations.length === 0 ? (
              <p className="text-gray-400 text-center mt-4 text-sm">会話履歴がありません</p>
            ) : (
              conversations.map((conv) => (
                <div
                  key={conv.conversationId}
                  onClick={() => handleSelectConversation(conv.conversationId)}
                  className={`p-3 border-b cursor-pointer hover:bg-gray-50 flex items-center justify-between group ${
                    currentConversationId === conv.conversationId ? 'bg-purple-50' : ''
                  }`}
                >
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-medium truncate">{conv.title}</p>
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
        </Card>

        {/* チャットエリア */}
        <Card className="flex-1 flex flex-col">
          <div className="flex-1 overflow-y-auto p-4 space-y-4">
            {messages.length === 0 && (
              <p className="text-gray-400 text-center mt-20">メッセージを入力してください</p>
            )}
            {messages.map((msg, idx) => (
              <div
                key={idx}
                className={`flex items-start gap-3 ${msg.role === 'user' ? 'flex-row-reverse' : ''}`}
              >
                <div
                  className={`p-2 rounded-full ${msg.role === 'user' ? 'bg-blue-100' : 'bg-purple-100'}`}
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
                  <span className="animate-pulse">考え中...</span>
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
                placeholder="メッセージを入力..."
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
    </div>
  );
};

export default GeminiChatPage;
