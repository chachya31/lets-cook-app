package com.cookingapp.domain.entity;

import java.time.Instant;

import com.github.f4b6a3.ulid.UlidCreator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * AIチャットメッセージエンティティ
 */
@Getter
@Builder
@AllArgsConstructor
public class ChatMessage {
    private final String messageId;
    private final String conversationId;
    private final String role;
    private final String content;
    private final String generatedRecipe;
    private final Instant createdAt;

    /**
     * 新しいメッセージを作成
     */
    public static ChatMessage create(String conversationId, String role, String content, String generatedRecipe) {
        return ChatMessage.builder()
                .messageId(UlidCreator.getUlid().toString())
                .conversationId(conversationId)
                .role(role)
                .content(content)
                .generatedRecipe(generatedRecipe)
                .createdAt(Instant.now())
                .build();
    }

    /**
     * ユーザーメッセージを作成
     */
    public static ChatMessage createUserMessage(String conversationId, String content) {
        return create(conversationId, "user", content, null);
    }

    /**
     * アシスタントメッセージを作成
     */
    public static ChatMessage createAssistantMessage(String conversationId, String content, String generatedRecipe) {
        return create(conversationId, "assistant", content, generatedRecipe);
    }
}
