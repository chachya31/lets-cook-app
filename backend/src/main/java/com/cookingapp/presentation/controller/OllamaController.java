package com.cookingapp.presentation.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cookingapp.infrastructure.external.ollama.OllamaService;

/**
 * Ollamaコントローラー
 * ローカルLLM機能のエンドポイントを提供
 */
@RestController
@RequestMapping("/api/ollama")
@ConditionalOnProperty(name = "ollama.enabled", havingValue = "true")
public class OllamaController {

    private static final Logger log = LoggerFactory.getLogger(OllamaController.class);

    private final OllamaService ollamaService;

    public OllamaController(OllamaService ollamaService) {
        this.ollamaService = ollamaService;
    }

    /**
     * テキスト生成
     */
    @PostMapping("/generate")
    public ResponseEntity<GenerateResponse> generate(@RequestBody GenerateRequest request) {
        log.info("POST /api/ollama/generate - prompt length: {}",
                request.prompt() != null ? request.prompt().length() : 0);

        String result = ollamaService.generateContent(request.prompt());
        return ResponseEntity.ok(new GenerateResponse(result));
    }

    // Request/Response DTOs
    public record GenerateRequest(String prompt) {
    }

    public record GenerateResponse(String response) {
    }
}
