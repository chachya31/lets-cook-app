# デプロイガイド - Lambda + Amplify Hosting

このガイドでは、AWS Lambda + Amplify Hostingを使用した完全なデプロイ手順を説明します。

## 前提条件

- AWS アカウント
- AWS CLI インストール済み（`aws --version`で確認）
- Node.js 18以上（`node --version`で確認）
- Java 21以上（`java --version`で確認）
- Git（Amplify Hosting用）

## ステップ1: AWS認証情報の設定

```bash
# AWS CLIの設定
aws configure

# 入力項目:
# AWS Access Key ID: YOUR_ACCESS_KEY
# AWS Secret Access Key: YOUR_SECRET_KEY
# Default region name: ap-northeast-1
# Default output format: json

# 確認
aws sts get-caller-identity
```

## ステップ2: バックエンドのビルド

```bash
cd backend

# Gradleビルド（Lambda用のFat JAR作成）
gradlew.bat clean build shadowJar

# ビルド成功を確認
# backend/build/libs/backend-1.0.0.jar が生成されていることを確認
```

## ステップ3: CDKのブートストラップ（初回のみ）

```bash
cd infrastructure/cdk

# 依存関係のインストール
npm install

# CDKのブートストラップ
cdk bootstrap

# 確認
cdk list
# 出力例:
# CookingAppStack-Dev
# CookingAppStack-Prod
```

## ステップ4: インフラのデプロイ

```bash
# 本番環境にデプロイ
cdk deploy CookingAppStack-Prod

# または開発環境
cdk deploy CookingAppStack-Dev

# デプロイ確認のプロンプトが表示されたら "y" を入力
```

デプロイ完了後、以下の情報が出力されます：

```
Outputs:
CookingAppStack-Prod.ApiUrl = https://xxxxxxxxxx.execute-api.ap-northeast-1.amazonaws.com/prod/
CookingAppStack-Prod.UserPoolId = ap-northeast-1_xxxxxxxxx
CookingAppStack-Prod.UserPoolClientId = xxxxxxxxxxxxxxxxxxxxxxxxxx
CookingAppStack-Prod.ImagesBucketName = cooking-app-images-prod
CookingAppStack-Prod.LambdaFunctionArn = arn:aws:lambda:ap-northeast-1:xxxxxxxxxxxx:function:cooking-app-prod
```

**これらの値をメモしてください！**

## ステップ5: フロントエンドの環境変数設定

デプロイ結果を使って、フロントエンドの環境変数を設定します。

```bash
cd frontend

# .env.productionファイルを編集
```

`frontend/.env.production`を以下のように更新：

```env
VITE_API_URL=https://xxxxxxxxxx.execute-api.ap-northeast-1.amazonaws.com/prod
```

## ステップ6: フロントエンドのデプロイ（Amplify Hosting）

```bash
cd frontend

# Amplify CLIのインストール（初回のみ）
npm install -g @aws-amplify/cli

# Amplifyの初期化
amplify init

# プロンプトに従って入力:
# ? Enter a name for the project: cookingapp
# ? Enter a name for the environment: prod
# ? Choose your default editor: Visual Studio Code
# ? Choose the type of app: javascript
# ? What javascript framework: react
# ? Source Directory Path: src
# ? Distribution Directory Path: dist
# ? Build Command: npm run build
# ? Start Command: npm run dev
# ? Do you want to use an AWS profile? Yes
# ? Please choose the profile: default

# ホスティングの追加
amplify add hosting

# プロンプトに従って入力:
# ? Select the plugin module to execute: Hosting with Amplify Console
# ? Choose a type: Manual deployment

# デプロイ
amplify publish

# デプロイ完了後、URLが表示されます
# Hosting endpoint: https://prod.xxxxxxxxxxxxxx.amplifyapp.com
```

## ステップ7: 動作確認

1. **フロントエンドにアクセス**
   - Amplify URL: `https://prod.xxxxxxxxxxxxxx.amplifyapp.com`

2. **ユーザー登録**
   - 新規登録画面でアカウントを作成
   - メールアドレスに検証コードが届く

3. **API動作確認**
   - ログイン後、レシピ一覧が表示されることを確認
   - レシピの作成・編集・削除が動作することを確認

## トラブルシューティング

### Lambda関数が起動しない

```bash
# Lambda関数のログを確認
aws logs tail /aws/lambda/cooking-app-prod --follow

# エラーが表示される場合、環境変数を確認
aws lambda get-function-configuration --function-name cooking-app-prod
```

### API Gatewayでエラーが発生

```bash
# API Gatewayのログを有効化
aws apigateway update-stage \
  --rest-api-id YOUR_API_ID \
  --stage-name prod \
  --patch-operations op=replace,path=/logging/loglevel,value=INFO
```

### フロントエンドがAPIに接続できない

1. `.env.production`のAPI URLが正しいか確認
2. CORS設定を確認（CDKスタックで設定済み）
3. ブラウザの開発者ツールでネットワークエラーを確認

### ビルドエラー

```bash
# バックエンド
cd backend
gradlew.bat clean build --stacktrace

# フロントエンド
cd frontend
npm run build
```

## コスト見積もり

個人利用（月間想定）:
- **Lambda**: 無料枠内（100万リクエスト/月まで無料）
- **API Gateway**: 約$3.50（100万リクエスト）
- **DynamoDB**: 無料枠内（25GB、読み書き各25ユニット/秒まで無料）
- **S3**: 約$0.50（5GB、1000リクエスト）
- **Cognito**: 無料枠内（50,000 MAU まで無料）
- **Amplify Hosting**: 約$1（5GB転送、ビルド時間15分/月）

**合計: 月額約$5（約750円）**

## 更新・再デプロイ

### バックエンドの更新

```bash
cd backend
gradlew.bat clean build shadowJar

cd ../../infrastructure/cdk
cdk deploy CookingAppStack-Prod
```

### フロントエンドの更新

```bash
cd frontend
npm run build
amplify publish
```

## スタックの削除

```bash
cd infrastructure/cdk

# スタックを削除
cdk destroy CookingAppStack-Prod

# 確認プロンプトで "y" を入力
```

**注意**: 本番環境のDynamoDBテーブルとS3バケットは`RETAIN`ポリシーで保護されているため、手動で削除する必要があります。

## 参考リンク

- [AWS Lambda Documentation](https://docs.aws.amazon.com/lambda/)
- [AWS Amplify Hosting](https://docs.amplify.aws/)
- [Spring Boot on AWS Lambda](https://github.com/awslabs/aws-serverless-java-container)
- [AWS CDK Documentation](https://docs.aws.amazon.com/cdk/)
