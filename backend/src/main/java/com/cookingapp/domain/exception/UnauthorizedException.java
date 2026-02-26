package com.cookingapp.domain.exception;

/**
 * 認可エラーの例外
 */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
