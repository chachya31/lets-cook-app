package com.cookingapp.presentation.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * スケジュール更新リクエストDTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateScheduleRequest {
    @Size(max = 120, message = "Memo must be 120 characters or less")
    private String memo;
}
