package com.cookingapp.domain.valueobject;

import lombok.Getter;

/**
 * 言語バリューオブジェクト
 * サポートされる言語を定義
 */
@Getter
public enum Language {
    JA("ja", "日本語"),
    KO("ko", "한국어");

    private final String code;
    private final String displayName;

    Language(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    /**
     * 言語コードから言語を取得
     * 
     * @param code 言語コード（"ja", "ko"）
     * @return 対応する言語、見つからない場合はデフォルト（JA）
     */
    public static Language fromCode(String code) {
        if (code == null) {
            return JA;
        }
        
        for (Language lang : values()) {
            if (lang.code.equalsIgnoreCase(code)) {
                return lang;
            }
        }
        
        return JA; // デフォルト
    }
}
