# 自炊支援・食費節約アプリ - Claude Code実装ガイド

## 🎯 このプロジェクトについて

一人暮らしや小家族向けの自炊支援Webアプリケーションです。レシピ管理、スケジュール管理、買い物リスト、在庫管理、AIチャット機能を提供します。

## 📋 実装に必要なドキュメント

### 必読（実装前に必ず確認）

1. **[実装ガイド](./IMPLEMENTATION_GUIDE.md)** ⭐ まずはここから
   - アーキテクチャ概要
   - セットアップ手順
   - 実装の進め方
   - コード例

2. **[コーディング規約](../CODING_STANDARDS.md)** ⭐ 必須
   - バックエンド（Java）のルール
   - フロントエンド（TypeScript/React）のルール
   - 禁止事項と推奨事項

3. **[API仕様書](./API_SPECIFICATION.md)** + **[OpenAPI仕様](./openapi.yaml)**
   - 全エンドポイント定義
   - リクエスト/レスポンス形式
   - バリデーションルール

4. **[データベース設計](./DATABASE_DESIGN.md)**
   - DynamoDBテーブル構造
   - アクセスパターン
   - インデックス設計

5. **[要件定義書](./要件定義書_自炊支援アプリ.md)**
   - 機能要件
   - 非機能要件
   - UI/UX要件

## 🚀 クイックスタート

### 1. バックエンド起動

```bash
cd backend
./gradlew.bat bootRun
```

### 2. フロントエンド起動

```bash
cd frontend
npm install
npm run dev
```

## 🏗️ 技術スタック

### バックエンド
- Java 17 + Spring Boot 3.x
- AWS SDK for Java 2.x
- DynamoDB（データストア）
- AWS Cognito（認証）
- Clean Architecture

### フロントエンド
- TypeScript 5.x + React 18.x
- Redux Toolkit（状態管理）
- React Router 6.x（ルーティング）
- Tailwind CSS（スタイリング）
- Vite（ビルドツール）

## 📁 プロジェクト構造

```
lets-cook-app/
├── backend/                    # バックエンド（Java + Spring Boot）
│   └── src/main/java/com/cookingapp/
│       ├── presentation/       # Controllers, DTOs
│       ├── application/        # Use Cases
│       ├── domain/             # Entities, Repository Interfaces
│       └── infrastructure/     # Repository Implementations
│
├── frontend/                   # フロントエンド（TypeScript + React）
│   └── src/
│       ├── components/         # Reactコンポーネント
│       ├── store/              # Redux Store
│       ├── utils/              # ユーティリティ（apiClient等）
│       └── types/              # TypeScript型定義
│
└── docs/                       # ドキュメント
    ├── IMPLEMENTATION_GUIDE.md # 実装ガイド（⭐まずはここから）
    ├── API_SPECIFICATION.md    # API仕様書
    ├── openapi.yaml            # OpenAPI仕様
    ├── DATABASE_DESIGN.md      # データベース設計
    └── 要件定義書_自炊支援アプリ.md
```

## ⚠️ 重要な制約事項

### バックエンド（Java）

#### ❌ 禁止
- `@Slf4j` アノテーション → ✅ `Logger` を直接定義
- `System.getProperty()` → ✅ `@Value` アノテーション
- `System.out.println()` → ✅ `log.info()`
- ワイルドカードimport (`import java.util.*`) → ✅ 個別import
- `public` フィールド → ✅ `private final` フィールド

#### ✅ 必須
```java
// Logger の直接定義
private static final Logger log = LoggerFactory.getLogger(MyClass.class);

// @Value でテーブル名注入
public MyRepository(
    DynamoDbClient dynamoDbClient,
    @Value("${aws.dynamodb.table.myTable}") String tableName) {
    this.dynamoDbClient = dynamoDbClient;
    this.tableName = tableName;
}

// 個別import
import java.util.List;
import java.util.Map;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
```

### フロントエンド（TypeScript/React）

#### ❌ 禁止
- `axios` の使用 → ✅ `apiClient.ts` のヘルパー関数
- `fetch` の直接使用 → ✅ `apiGet()`, `apiPost()` 等
- `console.log()` （本番コード）
- `let` （再代入不要な場合） → ✅ `const` 優先
- `any` 型 → ✅ 具体的な型定義 or `unknown`

#### ✅ 必須
```typescript
// apiClient.ts のヘルパー関数を使用
import { apiGet, apiPost } from '../utils/apiClient';

export const getUsers = async (): Promise<User[]> => {
  return apiGet<User[]>('/api/users');
};

// const 優先
const apiUrl = '/api/users';
const config = { timeout: 5000 };

// 適切な型定義
interface UserResponse {
  id: string;
  name: string;
  email: string;
}
```

## 📝 実装の進め方

### Phase 1: 基盤機能
1. ユーザー管理（登録・ログイン・プロフィール）
2. レシピ管理（作成・編集・削除・検索）

### Phase 2: コア機能
3. スケジュール管理（カレンダー・予定・実績）
4. 買い物リスト（追加・削除・チェック）

### Phase 3: 拡張機能
5. 在庫管理（登録・賞味期限管理）
6. AIチャット（Gemini API連携）
7. レビュー機能（星評価・コメント）

## 🔍 実装時のチェックポイント

### コミット前に必ず確認

- [ ] ビルドが成功する
  - バックエンド: `./gradlew.bat build -x test`
  - フロントエンド: `npm run build`
- [ ] コーディング規約に準拠
- [ ] デバッグコード削除（`console.log()`, `System.out.println()`）
- [ ] 未使用import削除
- [ ] ワイルドカードimport未使用
- [ ] `@Slf4j` 未使用
- [ ] `axios`/`fetch` 直接使用なし
- [ ] `private final` フィールド使用
- [ ] `const` 優先
- [ ] `any` 型未使用

## 🐛 トラブルシューティング

### DynamoDB接続エラー
```
ResourceNotFoundException: Requested resource not found
```
→ テーブル名、AWS認証情報を確認

### Cognito認証エラー
```
NotAuthorizedException: Incorrect username or password
```
→ パスワード要件（8文字以上、大文字・小文字・数字）を確認

### CORS エラー
```
Access to fetch has been blocked by CORS policy
```
→ API GatewayのCORS設定を確認

## 📚 参考リンク

- [実装ガイド](./IMPLEMENTATION_GUIDE.md) - 詳細な実装手順
- [コーディング規約](../CODING_STANDARDS.md) - コーディングルール
- [API仕様書](./API_SPECIFICATION.md) - エンドポイント定義
- [データベース設計](./DATABASE_DESIGN.md) - テーブル設計

## 💡 ヒント

1. **まずは実装ガイドを読む**: [IMPLEMENTATION_GUIDE.md](./IMPLEMENTATION_GUIDE.md) に詳細な実装例があります
2. **コーディング規約を守る**: [CODING_STANDARDS.md](../CODING_STANDARDS.md) の禁止事項に注意
3. **API仕様書を参照**: [openapi.yaml](./openapi.yaml) に全エンドポイントの詳細があります
4. **データベース設計を確認**: [DATABASE_DESIGN.md](./DATABASE_DESIGN.md) にテーブル構造とアクセスパターンがあります

---

**実装開始前に**: 必ず [IMPLEMENTATION_GUIDE.md](./IMPLEMENTATION_GUIDE.md) と [CODING_STANDARDS.md](../CODING_STANDARDS.md) を確認してください！
