"""
DynamoDB ユーザーリポジトリ。
Java版 DynamoDBUserRepository に対応。

テーブルキー構成:
  PK: UserId (String)
"""

import logging
from datetime import date, datetime

from boto3.dynamodb.conditions import Attr

from app.models.user import User
from app.repositories.base import DynamoDBBaseRepository

logger = logging.getLogger(__name__)


class UserRepository(DynamoDBBaseRepository):

    def save(self, user: User) -> User:
        item = self._to_item(user)
        self._put_item(item)
        return user

    def find_by_id(self, user_id: str) -> User | None:
        item = self._get_item({"UserId": user_id})
        return self._to_entity(item) if item else None

    def find_by_email(self, email: str) -> User | None:
        """メールアドレスで検索 (Scan - 本番ではGSI推奨)"""
        items = self._scan(filter_expression=Attr("Email").eq(email))
        return self._to_entity(items[0]) if items else None

    def delete(self, user_id: str) -> None:
        self._delete_item({"UserId": user_id})

    def exists_by_id(self, user_id: str) -> bool:
        return self._exists({"UserId": user_id})

    def find_all(self) -> list[User]:
        """全ユーザー取得 (管理者用)"""
        items = self._scan()
        return [self._to_entity(item) for item in items]

    # --- マッピング ---

    @staticmethod
    def _to_item(user: User) -> dict:
        item = {
            "UserId": user.user_id,
            "Email": user.email,
            "Nickname": user.nickname,
            "DisplayName": user.display_name,
            "ProfileImageUrl": user.profile_image_url,
            "PreferredLanguage": user.preferred_language,
            "Timezone": user.timezone,
            "MarketingOptOut": user.marketing_opt_out,
            "CreatedAt": user.created_at.isoformat(),
        }
        if user.last_cooking_date:
            item["LastCookingDate"] = user.last_cooking_date.isoformat()
        if user.last_login_date:
            item["LastLoginDate"] = user.last_login_date.isoformat()
        return item

    @staticmethod
    def _to_entity(item: dict) -> User:
        last_cooking = item.get("LastCookingDate")
        last_login = item.get("LastLoginDate")

        return User(
            user_id=item["UserId"],
            email=item.get("Email", ""),
            nickname=item.get("Nickname", ""),
            display_name=item.get("DisplayName", ""),
            profile_image_url=item.get("ProfileImageUrl", ""),
            preferred_language=item.get("PreferredLanguage", "JA"),
            last_cooking_date=date.fromisoformat(last_cooking) if last_cooking else None,
            last_login_date=datetime.fromisoformat(last_login) if last_login else None,
            created_at=datetime.fromisoformat(item.get("CreatedAt", datetime.now().isoformat())),
            timezone=item.get("Timezone", "Asia/Tokyo"),
            marketing_opt_out=item.get("MarketingOptOut", False),
        )
