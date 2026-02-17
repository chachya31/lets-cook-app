"""RecipeService の単体テスト"""

from datetime import datetime
from unittest.mock import MagicMock

import pytest

from app.exceptions.exceptions import RecipeNotFoundException, UnauthorizedException
from app.models.recipe import Ingredient, Recipe, Step
from app.schemas.recipe import RecipeCreateRequest, RecipeUpdateRequest
from app.services.recipe_service import RecipeService


def _make_recipe(
    recipe_id: str = "recipe-1",
    title: str = "カレーライス",
    author_id: str = "user-1",
    is_public: bool = True,
    is_deleted: bool = False,
) -> Recipe:
    return Recipe(
        recipe_id=recipe_id,
        title=title,
        author_id=author_id,
        ingredients=[Ingredient(name="にんじん", quantity="1", unit="本")],
        steps=[Step(description="切る")],
        cooking_time=30,
        is_public=is_public,
        is_deleted=is_deleted,
        created_at=datetime(2025, 1, 1),
        updated_at=datetime(2025, 1, 1),
    )


@pytest.fixture
def mock_repo():
    return MagicMock()


@pytest.fixture
def service(mock_repo):
    return RecipeService(mock_repo)


# --- search_public ---


class TestSearchPublic:
    def test_returns_all_public_recipes(self, service, mock_repo):
        mock_repo.find_all_public.return_value = [_make_recipe(), _make_recipe(recipe_id="recipe-2", title="味噌汁")]

        result = service.search_public()

        assert len(result) == 2
        mock_repo.find_all_public.assert_called_once()

    def test_filters_by_keyword(self, service, mock_repo):
        mock_repo.find_all_public.return_value = [
            _make_recipe(title="カレーライス"),
            _make_recipe(recipe_id="recipe-2", title="味噌汁"),
        ]

        result = service.search_public(keyword="カレー")

        assert len(result) == 1
        assert result[0].title == "カレーライス"

    def test_keyword_case_insensitive(self, service, mock_repo):
        mock_repo.find_all_public.return_value = [_make_recipe(title="Pasta Carbonara")]

        result = service.search_public(keyword="pasta")

        assert len(result) == 1

    def test_no_match_returns_empty(self, service, mock_repo):
        mock_repo.find_all_public.return_value = [_make_recipe()]

        result = service.search_public(keyword="寿司")

        assert len(result) == 0


# --- search_by_author ---


class TestSearchByAuthor:
    def test_returns_recipes_by_author(self, service, mock_repo):
        mock_repo.find_by_author_id.return_value = [_make_recipe()]

        result = service.search_by_author("user-1")

        assert len(result) == 1
        mock_repo.find_by_author_id.assert_called_once_with("user-1")


# --- get_by_id ---


class TestGetById:
    def test_returns_recipe(self, service, mock_repo):
        mock_repo.find_by_id.return_value = _make_recipe()

        result = service.get_by_id("recipe-1")

        assert result.recipe_id == "recipe-1"
        assert result.title == "カレーライス"

    def test_raises_not_found(self, service, mock_repo):
        mock_repo.find_by_id.return_value = None

        with pytest.raises(RecipeNotFoundException):
            service.get_by_id("nonexistent")


# --- create ---


class TestCreate:
    def test_creates_recipe(self, service, mock_repo):
        request = RecipeCreateRequest(
            title="新しいレシピ",
            ingredients=[{"name": "塩", "quantity": "1", "unit": "tsp"}],
            steps=[{"description": "混ぜる"}],
            cooking_time=10,
        )

        result = service.create(request, "user-1")

        assert result.title == "新しいレシピ"
        assert result.author_id == "user-1"
        assert result.cooking_time == 10
        assert len(result.ingredients) == 1
        assert len(result.steps) == 1
        mock_repo.save.assert_called_once()

    def test_generates_uuid(self, service, mock_repo):
        request = RecipeCreateRequest(
            title="テスト",
            ingredients=[{"name": "水", "quantity": "1", "unit": "cup"}],
            steps=[{"description": "沸かす"}],
        )

        result = service.create(request, "user-1")

        assert result.recipe_id  # UUID が生成されている
        assert len(result.recipe_id) == 36  # UUID形式


# --- update ---


class TestUpdate:
    def test_updates_recipe(self, service, mock_repo):
        mock_repo.find_by_id.return_value = _make_recipe(author_id="user-1")

        request = RecipeUpdateRequest(
            title="更新後タイトル",
            ingredients=[{"name": "塩", "quantity": "2", "unit": "tsp"}],
            steps=[{"description": "新しい手順"}],
            cooking_time=20,
        )

        result = service.update("recipe-1", request, "user-1")

        assert result.title == "更新後タイトル"
        assert result.cooking_time == 20
        mock_repo.save.assert_called_once()

    def test_raises_not_found(self, service, mock_repo):
        mock_repo.find_by_id.return_value = None

        request = RecipeUpdateRequest(
            title="x",
            ingredients=[{"name": "a", "quantity": "1", "unit": ""}],
            steps=[{"description": "b"}],
        )

        with pytest.raises(RecipeNotFoundException):
            service.update("nonexistent", request, "user-1")

    def test_raises_unauthorized_for_other_user(self, service, mock_repo):
        mock_repo.find_by_id.return_value = _make_recipe(author_id="user-1")

        request = RecipeUpdateRequest(
            title="x",
            ingredients=[{"name": "a", "quantity": "1", "unit": ""}],
            steps=[{"description": "b"}],
        )

        with pytest.raises(UnauthorizedException):
            service.update("recipe-1", request, "other-user")


# --- delete ---


class TestDelete:
    def test_marks_as_deleted(self, service, mock_repo):
        recipe = _make_recipe(author_id="user-1")
        mock_repo.find_by_id.return_value = recipe

        service.delete("recipe-1", "user-1")

        assert recipe.is_deleted is True
        mock_repo.save.assert_called_once()

    def test_raises_not_found(self, service, mock_repo):
        mock_repo.find_by_id.return_value = None

        with pytest.raises(RecipeNotFoundException):
            service.delete("nonexistent", "user-1")

    def test_raises_unauthorized_for_other_user(self, service, mock_repo):
        mock_repo.find_by_id.return_value = _make_recipe(author_id="user-1")

        with pytest.raises(UnauthorizedException):
            service.delete("recipe-1", "other-user")


# --- _to_response ---


class TestToResponse:
    def test_maps_all_fields(self):
        recipe = _make_recipe()

        result = RecipeService._to_response(recipe)

        assert result.recipe_id == "recipe-1"
        assert result.title == "カレーライス"
        assert result.author_id == "user-1"
        assert result.cooking_time == 30
        assert result.is_public is True
        assert len(result.ingredients) == 1
        assert result.ingredients[0].name == "にんじん"
        assert len(result.steps) == 1
        assert result.steps[0].description == "切る"
