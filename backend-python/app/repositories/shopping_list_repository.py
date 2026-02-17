"""
DynamoDB 買い物リストリポジトリ。
Java版 DynamoDBShoppingListRepository に対応。

テーブルキー構成:
  PK: UserId (String)
  SK: ItemId (String)
  GSI: GSI_NormalizedKey - PK: UserId, SK: NormalizedKey
"""

import logging
from datetime import datetime
from decimal import Decimal

from boto3.dynamodb.conditions import Key

from app.models.shopping_list import ShoppingListItem
from app.repositories.base import DynamoDBBaseRepository

logger = logging.getLogger(__name__)


class ShoppingListRepository(DynamoDBBaseRepository):

    def save(self, item: ShoppingListItem) -> ShoppingListItem:
        db_item = self._to_item(item)
        self._put_item(db_item)
        return item

    def find_by_id(self, user_id: str, item_id: str) -> ShoppingListItem | None:
        item = self._get_item({"UserId": user_id, "ItemId": item_id})
        return self._to_entity(item) if item else None

    def find_by_user_id(self, user_id: str) -> list[ShoppingListItem]:
        items = self._query(key_condition=Key("UserId").eq(user_id))
        return [self._to_entity(item) for item in items]

    def find_by_normalized_key(self, user_id: str, normalized_key: str) -> ShoppingListItem | None:
        """正規化キーで検索 (GSI_NormalizedKey)"""
        items = self._query(
            key_condition=Key("UserId").eq(user_id) & Key("NormalizedKey").eq(normalized_key),
            index_name="GSI_NormalizedKey",
        )
        return self._to_entity(items[0]) if items else None

    def delete(self, user_id: str, item_id: str) -> None:
        self._delete_item({"UserId": user_id, "ItemId": item_id})

    def exists_by_id(self, user_id: str, item_id: str) -> bool:
        return self._exists({"UserId": user_id, "ItemId": item_id})

    @staticmethod
    def _to_item(item: ShoppingListItem) -> dict:
        db_item: dict = {
            "UserId": item.user_id,
            "ItemId": item.item_id,
            "Name": item.name,
            "Quantity": item.quantity,
            "Unit": item.unit,
            "IsChecked": item.is_checked,
            "AddedAt": item.added_at.isoformat(),
            "NormalizedKey": item.normalized_key,
        }
        if item.is_checked_at:
            db_item["IsCheckedAt"] = item.is_checked_at.isoformat()
        if item.source_recipe_id:
            db_item["SourceRecipeId"] = item.source_recipe_id
        return db_item

    @staticmethod
    def _to_entity(item: dict) -> ShoppingListItem:
        checked_at = item.get("IsCheckedAt")
        return ShoppingListItem(
            item_id=item["ItemId"],
            user_id=item["UserId"],
            name=item.get("Name", ""),
            quantity=Decimal(str(item.get("Quantity", 0))),
            unit=item.get("Unit", ""),
            is_checked=item.get("IsChecked", False),
            is_checked_at=datetime.fromisoformat(checked_at) if checked_at else None,
            added_at=datetime.fromisoformat(item.get("AddedAt", datetime.now().isoformat())),
            source_recipe_id=item.get("SourceRecipeId", ""),
            normalized_key=item.get("NormalizedKey", ""),
        )
