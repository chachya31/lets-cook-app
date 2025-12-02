# コード品質チェック体制

このプロジェクトでは、多層防御戦略でコード品質を保証しています。

## 🛡️ 3層の品質チェック

### 1️⃣ ローカル（Kiro Hooks）- 即座のフィードバック

開発中にリアルタイムでチェック

**使い方：**
- ファイル保存時: チャットで「**保存されたファイルをチェック**」
- ビルド前: チャットで「**ビルドして**」
- コミット前: チャットで「**コミット前チェック**」
- コードレビュー: チャットで「**レビュー**」

**チェック内容：**
- ✅ コーディング規約違反
- ✅ デバッグコード（`System.out.println`, `console.log`）
- ✅ 未使用のimport
- ✅ ビルド成功

**設定方法：**
`.kiro/hooks/README.md` を参照

---

### 2️⃣ Git Hooks - コミット前の自動チェック

コミット時に自動実行

**セットアップ：**
```bash
# Windows
setup-hooks.bat

# または手動で
git config core.hooksPath .githooks
```

**チェック内容：**
- ❌ `System.out.println` 検出で**コミット拒否**
- ⚠️ `console.log` 検出で**警告**
- ✅ バックエンドビルド成功
- ✅ ESLint成功

---

### 3️⃣ GitHub Actions - プッシュ後の最終検証

プッシュ・プルリクエスト時に自動実行

**トリガー：**
- `main`, `develop` ブランチへのプッシュ
- プルリクエストの作成・更新

**チェック内容：**

#### Backend Tests & Build
- ✅ 全テスト実行
- ✅ ビルド成功
- ✅ テスト結果レポート生成

#### Frontend Tests & Build
- ✅ ESLint
- ✅ ユニットテスト
- ✅ ビルド成功

#### Code Quality Check
- ❌ `System.out.println` 検出で**失敗**
- ⚠️ `console.log` 検出で**警告**
- ✅ マージコンフリクトチェック
- ✅ 大容量ファイルチェック

#### PR Validation
- ✅ PRタイトルがConventional Commits形式
  - 例: `feat: add user login`, `fix(api): resolve timeout`

---

## 📋 チェック項目一覧

### バックエンド（Java）

| チェック項目 | Kiro | Git Hook | GitHub Actions |
|------------|------|----------|----------------|
| `System.out.println` 禁止 | ✅ | ❌ 拒否 | ❌ 失敗 |
| `@Slf4j` 禁止 | ✅ | - | - |
| `System.getProperty()` 禁止 | ✅ | - | - |
| 未使用import | ✅ | - | - |
| ビルド成功 | ✅ | ✅ | ✅ |
| テスト成功 | - | - | ✅ |

### フロントエンド（TypeScript）

| チェック項目 | Kiro | Git Hook | GitHub Actions |
|------------|------|----------|----------------|
| `console.log` 禁止 | ✅ | ⚠️ 警告 | ⚠️ 警告 |
| `axios` 禁止 | ✅ | - | - |
| `fetch` 直接使用禁止 | ✅ | - | - |
| ESLint | ✅ | ✅ | ✅ |
| ビルド成功 | ✅ | - | ✅ |
| テスト成功 | - | - | ✅ |

---

## 🚀 推奨ワークフロー

### 開発中
```
1. コードを書く
2. ファイルを保存
3. Kiroで「保存されたファイルをチェック」
4. 問題を修正
```

### コミット前
```
1. Kiroで「コミット前チェック」
2. すべてOKを確認
3. git commit（Git Hookが自動実行）
4. 問題があれば修正してリトライ
```

### プッシュ後
```
1. git push
2. GitHub Actionsが自動実行
3. GitHubのActionsタブで結果確認
4. 失敗したら修正してプッシュ
```

### プルリクエスト
```
1. PRを作成
2. GitHub Actionsが自動実行
3. すべてのチェックが✅になるまで修正
4. レビュー依頼
5. マージ
```

---

## 🔧 トラブルシューティング

### Git Hookが動かない
```bash
# Hooksパスを確認
git config core.hooksPath

# 再設定
git config core.hooksPath .githooks
```

### GitHub Actionsが失敗する
1. GitHubのActionsタブで詳細を確認
2. ローカルで同じコマンドを実行して再現
3. 修正してプッシュ

### Kiro Hooksが動かない
1. `.kiro/hooks/README.md` を確認
2. コマンドパレットで「Open Kiro Hook UI」
3. Hookが正しく設定されているか確認

---

## 📚 関連ドキュメント

- [コーディング規約](CODING_STANDARDS.md)
- [Kiro Hooks設定](.kiro/hooks/README.md)
- [GitHub Actions設定](.github/workflows/)

---

## 💡 ヒント

- **Kiro Hooks**: 開発中の即座のフィードバック
- **Git Hooks**: コミット前の最後の砦
- **GitHub Actions**: チーム全体の品質保証

3層すべてを活用することで、高品質なコードを維持できます！
