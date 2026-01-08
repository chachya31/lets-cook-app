package com.cookingapp.domain.service;

import java.io.InputStream;

/**
 * 画像ストレージサービスのインターフェース
 * テスト可能性を向上させるために導入
 */
public interface ImageStorageService {

    /**
     * 画像をアップロード
     * 
     * @param fileName      ファイル名
     * @param contentType   コンテンツタイプ
     * @param inputStream   画像データ
     * @param contentLength ファイルサイズ
     * @return アップロードされた画像のS3キー（パス）
     */
    String uploadImage(String fileName, String contentType, InputStream inputStream, long contentLength);

    /**
     * 画像を削除
     * 
     * @param imageKey S3キー（パス）
     */
    void deleteImage(String imageKey);

    /**
     * 画像アップロード用のPre-signed URLを生成
     * 
     * @param fileName    ファイル名
     * @param contentType コンテンツタイプ
     * @return Pre-signed URL
     */
    String generateUploadUrl(String fileName, String contentType);

    /**
     * S3キーから画像取得用のPre-signed URLを生成
     * 
     * @param imageKey S3キー（パス）
     * @return Pre-signed URL
     */
    String generateDownloadUrl(String imageKey);

    /**
     * 画像URLまたはS3キーから画像取得用のPre-signed URLを生成
     * 既存のPresigned URLやS3キーの両方に対応
     * 
     * @param imageUrlOrKey 画像URLまたはS3キー
     * @return Pre-signed URL
     */
    String generatePresignedUrl(String imageUrlOrKey);
}
