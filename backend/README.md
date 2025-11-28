# Cooking Support App - Backend

自炊支援・食費節約アプリケーションのバックエンドサービスです。

## 技術スタック

- Java 21+ (Java 23推奨)
- Spring Boot 3.2.0
- Gradle 8.11.1
- AWS SDK (DynamoDB, S3, Cognito)
- jqwik (Property-based testing)

## プロジェクト構造

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/cookingapp/
│   │   │   ├── presentation/      # プレゼンテーション層
│   │   │   ├── application/       # アプリケーション層
│   │   │   ├── domain/            # ドメイン層
│   │   │   └── infrastructure/    # インフラストラクチャ層
│   │   └── resources/
│   │       ├── application.yml
│   │       └── messages/          # 多言語メッセージ
│   └── test/
│       └── java/com/cookingapp/
│           ├── unit/              # ユニットテスト
│           └── property/          # プロパティベーステスト
└── build.gradle
```

## ビルドと実行

```bash
# ビルド
./gradlew build

# テスト実行
./gradlew test

# アプリケーション起動
./gradlew bootRun
```

## 環境変数

- `AWS_DYNAMODB_ENDPOINT`: DynamoDBエンドポイント（ローカル開発用）
- `AWS_S3_BUCKET`: S3バケット名
- `AWS_COGNITO_USER_POOL_ID`: Cognitoユーザープール ID
- `AWS_COGNITO_CLIENT_ID`: Cognitoクライアント ID
