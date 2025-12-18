package com.cookingapp.presentation.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cookingapp.application.usecase.admin.DeleteRecipeByAdminUseCase;
import com.cookingapp.application.usecase.admin.DeleteUserByAdminUseCase;
import com.cookingapp.application.usecase.admin.GetAdminDashboardStatsUseCase;
import com.cookingapp.application.usecase.admin.GetAllRecipesForAdminUseCase;
import com.cookingapp.application.usecase.admin.SetRecipeStatusUseCase;
import com.cookingapp.application.usecase.admin.SuspendUserUseCase;
import com.cookingapp.domain.entity.Recipe;
import com.cookingapp.presentation.dto.RecipeResponse;
import com.cookingapp.presentation.dto.request.SetRecipeStatusRequest;
import com.cookingapp.presentation.dto.response.AdminDashboardResponse;
import com.cookingapp.presentation.mapper.RecipeMapper;

import jakarta.validation.Valid;

/**
 * 管理者機能コントローラー
 * 管理者権限（ADMINSグループ）が必要なエンドポイントを提供
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMINS')")
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    private final GetAdminDashboardStatsUseCase getAdminDashboardStatsUseCase;
    private final SuspendUserUseCase suspendUserUseCase;
    private final DeleteUserByAdminUseCase deleteUserByAdminUseCase;
    private final GetAllRecipesForAdminUseCase getAllRecipesForAdminUseCase;
    private final SetRecipeStatusUseCase setRecipeStatusUseCase;
    private final DeleteRecipeByAdminUseCase deleteRecipeByAdminUseCase;

    public AdminController(
            GetAdminDashboardStatsUseCase getAdminDashboardStatsUseCase,
            SuspendUserUseCase suspendUserUseCase,
            DeleteUserByAdminUseCase deleteUserByAdminUseCase,
            GetAllRecipesForAdminUseCase getAllRecipesForAdminUseCase,
            SetRecipeStatusUseCase setRecipeStatusUseCase,
            DeleteRecipeByAdminUseCase deleteRecipeByAdminUseCase) {
        this.getAdminDashboardStatsUseCase = getAdminDashboardStatsUseCase;
        this.suspendUserUseCase = suspendUserUseCase;
        this.deleteUserByAdminUseCase = deleteUserByAdminUseCase;
        this.getAllRecipesForAdminUseCase = getAllRecipesForAdminUseCase;
        this.setRecipeStatusUseCase = setRecipeStatusUseCase;
        this.deleteRecipeByAdminUseCase = deleteRecipeByAdminUseCase;
    }

    /**
     * 管理者ダッシュボード統計を取得
     * 
     * @return 統計情報
     */
    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardResponse> getDashboard() {
        log.info("管理者ダッシュボード統計取得リクエスト");

        GetAdminDashboardStatsUseCase.AdminDashboardStats stats = getAdminDashboardStatsUseCase.execute();

        AdminDashboardResponse response = new AdminDashboardResponse(
                stats.message(),
                stats.totalUsers(),
                stats.totalRecipes());

        return ResponseEntity.ok(response);
    }

    /**
     * ユーザーを停止
     * 
     * @param userId ユーザーID
     * @return 204 No Content
     */
    @PutMapping("/users/{userId}/suspend")
    public ResponseEntity<Void> suspendUser(@PathVariable("userId") String userId) {
        log.info("ユーザー停止リクエスト: userId={}", userId);

        suspendUserUseCase.execute(userId);

        return ResponseEntity.noContent().build();
    }

    /**
     * ユーザーを削除
     * 
     * @param userId ユーザーID
     * @return 204 No Content
     */
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable("userId") String userId) {
        log.info("ユーザー削除リクエスト: userId={}", userId);

        deleteUserByAdminUseCase.execute(userId);

        return ResponseEntity.noContent().build();
    }

    /**
     * すべてのレシピを取得（管理者用）
     * 審査待ちを含むすべてのレシピを返す
     * 
     * @return レシピリスト
     */
    @GetMapping("/recipes")
    public ResponseEntity<List<RecipeResponse>> getAllRecipes() {
        log.info("管理者用レシピ一覧取得リクエスト");

        List<Recipe> recipes = getAllRecipesForAdminUseCase.execute();

        return ResponseEntity.ok(RecipeMapper.toResponseList(recipes));
    }

    /**
     * レシピのステータスを設定
     * 
     * @param recipeId レシピID
     * @param request  ステータス設定リクエスト
     * @return 更新されたレシピ
     */
    @PutMapping("/recipes/{recipeId}/status")
    public ResponseEntity<RecipeResponse> setRecipeStatus(
            @PathVariable("recipeId") String recipeId,
            @Valid @RequestBody SetRecipeStatusRequest request) {
        log.info("レシピステータス設定リクエスト: recipeId={}, isPublic={}",
                recipeId, request.isPublic());

        Recipe recipe = setRecipeStatusUseCase.execute(recipeId, request.isPublic());

        return ResponseEntity.ok(RecipeMapper.toResponse(recipe));
    }

    /**
     * レシピを削除（論理削除）
     * 
     * @param recipeId レシピID
     * @return 204 No Content
     */
    @DeleteMapping("/recipes/{recipeId}")
    public ResponseEntity<Void> deleteRecipe(@PathVariable("recipeId") String recipeId) {
        log.info("管理者によるレシピ削除リクエスト: recipeId={}", recipeId);

        deleteRecipeByAdminUseCase.execute(recipeId);

        return ResponseEntity.noContent().build();
    }
}
