"""
AWS Lambda エントリーポイント。
Java版の StreamLambdaHandler に対応。
Mangum が FastAPI (ASGI) を Lambda イベントに変換する。
"""

from mangum import Mangum

from app.main import app

handler = Mangum(app, lifespan="off")
