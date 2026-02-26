package com.cookingapp.presentation.mapper;

import com.cookingapp.domain.entity.Schedule;
import com.cookingapp.presentation.dto.response.ScheduleResponse;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ScheduleMapper
 * Schedule エンティティと DTO の変換を担当
 */
public class ScheduleMapper {

    private ScheduleMapper() {
        // ユーティリティクラスのため、インスタンス化を防ぐ
    }

    /**
     * Schedule エンティティから ScheduleResponse に変換
     */
    public static ScheduleResponse toResponse(Schedule schedule) {
        return ScheduleResponse.from(schedule);
    }

    /**
     * Schedule エンティティのリストから ScheduleResponse のリストに変換
     */
    public static List<ScheduleResponse> toResponseList(List<Schedule> schedules) {
        return schedules.stream()
                .map(ScheduleMapper::toResponse)
                .collect(Collectors.toList());
    }
}
