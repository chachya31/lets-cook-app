package com.cookingapp.presentation.exception;

import com.cookingapp.application.exception.RecipeNotFoundException;
import com.cookingapp.application.exception.UnauthorizedRecipeAccessException;
import com.cookingapp.application.exception.UserNotAuthenticatedException;
import com.cookingapp.infrastructure.auth.AuthenticationException;
import com.cookingapp.presentation.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException e) {
        logger.warn("Authentication failed: {}", e.getMessage());
        ErrorResponse response = ErrorResponse.builder()
                .message(e.getMessage())
                .code("AUTHENTICATION_FAILED")
                .build();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(UserNotAuthenticatedException.class)
    public ResponseEntity<ErrorResponse> handleUserNotAuthenticatedException(UserNotAuthenticatedException e) {
        logger.warn("User not authenticated: {}", e.getMessage());
        ErrorResponse response = ErrorResponse.builder()
                .message(e.getMessage())
                .code("USER_NOT_AUTHENTICATED")
                .build();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(RecipeNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleRecipeNotFoundException(RecipeNotFoundException e) {
        logger.warn("Recipe not found: {}", e.getMessage());
        ErrorResponse response = ErrorResponse.builder()
                .message(e.getMessage())
                .code("RECIPE_NOT_FOUND")
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(UnauthorizedRecipeAccessException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedRecipeAccessException(UnauthorizedRecipeAccessException e) {
        logger.warn("Unauthorized recipe access: {}", e.getMessage());
        ErrorResponse response = ErrorResponse.builder()
                .message(e.getMessage())
                .code("UNAUTHORIZED_RECIPE_ACCESS")
                .build();
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("Validation failed");

        ErrorResponse response = ErrorResponse.builder()
                .message(message)
                .code("VALIDATION_ERROR")
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception e) {
        logger.error("Unexpected error occurred", e);
        ErrorResponse response = ErrorResponse.builder()
                .message("An unexpected error occurred")
                .code("INTERNAL_ERROR")
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
