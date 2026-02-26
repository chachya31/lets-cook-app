package com.cookingapp.presentation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cookingapp.application.usecase.alert.AlertResponse;
import com.cookingapp.application.usecase.alert.CheckAlertUseCase;
import com.cookingapp.infrastructure.security.SecurityUtils;

import lombok.RequiredArgsConstructor;

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
     * @return アラート情報
     */
    @GetMapping("/check")
    public ResponseEntity<AlertResponse> checkAlert() {
        String userId = SecurityUtils.getCurrentUserId();
        AlertResponse response = checkAlertUseCase.checkAlert(userId);
        return ResponseEntity.ok(response);
    }
}
