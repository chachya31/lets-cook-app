import { Bot, Send, User } from 'lucide-react';
import React, { useState } from 'react';
import { chatWithGemini } from '../../api/geminiApi';
import { Button } from '../ui/button';
import { Card } from '../ui/card';

interface Message {
  role: 'user' | 'assistant';
  content: string;
}

const GeminiChatPage: React.FC = () => {
  const [messages, setMessages] = useState<Message[]>([]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSend = async () => {
    if (!input.trim() || loading) return;

    const userMessage = input.trim();
    setInput('');
    setMessages((prev) => [...prev, { role: 'user', content: userMessage }]);
    setLoading(true);

    try {
      const response = await chatWithGemini(userMessage);
      setMessages((prev) => [...prev, { role: 'assistant', content: response.response }]);
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

  return (
    <div className="container mx-auto px-4 py-8 max-w-3xl">
      <div className="flex items-center space-x-3 mb-6">
        <Bot size={32} className="text-purple-600" />
        <h1 className="text-2xl font-bold">Gemini Chat</h1>
      </div>

      <Card className="h-[500px] flex flex-col">
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
  );
};

export default GeminiChatPage;
