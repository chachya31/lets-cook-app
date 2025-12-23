# データベース設計書

## 概要

本アプリケーションは、Amazon DynamoDBをデータストアとして使用します。DynamoDBはNoSQLデータベースであり、スケーラビリティと高可用性を提供します。

## テーブル一覧

| テーブル名    | 用途             | Partition Key | Sort Key         | GSI               |
| ------------- | ---------------- | ------------- | ---------------- | ----------------- |
| Users         | ユーザー情報     | UserId        | -                | -                 |
| Recipes       | レシピ情報       | RecipeId      | -                | GSI_Author        |
| Reviews       | レビュー情報     | RecipeId      | ReviewId         | GSI_User          |
| Schedules     | スケジュール情報 | UserId        | DateTypeRecipeId | -                 |
| ShoppingLists | 買い物リスト     | UserId        | ItemId           | GSI_NormalizedKey |

---

## 1. Users テーブル

### 概要
ユーザーアカウント情報とプロフィールを管理するテーブル。

### キー構造
- **Partition Key**: `UserId` (String, UUID)
- **Sort Key**: なし

### 属性

| 属性名            | 型      | 必須 | 説明                            | 例                                     |
| ----------------- | ------- | ---- | ------------------------------- | -------------------------------------- |
| UserId            | String  | ✓    | ユーザーID（UUID）              | "550e8400-e29b-41d4-a716-446655440000" |
| Email             | String  | ✓    | メールアドレス                  | "user@example.com"                     |
| Nickname          | String  | ✓    | ニックネーム（1-50文字）        | "山田太郎"                             |
| DisplayName       | String  | ✓    | 表示名（1-50文字）              | "Taro Yamada"                          |
| ProfileImageUrl   | String  |      | プロフィール画像URL             | "https://s3.../profile.jpg"            |
| PreferredLanguage | String  | ✓    | 優先言語（"ja" or "ko"）        | "ja"                                   |
| LastCookingDate   | String  |      | 最終料理日（ISO8601形式）       | "2024-12-01"                           |
| LastLoginDate     | String  |      | 最終ログイン日時（ISO8601形式） | "2024-12-02T10:30:00"                  |
| CreatedAt         | String  | ✓    | 作成日時（ISO8601形式）         | "2024-11-01T09:00:00"                  |
| Timezone          | String  | ✓    | タイムゾーン                    | "Asia/Tokyo"                           |
| MarketingOptOut   | Boolean | ✓    | マーケティング配信拒否フラグ    | false                                  |

### インデックス
なし

### アクセスパターン
1. **ユーザーIDで検索**: `UserId` で直接取得（GetItem）
2. **メールアドレスで検索**: `Email` でスキャン（Scan）
   - ⚠️ 本番環境では GSI の追加を推奨

### 備考
- Cognitoと連携してユーザー認証を管理
- `LastCookingDate` はサボり防止アラート機能で使用
- メールアドレスでの検索は現在Scanを使用しているため、ユーザー数が増加した場合はGSIの追加が必要

---

## 2. Recipes テーブル

### 概要
レシピ情報を管理するテーブル。食材、手順、画像URLなどを含む。

### キー構造
- **Partition Key**: `RecipeId` (String, UUID)
- **Sort Key**: なし

### 属性

| 属性名      | 型           | 必須 | 説明                          | 例                                     |
| ----------- | ------------ | ---- | ----------------------------- | -------------------------------------- |
| RecipeId    | String       | ✓    | レシピID（UUID）              | "660e8400-e29b-41d4-a716-446655440001" |
| Title       | String       | ✓    | レシピタイトル（最大100文字） | "簡単カレーライス"                     |
| AuthorId    | String       | ✓    | 作成者ID（UserId）            | "550e8400-e29b-41d4-a716-446655440000" |
| Ingredients | List<Map>    | ✓    | 食材リスト                    | 下記参照                               |
| Steps       | List<String> | ✓    | 調理手順リスト                | ["野菜を切る", "炒める", ...]          |
| CookingTime | Number       | ✓    | 調理時間（分）                | 30                                     |
| ImageUrl    | String       |      | レシピ画像URL                 | "https://s3.../recipe.jpg"             |
| IsPublic    | Boolean      | ✓    | 公開フラグ                    | true                                   |
| IsDeleted   | Boolean      | ✓    | 論理削除フラグ                | false                                  |
| CreatedAt   | String       | ✓    | 作成日時（ISO8601形式）       | "2024-11-15T14:30:00"                  |
| UpdatedAt   | String       | ✓    | 更新日時（ISO8601形式）       | "2024-11-20T16:45:00"                  |

