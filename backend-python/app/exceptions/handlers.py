"""
グローバル例外ハンドラー。
Java版の GlobalExceptionHandler に対応。
"""

import logging
import traceback
from datetime import datetime

from botocore.exceptions import ClientError
from fastapi import FastAPI, Request
from fastapi.exceptions import RequestValidationError
from fastapi.responses import JSONResponse

from app.exceptions.exceptions import AppException

logger = logging.getLogger(__name__)


def _error_response(code: str, message: str, details: dict | None = None, status_code: int = 500) -> JSONResponse:
    """統一エラーレスポンスを生成"""
    body = {
        "code": code,
        "message": message,
        "details": details or {},
        "timestamp": datetime.now().isoformat(),
    }
    return JSONResponse(status_code=status_code, content=body)


def _stack_trace(exc: Exception, max_lines: int = 10) -> str:
    """スタックトレースの先頭N行を文字列化"""
    lines = traceback.format_exception(type(exc), exc, exc.__traceback__)
    return "".join(lines)[:2000]


def setup_exception_handlers(app: FastAPI) -> None:
    """FastAPI アプリにグローバル例外ハンドラーを登録"""

    @app.exception_handler(AppException)
    async def app_exception_handler(_request: Request, exc: AppException) -> JSONResponse:
        """アプリケーション例外 (認証エラー、リソース未検出など)"""
        logger.warning("%s: %s", exc.code, exc.message)
        return _error_response(
            code=exc.code,
            message=exc.message,
            status_code=exc.status_code,
        )

    @app.exception_handler(RequestValidationError)
    async def validation_exception_handler(_request: Request, exc: RequestValidationError) -> JSONResponse:
        """リクエストバリデーションエラー (Pydantic)"""
        details = {}
        for error in exc.errors():
            field = ".".join(str(loc) for loc in error["loc"] if loc != "body")
            details[field] = error["msg"]

        logger.warning("バリデーションエラー: %s", details)
        return _error_response(
            code="VALIDATION_ERROR",
            message="入力内容に誤りがあります",
            details=details,
            status_code=400,
        )

    @app.exception_handler(ValueError)
    async def value_error_handler(_request: Request, exc: ValueError) -> JSONResponse:
        """不正な引数 (Java版 IllegalArgumentException 相当)"""
        logger.warning("ValueError: %s", exc)
        return _error_response(
            code="INVALID_ARGUMENT",
            message=str(exc),
            status_code=400,
        )

    @app.exception_handler(ClientError)
    async def aws_client_error_handler(_request: Request, exc: ClientError) -> JSONResponse:
        """AWS SDK エラー (DynamoDB, S3, Cognito)"""
        error_code = exc.response.get("Error", {}).get("Code", "Unknown")
        error_message = exc.response.get("Error", {}).get("Message", str(exc))

        logger.error("AWS ClientError [%s]: %s", error_code, error_message)
        return _error_response(
            code="AWS_ERROR",
            message="サーバーエラーが発生しました",
            details={
                "aws_error_code": error_code,
                "aws_error_message": error_message,
            },
            status_code=500,
        )

    @app.exception_handler(Exception)
    async def unhandled_exception_handler(_request: Request, exc: Exception) -> JSONResponse:
        """予期しないエラー (フォールバック)"""
        logger.error("予期しないエラー: %s", exc, exc_info=True)
        return _error_response(
            code="INTERNAL_SERVER_ERROR",
            message="予期しないエラーが発生しました",
            details={
                "exception": type(exc).__name__,
                "detail": str(exc),
                "stackTrace": _stack_trace(exc),
            },
            status_code=500,
        )
