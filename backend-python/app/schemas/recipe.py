"""
レシピ関連のリクエスト/レスポンススキーマ (Pydantic)。
Java版の RecipeRequest, RecipeResponse, IngredientDto, StepDto に対応。
"""

from datetime import datetime

from pydantic import BaseModel, Field


# --- 共通サブモデル ---


class IngredientSchema(BaseModel):
    name: str = Field(..., max_length=100)
    quantity: str = Field(...)
    unit: str = Field("", max_length=50)


class StepSchema(BaseModel):
    description: str = Field(...)
    image_url: str = ""
    video_url: str = ""


# --- Request ---


class RecipeCreateRequest(BaseModel):
    """レシピ作成リクエスト"""
    title: str = Field(..., min_length=1, max_length=100)
    ingredients: list[IngredientSchema] = Field(..., min_length=1)
    steps: list[StepSchema] = Field(..., min_length=1)
    cooking_time: int = Field(0, ge=0)


class RecipeUpdateRequest(BaseModel):
    """レシピ更新リクエスト"""
    title: str = Field(..., min_length=1, max_length=100)
    ingredients: list[IngredientSchema] = Field(..., min_length=1)
    steps: list[StepSchema] = Field(..., min_length=1)
    cooking_time: int = Field(0, ge=0)


class SearchByIngredientsRequest(BaseModel):
    """食材検索リクエスト"""
    ingredients: list[str] = Field(..., min_length=1)


# --- Response ---


class RecipeResponse(BaseModel):
    """レシピレスポンス"""
    recipe_id: str
    title: str
    author_id: str
    ingredients: list[IngredientSchema]
    steps: list[StepSchema]
    cooking_time: int
    image_url: str
    is_public: bool
    created_at: datetime
    updated_at: datetime


class RecipeSummary(BaseModel):
    """レシピサマリー (食材検索結果用)"""
    recipe_id: str
    title: str
    image_url: str


class RecipeSearchByIngredientResponse(BaseModel):
    """食材検索レスポンス"""
    recipes: list[RecipeSummary]
    total_count: int
    searched_ingredients: list[str]
