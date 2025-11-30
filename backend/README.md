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
│   │   │   │   ├── controller/    # REST APIコントローラー
│   │   │   │   ├── dto/           # リクエスト/レスポンスDTO
│   │   │   │   └── exception/     # グローバル例外ハンドラー
│   │   │   ├── application/       # アプリケーション層
│   │   │   │   ├── usecase/       # ユースケース実装
│   │   │   │   └── validation/    # バリデーション
│   │   │   ├── domain/            # ドメイン層
│   │   │   │   ├── entity/        # エンティティ
│   │   │   │   ├── valueobject/   # バリューオブジェクト
│   │   │   │   ├── repository/    # リポジトリインターフェース
│   │   │   │   └── exception/     # ドメイン例外
│   │   │   └── infrastructure/    # インフラストラクチャ層
│   │   │       ├── repository/    # リポジトリ実装
│   │   │       ├── external/      # 外部API統合
│   │   │       └── config/        # 設定クラス
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-local.yml
│   │       └── messages/          # 多言語メッセージ
│   └── test/
│       └── java/com/cookingapp/
│           ├── unit/              # ユニットテスト
│           └── property/          # プロパティベーステスト
└── build.gradle
```

## 実装済み機能

### 1. ユーザー管理機能
- ユーザー登録（Cognito + DynamoDB）
- ログイン（Cognito認証）
- プロフィール取得・更新
- アカウント削除
- メール確認機能
- プロフィール画像アップロード（S3）

### 2. レシピ管理機能
- レシピ作成・更新・削除（論理削除）
- レシピ詳細取得
- レシピ検索（キーワード、作成者）
- レシピ画像アップロード（S3）
- 食材管理（Ingredient Value Object）
- 単位管理（Unit Enum）

### 3. レビュー機能
- レビュー作成・更新・削除
- レシピIDでレビュー一覧取得
- レビュー通報（通報カウント増加、3回以上で自動非表示）
- 権限チェック（自分のレビューのみ編集・削除可能）
- 表示可能なレビューのみフィルタリング

### 4. 画像管理機能
- S3統合（LocalStack対応）
- 画像バリデーション（サイズ、フォーマット）
- Pre-signed URL生成
- 画像アップロード・取得・削除
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
