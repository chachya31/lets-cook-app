package com.cookingapp.application.usecase.admin;

import com.cookingapp.domain.entity.User;
import com.cookingapp.domain.exception.UserNotFoundException;
import com.cookingapp.domain.repository.UserRepository;
import com.cookingapp.infrastructure.external.cognito.CognitoAuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * ユーザー停止ユースケース（管理者機能）
 */
@Service
public class SuspendUserUseCase {
    
    private static final Logger log = LoggerFactory.getLogger(SuspendUserUseCase.class);
    
    private final UserRepository userRepository;
    private final CognitoAuthService cognitoAuthService;
    
    public SuspendUserUseCase(UserRepository userRepository, CognitoAuthService cognitoAuthService) {
        this.userRepository = userRepository;
        this.cognitoAuthService = cognitoAuthService;
    }
    
    /**
     * ユーザーを停止
     * 
     * @param userId ユーザーID
     */
    public void execute(String userId) {
        log.info("ユーザーを停止中: userId={}", userId);
        
        // ユーザーの存在確認
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("ユーザーが見つかりません: " + userId));
        
        // Cognitoでユーザーを無効化
        cognitoAuthService.disableUser(user.getEmail());
        
        log.info("ユーザーを停止しました: userId={}", userId);
    }
}
