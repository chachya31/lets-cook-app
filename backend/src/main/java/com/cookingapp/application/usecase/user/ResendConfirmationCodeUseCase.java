package com.cookingapp.application.usecase.user;

import com.cookingapp.infrastructure.external.cognito.CognitoAuthService;
import org.springframework.stereotype.Service;

/**
 * 確認コード�E送信ユースケース
 */
@Service
public class ResendConfirmationCodeUseCase {
    
    private final CognitoAuthService cognitoAuthService;

    public ResendConfirmationCodeUseCase(CognitoAuthService cognitoAuthService) {
        this.cognitoAuthService = cognitoAuthService;
    }

    /**
     * 確認コードを再送信
     * 
     * @param email メールアドレス
     */
    public void execute(String email) {
        cognitoAuthService.resendConfirmationCode(email);
    }
}
