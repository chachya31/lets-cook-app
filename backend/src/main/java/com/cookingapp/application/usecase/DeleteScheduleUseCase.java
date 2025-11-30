package com.cookingapp.application.usecase;

import com.cookingapp.domain.entity.Schedule;
import com.cookingapp.domain.exception.ScheduleNotFoundException;
import com.cookingapp.domain.exception.UnauthorizedException;
import com.cookingapp.domain.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * スケジュール削除ユースケース
 */
@Service
@RequiredArgsConstructor
public class DeleteScheduleUseCase {
    private final ScheduleRepository scheduleRepository;

    public void execute(String scheduleId, String userId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ScheduleNotFoundException("Schedule not found: " + scheduleId));

        if (!schedule.canEdit(userId)) {
            throw new UnauthorizedException("You are not authorized to delete this schedule");
        }

        scheduleRepository.delete(scheduleId);
    }
}
