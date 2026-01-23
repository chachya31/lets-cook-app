package com.cookingapp.application.port;

/**
 * Port interface for image storage operations.
 * This abstraction allows the application layer to remain independent
 * of specific storage implementations (S3, local filesystem, etc.).
 */
public interface ImageStoragePort {

    /**
     * Generates a presigned URL for accessing an image.
     *
     * @param imageKey the storage key of the image
     * @return the presigned URL, or null if imageKey is null/empty
     */
    String generatePresignedUrl(String imageKey);
}
