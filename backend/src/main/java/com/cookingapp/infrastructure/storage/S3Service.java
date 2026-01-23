package com.cookingapp.infrastructure.storage;

import com.cookingapp.application.port.ImageStoragePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

@Service
public class S3Service implements ImageStoragePort {

    private static final Logger logger = LoggerFactory.getLogger(S3Service.class);
    private static final String RECIPE_IMAGE_PREFIX = "images/recipe/";

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final String bucketName;
    private final int presignedUrlExpirationMinutes;

    public S3Service(
            S3Client s3Client,
            S3Presigner s3Presigner,
            @Value("${aws.s3.bucket-name}") String bucketName,
            @Value("${aws.s3.presigned-url-expiration-minutes:60}") int presignedUrlExpirationMinutes) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.bucketName = bucketName;
        this.presignedUrlExpirationMinutes = presignedUrlExpirationMinutes;
    }

    public String uploadRecipeImage(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String uniqueFilename = UUID.randomUUID().toString() + extension;
        String key = RECIPE_IMAGE_PREFIX + uniqueFilename;

        logger.debug("Uploading recipe image to S3: {}", key);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(file.getContentType())
                .contentLength(file.getSize())
                .build();

        try (var inputStream = file.getInputStream()) {
            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, file.getSize()));
        }

        logger.info("Recipe image uploaded successfully: {}", key);
        return key;
    }

    @Override
    public String generatePresignedUrl(String key) {
        if (key == null || key.isEmpty()) {
            return null;
        }

        logger.debug("Generating presigned URL for key: {}", key);

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(presignedUrlExpirationMinutes))
                .getObjectRequest(getObjectRequest)
                .build();

        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
        String url = presignedRequest.url().toString();

        logger.debug("Presigned URL generated for key: {}", key);
        return url;
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex).toLowerCase();
    }
}
