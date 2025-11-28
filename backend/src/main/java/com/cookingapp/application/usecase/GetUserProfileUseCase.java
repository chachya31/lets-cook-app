package com.cookingapp.application.usecase;

import com.cookingapp.domain.entity.User;
import com.cookingapp.domain.repository.UserRepository;
import org.springframework.stereotype.Service;

/**
 * ユーザープロフィール取得ユースケース
 */
@Service
public class GetUserProfileUseCase {
    
    private final UserRepository userRepository;

    public GetUserProfileUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * ユーザープロフィールを取得
     * 
     * @param userId ユーザーID
     * @return ユーザー
     */
    public User execute(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
    }
}
