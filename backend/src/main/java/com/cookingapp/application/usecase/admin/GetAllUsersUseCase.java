package com.cookingapp.application.usecase.admin;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.cookingapp.domain.entity.User;
import com.cookingapp.domain.repository.UserRepository;
import com.cookingapp.infrastructure.external.cognito.CognitoAuthService;
import com.cookingapp.presentation.dto.UserResponse;

/**
 * 全ユーザー取得ユースケース（管理者用）
 */
@Service
public class GetAllUsersUseCase {

    private static final Logger log = LoggerFactory.getLogger(GetAllUsersUseCase.class);

    private final UserRepository userRepository;
    private final CognitoAuthService cognitoAuthService;

    public GetAllUsersUseCase(UserRepository userRepository, CognitoAuthService cognitoAuthService) {
        this.userRepository = userRepository;
        this.cognitoAuthService = cognitoAuthService;
    }

    /**
     * すべてのユーザーを取得（グループ情報を含む）
     * 
     * @return ユーザーレスポンスリスト
     */
    public List<UserResponse> execute() {
        List<User> users = userRepository.findAll();
        log.info("管理者用にユーザー一覧を取得しました: count={}", users.size());

        return users.stream()
                .map(user -> {
                    List<String> roles;
                    try {
                        roles = cognitoAuthService.getUserGroups(user.getEmail());
                    } catch (Exception e) {
                        log.warn("ユーザーのグループ取得に失敗しました: email={}, error={}",
                                user.getEmail(), e.getMessage());
                        roles = List.of(); // 空のリストを返す
                    }
                    return UserResponse.from(user, roles);
                })
                .collect(Collectors.toList());
    }
}
