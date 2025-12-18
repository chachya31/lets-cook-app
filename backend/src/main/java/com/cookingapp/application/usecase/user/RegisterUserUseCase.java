package com.cookingapp.application.usecase.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.cookingapp.domain.entity.User;
import com.cookingapp.domain.exception.UserAlreadyExistsException;
import com.cookingapp.domain.repository.UserRepository;
import com.cookingapp.domain.service.AuthService;
import com.cookingapp.domain.service.UserGroupService;
import com.cookingapp.domain.valueobject.Language;

/**
 * ユーザー登録ユースケース
 */
@Service
public class RegisterUserUseCase {

    private static final Logger log = LoggerFactory.getLogger(RegisterUserUseCase.class);
    private static final String DEFAULT_USER_GROUP = "Users";

    private final AuthService authService;
    private final UserRepository userRepository;
    private final UserGroupService userGroupService;

    public RegisterUserUseCase(
            AuthService authService,
            UserRepository userRepository,
            UserGroupService userGroupService) {
        this.authService = authService;
        this.userRepository = userRepository;
        this.userGroupService = userGroupService;
    }

    /**
     * ユーザーを登録
     * 
     * @param email             メールアドレス
     * @param password          パスワード
     * @param nickname          ニックネーム
     * @param preferredLanguage 優先言語
     * @return 登録されたユーザー
     * @throws UserAlreadyExistsException ユーザーが既に存在する場合
     */
    public User execute(String email, String password, String nickname, Language preferredLanguage) {
        // パスワードバリデーション
        if (!User.validatePassword(password)) {
            throw new IllegalArgumentException(
                    "Password must be at least 8 characters long and contain uppercase, lowercase, numbers, and special characters");
        }

        // メールアドレスの重複チェック
        if (userRepository.findByEmail(email).isPresent()) {
            throw new UserAlreadyExistsException("User with email " + email + " already exists");
        }

        // Cognitoにユーザー登録
        authService.signUp(email, password, nickname);

        // デフォルトでUsersグループに追加
        try {
            userGroupService.addUserToGroup(email, DEFAULT_USER_GROUP);
            log.info("ユーザーをUsersグループに追加しました: email={}", email);
        } catch (Exception e) {
            log.error("Usersグループへの追加に失敗しました: email={}, error={}", email, e.getMessage());
            // グループ追加失敗してもユーザー登録は継続
        }

        // ユーザーエンティティ作成
        User user = new User(email, nickname, preferredLanguage);

        // DynamoDBに保存
        return userRepository.save(user);
    }
}
