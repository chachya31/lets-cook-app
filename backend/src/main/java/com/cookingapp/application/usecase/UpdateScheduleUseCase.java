package com.cookingapp.application.usecase;

import com.cookingapp.domain.entity.Schedule;
import com.cookingapp.domain.exception.ScheduleNotFoundException;
import com.cookingapp.domain.exception.UnauthorizedException;
import com.cookingapp.domain.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * スケジュール更新ユースケース
 */
@Service
@RequiredArgsConstructor
public class UpdateScheduleUseCase {
    private final ScheduleRepository scheduleRepository;

    public Schedule execute(String scheduleId, String userId, String memo) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ScheduleNotFoundException("Schedule not found: " + scheduleId));

        if (!schedule.canEdit(userId)) {
            throw new UnauthorizedException("You are not authorized to update this schedule");
        }

        schedule.updateMemo(memo);
        return scheduleRepository.save(schedule);
    }
}
