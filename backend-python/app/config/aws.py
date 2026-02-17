import logging

import boto3
from botocore.config import Config as BotoConfig

from app.config.settings import Settings, get_settings

logger = logging.getLogger(__name__)


def _base_client_kwargs(settings: Settings) -> dict:
    """AWS クライアント共通の引数を生成"""
    return {"region_name": settings.aws_region}


def _localstack_kwargs(endpoint: str) -> dict:
    """LocalStack 用の追加引数"""
    return {
        "endpoint_url": endpoint,
        "aws_access_key_id": "test",
        "aws_secret_access_key": "test",
    }


# -------------------------------------------------------
# DynamoDB
# -------------------------------------------------------
def create_dynamodb_client(settings: Settings | None = None):
    """DynamoDB クライアントを生成"""
    settings = settings or get_settings()
    kwargs = _base_client_kwargs(settings)

    if settings.dynamodb_endpoint:
        kwargs.update(_localstack_kwargs(settings.dynamodb_endpoint))
        logger.info("DynamoDB: LocalStack (%s) に接続", settings.dynamodb_endpoint)
    else:
        logger.info("DynamoDB: AWS デフォルト認証情報で接続")

    return boto3.client("dynamodb", **kwargs)


def create_dynamodb_resource(settings: Settings | None = None):
    """DynamoDB リソース (Table操作向け) を生成"""
    settings = settings or get_settings()
    kwargs = _base_client_kwargs(settings)

    if settings.dynamodb_endpoint:
        kwargs.update(_localstack_kwargs(settings.dynamodb_endpoint))

    return boto3.resource("dynamodb", **kwargs)


# -------------------------------------------------------
# S3
# -------------------------------------------------------
def create_s3_client(settings: Settings | None = None):
    """S3 クライアントを生成"""
    settings = settings or get_settings()
    kwargs = _base_client_kwargs(settings)

    if settings.s3_endpoint:
        kwargs.update(_localstack_kwargs(settings.s3_endpoint))
        kwargs["config"] = BotoConfig(s3={"addressing_style": "path"})
        logger.info("S3: LocalStack (%s) に接続", settings.s3_endpoint)
    else:
        logger.info("S3: AWS デフォルト認証情報で接続")

    return boto3.client("s3", **kwargs)


# -------------------------------------------------------
# Cognito
# -------------------------------------------------------
def create_cognito_client(settings: Settings | None = None):
    """Cognito Identity Provider クライアントを生成"""
    settings = settings or get_settings()
    kwargs = _base_client_kwargs(settings)

    if settings.aws_access_key_id and settings.aws_secret_access_key:
        kwargs["aws_access_key_id"] = settings.aws_access_key_id
        kwargs["aws_secret_access_key"] = settings.aws_secret_access_key
        logger.info("Cognito: 静的認証情報で接続")
    else:
        logger.info("Cognito: デフォルト認証情報 (Lambda IAM Role) で接続")

    return boto3.client("cognito-idp", **kwargs)


# -------------------------------------------------------
# Secrets Manager
# -------------------------------------------------------
def create_secrets_manager_client(settings: Settings | None = None):
    """Secrets Manager クライアントを生成 (Gemini API Key 取得用)"""
    settings = settings or get_settings()
    kwargs = _base_client_kwargs(settings)
    return boto3.client("secretsmanager", **kwargs)


def resolve_gemini_api_key(settings: Settings | None = None) -> str:
    """Gemini API Key を解決 (Secrets Manager or 環境変数)"""
    settings = settings or get_settings()

    if settings.gemini_api_key_secret_arn:
        logger.info("Gemini API Key: Secrets Manager から取得")
        client = create_secrets_manager_client(settings)
        response = client.get_secret_value(SecretId=settings.gemini_api_key_secret_arn)
        return response["SecretString"]

    logger.info("Gemini API Key: 環境変数から取得")
    return settings.gemini_api_key
