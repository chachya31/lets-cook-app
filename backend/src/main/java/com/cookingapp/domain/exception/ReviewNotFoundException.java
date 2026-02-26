package com.cookingapp.domain.exception;

/**
 * ReviewNotFoundException
 * レビューが見つからない場合の例外
 */
public class ReviewNotFoundException extends RuntimeException {
    public ReviewNotFoundException(String message) {
        super(message);
    }
}
