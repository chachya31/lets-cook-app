"""認証依存関係 (deps.py) の単体テスト"""

import pytest

from app.api.deps import get_current_user, get_current_user_roles, require_admin
from app.exceptions.exceptions import AuthenticationException, UnauthorizedException


class FakeState:
    pass


class FakeRequest:
    def __init__(self, user_id: str | None = None, roles: list[str] | None = None):
        self.state = FakeState()
        if user_id is not None:
            self.state.user_id = user_id
        if roles is not None:
            self.state.user_roles = roles


# --- get_current_user ---


class TestGetCurrentUser:
    def test_returns_user_id(self):
        request = FakeRequest(user_id="user-123")
        assert get_current_user(request) == "user-123"

    def test_raises_when_no_user_id(self):
        request = FakeRequest()
        with pytest.raises(AuthenticationException) as exc_info:
            get_current_user(request)
        assert exc_info.value.status_code == 401

    def test_raises_when_user_id_is_empty(self):
        request = FakeRequest(user_id="")
        with pytest.raises(AuthenticationException):
            get_current_user(request)

    def test_raises_when_user_id_is_none(self):
        request = FakeRequest(user_id=None)
        # user_id=None は setattr されるが falsy
        with pytest.raises(AuthenticationException):
            get_current_user(request)


# --- get_current_user_roles ---


class TestGetCurrentUserRoles:
    def test_returns_roles(self):
        request = FakeRequest(roles=["USERS", "ADMINS"])
        assert get_current_user_roles(request) == ["USERS", "ADMINS"]

    def test_returns_empty_when_no_roles(self):
        request = FakeRequest()
        assert get_current_user_roles(request) == []


# --- require_admin ---


class TestRequireAdmin:
    def test_returns_user_id_for_admin(self):
        request = FakeRequest(user_id="admin-1", roles=["ADMINS"])
        assert require_admin(request) == "admin-1"

    def test_returns_user_id_for_user_with_multiple_roles(self):
        request = FakeRequest(user_id="admin-1", roles=["USERS", "ADMINS"])
        assert require_admin(request) == "admin-1"

    def test_raises_unauthorized_for_non_admin(self):
        request = FakeRequest(user_id="user-1", roles=["USERS"])
        with pytest.raises(UnauthorizedException) as exc_info:
            require_admin(request)
        assert exc_info.value.status_code == 403

    def test_raises_unauthorized_for_no_roles(self):
        request = FakeRequest(user_id="user-1", roles=[])
        with pytest.raises(UnauthorizedException):
            require_admin(request)

    def test_raises_auth_error_when_not_authenticated(self):
        request = FakeRequest()
        with pytest.raises(AuthenticationException):
            require_admin(request)
