package com.cookingapp.application.usecase.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.cookingapp.infrastructure.external.gemini.GeminiService;

/**
 * AIチャットユースケース
 */
@Service
public class ChatUseCase {

    private static final Logger log = LoggerFactory.getLogger(ChatUseCase.class);

    private final GeminiService geminiService;

    public ChatUseCase(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    /**
     * チャットメッセージを処理
     */
    public String chat(String message) {
        log.info("Processing chat message");
        return geminiService.generateContent(message);
    }
}
