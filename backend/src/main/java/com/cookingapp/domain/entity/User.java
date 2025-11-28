package com.cookingapp.domain.entity;

import com.cookingapp.domain.valueobject.Language;
import lombok.Getter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * ユーザーエンティティ
 * ユーザーアカウント情報とプロフィールを管理する
 */
@Getter
public class User {
    // パスワード要件: 少なくとも8文字で、大文字、小文字、数字、特殊文字を含む
    // 特殊文字: $ * . [ ] { } ( ) ? - " ! @ # % & / \ , > < ' : ; | _ ~ ` + =
    private static final Pattern PASSWORD_PATTERN = 
        Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[$*.\\[\\]{}()?\\-\"!@#%&/\\\\,><':;|_~`+=]).{8,}$");
    
    private final String userId;
    private String email;
    private String nickname;
    private String displayName;
    private String profileImageUrl;
    private Language preferredLanguage;
    private LocalDate lastCookingDate;
    private LocalDateTime lastLoginDate;
    private final LocalDateTime createdAt;
    private String timezone;
    private boolean marketingOptOut;

    /**
     * 新規ユーザーを作成
     */
    public User(String email, String nickname, Language preferredLanguage) {
        this.userId = UUID.randomUUID().toString();
        this.email = email;
        this.nickname = nickname;
        this.displayName = nickname;
        this.preferredLanguage = preferredLanguage != null ? preferredLanguage : Language.JA;
        this.createdAt = LocalDateTime.now();
        this.timezone = "Asia/Tokyo";
        this.marketingOptOut = false;
    }

    /**
     * 既存ユーザーを復元（リポジトリから取得時）
     */
    public User(String userId, String email, String nickname, String displayName,
                String profileImageUrl, Language preferredLanguage, LocalDate lastCookingDate,
                LocalDateTime lastLoginDate, LocalDateTime createdAt, String timezone,
                boolean marketingOptOut) {
        this.userId = userId;
        this.email = email;
        this.nickname = nickname;
        this.displayName = displayName;
        this.profileImageUrl = profileImageUrl;
        this.preferredLanguage = preferredLanguage;
        this.lastCookingDate = lastCookingDate;
        this.lastLoginDate = lastLoginDate;
        this.createdAt = createdAt;
        this.timezone = timezone;
        this.marketingOptOut = marketingOptOut;
    }

    /**
     * パスワードバリデーション
     * 要件: 少なくとも8文字で、大文字、小文字、数字、特殊文字を含む
     * 特殊文字: $ * . [ ] { } ( ) ? - " ! @ # % & / \ , > < ' : ; | _ ~ ` + =
     * 
     * @param password 検証するパスワード
     * @return バリデーション結果
     */
    public static boolean validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            return false;
        }
        return PASSWORD_PATTERN.matcher(password).matches();
    }

    /**
     * アカウント削除可能かチェック
     * 
     * @return 削除可能な場合true
     */
    public boolean canDeleteAccount() {
        // 現時点では常に削除可能
        // 将来的に削除制限ロジックを追加する可能性がある
        return true;
    }

    /**
     * 最終料理日を更新
     */
    public void updateLastCookingDate(LocalDate date) {
        this.lastCookingDate = date;
    }

    /**
     * 最終ログイン日時を更新
     */
    public void updateLastLoginDate(LocalDateTime dateTime) {
        this.lastLoginDate = dateTime;
    }

    /**
     * プロフィール情報を更新
     */
    public void updateProfile(String nickname, String displayName, Language preferredLanguage,
                             String timezone, boolean marketingOptOut) {
        if (nickname != null && !nickname.trim().isEmpty()) {
            this.nickname = nickname;
        }
        if (displayName != null && !displayName.trim().isEmpty()) {
            this.displayName = displayName;
        }
        if (preferredLanguage != null) {
            this.preferredLanguage = preferredLanguage;
        }
        if (timezone != null && !timezone.trim().isEmpty()) {
            this.timezone = timezone;
        }
        this.marketingOptOut = marketingOptOut;
    }

    /**
     * プロフィール画像URLを更新
     */
    public void updateProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}
