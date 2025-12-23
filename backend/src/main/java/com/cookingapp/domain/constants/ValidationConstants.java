package com.cookingapp.domain.constants;

/**
 * バリデーション定数
 * アプリケーション全体で使用するバリデーション関連の定数を定義
 */
public final class ValidationConstants {

    private ValidationConstants() {
        // ユーティリティクラスのため、インスタンス化を防ぐ
        throw new AssertionError("Cannot instantiate ValidationConstants");
    }

    // ========================================
    // ユーザー関連
    // ========================================

    /**
     * パスワードの最小文字数
     */
    public static final int PASSWORD_MIN_LENGTH = 8;

    /**
     * ニックネームの最小文字数
     */
    public static final int NICKNAME_MIN_LENGTH = 1;

    /**
     * ニックネームの最大文字数
     */
    public static final int NICKNAME_MAX_LENGTH = 50;

    /**
     * 表示名の最小文字数
     */
    public static final int DISPLAY_NAME_MIN_LENGTH = 1;

    /**
     * 表示名の最大文字数
     */
    public static final int DISPLAY_NAME_MAX_LENGTH = 50;

    // ========================================
    // レシピ関連
    // ========================================

    /**
     * レシピタイトルの最大文字数
     */
    public static final int RECIPE_TITLE_MAX_LENGTH = 100;

    /**
     * レシピの最小食材数
     */
    public static final int RECIPE_MIN_INGREDIENTS = 1;

    /**
     * レシピの最小手順数
     */
    public static final int RECIPE_MIN_STEPS = 1;

    /**
     * 調理時間の最小値（分）
     */
    public static final int COOKING_TIME_MIN = 0;

    // ========================================
    // 食材関連
    // ========================================

    /**
     * 食材名の最小文字数
     */
    public static final int INGREDIENT_NAME_MIN_LENGTH = 1;

    /**
     * 食材名の最大文字数
     */
    public static final int INGREDIENT_NAME_MAX_LENGTH = 100;

    /**
     * 食材数量の最小値
     */
    public static final String INGREDIENT_QUANTITY_MIN = "0.01";

    /**
     * 食材数量の最大値
     */
    public static final String INGREDIENT_QUANTITY_MAX = "9999";

    /**
     * 食材メモの最大文字数
     */
    public static final int INGREDIENT_NOTE_MAX_LENGTH = 200;

    /**
     * 食材単位の最大文字数
     */
    public static final int INGREDIENT_UNIT_MAX_LENGTH = 50;

    // ========================================
    // レビュー関連
    // ========================================

    /**
     * レビュー星評価の最小値
     */
    public static final int REVIEW_RATING_MIN = 1;

    /**
     * レビュー星評価の最大値
     */
    public static final int REVIEW_RATING_MAX = 5;

    /**
     * レビューコメントの最大文字数
     */
    public static final int REVIEW_COMMENT_MAX_LENGTH = 300;

    /**
     * レビュー自動非表示の通報カウント閾値
     */
    public static final int REVIEW_AUTO_HIDE_THRESHOLD = 3;

    // ========================================
    // スケジュール関連
    // ========================================

    /**
     * スケジュールメモの最大文字数
     */
    public static final int SCHEDULE_MEMO_MAX_LENGTH = 120;

    // ========================================
    // 買い物リスト関連
    // ========================================

    /**
     * 買い物リストアイテム名の最大文字数
     */
    public static final int SHOPPING_LIST_ITEM_NAME_MAX_LENGTH = 100;

    /**
     * 買い物リストアイテム数量の最小値
     */
    public static final String SHOPPING_LIST_QUANTITY_MIN = "0.01";

    /**
     * 買い物リストアイテム数量の最大値
     */
    public static final String SHOPPING_LIST_QUANTITY_MAX = "9999";

    /**
     * チェック済みアイテムの自動削除日数
     */
    public static final int SHOPPING_LIST_AUTO_DELETE_DAYS = 3;

    // ========================================
    // 画像関連
    // ========================================

    /**
     * 画像ファイルの最大サイズ（バイト）: 5MB
     */
    public static final long IMAGE_MAX_FILE_SIZE = 5 * 1024 * 1024;

    /**
     * 画像ファイルの最大サイズ（MB）
     */
    public static final int IMAGE_MAX_FILE_SIZE_MB = 5;

    // ========================================
    // アラート関連
    // ========================================

    /**
     * サボり防止アラート表示の日数閾値
     */
    public static final int ALERT_DAYS_THRESHOLD = 3;
}
