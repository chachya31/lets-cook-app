---
inclusion: manual
---

# 画面設計書生成ガイド

画面設計書（Markdown）を作成します。Playwrightで対象画面のスクリーンショットを取得し、drawioでフルスクリーンショット上に項番（正方形+数字）を付与したレイアウト図（PNG）を作成したうえで、画面項目・入力規則・遷移・メッセージをテンプレートに沿って記述します。

# 目的
自炊支援・食費節約アプリの画面設計書を、一定の品質・粒度・形式で作成します。

# このガイドを使うタイミング
- 画面設計書の作成を依頼されたとき

# 対象アプリ情報
- アプリ名：自炊支援・食費節約アプリ
- フロントエンド：React 18 + TypeScript + Vite（`http://localhost:3000`）
- バックエンド：Spring Boot 3（`http://localhost:8080`）
- 画面操作手順：`app-operation-playwright.md` を参照

## 主要画面一覧（screen-name の参考）

| 画面名               | screen-name     | URLパス             | 認証   |
| -------------------- | --------------- | ------------------- | ------ |
| ダッシュボード       | dashboard       | `/dashboard`        | 不要   |
| ログイン             | login           | `/login`            | 不要   |
| 新規登録             | register        | `/register`         | 不要   |
| メール確認           | confirm-email   | `/confirm-email`    | 不要   |
| パスワードリセット   | password-reset  | `/password-reset`   | 不要   |
| レシピ検索           | recipe-search   | `/recipes`          | 不要   |
| レシピ詳細           | recipe-detail   | `/recipes/:id`      | 不要   |
| レシピ作成           | recipe-new      | `/recipes/new`      | 必要   |
| レシピ編集           | recipe-edit     | `/recipes/:id/edit` | 必要   |
| プロフィール編集     | profile-edit    | `/profile/edit`     | 必要   |
| スケジュール         | schedule        | `/schedules`        | 必要   |
| 買い物リスト         | shopping-list   | `/shopping-list`    | 必要   |
| 食材在庫             | inventory       | `/inventory`        | 必要   |
| AIチャット           | chat            | `/chat`             | 必要   |
| 管理者ダッシュボード | admin-dashboard | `/admin`            | 管理者 |
| ユーザー管理         | admin-users     | `/admin/users`      | 管理者 |
| レシピ管理           | admin-recipes   | `/admin/recipes`    | 管理者 |

# 前提（入力として必要なもの）
ユーザーに不足があれば、作業開始前に質問して埋めてください（推測で埋めない）。
- 対象画面名（上記一覧の screen-name を使用、または新規命名）
- 画面のURL（ログインが必要なら `app-operation-playwright.md` の手順に従う）
- 画面の目的（誰が/何をする画面か）
- 主要ユースケース（最大3〜5）
- 既存のテンプレート/フォルダ構成がある場合はそのパス

# 成果物（出力）
以下を作成/更新します（パスは必ずこの通りに統一）。
- `docs/ui-design/<screen-name>.md`（画面設計書）
- `docs/ui-design/assets/screens/<screen-name>_full.png`（フルスクリーンショット）
- `docs/ui-design/assets/screens/<screen-name>_<n>.png`（追加スクリーンショット：任意）
- `docs/ui-design/assets/layouts/<screen-name>.png`（drawioで作成したレイアウト図：フルスクショ+項番）
- `docs/ui-design/assets/layouts/<screen-name>.drawio`（編集用ソース）

# 作業手順
## 1. 対象画面の把握
- 画面の目的、利用者、前提条件（ログイン/権限/事前データ）を整理する
- 画面の主要ユースケース（最大3〜5）を箇条書きにする

## 2. Playwrightで画面キャプチャを取得
- **スクリーンショット取得前に、ブラウザサイズを 1280x800 に設定する**（`app-operation-playwright` の 4.0 参照）
- 画面を開き、初期表示のフルスクリーンショットを撮る
- 可能なら以下も撮る（必要な範囲で）
  - 画面内のモーダル
  - バリデーションエラー表示
  - 成功トースト/失敗メッセージ
  - 操作後の状態（例：フォーム送信後の結果表示、チャットのやり取り後の画面など）
- 取得した画像を `docs/ui-design/assets/screens/` に保存
  - フルスクリーンショット：`<screen-name>_full.png`
  - その他：`<screen-name>_<n>.png`（nは1始まりの連番）

## 3. drawioでレイアウト図（整形版）を作成（フルスクショ上に項番を付与）

