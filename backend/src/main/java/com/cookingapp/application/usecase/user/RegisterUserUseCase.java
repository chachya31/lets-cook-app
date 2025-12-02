package com.cookingapp.application.usecase.user;

import com.cookingapp.domain.entity.User;
import com.cookingapp.domain.exception.UserAlreadyExistsException;
import com.cookingapp.domain.repository.UserRepository;
import com.cookingapp.domain.service.AuthService;
import com.cookingapp.domain.valueobject.Language;
import org.springframework.stereotype.Service;

/**
 * ユーザー登録ユースケース
 */
@Service
public class RegisterUserUseCase {
    
    private final AuthService authService;
    private final UserRepository userRepository;

    public RegisterUserUseCase(AuthService authService, UserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
    }

    /**
     * ユーザーを登録
     * 
     * @param email メールアドレス
     * @param password パスワード
     * @param nickname ニックネーム
     * @param preferredLanguage 優先言語
     * @return 登録されたユーザー
     * @throws UserAlreadyExistsException ユーザーが既に存在する場合
     */
    public User execute(String email, String password, String nickname, Language preferredLanguage) {
        // パスワードバリデーション
        if (!User.validatePassword(password)) {
            throw new IllegalArgumentException(
                "Password must be at least 8 characters long and contain uppercase, lowercase, numbers, and special characters"
            );
        }

        // メールアドレスの重複チェック
        if (userRepository.findByEmail(email).isPresent()) {
            throw new UserAlreadyExistsException("User with email " + email + " already exists");
        }

        // Cognitoにユーザー登録
        String cognitoUserId = authService.signUp(email, password, nickname);

        // ユーザーエンティティ作成
        User user = new User(email, nickname, preferredLanguage);

        // DynamoDBに保存
        return userRepository.save(user);
    }
}
