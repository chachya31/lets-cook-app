package com.cookingapp.application.usecase;

import com.cookingapp.domain.entity.Schedule;
import com.cookingapp.domain.entity.User;
import com.cookingapp.domain.exception.ScheduleNotFoundException;
import com.cookingapp.domain.exception.UnauthorizedException;
import com.cookingapp.domain.exception.UserNotFoundException;
import com.cookingapp.domain.repository.ScheduleRepository;
import com.cookingapp.domain.repository.UserRepository;
import com.cookingapp.domain.valueobject.ScheduleType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * 予定を実績に変換するユースケース
 * LastCookingDateも更新する
 */
@Service
@RequiredArgsConstructor
public class ConvertScheduleToCookedUseCase {
    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;

    public Schedule execute(String scheduleId, String userId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ScheduleNotFoundException("Schedule not found: " + scheduleId));

        if (!schedule.canEdit(userId)) {
            throw new UnauthorizedException("You are not authorized to convert this schedule");
        }

        schedule.convertToCooked();
        Schedule updatedSchedule = scheduleRepository.save(schedule);

        // LastCookingDateを更新
        updateLastCookingDate(userId, schedule.getDate());

        return updatedSchedule;
    }

    private void updateLastCookingDate(String userId, LocalDate cookingDate) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

        if (user.getLastCookingDate() == null || cookingDate.isAfter(user.getLastCookingDate())) {
            user.updateLastCookingDate(cookingDate);
            userRepository.save(user);
        }
    }
}
