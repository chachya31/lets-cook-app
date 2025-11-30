package com.cookingapp.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * スケジュール作成リクエストDTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateScheduleRequest {
    @NotBlank(message = "Date is required")
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Date must be in YYYY-MM-DD format")
    private String date;

    @NotBlank(message = "Type is required")
    @Pattern(regexp = "planned|cooked", message = "Type must be 'planned' or 'cooked'")
    private String type;

    @NotBlank(message = "Recipe ID is required")
    private String recipeId;

    @NotBlank(message = "Recipe title is required")
    private String recipeTitle;

    @Size(max = 120, message = "Memo must be 120 characters or less")
    private String memo;
}
