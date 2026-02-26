package com.cookingapp.unit.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cookingapp.domain.entity.ChatConversation;

/**
 * ChatConversationエンティティのユニットテスト
 */
@DisplayName("ChatConversation ユニットテスト")
class ChatConversationTest {

    private static final String TEST_USER_ID = "user-123";

    @Nested
    @DisplayName("create - 新しい会話を作成")
    class Create {

        @Test
        @DisplayName("新しい会話が正常に作成される")
        void shouldCreateNewConversation() {
            // Arrange
            String title = "テスト会話";
            String conversationType = "general";

            // Act
            ChatConversation conversation = ChatConversation.create(TEST_USER_ID, title, conversationType);

            // Assert
            assertThat(conversation).isNotNull();
            assertThat(conversation.getConversationId()).isNotNull().isNotEmpty();
            assertThat(conversation.getUserId()).isEqualTo(TEST_USER_ID);
            assertThat(conversation.getTitle()).isEqualTo(title);
            assertThat(conversation.getConversationType()).isEqualTo(conversationType);
            assertThat(conversation.getCreatedAt()).isNotNull();
            assertThat(conversation.getUpdatedAt()).isNotNull();
            assertThat(conversation.getCreatedAt()).isEqualTo(conversation.getUpdatedAt());
        }

        @Test
        @DisplayName("conversationTypeがnullの場合、generalがデフォルトで設定される")
        void shouldUseGeneralAsDefaultConversationType() {
            // Act
            ChatConversation conversation = ChatConversation.create(TEST_USER_ID, "テスト", null);

            // Assert
            assertThat(conversation.getConversationType()).isEqualTo("general");
        }

        @Test
        @DisplayName("UUIDが毎回異なる値で生成される")
        void shouldGenerateUniqueConversationId() {
            // Act
            ChatConversation conversation1 = ChatConversation.create(TEST_USER_ID, "テスト1", "general");
            ChatConversation conversation2 = ChatConversation.create(TEST_USER_ID, "テスト2", "general");

            // Assert
            assertThat(conversation1.getConversationId()).isNotEqualTo(conversation2.getConversationId());
        }

        @Test
        @DisplayName("recipe_recommendationタイプで会話を作成できる")
        void shouldCreateWithRecipeRecommendationType() {
            // Act
            ChatConversation conversation = ChatConversation.create(
                    TEST_USER_ID, "レシピ相談", "recipe_recommendation");

            // Assert
            assertThat(conversation.getConversationType()).isEqualTo("recipe_recommendation");
        }
    }

    @Nested
    @DisplayName("updateTitle - タイトルを更新")
    class UpdateTitle {

        @Test
        @DisplayName("タイトルが正常に更新される")
        void shouldUpdateTitle() {
            // Arrange
            ChatConversation conversation = ChatConversation.create(TEST_USER_ID, "元のタイトル", "general");
            Instant originalUpdatedAt = conversation.getUpdatedAt();

            // 少し待機して時間差を確保
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // Act
            conversation.updateTitle("新しいタイトル");

            // Assert
            assertThat(conversation.getTitle()).isEqualTo("新しいタイトル");
            assertThat(conversation.getUpdatedAt()).isAfterOrEqualTo(originalUpdatedAt);
        }
    }

    @Nested
    @DisplayName("touch - 更新日時を更新")
    class Touch {

        @Test
        @DisplayName("更新日時が更新される")
        void shouldUpdateUpdatedAt() {
            // Arrange
            ChatConversation conversation = ChatConversation.create(TEST_USER_ID, "テスト", "general");
            Instant originalUpdatedAt = conversation.getUpdatedAt();

            // 少し待機して時間差を確保
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // Act
            conversation.touch();

            // Assert
            assertThat(conversation.getUpdatedAt()).isAfterOrEqualTo(originalUpdatedAt);
        }

        @Test
        @DisplayName("createdAtは変更されない")
        void shouldNotChangeCreatedAt() {
            // Arrange
            ChatConversation conversation = ChatConversation.create(TEST_USER_ID, "テスト", "general");
            Instant originalCreatedAt = conversation.getCreatedAt();

            // Act
            conversation.touch();

            // Assert
            assertThat(conversation.getCreatedAt()).isEqualTo(originalCreatedAt);
        }
    }

    @Nested
    @DisplayName("canEdit - 編集権限チェック")
    class CanEdit {

        @Test
        @DisplayName("所有者はtrueを返す")
        void shouldReturnTrueForOwner() {
            // Arrange
            ChatConversation conversation = ChatConversation.create(TEST_USER_ID, "テスト", "general");

            // Act & Assert
            assertThat(conversation.canEdit(TEST_USER_ID)).isTrue();
        }

        @Test
        @DisplayName("所有者以外はfalseを返す")
        void shouldReturnFalseForNonOwner() {
            // Arrange
            ChatConversation conversation = ChatConversation.create(TEST_USER_ID, "テスト", "general");

            // Act & Assert
            assertThat(conversation.canEdit("other-user")).isFalse();
        }

        @Test
        @DisplayName("nullユーザーはfalseを返す")
        void shouldReturnFalseForNullUser() {
            // Arrange
            ChatConversation conversation = ChatConversation.create(TEST_USER_ID, "テスト", "general");

            // Act & Assert
            assertThat(conversation.canEdit(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("Builder - ビルダーパターン")
    class Builder {

        @Test
        @DisplayName("ビルダーで全フィールドを設定できる")
        void shouldBuildWithAllFields() {
            // Arrange
            Instant now = Instant.now();

            // Act
            ChatConversation conversation = ChatConversation.builder()
                    .conversationId("conv-123")
                    .userId(TEST_USER_ID)
                    .title("テスト会話")
                    .conversationType("recipe_recommendation")
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            // Assert
            assertThat(conversation.getConversationId()).isEqualTo("conv-123");
            assertThat(conversation.getUserId()).isEqualTo(TEST_USER_ID);
            assertThat(conversation.getTitle()).isEqualTo("テスト会話");
            assertThat(conversation.getConversationType()).isEqualTo("recipe_recommendation");
            assertThat(conversation.getCreatedAt()).isEqualTo(now);
            assertThat(conversation.getUpdatedAt()).isEqualTo(now);
        }
    }
}
