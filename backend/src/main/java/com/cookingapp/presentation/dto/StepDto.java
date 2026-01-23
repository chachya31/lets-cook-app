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

    @NotBlank(message = "Instruction is required")
    private final String instruction;

    private final String imageUrl;

    public StepDto() {
        this.stepNumber = null;
        this.instruction = null;
        this.imageUrl = null;
    }

    public Step toEntity() {
        return Step.builder()
                .stepNumber(stepNumber)
                .instruction(instruction)
                .imageUrl(imageUrl)
                .build();
    }

    public static StepDto from(Step step) {
        return StepDto.builder()
                .stepNumber(step.getStepNumber())
                .instruction(step.getInstruction())
                .imageUrl(step.getImageUrl())
                .build();
    }
}
