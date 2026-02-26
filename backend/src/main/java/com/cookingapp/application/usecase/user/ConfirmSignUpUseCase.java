package com.cookingapp.application.usecase.user;

import com.cookingapp.domain.service.AuthService;
import org.springframework.stereotype.Service;

/**
 * メール確認コード検証ユースケース
 */
@Service
public class ConfirmSignUpUseCase {
    
    private final AuthService authService;

    public ConfirmSignUpUseCase(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 確認コードを検証してユーザーを有効化
     * 
     * @param email メールアドレス
     * @param confirmationCode 確認コード
     */
    public void execute(String email, String confirmationCode) {
        authService.confirmSignUp(email, confirmationCode);
    }
}
