package com.cookingapp.domain.exception;

/**
 * スケジュールが見つからない場合の例外
 */
public class ScheduleNotFoundException extends RuntimeException {
    public ScheduleNotFoundException(String message) {
        super(message);
    }

    public ScheduleNotFoundException(String scheduleId, Throwable cause) {
        super("Schedule not found: " + scheduleId, cause);
    }
}
