package com.cookingapp.domain.valueobject;

import lombok.Getter;

/**
 * サブスクリプションステータス
 */
@Getter
public enum SubscriptionStatus {
    ACTIVE("ACTIVE"),
    EXPIRED("EXPIRED"),
    SUSPENDED("SUSPENDED");

    private final String code;

    SubscriptionStatus(String code) {
        this.code = code;
    }

    public static SubscriptionStatus fromCode(String code) {
        if (code == null) {
            return ACTIVE;
        }
        for (SubscriptionStatus status : values()) {
            if (status.code.equalsIgnoreCase(code)) {
                return status;
            }
        }
        return ACTIVE;
    }
}
