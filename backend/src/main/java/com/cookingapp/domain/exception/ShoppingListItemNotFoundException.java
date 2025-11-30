package com.cookingapp.domain.exception;

/**
 * 買い物リストアイテムが見つからない例外
 */
public class ShoppingListItemNotFoundException extends RuntimeException {
    public ShoppingListItemNotFoundException(String message) {
        super(message);
    }
}
