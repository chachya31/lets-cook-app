package com.cookingapp.application.validation;

/**
 * 画像バリデーション例外
 */
public class ImageValidationException extends RuntimeException {

    public ImageValidationException(String message) {
        super(message);
    }

    public ImageValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
