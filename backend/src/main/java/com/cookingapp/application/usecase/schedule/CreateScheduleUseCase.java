package com.cookingapp.application.usecase.schedule;

import com.cookingapp.domain.entity.Schedule;
import com.cookingapp.domain.repository.ScheduleRepository;
import com.cookingapp.domain.valueobject.ScheduleType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

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
            ScheduleType type,
            String recipeId,
            String recipeTitle,
            String memo
    ) {
        Schedule schedule = Schedule.create(userId, date, type, recipeId, recipeTitle, memo);
        return scheduleRepository.save(schedule);
    }
}
