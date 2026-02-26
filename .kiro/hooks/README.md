# Kiro Agent Hooks 設定ガイド

このディレクトリには、Kiro IDEで使用するAgent Hooksの設定が含まれています。

## 📋 利用可能なフック

### 1. **Code Quality Check on Save** (`on-save-quality-check.md`)
ファイル保存時に自動的にコーディング規約をチェックします。

**使い方**: 
チャットで「保存されたファイルをチェック」と入力

### 2. **Pre-Build Check** (`pre-build-check.md`)
ビルド前にコード品質をチェックします。

**使い方**: 
チャットで「ビルドして」または「build」と入力

### 3. **Pre-Commit Check** (`pre-commit-check.md`)
Gitコミット前に品質チェックを実行します。

**使い方**: 
チャットで「コミット前チェック」または「pre-commit」と入力

### 4. **Code Review** (`code-review.md`)
手動でコードレビューを実行します。

**使い方**: 
チャットで「レビュー」または「code review」と入力

### 5. **Auto Format** (`auto-format.md`)
コードの自動フォーマットを実行します。

**使い方**: 
チャットで「フォーマット」または「format」と入力

## 🛠️ フックの設定方法

### 方法1: コマンドパレットから設定

1. **Ctrl+Shift+P** (Windows/Linux) または **Cmd+Shift+P** (Mac) でコマンドパレットを開く
2. 「**Open Kiro Hook UI**」を検索して選択
3. 各フックのドキュメントに記載された設定を追加

### 方法2: Kiroフィーチャーパネルから設定

1. Kiro IDEのサイドバーから「**Agent Hooks**」セクションを開く
2. 「**+ New Hook**」をクリック
3. 各フックのドキュメントに記載された設定を入力

## 📝 フック設定例

### ファイル保存時のチェック

```
Name: Code Quality Check on Save
Trigger: On Message Sent
Message Pattern: (保存されたファイルをチェック|check saved file)
Action: Send Message to Agent
Message: [on-save-quality-check.mdの内容を参照]
```

### コミット前チェック

```
Name: Pre-Commit Check
Trigger: On Message Sent
Message Pattern: (コミット前チェック|pre-commit)
Action: Send Message to Agent
Message: [pre-commit-check.mdの内容を参照]
```

## 🎯 推奨ワークフロー

### 開発中
1. コードを書く
2. ファイルを保存
3. チャットで「**保存されたファイルをチェック**」と入力
4. 指摘された問題を修正

### ビルド前
1. チャットで「**ビルドして**」と入力
2. ビルドエラーがあれば修正
3. 再度ビルド確認

### コミット前
1. チャットで「**コミット前チェック**」と入力
2. すべてのチェックが成功することを確認
3. Gitコミット実行

### コードレビュー
1. チャットで「**レビュー**」と入力
2. 改善提案を確認
3. 必要に応じて修正

## 🔧 カスタマイズ

各フックは`.kiro/hooks/`ディレクトリ内のMarkdownファイルで管理されています。
プロジェクトの要件に応じて、チェック内容やメッセージをカスタマイズできます。

## 📚 参考資料

- コーディング規約: `.kiro/steering/coding-standards.md`
- 設計書: `.kiro/specs/cooking-support-app/design.md`
- タスク管理: `.kiro/specs/cooking-support-app/tasks.md`

## 💡 ヒント

- フックは自動的には実行されません。チャットでキーワードを入力することで起動します
- 複数のフックを組み合わせて使用できます
- フックの設定は`.kiro/hooks/`ディレクトリで管理されているため、Gitで共有できます
