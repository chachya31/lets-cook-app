package com.cookingapp.domain.repository;

import com.cookingapp.domain.entity.Schedule;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * スケジュールリポジトリインターフェース
 */
public interface ScheduleRepository {
    /**
     * スケジュールを保存
     */
    Schedule save(Schedule schedule);

    /**
     * スケジュールIDで検索
     */
    Optional<Schedule> findById(String scheduleId);

    /**
     * ユーザーIDと日付範囲でスケジュールを検索
     */
    List<Schedule> findByUserIdAndDateRange(String userId, LocalDate startDate, LocalDate endDate);

    /**
     * スケジュールを削除
     */
    void delete(String scheduleId);

    /**
     * スケジュールが存在するかチェック
     */
    boolean existsById(String scheduleId);
}
