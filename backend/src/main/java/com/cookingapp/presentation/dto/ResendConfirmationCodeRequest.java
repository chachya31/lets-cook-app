package com.cookingapp.presentation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * 確認コード再送信リクエスト
 */
public class ResendConfirmationCodeRequest {
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    public ResendConfirmationCodeRequest() {
    }

    public ResendConfirmationCodeRequest(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
