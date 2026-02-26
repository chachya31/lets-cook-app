package com.cookingapp.domain.valueobject;

import lombok.Getter;

/**
 * 調理手順の値オブジェクト
 * テキスト、任意の画像URL、任意の動画URLを持つ
 */
@Getter
public class Step {
    private final String description;
    private final String imageUrl;
    private final String videoUrl;

    public Step(String description, String imageUrl, String videoUrl) {
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("手順の説明は必須です");
        }
        this.description = description;
        this.imageUrl = imageUrl;
        this.videoUrl = videoUrl;
    }

    public Step(String description, String imageUrl) {
        this(description, imageUrl, null);
    }

    public Step(String description) {
        this(description, null, null);
    }

    /**
     * 画像URLを更新した新しいStepを返す
     */
    public Step withImageUrl(String newImageUrl) {
        return new Step(this.description, newImageUrl, this.videoUrl);
    }

    /**
     * 動画URLを更新した新しいStepを返す
     */
    public Step withVideoUrl(String newVideoUrl) {
        return new Step(this.description, this.imageUrl, newVideoUrl);
    }

    /**
     * 画像があるかどうか
     */
    public boolean hasImage() {
        return imageUrl != null && !imageUrl.isEmpty();
    }

    /**
     * 動画があるかどうか
     */
    public boolean hasVideo() {
        return videoUrl != null && !videoUrl.isEmpty();
    }
}
