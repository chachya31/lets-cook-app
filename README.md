# 自炊支援・食費節約アプリ

一人暮らしまたは2〜4人家族の世帯を対象とした自炊支援・食費節約Webアプリケーションです。

## 概要

本アプリケーションは、Uber Eatsなどのデリバリーサービス依存からの脱却を支援し、簡単なレシピ提供により料理への心理的ハードルを下げ、食材の賞味期限や保管方法を提示することで計画的な買い物を促進し、食品ロスを削減します。

## 主要機能

- **レシピ検索・編集**: カテゴリ、食材、調理時間でレシピを検索
- **スケジュール管理**: 料理予定と実績をカレンダーで管理
- **買い物リスト**: レシピから食材を買い物リストに追加
- **AIアドバイザー**: 食材の保存法や代替食材を提案
- **サボり防止アラート**: 料理をしていない期間が続いたら警告
- **多言語対応**: 日本語・韓国語に対応

## 技術スタック

### バックエンド
- Java 21+ (Java 23推奨)
- Spring Boot 3.2.0
- Gradle 8.11.1
- AWS SDK (DynamoDB, S3, Cognito)
- Clean Architecture

### フロントエンド
- React 18
- TypeScript
- Redux Toolkit
- React Router
- i18next
- Vite

### インフラ
- AWS Lambda
- Amazon DynamoDB
- Amazon S3
- Amazon Cognito
- API Gateway

## プロジェクト構造

```
.
├── backend/           # バックエンド（Spring Boot）
├── frontend/          # フロントエンド（React + TypeScript）
├── infrastructure/    # インフラストラクチャ（AWS CDK + LocalStack）
├── docs/              # ドキュメント
└── .kiro/specs/       # 仕様書
```

## セットアップ

### ローカル開発環境

#### 1. LocalStackの起動（AWSサービスのエミュレーション）

```bash
cd infrastructure
docker-compose up -d
```

詳細は`infrastructure/SETUP.md`を参照してください。

#### 2. バックエンド

```bash
cd backend
./gradlew.bat bootRun --args='--spring.profiles.active=local'
```

#### 3. フロントエンド

```bash
cd frontend
npm install
npm run dev
```

### 本番環境へのデプロイ

```bash
cd infrastructure/cdk
cdk deploy CookingAppStack-Prod
```

詳細は各ディレクトリのREADMEを参照してください。

## テスト

### バックエンド

```bash
cd backend
./gradlew test
```

### フロントエンド

```bash
cd frontend
npm test
```

## ライセンス

Private
