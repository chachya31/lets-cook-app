"""
DynamoDB レシピリポジトリ。
Java版 DynamoDBRecipeRepository に対応。

テーブルキー構成:
  PK: RecipeId (String)
  GSI: GSI_Author - PK: AuthorId
"""

import logging
from datetime import datetime

from boto3.dynamodb.conditions import Attr, Key

from app.models.recipe import Ingredient, Recipe, Step
from app.repositories.base import DynamoDBBaseRepository

logger = logging.getLogger(__name__)


class RecipeRepository(DynamoDBBaseRepository):

    def save(self, recipe: Recipe) -> Recipe:
        item = self._to_item(recipe)
        self._put_item(item)
        return recipe

    def find_by_id(self, recipe_id: str) -> Recipe | None:
        item = self._get_item({"RecipeId": recipe_id})
        return self._to_entity(item) if item else None

    def find_by_author_id(self, author_id: str) -> list[Recipe]:
        """作成者IDでレシピを検索 (GSI_Author)"""
        items = self._query(
            key_condition=Key("AuthorId").eq(author_id),
            index_name="GSI_Author",
        )
        return [self._to_entity(item) for item in items]

    def find_all_public(self) -> list[Recipe]:
        """公開レシピを全件取得"""
        items = self._scan(
            filter_expression=Attr("IsPublic").eq(True) & Attr("IsDeleted").eq(False),
        )
        return [self._to_entity(item) for item in items]

    def delete(self, recipe_id: str) -> None:
        self._delete_item({"RecipeId": recipe_id})

    def exists_by_id(self, recipe_id: str) -> bool:
        return self._exists({"RecipeId": recipe_id})

    # --- マッピング ---

    @staticmethod
    def _to_item(recipe: Recipe) -> dict:
        ingredients_list = [
            {"Name": ing.name, "Quantity": ing.quantity, "Unit": ing.unit}
            for ing in recipe.ingredients
        ]
        steps_list = [
            {"Description": step.description, "ImageUrl": step.image_url, "VideoUrl": step.video_url}
            for step in recipe.steps
        ]

        return {
            "RecipeId": recipe.recipe_id,
            "Title": recipe.title,
            "AuthorId": recipe.author_id,
            "Ingredients": ingredients_list,
            "Steps": steps_list,
            "CookingTime": recipe.cooking_time,
            "ImageUrl": recipe.image_url,
            "IsPublic": recipe.is_public,
            "IsDeleted": recipe.is_deleted,
            "CreatedAt": recipe.created_at.isoformat(),
            "UpdatedAt": recipe.updated_at.isoformat(),
        }

    @staticmethod
    def _to_entity(item: dict) -> Recipe:
        ingredients = [
            Ingredient(
                name=ing.get("Name", ""),
                quantity=ing.get("Quantity", ""),
                unit=ing.get("Unit", ""),
            )
            for ing in item.get("Ingredients", [])
        ]
        steps = []
        for step_data in item.get("Steps", []):
            # 旧形式(文字列)と新形式(マップ)の両方に対応
            if isinstance(step_data, str):
                steps.append(Step(description=step_data))
            else:
                steps.append(Step(
                    description=step_data.get("Description", ""),
                    image_url=step_data.get("ImageUrl", ""),
                    video_url=step_data.get("VideoUrl", ""),
                ))

        return Recipe(
            recipe_id=item["RecipeId"],
            title=item.get("Title", ""),
            author_id=item.get("AuthorId", ""),
            ingredients=ingredients,
            steps=steps,
            cooking_time=int(item.get("CookingTime", 0)),
            image_url=item.get("ImageUrl", ""),
            is_public=item.get("IsPublic", True),
            is_deleted=item.get("IsDeleted", False),
            created_at=datetime.fromisoformat(item.get("CreatedAt", datetime.now().isoformat())),
            updated_at=datetime.fromisoformat(item.get("UpdatedAt", datetime.now().isoformat())),
        )
