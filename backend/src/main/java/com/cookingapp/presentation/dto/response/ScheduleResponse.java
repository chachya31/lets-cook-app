package com.cookingapp.presentation.dto.response;

import java.time.format.DateTimeFormatter;

import com.cookingapp.domain.entity.Schedule;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    @JsonProperty("isDone")
    private boolean isDone;
    private String recipeId;
    private String recipeTitle;
    private String memo;
    private String createdAt;

    public static ScheduleResponse from(Schedule schedule) {
        return ScheduleResponse.builder()
                .scheduleId(schedule.getScheduleId())
                .userId(schedule.getUserId())
                .date(schedule.getDate().format(DATE_FORMATTER))
                .isDone(schedule.isDone())
                .recipeId(schedule.getRecipeId())
                .recipeTitle(schedule.getRecipeTitle())
                .memo(schedule.getMemo())
                .createdAt(schedule.getCreatedAt().toString())
                .build();
    }
}
