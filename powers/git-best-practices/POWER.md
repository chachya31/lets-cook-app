---
name: "git-best-practices"
displayName: "Gitベストプラクティス"
description: "クリーンなコミット履歴と効果的なコラボレーションのための、必須のGitワークフローパターンとベストプラクティス。"
keywords: ["git", "バージョン管理", "コミット", "ブランチ", "コラボレーション"]
author: "サンプル作成者"
---

# Gitベストプラクティス

## 概要

このパワーは、クリーンなコミット履歴を維持し、チームと効果的にコラボレーションし、よくある落とし穴を避けるための、必須のGitワークフローパターンとベストプラクティスを提供します。個人作業でもチーム作業でも、これらのプラクティスはGitワークフローを改善します。

## 基本原則

1. **頻繁にコミット、定期的にプッシュ** - 小さく焦点を絞ったコミットは、レビューや取り消しが簡単
2. **明確なコミットメッセージを書く** - 未来の自分（とチーム）が感謝します
3. **戦略的にブランチを使う** - フィーチャーブランチを使ってmainを安定に保つ
4. **コミット前に確認** - コミットしようとしている内容を必ず確認する

## 一般的なパターン

### パターン1: アトミックコミット

**問題:** 複数の変更を混在させた大きなコミットは、レビューや取り消しが困難

**解決策:** 一つのことだけを行う、小さく焦点を絞ったコミットを作成

```bash
# 良い例 - アトミックコミット
git add src/auth/login.js
git commit -m "feat: ログイン検証を追加"

git add src/auth/logout.js
git commit -m "feat: ログアウト機能を実装"

# 悪い例 - 無関係な変更を混在
git add .
git commit -m "いろいろ更新"
```

### パターン2: 意味のあるコミットメッセージ

**フォーマット:** `<type>: <説明>`

**タイプ:**
- `feat`: 新機能
- `fix`: バグ修正
- `docs`: ドキュメント変更
- `refactor`: コードリファクタリング
- `test`: テスト追加
- `chore`: メンテナンスタスク

**例:**
```bash
git commit -m "feat: ユーザー認証を追加"
git commit -m "fix: ログインのnullポインタを解決"
git commit -m "docs: APIドキュメントを更新"
```

### パターン3: フィーチャーブランチワークフロー

**ワークフロー:**
```bash
# フィーチャーブランチを作成
git checkout -b feature/user-profile

# 変更してコミット
git add src/profile.js
git commit -m "feat: ユーザープロフィールページを追加"

# mainブランチの最新状態を取り込む
git checkout main
git pull
git checkout feature/user-profile
git rebase main

# プッシュしてPRを作成
git push origin feature/user-profile
```

## ベストプラクティス

- 新しい作業を始める前に必ずpullする
- コミット前に`git diff`で変更を確認する
- `.gitignore`を使ってビルド成果物やシークレットを除外する
- 機密データ（APIキー、パスワード）は絶対にコミットしない
- フィーチャーブランチをrebaseして履歴をクリーンに保つ
- マージ済みのブランチは削除して整理する

## トラブルシューティング

### 問題: 間違ったブランチにコミットしてしまった

**解決策:**
```bash
# コミットを正しいブランチに移動
git log  # コミットハッシュをメモ
git reset --hard HEAD~1  # 現在のブランチでコミットを取り消し
git checkout correct-branch
git cherry-pick <commit-hash>
```

### 問題: 最後のコミットを取り消したい

**解決策:**
```bash
# 変更は保持、コミットのみ取り消し
git reset --soft HEAD~1

# 変更もコミットも破棄
git reset --hard HEAD~1
```

---

**これはナレッジベースパワーです** - MCPサーバーは不要
