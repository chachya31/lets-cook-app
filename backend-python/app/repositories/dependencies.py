"""
リポジトリの依存注入 (FastAPI Depends)。
DynamoDB テーブルリソースを生成し、各リポジトリに注入する。
"""

from functools import lru_cache
from typing import Annotated

from fastapi import Depends

from app.config.aws import create_dynamodb_resource
from app.config.settings import Settings, get_settings
from app.repositories.chat_repository import ChatConversationRepository, ChatMessageRepository
from app.repositories.inventory_repository import InventoryRepository
from app.repositories.recipe_repository import RecipeRepository
from app.repositories.review_repository import ReviewRepository
from app.repositories.schedule_repository import ScheduleRepository
from app.repositories.shopping_list_repository import ShoppingListRepository
from app.repositories.user_repository import UserRepository


@lru_cache
def _get_dynamodb_resource():
    return create_dynamodb_resource()


def _get_table(table_name: str):
    return _get_dynamodb_resource().Table(table_name)


# --- リポジトリ ファクトリ ---


def get_user_repository(settings: Annotated[Settings, Depends(get_settings)]) -> UserRepository:
    table = _get_table(settings.dynamodb_users_table)
    return UserRepository(table, settings.dynamodb_users_table)


def get_recipe_repository(settings: Annotated[Settings, Depends(get_settings)]) -> RecipeRepository:
    table = _get_table(settings.dynamodb_recipes_table)
    return RecipeRepository(table, settings.dynamodb_recipes_table)


def get_schedule_repository(settings: Annotated[Settings, Depends(get_settings)]) -> ScheduleRepository:
    table = _get_table(settings.dynamodb_schedules_table)
    return ScheduleRepository(table, settings.dynamodb_schedules_table)


def get_shopping_list_repository(settings: Annotated[Settings, Depends(get_settings)]) -> ShoppingListRepository:
    table = _get_table(settings.dynamodb_shopping_lists_table)
    return ShoppingListRepository(table, settings.dynamodb_shopping_lists_table)


def get_review_repository(settings: Annotated[Settings, Depends(get_settings)]) -> ReviewRepository:
    table = _get_table(settings.dynamodb_reviews_table)
    return ReviewRepository(table, settings.dynamodb_reviews_table)


def get_chat_conversation_repository(
    settings: Annotated[Settings, Depends(get_settings)],
) -> ChatConversationRepository:
    table = _get_table(settings.dynamodb_chat_conversations_table)
    return ChatConversationRepository(table, settings.dynamodb_chat_conversations_table)


def get_chat_message_repository(settings: Annotated[Settings, Depends(get_settings)]) -> ChatMessageRepository:
    table = _get_table(settings.dynamodb_chat_messages_table)
    return ChatMessageRepository(table, settings.dynamodb_chat_messages_table)


def get_inventory_repository(settings: Annotated[Settings, Depends(get_settings)]) -> InventoryRepository:
    table = _get_table(settings.dynamodb_inventory_table)
    return InventoryRepository(table, settings.dynamodb_inventory_table)
