package com.cookingapp.domain.repository;

import java.util.List;
import java.util.Optional;

import com.cookingapp.domain.entity.ChatConversation;

/**
 * チャット会話リポジトリインターフェース
 */
public interface ChatConversationRepository {

    ChatConversation save(ChatConversation conversation);

    Optional<ChatConversation> findById(String userId, String conversationId);

    List<ChatConversation> findByUserId(String userId);

    void delete(String userId, String conversationId);
}
