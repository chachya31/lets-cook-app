# AWS インフラストラクチャ

このディレクトリには、自炊支援アプリのAWSインフラストラクチャ定義が含まれています。

## 必要なAWSリソース

### 1. DynamoDB テーブル

#### Users テーブル
- **Partition Key**: UserId (String)
- **属性**: Email, Nickname, ProfileImageUrl, PreferredLanguage, LastCookingDate, CreatedAt
- **読み取り/書き込みキャパシティ**: オンデマンド

#### Recipes テーブル
- **Partition Key**: RecipeId (String)
- **Sort Key**: CreatedAt (String)
- **GSI**: 
  - GSI_Author (PK: AuthorId)
  - GSI_Category (PK: Category)
- **読み取り/書き込みキャパシティ**: オンデマンド

#### Schedules テーブル
- **Partition Key**: UserId (String)
- **Sort Key**: Date#Type#RecipeId (String)
- **読み取り/書き込みキャパシティ**: オンデマンド

#### ShoppingLists テーブル
- **Partition Key**: UserId (String)
- **Sort Key**: ItemId (String)
- **読み取り/書き込みキャパシティ**: オンデマンド

#### Reviews テーブル
- **Partition Key**: RecipeId (String)
- **Sort Key**: ReviewId (String)
- **GSI**: GSI_User (PK: UserId)
- **読み取り/書き込みキャパシティ**: オンデマンド

### 2. S3 バケット

- **バケット名**: `cooking-app-images-{環境}`
- **用途**: プロフィール画像、レシピ画像
- **設定**:
  - バージョニング: 有効
  - 暗号化: AES-256
  - パブリックアクセス: ブロック（Pre-signed URLで制御）
  - ライフサイクルポリシー: 古いバージョンを90日後に削除

### 3. Cognito ユーザープール

- **プール名**: `cooking-app-users-{環境}`
- **認証フロー**: USER_PASSWORD_AUTH
- **パスワードポリシー**:
  - 最小長: 8文字
  - 大文字、小文字、数字を必須
- **トークン有効期限**:
  - アクセストークン: 1時間
  - リフレッシュトークン: 90日
- **属性**:
  - email (必須、検証必要)
  - nickname
  - preferred_language

### 4. API Gateway

- **タイプ**: REST API
- **認証**: Cognito Authorizer
- **CORS**: 有効（フロントエンドドメインを許可）
- **ステージ**: dev, staging, production
- **スロットリング**: 
  - レート制限: 1000リクエスト/秒
  - バースト: 2000リクエスト

## デプロイ方法

### 前提条件

- AWS CLI インストール済み
- AWS認証情報設定済み
- Node.js 18以上（CDK使用時）

### AWS CDKを使用する場合

```bash
cd infrastructure/cdk
npm install
cdk bootstrap
cdk deploy --all
```

### CloudFormationを使用する場合

```bash
cd infrastructure/cloudformation
aws cloudformation create-stack \
  --stack-name cooking-app-infrastructure \
  --template-body file://template.yaml \
  --capabilities CAPABILITY_IAM
```

## ローカル開発

ローカル開発では、LocalStackを使用してAWSサービスをエミュレートします。

### LocalStackのセットアップ

```bash
# Docker Composeで起動
docker-compose up -d

# DynamoDBテーブル作成
./scripts/setup-local-dynamodb.sh

# S3バケット作成
./scripts/setup-local-s3.sh
```

### 環境変数

```bash
# ローカル開発
export AWS_ENDPOINT=http://localhost:4566
export AWS_REGION=ap-northeast-1
export AWS_ACCESS_KEY_ID=test
export AWS_SECRET_ACCESS_KEY=test

# 本番環境
export AWS_REGION=ap-northeast-1
# 認証情報はIAMロールまたはAWS CLIから取得
```

## コスト見積もり

### 初期フェーズ（10ユーザー想定）

- **DynamoDB**: 無料枠内（25GB、読み書き各25ユニット/秒）
- **S3**: 約$0.50/月（5GB、1000リクエスト）
- **Cognito**: 無料枠内（50,000 MAU）
- **API Gateway**: 約$3.50/月（100万リクエスト）
- **Lambda**: 無料枠内（100万リクエスト、400,000 GB-秒）

**合計**: 約$4/月

### スケール時（1000ユーザー想定）

- **DynamoDB**: 約$10/月
- **S3**: 約$5/月
- **Cognito**: 無料枠内
- **API Gateway**: 約$35/月
- **Lambda**: 約$5/月

**合計**: 約$55/月
