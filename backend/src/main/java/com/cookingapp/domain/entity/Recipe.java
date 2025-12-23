package com.cookingapp.domain.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.cookingapp.domain.valueobject.Ingredient;
import com.cookingapp.domain.valueobject.Step;

import lombok.Getter;

/**
 * レシピエンティティ
 * レシピ情報を管理する
 */
@Getter
public class Recipe {
    private final String recipeId;
    private String title;
    private final String authorId;
    private List<Ingredient> ingredients;
    private List<Step> steps;
    private int cookingTime;
    private String imageUrl;
    private boolean isPublic;
    private boolean isDeleted;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 新規レシピを作成
     */
    public Recipe(String authorId, String title, List<Ingredient> ingredients,
            List<Step> steps, int cookingTime) {
        validateTitle(title);
        validateIngredients(ingredients);
        validateSteps(steps);
        validateCookingTime(cookingTime);

        this.recipeId = UUID.randomUUID().toString();
        this.authorId = authorId;
        this.title = title;
        this.ingredients = new ArrayList<>(ingredients);
        this.steps = new ArrayList<>(steps);
        this.cookingTime = cookingTime;
        this.isPublic = true;
        this.isDeleted = false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 既存レシピを復元（リポジトリから取得時）
     */
    public Recipe(String recipeId, String authorId, String title, List<Ingredient> ingredients,
            List<Step> steps, int cookingTime, String imageUrl, boolean isPublic,
            boolean isDeleted, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.recipeId = recipeId;
        this.authorId = authorId;
        this.title = title;
        this.ingredients = ingredients != null ? new ArrayList<>(ingredients) : new ArrayList<>();
        this.steps = steps != null ? new ArrayList<>(steps) : new ArrayList<>();
        this.cookingTime = cookingTime;
        this.imageUrl = imageUrl;
        this.isPublic = isPublic;
        this.isDeleted = isDeleted;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    private void validateTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("タイトルは必須です");
        }
        if (title.length() > 100) {
            throw new IllegalArgumentException("タイトルは100文字以内である必要があります");
        }
    }

    private void validateIngredients(List<Ingredient> ingredients) {
        if (ingredients == null || ingredients.isEmpty()) {
            throw new IllegalArgumentException("食材は最低1つ必要です");
        }
    }

    private void validateSteps(List<Step> steps) {
        if (steps == null || steps.isEmpty()) {
            throw new IllegalArgumentException("手順は最低1つ必要です");
        }
    }

    private void validateCookingTime(int cookingTime) {
        if (cookingTime < 0) {
            throw new IllegalArgumentException("調理時間は0以上である必要があります");
        }
    }

    public boolean validateIngredient(Ingredient ingredient) {
        return ingredient != null;
    }

    /**
     * レシピを更新
     */
    public void update(String title, List<Ingredient> ingredients, List<Step> steps, int cookingTime) {
        validateTitle(title);
        validateIngredients(ingredients);
        validateSteps(steps);
        validateCookingTime(cookingTime);

        this.title = title;
        this.ingredients = new ArrayList<>(ingredients);
        this.steps = new ArrayList<>(steps);
        this.cookingTime = cookingTime;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 画像URLを更新
     */
    public void updateImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 手順の画像URLを更新
     */
    public void updateStepImageUrl(int stepIndex, String imageUrl) {
        if (stepIndex < 0 || stepIndex >= steps.size()) {
            throw new IllegalArgumentException("無効な手順インデックス: " + stepIndex);
        }
        Step oldStep = steps.get(stepIndex);
        steps.set(stepIndex, oldStep.withImageUrl(imageUrl));
        this.updatedAt = LocalDateTime.now();
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsDeleted() {
        this.isDeleted = true;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isAuthor(String userId) {
        return this.authorId.equals(userId);
    }

    public boolean canEdit(String userId) {
        return isAuthor(userId) && !isDeleted;
    }

    public boolean canDelete(String userId) {
        return isAuthor(userId) && !isDeleted;
    }
}
