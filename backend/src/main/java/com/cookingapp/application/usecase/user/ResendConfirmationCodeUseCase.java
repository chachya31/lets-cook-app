package com.cookingapp.application.usecase.user;

import com.cookingapp.domain.service.AuthService;
import org.springframework.stereotype.Service;

/**
 * 確認コード再送信ユースケース
 */
@Service
public class ResendConfirmationCodeUseCase {
    
    private final AuthService authService;

    public ResendConfirmationCodeUseCase(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 確認コードを再送信
     * 
     * @param email メールアドレス
     */
    public void execute(String email) {
        authService.resendConfirmationCode(email);
    }
}