### 3.0 目的
- `docs/ui-design/assets/layouts/<screen-name>.drawio` に **フルスクショ画像が埋め込み済み**で保存されること
- `docs/ui-design/assets/layouts/<screen-name>.png` が **確実にエクスポート**されること
- `<screen-name>.png` は「フルスクショを下地」＋「項番（正方形）を最前面」で合成された一枚絵であること
- 項番の背景色は **透明度（Opacity）あり**とすること

### 3.1 入力画像（下地）のパス（固定）
- 下地画像：`docs/ui-design/assets/screens/<screen-name>_full.png`

### 3.2 drawio ソースの作成・保存先（固定）
- ソース：`docs/ui-design/assets/layouts/<screen-name>.drawio`

### 3.3 drawio での作業（必須：画像は「埋め込み」）
1. `docs/ui-design/assets/layouts/<screen-name>.drawio` を新規作成（または作り直し）して開く
2. **画像を埋め込みで挿入**する
   - `docs/ui-design/assets/screens/<screen-name>_full.png` を drawio に挿入する
   - 挿入時に「リンク」か「埋め込み」を選べる場合は、必ず **埋め込み（Embed）** を選択する
3. 挿入した画像を下地として整形する
   - 画像をページの左上（原点）に配置する
   - 画像がページ全体の下地になるよう、ページサイズを画像に合わせる（「ページを内容に合わせる / Fit to content」等）
   - 画像は **最背面（Send to back）** に移動し、**ロック（Lock）** して以後動かないようにする

### 3.4 項番（正方形＋数字）の付与（必須：最前面・透明度あり）
1. 項番は **正方形（square）** を使用する（円や吹き出しは禁止）
2. 正方形の中央に数字（`1, 2, 3...`）を記載する
   - 文字は中央揃え
   - 正方形サイズは全項目で統一する
3. 項番の背景色は **Opacity（透明度）を付ける**
   - 例：Opacity 40〜60%（数値は任意だが必ず透明度あり）
4. すべての項番は **最前面（Bring to front）** に統一する
   - 下地画像は最背面、項番は最前面
5. 項番は「画面項目表」の項番と一致させる

### 3.5 （任意）主要領域の枠
- 必要に応じて主要領域（ヘッダー/検索条件/一覧/フッター等）を枠で区切る（任意）

### 3.6 保存（必須：.drawio に画像が入っている状態で保存）
- `docs/ui-design/assets/layouts/<screen-name>.drawio` として保存する
- 保存後、**一度 drawio ファイルを閉じて再度開き**、フルスクショが表示されることを確認する
  - 表示されない場合は「リンク保存」になっている可能性があるため、必ず「埋め込み」で挿入し直す

### 3.7 PNG エクスポート（必須：合成一枚絵）
- PNG の出力先：`docs/ui-design/assets/layouts/<screen-name>.png`
- エクスポートする PNG は **「フルスクショ + 正方形項番」が合成された一枚絵**であること
- エクスポート時は「ページ全体（Page）」を対象に出力する（画像だけ/選択範囲だけの出力は禁止）

### 3.7.1 代替手段：AIが自動実行する場合（drawioエディタ操作が困難な場合）

drawioエディタでの対話的操作（画像埋め込み・レイヤー操作・PNGエクスポート）が確実に実行できない場合、以下の代替手段を使用してください。

#### 方式A：drawio XML直接生成（推奨）
1. フルスクショをBase64エンコードし、drawio XMLの `<mxCell>` に `style="shape=image;image=data:image/png;base64,..."` として直接埋め込む
2. 項番の正方形も `<mxCell>` として座標・サイズ・スタイルをXMLで定義する
3. 生成したXMLを `.drawio` ファイルとして保存する
4. `mcp_drawio_open_drawio_xml` でXMLを開き、PNGエクスポートを試みる

#### 方式B：HTMLオーバーレイ方式（方式Aが失敗した場合のフォールバック）
1. フルスクショを `<img>` タグで配置し、項番を `position: absolute` の `<div>` で重ねたHTMLファイルを生成する
2. Playwright MCP でそのHTMLを開き、`browser_take_screenshot` でPNGを取得する
3. 取得したPNGを `docs/ui-design/assets/layouts/<screen-name>.png` に保存する
4. `.drawio` ファイルは方式Aの手順で別途生成する（PNGエクスポートは不要、編集用ソースとして保存）

