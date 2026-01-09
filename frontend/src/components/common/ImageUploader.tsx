import React, { ChangeEvent, DragEvent, useEffect, useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { compressImage, ImageCompressionOptions } from '../../utils/imageCompression';

export type ImageShape = 'circle' | 'rectangle';

interface ImageUploaderProps {
  currentImageUrl?: string;
  onImageSelect: (file: File) => void;
  onImageRemove?: () => void;
  maxSizeMB?: number;
  allowedFormats?: string[];
  shape?: ImageShape;
  height?: string;
  enableCompression?: boolean;
  compressionOptions?: ImageCompressionOptions;
  label?: string;
}

/**
 * 汎用画像アップローダーコンポーネント
 * ドラッグ&ドロップ、プレビュー表示、バリデーション、圧縮機能を提供
 */
export const ImageUploader: React.FC<ImageUploaderProps> = ({
  currentImageUrl,
  onImageSelect,
  onImageRemove,
  maxSizeMB = 5,
  allowedFormats = ['image/jpeg', 'image/jpg', 'image/png'],
  shape = 'circle',
  height = 'h-32',
  enableCompression = true,
  compressionOptions = { maxWidthOrHeight: 1920, maxSizeMB: 1 },
  label,
}) => {
  const { t } = useTranslation();
  const [previewUrl, setPreviewUrl] = useState<string | null>(currentImageUrl || null);
  const [isDragging, setIsDragging] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [isCompressing, setIsCompressing] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    setPreviewUrl(currentImageUrl || null);
  }, [currentImageUrl]);

  const maxSizeBytes = maxSizeMB * 1024 * 1024;

  const validateFile = (file: File): string | null => {
    if (file.size > maxSizeBytes) {
      return t('imageUploader.errors.fileTooLarge', { maxSize: maxSizeMB });
    }
    if (!allowedFormats.includes(file.type)) {
      return t('imageUploader.errors.invalidFormat');
    }
    return null;
  };

  const handleFile = async (file: File) => {
    setError(null);

    const validationError = validateFile(file);
    if (validationError) {
      setError(validationError);
      return;
    }

    let processedFile = file;

    // 圧縮処理
    if (enableCompression) {
      setIsCompressing(true);
      try {
        processedFile = await compressImage(file, compressionOptions);
      } finally {
        setIsCompressing(false);
      }
    }

    // プレビュー表示
    const reader = new FileReader();
    reader.onloadend = () => {
      setPreviewUrl(reader.result as string);
    };
    reader.readAsDataURL(processedFile);

    onImageSelect(processedFile);
  };

  const handleFileChange = (e: ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      handleFile(file);
    }
  };

  const handleDragOver = (e: DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    setIsDragging(true);
  };

  const handleDragLeave = (e: DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    setIsDragging(false);
  };

  const handleDrop = (e: DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    setIsDragging(false);
    const file = e.dataTransfer.files?.[0];
    if (file) {
      handleFile(file);
    }
  };

  const handleClick = () => {
    fileInputRef.current?.click();
  };

  const handleRemove = () => {
    setPreviewUrl(null);
    setError(null);
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
    onImageRemove?.();
  };

  const imageClasses =
    shape === 'circle'
      ? 'w-24 h-24 object-cover rounded-full border-2 border-gray-300'
      : 'w-full h-full object-contain rounded-lg border-2 border-gray-300';

  return (
    <div className="w-full">
      {label && <p className="text-sm font-medium text-gray-700 mb-2">{label}</p>}

      {isCompressing ? (
        <div
          className={`w-full ${height} border-2 border-dashed rounded-lg flex items-center justify-center bg-gray-50`}
        >
          <div className="text-center">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-green-500 mx-auto mb-2"></div>
            <p className="text-sm text-gray-600">{t('imageUploader.compressing')}</p>
          </div>
        </div>
      ) : previewUrl ? (
        <div className={shape === 'circle' ? 'flex items-center space-x-4' : 'space-y-2'}>
          <div className="relative inline-block">
            <img src={previewUrl} alt="Preview" className={imageClasses} />
            {onImageRemove && (
              <button
                type="button"
                onClick={handleRemove}
                className="absolute -top-1 -right-1 bg-red-500 text-white rounded-full p-1 hover:bg-red-600 transition-colors"
                aria-label={t('imageUploader.removeImage')}
              >
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  className="h-4 w-4"
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
            )}
          </div>
          <button
            type="button"
            onClick={handleClick}
            className="text-sm text-green-600 hover:text-green-700"
          >
            {t('imageUploader.changeImage')}
          </button>
        </div>
      ) : (
        <div
          onDragOver={handleDragOver}
          onDragLeave={handleDragLeave}
          onDrop={handleDrop}
          onClick={handleClick}
          className={`w-full ${height} border-2 border-dashed rounded-lg flex flex-col items-center justify-center cursor-pointer transition-colors ${
            isDragging ? 'border-green-500 bg-green-50' : 'border-gray-300 hover:border-gray-400'
          }`}
        >
          <svg
            xmlns="http://www.w3.org/2000/svg"
            className="h-8 w-8 text-gray-400 mb-2"
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
          <p className="text-gray-600 text-center text-sm mb-1">{t('imageUploader.dragAndDrop')}</p>
          <p className="text-gray-400 text-xs">
            {t('imageUploader.formats', { formats: 'JPEG, PNG' })} /{' '}
            {t('imageUploader.maxSize', { maxSize: maxSizeMB })}
          </p>
        </div>
      )}

      <input
        ref={fileInputRef}
        type="file"
        accept={allowedFormats.join(',')}
        onChange={handleFileChange}
        className="hidden"
      />

      {error && (
        <div className="mt-2 p-3 bg-red-50 border border-red-200 rounded-md">
          <p className="text-red-600 text-sm">{error}</p>
        </div>
      )}
    </div>
  );
};
