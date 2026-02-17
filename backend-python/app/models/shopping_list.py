from dataclasses import dataclass, field
from datetime import datetime
from decimal import Decimal


@dataclass
class ShoppingListItem:
    item_id: str
    user_id: str
    name: str
    quantity: Decimal
    unit: str
    is_checked: bool = False
    is_checked_at: datetime | None = None
    added_at: datetime = field(default_factory=datetime.now)
    source_recipe_id: str = ""
    normalized_key: str = ""
