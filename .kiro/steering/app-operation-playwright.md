---
inclusion: manual
---

# Playwright によるアプリ操作手順

Playwright（Playwright MCP）でローカル Web アプリ（http://localhost:3000）を確実に起動・ログイン・操作する手順です。

## 前提（重要）
- 対象URL：`http://localhost:3000`（フロントエンド）
- バックエンドURL：`http://localhost:8080`（Spring Boot）
- アプリ起動は **Windows** を前提とします。
- Frontendアプリ起動コマンドは以下です。
  - `npm run dev`（frontend ディレクトリで実行）
- Backendアプリ起動コマンドは以下です。
  - `./gradlew.bat bootRun --args='--spring.profiles.active=local'`（backend ディレクトリで実行）

## 目的
- Kiro が Playwright（Playwright MCP）を用いて、ローカルアプリを
  **起動状態確認 → 必要なら起動 → ログイン → 画面操作**
  まで確実に実施できるようにします。

## 禁止事項
- **資格情報（ID/Password）の推測は禁止**（見つからない場合は止めてユーザー確認）
- **破壊的操作は禁止**（削除、退会、課金、管理者操作、データ変更など）

## Playwright の要素特定ルール（本アプリ前提）
本アプリの HTML には `data-testid` 等のテスト用属性が存在しないため、以下の順で特定します。

1. **id / name 属性**（フォーム部品が持っている場合）
2. **ラベルと紐づく入力**（`label` → `input`）
3. **placeholder**（入力欄のヒント文字）
4. **ボタンやリンクの表示テキスト**（例：`ログイン` / `ログアウト`）

`nth-child` や過度に長い CSS セレクタは使いません

---

# 作業手順

## 1. 事前に「起動済みか」を確認する（確実チェック）

### 1.1 HTTP 疎通確認（最優先）
**HTTP 200/3xx が返るなら起動済み**です。

```powershell
# PowerShell
try {
  $r = Invoke-WebRequest -UseBasicParsing http://localhost:3000/ -TimeoutSec 3
  $r.StatusCode
} catch {
  "NOT_RUNNING"
}
```

```bash
# bash
curl -s -o /dev/null -w "%{http_code}" --connect-timeout 3 http://localhost:3000/ || echo "NOT_RUNNING"
```

* 結果が `NOT_RUNNING` の場合 → 「2. 起動手順」へ

### 1.2 3000 LISTEN 確認（補助）

```powershell
# PowerShell
netstat -ano | Select-String ":3000"
```

```bash
# bash
ss -tlnp | grep :3000 || netstat -tlnp 2>/dev/null | grep :3000
```

### 1.3 8080 LISTEN 確認（補助）

```powershell
# PowerShell
netstat -ano | Select-String ":8080"
```

```bash
# bash
ss -tlnp | grep :8080 || netstat -tlnp 2>/dev/null | grep :8080
```

---

## 2. 起動手順（未起動の場合）

### 2.1 Backendアプリ（Spring Boot）を起動する（確定コマンド）

```powershell
# PowerShell（Windows）
# backend ディレクトリで実行
./gradlew.bat bootRun --args='--spring.profiles.active=local'
```

```bash
# bash（macOS / Linux）
# backend ディレクトリで実行
./gradlew bootRun --args='--spring.profiles.active=local'
```

### 2.2 Frontendアプリ（Vite + React）を起動する（確定コマンド）

```powershell
# PowerShell / bash 共通
# frontend ディレクトリで実行
npm run dev
```

### 2.3 起動完了（HTTP 応答）を確認する（確定待機）

バックエンド・フロントエンドの両方が応答するまで待機します。

```powershell
# PowerShell（Windows）
# バックエンド確認
for ($i=0; $i -lt 120; $i++) {
  try {
    $r = Invoke-WebRequest -UseBasicParsing http://localhost:8080/ -TimeoutSec 2
    if ($r.StatusCode -ge 200 -and $r.StatusCode -lt 400) { "OK: backend is up"; break }
  } catch {}
  Start-Sleep -Seconds 1
}

# フロントエンド確認
for ($i=0; $i -lt 60; $i++) {
  try {
    $r = Invoke-WebRequest -UseBasicParsing http://localhost:3000/ -TimeoutSec 2
    if ($r.StatusCode -ge 200 -and $r.StatusCode -lt 400) { "OK: frontend is up"; break }
  } catch {}
  Start-Sleep -Seconds 1
}
```

```bash
# bash（macOS / Linux）
# バックエンド確認
for i in $(seq 1 120); do
  code=$(curl -s -o /dev/null -w "%{http_code}" --connect-timeout 2 http://localhost:8080/ 2>/dev/null)
  if [ "$code" -ge 200 ] && [ "$code" -lt 400 ]; then echo "OK: backend is up"; break; fi
  sleep 1
done

# フロントエンド確認
for i in $(seq 1 60); do
  code=$(curl -s -o /dev/null -w "%{http_code}" --connect-timeout 2 http://localhost:3000/ 2>/dev/null)
  if [ "$code" -ge 200 ] && [ "$code" -lt 400 ]; then echo "OK: frontend is up"; break; fi
  sleep 1
done
```

