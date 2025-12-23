package com.cookingapp.presentation.dto;

import com.cookingapp.domain.valueobject.Step;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 手順DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StepDto {

    @NotBlank(message = "Step description is required")
    private String description;

    private String imageUrl; // 任意

    private String videoUrl; // 任意（YouTube等のURL）

    /**
     * DTOからエンティティに変換
     */
    public Step toEntity() {
        return new Step(description, imageUrl, videoUrl);
    }

    /**
     * エンティティからDTOに変換
     */
    public static StepDto fromEntity(Step step) {
        return new StepDto(step.getDescription(), step.getImageUrl(), step.getVideoUrl());
    }
}
