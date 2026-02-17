from functools import lru_cache

from pydantic import Field
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    """アプリケーション設定 (環境変数 / .env から読み込み)"""

    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        case_sensitive=False,
    )

    # --- Environment ---
    environment: str = Field(default="local", description="実行環境 (local / dev / prod)")
    server_port: int = Field(default=8080)

    # --- AWS General ---
    aws_region: str = Field(default="ap-northeast-1")
    aws_access_key_id: str | None = Field(default=None)
    aws_secret_access_key: str | None = Field(default=None)

    # --- DynamoDB ---
    dynamodb_endpoint: str | None = Field(default=None, description="LocalStack用エンドポイント")
    dynamodb_users_table: str = Field(default="Users")
    dynamodb_recipes_table: str = Field(default="Recipes")
    dynamodb_recipe_ingredients_table: str = Field(default="RecipeIngredients")
    dynamodb_reviews_table: str = Field(default="Reviews")
    dynamodb_schedules_table: str = Field(default="Schedules")
    dynamodb_shopping_lists_table: str = Field(default="ShoppingLists")
    dynamodb_chat_conversations_table: str = Field(default="ChatConversations")
    dynamodb_chat_messages_table: str = Field(default="ChatMessages")
    dynamodb_inventory_table: str = Field(default="Inventory")

    # --- S3 ---
    s3_endpoint: str | None = Field(default=None, description="LocalStack用エンドポイント")
    s3_bucket: str = Field(default="cooking-app-images")

    # --- Cognito ---
    aws_cognito_user_pool_id: str = Field(default="")
    aws_cognito_client_id: str = Field(default="")

    # --- Gemini ---
    gemini_api_key: str = Field(default="")
    gemini_api_key_secret_arn: str | None = Field(default=None)

    # --- Upload ---
    max_file_size: int = Field(default=5 * 1024 * 1024, description="最大ファイルサイズ (bytes)")

    @property
    def is_local(self) -> bool:
        return self.environment == "local"

    @property
    def is_prod(self) -> bool:
        return self.environment == "prod"

    @property
    def use_localstack(self) -> bool:
        """DynamoDB/S3にLocalStackを使用するかどうか"""
        return self.dynamodb_endpoint is not None


@lru_cache
def get_settings() -> Settings:
    """設定のシングルトンインスタンスを返す (FastAPI Depends で使用)"""
    return Settings()
