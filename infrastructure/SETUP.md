# AWS インフラストラクチャ セットアップガイド

## 目次

1. [ローカル開発環境のセットアップ](#ローカル開発環境のセットアップ)
2. [AWS本番環境のセットアップ](#aws本番環境のセットアップ)
3. [トラブルシューティング](#トラブルシューティング)

## ローカル開発環境のセットアップ

ローカル開発では、LocalStackを使用してAWSサービスをエミュレートします。

### 前提条件

- Docker Desktop インストール済み
- Docker Compose インストール済み

### 手順

1. **LocalStackの起動**

```bash
cd infrastructure
docker-compose up -d
```

2. **初期化スクリプトの実行確認**

LocalStackが起動すると、自動的に以下が実行されます：
- DynamoDBテーブルの作成
- S3バケットの作成

3. **動作確認**

```bash
# DynamoDBテーブル一覧を確認
aws dynamodb list-tables --endpoint-url http://localhost:4566

# S3バケット一覧を確認
aws s3 ls --endpoint-url http://localhost:4566
```

4. **バックエンドの環境変数設定**

`backend/src/main/resources/application-local.yml`を作成：

```yaml
aws:
  region: ap-northeast-1
  dynamodb:
    endpoint: http://localhost:4566
  s3:
    endpoint: http://localhost:4566
    bucket: cooking-app-images-local
  cognito:
    endpoint: http://localhost:4566
    userPoolId: local-user-pool
    clientId: local-client-id

spring:
  profiles:
    active: local
```

5. **バックエンドの起動**

```bash
cd backend
./gradlew bootRun --args='--spring.profiles.active=local'
```

## AWS本番環境のセットアップ

### 前提条件

- AWS アカウント
- AWS CLI インストール済み
- AWS認証情報設定済み (`aws configure`)
- Node.js 18以上

### オプション1: AWS CDKを使用

1. **CDKのインストール**

```bash
npm install -g aws-cdk
```

2. **依存関係のインストール**

```bash
cd infrastructure/cdk
npm install
```

3. **CDKのブートストラップ（初回のみ）**

```bash
cdk bootstrap aws://ACCOUNT-ID/REGION
```

4. **デプロイ**

```bash
# 開発環境
cdk deploy CookingAppStack-Dev

# 本番環境
cdk deploy CookingAppStack-Prod
```

5. **出力の確認**

デプロイ完了後、以下の情報が出力されます：
- Cognito User Pool ID
- API Gateway URL

これらの値を`backend/src/main/resources/application-prod.yml`に設定してください。

### オプション2: AWSマネジメントコンソールを使用

#### 1. DynamoDBテーブルの作成

各テーブルを以下の設定で作成：

**Users テーブル**
- テーブル名: `cooking-app-users-prod`
- パーティションキー: `UserId` (String)
- 読み取り/書き込みキャパシティ: オンデマンド

**Recipes テーブル**
- テーブル名: `cooking-app-recipes-prod`
- パーティションキー: `RecipeId` (String)
- ソートキー: `CreatedAt` (String)
- GSI:
  - `GSI_Author`: PK=`AuthorId`, SK=`CreatedAt`
  - `GSI_Category`: PK=`Category`, SK=`CreatedAt`

（他のテーブルも同様に作成）

#### 2. S3バケットの作成

1. S3コンソールで「バケットを作成」
2. バケット名: `cooking-app-images-prod`
3. リージョン: `ap-northeast-1`
4. 設定:
   - バージョニング: 有効化
   - デフォルト暗号化: 有効化（SSE-S3）
   - パブリックアクセス: すべてブロック
5. CORSポリシーを設定：

```json
[
  {
    "AllowedHeaders": ["*"],
    "AllowedMethods": ["GET", "PUT", "POST"],
    "AllowedOrigins": ["https://your-frontend-domain.com"],
    "ExposeHeaders": []
  }
]
```

#### 3. Cognitoユーザープールの作成

1. Cognitoコンソールで「ユーザープールを作成」
2. サインインオプション: メールアドレス
3. パスワードポリシー:
   - 最小長: 8文字
   - 大文字、小文字、数字を必須
4. MFA: オプション（推奨: 有効）
5. アプリクライアントを作成:
   - 認証フロー: `USER_PASSWORD_AUTH`を有効化
   - トークン有効期限:
     - アクセストークン: 1時間
     - リフレッシュトークン: 90日

#### 4. API Gatewayの作成

1. API Gatewayコンソールで「REST API」を作成
2. API名: `cooking-app-api-prod`
3. Cognito Authorizerを設定
4. CORSを有効化
5. ステージを作成: `prod`
6. スロットリング設定:
   - レート: 1000リクエスト/秒
   - バースト: 2000リクエスト

### 環境変数の設定

デプロイ後、以下の環境変数をバックエンドに設定：

```yaml
# application-prod.yml
aws:
  region: ap-northeast-1
  dynamodb:
    endpoint: # 空欄（デフォルトエンドポイントを使用）
  s3:
    bucket: cooking-app-images-prod
  cognito:
    userPoolId: <YOUR_USER_POOL_ID>
    clientId: <YOUR_CLIENT_ID>
```

## トラブルシューティング

### LocalStackが起動しない

```bash
# Dockerログを確認
docker logs cooking-app-localstack

# コンテナを再起動
docker-compose down
docker-compose up -d
```

### DynamoDBテーブルが作成されない

```bash
# 手動で初期化スクリプトを実行
docker exec cooking-app-localstack /etc/localstack/init/ready.d/01-create-dynamodb-tables.sh
```

### DynamoDBテーブルの手動再作成（Windows）

LocalStackを再起動してテーブルが消えた場合、以下のコマンドで再作成できます：

```cmd
@REM Users テーブル
aws --endpoint-url=http://localhost:4566 dynamodb create-table --table-name cooking-app-users-local --attribute-definitions AttributeName=UserId,AttributeType=S --key-schema AttributeName=UserId,KeyType=HASH --billing-mode PAY_PER_REQUEST

@REM Recipes テーブル
aws --endpoint-url=http://localhost:4566 dynamodb create-table --table-name cooking-app-recipes-local --attribute-definitions AttributeName=RecipeId,AttributeType=S AttributeName=AuthorId,AttributeType=S AttributeName=CreatedAt,AttributeType=S --key-schema AttributeName=RecipeId,KeyType=HASH --global-secondary-indexes "[{\"IndexName\":\"GSI_Author\",\"KeySchema\":[{\"AttributeName\":\"AuthorId\",\"KeyType\":\"HASH\"},{\"AttributeName\":\"CreatedAt\",\"KeyType\":\"RANGE\"}],\"Projection\":{\"ProjectionType\":\"ALL\"}}]" --billing-mode PAY_PER_REQUEST

@REM Schedules テーブル
aws --endpoint-url=http://localhost:4566 dynamodb create-table --table-name cooking-app-schedules-local --attribute-definitions AttributeName=UserId,AttributeType=S AttributeName=DateRecipeId,AttributeType=S --key-schema AttributeName=UserId,KeyType=HASH AttributeName=DateRecipeId,KeyType=RANGE --billing-mode PAY_PER_REQUEST

@REM ShoppingLists テーブル
aws --endpoint-url=http://localhost:4566 dynamodb create-table --table-name cooking-app-shopping-lists-local --attribute-definitions AttributeName=UserId,AttributeType=S AttributeName=ItemId,AttributeType=S AttributeName=NormalizedKey,AttributeType=S --key-schema AttributeName=UserId,KeyType=HASH AttributeName=ItemId,KeyType=RANGE --global-secondary-indexes "[{\"IndexName\":\"GSI_NormalizedKey\",\"KeySchema\":[{\"AttributeName\":\"UserId\",\"KeyType\":\"HASH\"},{\"AttributeName\":\"NormalizedKey\",\"KeyType\":\"RANGE\"}],\"Projection\":{\"ProjectionType\":\"ALL\"}}]" --billing-mode PAY_PER_REQUEST

@REM Reviews テーブル
aws --endpoint-url=http://localhost:4566 dynamodb create-table --table-name cooking-app-reviews-local --attribute-definitions AttributeName=RecipeId,AttributeType=S AttributeName=ReviewId,AttributeType=S AttributeName=UserId,AttributeType=S AttributeName=CreatedAt,AttributeType=S --key-schema AttributeName=RecipeId,KeyType=HASH AttributeName=ReviewId,KeyType=RANGE --global-secondary-indexes "[{\"IndexName\":\"GSI_User\",\"KeySchema\":[{\"AttributeName\":\"UserId\",\"KeyType\":\"HASH\"},{\"AttributeName\":\"CreatedAt\",\"KeyType\":\"RANGE\"}],\"Projection\":{\"ProjectionType\":\"ALL\"}}]" --billing-mode PAY_PER_REQUEST

@REM 作成確認
aws --endpoint-url=http://localhost:4566 dynamodb list-tables
```

> **Note**: `PERSISTENCE=1`を設定済みのため、通常はLocalStack再起動後もデータが保持されます。ただし、Dockerボリュームを削除した場合は再作成が必要です。

### AWS CDKデプロイエラー

```bash
# スタックの状態を確認
cdk list

# 詳細なログを表示
cdk deploy --verbose
```

### 認証情報エラー

```bash
# AWS認証情報を確認
aws sts get-caller-identity

# 認証情報を再設定
aws configure
```

## 参考リンク

- [LocalStack Documentation](https://docs.localstack.cloud/)
- [AWS CDK Documentation](https://docs.aws.amazon.com/cdk/)
- [DynamoDB Best Practices](https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/best-practices.html)
- [S3 Security Best Practices](https://docs.aws.amazon.com/AmazonS3/latest/userguide/security-best-practices.html)
