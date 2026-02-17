"""
レシピサービス。
Java版の recipe ユースケース群 (Create, Update, Delete, Get, Search) に対応。
"""

import logging
import uuid
from datetime import datetime

from app.exceptions.exceptions import RecipeNotFoundException, UnauthorizedException
from app.models.recipe import Ingredient, Recipe, Step
from app.repositories.recipe_repository import RecipeRepository
from app.schemas.recipe import (
    RecipeCreateRequest,
    RecipeResponse,
    RecipeSearchByIngredientResponse,
    RecipeSummary,
    RecipeUpdateRequest,
    StepSchema,
)

logger = logging.getLogger(__name__)


class RecipeService:

    def __init__(self, recipe_repo: RecipeRepository):
        self.recipe_repo = recipe_repo

    # --- 検索 ---

    def search_public(self, keyword: str | None = None) -> list[RecipeResponse]:
        """公開レシピを検索 (SearchRecipesUseCase.executePublic / executeByKeyword)"""
        recipes = self.recipe_repo.find_all_public()

        if keyword:
            lower_keyword = keyword.lower()
            recipes = [r for r in recipes if lower_keyword in r.title.lower()]

        logger.info("GET /api/recipes - keyword=%s, results=%d", keyword, len(recipes))
        return [self._to_response(r) for r in recipes]

    def search_by_author(self, author_id: str) -> list[RecipeResponse]:
        """作成者IDでレシピを検索 (SearchRecipesUseCase.executeByAuthor)"""
        recipes = self.recipe_repo.find_by_author_id(author_id)
        return [self._to_response(r) for r in recipes]

    # --- 取得 ---

    def get_by_id(self, recipe_id: str) -> RecipeResponse:
        """レシピ詳細取得 (GetRecipeUseCase)"""
        recipe = self.recipe_repo.find_by_id(recipe_id)
        if not recipe:
            raise RecipeNotFoundException(f"レシピが見つかりません: {recipe_id}")
        return self._to_response(recipe)

    # --- 作成 ---

    def create(self, request: RecipeCreateRequest, author_id: str) -> RecipeResponse:
        """レシピ作成 (CreateRecipeUseCase)"""
        recipe = Recipe(
            recipe_id=str(uuid.uuid4()),
            title=request.title,
            author_id=author_id,
            ingredients=[Ingredient(name=i.name, quantity=i.quantity, unit=i.unit) for i in request.ingredients],
            steps=[Step(description=s.description, image_url=s.image_url, video_url=s.video_url) for s in request.steps],
            cooking_time=request.cooking_time,
        )
        self.recipe_repo.save(recipe)
        logger.info("レシピ作成: recipeId=%s, authorId=%s", recipe.recipe_id, author_id)
        return self._to_response(recipe)

    # --- 更新 ---

    def update(self, recipe_id: str, request: RecipeUpdateRequest, user_id: str) -> RecipeResponse:
        """レシピ更新 (UpdateRecipeUseCase)"""
        recipe = self.recipe_repo.find_by_id(recipe_id)
        if not recipe:
            raise RecipeNotFoundException(f"レシピが見つかりません: {recipe_id}")

        if recipe.author_id != user_id:
            raise UnauthorizedException("このレシピを編集する権限がありません")

        recipe.title = request.title
        recipe.ingredients = [Ingredient(name=i.name, quantity=i.quantity, unit=i.unit) for i in request.ingredients]
        recipe.steps = [Step(description=s.description, image_url=s.image_url, video_url=s.video_url) for s in request.steps]
        recipe.cooking_time = request.cooking_time
        recipe.updated_at = datetime.now()

        self.recipe_repo.save(recipe)
        logger.info("レシピ更新: recipeId=%s, userId=%s", recipe_id, user_id)
        return self._to_response(recipe)

    # --- 削除 ---

    def delete(self, recipe_id: str, user_id: str) -> None:
        """レシピ削除 - 論理削除 (DeleteRecipeUseCase)"""
        recipe = self.recipe_repo.find_by_id(recipe_id)
        if not recipe:
            raise RecipeNotFoundException(f"レシピが見つかりません: {recipe_id}")

        if recipe.author_id != user_id:
            raise UnauthorizedException("このレシピを削除する権限がありません")

        recipe.is_deleted = True
        self.recipe_repo.save(recipe)
        logger.info("レシピ削除(論理): recipeId=%s, userId=%s", recipe_id, user_id)

    # --- マッピング ---

    @staticmethod
    def _to_response(recipe: Recipe) -> RecipeResponse:
        """Recipe エンティティ → RecipeResponse (Java版 RecipeMapper.toResponse 相当)"""
        return RecipeResponse(
            recipe_id=recipe.recipe_id,
            title=recipe.title,
            author_id=recipe.author_id,
            ingredients=[
                {"name": i.name, "quantity": i.quantity, "unit": i.unit}
                for i in recipe.ingredients
            ],
            steps=[
                {"description": s.description, "image_url": s.image_url, "video_url": s.video_url}
                for s in recipe.steps
            ],
            cooking_time=recipe.cooking_time,
            image_url=recipe.image_url,
            is_public=recipe.is_public,
            created_at=recipe.created_at,
            updated_at=recipe.updated_at,
        )
