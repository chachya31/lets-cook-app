package com.cookingapp.domain.service;

import com.cookingapp.infrastructure.external.cognito.dto.AuthTokens;

import java.util.Map;

/**
 * 認証サービスのインターフェース
 * テスト可能性を高めるため、CognitoAuthServiceの抽象化を提供
 */
public interface AuthService {
    
    /**
     * ユーザーを登録
     * 
     * @param email メールアドレス
     * @param password パスワード
     * @param nickname ニックネーム
     * @return ユーザーID
     */
    String signUp(String email, String password, String nickname);
    
    /**
     * ユーザーをログイン
     * 
     * @param email メールアドレス
     * @param password パスワード
     * @return 認証トークン
     */
    AuthTokens signIn(String email, String password);
    
    /**
     * ユーザー属性を取得
     * 
     * @param accessToken アクセストークン
     * @return ユーザー属性
     */
    Map<String, String> getUserAttributes(String accessToken);
    
    /**
     * ユーザーを削除
     * 
     * @param email メールアドレス
     */
    void deleteUser(String email);
    
    /**
     * メール確認
     * 
     * @param email メールアドレス
     * @param confirmationCode 確認コード
     */
    void confirmSignUp(String email, String confirmationCode);
    
    /**
     * 確認コードを再送信
     * 
     * @param email メールアドレス
     */
    void resendConfirmationCode(String email);
}
