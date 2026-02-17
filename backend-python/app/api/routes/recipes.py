"""
レシピ API ルーター。
Java版 RecipeController に対応。
"""

import logging
from typing import Annotated

from fastapi import APIRouter, Depends, Query, status

from app.api.deps import CurrentUser
from app.repositories.dependencies import get_recipe_repository
from app.repositories.recipe_repository import RecipeRepository
from app.schemas.recipe import (
    RecipeCreateRequest,
    RecipeResponse,
    RecipeUpdateRequest,
)
from app.services.recipe_service import RecipeService

logger = logging.getLogger(__name__)

router = APIRouter(prefix="/api/recipes", tags=["recipes"])


# --- 依存注入 ---


def get_recipe_service(
    recipe_repo: Annotated[RecipeRepository, Depends(get_recipe_repository)],
) -> RecipeService:
    return RecipeService(recipe_repo)


RecipeServiceDep = Annotated[RecipeService, Depends(get_recipe_service)]


# --- エンドポイント ---


@router.get("", response_model=list[RecipeResponse])
def search_recipes(
    service: RecipeServiceDep,
    keyword: str | None = Query(None),
    author_id: str | None = Query(None, alias="authorId"),
):
    """
    レシピ検索 (公開エンドポイント)。
    - keyword 指定: タイトルで部分一致検索
    - authorId 指定: 作成者IDで検索
    - 両方なし: 全公開レシピ取得
    """
    if author_id:
        return service.search_by_author(author_id)
    return service.search_public(keyword)


@router.get("/{recipe_id}", response_model=RecipeResponse)
def get_recipe(
    recipe_id: str,
    service: RecipeServiceDep,
):
    """レシピ詳細取得 (公開エンドポイント)"""
    return service.get_by_id(recipe_id)


@router.post("", response_model=RecipeResponse, status_code=status.HTTP_201_CREATED)
def create_recipe(
    request: RecipeCreateRequest,
    current_user: CurrentUser,
    service: RecipeServiceDep,
):
    """レシピ作成 (認証必須)"""
    return service.create(request, current_user)


@router.put("/{recipe_id}", response_model=RecipeResponse)
def update_recipe(
    recipe_id: str,
    request: RecipeUpdateRequest,
    current_user: CurrentUser,
    service: RecipeServiceDep,
):
    """レシピ更新 (認証必須・作成者のみ)"""
    return service.update(recipe_id, request, current_user)


@router.delete("/{recipe_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_recipe(
    recipe_id: str,
    current_user: CurrentUser,
    service: RecipeServiceDep,
):
    """レシピ削除 - 論理削除 (認証必須・作成者のみ)"""
    service.delete(recipe_id, current_user)
