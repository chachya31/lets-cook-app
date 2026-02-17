import logging
import sys

from pythonjsonlogger.json import JsonFormatter


def setup_logging(environment: str = "local") -> None:
    """
    ログ設定を初期化する。

    - local: 人間が読みやすいテキスト形式 (DEBUG レベル)
    - dev/prod: CloudWatch 向け JSON 形式 (INFO レベル)
    """
    root_logger = logging.getLogger()

    # 既存ハンドラをクリア (Lambda再起動時の重複防止)
    root_logger.handlers.clear()

    handler = logging.StreamHandler(sys.stdout)

    if environment == "local":
        _configure_local(handler)
    else:
        _configure_cloud(handler)

    root_logger.addHandler(handler)


def _configure_local(handler: logging.StreamHandler) -> None:
    """ローカル開発環境: テキスト形式、詳細ログ"""

    # フォーマット: Java版の LOCAL appender と同等
    formatter = logging.Formatter(
        fmt="%(asctime)s [%(threadName)s] %(levelname)-5s %(name)s - %(message)s",
        datefmt="%Y-%m-%d %H:%M:%S",
    )
    formatter.default_msec_format = "%s.%03d"

    handler.setFormatter(formatter)

    # アプリケーションログ: DEBUG
    logging.getLogger("app").setLevel(logging.DEBUG)

    # uvicorn: DEBUG (Spring Web 相当)
    logging.getLogger("uvicorn").setLevel(logging.DEBUG)

    # boto3 / botocore: INFO (AWS SDK 相当)
    logging.getLogger("boto3").setLevel(logging.INFO)
    logging.getLogger("botocore").setLevel(logging.INFO)
    logging.getLogger("urllib3").setLevel(logging.INFO)

    # ルートロガー: INFO
    logging.getLogger().setLevel(logging.INFO)


def _configure_cloud(handler: logging.StreamHandler) -> None:
    """Dev/Prod 環境: JSON 形式、CloudWatch 向け"""

    formatter = JsonFormatter(
        fmt="%(asctime)s %(levelname)s %(name)s %(message)s",
        rename_fields={"asctime": "timestamp", "levelname": "level", "name": "logger"},
    )
    handler.setFormatter(formatter)

    # アプリケーションログ: INFO
    logging.getLogger("app").setLevel(logging.INFO)

    # uvicorn: INFO
    logging.getLogger("uvicorn").setLevel(logging.INFO)

    # boto3 / botocore: WARN (AWS SDK 相当)
    logging.getLogger("boto3").setLevel(logging.WARNING)
    logging.getLogger("botocore").setLevel(logging.WARNING)
    logging.getLogger("urllib3").setLevel(logging.WARNING)

    # ルートロガー: WARN
    logging.getLogger().setLevel(logging.WARNING)
