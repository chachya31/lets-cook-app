package com.cookingapp.domain.exception;

import java.time.LocalDate;
import java.util.List;

import com.cookingapp.domain.valueobject.PlanType;

import lombok.Getter;

/**
 * クレジット上限超過例外
 * ユーザーの利用可能クレジットが0の場合にスロー
 */
@Getter
public class QuotaExceededException extends RuntimeException {

    private final String userId;
    private final PlanType planType;
    private final int limit;
    private final LocalDate resetDate;
    private final List<String> upgradeOptions;

    public QuotaExceededException(String userId, PlanType planType, int limit, LocalDate resetDate) {
        super(String.format("User %s has exceeded quota for plan %s (limit: %d)",
                userId, planType.getCode(), limit));
        this.userId = userId;
        this.planType = planType;
        this.limit = limit;
        this.resetDate = resetDate;
        this.upgradeOptions = determineUpgradeOptions(planType);
    }

    private List<String> determineUpgradeOptions(PlanType currentPlan) {
        return switch (currentPlan) {
            case FREE -> List.of("LITE", "STANDARD", "PREMIUM", "ADD_ON");
            case LITE -> List.of("STANDARD", "PREMIUM", "ADD_ON");
            case STANDARD -> List.of("PREMIUM", "ADD_ON");
            case PREMIUM -> List.of();
        };
    }
}
