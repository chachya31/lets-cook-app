package com.cookingapp.application.exception;

public class UserNotAuthenticatedException extends RuntimeException {

    public UserNotAuthenticatedException() {
        super("User is not authenticated");
    }

    public UserNotAuthenticatedException(String message) {
        super(message);
    }
}
