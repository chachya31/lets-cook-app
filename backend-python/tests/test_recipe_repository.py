"""RecipeRepository のマッピングテスト (_to_item / _to_entity)"""

from datetime import datetime

import pytest

from app.models.recipe import Ingredient, Recipe, Step
from app.repositories.recipe_repository import RecipeRepository


class TestToItem:
    """Recipe -> DynamoDB Item 変換"""

    def test_maps_all_fields(self):
        recipe = Recipe(
            recipe_id="r-1",
            title="カレー",
            author_id="u-1",
            ingredients=[Ingredient(name="にんじん", quantity="1", unit="本")],
            steps=[Step(description="切る", image_url="https://img.png", video_url="")],
            cooking_time=30,
            image_url="https://main.png",
            is_public=True,
            is_deleted=False,
            created_at=datetime(2025, 1, 1, 12, 0, 0),
            updated_at=datetime(2025, 1, 2, 12, 0, 0),
        )

        item = RecipeRepository._to_item(recipe)

        assert item["RecipeId"] == "r-1"
        assert item["Title"] == "カレー"
        assert item["AuthorId"] == "u-1"
        assert item["CookingTime"] == 30
        assert item["ImageUrl"] == "https://main.png"
        assert item["IsPublic"] is True
        assert item["IsDeleted"] is False
        assert item["CreatedAt"] == "2025-01-01T12:00:00"
        assert item["UpdatedAt"] == "2025-01-02T12:00:00"

    def test_maps_ingredients(self):
        recipe = Recipe(
            recipe_id="r-1",
            title="t",
            author_id="u-1",
            ingredients=[
                Ingredient(name="塩", quantity="1", unit="tsp"),
                Ingredient(name="水", quantity="500", unit="ml"),
            ],
            steps=[Step(description="s")],
        )

        item = RecipeRepository._to_item(recipe)

        assert len(item["Ingredients"]) == 2
        assert item["Ingredients"][0] == {"Name": "塩", "Quantity": "1", "Unit": "tsp"}
        assert item["Ingredients"][1] == {"Name": "水", "Quantity": "500", "Unit": "ml"}

    def test_maps_steps_with_images(self):
        recipe = Recipe(
            recipe_id="r-1",
            title="t",
            author_id="u-1",
            ingredients=[Ingredient(name="a", quantity="1", unit="")],
            steps=[
                Step(description="手順1", image_url="https://step1.png", video_url="https://v1.mp4"),
                Step(description="手順2"),
            ],
        )

        item = RecipeRepository._to_item(recipe)

        assert len(item["Steps"]) == 2
        assert item["Steps"][0] == {
            "Description": "手順1",
            "ImageUrl": "https://step1.png",
            "VideoUrl": "https://v1.mp4",
        }
        assert item["Steps"][1]["ImageUrl"] == ""


class TestToEntity:
    """DynamoDB Item -> Recipe 変換"""

    def test_maps_all_fields(self):
        item = {
            "RecipeId": "r-1",
            "Title": "味噌汁",
            "AuthorId": "u-2",
            "Ingredients": [{"Name": "豆腐", "Quantity": "1", "Unit": "丁"}],
            "Steps": [{"Description": "煮る", "ImageUrl": "", "VideoUrl": ""}],
            "CookingTime": 15,
            "ImageUrl": "https://img.png",
            "IsPublic": True,
            "IsDeleted": False,
            "CreatedAt": "2025-06-01T10:00:00",
            "UpdatedAt": "2025-06-02T10:00:00",
        }

        recipe = RecipeRepository._to_entity(item)

        assert recipe.recipe_id == "r-1"
        assert recipe.title == "味噌汁"
        assert recipe.author_id == "u-2"
        assert recipe.cooking_time == 15
        assert recipe.image_url == "https://img.png"
        assert recipe.is_public is True
        assert recipe.is_deleted is False
        assert recipe.created_at == datetime(2025, 6, 1, 10, 0, 0)
        assert recipe.updated_at == datetime(2025, 6, 2, 10, 0, 0)

    def test_maps_ingredients(self):
        item = {
            "RecipeId": "r-1",
            "Ingredients": [
                {"Name": "玉ねぎ", "Quantity": "2", "Unit": "個"},
                {"Name": "じゃがいも", "Quantity": "3", "Unit": "個"},
            ],
            "Steps": [],
        }

        recipe = RecipeRepository._to_entity(item)

        assert len(recipe.ingredients) == 2
        assert recipe.ingredients[0].name == "玉ねぎ"
        assert recipe.ingredients[1].quantity == "3"

    def test_handles_legacy_string_steps(self):
        """旧形式の文字列ステップに対応"""
        item = {
            "RecipeId": "r-1",
            "Ingredients": [],
            "Steps": ["手順1のテキスト", "手順2のテキスト"],
        }

        recipe = RecipeRepository._to_entity(item)

        assert len(recipe.steps) == 2
        assert recipe.steps[0].description == "手順1のテキスト"
        assert recipe.steps[0].image_url == ""
        assert recipe.steps[1].description == "手順2のテキスト"

    def test_handles_map_format_steps(self):
        """新形式のマップステップに対応"""
        item = {
            "RecipeId": "r-1",
            "Ingredients": [],
            "Steps": [
                {"Description": "手順1", "ImageUrl": "https://s1.png", "VideoUrl": ""},
            ],
        }

        recipe = RecipeRepository._to_entity(item)

        assert recipe.steps[0].description == "手順1"
        assert recipe.steps[0].image_url == "https://s1.png"

    def test_defaults_for_missing_fields(self):
        """必須フィールド以外が欠落した場合のデフォルト値"""
        item = {"RecipeId": "r-1"}

        recipe = RecipeRepository._to_entity(item)

        assert recipe.recipe_id == "r-1"
        assert recipe.title == ""
        assert recipe.author_id == ""
        assert recipe.ingredients == []
        assert recipe.steps == []
        assert recipe.cooking_time == 0
        assert recipe.image_url == ""
        assert recipe.is_public is True
        assert recipe.is_deleted is False


class TestRoundTrip:
    """_to_item → _to_entity のラウンドトリップ"""

    def test_round_trip_preserves_data(self):
        original = Recipe(
            recipe_id="r-1",
            title="パスタ",
            author_id="u-1",
            ingredients=[
                Ingredient(name="パスタ", quantity="200", unit="g"),
                Ingredient(name="オリーブオイル", quantity="大さじ1", unit=""),
            ],
            steps=[
                Step(description="茹でる", image_url="https://s1.png", video_url=""),
                Step(description="和える"),
            ],
            cooking_time=20,
            image_url="https://main.png",
            is_public=True,
            is_deleted=False,
            created_at=datetime(2025, 3, 15, 10, 30, 0),
            updated_at=datetime(2025, 3, 16, 11, 0, 0),
        )

        item = RecipeRepository._to_item(original)
        restored = RecipeRepository._to_entity(item)

        assert restored.recipe_id == original.recipe_id
        assert restored.title == original.title
        assert restored.author_id == original.author_id
        assert restored.cooking_time == original.cooking_time
        assert restored.image_url == original.image_url
        assert restored.is_public == original.is_public
        assert restored.is_deleted == original.is_deleted
        assert restored.created_at == original.created_at
        assert restored.updated_at == original.updated_at
        assert len(restored.ingredients) == len(original.ingredients)
        assert restored.ingredients[0].name == original.ingredients[0].name
        assert len(restored.steps) == len(original.steps)
        assert restored.steps[0].description == original.steps[0].description
