# Pre-Build Quality Check

このフックはビルド前にコード品質をチェックします。

## トリガー
- イベント: ユーザーがビルドコマンドを実行する前
- コマンド: `build`, `gradlew build`, `npm run build`

## チェック内容

### 全体チェック
1. デバッグコードの残存チェック
2. 未使用のimportチェック
3. コーディング規約違反チェック

### バックエンド
- Gradleビルドの実行
- Checkstyleチェック（設定されている場合）

### フロントエンド
- TypeScriptコンパイルチェック
- ESLintチェック
- Viteビルドチェック

## アクション
ビルド前にコード品質をチェックし、問題があれば警告を表示します。

---

**設定方法**: 
Kiro IDEのコマンドパレット（Ctrl+Shift+P / Cmd+Shift+P）から「Open Kiro Hook UI」を選択し、以下の設定を追加してください：

```
Trigger: On Message Sent
Message Pattern: (ビルドして|build|ビルド確認)
Action: Send Message to Agent
Message: 
ビルド前のコード品質チェックを実行してください。

1. 最近変更されたファイルのコーディング規約チェック
2. バックエンドビルド: ./gradlew build -x test
3. フロントエンドビルド: npm run build

すべてのチェックが成功したら「✅ ビルド成功」と報告してください。
問題があれば具体的に指摘し、修正案を提示してください。
```
