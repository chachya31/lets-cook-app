from dataclasses import dataclass, field
from datetime import datetime
from enum import StrEnum


class ReviewStatus(StrEnum):
    VISIBLE = "VISIBLE"
    HIDDEN = "HIDDEN"


@dataclass
class Review:
    review_id: str
    recipe_id: str
    user_id: str
    rating: int
    comment: str = ""
    status: ReviewStatus = ReviewStatus.VISIBLE
    reported_count: int = 0
    created_at: datetime = field(default_factory=datetime.now)
    updated_at: datetime = field(default_factory=datetime.now)
