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
├── backend/                          # バックエンド（Spring Boot / Clean Architecture）
│   └── src/main/java/com/cookingapp/
│       ├── application/              # ユースケース層
│       ├── domain/                   # ドメインモデル・リポジトリIF
│       ├── infrastructure/           # DynamoDB/S3/Cognito アダプタ
│       └── presentation/             # REST コントローラ
├── frontend/                         # フロントエンド（React + TypeScript + Vite）
│   └── src/
│       ├── components/               # 画面別コンポーネント（auth, dashboard, recipe, schedule, chat ほか）
│       ├── store/                    # Redux Toolkit ストア
│       ├── api/                      # API クライアント
│       ├── hooks/                    # カスタムフック
│       ├── i18n/locales/             # 多言語リソース（ja, ko）
│       └── types/                    # 型定義
├── infrastructure/                   # インフラ（AWS CDK + LocalStack）
│   ├── cdk/                          # 本番AWS環境用 CDK スタック
│   └── localstack/                   # LocalStack 初期化スクリプト
├── docs/                             # ドキュメント（ポートフォリオ・再構築ガイド等）
├── .kiro/
│   ├── specs/                        # 仕様書
│   ├── hooks/                        # Kiro Agent Hooks設定
│   └── steering/                     # Kiroステアリングルール
├── CODING_STANDARDS.md               # コーディング規約
└── README.md                         # このファイル
```

## ドキュメント

- **[セットアップガイド](SETUP_GUIDE.md)** - macOS/Windows環境別セットアップ手順
- **[コーディング規約](CODING_STANDARDS.md)** - プロジェクトのコーディング規約
- **[設計書](.kiro/specs/cooking-support-app/design.md)** - システム設計書
- **[タスク管理](.kiro/specs/cooking-support-app/tasks.md)** - 実装タスク一覧
- **[Kiro Hooks](.kiro/hooks/README.md)** - コード品質チェックの自動化設定

## セットアップ

### 推奨IDE拡張機能

プロジェクトを開くと、推奨される拡張機能のインストールを促すメッセージが表示されます。
`.vscode/extensions.json`に定義された拡張機能をインストールしてください。

**必須拡張機能**:
- Extension Pack for Java (Microsoft)
- Spring Boot Extension Pack (VMware)
- Gradle for Java (Microsoft)
- Lombok Annotations Support

**推奨拡張機能**:
- ESLint, Prettier (フロントエンド)
- Tailwind CSS IntelliSense
- GitLens, SonarLint

### ローカル開発環境

#### 1. LocalStackの起動（AWSサービスのエミュレーション）

```bash
cd infrastructure
docker-compose up -d
```

詳細は`infrastructure/SETUP.md`を参照してください。

#### 2. バックエンド

**Windows**:
```bash
cd backend
gradlew.bat bootRun --args='--spring.profiles.active=local'
```

**macOS/Linux**:
```bash
cd backend
./gradlew bootRun --args='--spring.profiles.active=local'
```

**IDE (Kiro/VS Code)**:
- Run and Debug パネル (`⇧⌘D`) を開く
- "Spring Boot (Local)" を選択
- F5 または緑の再生ボタンをクリック

#### 3. フロントエンド

```bash
cd frontend
npm install
npm run dev
```

### 本番環境へのデプロイ

#### クイックデプロイ（推奨）

```bash
# 1. バックエンドのビルド
cd backend
gradlew.bat clean build shadowJar

# 2. インフラのデプロイ（自動チェック付き）
cd ../infrastructure/cdk
pre-deploy.bat

# 3. フロントエンドのデプロイ
cd ../../frontend
npm install -g @aws-amplify/cli
amplify init
amplify add hosting
amplify publish
```

詳細は以下を参照：
- **クイックガイド**: `infrastructure/QUICK_DEPLOY.md`
- **完全ガイド**: `infrastructure/DEPLOY_GUIDE.md`

#### 月額コスト

個人利用なら **約$5（約750円）/月**
- Lambda、DynamoDB、Cognitoは無料枠内
- API Gateway: $3.50、S3: $0.50、Amplify: $1

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
