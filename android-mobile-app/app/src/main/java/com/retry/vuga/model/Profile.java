package com.retry.vuga.model;

import com.google.gson.annotations.SerializedName;

public class Profile {
    @SerializedName("profile_id")
    private int profileId;
    
    @SerializedName("app_user_id")
    private int appUserId;
    
    @SerializedName("name")
    private String name;
    
    @SerializedName("avatar_type")
    private String avatarType;
    
    @SerializedName("avatar_url")
    private String avatarUrl;
    
    @SerializedName("avatar_color")
    private String avatarColor;
    
    @SerializedName("is_kids")
    private boolean isKids;
    
    @SerializedName("is_active")
    private boolean isActive;
    
    @SerializedName("created_at")
    private String createdAt;
    
    @SerializedName("updated_at")
    private String updatedAt;

    // Getters and Setters
    public int getProfileId() {
        return profileId;
    }

    public void setProfileId(int profileId) {
        this.profileId = profileId;
    }

    public int getAppUserId() {
        return appUserId;
    }

    public void setAppUserId(int appUserId) {
        this.appUserId = appUserId;
    }

    public String getName() {
        return name == null ? "" : name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatarType() {
        return avatarType == null ? "color" : avatarType;
    }

    public void setAvatarType(String avatarType) {
        this.avatarType = avatarType;
    }

    public String getAvatarUrl() {
        return avatarUrl == null ? "" : avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getAvatarColor() {
        return avatarColor == null ? "#FF5252" : avatarColor;
    }

    public void setAvatarColor(String avatarColor) {
        this.avatarColor = avatarColor;
    }

    public boolean isKids() {
        return isKids;
    }

    public void setKids(boolean kids) {
        isKids = kids;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}