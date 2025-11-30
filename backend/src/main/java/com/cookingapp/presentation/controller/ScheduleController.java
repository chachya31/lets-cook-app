package com.cookingapp.presentation.controller;

import com.cookingapp.application.usecase.schedule.*;
import com.cookingapp.domain.entity.Schedule;
import com.cookingapp.domain.valueobject.ScheduleType;
import com.cookingapp.presentation.dto.request.CreateScheduleRequest;
import com.cookingapp.presentation.dto.request.UpdateScheduleRequest;
import com.cookingapp.presentation.dto.response.ScheduleResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * スケジュール管琁E��ントローラー
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
     * スケジュール一覧取征E
     */
    @GetMapping
    public ResponseEntity<List<ScheduleResponse>> getSchedules(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        List<Schedule> schedules = getSchedulesUseCase.execute(userId, startDate, endDate);
        List<ScheduleResponse> response = schedules.stream()
                .map(ScheduleResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    /**
     * スケジュール作�E
     */
    @PostMapping
    public ResponseEntity<ScheduleResponse> createSchedule(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody CreateScheduleRequest request
    ) {
        Schedule schedule = createScheduleUseCase.execute(
                userId,
                LocalDate.parse(request.getDate()),
                ScheduleType.fromCode(request.getType()),
                request.getRecipeId(),
                request.getRecipeTitle(),
                request.getMemo()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(ScheduleResponse.from(schedule));
    }

    /**
     * スケジュール更新
     */
    @PutMapping("/{scheduleId}")
    public ResponseEntity<ScheduleResponse> updateSchedule(
            @PathVariable String scheduleId,
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody UpdateScheduleRequest request
    ) {
        Schedule schedule = updateScheduleUseCase.execute(scheduleId, userId, request.getMemo());
        return ResponseEntity.ok(ScheduleResponse.from(schedule));
    }

    /**
     * スケジュール削除
     */
    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<Void> deleteSchedule(
            @PathVariable String scheduleId,
            @RequestHeader("X-User-Id") String userId
    ) {
        deleteScheduleUseCase.execute(scheduleId, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 予定を実績に変換
     */
    @PostMapping("/{scheduleId}/convert-to-cooked")
    public ResponseEntity<ScheduleResponse> convertToCooked(
            @PathVariable String scheduleId,
            @RequestHeader("X-User-Id") String userId
    ) {
        Schedule schedule = convertScheduleToCookedUseCase.execute(scheduleId, userId);
        return ResponseEntity.ok(ScheduleResponse.from(schedule));
    }
}
