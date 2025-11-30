package com.cookingapp.domain.entity;

import com.cookingapp.domain.valueobject.ScheduleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * スケジュールエンティティ
 * 料理予定と料理実績を管理する
 */
@Getter
@Builder
@AllArgsConstructor
public class Schedule {
    private final String scheduleId;
    private final String userId;
    private final LocalDate date;
    private ScheduleType type;
    private final String recipeId;
    private final String recipeTitle;
    private String memo;
    private final Instant createdAt;

    /**
     * 新しいスケジュールを作成
     */
    public static Schedule create(
            String userId,
            LocalDate date,
            ScheduleType type,
            String recipeId,
            String recipeTitle,
            String memo
    ) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        if (type == null) {
            throw new IllegalArgumentException("Schedule type cannot be null");
        }
        if (recipeId == null || recipeId.isBlank()) {
            throw new IllegalArgumentException("Recipe ID cannot be null or empty");
        }
        if (recipeTitle == null || recipeTitle.isBlank()) {
            throw new IllegalArgumentException("Recipe title cannot be null or empty");
        }
        if (memo != null && memo.length() > 120) {
            throw new IllegalArgumentException("Memo must be 120 characters or less");
        }

        return Schedule.builder()
                .scheduleId(UUID.randomUUID().toString())
                .userId(userId)
                .date(date)
                .type(type)
                .recipeId(recipeId)
                .recipeTitle(recipeTitle)
                .memo(memo)
                .createdAt(Instant.now())
                .build();
    }

    /**
     * 予定を実績に変換
     */
    public void convertToCooked() {
        if (this.type != ScheduleType.PLANNED) {
            throw new IllegalStateException("Only PLANNED schedules can be converted to COOKED");
        }
        this.type = ScheduleType.COOKED;
    }

    /**
     * メモを更新
     */
    public void updateMemo(String newMemo) {
        if (newMemo != null && newMemo.length() > 120) {
            throw new IllegalArgumentException("Memo must be 120 characters or less");
        }
        this.memo = newMemo;
    }

    /**
     * スケジュールを編集可能かチェック
     */
    public boolean canEdit(String requestUserId) {
        return this.userId.equals(requestUserId);
    }
}
