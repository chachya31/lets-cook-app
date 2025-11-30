package com.cookingapp.application.usecase.user;

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
     * 確認コードを検証してユーザーを有効匁E
     * 
     * @param email メールアドレス
     * @param confirmationCode 確認コーチE
     */
    public void execute(String email, String confirmationCode) {
        cognitoAuthService.confirmSignUp(email, confirmationCode);
    }
}
