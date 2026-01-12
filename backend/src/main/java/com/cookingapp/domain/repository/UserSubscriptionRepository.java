package com.cookingapp.domain.repository;

import java.util.Optional;

import com.cookingapp.domain.entity.UserSubscription;

/**
 * ユーザーサブスクリプションリポジトリインターフェース
 */
public interface UserSubscriptionRepository {

    /**
     * サブスクリプションを保存
     */
    UserSubscription save(UserSubscription subscription);

    /**
     * ユーザーIDでサブスクリプションを取得
     */
    Optional<UserSubscription> findByUserId(String userId);

    /**
     * クレジットをアトミックにデクリメント
     * ConditionExpressionで残数が0より大きいことを保証
     * 
     * @param userId ユーザーID
     * @return 更新後の残りクレジット数
     * @throws com.cookingapp.domain.exception.QuotaExceededException クレジットが0の場合
     */
    int decrementCreditsAtomic(String userId);

    /**
     * クレジットを追加（ADD_ON購入時）
     * 
     * @param userId ユーザーID
     * @param amount 追加するクレジット数
     * @return 更新後の残りクレジット数
     */
    int addCredits(String userId, int amount);

    /**
     * 累計使用回数をインクリメント
     */
    void incrementTotalCreditsUsed(String userId);
}
