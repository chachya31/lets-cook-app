import React from 'react';

/**
 * ローディングスケルトンコンポーネント
 * コンテンツ読み込み中のプレースホルダー表示
 */

interface LoadingSkeletonProps {
  /**
   * スケルトンのタイプ
   * - card: カード型（レシピカードなど）
   * - list: リスト型（スケジュール、買い物リストなど）
   * - text: テキスト型（単一行）
   */
  type?: 'card' | 'list' | 'text';
  /**
   * 表示する行数（listタイプの場合）
   */
  lines?: number;
  /**
   * カスタムクラス名
   */
  className?: string;
}

const LoadingSkeleton: React.FC<LoadingSkeletonProps> = ({
  type = 'card',
  lines = 3,
  className = '',
}) => {
  // アニメーションクラス
  const animateClass = 'animate-pulse bg-gray-200 rounded';

  if (type === 'text') {
    return <div className={`h-4 ${animateClass} ${className}`}></div>;
  }

  if (type === 'list') {
    return (
      <div className={`space-y-3 ${className}`}>
        {Array.from({ length: lines }).map((_, index) => (
          <div key={index} className="flex items-center space-x-3">
            <div className={`h-4 w-4 rounded-full ${animateClass}`}></div>
            <div className={`h-4 flex-1 ${animateClass}`}></div>
          </div>
        ))}
      </div>
    );
  }

  // card タイプ
  return (
    <div className={`border border-gray-200 rounded-lg p-4 ${className}`}>
      <div className={`h-48 mb-4 ${animateClass}`}></div>
      <div className={`h-6 mb-2 ${animateClass}`}></div>
      <div className={`h-4 w-3/4 ${animateClass}`}></div>
    </div>
  );
};

export default LoadingSkeleton;
