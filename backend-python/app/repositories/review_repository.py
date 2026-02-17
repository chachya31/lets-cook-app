"""
DynamoDB レビューリポジトリ。
Java版 DynamoDBReviewRepository に対応。

テーブルキー構成:
  PK: RecipeId (String)
  SK: ReviewId (String)
  GSI: GSI_User - PK: UserId
"""

import logging
from datetime import datetime

from boto3.dynamodb.conditions import Attr, Key

from app.models.review import Review, ReviewStatus
from app.repositories.base import DynamoDBBaseRepository

logger = logging.getLogger(__name__)


class ReviewRepository(DynamoDBBaseRepository):

    def save(self, review: Review) -> Review:
        item = self._to_item(review)
        self._put_item(item)
        return review

    def find_by_id(self, review_id: str) -> Review | None:
        """レビューIDで検索 (Scan - RecipeIdが不明なため)"""
        items = self._scan(filter_expression=Attr("ReviewId").eq(review_id))
        return self._to_entity(items[0]) if items else None

    def find_by_recipe_id(self, recipe_id: str) -> list[Review]:
        items = self._query(key_condition=Key("RecipeId").eq(recipe_id))
        return [self._to_entity(item) for item in items]

    def find_by_user_id(self, user_id: str) -> list[Review]:
        """ユーザーIDで検索 (GSI_User)"""
        items = self._query(
            key_condition=Key("UserId").eq(user_id),
            index_name="GSI_User",
        )
        return [self._to_entity(item) for item in items]

    def delete(self, review_id: str) -> None:
        review = self.find_by_id(review_id)
        if review:
            self._delete_item({"RecipeId": review.recipe_id, "ReviewId": review.review_id})

    def exists_by_id(self, review_id: str) -> bool:
        return self.find_by_id(review_id) is not None

    @staticmethod
    def _to_item(review: Review) -> dict:
        item: dict = {
            "RecipeId": review.recipe_id,
            "ReviewId": review.review_id,
            "UserId": review.user_id,
            "Rating": review.rating,
            "Status": review.status.value,
            "ReportedCount": review.reported_count,
            "CreatedAt": review.created_at.isoformat(),
            "UpdatedAt": review.updated_at.isoformat(),
        }
        if review.comment:
            item["Comment"] = review.comment
        return item

    @staticmethod
    def _to_entity(item: dict) -> Review:
        return Review(
            review_id=item["ReviewId"],
            recipe_id=item["RecipeId"],
            user_id=item.get("UserId", ""),
            rating=int(item.get("Rating", 0)),
            comment=item.get("Comment", ""),
            status=ReviewStatus(item.get("Status", "VISIBLE")),
            reported_count=int(item.get("ReportedCount", 0)),
            created_at=datetime.fromisoformat(item.get("CreatedAt", datetime.now().isoformat())),
            updated_at=datetime.fromisoformat(item.get("UpdatedAt", datetime.now().isoformat())),
        )
