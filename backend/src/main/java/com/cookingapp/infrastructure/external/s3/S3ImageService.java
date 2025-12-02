package com.cookingapp.infrastructure.external.s3;

import com.cookingapp.domain.service.ImageStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.InputStream;
import java.time.Duration;
import java.util.UUID;

/**
 * S3画像サービス
 * 画像のアップロード、取得、削除を管理
 */
@Service
public class S3ImageService implements ImageStorageService {

    private static final Logger logger = LoggerFactory.getLogger(S3ImageService.class);

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final String bucketName;

    public S3ImageService(
            S3Client s3Client,
            S3Presigner s3Presigner,
            @Value("${aws.s3.bucket}") String bucketName) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.bucketName = bucketName;
    }

    /**
     * 画像アップロード用のPre-signed URLを生成
     * 
     * @param fileName ファイル名
     * @param contentType コンテンツタイプ（image/jpeg, image/png）
     * @return Pre-signed URL
     */
    public String generateUploadUrl(String fileName, String contentType) {
        String key = generateKey(fileName);
        
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(15)) // 15分間有効
                .putObjectRequest(putObjectRequest)
                .build();

        String presignedUrl = s3Presigner.presignPutObject(presignRequest).url().toString();
        
        logger.info("Generated upload URL for key: {}", key);
        return presignedUrl;
    }

    /**
     * 画像を直接アップロード
     * 
     * @param fileName ファイル名
     * @param contentType コンテンツタイプ
     * @param inputStream 画像データ
     * @param contentLength ファイルサイズ
     * @return 画像URL
     */
    public String uploadImage(String fileName, String contentType, InputStream inputStream, long contentLength) {
        String key = generateKey(fileName);
        
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(contentType)
                    .contentLength(contentLength)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, contentLength));
            
            logger.info("Uploaded image with key: {}", key);
            return getImageUrl(key);
        } catch (S3Exception e) {
            logger.error("Failed to upload image: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload image", e);
        }
    }

    /**
     * 画像取得用のURLを生成
     * 
     * @param key S3オブジェクトキー
     * @return 画像URL
     */
    public String getImageUrl(String key) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofHours(1)) // 1時間有効
                .getObjectRequest(getObjectRequest)
                .build();

        return s3Presigner.presignGetObject(presignRequest).url().toString();
    }

    @Override
    public String generateDownloadUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return null;
        }
        String key = extractKeyFromUrl(imageUrl);
        return getImageUrl(key);
    }

    /**
     * 画像を削除
     * 
     * @param imageUrl 画像URL
     */
    public void deleteImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return;
        }

        try {
            String key = extractKeyFromUrl(imageUrl);
            
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            
            logger.info("Deleted image with key: {}", key);
        } catch (S3Exception e) {
            logger.error("Failed to delete image: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete image", e);
        }
    }

    /**
     * 画像が存在するか確認
     * 
     * @param imageUrl 画像URL
     * @return 存在する場合true
     */
    public boolean imageExists(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return false;
        }

        try {
            String key = extractKeyFromUrl(imageUrl);
            
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.headObject(headObjectRequest);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (S3Exception e) {
            logger.error("Failed to check image existence: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * ユニークなキーを生成
     * 
     * @param fileName ファイル名
     * @return S3オブジェクトキー
     */
    private String generateKey(String fileName) {
        String uuid = UUID.randomUUID().toString();
        String extension = getFileExtension(fileName);
        return String.format("images/%s%s", uuid, extension);
    }

    /**
     * ファイル拡張子を取得
     * 
     * @param fileName ファイル名
     * @return 拡張子（.を含む）
     */
    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex);
        }
        return "";
    }

    /**
     * URLからS3キーを抽出
     * 
     * @param imageUrl 画像URL
     * @return S3オブジェクトキー
     */
    private String extractKeyFromUrl(String imageUrl) {
        // Pre-signed URLからキーを抽出
        // 例: https://bucket.s3.region.amazonaws.com/images/uuid.jpg?params...
        // または: http://localhost:4566/bucket/images/uuid.jpg
        
        String[] parts = imageUrl.split("\\?")[0].split("/");
        
        // LocalStackの場合: http://localhost:4566/bucket/images/uuid.jpg
        // AWS S3の場合: https://bucket.s3.region.amazonaws.com/images/uuid.jpg
        
        if (imageUrl.contains("localhost")) {
            // LocalStack形式: バケット名の後からがキー
            int bucketIndex = -1;
            for (int i = 0; i < parts.length; i++) {
                if (parts[i].equals(bucketName)) {
                    bucketIndex = i;
                    break;
                }
            }
            if (bucketIndex >= 0 && bucketIndex < parts.length - 1) {
                StringBuilder key = new StringBuilder();
                for (int i = bucketIndex + 1; i < parts.length; i++) {
                    if (i > bucketIndex + 1) {
                        key.append("/");
                    }
                    key.append(parts[i]);
                }
                return key.toString();
            }
        } else {
            // AWS S3形式: ドメインの後からがキー
            int startIndex = 3; // https://bucket.s3.region.amazonaws.com/ の後
            if (parts.length > startIndex) {
                StringBuilder key = new StringBuilder();
                for (int i = startIndex; i < parts.length; i++) {
                    if (i > startIndex) {
                        key.append("/");
                    }
                    key.append(parts[i]);
                }
                return key.toString();
            }
        }
        
        throw new IllegalArgumentException("Invalid image URL format: " + imageUrl);
    }
}
