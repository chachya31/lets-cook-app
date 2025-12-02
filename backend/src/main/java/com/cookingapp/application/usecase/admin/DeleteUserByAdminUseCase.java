package com.cookingapp.application.usecase.admin;

import com.cookingapp.domain.entity.User;
import com.cookingapp.domain.exception.UserNotFoundException;
import com.cookingapp.domain.repository.UserRepository;
import com.cookingapp.infrastructure.external.cognito.CognitoAuthService;
import com.cookingapp.infrastructure.external.s3.S3ImageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * ユーザー削除ユースケース（管理者機能）
 */
@Service
public class DeleteUserByAdminUseCase {
    
    private static final Logger log = LoggerFactory.getLogger(DeleteUserByAdminUseCase.class);
    
    private final UserRepository userRepository;
    private final CognitoAuthService cognitoAuthService;
    private final S3ImageService s3ImageService;
    
    public DeleteUserByAdminUseCase(UserRepository userRepository, 
                                    CognitoAuthService cognitoAuthService,
                                    S3ImageService s3ImageService) {
        this.userRepository = userRepository;
        this.cognitoAuthService = cognitoAuthService;
        this.s3ImageService = s3ImageService;
    }
    
    /**
     * ユーザーを削除（管理者権限）
     * 
     * @param userId ユーザーID
     */
    public void execute(String userId) {
        log.info("管理者によるユーザー削除を開始: userId={}", userId);
        
        // ユーザーの存在確認
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("ユーザーが見つかりません: " + userId));
        
        // プロフィール画像を削除
        if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
            try {
                s3ImageService.deleteImage(user.getProfileImageUrl());
                log.info("プロフィール画像を削除しました: userId={}", userId);
            } catch (Exception e) {
                log.warn("プロフィール画像の削除に失敗しました: userId={}, error={}", userId, e.getMessage());
            }
        }
        
        // Cognitoからユーザーを削除
        try {
            cognitoAuthService.deleteUser(user.getEmail());
            log.info("Cognitoからユーザーを削除しました: userId={}", userId);
        } catch (Exception e) {
            log.warn("Cognitoからのユーザー削除に失敗しました: userId={}, error={}", userId, e.getMessage());
        }
        
        // DynamoDBからユーザーを削除
        userRepository.delete(userId);
        
        log.info("管理者によるユーザー削除が完了しました: userId={}", userId);
    }
}
