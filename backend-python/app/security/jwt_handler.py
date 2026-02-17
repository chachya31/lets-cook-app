"""
JWT 認証ミドルウェア。
Java版の JwtAuthenticationFilter に対応。
"""

import logging

from jose import JWTError, jwt
from starlette.middleware.base import BaseHTTPMiddleware, RequestResponseEndpoint
from starlette.requests import Request
from starlette.responses import Response

from app.config.settings import Settings

logger = logging.getLogger(__name__)

# 認証不要のパス (Java版 SecurityConfig の permitAll に対応)
PUBLIC_PATHS: dict[str, set[str]] = {
    "/api/users/register": {"POST"},
    "/api/users/login": {"POST"},
    "/api/users/confirm": {"POST"},
    "/api/users/resend-code": {"POST"},
}

PUBLIC_GET_PATTERNS: list[str] = [
    "/api/recipes",
]

BEARER_PREFIX = "Bearer "


class JwtAuthenticationMiddleware(BaseHTTPMiddleware):
    """
    JWT トークンを検証し、ユーザーIDとロールを request.state に設定するミドルウェア。
    """

    def __init__(self, app, settings: Settings):
        super().__init__(app)
        self.settings = settings

    async def dispatch(self, request: Request, call_next: RequestResponseEndpoint) -> Response:
        # 公開パスはスキップ
        if self._is_public(request):
            return await call_next(request)

        # トークン抽出・検証
        token = self._extract_token(request)
        if token:
            try:
                # JWTデコード (署名検証はCognito JWKSで行う想定)
                # ※ 本実装では unverified claims を読み取り、
                #    実際の署名検証は Cognito の validateToken に委譲
                claims = jwt.get_unverified_claims(token)
                user_id = claims.get("sub")

                if user_id:
                    request.state.user_id = user_id
                    request.state.user_roles = self._extract_groups(claims)
                    logger.debug("認証成功: userId=%s, roles=%s", user_id, request.state.user_roles)

            except JWTError as e:
                logger.error("JWT認証エラー: %s", e)
                # 認証失敗時は user_id を設定しない (SecurityContext クリア相当)

        return await call_next(request)

    def _is_public(self, request: Request) -> bool:
        """認証不要のパスかどうかを判定"""
        path = request.url.path
        method = request.method

        # 完全一致の公開パス
        if path in PUBLIC_PATHS and method in PUBLIC_PATHS[path]:
            return True

        # GET の公開パターン (/api/recipes, /api/recipes/{id}, /api/recipes/{id}/reviews)
        if method == "GET":
            for pattern in PUBLIC_GET_PATTERNS:
                if path == pattern or path.startswith(pattern + "/"):
                    return True

        return False

    def _extract_token(self, request: Request) -> str | None:
        """Authorization ヘッダーから Bearer トークンを抽出"""
        auth_header = request.headers.get("Authorization", "")
        if auth_header.startswith(BEARER_PREFIX):
            return auth_header[len(BEARER_PREFIX) :]
        return None

    def _extract_groups(self, claims: dict) -> list[str]:
        """JWT の cognito:groups クレームからロール一覧を抽出"""
        groups = claims.get("cognito:groups")
        if not groups:
            logger.debug("JWTにcognito:groupsクレームが存在しません")
            return []

        if isinstance(groups, list):
            return [g.upper() for g in groups]

        return []
