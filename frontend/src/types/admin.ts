/**
 * 管理者ダッシュボード統計
 */
export interface AdminDashboardStats {
  message: string;
  totalUsers: number;
  totalRecipes: number;
}

/**
 * レシピステータス設定リクエスト
 */
export interface SetRecipeStatusRequest {
  isPublic: boolean;
}

/**
 * 管理者状態
 */
export interface AdminState {
  stats: AdminDashboardStats | null;
  recipes: any[]; // Recipe型を使用
  loading: boolean;
  error: string | null;
}
