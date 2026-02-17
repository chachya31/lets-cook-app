"""
DynamoDB リポジトリ基底クラス。
全リポジトリ共通の CRUD 操作パターンを提供する。
"""

import logging
from typing import Any

from boto3.dynamodb.conditions import Key

logger = logging.getLogger(__name__)


class DynamoDBBaseRepository:
    """DynamoDB テーブル操作の基底クラス"""

    def __init__(self, table, table_name: str):
        self._table = table
        self._table_name = table_name

    # --- 共通操作 ---

    def _put_item(self, item: dict[str, Any]) -> None:
        """アイテムを保存 (PutItem)"""
        self._table.put_item(Item=item)
        logger.debug("PutItem [%s]: %s", self._table_name, item)

    def _get_item(self, key: dict[str, Any]) -> dict[str, Any] | None:
        """主キーでアイテムを取得 (GetItem)"""
        response = self._table.get_item(Key=key)
        item = response.get("Item")
        if item:
            logger.debug("GetItem [%s]: found", self._table_name)
        else:
            logger.debug("GetItem [%s]: not found (key=%s)", self._table_name, key)
        return item

    def _delete_item(self, key: dict[str, Any]) -> None:
        """主キーでアイテムを削除 (DeleteItem)"""
        self._table.delete_item(Key=key)
        logger.debug("DeleteItem [%s]: %s", self._table_name, key)

    def _query(
        self,
        key_condition,
        index_name: str | None = None,
        filter_expression=None,
        scan_forward: bool = True,
    ) -> list[dict[str, Any]]:
        """キー条件でクエリ (Query)"""
        kwargs: dict[str, Any] = {
            "KeyConditionExpression": key_condition,
            "ScanIndexForward": scan_forward,
        }
        if index_name:
            kwargs["IndexName"] = index_name
        if filter_expression:
            kwargs["FilterExpression"] = filter_expression

        response = self._table.query(**kwargs)
        items = response.get("Items", [])
        logger.debug("Query [%s]: %d items", self._table_name, len(items))
        return items

    def _scan(self, filter_expression=None) -> list[dict[str, Any]]:
        """テーブル全体をスキャン (Scan)"""
        kwargs: dict[str, Any] = {}
        if filter_expression:
            kwargs["FilterExpression"] = filter_expression

        items: list[dict[str, Any]] = []
        response = self._table.scan(**kwargs)
        items.extend(response.get("Items", []))

        # ページネーション対応
        while "LastEvaluatedKey" in response:
            kwargs["ExclusiveStartKey"] = response["LastEvaluatedKey"]
            response = self._table.scan(**kwargs)
            items.extend(response.get("Items", []))

        logger.debug("Scan [%s]: %d items", self._table_name, len(items))
        return items

    def _exists(self, key: dict[str, Any]) -> bool:
        """アイテムの存在確認"""
        return self._get_item(key) is not None
