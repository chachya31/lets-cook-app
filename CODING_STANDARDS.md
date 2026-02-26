# コーディング規約

このプロジェクトのコーディング規約です。すべてのコード変更時に遵守してください。

## 目次

- [バックエンド（Java）](#バックエンドjava)
- [フロントエンド（TypeScript/React）](#フロントエンドtypescriptreact)
- [共通](#共通)
- [コミット前チェックリスト](#コミット前チェックリスト)

---

## バックエンド（Java）

### ロギング

#### ❌ 禁止
```java
// @Slf4jアノテーションの使用
@Slf4j
public class MyClass {
    // ...
}
```

#### ✅ 必須
```java
// Logger の直接定義
public class MyClass {
    private static final Logger log = LoggerFactory.getLogger(MyClass.class);
    // ...
}
```

**理由**: セキュリティ（コード生成ツールのリスク軽減）、透明性

---

### DynamoDBテーブル名管理

#### ❌ 禁止
```java
// System.getProperty()の使用
private String getTableName() {
    return System.getProperty("aws.dynamodb.table.users");
}
```

#### ✅ 必須
```java
// @Value アノテーション + コンストラクタインジェクション
public MyRepository(
    DynamoDbClient dynamoDbClient,
    @Value("${aws.dynamodb.table.myTable:DefaultName}") String tableName) {
    this.dynamoDbClient = dynamoDbClient;
    this.tableName = tableName;
}
```

**理由**: Springの設定管理と統合、テストでのモック化が容易

---

### Lombokの使用

#### ✅ 許可
- `@Builder`
- `@Getter`
- `@AllArgsConstructor`

#### ❌ 禁止
- `@Slf4j`
- `@RequiredArgsConstructor`（使用しない場合）

**理由**: セキュリティリスクの最小化

---

### import文の管理

#### ❌ 禁止：ワイルドカードimport
```java
// ワイルドカード（*）を使用したimport
import java.util.*;
import software.amazon.awssdk.services.dynamodb.model.*;
```

#### ✅ 必須：個別import
```java
// 使用するクラスを個別にimport
import java.util.List;
import java.util.Map;
import java.util.Optional;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;
```

**理由**: 
- 依存関係が明確になる
- コードレビューしやすい
- 名前衝突のリスク回避
- IDEの自動補完・リファクタリング機能の活用

---

#### ❌ 禁止：完全修飾名の使用
```java
// メソッドシグネチャやフィールドで完全修飾名を使用
public java.util.List<User> findAll() {
    return items.stream()
        .map(this::mapToUser)
        .collect(java.util.stream.Collectors.toList());
}
```

#### ✅ 必須：import文を使用
```java
import java.util.List;
import java.util.stream.Collectors;

public List<User> findAll() {
    return items.stream()
        .map(this::mapToUser)
        .collect(Collectors.toList());
}
```

**理由**: 
- コードの可読性向上
- 一貫性の維持
- IDEの自動補完・リファクタリング機能の活用

**例外**: 
- 同名のクラスが複数のパッケージに存在する場合のみ、片方を完全修飾名で記述
  ```java
  import java.util.Date;
  
  public void process(Date utilDate, java.sql.Date sqlDate) {
      // java.util.Date と java.sql.Date を区別
  }
  ```

---

### フィールド定義

#### ❌ 禁止：可変フィールド
```java
// publicフィールドや再代入可能なフィールド
public DynamoDbClient dynamoDbClient;
private String tableName;
```

#### ✅ 必須：private final
```java
// 不変フィールドとして定義
private final DynamoDbClient dynamoDbClient;
private final String tableName;
```

**理由**: 
- イミュータビリティ保証（スレッドセーフ）
- 意図しない再代入を防止
- 設計意図の明確化
- JVMの最適化が効きやすい

**例外**: 
- 状態を持つ必要があるフィールド（カウンター、キャッシュ等）
- Springの`@Autowired`フィールドインジェクション（ただしコンストラクタインジェクション推奨）

---

### デバッグコード

#### ❌ 禁止
```java
System.out.println("Debug: " + value);
```

#### ✅ 必須
```java
log.debug("Debug: value={}", value);
log.info("User action: userId={}, action={}", userId, action);
```

**理由**: 本番環境でのログ管理

---

### ログ出力時の注意

#### ❌ 禁止：機密情報のログ出力
```java
log.info("Password: {}", password);
log.info("Credit card: {}", creditCard);
log.info("API Key: {}", apiKey);
```

#### ✅ 必須：パラメータ化されたログ
```java
log.info("User action: userId={}, action={}", userId, action);
log.info("Payment processed: last4={}", creditCard.getLast4Digits());
```

---

### Clean Architecture

#### レイヤー構成
```
presentation/    # Controllers, DTOs
application/     # Use Cases
domain/          # Entities, Value Objects, Repository Interfaces
infrastructure/  # Repository Implementations, External APIs
```

#### 依存関係のルール
- 外側のレイヤーは内側のレイヤーに依存できる
- 内側のレイヤーは外側のレイヤーに依存してはいけない

---

## フロントエンド（TypeScript/React）

### API呼び出し

#### ❌ 禁止
```typescript
// axiosの使用
import axios from 'axios';
const response = await axios.get('/api/users');

// fetchの直接使用
const response = await fetch('/api/users');
```

#### ✅ 必須
```typescript
// apiClient.ts のヘルパー関数を使用
import { apiGet, apiPost } from '../utils/apiClient';

export const getUsers = async (): Promise<User[]> => {
  return apiGet<User[]>('/api/users');
};
```

**理由**: エラーハンドリングの一元管理、コードの重複削減

---

### エラーハンドリング

#### ✅ 必須
- すべてのAPI呼び出しでエラーハンドリング
- ユーザーフレンドリーなエラーメッセージ
- Redux状態でのエラー管理

```typescript
try {
  const data = await apiGet<User[]>('/api/users');
  // 成功処理
} catch (error) {
  // エラーハンドリング
  console.error('Failed to fetch users:', error);
  throw error;
}
```

---

### デバッグコード

#### ❌ 禁止（本番コード）
```typescript
console.log('Debug:', data);
console.error('Error:', error);
debugger;
```

#### ✅ 許可（開発中のみ、コミット前に削除）
```typescript
// 開発中のデバッグ
console.log('TODO: Remove this debug log', data);
```

---

### コンポーネント設計

#### シンプルな画面
```
Login/
├── LoginPage.tsx          # ページコンポーネント（UI + ロジック）
└── loginFormConfig.ts     # フォーム設定（バリデーション、フィールド定義）
```

#### 複雑な画面
```
RecipeEdit/
├── RecipeEditPage.tsx           # 表示コンポーネント（UIのみ）
├── useRecipeEditHandlers.ts     # イベントハンドラー（ロジック）
└── recipeEditConfig.ts          # フォーム設定（属性）
```

**責任の分離**:
- **表示（Page.tsx）**: UIの構造、propsの受け渡し
- **操作（useHandlers.ts）**: イベント処理、状態管理、副作用
- **属性（Config.ts）**: 設定値、バリデーションルール、定数

---

### 型定義

#### ✅ 必須
```typescript
// すべてのAPI関数に型定義
export const getUser = async (userId: string): Promise<User> => {
  return apiGet<User>(`/api/users/${userId}`);
};

// ジェネリクスの活用
export async function apiGet<T>(endpoint: string): Promise<T> {
  // ...
}
```

---

### クォート（引用符）

#### ✅ 必須：シングルクォート
```typescript
// シングルクォートを使用
import { User } from '../types/user';
const message = 'Hello, World!';
```

#### ❌ 禁止：ダブルクォート
```typescript
// ダブルクォートは使用しない
import { User } from "../types/user";
const message = "Hello, World!";
```

**例外**: 
- テンプレートリテラル: `` `Hello, ${name}!` ``
- 文字列内にシングルクォートが含まれる場合: `"It's a beautiful day"`

**自動フォーマット**: 
- ESLint: `quotes` ルールで強制
- Prettier: `singleQuote: true` で自動変換
- 保存時に自動修正

---

### import文の整理

#### ✅ 推奨順序
```typescript
// 1. 外部ライブラリ
import React from 'react';
import { useDispatch } from 'react-redux';

// 2. 内部モジュール（絶対パス）
import { apiGet } from '../utils/apiClient';
import { User } from '../types/user';

// 3. 相対パス
import { LoginForm } from './LoginForm';

// 4. スタイル
import './styles.css';
```

---

### 変数定義

#### ❌ 禁止：再代入可能な変数
```typescript
// letを使用した再代入可能な変数
let apiUrl = '/api/users';
let config = { timeout: 5000 };
```

#### ✅ 必須：const優先
```typescript
// constを使用した不変変数
const apiUrl = '/api/users';
const config = { timeout: 5000 } as const;

// 再代入が必要な場合のみletを使用
let retryCount = 0;
while (retryCount < 3) {
  retryCount++;
}
```

**理由**: 
- 意図しない再代入を防止
- コードの予測可能性向上
- ESLintの`prefer-const`ルールで自動検出

---

### any型の使用

#### ❌ 禁止：any型
```typescript
// any型の使用
const data: any = response.data;
function process(input: any): any {
  return input;
}
```

#### ✅ 必須：適切な型定義
```typescript
// 具体的な型を定義
interface UserResponse {
  id: string;
  name: string;
  email: string;
}

const data: UserResponse = response.data;

// 型が不明な場合はunknownを使用
function process(input: unknown): string {
  if (typeof input === 'string') {
    return input;
  }
  return String(input);
}
```

**理由**: 
- 型安全性の確保
- コンパイル時のエラー検出
- IDEの補完機能の活用

**例外**: 
- 外部ライブラリの型定義が不完全な場合（コメントで理由を記載）
- テストコードでのモック作成時

---

## 共通

### コミット前チェックリスト

#### 必須チェック項目
- [ ] ビルドが成功すること
  - バックエンド: `./gradlew build -x test`
  - フロントエンド: `npm run build`
- [ ] ESLint/Checkstyleエラーがないこと
- [ ] デバッグコードが削除されていること
  - `console.log()`, `System.out.println()`, `debugger`
- [ ] 未使用のimportが削除されていること
- [ ] コーディング規約に違反していないこと

---

### コードレビュー観点

#### 1. コーディング規約の遵守
- ロギング方法
- API呼び出し方法
- 型定義

#### 2. セキュリティ
- 機密情報のログ出力
- SQLインジェクション対策
- XSS対策
- CSRF対策

#### 3. パフォーマンス
- 不要なループ
- 非効率なデータベースクエリ
- メモリリーク
- 不要な再レンダリング

#### 4. 可読性
- 適切な変数名
- コメントの適切性
- 関数の責任分離
- マジックナンバーの排除

#### 5. テスタビリティ
- 依存関係の注入
- モック化の容易性
- テストカバレッジ

---

## ツール設定

### ESLint（フロントエンド）

推奨設定:
```javascript
// .eslintrc.js
module.exports = {
  rules: {
    'no-console': 'warn',
    'no-debugger': 'error',
    '@typescript-eslint/no-explicit-any': 'error',
  }
}
```

### Checkstyle（バックエンド）

推奨設定:
- インデント: 4スペース
- 行の最大長: 120文字
- 未使用のimport検出

---

## 参考資料

- [設計書](.kiro/specs/cooking-support-app/design.md)
- [タスク管理](.kiro/specs/cooking-support-app/tasks.md)
- [Kiro Hooks設定](.kiro/hooks/README.md)

---

## 更新履歴

- 2024-12-24: AIモデル間の一貫性向上
  - バックエンド: ワイルドカードimport禁止、private final必須を追加
  - フロントエンド: const優先、any型禁止を追加
  - pre-commitフック: 新規ルールのチェック追加
  - ESLint: prefer-const、no-explicit-anyルール追加
- 2024-11-30: 初版作成
  - バックエンド規約（ロギング、DynamoDB、Lombok）
  - フロントエンド規約（API呼び出し、コンポーネント設計）
  - 共通規約（コミット前チェック、コードレビュー）
