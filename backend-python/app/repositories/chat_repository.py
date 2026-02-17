"""
DynamoDB チャットリポジトリ。
Java版 DynamoDBChatConversationRepository / DynamoDBChatMessageRepository に対応。

ChatConversations テーブル:
  PK: UserId (String)
  SK: ConversationId (String)

ChatMessages テーブル:
  PK: ConversationId (String)
  SK: MessageId (String, ULID形式)
"""

import logging
from datetime import datetime

from boto3.dynamodb.conditions import Key

from app.models.chat import ChatConversation, ChatMessage
from app.repositories.base import DynamoDBBaseRepository

logger = logging.getLogger(__name__)


class ChatConversationRepository(DynamoDBBaseRepository):

    def save(self, conversation: ChatConversation) -> ChatConversation:
        item = self._to_item(conversation)
        self._put_item(item)
        return conversation

    def find_by_id(self, user_id: str, conversation_id: str) -> ChatConversation | None:
        item = self._get_item({"UserId": user_id, "ConversationId": conversation_id})
        return self._to_entity(item) if item else None

    def find_by_user_id(self, user_id: str) -> list[ChatConversation]:
        """ユーザーの会話一覧 (新しい順)"""
        items = self._query(
            key_condition=Key("UserId").eq(user_id),
            scan_forward=False,
        )
        return [self._to_entity(item) for item in items]

    def delete(self, user_id: str, conversation_id: str) -> None:
        self._delete_item({"UserId": user_id, "ConversationId": conversation_id})

    @staticmethod
    def _to_item(conv: ChatConversation) -> dict:
        return {
            "UserId": conv.user_id,
            "ConversationId": conv.conversation_id,
            "Title": conv.title,
            "ConversationType": conv.conversation_type,
            "CreatedAt": conv.created_at.isoformat(),
            "UpdatedAt": conv.updated_at.isoformat(),
        }

    @staticmethod
    def _to_entity(item: dict) -> ChatConversation:
        return ChatConversation(
            conversation_id=item["ConversationId"],
            user_id=item["UserId"],
            title=item.get("Title", ""),
            conversation_type=item.get("ConversationType", "general"),
            created_at=datetime.fromisoformat(item.get("CreatedAt", datetime.now().isoformat())),
            updated_at=datetime.fromisoformat(item.get("UpdatedAt", datetime.now().isoformat())),
        )


class ChatMessageRepository(DynamoDBBaseRepository):

    def save(self, message: ChatMessage) -> ChatMessage:
        item = self._to_item(message)
        self._put_item(item)
        return message

    def find_by_conversation_id(self, conversation_id: str) -> list[ChatMessage]:
        """会話のメッセージ一覧 (古い順 = 会話フロー順)"""
        items = self._query(
            key_condition=Key("ConversationId").eq(conversation_id),
            scan_forward=True,
        )
        return [self._to_entity(item) for item in items]

    def delete_by_conversation_id(self, conversation_id: str) -> None:
        """会話のメッセージを全件削除"""
        items = self._query(key_condition=Key("ConversationId").eq(conversation_id))
        for item in items:
            self._delete_item({"ConversationId": conversation_id, "MessageId": item["MessageId"]})

    @staticmethod
    def _to_item(msg: ChatMessage) -> dict:
        item: dict = {
            "ConversationId": msg.conversation_id,
            "MessageId": msg.message_id,
            "Role": msg.role,
            "Content": msg.content,
            "CreatedAt": msg.created_at.isoformat(),
        }
        if msg.generated_recipe:
            item["GeneratedRecipe"] = msg.generated_recipe
        return item

    @staticmethod
    def _to_entity(item: dict) -> ChatMessage:
        return ChatMessage(
            message_id=item["MessageId"],
            conversation_id=item["ConversationId"],
            role=item.get("Role", ""),
            content=item.get("Content", ""),
            generated_recipe=item.get("GeneratedRecipe", ""),
            created_at=datetime.fromisoformat(item.get("CreatedAt", datetime.now().isoformat())),
        )
