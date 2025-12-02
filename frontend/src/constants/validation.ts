/**
 * バリデーション定数
 * アプリケーション全体で使用するバリデーション関連の定数を定義
 */

// ========================================
// ユーザー関連
// ========================================

/**
 * パスワードの最小文字数
 */
export const PASSWORD_MIN_LENGTH = 8;

/**
 * ニックネームの最小文字数
 */
export const NICKNAME_MIN_LENGTH = 1;

/**
 * ニックネームの最大文字数
 */
export const NICKNAME_MAX_LENGTH = 50;

/**
 * 表示名の最小文字数
 */
export const DISPLAY_NAME_MIN_LENGTH = 1;

/**
 * 表示名の最大文字数
 */
export const DISPLAY_NAME_MAX_LENGTH = 50;

// ========================================
// レシピ関連
// ========================================

/**
 * レシピタイトルの最大文字数
 */
export const RECIPE_TITLE_MAX_LENGTH = 100;

/**
 * レシピの最小食材数
 */
export const RECIPE_MIN_INGREDIENTS = 1;

/**
 * レシピの最小手順数
 */
export const RECIPE_MIN_STEPS = 1;

/**
 * 調理時間の最小値（分）
 */
export const COOKING_TIME_MIN = 0;

// ========================================
// 食材関連
// ========================================

/**
 * 食材名の最小文字数
 */
export const INGREDIENT_NAME_MIN_LENGTH = 1;

/**
 * 食材名の最大文字数
 */
export const INGREDIENT_NAME_MAX_LENGTH = 100;

/**
 * 食材数量の最小値
 */
export const INGREDIENT_QUANTITY_MIN = 0.01;

/**
 * 食材数量の最大値
 */
export const INGREDIENT_QUANTITY_MAX = 9999;

/**
 * 食材メモの最大文字数
 */
export const INGREDIENT_NOTE_MAX_LENGTH = 200;

// ========================================
// レビュー関連
// ========================================

/**
 * レビュー星評価の最小値
 */
export const REVIEW_RATING_MIN = 1;

/**
 * レビュー星評価の最大値
 */
export const REVIEW_RATING_MAX = 5;

/**
 * レビューコメントの最大文字数
 */
export const REVIEW_COMMENT_MAX_LENGTH = 300;

// ========================================
// スケジュール関連
// ========================================

/**
 * スケジュールメモの最大文字数
 */
export const SCHEDULE_MEMO_MAX_LENGTH = 120;

// ========================================
// 買い物リスト関連
// ========================================

/**
 * 買い物リストアイテム名の最大文字数
 */
export const SHOPPING_LIST_ITEM_NAME_MAX_LENGTH = 100;

/**
 * 買い物リストアイテム数量の最小値
 */
export const SHOPPING_LIST_QUANTITY_MIN = 0.01;

/**
 * 買い物リストアイテム数量の最大値
 */
export const SHOPPING_LIST_QUANTITY_MAX = 9999;

/**
 * チェック済みアイテムの自動削除日数
 */
export const SHOPPING_LIST_AUTO_DELETE_DAYS = 3;

// ========================================
// 画像関連
// ========================================

/**
 * 画像ファイルの最大サイズ（バイト）: 5MB
 */
export const IMAGE_MAX_FILE_SIZE = 5 * 1024 * 1024;

/**
 * 画像ファイルの最大サイズ（MB）
 */
export const IMAGE_MAX_FILE_SIZE_MB = 5;

/**
 * 許可される画像フォーマット
 */
export const ALLOWED_IMAGE_FORMATS = ['image/jpeg', 'image/jpg', 'image/png'];

/**
 * 許可される画像拡張子
 */
export const ALLOWED_IMAGE_EXTENSIONS = ['.jpg', '.jpeg', '.png'];

// ========================================
// アラート関連
// ========================================

/**
 * サボり防止アラート表示の日数閾値
 */
export const ALERT_DAYS_THRESHOLD = 3;
