package com.retry.vuga.models;

import com.google.gson.annotations.SerializedName;
import com.retry.vuga.model.Profile;

public class ProfileAvatarResponse {
    @SerializedName("status")
    private boolean status;
    
    @SerializedName("message")
    private String message;
    
    @SerializedName("profile")
    private Profile profile;
    
    @SerializedName("avatar_url")
    private String avatarUrl;
    
    public boolean isStatus() {
        return status;
    }
    
    public boolean getStatus() {
        return status;
    }
    
    public void setStatus(boolean status) {
        this.status = status;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public Profile getProfile() {
        return profile;
    }
    
    public void setProfile(Profile profile) {
        this.profile = profile;
    }
    
    public String getAvatarUrl() {
        return avatarUrl;
    }
    
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}