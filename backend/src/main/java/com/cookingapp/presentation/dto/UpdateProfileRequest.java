package com.cookingapp.presentation.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * プロフィール更新リクエスト
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {
    
    @Size(min = 1, max = 50, message = "Nickname must be between 1 and 50 characters")
    private String nickname;
    
    @Size(min = 1, max = 50, message = "Display name must be between 1 and 50 characters")
    private String displayName;
    
    private String preferredLanguage; // "ja" or "ko"
    
    private String timezone;
    
    private Boolean marketingOptOut;
}
