package com.cookingapp.domain.exception;

/**
 * レシピが見つからない場合の例外
 */
public class RecipeNotFoundException extends RuntimeException {
    public RecipeNotFoundException(String message) {
        super(message);
    }
}
