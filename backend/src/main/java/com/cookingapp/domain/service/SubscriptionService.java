package com.cookingapp.domain.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.cookingapp.domain.entity.User;
import com.cookingapp.domain.entity.UserSubscription;
import com.cookingapp.domain.exception.QuotaExceededException;
import com.cookingapp.domain.repository.UserRepository;
import com.cookingapp.domain.repository.UserSubscriptionRepository;
import com.cookingapp.domain.valueobject.PlanType;

/**
 * サブスクリプション管理サービス
 * クレジットの消費・追加・月次リセットを管理
 */
@Service
public class SubscriptionService {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionService.class);
    private static final int ADD_ON_CREDITS = 10;

    private final UserSubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;

    public SubscriptionService(UserSubscriptionRepository subscriptionRepository,
            UserRepository userRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.userRepository = userRepository;
    }

    /**
     * クレジットをチェックして消費
     * 初回ユーザーにはFREEプランを自動付与
     * 月次リセットが必要な場合は実行
     * 
     * @param userId ユーザーID
     * @return 消費後の残りクレジット数
     * @throws QuotaExceededException クレジットが0の場合
     */
    public int checkAndConsumeCredit(String userId) {
        // ユーザーのタイムゾーンを取得
        LocalDate userToday = getUserLocalDate(userId);

        // サブスクリプション取得または新規作成
        UserSubscription subscription = getOrCreateSubscription(userId);

        // PREMIUMプランは無制限
        if (subscription.getPlanType() == PlanType.PREMIUM) {
            logCreditConsumption(userId, subscription, -1, -1, "PREMIUM_UNLIMITED");
            subscriptionRepository.incrementTotalCreditsUsed(userId);
            return Integer.MAX_VALUE;
        }

        // 月次リセットチェック
        if (subscription.needsMonthlyReset(userToday)) {
            performMonthlyReset(userId, subscription, userToday);
        }

        // アトミックにクレジットをデクリメント
        int remainingBefore = subscription.getRemainingCredits();
        int remainingAfter = subscriptionRepository.decrementCreditsAtomic(userId);

        // 監査ログ出力
        logCreditConsumption(userId, subscription, remainingBefore, remainingAfter, "CONSUMED");

        return remainingAfter;
    }

    /**
     * クレジットを追加（ADD_ON購入時）
     * 
     * @param userId ユーザーID
     * @param amount 追加するクレジット数（デフォルト10）
     * @return 追加後の残りクレジット数
     */
    public int addCredits(String userId, int amount) {
        if (amount <= 0) {
            amount = ADD_ON_CREDITS;
        }

        // サブスクリプションが存在しない場合は作成
        getOrCreateSubscription(userId);

        int newBalance = subscriptionRepository.addCredits(userId, amount);

        // 監査ログ出力
        logCreditAddition(userId, amount, newBalance, "ADD_ON_PURCHASE");

        return newBalance;
    }

    /**
     * クレジットを追加（デフォルト10回）
     */
    public int addCredits(String userId) {
        return addCredits(userId, ADD_ON_CREDITS);
    }

    /**
     * プランをアップグレード
     */
    public UserSubscription upgradePlan(String userId, PlanType newPlan) {
        LocalDate userToday = getUserLocalDate(userId);
        UserSubscription subscription = getOrCreateSubscription(userId);

        PlanType oldPlan = subscription.getPlanType();
        subscription.upgradePlan(newPlan, userToday);
        subscriptionRepository.save(subscription);

        // 監査ログ出力
        logPlanChange(userId, oldPlan, newPlan);

        return subscription;
    }

    /**
     * 現在のサブスクリプション情報を取得
     */
    public UserSubscription getSubscription(String userId) {
        return getOrCreateSubscription(userId);
    }

    /**
     * サブスクリプションを取得、存在しない場合はFREEプランで新規作成
     */
    private UserSubscription getOrCreateSubscription(String userId) {
        Optional<UserSubscription> existing = subscriptionRepository.findByUserId(userId);

        if (existing.isPresent()) {
            return existing.get();
        }

        // 新規ユーザー: FREEプランを付与
        UserSubscription newSubscription = UserSubscription.createFreeSubscription(userId);
        subscriptionRepository.save(newSubscription);

        log.info("{{\"event\": \"SUBSCRIPTION_CREATED\", \"userId\": \"{}\", \"planType\": \"FREE\", \"credits\": {}}}",
                userId, PlanType.FREE.getMonthlyCredits());

        return newSubscription;
    }

    /**
     * ユーザーのタイムゾーンに基づいた今日の日付を取得
     */
    private LocalDate getUserLocalDate(String userId) {
        try {
            Optional<User> user = userRepository.findById(userId);
            if (user.isPresent() && user.get().getTimezone() != null) {
                ZoneId zoneId = ZoneId.of(user.get().getTimezone());
                return ZonedDateTime.now(zoneId).toLocalDate();
            }
        } catch (Exception e) {
            log.warn("Failed to get user timezone for {}, using Asia/Tokyo", userId);
        }
        // デフォルト: Asia/Tokyo
        return ZonedDateTime.now(ZoneId.of("Asia/Tokyo")).toLocalDate();
    }

    /**
     * 月次リセットを実行
     */
    private void performMonthlyReset(String userId, UserSubscription subscription, LocalDate today) {
        int oldCredits = subscription.getRemainingCredits();
        subscription.performMonthlyReset(today);
        subscriptionRepository.save(subscription);

        log.info("{{\"event\": \"MONTHLY_RESET\", \"userId\": \"{}\", \"planType\": \"{}\", " +
                "\"oldCredits\": {}, \"newCredits\": {}, \"newResetDate\": \"{}\"}}",
                userId, subscription.getPlanType().getCode(), oldCredits,
                subscription.getRemainingCredits(), subscription.getResetDate());
    }

    // ===== 監査ログメソッド =====

    private void logCreditConsumption(String userId, UserSubscription subscription,
            int remainingBefore, int remainingAfter, String action) {
        log.info("{{\"event\": \"CREDIT_{}\", \"userId\": \"{}\", \"planType\": \"{}\", " +
                "\"remainingBefore\": {}, \"remainingAfter\": {}, \"timestamp\": \"{}\"}}",
                action, userId, subscription.getPlanType().getCode(),
                remainingBefore, remainingAfter, java.time.Instant.now());
    }

    private void logCreditAddition(String userId, int amount, int newBalance, String source) {
        log.info("{{\"event\": \"CREDIT_ADDED\", \"userId\": \"{}\", \"amount\": {}, " +
                "\"newBalance\": {}, \"source\": \"{}\", \"timestamp\": \"{}\"}}",
                userId, amount, newBalance, source, java.time.Instant.now());
    }

    private void logPlanChange(String userId, PlanType oldPlan, PlanType newPlan) {
        log.info("{{\"event\": \"PLAN_CHANGED\", \"userId\": \"{}\", \"oldPlan\": \"{}\", " +
                "\"newPlan\": \"{}\", \"timestamp\": \"{}\"}}",
                userId, oldPlan.getCode(), newPlan.getCode(), java.time.Instant.now());
    }
}
