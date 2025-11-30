package com.cookingapp.presentation.exception;

import com.cookingapp.application.validation.ImageValidationException;
import com.cookingapp.domain.exception.AuthenticationException;
import com.cookingapp.domain.exception.UserAlreadyExistsException;
import com.cookingapp.domain.exception.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * グローバル例外ハンドラー
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * バリデーションエラー（400）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ErrorResponse response = new ErrorResponse(
                "VALIDATION_ERROR",
                "Validation failed",
                errors,
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 認証エラー（401）
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex) {
        ErrorResponse response = new ErrorResponse(
                "AUTHENTICATION_ERROR",
                ex.getMessage(),
                null,
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * ユーザー重複エラー（409）
     */
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExistsException(UserAlreadyExistsException ex) {
        ErrorResponse response = new ErrorResponse(
                "USER_ALREADY_EXISTS",
                ex.getMessage(),
                null,
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    /**
     * 画像バリデーションエラー（400）
     */
    @ExceptionHandler(ImageValidationException.class)
    public ResponseEntity<ErrorResponse> handleImageValidationException(ImageValidationException ex) {
        ErrorResponse response = new ErrorResponse(
                "IMAGE_VALIDATION_ERROR",
                ex.getMessage(),
                null,
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * ユーザー未検出エラー（404）
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(UserNotFoundException ex) {
        ErrorResponse response = new ErrorResponse(
                "USER_NOT_FOUND",
                ex.getMessage(),
                null,
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * 不正な引数エラー（400）
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        ErrorResponse response = new ErrorResponse(
                "INVALID_ARGUMENT",
                ex.getMessage(),
                null,
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 不正な状態エラー（400）
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(IllegalStateException ex) {
        ErrorResponse response = new ErrorResponse(
                "INVALID_STATE",
                ex.getMessage(),
                null,
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * DynamoDB リソース未検出エラー（500）
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        logger.error("DynamoDB table not found: {}", ex.getMessage(), ex);
        
        Map<String, String> details = new HashMap<>();
        details.put("error", "DynamoDB table does not exist");
        details.put("suggestion", "Please ensure LocalStack is running and tables are created");
        details.put("awsErrorCode", ex.awsErrorDetails().errorCode());
        details.put("awsErrorMessage", ex.awsErrorDetails().errorMessage());
        details.put("stackTrace", getStackTraceAsString(ex));
        
        ErrorResponse response = new ErrorResponse(
                "DYNAMODB_TABLE_NOT_FOUND",
                "Database table not found: " + ex.getMessage(),
                details,
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * DynamoDB エラー（500）
     */
    @ExceptionHandler(DynamoDbException.class)
    public ResponseEntity<ErrorResponse> handleDynamoDbException(DynamoDbException ex) {
        logger.error("DynamoDB error: {}", ex.getMessage(), ex);
        
        Map<String, String> details = new HashMap<>();
        details.put("awsErrorCode", ex.awsErrorDetails().errorCode());
        details.put("awsErrorMessage", ex.awsErrorDetails().errorMessage());
        details.put("statusCode", String.valueOf(ex.statusCode()));
        details.put("stackTrace", getStackTraceAsString(ex));
        
        ErrorResponse response = new ErrorResponse(
                "DYNAMODB_ERROR",
                "Database error: " + ex.getMessage(),
                details,
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * その他のエラー（500）
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        logger.error("Unexpected error occurred: {}", ex.getMessage(), ex);
        
        Map<String, String> details = new HashMap<>();
        details.put("exceptionType", ex.getClass().getName());
        details.put("message", ex.getMessage());
        details.put("stackTrace", getStackTraceAsString(ex));
        
        ErrorResponse response = new ErrorResponse(
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred: " + ex.getMessage(),
                details,
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    
    /**
     * スタックトレースを文字列として取得
     */
    private String getStackTraceAsString(Exception ex) {
        StringBuilder sb = new StringBuilder();
        sb.append(ex.getClass().getName()).append(": ").append(ex.getMessage()).append("\n");
        
        StackTraceElement[] elements = ex.getStackTrace();
        int limit = Math.min(10, elements.length); // 最初の10行のみ
        for (int i = 0; i < limit; i++) {
            sb.append("  at ").append(elements[i].toString()).append("\n");
        }
        
        if (elements.length > limit) {
            sb.append("  ... ").append(elements.length - limit).append(" more\n");
        }
        
        return sb.toString();
    }

    /**
     * エラーレスポンス
     */
    public record ErrorResponse(
            String code,
            String message,
            Map<String, String> details,
            LocalDateTime timestamp
    ) {}
}
