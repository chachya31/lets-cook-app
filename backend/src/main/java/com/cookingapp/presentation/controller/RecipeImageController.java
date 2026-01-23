package com.cookingapp.presentation.controller;

import com.cookingapp.infrastructure.storage.S3Service;
import com.cookingapp.presentation.dto.ImageUploadResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Set;

@RestController
@RequestMapping("/api/recipes/images")
public class RecipeImageController {

    private static final Logger logger = LoggerFactory.getLogger(RecipeImageController.class);
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png");
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png");

    private final S3Service s3Service;

    public RecipeImageController(S3Service s3Service) {
        this.s3Service = s3Service;
    }

    @PostMapping
    public ResponseEntity<ImageUploadResponse> uploadImage(@RequestParam("file") MultipartFile file) {
        logger.debug("Received image upload request: {}", file.getOriginalFilename());

        String validationError = validateFile(file);
        if (validationError != null) {
            logger.warn("Image upload validation failed: {}", validationError);
            return ResponseEntity.badRequest()
                    .body(ImageUploadResponse.error(validationError));
        }

        try {
            String imageKey = s3Service.uploadRecipeImage(file);
            logger.info("Image uploaded successfully: {}", imageKey);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ImageUploadResponse.success(imageKey));
        } catch (IOException e) {
            logger.error("Failed to upload image", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ImageUploadResponse.error("Failed to upload image"));
        }
    }

    private String validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return "File is required";
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            return "File size exceeds maximum limit of 5MB";
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            return "File name is required";
        }

        String extension = getFileExtension(originalFilename).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            return "Only JPG and PNG files are allowed";
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            return "Invalid file type. Only JPG and PNG images are allowed";
        }

        return null;
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex);
    }
}
