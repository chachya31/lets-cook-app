package com.cookingapp.application.usecase.admin;

import com.cookingapp.domain.entity.User;
import com.cookingapp.domain.exception.UserNotFoundException;
import com.cookingapp.domain.repository.UserRepository;
import com.cookingapp.domain.service.AuthService;
import com.cookingapp.domain.service.ImageStorageService;
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
    private final AuthService authService;
    private final ImageStorageService imageStorageService;
    
    public DeleteUserByAdminUseCase(UserRepository userRepository, 
                                    AuthService authService,
                                    ImageStorageService imageStorageService) {
        this.userRepository = userRepository;
        this.authService = authService;
        this.imageStorageService = imageStorageService;
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
                imageStorageService.deleteImage(user.getProfileImageUrl());
                log.info("プロフィール画像を削除しました: userId={}", userId);
            } catch (Exception e) {
                log.warn("プロフィール画像の削除に失敗しました: userId={}, error={}", userId, e.getMessage());
            }
        }
        
        // Cognitoからユーザーを削除
        try {
            authService.deleteUser(user.getEmail());
            log.info("Cognitoからユーザーを削除しました: userId={}", userId);
        } catch (Exception e) {
            log.warn("Cognitoからのユーザー削除に失敗しました: userId={}, error={}", userId, e.getMessage());
        }
        
        // DynamoDBからユーザーを削除
        userRepository.delete(userId);
        
        log.info("管理者によるユーザー削除が完了しました: userId={}", userId);
    }
}
