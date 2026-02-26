# Pre-Commit Quality Check

このフックはGitコミット前にコード品質をチェックします。

## トリガー
- イベント: Gitコミット前
- コマンド: `git commit`, `コミット`

## チェック内容

### 必須チェック項目
1. ✅ ビルドが成功すること
2. ✅ ESLint/Checkstyleエラーがないこと
3. ✅ デバッグコードが削除されていること
4. ✅ 未使用のimportが削除されていること
5. ✅ コーディング規約に違反していないこと

### バックエンドチェック
```bash
cd backend && ./gradlew build -x test
```

### フロントエンドチェック
```bash
cd frontend && npm run build
```

## アクション
コミット前にすべてのチェックを実行し、問題があればコミットを中止します。

---

**設定方法**: 
Kiro IDEのコマンドパレット（Ctrl+Shift+P / Cmd+Shift+P）から「Open Kiro Hook UI」を選択し、以下の設定を追加してください：

```
Trigger: On Message Sent
Message Pattern: (コミット前チェック|pre-commit|commit check)
Action: Send Message to Agent
Message: 
コミット前の品質チェックを実行してください。

チェック項目：
1. 変更されたファイルのコーディング規約チェック
2. デバッグコード（console.log, System.out.println）の残存チェック
3. 未使用のimportチェック
4. バックエンドビルド: cd backend && ./gradlew build -x test
5. フロントエンドビルド: cd frontend && npm run build

すべてのチェックが成功したら「✅ コミット可能」と報告してください。
問題があれば具体的に指摘し、修正してからコミットするよう警告してください。
```
