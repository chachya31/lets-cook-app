package com.cookingapp.domain.entity;

import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * AIチャット会話エンティティ
 */
@Getter
@Builder
@AllArgsConstructor
public class ChatConversation {
    private final String conversationId;
    private final String userId;
    private String title;
    private final String conversationType;
    private final Instant createdAt;
    private Instant updatedAt;

    /**
     * 新しい会話を作成
     */
    public static ChatConversation create(String userId, String title, String conversationType) {
        Instant now = Instant.now();
        return ChatConversation.builder()
                .conversationId(UUID.randomUUID().toString())
                .userId(userId)
                .title(title)
                .conversationType(conversationType != null ? conversationType : "general")
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    /**
     * タイトルを更新
     */
    public void updateTitle(String title) {
        this.title = title;
        this.updatedAt = Instant.now();
    }

    /**
     * 更新日時を更新
     */
    public void touch() {
        this.updatedAt = Instant.now();
    }

    /**
     * 編集権限チェック
     */
    public boolean canEdit(String requestUserId) {
        return this.userId.equals(requestUserId);
    }
}
