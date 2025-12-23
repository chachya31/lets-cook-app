package com.cookingapp.domain.valueobject;

import java.math.BigDecimal;
import java.util.Objects;

import com.cookingapp.domain.constants.ValidationConstants;

import lombok.Getter;

/**
 * 食材バリューオブジェクト
 */
@Getter
public class Ingredient {
    private final String name;
    private final BigDecimal quantity;
    private final String unit;
    private final String note;
    private final boolean optional;

    /**
     * 食材を作成
     * 
     * @param name     食材名（1-100文字）
     * @param quantity 数量（オプション、0 < qty <= 9999）
     * @param unit     単位（オプション、自由入力）
     * @param note     メモ（最大200文字、オプション）
     * @param optional 任意フラグ
     */
    public Ingredient(String name, BigDecimal quantity, String unit, String note, boolean optional) {
        validateName(name);
        validateQuantity(quantity);
        validateUnit(unit);
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
    public Ingredient(String name, BigDecimal quantity, String unit, boolean optional) {
        this(name, quantity, unit, null, optional);
    }

    /**
     * 食材を作成（必須食材、メモなし）
     */
    public Ingredient(String name, BigDecimal quantity, String unit) {
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
                            ValidationConstants.INGREDIENT_NAME_MAX_LENGTH));
        }
    }

    /**
     * 数量をバリデーション（オプション）
     */
    private void validateQuantity(BigDecimal quantity) {
        if (quantity == null) {
            return;
        }
        if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("数量は0より大きい必要があります");
        }
        if (quantity.compareTo(new BigDecimal(ValidationConstants.INGREDIENT_QUANTITY_MAX)) > 0) {
            throw new IllegalArgumentException(
                    String.format("数量は%s以下である必要があります",
                            ValidationConstants.INGREDIENT_QUANTITY_MAX));
        }
    }

    /**
     * 単位をバリデーション（オプション）
     */
    private void validateUnit(String unit) {
        if (unit != null && unit.length() > ValidationConstants.INGREDIENT_UNIT_MAX_LENGTH) {
            throw new IllegalArgumentException(
                    String.format("単位は%d文字以内である必要があります",
                            ValidationConstants.INGREDIENT_UNIT_MAX_LENGTH));
        }
    }

    /**
     * メモをバリデーション
     */
    private void validateNote(String note) {
        if (note != null && note.length() > ValidationConstants.INGREDIENT_NOTE_MAX_LENGTH) {
            throw new IllegalArgumentException(
                    String.format("メモは%d文字以内である必要があります",
                            ValidationConstants.INGREDIENT_NOTE_MAX_LENGTH));
        }
    }

    /**
     * 正規化キーを生成（買い物リスト用）
     * 
     * @return 正規化キー（名前と単位の組み合わせ）
     */
    public String normalize() {
        String unitKey = unit != null ? unit.trim().toLowerCase() : "";
        return name.trim().toLowerCase() + "_" + unitKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Ingredient that = (Ingredient) o;
        return optional == that.optional &&
                Objects.equals(name, that.name) &&
                Objects.equals(quantity, that.quantity) &&
                Objects.equals(unit, that.unit) &&
                Objects.equals(note, that.note);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, quantity, unit, note, optional);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(name);
        if (quantity != null) {
            sb.append(" ").append(quantity);
        }
        if (unit != null && !unit.isEmpty()) {
            sb.append(unit);
        }
        if (note != null) {
            sb.append(" (").append(note).append(")");
        }
        if (optional) {
            sb.append(" [任意]");
        }
        return sb.toString();
    }
}
