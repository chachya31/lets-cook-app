package com.cookingapp.application.dto;

import com.cookingapp.domain.model.Step;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StepInput {
    private final Integer stepNumber;
    private final String instruction;
    private final String imageUrl;

    public Step toEntity() {
        return Step.builder()
                .stepNumber(stepNumber)
                .instruction(instruction)
                .imageUrl(imageUrl)
                .build();
    }
}