### Ingredients（食材）の構造

各食材は以下の属性を持つMapオブジェクト：

| 属性名   | 型      | 必須 | 説明                         | 例         |
| -------- | ------- | ---- | ---------------------------- | ---------- |
| name     | String  | ✓    | 食材名（最大100文字）        | "玉ねぎ"   |
| quantity | Number  |      | 数量（0.01-9999、任意）      | 2          |
| unit     | String  |      | 単位（最大50文字、自由入力） | "個"       |
| note     | String  |      | メモ（最大200文字）          | "中サイズ" |
| optional | Boolean | ✓    | 任意フラグ                   | false      |

※ quantity と unit は任意項目です。「適量」「少々」など数量を指定しない食材に対応しています。
| slice | 枚 | 枚 |
| clove | 片 | 片 |
| pinch | ひとつまみ | ひとつまみ |
| to_taste | 適量 | 適量 |
| as_needed | 必要に応じて | 必要に応じて |

### インデックス

#### GSI_Author
- **Partition Key**: `AuthorId` (String)
- **用途**: 作成者別のレシピ検索

### アクセスパターン
1. **レシピIDで検索**: `RecipeId` で直接取得（GetItem）
2. **作成者別検索**: `GSI_Author` を使用して `AuthorId` で検索（Query）
3. **公開レシピ一覧**: `IsPublic = true AND IsDeleted = false` でスキャン（Scan）
4. **キーワード検索**: アプリケーション層でフィルタリング

### 備考
- 論理削除を採用（`IsDeleted` フラグ）
- スケジュールと買い物リストからの参照を保持するため、物理削除は行わない
- 食材と手順はJSON形式でシリアライズして保存

---

## 3. Reviews テーブル

### 概要
レシピに対するレビュー（星評価とコメント）を管理するテーブル。

### キー構造
- **Partition Key**: `RecipeId` (String)
- **Sort Key**: `ReviewId` (String, UUID)

### 属性

| 属性名        | 型     | 必須 | 説明                                | 例                                     |
| ------------- | ------ | ---- | ----------------------------------- | -------------------------------------- |
| RecipeId      | String | ✓    | レシピID                            | "660e8400-e29b-41d4-a716-446655440001" |
| ReviewId      | String | ✓    | レビューID（UUID）                  | "770e8400-e29b-41d4-a716-446655440002" |
| UserId        | String | ✓    | レビュー投稿者ID                    | "550e8400-e29b-41d4-a716-446655440000" |
| Rating        | Number | ✓    | 星評価（1-5）                       | 5                                      |
| Comment       | String |      | コメント（最大300文字）             | "とても美味しかったです！"             |
| Status        | String | ✓    | ステータス（"visible" or "hidden"） | "visible"                              |
| ReportedCount | Number | ✓    | 通報カウント                        | 0                                      |
| CreatedAt     | String | ✓    | 作成日時（ISO8601形式）             | "2024-11-16T10:00:00Z"                 |
| UpdatedAt     | String | ✓    | 更新日時（ISO8601形式）             | "2024-11-16T10:00:00Z"                 |

### ステータス値

| 値      | 説明                           |
| ------- | ------------------------------ |
| visible | 表示可能                       |
| hidden  | 非表示（通報により自動非表示） |

### インデックス

#### GSI_User
- **Partition Key**: `UserId` (String)
- **Sort Key**: `CreatedAt` (String)
- **用途**: ユーザー別のレビュー一覧取得

### アクセスパターン
1. **レシピ別レビュー一覧**: `RecipeId` で検索（Query）
2. **ユーザー別レビュー一覧**: `GSI_User` を使用して `UserId` で検索（Query）
3. **レビューID検索**: `ReviewId` でスキャン（Scan）
   - ⚠️ 非効率なため、本番環境では改善が必要

### 備考
- 通報カウントが3以上になると自動的に `Status` が "hidden" に変更される
- 編集・削除は投稿者本人のみ可能
- 表示時は `Status = "visible"` のレビューのみフィルタリング

---

## 4. Schedules テーブル

### 概要
料理予定と実績を管理するテーブル。カレンダー機能で使用。

