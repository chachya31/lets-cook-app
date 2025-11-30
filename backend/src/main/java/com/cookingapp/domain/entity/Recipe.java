package com.cookingapp.domain.entity;

import com.cookingapp.domain.valueobject.Ingredient;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
    private List<String> steps;
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
                  List<String> steps, int cookingTime) {
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
                  List<String> steps, int cookingTime, String imageUrl, boolean isPublic,
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

    /**
     * タイトルをバリデーション
     */
    private void validateTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("タイトルは必須です");
        }
        if (title.length() > 100) {
            throw new IllegalArgumentException("タイトルは100文字以内である必要があります");
        }
    }

    /**
     * 食材リストをバリデーション
     */
    private void validateIngredients(List<Ingredient> ingredients) {
        if (ingredients == null || ingredients.isEmpty()) {
            throw new IllegalArgumentException("食材は最低1つ必要です");
        }
    }

    /**
     * 手順リストをバリデーション
     */
    private void validateSteps(List<String> steps) {
        if (steps == null || steps.isEmpty()) {
            throw new IllegalArgumentException("手順は最低1つ必要です");
        }
        for (String step : steps) {
            if (step == null || step.trim().isEmpty()) {
                throw new IllegalArgumentException("手順に空の項目があります");
            }
        }
    }

    /**
     * 調理時間をバリデーション
     */
    private void validateCookingTime(int cookingTime) {
        if (cookingTime < 0) {
            throw new IllegalArgumentException("調理時間は0以上である必要があります");
        }
    }

    /**
     * 食材をバリデーション
     * 
     * @param ingredient 食材
     * @return バリデーション結果
     */
    public boolean validateIngredient(Ingredient ingredient) {
        try {
            // Ingredientのコンストラクタでバリデーションが行われる
            return ingredient != null;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * レシピを更新
     */
    public void update(String title, List<Ingredient> ingredients, List<String> steps, int cookingTime) {
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
     * 公開状態を変更
     */
    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * レシピを削除済みとしてマーク
     * スケジュールと買い物リストの参照を保持するため、物理削除ではなく論理削除
     */
    public void markAsDeleted() {
        this.isDeleted = true;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 作成者かどうか確認
     */
    public boolean isAuthor(String userId) {
        return this.authorId.equals(userId);
    }

    /**
     * 編集可能かどうか確認
     */
    public boolean canEdit(String userId) {
        return isAuthor(userId) && !isDeleted;
    }

    /**
     * 削除可能かどうか確認
     */
    public boolean canDelete(String userId) {
        return isAuthor(userId) && !isDeleted;
    }
}
