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
    
    // 計量
    TABLESPOON("tbsp", "大さじ"),
    TEASPOON("tsp", "小さじ"),
    CUP("cup", "カップ"),
    
    // 個数
    PIECE("piece", "個"),
    PACK("pack", "パック"),
    CAN("can", "缶"),
    BOTTLE("bottle", "本"),
    SLICE("slice", "枚"),
    CLOVE("clove", "片"),
    
    // その他
    PINCH("pinch", "ひとつまみ"),
    TO_TASTE("to_taste", "適量"),
    AS_NEEDED("as_needed", "必要に応じて");

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
     * 旧形式（日本語コード）との後方互換性あり
     * 
     * @param code 単位コード
     * @return 単位
     * @throws IllegalArgumentException 無効なコード
     */
    public static Unit fromCode(String code) {
        // 新形式のコードで検索
        for (Unit unit : values()) {
            if (unit.code.equals(code)) {
                return unit;
            }
        }
        
        // 旧形式（日本語コード）との互換性対応
        switch (code) {
            case "大さじ":
                return TABLESPOON;
            case "小さじ":
                return TEASPOON;
            case "個":
                return PIECE;
            case "パック":
                return PACK;
            case "カップ":
                return CUP;
            case "適量":
                return TO_TASTE;
            default:
                throw new IllegalArgumentException("Invalid unit code: " + code);
        }
    }
}
