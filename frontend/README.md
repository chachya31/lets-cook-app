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
│   │   ├── auth/          # 認証関連（実装済み）
│   │   ├── recipe/        # レシピ関連（実装済み）
│   │   ├── common/        # 共通コンポーネント（実装済み）
│   │   ├── dashboard/     # ダッシュボード（未実装）
│   │   ├── schedule/      # スケジュール関連（未実装）
│   │   ├── shopping/      # 買い物リスト（未実装）
│   │   ├── profile/       # プロフィール（未実装）
│   │   └── admin/         # 管理者機能（未実装）
│   ├── lib/               # ライブラリユーティリティ（実装済み）
│   ├── store/             # Redux状態管理（実装済み）
│   ├── api/               # API呼び出し（実装済み）
│   ├── hooks/             # カスタムフック（実装済み）
│   ├── utils/             # ユーティリティ関数（実装済み）
│   ├── types/             # TypeScript型定義（実装済み）
│   └── i18n/              # 多言語対応（実装済み）
└── tests/
    ├── unit/              # ユニットテスト（未実装）
    └── property/          # プロパティベーステスト（未実装）
```

## 実装済み機能

### 1. 認証機能
- ログイン（Login/: LoginPage + loginFormConfig）
- ユーザー登録（Register/: RegisterPage + registerFormConfig）
- メール確認（ConfirmEmail/: ConfirmEmailPage + confirmEmailFormConfig）
- Redux状態管理（authSlice）
- カスタムフック（useAuth、useForm）

### 2. レシピ機能
- レシピ検索（RecipeSearchPage）
- レシピ詳細（RecipeDetailPage）
- レシピ編集（RecipeEdit/: RecipeEditPage + useRecipeEditHandlers + recipeEditConfig）
- Redux状態管理（recipeSlice）
- API呼び出し（recipeApi）

### 3. レビュー機能
- レビュー一覧（ReviewList）
- レビュー投稿フォーム（ReviewForm）
- 星評価選択（1-5）
- コメント入力（300文字以内）
- 編集・削除・通報機能
- Redux状態管理（reviewSlice）
- API呼び出し（reviewApi）
- カスタムフック（useReview）

### 4. 共通コンポーネント
- FormField: 再利用可能なフォームフィールド
- ImageUploader: ドラッグ&ドロップ対応画像アップローダー
- shadcn/uiコンポーネント（Button、Card、Input、Label、Textarea）
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