---

## 3. ログイン手順（Playwright：確実フロー）

### 3.1 ログイン要否の判定（確実条件）

1. Playwright で `http://localhost:3000` を開く
2. 以下のどちらかで「ログイン画面」を断定する

   * URL パスが `http://localhost:3000/login` と一致する

* ログイン画面 → 3.2 へ
* それ以外 → ログイン済みとして 4 へ

### 3.2 ログイン実行（資格情報は backend/.env から取得）

#### 3.2.1 資格情報（確定情報）

資格情報は `backend/.env` の以下キーから取得します（確定）。

| 資格情報 | ソース         | キー           |
| -------- | -------------- | -------------- |
| email    | `backend/.env` | `APP_USERNAME` |
| password | `backend/.env` | `APP_PASSWORD` |

#### 3.2.2 UI 操作（要素特定）

1. `#email` でメールアドレス入力欄を特定し、email を入力
2. `#password` でパスワード入力欄を特定し、password を入力
3. ログインボタン（`type="submit"`）をクリック
4. 成功判定（必須）：
   * URL パスが `http://localhost:3000/login` 以外になった（通常は `/dashboard` に遷移）

### 3.3 ログイン失敗時（確実対応）

* クリック後もログイン画面のままの場合：

  1. 画面上のエラーメッセージを取得して記録する（スクリーンショットも取得）
  2. `backend/.env` の `APP_USERNAME` / `APP_PASSWORD` が正しいかをユーザーに確認する

## 4. 目的の画面操作（任意）

* 操作対象・成功条件（URL 変化 / 要素表示 / トースト表示など）を **必ず定義してから** 実行します。

### 4.0 ブラウザサイズの設定（必須：操作・スクリーンショット取得前に実行）
- 画面操作やスクリーンショット取得の前に、ブラウザのビューポートサイズを **1280x800**（一般的なノートPC）に設定する
- Playwright MCP の `browser_resize` を使用する（`width: 1280`, `height: 800`）
- これにより、スクリーンショットのサイズが統一される

### 4.1 主要画面一覧

本アプリの主要画面とURLパスは以下の通りです。

| 画面名               | URLパス             | 認証   | 備考                     |
| -------------------- | ------------------- | ------ | ------------------------ |
| ダッシュボード       | `/dashboard`        | 不要   | トップページ             |
| ログイン             | `/login`            | 不要   | 認証画面                 |
| 新規登録             | `/register`         | 不要   | ユーザー登録             |
| メール確認           | `/confirm-email`    | 不要   | メール認証               |
| パスワードリセット   | `/password-reset`   | 不要   | パスワード再設定         |
| レシピ検索           | `/recipes`          | 不要   | レシピ一覧・検索         |
| レシピ詳細           | `/recipes/:id`      | 不要   | レシピ詳細表示           |
| レシピ作成           | `/recipes/new`      | 必要   | 新規レシピ作成           |
| レシピ編集           | `/recipes/:id/edit` | 必要   | レシピ編集               |
| プロフィール編集     | `/profile/edit`     | 必要   | ユーザープロフィール編集 |
| スケジュール         | `/schedules`        | 必要   | 料理予定・実績カレンダー |
| 買い物リスト         | `/shopping-list`    | 必要   | 買い物リスト管理         |
| 食材在庫             | `/inventory`        | 必要   | 食材在庫管理             |
| AIチャット           | `/chat`             | 必要   | AIアドバイザーチャット   |
| 管理者ダッシュボード | `/admin`            | 管理者 | 管理者専用               |
| ユーザー管理         | `/admin/users`      | 管理者 | ユーザー管理             |
| レシピ管理           | `/admin/recipes`    | 管理者 | レシピ管理               |

### 4.2 共通UIパターン

#### 4.2.1 ヘッダー
- 認証ページ（`/login`, `/register`, `/confirm-email`, `/password-reset`）ではヘッダー非表示
- それ以外のページではヘッダーが表示される

#### 4.2.2 フッター
- 認証ページではフッター非表示
- それ以外のページではフッターが表示される

### 4.3 買い物リスト画面の操作パターン

買い物リスト画面（`/shopping-list`）でのアイテム管理操作の手順です。

#### 4.3.1 画面遷移
- ログイン後、`http://localhost:3000/shopping-list` に遷移する
- 画面の判定：ページタイトル「買い物リスト」（`ShoppingCart` アイコン付き）が表示されること

#### 4.3.2 画面構成

画面は以下の3つのセクションで構成されます。

| セクション   | 表示条件                          | 備考                                     |
| ------------ | --------------------------------- | ---------------------------------------- |
| アイテム追加 | 常に表示                          | Card内のフォーム                         |
| 購入予定     | 未チェックアイテムが1件以上ある   | 見出し「購入予定」+ アイテムカード一覧   |
| 購入済み     | チェック済みアイテムが1件以上ある | 見出し「購入済み」+ 半透明アイテムカード |
| 空メッセージ | アイテムが0件                     | 「買い物リストは空です」テキスト         |

