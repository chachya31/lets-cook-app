package com.cookingapp.presentation.controller;

import com.cookingapp.application.usecase.alert.AlertResponse;
import com.cookingapp.application.usecase.alert.CheckAlertUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 繧｢繝ｩ繝ｼ繝医さ繝ｳ繝医Ο繝ｼ繝ｩ繝ｼ
 * 繧ｵ繝懊ｊ髦ｲ豁｢繧｢繝ｩ繝ｼ繝域ｩ溯・縺ｮ繧ｨ繝ｳ繝峨・繧､繝ｳ繝医ｒ謠蝉ｾ・
 */
@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {
    private final CheckAlertUseCase checkAlertUseCase;

    /**
     * 繧｢繝ｩ繝ｼ繝郁｡ｨ遉ｺ蛻､螳・
     * 
     * @param userId 繝ｦ繝ｼ繧ｶ繝ｼID・・-User-Id繝倥ャ繝繝ｼ・・
     * @return 繧｢繝ｩ繝ｼ繝域ュ蝣ｱ
     */
    @GetMapping("/check")
    public ResponseEntity<AlertResponse> checkAlert(
            @RequestHeader("X-User-Id") String userId) {
        AlertResponse response = checkAlertUseCase.checkAlert(userId);
        return ResponseEntity.ok(response);
    }
}
