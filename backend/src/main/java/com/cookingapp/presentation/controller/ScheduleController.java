package com.cookingapp.presentation.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cookingapp.application.usecase.schedule.ConvertScheduleToCookedUseCase;
import com.cookingapp.application.usecase.schedule.CreateScheduleUseCase;
import com.cookingapp.application.usecase.schedule.DeleteScheduleUseCase;
import com.cookingapp.application.usecase.schedule.GetSchedulesUseCase;
import com.cookingapp.application.usecase.schedule.UpdateScheduleUseCase;
import com.cookingapp.domain.entity.Schedule;
import com.cookingapp.presentation.dto.request.CreateScheduleRequest;
import com.cookingapp.presentation.dto.request.UpdateScheduleRequest;
import com.cookingapp.presentation.dto.response.ScheduleResponse;
import com.cookingapp.presentation.mapper.ScheduleMapper;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * スケジュール管理コントローラー
 */
@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class ScheduleController {
    private final CreateScheduleUseCase createScheduleUseCase;
    private final UpdateScheduleUseCase updateScheduleUseCase;
    private final DeleteScheduleUseCase deleteScheduleUseCase;
    private final GetSchedulesUseCase getSchedulesUseCase;
    private final ConvertScheduleToCookedUseCase convertScheduleToCookedUseCase;

    /**
     * スケジュール一覧取得
     */
    @GetMapping
    public ResponseEntity<List<ScheduleResponse>> getSchedules(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(name = "startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<Schedule> schedules = getSchedulesUseCase.execute(userId, startDate, endDate);
        return ResponseEntity.ok(ScheduleMapper.toResponseList(schedules));
    }

    /**
     * スケジュール作成
     */
    @PostMapping
    public ResponseEntity<ScheduleResponse> createSchedule(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody CreateScheduleRequest request) {
        Schedule schedule = createScheduleUseCase.execute(
                userId,
                LocalDate.parse(request.getDate()),
                request.getRecipeId(),
                request.getRecipeTitle(),
                request.getMemo());
        return ResponseEntity.status(HttpStatus.CREATED).body(ScheduleMapper.toResponse(schedule));
    }

    /**
     * スケジュール更新
     */
    @PutMapping("/{scheduleId}")
    public ResponseEntity<ScheduleResponse> updateSchedule(
            @PathVariable("scheduleId") String scheduleId,
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody UpdateScheduleRequest request) {
        Schedule schedule = updateScheduleUseCase.execute(scheduleId, userId, request.getMemo());
        return ResponseEntity.ok(ScheduleMapper.toResponse(schedule));
    }

    /**
     * スケジュール削除
     */
    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<Void> deleteSchedule(
            @PathVariable("scheduleId") String scheduleId,
            @RequestHeader("X-User-Id") String userId) {
        deleteScheduleUseCase.execute(scheduleId, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 予定を実績に変換（完了にする）
     */
    @PostMapping("/{scheduleId}/mark-done")
    public ResponseEntity<ScheduleResponse> markAsDone(
            @PathVariable("scheduleId") String scheduleId,
            @RequestHeader("X-User-Id") String userId) {
        Schedule schedule = convertScheduleToCookedUseCase.execute(scheduleId, userId);
        return ResponseEntity.ok(ScheduleMapper.toResponse(schedule));
    }
}
