package com.cookingapp.unit.usecase.admin;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cookingapp.application.usecase.admin.GetAdminDashboardStatsUseCase;
import com.cookingapp.domain.repository.RecipeRepository;
import com.cookingapp.domain.repository.UserRepository;

/**
 * GetAdminDashboardStatsUseCaseのユニットテスト
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GetAdminDashboardStatsUseCase ユニットテスト")
class GetAdminDashboardStatsUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RecipeRepository recipeRepository;

    private GetAdminDashboardStatsUseCase getAdminDashboardStatsUseCase;

    @BeforeEach
    void setUp() {
        getAdminDashboardStatsUseCase = new GetAdminDashboardStatsUseCase(userRepository, recipeRepository);
    }

    @Test
    @DisplayName("ダッシュボード統計を正常に取得する")
    void testExecute_ReturnsDashboardStats() {
        // Act
        GetAdminDashboardStatsUseCase.AdminDashboardStats result = getAdminDashboardStatsUseCase.execute();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.message()).isEqualTo("統計情報は現在開発中です");
        assertThat(result.totalUsers()).isEqualTo(0);
        assertThat(result.totalRecipes()).isEqualTo(0);
    }

    @Test
    @DisplayName("AdminDashboardStatsレコードが正しく構築される")
    void testAdminDashboardStats_RecordConstruction() {
        // Arrange & Act
        GetAdminDashboardStatsUseCase.AdminDashboardStats stats = new GetAdminDashboardStatsUseCase.AdminDashboardStats(
                "テストメッセージ", 10, 25);

        // Assert
        assertThat(stats.message()).isEqualTo("テストメッセージ");
        assertThat(stats.totalUsers()).isEqualTo(10);
        assertThat(stats.totalRecipes()).isEqualTo(25);
    }

    @Test
    @DisplayName("execute()を複数回呼び出しても同じ結果を返す")
    void testExecute_ReturnsConsistentResults() {
        // Act
        GetAdminDashboardStatsUseCase.AdminDashboardStats result1 = getAdminDashboardStatsUseCase.execute();
        GetAdminDashboardStatsUseCase.AdminDashboardStats result2 = getAdminDashboardStatsUseCase.execute();

        // Assert
        assertThat(result1.message()).isEqualTo(result2.message());
        assertThat(result1.totalUsers()).isEqualTo(result2.totalUsers());
        assertThat(result1.totalRecipes()).isEqualTo(result2.totalRecipes());
    }
}
