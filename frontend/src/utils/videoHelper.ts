/**
 * 動画URL関連のヘルパー関数
 */

/**
 * YouTubeのURLから埋め込み用URLを生成
 * @param url YouTube URL (watch, youtu.be, embed形式に対応)
 * @returns 埋め込み用URL、または無効な場合はnull
 */
export const getYouTubeEmbedUrl = (url: string): string | null => {
  if (!url) return null;

  // youtube.com/watch?v=VIDEO_ID 形式
  const watchMatch = url.match(/youtube\.com\/watch\?v=([a-zA-Z0-9_-]+)/);
  if (watchMatch) {
    return `https://www.youtube.com/embed/${watchMatch[1]}`;
  }

  // youtu.be/VIDEO_ID 形式
  const shortMatch = url.match(/youtu\.be\/([a-zA-Z0-9_-]+)/);
  if (shortMatch) {
    return `https://www.youtube.com/embed/${shortMatch[1]}`;
  }

  // youtube.com/embed/VIDEO_ID 形式（既に埋め込み形式）
  const embedMatch = url.match(/youtube\.com\/embed\/([a-zA-Z0-9_-]+)/);
  if (embedMatch) {
    return url;
  }

  return null;
};

/**
 * URLがYouTubeのURLかどうかを判定
 */
export const isYouTubeUrl = (url: string): boolean => {
  return getYouTubeEmbedUrl(url) !== null;
};
