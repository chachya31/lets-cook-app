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
     * @param fileName ファイル名
     * @param contentType コンテンツタイプ
     * @param inputStream 画像データ
     * @param contentLength ファイルサイズ
     * @return アップロードされた画像のURL
     */
    String uploadImage(String fileName, String contentType, InputStream inputStream, long contentLength);
    
    /**
     * 画像を削除
     * 
     * @param imageUrl 画像URL
     */
    void deleteImage(String imageUrl);
    
    /**
     * 画像アップロード用のPre-signed URLを生成
     * 
     * @param fileName ファイル名
     * @param contentType コンテンツタイプ
     * @return Pre-signed URL
     */
    String generateUploadUrl(String fileName, String contentType);
    
    /**
     * 画像取得用のPre-signed URLを生成
     * 
     * @param imageUrl 画像URL
     * @return Pre-signed URL
     */
    String generateDownloadUrl(String imageUrl);
}
