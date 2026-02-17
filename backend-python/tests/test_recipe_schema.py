"""Pydantic スキーマのバリデーションテスト"""

import pytest
from pydantic import ValidationError

from app.schemas.recipe import (
    IngredientSchema,
    RecipeCreateRequest,
    RecipeUpdateRequest,
    SearchByIngredientsRequest,
    StepSchema,
)


# --- RecipeCreateRequest ---


class TestRecipeCreateRequest:
    def test_valid_request(self):
        req = RecipeCreateRequest(
            title="カレー",
            ingredients=[{"name": "にんじん", "quantity": "1", "unit": "本"}],
            steps=[{"description": "切る"}],
            cooking_time=30,
        )
        assert req.title == "カレー"
        assert len(req.ingredients) == 1
        assert len(req.steps) == 1
        assert req.cooking_time == 30

    def test_cooking_time_defaults_to_zero(self):
        req = RecipeCreateRequest(
            title="test",
            ingredients=[{"name": "a", "quantity": "1", "unit": ""}],
            steps=[{"description": "b"}],
        )
        assert req.cooking_time == 0

    def test_rejects_empty_title(self):
        with pytest.raises(ValidationError) as exc_info:
            RecipeCreateRequest(
                title="",
                ingredients=[{"name": "a", "quantity": "1", "unit": ""}],
                steps=[{"description": "b"}],
            )
        errors = exc_info.value.errors()
        assert any(e["loc"] == ("title",) for e in errors)

    def test_rejects_title_over_100_chars(self):
        with pytest.raises(ValidationError):
            RecipeCreateRequest(
                title="a" * 101,
                ingredients=[{"name": "a", "quantity": "1", "unit": ""}],
                steps=[{"description": "b"}],
            )

    def test_rejects_empty_ingredients(self):
        with pytest.raises(ValidationError) as exc_info:
            RecipeCreateRequest(
                title="test",
                ingredients=[],
                steps=[{"description": "b"}],
            )
        errors = exc_info.value.errors()
        assert any("ingredients" in str(e["loc"]) for e in errors)

    def test_rejects_empty_steps(self):
        with pytest.raises(ValidationError):
            RecipeCreateRequest(
                title="test",
                ingredients=[{"name": "a", "quantity": "1", "unit": ""}],
                steps=[],
            )

    def test_rejects_negative_cooking_time(self):
        with pytest.raises(ValidationError):
            RecipeCreateRequest(
                title="test",
                ingredients=[{"name": "a", "quantity": "1", "unit": ""}],
                steps=[{"description": "b"}],
                cooking_time=-1,
            )


# --- IngredientSchema ---


class TestIngredientSchema:
    def test_valid_ingredient(self):
        ing = IngredientSchema(name="にんじん", quantity="2", unit="本")
        assert ing.name == "にんじん"

    def test_unit_defaults_to_empty(self):
        ing = IngredientSchema(name="塩", quantity="少々")
        assert ing.unit == ""

    def test_rejects_name_over_100_chars(self):
        with pytest.raises(ValidationError):
            IngredientSchema(name="a" * 101, quantity="1")

    def test_rejects_unit_over_50_chars(self):
        with pytest.raises(ValidationError):
            IngredientSchema(name="a", quantity="1", unit="x" * 51)


# --- StepSchema ---


class TestStepSchema:
    def test_valid_step(self):
        step = StepSchema(description="切る")
        assert step.description == "切る"
        assert step.image_url == ""
        assert step.video_url == ""

    def test_with_urls(self):
        step = StepSchema(description="混ぜる", image_url="https://img.png", video_url="https://v.mp4")
        assert step.image_url == "https://img.png"
        assert step.video_url == "https://v.mp4"


# --- RecipeUpdateRequest ---


class TestRecipeUpdateRequest:
    def test_valid_update(self):
        req = RecipeUpdateRequest(
            title="更新後",
            ingredients=[{"name": "a", "quantity": "1", "unit": ""}],
            steps=[{"description": "b"}],
            cooking_time=10,
        )
        assert req.title == "更新後"

    def test_same_validations_as_create(self):
        """Create と同じバリデーションルールが適用される"""
        with pytest.raises(ValidationError):
            RecipeUpdateRequest(title="", ingredients=[], steps=[], cooking_time=-1)


# --- SearchByIngredientsRequest ---


class TestSearchByIngredientsRequest:
    def test_valid_request(self):
        req = SearchByIngredientsRequest(ingredients=["にんじん", "玉ねぎ"])
        assert len(req.ingredients) == 2

    def test_rejects_empty_ingredients(self):
        with pytest.raises(ValidationError):
            SearchByIngredientsRequest(ingredients=[])
