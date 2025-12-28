package com.cookingapp.application.usecase.ai;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.cookingapp.domain.entity.ChatConversation;
import com.cookingapp.domain.entity.ChatMessage;
import com.cookingapp.domain.repository.ChatConversationRepository;
import com.cookingapp.domain.repository.ChatMessageRepository;
import com.cookingapp.infrastructure.external.gemini.GeminiService;

/**
 * AIチャットユースケース
 */
@Service
public class ChatUseCase {

    private static final Logger log = LoggerFactory.getLogger(ChatUseCase.class);

    private final GeminiService geminiService;
    private final ChatConversationRepository conversationRepository;
    private final ChatMessageRepository messageRepository;

    public ChatUseCase(
            GeminiService geminiService,
            ChatConversationRepository conversationRepository,
            ChatMessageRepository messageRepository) {
        this.geminiService = geminiService;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
    }

    /**
     * 新しい会話を開始してチャット
     */
    public ChatResponse chat(String userId, String message) {
        log.info("Starting new conversation for userId={}", userId);

        String title = generateTitle(message);
        ChatConversation conversation = ChatConversation.create(userId, title, "general");
        conversationRepository.save(conversation);

        return processChat(conversation, message);
    }

    /**
     * 既存の会話でチャット
     */
    public ChatResponse chatInConversation(String userId, String conversationId, String message) {
        log.info("Continuing conversation: conversationId={}", conversationId);

        ChatConversation conversation = conversationRepository.findById(userId, conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));

        if (!conversation.canEdit(userId)) {
            throw new IllegalArgumentException("Access denied");
        }

        return processChat(conversation, message);
    }

    /**
     * ユーザーの会話一覧を取得
     */
    public List<ChatConversation> getConversations(String userId) {
        return conversationRepository.findByUserId(userId);
    }

    /**
     * 会話のメッセージ一覧を取得
     */
    public List<ChatMessage> getMessages(String userId, String conversationId) {
        ChatConversation conversation = conversationRepository.findById(userId, conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));

        if (!conversation.canEdit(userId)) {
            throw new IllegalArgumentException("Access denied");
        }

        return messageRepository.findByConversationId(conversationId);
    }

    /**
     * 会話を削除
     */
    public void deleteConversation(String userId, String conversationId) {
        ChatConversation conversation = conversationRepository.findById(userId, conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));

        if (!conversation.canEdit(userId)) {
            throw new IllegalArgumentException("Access denied");
        }

        messageRepository.deleteByConversationId(conversationId);
        conversationRepository.delete(userId, conversationId);
        log.info("Conversation deleted: conversationId={}", conversationId);
    }

    private ChatResponse processChat(ChatConversation conversation, String message) {
        ChatMessage userMessage = ChatMessage.createUserMessage(conversation.getConversationId(), message);
        messageRepository.save(userMessage);

        String response = geminiService.generateContent(message);

        ChatMessage assistantMessage = ChatMessage.createAssistantMessage(
                conversation.getConversationId(), response, null);
        messageRepository.save(assistantMessage);

        conversation.touch();
        conversationRepository.save(conversation);

        return new ChatResponse(conversation.getConversationId(), response);
    }

    private String generateTitle(String message) {
        if (message.length() <= 30) {
            return message;
        }
        return message.substring(0, 30) + "...";
    }

    public record ChatResponse(String conversationId, String response) {
    }
}
