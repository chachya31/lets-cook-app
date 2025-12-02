package com.cookingapp.application.usecase.user;

import com.cookingapp.domain.entity.User;
import com.cookingapp.domain.repository.UserRepository;
import com.cookingapp.domain.service.AuthService;
import org.springframework.stereotype.Service;

/**
 * アカウント削除ユースケース
 */
@Service
public class DeleteUserAccountUseCase {
    
    private final AuthService authService;
    private final UserRepository userRepository;

    public DeleteUserAccountUseCase(AuthService authService, UserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
    }

    /**
     * ユーザーアカウントを削除
     * 
     * 要件: アカウントとプロフィール画像を削除し、投稿したレシピとレビューを匿名化
     * 
     * @param userId ユーザーID
     */
    public void execute(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        // 削除可能かチェック
        if (!user.canDeleteAccount()) {
            throw new IllegalStateException("Cannot delete account at this time");
        }

        // Cognitoからユーザー削除
        authService.deleteUser(user.getEmail());

        // DynamoDBからユーザー削除
        userRepository.delete(userId);

        // TODO: プロフィール画像をS3から削除（タスク4で実装）
        // TODO: 投稿したレシピとレビューを匿名化（タスク5、6で実装）
    }
}
