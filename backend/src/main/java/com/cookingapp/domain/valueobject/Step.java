package com.cookingapp.domain.valueobject;

import lombok.Getter;

/**
 * 調理手順の値オブジェクト
 * テキストと任意の画像URLを持つ
 */
@Getter
public class Step {
    private final String description;
    private final String imageUrl;

    public Step(String description, String imageUrl) {
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("手順の説明は必須です");
        }
        this.description = description;
        this.imageUrl = imageUrl; // 任意項目（nullも可）
    }

    public Step(String description) {
        this(description, null);
    }

    /**
     * 画像URLを更新した新しいStepを返す
     */
    public Step withImageUrl(String newImageUrl) {
        return new Step(this.description, newImageUrl);
    }

    /**
     * 画像があるかどうか
     */
    public boolean hasImage() {
        return imageUrl != null && !imageUrl.isEmpty();
    }
}
