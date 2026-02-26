package com.cookingapp.application.validation;

import com.cookingapp.domain.constants.ValidationConstants;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

/**
 * 画像バリデーター
 * ファイルサイズとフォーマットを検証
 */
@Component
public class ImageValidator {

    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "image/jpeg",
            "image/jpg",
            "image/png"
    );
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            ".jpg",
            ".jpeg",
            ".png"
    );

    /**
     * 画像ファイルを検証
     * 
     * @param file アップロードされたファイル
     * @throws ImageValidationException バリデーションエラー
     */
    public void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ImageValidationException("ファイルが選択されていません");
        }

        validateFileSize(file);
        validateContentType(file);
        validateFileExtension(file);
    }

    /**
     * ファイルサイズを検証（5MB以下）
     * 
     * @param file アップロードされたファイル
     * @throws ImageValidationException サイズ超過
     */
    private void validateFileSize(MultipartFile file) {
        if (file.getSize() > ValidationConstants.IMAGE_MAX_FILE_SIZE) {
            throw new ImageValidationException(
                    String.format("ファイルサイズが大きすぎます。最大%dMBまでです", 
                        ValidationConstants.IMAGE_MAX_FILE_SIZE_MB)
            );
        }
    }

    /**
     * コンテンツタイプを検証（JPEG、PNG）
     * 
     * @param file アップロードされたファイル
     * @throws ImageValidationException 無効なコンテンツタイプ
     */
    private void validateContentType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new ImageValidationException(
                    "サポートされていないファイル形式です。JPEG、PNGのみ対応しています"
            );
        }
    }

    /**
     * ファイル拡張子を検証
     * 
     * @param file アップロードされたファイル
     * @throws ImageValidationException 無効な拡張子
     */
    private void validateFileExtension(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new ImageValidationException("ファイル名が無効です");
        }

        String extension = getFileExtension(originalFilename).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new ImageValidationException(
                    "サポートされていないファイル拡張子です。.jpg、.jpeg、.pngのみ対応しています"
            );
        }
    }

    /**
     * ファイル拡張子を取得
     * 
     * @param filename ファイル名
     * @return 拡張子（.を含む）
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            return filename.substring(lastDotIndex);
        }
        return "";
    }

    /**
     * コンテンツタイプが有効か確認
     * 
     * @param contentType コンテンツタイプ
     * @return 有効な場合true
     */
    public boolean isValidContentType(String contentType) {
        return contentType != null && ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase());
    }

    /**
     * ファイルサイズが有効か確認
     * 
     * @param fileSize ファイルサイズ（バイト）
     * @return 有効な場合true
     */
    public boolean isValidFileSize(long fileSize) {
        return fileSize > 0 && fileSize <= ValidationConstants.IMAGE_MAX_FILE_SIZE;
    }
}
