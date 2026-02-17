from dataclasses import dataclass, field
from datetime import date, datetime
from decimal import Decimal


@dataclass
class InventoryItem:
    item_id: str
    user_id: str
    name: str
    quantity: Decimal
    unit: str
    expiry_date: date | None = None
    purchased_at: datetime = field(default_factory=datetime.now)
    created_at: datetime = field(default_factory=datetime.now)
