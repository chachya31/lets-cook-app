package com.cookingapp.presentation.controller;

import com.cookingapp.application.usecase.alert.AlertResponse;
import com.cookingapp.application.usecase.alert.CheckAlertUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * アラートコントローラー
 * サボり防止アラート機能のエンドポイントを提供
 */
@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {
    private final CheckAlertUseCase checkAlertUseCase;

    /**
     * アラート表示判定
     * 
     * @param userId ユーザーID（X-User-Idヘッダー）
     * @return アラート情報
     */
    @GetMapping("/check")
    public ResponseEntity<AlertResponse> checkAlert(
            @RequestHeader("X-User-Id") String userId) {
        AlertResponse response = checkAlertUseCase.checkAlert(userId);
        return ResponseEntity.ok(response);
    }
}
