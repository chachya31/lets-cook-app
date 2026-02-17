from dataclasses import dataclass, field
from datetime import datetime


@dataclass
class Ingredient:
    name: str
    quantity: str
    unit: str


@dataclass
class Step:
    description: str
    image_url: str = ""
    video_url: str = ""


@dataclass
class Recipe:
    recipe_id: str
    title: str
    author_id: str
    ingredients: list[Ingredient] = field(default_factory=list)
    steps: list[Step] = field(default_factory=list)
    cooking_time: int = 0
    image_url: str = ""
    is_public: bool = True
    is_deleted: bool = False
    created_at: datetime = field(default_factory=datetime.now)
    updated_at: datetime = field(default_factory=datetime.now)


@dataclass
class RecipeIngredient:
    """食材→レシピの逆引きインデックス"""
    ingredient_name: str
    recipe_id: str
    recipe_title: str
    recipe_image_url: str = ""
