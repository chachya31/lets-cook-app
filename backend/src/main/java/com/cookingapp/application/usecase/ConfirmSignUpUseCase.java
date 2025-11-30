package com.cookingapp.application.usecase;

import com.cookingapp.infrastructure.external.cognito.CognitoAuthService;
import org.springframework.stereotype.Service;

/**
 * メール確認コード検証ユースケース
 */
@Service
public class ConfirmSignUpUseCase {
    
    private final CognitoAuthService cognitoAuthService;

    public ConfirmSignUpUseCase(CognitoAuthService cognitoAuthService) {
        this.cognitoAuthService = cognitoAuthService;
    }

    /**
     * 確認コードを検証してユーザーを有効化
     * 
     * @param email メールアドレス
     * @param confirmationCode 確認コード
     */
    public void execute(String email, String confirmationCode) {
        cognitoAuthService.confirmSignUp(email, confirmationCode);
    }
}
