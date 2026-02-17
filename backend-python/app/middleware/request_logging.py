import logging
import time

from starlette.middleware.base import BaseHTTPMiddleware, RequestResponseEndpoint
from starlette.requests import Request
from starlette.responses import Response

logger = logging.getLogger("app.middleware.request")


class RequestLoggingMiddleware(BaseHTTPMiddleware):
    """
    リクエスト/レスポンスのログを記録するミドルウェア。
    Java版 StreamLambdaHandler のログ出力に相当。
    """

    async def dispatch(self, request: Request, call_next: RequestResponseEndpoint) -> Response:
        start_time = time.perf_counter()

        # リクエストログ
        logger.info(
            "%s %s",
            request.method,
            request.url.path,
        )

        response = await call_next(request)

        # レスポンスログ (処理時間込み)
        elapsed_ms = (time.perf_counter() - start_time) * 1000
        logger.info(
            "%s %s -> %d (%.1fms)",
            request.method,
            request.url.path,
            response.status_code,
            elapsed_ms,
        )

        return response
