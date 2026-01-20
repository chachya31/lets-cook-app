package com.cookingapp.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@DynamoDbBean
@Getter
@Builder
@AllArgsConstructor
public class User {

    private String userId;
    private String email;
    private String nickname;
    private String displayName;
    private String profileImageUrl;
    private String preferredLanguage;
    private String lastLoginDate;
    private String createdAt;
    private String timezone;
    private Boolean marketingOptOut;

    public User() {
    }

    @DynamoDbPartitionKey
    @DynamoDbAttribute("UserId")
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @DynamoDbAttribute("Email")
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @DynamoDbAttribute("Nickname")
    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    @DynamoDbAttribute("DisplayName")
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @DynamoDbAttribute("ProfileImageUrl")
    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    @DynamoDbAttribute("PreferredLanguage")
    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    public void setPreferredLanguage(String preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }

    @DynamoDbAttribute("LastLoginDate")
    public String getLastLoginDate() {
        return lastLoginDate;
    }

    public void setLastLoginDate(String lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }

    @DynamoDbAttribute("CreatedAt")
    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    @DynamoDbAttribute("Timezone")
    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    @DynamoDbAttribute("MarketingOptOut")
    public Boolean getMarketingOptOut() {
        return marketingOptOut;
    }

    public void setMarketingOptOut(Boolean marketingOptOut) {
        this.marketingOptOut = marketingOptOut;
    }
}
