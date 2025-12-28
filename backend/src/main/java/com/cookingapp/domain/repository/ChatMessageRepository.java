package com.cookingapp.domain.repository;

import java.util.List;

import com.cookingapp.domain.entity.ChatMessage;

/**
 * チャットメッセージリポジトリインターフェース
 */
public interface ChatMessageRepository {

    ChatMessage save(ChatMessage message);

    List<ChatMessage> findByConversationId(String conversationId);

    void deleteByConversationId(String conversationId);
}
