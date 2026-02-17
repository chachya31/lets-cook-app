"""
共通依存関係 (FastAPI Depends)。
Java版の SecurityConfig / JwtAuthenticationFilter に対応。
"""

import logging
from typing import Annotated

from fastapi import Depends, Header, Request

from app.config.settings import Settings, get_settings
from app.exceptions.exceptions import AuthenticationException, UnauthorizedException

logger = logging.getLogger(__name__)

# Settings 依存
SettingsDep = Annotated[Settings, Depends(get_settings)]


def get_current_user(request: Request) -> str:
    """
    認証済みユーザーIDを取得する。

    JwtAuthenticationFilter で認証されたユーザーIDを request.state から取得。
    認証されていない場合は AuthenticationException を送出。
    """
    user_id: str | None = getattr(request.state, "user_id", None)
    if not user_id:
        raise AuthenticationException("認証が必要です")
    return user_id


def get_current_user_roles(request: Request) -> list[str]:
    """認証済みユーザーのロール一覧を取得する。"""
    return getattr(request.state, "user_roles", [])


def require_admin(request: Request) -> str:
    """
    ADMINS ロールを要求する。
    Java版の @PreAuthorize("hasRole('ADMINS')") に対応。
    """
    user_id = get_current_user(request)
    roles = get_current_user_roles(request)

    if "ADMINS" not in roles:
        raise UnauthorizedException("管理者権限が必要です")
    return user_id


# 型エイリアス (ルーターで使用)
CurrentUser = Annotated[str, Depends(get_current_user)]
AdminUser = Annotated[str, Depends(require_admin)]
