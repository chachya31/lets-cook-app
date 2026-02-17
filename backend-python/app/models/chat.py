from dataclasses import dataclass, field
from datetime import datetime


@dataclass
class ChatConversation:
    conversation_id: str
    user_id: str
    title: str = ""
    conversation_type: str = "general"
    created_at: datetime = field(default_factory=datetime.now)
    updated_at: datetime = field(default_factory=datetime.now)


@dataclass
class ChatMessage:
    message_id: str
    conversation_id: str
    role: str  # "user" or "assistant"
    content: str
    generated_recipe: str = ""
    created_at: datetime = field(default_factory=datetime.now)
