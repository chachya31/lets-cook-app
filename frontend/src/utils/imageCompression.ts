import imageCompression from 'browser-image-compression';

/**
 * 画像圧縮オプション
 */
export interface ImageCompressionOptions {
  maxWidthOrHeight?: number;
  maxSizeMB?: number;
  useWebWorker?: boolean;
}

/**
 * デフォルトの圧縮オプション
 */
const defaultOptions: ImageCompressionOptions = {
  maxWidthOrHeight: 1920,
  maxSizeMB: 1,
  useWebWorker: true,
};

/**
 * 画像を圧縮・リサイズする
 * @param file 元の画像ファイル
 * @param options 圧縮オプション
 * @returns 圧縮された画像ファイル
 */
export const compressImage = async (
  file: File,
  options: ImageCompressionOptions = {}
): Promise<File> => {
  const mergedOptions = { ...defaultOptions, ...options };

  // 画像ファイルでない場合はそのまま返す
  if (!file.type.startsWith('image/')) {
    return file;
  }

  // GIFは圧縮しない（アニメーションが壊れる可能性）
  if (file.type === 'image/gif') {
    return file;
  }

  try {
    const compressedFile = await imageCompression(file, {
      maxWidthOrHeight: mergedOptions.maxWidthOrHeight,
      maxSizeMB: mergedOptions.maxSizeMB,
      useWebWorker: mergedOptions.useWebWorker,
    });

    // 圧縮後のファイルを元のファイル名で返す
    return new File([compressedFile], file.name, {
      type: compressedFile.type,
      lastModified: Date.now(),
    });
  } catch {
    // 圧縮に失敗した場合は元のファイルを返す
    return file;
  }
};

/**
 * 複数の画像を圧縮する
 * @param files 画像ファイルの配列
 * @param options 圧縮オプション
 * @returns 圧縮された画像ファイルの配列
 */
export const compressImages = async (
  files: (File | null)[],
  options: ImageCompressionOptions = {}
): Promise<(File | null)[]> => {
  return Promise.all(
    files.map(async (file) => {
      if (!file) return null;
      return compressImage(file, options);
    })
  );
};
