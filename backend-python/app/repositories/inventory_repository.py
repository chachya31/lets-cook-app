"""
DynamoDB 在庫リポジトリ。
Java版 DynamoDBInventoryRepository に対応。

テーブルキー構成:
  PK: UserId (String)
  SK: ItemId (String)
"""

import logging
from datetime import date, datetime
from decimal import Decimal

from boto3.dynamodb.conditions import Key

from app.models.inventory import InventoryItem
from app.repositories.base import DynamoDBBaseRepository

logger = logging.getLogger(__name__)


class InventoryRepository(DynamoDBBaseRepository):

    def save(self, item: InventoryItem) -> InventoryItem:
        db_item = self._to_item(item)
        self._put_item(db_item)
        return item

    def find_by_user_id(self, user_id: str) -> list[InventoryItem]:
        items = self._query(key_condition=Key("UserId").eq(user_id))
        return [self._to_entity(item) for item in items]

    def find_by_user_id_order_by_expiry_date(self, user_id: str) -> list[InventoryItem]:
        """賞味期限順で取得 (アプリケーション側ソート)"""
        items = self.find_by_user_id(user_id)
        return sorted(items, key=lambda x: x.expiry_date or date.max)

    def find_by_id(self, user_id: str, item_id: str) -> InventoryItem | None:
        item = self._get_item({"UserId": user_id, "ItemId": item_id})
        return self._to_entity(item) if item else None

    def delete(self, user_id: str, item_id: str) -> None:
        self._delete_item({"UserId": user_id, "ItemId": item_id})

    def find_by_user_id_and_name_and_unit(
        self, user_id: str, name: str, unit: str
    ) -> InventoryItem | None:
        """同名・同単位の在庫を検索 (アプリケーション側フィルター)"""
        items = self.find_by_user_id(user_id)
        for item in items:
            if item.name == name and item.unit == unit:
                return item
        return None

    @staticmethod
    def _to_item(item: InventoryItem) -> dict:
        db_item: dict = {
            "UserId": item.user_id,
            "ItemId": item.item_id,
            "Name": item.name,
            "Quantity": item.quantity,
            "Unit": item.unit,
            "PurchasedAt": item.purchased_at.isoformat(),
            "CreatedAt": item.created_at.isoformat(),
        }
        if item.expiry_date:
            db_item["ExpiryDate"] = item.expiry_date.isoformat()
        return db_item

    @staticmethod
    def _to_entity(item: dict) -> InventoryItem:
        expiry = item.get("ExpiryDate")
        return InventoryItem(
            item_id=item["ItemId"],
            user_id=item["UserId"],
            name=item.get("Name", ""),
            quantity=Decimal(str(item.get("Quantity", 0))),
            unit=item.get("Unit", ""),
            expiry_date=date.fromisoformat(expiry) if expiry else None,
            purchased_at=datetime.fromisoformat(item.get("PurchasedAt", datetime.now().isoformat())),
            created_at=datetime.fromisoformat(item.get("CreatedAt", datetime.now().isoformat())),
        )
