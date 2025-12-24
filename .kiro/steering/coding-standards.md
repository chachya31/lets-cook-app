---
inclusion: always
---

# コーディング規約（参照）

このプロジェクトのコーディング規約は `CODING_STANDARDS.md` で管理されています。

## 📋 コーディング規約ドキュメント

詳細なコーディング規約は以下のファイルを参照してください：

**#[[file:../../CODING_STANDARDS.md]]**

## 🎯 主要なルール（クイックリファレンス）

### バックエンド（Java）
- ❌ `@Slf4j` 禁止 → ✅ `Logger` 直接定義
- ❌ `System.getProperty()` 禁止 → ✅ `@Value` アノテーション
- ❌ `System.out.println()` 禁止 → ✅ `log.info()`
- ❌ ワイルドカードimport禁止 (`import java.util.*`) → ✅ 個別import (`import java.util.List`)
- ❌ `public` フィールド禁止 → ✅ `private final` フィールド
- ✅ Gradleコマンドは `./gradlew.bat` を使用（例: `./gradlew.bat build`）

### フロントエンド（TypeScript）
- ❌ `axios` 禁止 → ✅ `apiClient.ts` のヘルパー関数
- ❌ `fetch` 直接使用禁止 → ✅ `apiGet()`, `apiPost()` 等
- ❌ `console.log()` 禁止（本番コード）
- ❌ `let` 禁止（再代入不要な場合） → ✅ `const` 優先
- ❌ `any` 型禁止 → ✅ 具体的な型定義 or `unknown`

### 共通
- ✅ コミット前にビルド確認
- ✅ デバッグコード削除
- ✅ 未使用import削除

## 🔍 コード検証

コーディング規約の検証は以下のコマンドで実行できます：

- **保存時チェック**: チャットで「保存されたファイルをチェック」
- **ビルド前チェック**: チャットで「ビルドして」
- **コミット前チェック**: チャットで「コミット前チェック」
- **コードレビュー**: チャットで「レビュー」

詳細は `.kiro/hooks/README.md` を参照してください。
