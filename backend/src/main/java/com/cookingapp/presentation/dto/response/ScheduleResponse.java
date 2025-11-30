package com.cookingapp.presentation.dto.response;

import com.cookingapp.domain.entity.Schedule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.format.DateTimeFormatter;

/**
 * スケジュールレスポンスDTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleResponse {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private String scheduleId;
    private String userId;
    private String date;
    private String type;
    private String recipeId;
    private String recipeTitle;
    private String memo;
    private String createdAt;

    public static ScheduleResponse from(Schedule schedule) {
        return ScheduleResponse.builder()
                .scheduleId(schedule.getScheduleId())
                .userId(schedule.getUserId())
                .date(schedule.getDate().format(DATE_FORMATTER))
                .type(schedule.getType().getCode())
                .recipeId(schedule.getRecipeId())
                .recipeTitle(schedule.getRecipeTitle())
                .memo(schedule.getMemo())
                .createdAt(schedule.getCreatedAt().toString())
                .build();
    }
}
