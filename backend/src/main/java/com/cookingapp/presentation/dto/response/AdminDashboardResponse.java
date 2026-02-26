package com.cookingapp.presentation.dto.response;

/**
 * 管理者ダッシュボードレスポンス
 */
public record AdminDashboardResponse(
    String message,
    long totalUsers,
    long totalRecipes
) {}
