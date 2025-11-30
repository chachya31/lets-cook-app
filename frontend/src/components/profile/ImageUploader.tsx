import React, { useState, useRef, DragEvent, ChangeEvent } from 'react';
import { useTranslation } from 'react-i18next';

interface ImageUploaderProps {
  currentImageUrl?: string;
  onImageSelect: (file: File) => void;
  onImageRemove?: () => void;
  maxSizeMB?: number;
  allowedFormats?: string[];
}

/**
 * 画像アップローダーコンポーネント
 * ドラッグ&ドロップ、プレビュー表示、バリデーション機能を提供
 */
export const ImageUploader: React.FC<ImageUploaderProps> = ({
  currentImageUrl,
  onImageSelect,
  onImageRemove,
  maxSizeMB = 5,
  allowedFormats = ['image/jpeg', 'image/jpg', 'image/png'],
}) => {
  const { t } = useTranslation();
  const [previewUrl, setPreviewUrl] = useState<string | null>(currentImageUrl || null);
  const [isDragging, setIsDragging] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const maxSizeBytes = maxSizeMB * 1024 * 1024;

  /**
   * ファイルバリデーション
   */
  const validateFile = (file: File): string | null => {
    // ファイルサイズチェック
    if (file.size > maxSizeBytes) {
      return t('imageUploader.errors.fileTooLarge', { maxSize: maxSizeMB });
    }

    // フォーマットチェック
    if (!allowedFormats.includes(file.type)) {
      return t('imageUploader.errors.invalidFormat');
    }

    return null;
  };

  /**
   * ファイル処理
   */
  const handleFile = (file: File) => {
    setError(null);

    const validationError = validateFile(file);
    if (validationError) {
      setError(validationError);
      return;
    }

    // プレビュー表示
    const reader = new FileReader();
    reader.onloadend = () => {
      setPreviewUrl(reader.result as string);
    };
    reader.readAsDataURL(file);

    // 親コンポーネントに通知
    onImageSelect(file);
  };

  /**
   * ファイル選択ハンドラー
   */
  const handleFileChange = (e: ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      handleFile(file);
    }
  };

  /**
   * ドラッグオーバーハンドラー
   */
  const handleDragOver = (e: DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    setIsDragging(true);
  };

  /**
   * ドラッグリーブハンドラー
   */
  const handleDragLeave = (e: DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    setIsDragging(false);
  };

  /**
   * ドロップハンドラー
   */
  const handleDrop = (e: DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    setIsDragging(false);

    const file = e.dataTransfer.files?.[0];
    if (file) {
      handleFile(file);
    }
  };

  /**
   * クリックハンドラー
   */
  const handleClick = () => {
    fileInputRef.current?.click();
  };

  /**
   * 画像削除ハンドラー
   */
  const handleRemove = () => {
    setPreviewUrl(null);
    setError(null);
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
    onImageRemove?.();
  };

  return (
    <div className="w-full">
      {/* プレビュー表示 */}
      {previewUrl ? (
        <div className="relative">
          <img
            src={previewUrl}
            alt="Preview"
            className="w-full h-64 object-cover rounded-lg border-2 border-gray-300"
          />
          <button
            type="button"
            onClick={handleRemove}
            className="absolute top-2 right-2 bg-red-500 text-white rounded-full p-2 hover:bg-red-600 transition-colors"
            aria-label={t('imageUploader.removeImage')}
          >
            <svg
              xmlns="http://www.w3.org/2000/svg"
              className="h-5 w-5"
              viewBox="0 0 20 20"
              fill="currentColor"
            >
              <path
                fillRule="evenodd"
                d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z"
                clipRule="evenodd"
              />
            </svg>
          </button>
        </div>
      ) : (
        /* ドラッグ&ドロップエリア */
        <div
          onDragOver={handleDragOver}
          onDragLeave={handleDragLeave}
          onDrop={handleDrop}
          onClick={handleClick}
          className={`
            w-full h-64 border-2 border-dashed rounded-lg
            flex flex-col items-center justify-center
            cursor-pointer transition-colors
            ${isDragging ? 'border-green-500 bg-green-50' : 'border-gray-300 hover:border-gray-400'}
          `}
        >
          <svg
            xmlns="http://www.w3.org/2000/svg"
            className="h-12 w-12 text-gray-400 mb-4"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12"
            />
          </svg>
          <p className="text-gray-600 text-center mb-2">
            {t('imageUploader.dragAndDrop')}
          </p>
          <p className="text-gray-400 text-sm">
            {t('imageUploader.formats', { formats: 'JPEG, PNG' })}
          </p>
          <p className="text-gray-400 text-sm">
            {t('imageUploader.maxSize', { maxSize: maxSizeMB })}
          </p>
        </div>
      )}

      {/* 隠しファイル入力 */}
      <input
        ref={fileInputRef}
        type="file"
        accept={allowedFormats.join(',')}
        onChange={handleFileChange}
        className="hidden"
      />

      {/* エラーメッセージ */}
      {error && (
        <div className="mt-2 p-3 bg-red-50 border border-red-200 rounded-md">
          <p className="text-red-600 text-sm">{error}</p>
        </div>
      )}
    </div>
  );
};
