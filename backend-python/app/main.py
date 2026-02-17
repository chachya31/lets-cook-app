"""
FastAPI アプリケーション エントリーポイント。
Java版の CookingAppApplication + SecurityConfig + CorsConfig に対応。
"""

import logging

from fastapi import FastAPI

from app.config.cors import setup_cors
from app.config.logging import setup_logging
from app.config.settings import get_settings
from app.exceptions.handlers import setup_exception_handlers
from app.middleware.request_logging import RequestLoggingMiddleware
from app.security.jwt_handler import JwtAuthenticationMiddleware

logger = logging.getLogger(__name__)


def create_app() -> FastAPI:
    """FastAPI アプリケーションを生成・設定する (Factory パターン)"""
    settings = get_settings()

    # ログ設定 (logback-spring.xml 相当)
    setup_logging(settings.environment)

    app = FastAPI(
        title="Cooking Support App",
        version="1.0.0",
        docs_url="/docs" if settings.is_local else None,
        redoc_url="/redoc" if settings.is_local else None,
    )

    # ミドルウェア (適用順: 下から上)
    app.add_middleware(RequestLoggingMiddleware)
    app.add_middleware(JwtAuthenticationMiddleware, settings=settings)

    # CORS (CorsConfig 相当)
    setup_cors(app)

    # 例外ハンドラー (GlobalExceptionHandler 相当)
    setup_exception_handlers(app)

    # ルーター登録
    _include_routers(app)

    logger.info("Cooking Support App 起動 (env=%s, port=%s)", settings.environment, settings.server_port)

    return app


def _include_routers(app: FastAPI) -> None:
    """全ルーターを登録する"""
    from app.api.routes import recipes

    app.include_router(recipes.router)

    # TODO: 今後追加するルーター
    # from app.api.routes import users, admin, schedules, shopping_lists, ...
    # app.include_router(users.router)
    # app.include_router(admin.router)


app = create_app()
