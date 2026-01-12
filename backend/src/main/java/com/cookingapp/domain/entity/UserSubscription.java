package com.cookingapp.domain.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.cookingapp.domain.valueobject.PlanType;
import com.cookingapp.domain.valueobject.SubscriptionStatus;

import lombok.Getter;

/**
 * ユーザーサブスクリプションエンティティ
 * 課金プランとクレジット残高を管理
 */
@Getter
public class UserSubscription {

    private final String userId;
    private PlanType planType;
    private int remainingCredits;
    private LocalDate resetDate;
    private SubscriptionStatus status;
    private LocalDate planStartDate;
    private long totalCreditsUsed;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 新規サブスクリプション作成（FREEプラン）
     */
    public static UserSubscription createFreeSubscription(String userId) {
        return new UserSubscription(
                userId,
                PlanType.FREE,
                PlanType.FREE.getMonthlyCredits(),
                null, // FREEプランはリセットなし
                SubscriptionStatus.ACTIVE,
                LocalDate.now(),
                0,
                LocalDateTime.now(),
                LocalDateTime.now());
    }

    /**
     * 既存サブスクリプション復元（リポジトリから取得時）
     */
    public UserSubscription(String userId, PlanType planType, int remainingCredits,
            LocalDate resetDate, SubscriptionStatus status,
            LocalDate planStartDate, long totalCreditsUsed,
            LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.userId = userId;
        this.planType = planType;
        this.remainingCredits = remainingCredits;
        this.resetDate = resetDate;
        this.status = status;
        this.planStartDate = planStartDate;
        this.totalCreditsUsed = totalCreditsUsed;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * クレジットを消費可能かチェック
     */
    public boolean canConsumeCredit() {
        if (status != SubscriptionStatus.ACTIVE) {
            return false;
        }
        if (planType == PlanType.PREMIUM) {
            return true;
        }
        return remainingCredits > 0;
    }

    /**
     * クレジットを1消費
     * 
     * @return 消費後の残りクレジット数
     */
    public int consumeCredit() {
        if (!canConsumeCredit()) {
            throw new IllegalStateException("No credits available");
        }
        if (planType != PlanType.PREMIUM) {
            this.remainingCredits--;
        }
        this.totalCreditsUsed++;
        this.updatedAt = LocalDateTime.now();
        return this.remainingCredits;
    }

    /**
     * クレジットを追加（ADD_ON購入時）
     */
    public void addCredits(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        this.remainingCredits += amount;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 月次リセットが必要かチェック
     */
    public boolean needsMonthlyReset(LocalDate today) {
        if (!planType.isHasMonthlyReset()) {
            return false;
        }
        if (resetDate == null) {
            return true;
        }
        return !today.isBefore(resetDate);
    }

    /**
     * 月次リセットを実行
     */
    public void performMonthlyReset(LocalDate today) {
        if (!planType.isHasMonthlyReset()) {
            return;
        }
        this.remainingCredits = planType.getMonthlyCredits();
        this.resetDate = today.plusMonths(1).withDayOfMonth(1);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * プランをアップグレード
     */
    public void upgradePlan(PlanType newPlan, LocalDate today) {
        this.planType = newPlan;
        this.planStartDate = today;
        this.status = SubscriptionStatus.ACTIVE;

        if (newPlan.isHasMonthlyReset()) {
            this.remainingCredits = newPlan.getMonthlyCredits();
            this.resetDate = today.plusMonths(1).withDayOfMonth(1);
        }
        this.updatedAt = LocalDateTime.now();
    }
}
