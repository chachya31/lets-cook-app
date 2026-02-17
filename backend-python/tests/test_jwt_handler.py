"""JwtAuthenticationMiddleware の単体テスト"""

import pytest

from app.security.jwt_handler import JwtAuthenticationMiddleware


class FakeURL:
    def __init__(self, path: str):
        self.path = path


class FakeRequest:
    def __init__(self, method: str, path: str, headers: dict | None = None):
        self.method = method
        self.url = FakeURL(path)
        self.headers = headers or {}


@pytest.fixture
def middleware():
    # app と settings は _is_public / _extract_token / _extract_groups テストでは不使用
    return JwtAuthenticationMiddleware.__new__(JwtAuthenticationMiddleware)


# --- _is_public ---


class TestIsPublic:
    def test_register_post_is_public(self, middleware):
        req = FakeRequest("POST", "/api/users/register")
        assert middleware._is_public(req) is True

    def test_login_post_is_public(self, middleware):
        req = FakeRequest("POST", "/api/users/login")
        assert middleware._is_public(req) is True

    def test_confirm_post_is_public(self, middleware):
        req = FakeRequest("POST", "/api/users/confirm")
        assert middleware._is_public(req) is True

    def test_resend_code_post_is_public(self, middleware):
        req = FakeRequest("POST", "/api/users/resend-code")
        assert middleware._is_public(req) is True

    def test_register_get_is_not_public(self, middleware):
        req = FakeRequest("GET", "/api/users/register")
        assert middleware._is_public(req) is False

    def test_get_recipes_is_public(self, middleware):
        req = FakeRequest("GET", "/api/recipes")
        assert middleware._is_public(req) is True

    def test_get_recipe_by_id_is_public(self, middleware):
        req = FakeRequest("GET", "/api/recipes/abc-123")
        assert middleware._is_public(req) is True

    def test_get_recipe_reviews_is_public(self, middleware):
        req = FakeRequest("GET", "/api/recipes/abc-123/reviews")
        assert middleware._is_public(req) is True

    def test_post_recipes_is_not_public(self, middleware):
        req = FakeRequest("POST", "/api/recipes")
        assert middleware._is_public(req) is False

    def test_delete_recipe_is_not_public(self, middleware):
        req = FakeRequest("DELETE", "/api/recipes/abc-123")
        assert middleware._is_public(req) is False

    def test_get_schedules_is_not_public(self, middleware):
        req = FakeRequest("GET", "/api/schedules")
        assert middleware._is_public(req) is False

    def test_get_admin_is_not_public(self, middleware):
        req = FakeRequest("GET", "/api/admin/dashboard")
        assert middleware._is_public(req) is False


# --- _extract_token ---


class TestExtractToken:
    def test_extracts_bearer_token(self, middleware):
        req = FakeRequest("GET", "/", headers={"Authorization": "Bearer my-jwt-token"})
        assert middleware._extract_token(req) == "my-jwt-token"

    def test_returns_none_for_no_header(self, middleware):
        req = FakeRequest("GET", "/", headers={})
        assert middleware._extract_token(req) is None

    def test_returns_none_for_empty_header(self, middleware):
        req = FakeRequest("GET", "/", headers={"Authorization": ""})
        assert middleware._extract_token(req) is None

    def test_returns_none_for_basic_auth(self, middleware):
        req = FakeRequest("GET", "/", headers={"Authorization": "Basic abc123"})
        assert middleware._extract_token(req) is None

    def test_returns_none_for_bearer_without_space(self, middleware):
        req = FakeRequest("GET", "/", headers={"Authorization": "Bearertoken"})
        assert middleware._extract_token(req) is None


# --- _extract_groups ---


class TestExtractGroups:
    def test_extracts_groups(self, middleware):
        claims = {"cognito:groups": ["USERS", "ADMINS"]}
        assert middleware._extract_groups(claims) == ["USERS", "ADMINS"]

    def test_uppercases_groups(self, middleware):
        claims = {"cognito:groups": ["users", "admins"]}
        assert middleware._extract_groups(claims) == ["USERS", "ADMINS"]

    def test_returns_empty_for_no_groups(self, middleware):
        claims = {}
        assert middleware._extract_groups(claims) == []

    def test_returns_empty_for_none_groups(self, middleware):
        claims = {"cognito:groups": None}
        assert middleware._extract_groups(claims) == []

    def test_returns_empty_for_non_list_groups(self, middleware):
        claims = {"cognito:groups": "single-string"}
        assert middleware._extract_groups(claims) == []
