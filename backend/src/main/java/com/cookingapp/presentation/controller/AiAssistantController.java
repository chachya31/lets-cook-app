package com.cookingapp.presentation.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cookingapp.application.usecase.ai.ChatUseCase;

/**
 * AIアシスタントコントローラー
 * AI機能のエンドポイントを提供
 */
@RestController
@RequestMapping("/api/ai")
public class AiAssistantController {

    private static final Logger log = LoggerFactory.getLogger(AiAssistantController.class);

    private final ChatUseCase chatUseCase;

    public AiAssistantController(ChatUseCase chatUseCase) {
        this.chatUseCase = chatUseCase;
    }

    @PostMapping("/chat")
    public ChatResponse chat(@RequestBody ChatRequest request) {
        log.info("AI chat request received");
        String response = chatUseCase.chat(request.message());
        return new ChatResponse(response);
    }

    public record ChatRequest(String message) {
    }

    public record ChatResponse(String response) {
    }
}
