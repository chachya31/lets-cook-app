# Cooking Support App - Frontend

自炊支援・食費節約アプリケーションのフロントエンドです。

## 技術スタック

- React 18
- TypeScript
- Redux Toolkit
- React Router
- shadcn/ui + Radix UI (UIコンポーネント)
- Tailwind CSS (スタイリング)
- i18next (多言語対応)
- Vite
- fast-check (Property-based testing)

## プロジェクト構造

```
frontend/
├── src/
│   ├── components/        # Reactコンポーネント
│   │   ├── ui/            # shadcn/uiコンポーネント
│   │   ├── auth/          # 認証関連
│   │   ├── dashboard/     # ダッシュボード
│   │   ├── recipe/        # レシピ関連
│   │   ├── schedule/      # スケジュール関連
│   │   ├── shopping/      # 買い物リスト
│   │   ├── profile/       # プロフィール
│   │   ├── admin/         # 管理者機能
│   │   └── common/        # 共通コンポーネント
│   ├── lib/               # ライブラリユーティリティ
│   ├── store/             # Redux状態管理
│   ├── api/               # API呼び出し
│   ├── hooks/             # カスタムフック
│   ├── utils/             # ユーティリティ関数
│   ├── types/             # TypeScript型定義
│   └── i18n/              # 多言語対応
└── tests/
    ├── unit/              # ユニットテスト
    └── property/          # プロパティベーステスト
```

## セットアップ

```bash
# 依存関係のインストール
npm install

# 開発サーバー起動
npm run dev

# ビルド
npm run build

# テスト実行
npm test

# Lint実行
npm run lint
```

## 環境変数

`.env`ファイルを作成し、以下の環境変数を設定してください：

```
VITE_API_BASE_URL=http://localhost:8080
```

## 多言語対応

- 日本語（ja）
- 韓国語（ko）

翻訳ファイルは`src/i18n/locales/`に配置されています。

## UIコンポーネント

本プロジェクトでは**shadcn/ui**を使用しています。

- **完全無料**: MITライセンス、費用は一切発生しません
- **アクセシブル**: Radix UIベースでWCAG 2.1準拠
- **カスタマイズ可能**: Tailwind CSSで簡単にスタイル調整
- **TypeScript対応**: 完全な型サポート

主要コンポーネント：
- Button, Card, Dialog, Form, Input, Select, Checkbox, Toast, Calendar, Label

テーマカラー：
- プライマリ: 明るい緑 (`hsl(142, 76%, 36%)`)
- CSS変数ベースでライト/ダークモード対応
