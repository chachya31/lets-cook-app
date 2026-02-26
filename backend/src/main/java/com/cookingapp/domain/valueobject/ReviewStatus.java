package com.cookingapp.domain.valueobject;

/**
 * ReviewStatus Enum
 * レビューステータス
 */
public enum ReviewStatus {
    VISIBLE("visible"),
    HIDDEN("hidden");

    private final String code;

    ReviewStatus(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static ReviewStatus fromCode(String code) {
        for (ReviewStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown ReviewStatus code: " + code);
    }
}
