from dataclasses import dataclass, field
from datetime import date, datetime


@dataclass
class Schedule:
    schedule_id: str
    user_id: str
    date: date
    recipe_id: str
    recipe_title: str
    is_done: bool = False
    memo: str = ""
    created_at: datetime = field(default_factory=datetime.now)
