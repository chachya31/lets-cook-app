package com.cookingapp.application.usecase.admin;

import com.cookingapp.domain.repository.RecipeRepository;
import com.cookingapp.domain.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 管理者ダッシュボード統計取得ユースケース
 */
@Service
public class GetAdminDashboardStatsUseCase {
    
    private static final Logger log = LoggerFactory.getLogger(GetAdminDashboardStatsUseCase.class);
    
    private final UserRepository userRepository;
    private final RecipeRepository recipeRepository;
    
    public GetAdminDashboardStatsUseCase(UserRepository userRepository, RecipeRepository recipeRepository) {
        this.userRepository = userRepository;
        this.recipeRepository = recipeRepository;
    }
    
    /**
     * 管理者ダッシュボード統計を取得
     * 
     * @return 統計情報
     */
    public AdminDashboardStats execute() {
        log.info("管理者ダッシュボード統計を取得中");
        
        // 現時点では基本的な統計のみ
        // 将来的にはより詳細な統計を追加可能
        AdminDashboardStats stats = new AdminDashboardStats(
            "統計情報は現在開発中です",
            0,
            0
        );
        
        log.info("管理者ダッシュボード統計を取得しました");
        return stats;
    }
    
    /**
     * 管理者ダッシュボード統計
     */
    public record AdminDashboardStats(
        String message,
        long totalUsers,
        long totalRecipes
    ) {}
}
