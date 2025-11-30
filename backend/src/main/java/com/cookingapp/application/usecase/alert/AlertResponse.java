package com.cookingapp.application.usecase.alert;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * アラートレスポンス
 */
@Getter
@AllArgsConstructor
public class AlertResponse {
    /**
     * アラート表示フラグ
     */
    private final boolean shouldShow;

    /**
     * アラートメッセージ
     */
    private final String message;
}
