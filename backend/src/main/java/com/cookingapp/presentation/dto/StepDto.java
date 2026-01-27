package com.cookingapp.presentation.dto;

import com.cookingapp.domain.model.Step;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class StepDto {

    @NotNull(message = "Step number is required")
    private final Integer stepNumber;

    @NotBlank(message = "Description is required")
    private final String description;

    private final String imageUrl;

    private final String videoUrl;

    public StepDto() {
        this.stepNumber = null;
        this.description = null;
        this.imageUrl = null;
        this.videoUrl = null;
    }

    public Step toEntity() {
        return Step.builder()
                .stepNumber(stepNumber)
                .description(description)
                .imageUrl(imageUrl)
                .videoUrl(videoUrl)
                .build();
    }

    public static StepDto from(Step step) {
        return StepDto.builder()
                .stepNumber(step.getStepNumber())
                .description(step.getDescription())
                .imageUrl(step.getImageUrl())
                .videoUrl(step.getVideoUrl())
                .build();
    }
}
