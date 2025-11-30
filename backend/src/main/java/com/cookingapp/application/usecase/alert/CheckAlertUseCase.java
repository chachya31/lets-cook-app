package com.cookingapp.application.usecase.alert;

import com.cookingapp.domain.entity.User;
import com.cookingapp.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * アラート判定ユースケース
 * 最終料理日から3日経過した場合にアラートを表示する判定を行う
 */
@Service
@RequiredArgsConstructor
public class CheckAlertUseCase {
    private final UserRepository userRepository;
    private final Random random = new Random();

    // 警告メッセージリスト
    private static final List<String> WARNING_MESSAGES = Arrays.asList(
        "もう3日も料理していませんよ！",
        "料理をサボっていませんか？",
        "自炊習慣が途切れそうです！"
    );

    // 励ましメッセージリスト
    private static final List<String> ENCOURAGEMENT_MESSAGES = Arrays.asList(
        "今日は何か作ってみませんか？",
        "簡単なレシピから始めてみましょう！",
        "料理を再開して、健康的な生活を取り戻しましょう！"
    );

    /**
     * アラート表示判定を実行
     * 
     * @param userId ユーザーID
     * @return アラート情報（表示不要の場合はnull）
     */
    public AlertResponse checkAlert(String userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        LocalDate lastCookingDate = user.getLastCookingDate();
        
        // 最終料理日がない場合はアラート表示
        if (lastCookingDate == null) {
            return createAlertResponse(true);
        }

        // 最終料理日から現在までの日数を計算
        LocalDate today = LocalDate.now();
        long daysSinceLastCooking = ChronoUnit.DAYS.between(lastCookingDate, today);

        // 3日経過（4日目の0時）でアラート表示
        if (daysSinceLastCooking >= 3) {
            return createAlertResponse(true);
        }

        return createAlertResponse(false);
    }

    /**
     * アラートレスポンスを作成
     * 
     * @param shouldShow アラート表示フラグ
     * @return アラートレスポンス
     */
    private AlertResponse createAlertResponse(boolean shouldShow) {
        if (!shouldShow) {
            return new AlertResponse(false, null);
        }

        // ランダムにメッセージを選択
        String message = selectRandomMessage();
        return new AlertResponse(true, message);
    }

    /**
     * ランダムにメッセージを選択
     * 警告メッセージまたは励ましメッセージをランダムに選択
     * 
     * @return 選択されたメッセージ
     */
    private String selectRandomMessage() {
        // 警告メッセージと励ましメッセージを50%の確率で選択
        boolean useWarning = random.nextBoolean();
        
        if (useWarning) {
            int index = random.nextInt(WARNING_MESSAGES.size());
            return WARNING_MESSAGES.get(index);
        } else {
            int index = random.nextInt(ENCOURAGEMENT_MESSAGES.size());
            return ENCOURAGEMENT_MESSAGES.get(index);
        }
    }
}
