# クイックデプロイガイド

個人利用向けの最短デプロイ手順です。

## 📋 事前準備チェックリスト

- [ ] AWS アカウント作成済み
- [ ] AWS CLI インストール済み（`aws --version`）
- [ ] Node.js 18以上（`node --version`）
- [ ] Java 21以上（`java --version`）

## 🚀 デプロイ手順（5ステップ）

### 1. AWS認証情報の設定

```bash
aws configure
```

入力項目:
- AWS Access Key ID: （AWSコンソールで作成）
- AWS Secret Access Key: （AWSコンソールで作成）
- Default region name: `ap-northeast-1`
- Default output format: `json`

### 2. バックエンドのビルド

```bash
cd backend
gradlew.bat clean build shadowJar
```

✅ `backend/build/libs/backend-1.0.0.jar` が生成されることを確認

### 3. CDKでインフラをデプロイ

```bash
cd infrastructure/cdk
npm install
cdk bootstrap
cdk deploy CookingAppStack-Prod
```

⚠️ デプロイ確認で `y` を入力

📝 出力される以下の値をメモ:
- `ApiUrl`
- `UserPoolId`
- `UserPoolClientId`
- `ImagesBucketName`

### 4. フロントエンドの環境変数設定

`frontend/.env.production` を編集:

```env
VITE_API_URL=https://xxxxxxxxxx.execute-api.ap-northeast-1.amazonaws.com/prod
```

### 5. フロントエンドのデプロイ

#### オプションA: Amplify CLI（推奨）

```bash
cd frontend
npm install -g @aws-amplify/cli
amplify init
amplify add hosting
amplify publish
```

#### オプションB: 手動デプロイ（S3）

```bash
cd frontend
npm run build

# S3バケット作成
aws s3 mb s3://cooking-app-frontend-prod

# 静的ウェブサイトホスティング有効化
aws s3 website s3://cooking-app-frontend-prod --index-document index.html

# アップロード
aws s3 sync dist/ s3://cooking-app-frontend-prod --acl public-read
```

アクセスURL: `http://cooking-app-frontend-prod.s3-website-ap-northeast-1.amazonaws.com`

## ✅ 動作確認

1. フロントエンドURLにアクセス
2. ユーザー登録（メールアドレスに検証コードが届く）
3. ログイン
4. レシピの作成・閲覧を確認

## 💰 月額コスト

個人利用なら **約$5（約750円）/月**

- Lambda: 無料枠内
- API Gateway: $3.50
- DynamoDB: 無料枠内
- S3: $0.50
- Cognito: 無料枠内
- Amplify: $1

## 🔄 更新方法

### バックエンド更新

```bash
cd backend
gradlew.bat clean build shadowJar
cd ../infrastructure/cdk
cdk deploy CookingAppStack-Prod
```

### フロントエンド更新

```bash
cd frontend
amplify publish

# または S3の場合
npm run build
aws s3 sync dist/ s3://cooking-app-frontend-prod --acl public-read --delete
```

## 🗑️ 削除方法

```bash
cd infrastructure/cdk
cdk destroy CookingAppStack-Prod
```

⚠️ DynamoDBとS3は手動削除が必要（データ保護のため）

## 📚 詳細ガイド

詳しい手順やトラブルシューティングは `DEPLOY_GUIDE.md` を参照してください。
