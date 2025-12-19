import { useCallback, useRef } from 'react';

/**
 * メッセージ表示時にスクロールするカスタムフック
 */
export const useScrollToMessage = () => {
  const messageRef = useRef<HTMLDivElement>(null);

  const scrollToMessage = useCallback(() => {
    setTimeout(() => {
      messageRef.current?.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }, 100);
  }, []);

  return { messageRef, scrollToMessage };
};
