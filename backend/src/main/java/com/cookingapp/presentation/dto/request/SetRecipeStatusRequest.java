package com.cookingapp.presentation.dto.request;

import jakarta.validation.constraints.NotNull;

/**
 * レシピステータス設定リクエスト（管理者機能）
 */
public record SetRecipeStatusRequest(
    @NotNull(message = "公開フラグは必須です")
    Boolean isPublic
) {}