### キー構造
- **Partition Key**: `UserId` (String)
- **Sort Key**: `DateTypeRecipeId` (String, 複合キー)
  - 形式: `{Date}#{Type}#{RecipeId}`
  - 例: `2024-12-01#PLANNED#660e8400-e29b-41d4-a716-446655440001`

### 属性

| 属性名           | 型     | 必須 | 説明                            | 例                                     |
| ---------------- | ------ | ---- | ------------------------------- | -------------------------------------- |
| UserId           | String | ✓    | ユーザーID                      | "550e8400-e29b-41d4-a716-446655440000" |
| DateTypeRecipeId | String | ✓    | 複合ソートキー                  | "2024-12-01#PLANNED#660e..."           |
| ScheduleId       | String | ✓    | スケジュールID（UUID）          | "880e8400-e29b-41d4-a716-446655440003" |
| Date             | String | ✓    | 日付（YYYY-MM-DD形式）          | "2024-12-01"                           |
| Type             | String | ✓    | タイプ（"PLANNED" or "COOKED"） | "PLANNED"                              |
| RecipeId         | String | ✓    | レシピID                        | "660e8400-e29b-41d4-a716-446655440001" |
| RecipeTitle      | String | ✓    | レシピタイトル（参照用）        | "簡単カレーライス"                     |
| Memo             | String |      | メモ（最大120文字）             | "夕食用"                               |
| CreatedAt        | String | ✓    | 作成日時（ISO8601形式）         | "2024-11-30T15:00:00Z"                 |

### タイプ値

| 値      | 説明     |
| ------- | -------- |
| PLANNED | 料理予定 |
| COOKED  | 料理実績 |

### インデックス
なし

### アクセスパターン
1. **日付範囲検索**: `UserId` と `DateTypeRecipeId` の範囲検索（Query）
   - 例: 2024-12-01 から 2024-12-07 までのスケジュール
2. **スケジュールID検索**: `ScheduleId` でスキャン（Scan）
   - ⚠️ 非効率だが、IDでの検索は稀なため許容

### 備考
- Sort Keyに日付を含めることで、日付範囲での効率的な検索が可能
- 予定（PLANNED）を実績（COOKED）に変換する機能あり
- 実績登録時は `Users` テーブルの `LastCookingDate` も更新

---

## 5. ShoppingLists テーブル

### 概要
買い物リストのアイテムを管理するテーブル。

### キー構造
- **Partition Key**: `UserId` (String)
- **Sort Key**: `ItemId` (String, UUID)

### 属性

| 属性名         | 型      | 必須 | 説明                                 | 例                                     |
| -------------- | ------- | ---- | ------------------------------------ | -------------------------------------- |
| UserId         | String  | ✓    | ユーザーID                           | "550e8400-e29b-41d4-a716-446655440000" |
| ItemId         | String  | ✓    | アイテムID（UUID）                   | "990e8400-e29b-41d4-a716-446655440004" |
| Name           | String  | ✓    | アイテム名（最大100文字）            | "玉ねぎ"                               |
| Quantity       | Number  | ✓    | 数量（0.01-9999）                    | 2                                      |
| Unit           | String  | ✓    | 単位コード                           | "piece"                                |
| IsChecked      | Boolean | ✓    | チェック済みフラグ                   | false                                  |
| IsCheckedAt    | String  |      | チェック日時（ISO8601形式）          | "2024-12-01T18:00:00Z"                 |
| AddedAt        | String  | ✓    | 追加日時（ISO8601形式）              | "2024-11-30T10:00:00Z"                 |
| SourceRecipeId | String  |      | 元レシピID（レシピから追加した場合） | "660e8400-e29b-41d4-a716-446655440001" |
| NormalizedKey  | String  | ✓    | 正規化キー（名前+単位）              | "玉ねぎ#piece"                         |

### インデックス

#### GSI_NormalizedKey
- **Partition Key**: `UserId` (String)
- **Sort Key**: `NormalizedKey` (String)
- **用途**: 同じ食材（名前+単位）の検索と数量合算

### アクセスパターン
1. **ユーザー別一覧**: `UserId` で検索（Query）
2. **正規化キー検索**: `GSI_NormalizedKey` を使用して同じ食材を検索（Query）
3. **期限切れアイテム削除**: チェック済みから3日経過したアイテムを削除

