# Zustand移行ガイド

## 概要

認証機能（ログイン）の状態管理をRedux ToolkitからZustandに移行しました。

## 移行内容

### 新規作成ファイル

- `frontend/src/store/authStore.ts` - Zustand認証ストア

### 修正ファイル

#### コアファイル
- `frontend/src/hooks/useAuth.ts` - Zustand版に書き換え
- `frontend/src/App.tsx` - `useAuthStore`を使用

#### 認証関連
- `frontend/src/components/auth/Login/LoginPage.tsx` - 変更なし（`useAuth`経由）
- `frontend/src/components/auth/Register/RegisterPage.tsx` - 変更なし（`useAuth`経由）
- `frontend/src/components/auth/ConfirmEmail/ConfirmEmailPage.tsx` - `useAuth`を使用

#### 共通コンポーネント
- `frontend/src/components/common/Header.tsx` - `useAuthStore`を使用
- `frontend/src/components/common/ProtectedRoute.tsx` - `useAuthStore`を使用
- `frontend/src/components/alert/AlertModal.tsx` - `useAuthStore`を使用

#### プロフィール
- `frontend/src/components/profile/ProfileEditPage.tsx` - `useAuthStore`を使用

#### 管理画面
- `frontend/src/components/admin/AdminDashboardPage.tsx` - `useAuthStore`を使用
- `frontend/src/components/admin/UserManagementPage.tsx` - `useAuthStore`を使用

#### スケジュール/買い物リスト
- `frontend/src/components/schedule/SchedulePage.tsx` - `useAuthStore`を使用
- `frontend/src/components/schedule/RecipeSelectModal.tsx` - `useAuthStore`を使用
- `frontend/src/components/shopping/ShoppingListPage.tsx` - `useAuthStore`を使用

#### レシピ
- `frontend/src/components/recipe/RecipeDetailPage.tsx` - `useAuthStore`を使用

## 使用方法

### 基本的な使い方

```typescript
import { useAuthStore } from '../store/authStore';

// コンポーネント内で使用
const MyComponent = () => {
  // 全体を取得
  const { user, isAuthenticated, login, logout } = useAuthStore();
  
  // または特定の値だけ取得（推奨：再レンダリング最適化）
  const user = useAuthStore((state) => state.user);
  const login = useAuthStore((state) => state.login);
};
```

### useAuthフック経由（推奨）

```typescript
import { useAuth } from '../hooks/useAuth';

const MyComponent = () => {
  const { user, isAuthenticated, login, logout } = useAuth();
  
  // ログイン
  await login({ email, password });
  
  // ログアウト
  logout();
};
```

## 機能

### 提供される状態

- `user: User | null` - ユーザー情報
- `accessToken: string | null` - アクセストークン
- `refreshToken: string | null` - リフレッシュトークン
- `isAuthenticated: boolean` - 認証状態
- `isLoading: boolean` - ローディング状態
- `error: string | null` - エラーメッセージ

### 提供されるアクション

- `login(data: LoginRequest): Promise<void>` - ログイン
- `register(data: RegisterRequest): Promise<User>` - ユーザー登録
- `logout(): void` - ログアウト
- `clearError(): void` - エラークリア
- `updateUser(userData: Partial<User>): void` - ユーザー情報更新

## Redux Toolkitとの違い

### Redux Toolkit

```typescript
// 複雑な設定
const dispatch = useDispatch<AppDispatch>();
const auth = useSelector((state: RootState) => state.auth);

// 非同期アクション
await dispatch(login(data)).unwrap();
```

### Zustand

```typescript
// シンプルな設定
const { login } = useAuthStore();

// 非同期アクション
await login(data);
```

## 今後の移行予定

現在、認証機能のみZustandに移行済みです。他の機能（レシピ、レビュー、スケジュール等）は引き続きRedux Toolkitを使用しています。

段階的に他の機能もZustandに移行することを検討できます。

## 注意事項

- `useAuth`フックを使用することで、将来的な実装変更に対応しやすくなります
- 直接`useAuthStore`を使用する場合は、必要な値だけを取得することで再レンダリングを最適化できます
- localStorageへの保存は自動的に行われます
