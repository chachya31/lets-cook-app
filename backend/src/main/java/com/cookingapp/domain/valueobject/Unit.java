package com.cookingapp.domain.valueobject;

/**
 * 食材の単位
 */
public enum Unit {
    // 重量
    G("g", "グラム"),
    KG("kg", "キログラム"),
    
    // 容量
    ML("ml", "ミリリットル"),
    L("l", "リットル"),
    
    // 個数
    PIECE("個", "個"),
    PACK("パック", "パック"),
    
    // その他
    TABLESPOON("大さじ", "大さじ"),
    TEASPOON("小さじ", "小さじ"),
    CUP("カップ", "カップ"),
    APPROPRIATE("適量", "適量");

    private final String code;
    private final String displayName;

    Unit(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * コードから単位を取得
     * 
     * @param code 単位コード
     * @return 単位
     * @throws IllegalArgumentException 無効なコード
     */
    public static Unit fromCode(String code) {
        for (Unit unit : values()) {
            if (unit.code.equals(code)) {
                return unit;
            }
        }
        throw new IllegalArgumentException("Invalid unit code: " + code);
    }
}
