package com.cookingapp.presentation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * メール確認コード検証リクエスト
 */
public class ConfirmSignUpRequest {
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    
    @NotBlank(message = "Confirmation code is required")
    private String confirmationCode;

    public ConfirmSignUpRequest() {
    }

    public ConfirmSignUpRequest(String email, String confirmationCode) {
        this.email = email;
        this.confirmationCode = confirmationCode;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getConfirmationCode() {
        return confirmationCode;
    }

    public void setConfirmationCode(String confirmationCode) {
        this.confirmationCode = confirmationCode;
    }
}
