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

        @SerializedName("id")
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
        private String watchlist_content_ids;

        @SerializedName("device_type")
        private int deviceType;

        @SerializedName("device_token")
        private String deviceToken;

        @SerializedName("status")
        private int status;

        @SerializedName("is_premium")
        private int isPremium;

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
    }


}