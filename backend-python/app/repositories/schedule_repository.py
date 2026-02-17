"""
DynamoDB スケジュールリポジトリ。
Java版 DynamoDBScheduleRepository に対応。

テーブルキー構成:
  PK: UserId (String)
  SK: DateRecipeId (String, "YYYY-MM-DD#RecipeId" 形式)
"""

import logging
from datetime import date, datetime, timedelta

from boto3.dynamodb.conditions import Attr, Key

from app.models.schedule import Schedule
from app.repositories.base import DynamoDBBaseRepository

logger = logging.getLogger(__name__)


class ScheduleRepository(DynamoDBBaseRepository):

    def save(self, schedule: Schedule) -> Schedule:
        item = self._to_item(schedule)
        self._put_item(item)
        return schedule

    def find_by_id(self, schedule_id: str) -> Schedule | None:
        """スケジュールIDで検索 (Scan)"""
        items = self._scan(filter_expression=Attr("ScheduleId").eq(schedule_id))
        return self._to_entity(items[0]) if items else None

    def find_by_user_id_and_date_range(
        self, user_id: str, start_date: date, end_date: date
    ) -> list[Schedule]:
        """ユーザーIDと日付範囲でスケジュールを検索"""
        start_key = start_date.isoformat()
        end_key = (end_date + timedelta(days=1)).isoformat()

        items = self._query(
            key_condition=Key("UserId").eq(user_id) & Key("DateRecipeId").between(start_key, end_key),
        )
        return [self._to_entity(item) for item in items]

    def delete(self, schedule_id: str) -> None:
        schedule = self.find_by_id(schedule_id)
        if schedule:
            sort_key = self._build_sort_key(schedule.date, schedule.recipe_id)
            self._delete_item({"UserId": schedule.user_id, "DateRecipeId": sort_key})

    def exists_by_id(self, schedule_id: str) -> bool:
        return self.find_by_id(schedule_id) is not None

    @staticmethod
    def _build_sort_key(d: date, recipe_id: str) -> str:
        return f"{d.isoformat()}#{recipe_id}"

    @staticmethod
    def _to_item(schedule: Schedule) -> dict:
        sort_key = f"{schedule.date.isoformat()}#{schedule.recipe_id}"
        item: dict = {
            "UserId": schedule.user_id,
            "DateRecipeId": sort_key,
            "ScheduleId": schedule.schedule_id,
            "Date": schedule.date.isoformat(),
            "RecipeId": schedule.recipe_id,
            "RecipeTitle": schedule.recipe_title,
            "IsDone": schedule.is_done,
            "CreatedAt": schedule.created_at.isoformat(),
        }
        if schedule.memo:
            item["Memo"] = schedule.memo
        return item

    @staticmethod
    def _to_entity(item: dict) -> Schedule:
        return Schedule(
            schedule_id=item["ScheduleId"],
            user_id=item["UserId"],
            date=date.fromisoformat(item["Date"]),
            recipe_id=item["RecipeId"],
            recipe_title=item.get("RecipeTitle", ""),
            is_done=item.get("IsDone", False),
            memo=item.get("Memo", ""),
            created_at=datetime.fromisoformat(item.get("CreatedAt", datetime.now().isoformat())),
        )
