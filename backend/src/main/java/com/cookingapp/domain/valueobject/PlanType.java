package com.cookingapp.domain.valueobject;

import lombok.Getter;

/**
 * サブスクリプションプランタイプ
 * 各プランの月間クレジット数を定義
 */
@Getter
public enum PlanType {
    FREE("FREE", 10, false), // 初回付与10回、月次リセットなし
    LITE("LITE", 30, true), // 月間30回
    STANDARD("STANDARD", 60, true), // 月間60回（標準プラン）
    PREMIUM("PREMIUM", 9999, true); // 実質無制限

    private final String code;
    private final int monthlyCredits;
    private final boolean hasMonthlyReset;

    PlanType(String code, int monthlyCredits, boolean hasMonthlyReset) {
        this.code = code;
        this.monthlyCredits = monthlyCredits;
        this.hasMonthlyReset = hasMonthlyReset;
    }

    /**
     * コードからPlanTypeを取得
     */
    public static PlanType fromCode(String code) {
        if (code == null) {
            return FREE;
        }
        for (PlanType plan : values()) {
            if (plan.code.equalsIgnoreCase(code)) {
                return plan;
            }
        }
        return FREE;
    }
}
