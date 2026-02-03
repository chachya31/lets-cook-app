package com.cookingapp.infrastructure.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.cookingapp.domain.exception.UnauthorizedException;

/**
 * セキュリティ関連ユーティリティ
 * SecurityContextから認証情報を取得するヘルパーメソッドを提供
 */
public final class SecurityUtils {

    private SecurityUtils() {
        // ユーティリティクラスのため、インスタンス化を防ぐ
    }

    /**
     * 現在認証されているユーザーのIDを取得
     * JWTのsubject（Cognito User ID）を返す
     *
     * @return ユーザーID
     * @throws UnauthorizedException 認証されていない場合
     */
    public static String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("User is not authenticated");
        }

        Object principal = authentication.getPrincipal();
        if (principal == null) {
            throw new UnauthorizedException("User is not authenticated");
        }

        // anonymousUserの場合は認証されていないとみなす
        if ("anonymousUser".equals(principal.toString())) {
            throw new UnauthorizedException("User is not authenticated");
        }

        return principal.toString();
    }

    /**
     * 現在のユーザーが認証されているかどうかを確認
     *
     * @return 認証されている場合true
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Object principal = authentication.getPrincipal();
        return principal != null && !"anonymousUser".equals(principal.toString());
    }
}
