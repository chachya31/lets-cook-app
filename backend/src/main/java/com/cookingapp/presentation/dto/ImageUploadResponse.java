package com.cookingapp.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ImageUploadResponse {

    private final String imageKey;
    private final String error;
    private final boolean success;

    public static ImageUploadResponse success(String imageKey) {
        return ImageUploadResponse.builder()
                .imageKey(imageKey)
                .success(true)
                .build();
    }

    public static ImageUploadResponse error(String error) {
        return ImageUploadResponse.builder()
                .error(error)
                .success(false)
                .build();
    }
}