### 備考
- 同じ食材（名前+単位が同じ）を追加した場合、数量を合算
- 単位が異なる場合は別アイテムとして扱う
- チェック済みアイテムは3日後に自動削除
- `NormalizedKey` は小文字化した名前と単位コードの組み合わせ

---

## 設計書と実装の差異

### ✅ 一致している点
1. **テーブル構造**: 5つのテーブルすべてが設計書通りに実装されている
2. **キー構造**: Partition Key、Sort Key、GSIの構造が一致
3. **属性**: 必須属性とオプション属性が設計書通り
4. **データ型**: String、Number、Boolean、List、Mapの使い分けが適切

### ⚠️ 差異・注意点

#### 1. Users テーブル
**差異**: メールアドレス検索にGSIが未実装
- **設計書**: GSIの追加を推奨（記載なし）
- **実装**: Scanを使用（非効率）
- **影響**: ユーザー数が増加するとパフォーマンスが低下
- **推奨**: `GSI_Email` の追加を検討

#### 2. Reviews テーブル
**差異**: ReviewIdのみでの検索が非効率
- **設計書**: 特に記載なし
- **実装**: Scanを使用（非効率）
- **影響**: レビュー数が増加するとパフォーマンスが低下
- **推奨**: RecipeIdとReviewIdの組み合わせで検索することを推奨

#### 3. Schedules テーブル
**差異**: ScheduleIdのみでの検索が非効率
- **設計書**: 特に記載なし
- **実装**: Scanを使用（非効率）
- **影響**: スケジュール数が増加するとパフォーマンスが低下
- **備考**: IDでの検索は稀なため、現状は許容範囲

#### 4. Ingredient の note 属性
**差異**: 最大文字数の記載
- **設計書**: 最大60文字
- **実装**: 最大200文字（ValidationConstants.INGREDIENT_NOTE_MAX_LENGTH）
- **影響**: なし（実装が正しい）
- **対応**: 設計書を実装に合わせて修正済み

---

## パフォーマンス最適化の推奨事項

### 1. GSIの追加

#### Users テーブル
```
GSI_Email
- Partition Key: Email (String)
- 用途: メールアドレスでの高速検索
```

#### Reviews テーブル
現状のGSI_Userで十分だが、ReviewIdのみでの検索を避ける設計を推奨

### 2. クエリパターンの最適化

#### 推奨パターン
- ✅ GetItem: 単一アイテムの取得
- ✅ Query: キーを使った範囲検索
- ⚠️ Scan: 全テーブルスキャン（最小限に）

#### 避けるべきパターン
- ❌ Scanでの頻繁な検索
- ❌ 大量データのフィルタリング

### 3. キャパシティモードの選択

#### 開発・テスト環境
- **オンデマンドモード**: 使用量に応じた課金
- **メリット**: 管理不要、柔軟なスケーリング

#### 本番環境（予測可能な負荷）
- **プロビジョニングモード**: 事前にキャパシティを設定
- **メリット**: コスト最適化、Auto Scaling対応

---

## セキュリティ考慮事項

### 1. アクセス制御
- IAMロールによる最小権限の原則
- アプリケーション層での権限チェック
- ユーザーは自分のデータのみアクセス可能

### 2. データ暗号化
- **保存時**: DynamoDB暗号化（AWS KMS）
- **転送時**: TLS 1.2以上

### 3. バックアップ
- **Point-in-Time Recovery (PITR)**: 有効化推奨
- **オンデマンドバックアップ**: 定期的に実施

---

## 運用・監視

### 1. CloudWatch メトリクス
- 読み取り/書き込みキャパシティの使用率
- スロットリングイベント
- レイテンシ

### 2. アラート設定
- キャパシティ使用率が80%を超えた場合
- エラー率が閾値を超えた場合
- レイテンシが閾値を超えた場合

### 3. ログ記録
- DynamoDB Streams: データ変更の追跡
- CloudTrail: API呼び出しの監査ログ

---

## まとめ

本データベース設計は、DynamoDBの特性を活かしたスケーラブルな構造となっています。主な特徴：

1. **効率的なアクセスパターン**: Partition KeyとSort Keyを適切に設計
2. **GSIの活用**: 複数のアクセスパターンに対応
3. **論理削除**: データの整合性を保ちながら削除を実現
4. **正規化キー**: 買い物リストの数量合算を効率的に実現

今後のスケーリングに備えて、GSIの追加やクエリパターンの最適化を継続的に検討することを推奨します。
