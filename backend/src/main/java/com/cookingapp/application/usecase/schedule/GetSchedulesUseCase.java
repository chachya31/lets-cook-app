package com.cookingapp.application.usecase.schedule;

import com.cookingapp.domain.entity.Schedule;
import com.cookingapp.domain.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * スケジュール取得ユースケース
 */
@Service
@RequiredArgsConstructor
public class GetSchedulesUseCase {
    private final ScheduleRepository scheduleRepository;

    public List<Schedule> execute(String userId, LocalDate startDate, LocalDate endDate) {
        return scheduleRepository.findByUserIdAndDateRange(userId, startDate, endDate);
    }
}
