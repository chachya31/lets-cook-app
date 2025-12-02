package com.cookingapp.domain.valueobject;

import com.cookingapp.domain.constants.ValidationConstants;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * 食材バリューオブジェクト
 */
@Getter
public class Ingredient {
    private final String name;
    private final BigDecimal quantity;
    private final Unit unit;
    private final String note;
    private final boolean optional;

    /**
     * 食材を作成
     * 
     * @param name 食材名（1-100文字）
     * @param quantity 数量（0 < qty <= 9999）
     * @param unit 単位
     * @param note メモ（最大200文字、オプション）
     * @param optional 任意フラグ
     */
    public Ingredient(String name, BigDecimal quantity, Unit unit, String note, boolean optional) {
        validateName(name);
        validateQuantity(quantity);
        validateNote(note);
        
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.note = note;
        this.optional = optional;
    }

    /**
     * 食材を作成（メモなし）
     */
    public Ingredient(String name, BigDecimal quantity, Unit unit, boolean optional) {
        this(name, quantity, unit, null, optional);
    }

    /**
     * 食材を作成（必須食材、メモなし）
     */
    public Ingredient(String name, BigDecimal quantity, Unit unit) {
        this(name, quantity, unit, null, false);
    }

    /**
     * 食材名をバリデーション
     */
    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("食材名は必須です");
        }
        if (name.length() > ValidationConstants.INGREDIENT_NAME_MAX_LENGTH) {
            throw new IllegalArgumentException(
                String.format("食材名は%d文字以内である必要があります", 
                    ValidationConstants.INGREDIENT_NAME_MAX_LENGTH)
            );
        }
    }

    /**
     * 数量をバリデーション
     */
    private void validateQuantity(BigDecimal quantity) {
        if (quantity == null) {
            throw new IllegalArgumentException("数量は必須です");
        }
        if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("数量は0より大きい必要があります");
        }
        if (quantity.compareTo(new BigDecimal(ValidationConstants.INGREDIENT_QUANTITY_MAX)) > 0) {
            throw new IllegalArgumentException(
                String.format("数量は%s以下である必要があります", 
                    ValidationConstants.INGREDIENT_QUANTITY_MAX)
            );
        }
    }

    /**
     * メモをバリデーション
     */
    private void validateNote(String note) {
        if (note != null && note.length() > ValidationConstants.INGREDIENT_NOTE_MAX_LENGTH) {
            throw new IllegalArgumentException(
                String.format("メモは%d文字以内である必要があります", 
                    ValidationConstants.INGREDIENT_NOTE_MAX_LENGTH)
            );
        }
    }

    /**
     * 正規化キーを生成（買い物リスト用）
     * 
     * @return 正規化キー（名前と単位の組み合わせ）
     */
    public String normalize() {
        return name.trim().toLowerCase() + "_" + unit.getCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ingredient that = (Ingredient) o;
        return optional == that.optional &&
                Objects.equals(name, that.name) &&
                Objects.equals(quantity, that.quantity) &&
                unit == that.unit &&
                Objects.equals(note, that.note);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, quantity, unit, note, optional);
    }

    @Override
    public String toString() {
        return String.format("%s %s%s%s%s",
                name,
                quantity,
                unit.getCode(),
                note != null ? " (" + note + ")" : "",
                optional ? " [任意]" : "");
    }
}
