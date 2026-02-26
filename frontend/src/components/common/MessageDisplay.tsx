import { forwardRef } from 'react';

interface MessageDisplayProps {
  error?: string | null;
  success?: string | null;
}

/**
 * エラー/成功メッセージ表示コンポーネント
 * スクロール用のrefを受け取る
 */
export const MessageDisplay = forwardRef<HTMLDivElement, MessageDisplayProps>(
  ({ error, success }, ref) => {
    if (!error && !success) {
      return null;
    }

    return (
      <div ref={ref}>
        {error && (
          <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
            {error}
          </div>
        )}
        {success && (
          <div className="bg-green-100 border border-green-400 text-green-700 px-4 py-3 rounded mb-4">
            {success}
          </div>
        )}
      </div>
    );
  }
);

MessageDisplay.displayName = 'MessageDisplay';
