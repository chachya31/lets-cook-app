"""例外ハンドラーの単体テスト (FastAPI TestClient 使用)"""

import pytest
from fastapi import FastAPI, Request
from fastapi.testclient import TestClient

from app.exceptions.exceptions import (
    AuthenticationException,
    ImageValidationException,
    RecipeNotFoundException,
    UnauthorizedException,
    UserAlreadyExistsException,
    UserNotFoundException,
)
from app.exceptions.handlers import setup_exception_handlers


def _create_test_app() -> FastAPI:
    """テスト用アプリを作成し、例外ハンドラーを登録"""
    app = FastAPI()
    setup_exception_handlers(app)

    @app.get("/auth-error")
    def raise_auth():
        raise AuthenticationException()

    @app.get("/not-found")
    def raise_not_found():
        raise RecipeNotFoundException("レシピID: abc が見つかりません")

    @app.get("/unauthorized")
    def raise_unauthorized():
        raise UnauthorizedException()

    @app.get("/user-exists")
    def raise_user_exists():
        raise UserAlreadyExistsException()

    @app.get("/user-not-found")
    def raise_user_not_found():
        raise UserNotFoundException()

    @app.get("/image-error")
    def raise_image_error():
        raise ImageValidationException("ファイルサイズが5MBを超えています")

    @app.get("/value-error")
    def raise_value():
        raise ValueError("不正な値です")

    @app.get("/unhandled")
    def raise_unhandled():
        raise RuntimeError("予期しないエラー")

    return app


@pytest.fixture
def client():
    return TestClient(_create_test_app(), raise_server_exceptions=False)


class TestAppExceptionHandler:
    def test_authentication_error_returns_401(self, client):
        resp = client.get("/auth-error")
        assert resp.status_code == 401
        body = resp.json()
        assert body["code"] == "AUTHENTICATION_ERROR"
        assert "timestamp" in body

    def test_not_found_returns_404(self, client):
        resp = client.get("/not-found")
        assert resp.status_code == 404
        body = resp.json()
        assert body["code"] == "RECIPE_NOT_FOUND"
        assert "abc" in body["message"]

    def test_unauthorized_returns_403(self, client):
        resp = client.get("/unauthorized")
        assert resp.status_code == 403
        body = resp.json()
        assert body["code"] == "UNAUTHORIZED"

    def test_user_exists_returns_409(self, client):
        resp = client.get("/user-exists")
        assert resp.status_code == 409
        body = resp.json()
        assert body["code"] == "USER_ALREADY_EXISTS"

    def test_user_not_found_returns_404(self, client):
        resp = client.get("/user-not-found")
        assert resp.status_code == 404
        body = resp.json()
        assert body["code"] == "USER_NOT_FOUND"

    def test_image_validation_returns_400(self, client):
        resp = client.get("/image-error")
        assert resp.status_code == 400
        body = resp.json()
        assert body["code"] == "IMAGE_VALIDATION_ERROR"
        assert "5MB" in body["message"]


class TestValueErrorHandler:
    def test_value_error_returns_400(self, client):
        resp = client.get("/value-error")
        assert resp.status_code == 400
        body = resp.json()
        assert body["code"] == "INVALID_ARGUMENT"
        assert body["message"] == "不正な値です"


class TestUnhandledExceptionHandler:
    def test_unhandled_returns_500(self, client):
        resp = client.get("/unhandled")
        assert resp.status_code == 500
        body = resp.json()
        assert body["code"] == "INTERNAL_SERVER_ERROR"
        assert body["details"]["exception"] == "RuntimeError"
        assert "stackTrace" in body["details"]


class TestErrorResponseFormat:
    """全エラーレスポンスが統一フォーマットであることを確認"""

    @pytest.mark.parametrize("path", ["/auth-error", "/not-found", "/unauthorized", "/value-error", "/unhandled"])
    def test_has_required_fields(self, client, path):
        resp = client.get(path)
        body = resp.json()
        assert "code" in body
        assert "message" in body
        assert "details" in body
        assert "timestamp" in body
