from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

# 許可するオリジンパターン
ALLOWED_ORIGIN_PATTERNS = [
    "http://localhost:*",
    "https://*.amplifyapp.com",
]

# FastAPI の CORSMiddleware は正規表現ベースなので変換
ALLOWED_ORIGIN_REGEX = r"(http://localhost:\d+|https://.*\.amplifyapp\.com)"

ALLOWED_METHODS = ["GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"]
ALLOWED_HEADERS = ["*"]
EXPOSE_HEADERS = ["Authorization", "Content-Type"]
MAX_AGE = 3600


def setup_cors(app: FastAPI) -> None:
    """FastAPI アプリに CORS ミドルウェアを追加"""
    app.add_middleware(
        CORSMiddleware,
        allow_origin_regex=ALLOWED_ORIGIN_REGEX,
        allow_credentials=True,
        allow_methods=ALLOWED_METHODS,
        allow_headers=ALLOWED_HEADERS,
        expose_headers=EXPOSE_HEADERS,
        max_age=MAX_AGE,
    )
