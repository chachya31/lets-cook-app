package com.cookingapp.application.dto;

import com.cookingapp.domain.model.Step;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StepInput {
    private final Integer stepNumber;
    private final String description;
    private final String imageUrl;
    private final String videoUrl;

    public Step toEntity() {
        return Step.builder()
                .stepNumber(stepNumber)
                .description(description)
                .imageUrl(imageUrl)
                .videoUrl(videoUrl)
                .build();
    }
}
