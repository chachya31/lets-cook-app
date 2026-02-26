package com.cookingapp.presentation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * ユーザー登録リクエスト
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterUserRequest {
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$", 
             message = "Password must contain uppercase, lowercase, and numbers")
    private String password;
    
    @NotBlank(message = "Nickname is required")
    @Size(min = 1, max = 50, message = "Nickname must be between 1 and 50 characters")
    private String nickname;
    
    private String preferredLanguage; // "ja" or "ko"
}
