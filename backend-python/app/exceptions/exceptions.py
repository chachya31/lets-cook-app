"""
ドメイン例外クラス。
Java版の domain/exception/ に対応。
"""


class AppException(Exception):
    """アプリケーション例外の基底クラス"""

    def __init__(self, message: str, code: str, status_code: int = 500):
        super().__init__(message)
        self.message = message
        self.code = code
        self.status_code = status_code


# --- 認証・認可 (401 / 403) ---


class AuthenticationException(AppException):
    """JWT検証失敗など認証エラー (401)"""

    def __init__(self, message: str = "認証に失敗しました"):
        super().__init__(message, code="AUTHENTICATION_ERROR", status_code=401)


class UnauthorizedException(AppException):
    """リソースへのアクセス権限なし (403)"""

    def __init__(self, message: str = "アクセス権限がありません"):
        super().__init__(message, code="UNAUTHORIZED", status_code=403)


# --- リソース未検出 (404) ---


class UserNotFoundException(AppException):
    """ユーザーが見つからない"""

    def __init__(self, message: str = "ユーザーが見つかりません"):
        super().__init__(message, code="USER_NOT_FOUND", status_code=404)


class RecipeNotFoundException(AppException):
    """レシピが見つからない"""

    def __init__(self, message: str = "レシピが見つかりません"):
        super().__init__(message, code="RECIPE_NOT_FOUND", status_code=404)


class ReviewNotFoundException(AppException):
    """レビューが見つからない"""

    def __init__(self, message: str = "レビューが見つかりません"):
        super().__init__(message, code="REVIEW_NOT_FOUND", status_code=404)


class ScheduleNotFoundException(AppException):
    """スケジュールが見つからない"""

    def __init__(self, message: str = "スケジュールが見つかりません"):
        super().__init__(message, code="SCHEDULE_NOT_FOUND", status_code=404)


class ShoppingListItemNotFoundException(AppException):
    """買い物リストアイテムが見つからない"""

    def __init__(self, message: str = "買い物リストのアイテムが見つかりません"):
        super().__init__(message, code="SHOPPING_LIST_ITEM_NOT_FOUND", status_code=404)


# --- 競合 (409) ---


class UserAlreadyExistsException(AppException):
    """ユーザーが既に存在する"""

    def __init__(self, message: str = "ユーザーは既に存在します"):
        super().__init__(message, code="USER_ALREADY_EXISTS", status_code=409)


# --- バリデーション (400) ---


class ImageValidationException(AppException):
    """画像バリデーションエラー"""

    def __init__(self, message: str = "画像が不正です"):
        super().__init__(message, code="IMAGE_VALIDATION_ERROR", status_code=400)
