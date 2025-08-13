package com.retry.vuga.model;

import com.google.gson.annotations.SerializedName;

public class UserRegistration {

    @SerializedName("message")
    private String message;

    @SerializedName("status")
    private boolean status;

    @SerializedName("data")
    private Data data;



    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public static class Data {

        @SerializedName("app_user_id")
        private int id;

        @SerializedName("fullname")
        private String fullname;

        @SerializedName("email")
        private String email;

        @SerializedName("login_type")
        private int loginType;

        @SerializedName("identity")
        private String identity;

        @SerializedName("profile_image")
        private String profileImage;


        @SerializedName("watchlist_content_ids")
        private String watchlist_content_ids = "";

        @SerializedName("device_type")
        private int deviceType;

        @SerializedName("device_token")
        private String deviceToken;

        @SerializedName("status")
        private int status;

        @SerializedName("is_premium")
        private int isPremium;
        
        @SerializedName("profiles")
        private java.util.List<Profile> profiles;
        
        @SerializedName("last_active_profile")
        private Profile lastActiveProfile;
        
        @SerializedName("last_active_profile_id")
        private Integer lastActiveProfileId;
        
        @SerializedName("email_consent")
        private Boolean emailConsent;
        
        @SerializedName("sms_consent")
        private Boolean smsConsent;

        public String getWatchlist_content_ids() {
            return watchlist_content_ids == null ? "" : watchlist_content_ids;
        }

        public void setWatchlist_content_ids(String watchlist_content_ids) {
            this.watchlist_content_ids = watchlist_content_ids;
        }

        public int getLoginType() {
            return loginType;
        }

        public void setLoginType(int loginType) {
            this.loginType = loginType;
        }

        public int getDeviceType() {
            return deviceType;
        }

        public void setDeviceType(int deviceType) {
            this.deviceType = deviceType;
        }


        public String getProfileImage() {
            return profileImage == null ? "" : profileImage;
        }

        public void setProfileImage(String profileImage) {
            this.profileImage = profileImage;
        }

        public int getIsPremium() {
            return isPremium;
        }

        public void setIsPremium(int isPremium) {
            this.isPremium = isPremium;
        }


        public String getIdentity() {
            return identity;
        }

        public void setIdentity(String identity) {
            this.identity = identity;
        }

        public String getDeviceToken() {
            return deviceToken;
        }

        public void setDeviceToken(String deviceToken) {
            this.deviceToken = deviceToken;
        }


        public int getId() {
            return id;
        }


        public void setId(int id) {
            this.id = id;
        }

        public String getFullname() {
            return fullname == null ? "" : fullname;
        }

        public void setFullname(String fullname) {
            this.fullname = fullname;
        }

        public String getEmail() {
            return email == null ? "" : email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }
        
        public java.util.List<Profile> getProfiles() {
            return profiles;
        }
        
        public void setProfiles(java.util.List<Profile> profiles) {
            this.profiles = profiles;
        }
        
        public Profile getLastActiveProfile() {
            return lastActiveProfile;
        }
        
        public void setLastActiveProfile(Profile lastActiveProfile) {
            this.lastActiveProfile = lastActiveProfile;
        }
        
        public Integer getLastActiveProfileId() {
            return lastActiveProfileId;
        }
        
        public void setLastActiveProfileId(Integer lastActiveProfileId) {
            this.lastActiveProfileId = lastActiveProfileId;
        }
    }
    
    public static class Profile {
        @SerializedName("profile_id")
        private int profileId;
        
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
        
        @SerializedName("email_consent")
        private Boolean emailConsent;
        
        @SerializedName("sms_consent")
        private Boolean smsConsent;
        
        public int getProfileId() {
            return profileId;
        }
        
        public void setProfileId(int profileId) {
            this.profileId = profileId;
        }
        
        public String getName() {
            return name == null ? "" : name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getAvatarType() {
            return avatarType;
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
            return avatarColor;
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
        
        public boolean isEmailConsent() {
            return emailConsent != null ? emailConsent : false;
        }
        
        public void setEmailConsent(Boolean emailConsent) {
            this.emailConsent = emailConsent;
        }
        
        public boolean isSmsConsent() {
            return smsConsent != null ? smsConsent : false;
        }
        
        public void setSmsConsent(Boolean smsConsent) {
            this.smsConsent = smsConsent;
        }
    }


}