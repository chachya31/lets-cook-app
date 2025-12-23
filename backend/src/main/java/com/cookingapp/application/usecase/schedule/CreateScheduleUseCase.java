package com.cookingapp.application.usecase.schedule;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.cookingapp.domain.entity.Schedule;
import com.cookingapp.domain.repository.ScheduleRepository;

import lombok.RequiredArgsConstructor;

/**
 * スケジュール作成ユースケース
 */
@Service
@RequiredArgsConstructor
public class CreateScheduleUseCase {
    private final ScheduleRepository scheduleRepository;

    public Schedule execute(
            String userId,
            LocalDate date,
            String recipeId,
            String recipeTitle,
            String memo) {
        Schedule schedule = Schedule.create(userId, date, recipeId, recipeTitle, memo);
        return scheduleRepository.save(schedule);
    }
}
