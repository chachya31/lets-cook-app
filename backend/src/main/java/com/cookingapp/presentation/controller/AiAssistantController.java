package com.cookingapp.presentation.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cookingapp.application.usecase.ai.ChatUseCase;
import com.cookingapp.domain.entity.ChatConversation;
import com.cookingapp.domain.entity.ChatMessage;
import com.cookingapp.infrastructure.security.SecurityUtils;

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

    /**
     * 新しい会話を開始してチャット
     */
    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        String userId = SecurityUtils.getCurrentUserId();
        log.info("POST /api/ai/chat - userId={}, conversationType={}", userId, request.conversationType());
        String conversationType = request.conversationType() != null ? request.conversationType() : "general";
        ChatUseCase.ChatResponse result = chatUseCase.chat(userId, request.message(), conversationType);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ChatResponse(result.conversationId(), result.response()));
    }

    /**
     * 会話一覧を取得
     */
    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationResponse>> getConversations() {
        String userId = SecurityUtils.getCurrentUserId();
        log.info("GET /api/ai/conversations - userId={}", userId);
        List<ChatConversation> conversations = chatUseCase.getConversations(userId);
        List<ConversationResponse> response = conversations.stream()
                .map(ConversationResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    /**
     * 会話のメッセージ一覧を取得
     */
    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<List<MessageResponse>> getMessages(@PathVariable String conversationId) {
        String userId = SecurityUtils.getCurrentUserId();
        log.info("GET /api/ai/conversations/{}/messages - userId={}", conversationId, userId);
        List<ChatMessage> messages = chatUseCase.getMessages(userId, conversationId);
        List<MessageResponse> response = messages.stream()
                .map(MessageResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    /**
     * 既存の会話にメッセージを送信
     */
    @PostMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<ChatResponse> sendMessage(
            @PathVariable String conversationId,
            @RequestBody ChatRequest request) {
        String userId = SecurityUtils.getCurrentUserId();
        log.info("POST /api/ai/conversations/{}/messages - userId={}", conversationId, userId);
        ChatUseCase.ChatResponse result = chatUseCase.chatInConversation(userId, conversationId, request.message());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ChatResponse(result.conversationId(), result.response()));
    }

    /**
     * 会話を削除
     */
    @DeleteMapping("/conversations/{conversationId}")
    public ResponseEntity<Void> deleteConversation(@PathVariable String conversationId) {
        String userId = SecurityUtils.getCurrentUserId();
        log.info("DELETE /api/ai/conversations/{} - userId={}", conversationId, userId);
        chatUseCase.deleteConversation(userId, conversationId);
        return ResponseEntity.noContent().build();
    }

    // Request/Response DTOs
    public record ChatRequest(String message, String conversationType) {
    }

    public record ChatResponse(String conversationId, String response) {
    }

    public record ConversationResponse(
            String conversationId,
            String title,
            String conversationType,
            String createdAt,
            String updatedAt) {

        public static ConversationResponse from(ChatConversation conversation) {
            return new ConversationResponse(
                    conversation.getConversationId(),
                    conversation.getTitle(),
                    conversation.getConversationType(),
                    conversation.getCreatedAt().toString(),
                    conversation.getUpdatedAt().toString());
        }
    }

    public record MessageResponse(
            String messageId,
            String role,
            String content,
            String createdAt) {

        public static MessageResponse from(ChatMessage message) {
            return new MessageResponse(
                    message.getMessageId(),
                    message.getRole(),
                    message.getContent(),
                    message.getCreatedAt().toString());
        }
    }
}
