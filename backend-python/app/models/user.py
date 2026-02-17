from dataclasses import dataclass, field
from datetime import date, datetime


@dataclass
class User:
    user_id: str
    email: str
    nickname: str = ""
    display_name: str = ""
    profile_image_url: str = ""
    preferred_language: str = "JA"
    last_cooking_date: date | None = None
    last_login_date: datetime | None = None
    created_at: datetime = field(default_factory=datetime.now)
    timezone: str = "Asia/Tokyo"
    marketing_opt_out: bool = False
