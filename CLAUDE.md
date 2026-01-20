# 開発ルール

## バックエンド開発ルール

### 技術スタック
- 言語: Java 17
- フレームワーク: Spring Boot 3.x
- ビルドツール: Gradle (Groovy DSL)
- テスト: JUnit 5, Mockito
- データベース: DynamoDB (AWS SDK 2.x経由)

### アーキテクチャ
- パターン: Clean Architecture
- レイヤー構成:
  - `presentation`: Controllers, DTOs
  - `application`: Use Cases
  - `domain`: Entities, Repository Interfaces
  - `infrastructure`: Repository Impls, Config
- 依存関係ルール: 内側のレイヤー (domain) は外側のレイヤーに依存してはならない

### コーディング規約
- Lombok: `@Builder`, `@Getter`, `@AllArgsConstructor` のみ使用可。`@Data` は使用禁止
- テスト: ドメインロジックとユースケースには単体テスト必須

---

## フロントエンド開発ルール

### 技術スタック
- 言語: TypeScript
- フレームワーク: React 18+
- ビルドツール: Vite
- 状態管理: Zustand
- ルーティング: React Router v7
- スタイリング: Tailwind CSS
- UIライブラリ: shadcn/ui
- HTTPクライアント: Axios

### アーキテクチャ
- パターン: Feature-based structure（機能ベース構造）
- ディレクトリ構成:
  ```
  src/
    ├── features/
    │   ├── auth/ (components, api, stores)
    │   └── misc/ (LandingPage, etc.)
    ├── components/ (共有UI: Button, Layout)
    ├── lib/ (axios.ts, etc.)
    └── routes/ (index.tsx)
  ```
- Store配置: `src/features/**/stores/`
- 機能の独立性: 各featureは自己完結型であること

### コーディング規約
- React Router: `createBrowserRouter` を使用 (React Router v7スタイル)
- Zustand: ストアは最小限かつ機能固有に保つ
- Axios: `baseURL: /api` で設定
- TypeScript: Strict mode有効、`any` 型の使用禁止
- コンポーネント: 関数コンポーネント + TypeScript interfaceでprops定義

### コードスタイル
- **クォート**: シングルクォート (`'`) を使用。ダブルクォートは使用しない
- **セミコロン**: 文末にセミコロンを付けない
- **一貫性**: `shadcn/ui` コンポーネント追加時は、このスタイルに変換すること

### UI/UXガイドライン
- **UIライブラリ**: `shadcn/ui` を標準コンポーネントライブラリとして使用。カスタムボタンや入力フォームは作成せず、常にshadcnコンポーネント (`@/components/ui/*`) を使用する
- **レスポンシブデザイン**: **Mobile First** を厳守。すべてのレイアウトはモバイル画面 (320px〜) を優先し、md/lg ブレークポイントでデスクトップ向けに拡張する
- **テーマ**:
  - ベース: Zinc (ニュートラル)
  - プライマリカラー: Emerald Green (健康的でポジティブな印象)
  - Tailwindクラスを使用し、ハードコードされた色 (hex値) は可能な限り避ける