#### 4.3.3 要素セレクタ一覧

| 要素                     | セレクタ                         | 備考                                      |
| ------------------------ | -------------------------------- | ----------------------------------------- |
| 商品名入力欄             | `#name`                          | placeholder「例: 玉ねぎ」、必須           |
| 数量入力欄               | `#quantity`                      | type="number"、placeholder「1」、必須     |
| 単位入力欄               | `#unit`                          | placeholder「例: 個」、任意               |
| 追加ボタン               | `button[type="submit"]`          | フォーム内の送信ボタン、テキスト「追加」  |
| チェックボックス         | `input[type="checkbox"]`         | 各アイテムカード内、購入予定/購入済み切替 |
| 削除ボタン               | テキスト「削除」の赤いボタン     | 各アイテムカード内、variant="destructive" |
| 購入予定セクション見出し | テキスト「購入予定」             | 未チェックアイテムがある場合のみ表示      |
| 購入済みセクション見出し | テキスト「購入済み」             | チェック済みアイテムがある場合のみ表示    |
| 空メッセージ             | テキスト「買い物リストは空です」 | アイテム0件の場合のみ表示                 |

#### 4.3.4 アイテムの追加

1. `#name` に商品名を入力する（例：「玉ねぎ」）
2. `#quantity` に数量を入力する（例：「3」）
3. `#unit` に単位を入力する（例：「個」）※任意
4. `button[type="submit"]`（「追加」ボタン）をクリックする
5. 成功判定：
   - 「購入予定」セクションに新しいアイテムカードが表示される
   - フォームの入力欄がリセット（空）になる
6. 注意事項：
   - 商品名と数量は必須。未入力の場合はフォーム送信されない
   - 数量は `0.01` 〜 `9999` の範囲、小数点以下2桁まで
   - 同名の食材を追加した場合、既存アイテムが更新される可能性がある

#### 4.3.5 チェックを入れる（購入予定 → 購入済み）

1. 「購入予定」セクション内の対象アイテムのチェックボックスをクリックする
2. 成功判定：
   - 対象アイテムが「購入予定」セクションから消え、「購入済み」セクションに移動する
   - 移動後のアイテムは半透明（`opacity-60`）+ 取り消し線（`line-through`）で表示される
   - 「購入予定」セクションのアイテムが0件になった場合、セクション見出しごと非表示になる

#### 4.3.6 チェックを外す（購入済み → 購入予定）

1. 「購入済み」セクション内の対象アイテムのチェックボックスをクリックする
2. 成功判定：
   - 対象アイテムが「購入済み」セクションから消え、「購入予定」セクションに移動する
   - 移動後のアイテムは通常表示（不透明、取り消し線なし）に戻る
   - 「購入済み」セクションのアイテムが0件になった場合、セクション見出しごと非表示になる

#### 4.3.7 アイテムの削除

1. 対象アイテムカード内の「削除」ボタン（赤色）をクリックする
2. 成功判定：
   - 対象アイテムがリストから消える
   - 該当セクション（購入予定 or 購入済み）のアイテムが0件になった場合、セクション見出しごと非表示になる
   - 全アイテムが0件になった場合、「買い物リストは空です」メッセージが表示される
3. 注意事項：
   - 削除は「購入予定」「購入済み」どちらのセクションからも可能（削除ボタンは常に有効）
   - 確認ダイアログは表示されない（即時削除）

#### 4.3.8 注意事項
- API通信中は追加ボタンが `disabled` になり、テキストが「読み込み中...」に変わる
- エラー発生時は画面上部にエラーメッセージが表示され、自動スクロールする
- 各アイテムカードには商品名・数量・単位が表示される

---

## 5. トラブルシューティング（確実に切り分ける）

### 5.1 `http://localhost:8080` に繋がらない

1. 1.1 の HTTP 確認を再実行
2. 2.1 Backendアプリ起動（`./gradlew.bat bootRun`）が生きているか確認（起動ターミナルのログを確認）
3. 8080 を別プロセスが占有していないか確認（1.3）

### 5.2 `http://localhost:3000` に繋がらない

1. フロントエンドの Vite 開発サーバーが起動しているか確認
2. 3000 を別プロセスが占有していないか確認（1.2）

### 5.3 UI 要素が見つからない / クリックできない

* スクリーンショットを取得して「実際に表示されている文言 / ラベル / placeholder / name / id / aria-label」を確認します。
* 待機が必要な場合は、**待機条件（要素の表示/非表示）を明示して** 待機します。

---

## 6. 実行チェックリスト（完了条件）

* [ ] `http://localhost:8080/` が応答
* [ ] `http://localhost:3000/` が応答
* [ ] ログイン成功判定を満たした