HTMLテンプレート例：
```html
<!DOCTYPE html>
<html>
<head>
<style>
  body { margin: 0; padding: 0; }
  .container { position: relative; display: inline-block; }
  .container img { display: block; }
  .marker {
    position: absolute;
    width: 28px; height: 28px;
    background: rgba(255, 80, 80, 0.55);
    color: #fff; font-weight: bold; font-size: 14px;
    display: flex; align-items: center; justify-content: center;
    border: 1px solid rgba(200, 0, 0, 0.7);
  }
</style>
</head>
<body>
<div class="container">
  <img src="<screen-name>_full.png">
  <!-- 項番：top/left を各要素の位置に合わせる -->
  <div class="marker" style="top:10px; left:10px;">1</div>
  <div class="marker" style="top:60px; left:10px;">2</div>
</div>
</body>
</html>
```

#### 方式の選択基準
| 条件                           | 使用する方式              |
| ------------------------------ | ------------------------- |
| drawioエディタで対話操作が可能 | 3.3〜3.7の標準手順        |
| drawio XMLを直接生成できる     | 方式A                     |
| 上記いずれも失敗した場合       | 方式B（HTMLオーバーレイ） |

### 3.8 エクスポート／埋め込みの検証（必須：失敗ならやり直し）
以下のコマンドで成果物を検証し、満たさない場合は 3.6〜3.7（または 3.7.1 の代替手段）をやり直す。

```powershell
# PowerShell（Windows）
# 1) PNG が存在し、0バイトではないこと
$png = "docs/ui-design/assets/layouts/<screen-name>.png"
if (!(Test-Path $png)) { throw "PNG not exported: $png" }
if ((Get-Item $png).Length -le 0) { throw "PNG is empty: $png" }

# 2) drawio が存在すること
$drawio = "docs/ui-design/assets/layouts/<screen-name>.drawio"
if (!(Test-Path $drawio)) { throw "DRAWIO not saved: $drawio" }

# 3) drawio に画像が埋め込まれている可能性を確認（data:image を含むこと）
#    ※埋め込み画像は drawio(XML) 内に data:image 形式で入ることが多い
if (!(Select-String -Path $drawio -Pattern "data:image" -Quiet)) {
  throw "Screenshot is likely NOT embedded in drawio: $drawio"
}
"OK: layout png exported and screenshot embedded"
```

```bash
# bash（macOS / Linux）
PNG="docs/ui-design/assets/layouts/<screen-name>.png"
DRAWIO="docs/ui-design/assets/layouts/<screen-name>.drawio"

# 1) PNG が存在し、0バイトではないこと
[ ! -f "$PNG" ] && echo "PNG not exported: $PNG" && exit 1
[ ! -s "$PNG" ] && echo "PNG is empty: $PNG" && exit 1

# 2) drawio が存在すること
[ ! -f "$DRAWIO" ] && echo "DRAWIO not saved: $DRAWIO" && exit 1

# 3) drawio に画像が埋め込まれている可能性を確認
grep -q "data:image" "$DRAWIO" || { echo "Screenshot is likely NOT embedded in drawio: $DRAWIO"; exit 1; }
echo "OK: layout png exported and screenshot embedded"
```
* 上記が通らない場合：
  * `.drawio` を「埋め込み」で作り直し → 保存 → PNGエクスポート → 再検証

## 4. Markdown設計書を作成
- テンプレートに沿って記述する
- 推測で埋めない。不明点は **「要確認」** と明示するか、ユーザーに質問して確定する

## 5. セルフレビュー（品質チェック）
- 項番が「レイアウト図」と「画面項目表」で一致している
- 入力規則（必須/桁/形式/範囲）が漏れていない
- ボタン/リンクの挙動（押下後の遷移/更新/確認）が明確
- 文言（ラベル/メッセージ）が画面と一致（未確定なら要確認）

# 画面設計書テンプレート（この構造を守る）
```md
## <画面名> 画面設計書

### 1. 概要
- 目的：
- 対象ユーザー：
- 前提条件：

### 2. 画面レイアウト
<!-- フルスクショ上に正方形項番を付与したレイアウト図 -->
<img src="./assets/layouts/<screen-name>.png">

### 3. 画面項目
| 項番 | 項目名 | 種別     | 表示/入力 | 必須  | 型/形式 |   桁 | 初期値 | 入力規則 | 備考 |
| ---: | ------ | -------- | --------- | :---: | ------- | ---: | ------ | -------- | ---- |
|    1 |        | テキスト | 入力      |       |         |      |        |          |      |

### 4. 操作・イベント
| 画面項番 | 操作 | トリガー       | 条件 | 処理内容 | 成功時 | 失敗時 |
| -------- | ---- | -------------- | ---- | -------- | ------ | ------ |
| 1        | 検索 | 検索ボタン押下 |      |          |        |        |

### 5. 補足
- 権限/ロール：
- 性能/制約：
- 要確認事項：
```
