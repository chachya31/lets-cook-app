package com.cookingapp.domain.valueobject;

/**
 * スケジュールタイプ
 * PLANNED: 料理予定
 * COOKED: 料理実績
 */
public enum ScheduleType {
    PLANNED("planned"),
    COOKED("cooked");

    private final String code;

    ScheduleType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static ScheduleType fromCode(String code) {
        for (ScheduleType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid schedule type code: " + code);
    }
}
