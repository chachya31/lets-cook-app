package com.cookingapp.domain.repository;

import com.cookingapp.domain.entity.User;
import java.util.Optional;

/**
 * ユーザーリポジトリインターフェース
 */
public interface UserRepository {
    
    /**
     * ユーザーを保存
     * 
     * @param user 保存するユーザー
     * @return 保存されたユーザー
     */
    User save(User user);
    
    /**
     * ユーザーIDでユーザーを取得
     * 
     * @param userId ユーザーID
     * @return ユーザー（存在しない場合はEmpty）
     */
    Optional<User> findById(String userId);
    
    /**
     * メールアドレスでユーザーを取得
     * 
     * @param email メールアドレス
     * @return ユーザー（存在しない場合はEmpty）
     */
    Optional<User> findByEmail(String email);
    
    /**
     * ユーザーを削除
     * 
     * @param userId ユーザーID
     */
    void delete(String userId);
    
    /**
     * ユーザーが存在するかチェック
     * 
     * @param userId ユーザーID
     * @return 存在する場合true
     */
    boolean existsById(String userId);
}
