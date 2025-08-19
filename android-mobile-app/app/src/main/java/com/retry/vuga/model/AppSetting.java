package com.retry.vuga.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class AppSetting {


    @SerializedName("status")
    private boolean status;

    @SerializedName("message")
    private String message;

    @SerializedName("setting")
    private Data setting;

    @SerializedName("admob")
    private List<Admob> admob;


    @SerializedName("genres")
    private List<GenreItem> genreItems;

    @SerializedName("languages")
    private List<LanguageItem> languageItems;



    public String getMessage() {
        return message;
    }

    public boolean getStatus() {
        return status;
    }

    public List<Admob> getAds() {
        return admob == null ? new ArrayList<>() : admob;
    }

    public Data getSettings() {
        return setting == null ? new Data() : setting;
    }

    public List<GenreItem> getGenreItems() {
        return genreItems == null ? new ArrayList<>() : genreItems;
    }

    public void setGenreItems(List<GenreItem> genreItems) {
        this.genreItems = genreItems;
    }

    public List<LanguageItem> getLanguageItems() {
        return languageItems == null ? new ArrayList<>() : languageItems;
    }

    public void setLanguageItems(List<LanguageItem> languageItems) {
        this.languageItems = languageItems;
    }

    public static class Data {

        @SerializedName("id")
        private int id;

        @SerializedName("privacy_url")
        private String privacyUrl;

        @SerializedName("terms_url")
        private String termsUrl;

        @SerializedName("more_apps_url")
        private String moreAppsUrl;

        @SerializedName("google_play_licence_key")
        private Object googlePlayLicenceKey;

        @SerializedName("app_name")
        private String appName;

        @SerializedName("is_live_tv_enable")
        private boolean liveTvEnable;

        @SerializedName("is_admob_android")
        private int isAdmobAnd;

        @SerializedName("is_custom_android")
        private int isCustomAnd;

        @SerializedName("videoad_skip_time")
        private int videoSkipTime;

        @SerializedName("updated_at")
        private String updatedAt;

        @SerializedName("created_at")
        private String createdAt;


        public String getAppName() {
            return appName;
        }

        public void setAppName(String appName) {
            this.appName = appName;
        }

        public boolean getLiveTvEnable() {
            return liveTvEnable;
        }

        public void setLiveTvEnable(boolean liveTvEnable) {
            this.liveTvEnable = liveTvEnable;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }

        public String getTermsUrl() {
            return termsUrl == null ? "" : termsUrl;
        }

        public String getPrivacyUrl() {
            return privacyUrl == null ? "" : privacyUrl;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public int getId() {
            return id;
        }

        public Object getGooglePlayLicenceKey() {
            return googlePlayLicenceKey;
        }

        public String getMoreAppsUrl() {
            return moreAppsUrl == null ? "" : moreAppsUrl;
        }

        public int getIsAdmobAnd() {
            return isAdmobAnd;
        }

        public void setIsAdmobAnd(int isAdmobAnd) {
            this.isAdmobAnd = isAdmobAnd;
        }

        public int getIsCustomAnd() {
            return isCustomAnd;
        }

        public void setIsCustomAnd(int isCustomAnd) {
            this.isCustomAnd = isCustomAnd;
        }

        public int getVideoSkipTime() {
            return videoSkipTime;
        }

        public void setVideoSkipTime(int videoSkipTime) {
            this.videoSkipTime = videoSkipTime;
        }
    }

    public static class Admob {

        @SerializedName("id")
        private int id;
        @SerializedName("banner_id")
        private String banner_id;

        @SerializedName("intersial_id")
        private String intersial_id;

        @SerializedName("rewarded_id")
        private String rewarded_id;

        @SerializedName("type")
        private int type;

        @SerializedName("updated_at")
        private String updatedAt;


        @SerializedName("created_at")
        private String createdAt;


        @SerializedName("android_admob_native_id")
        private String androidAdmobNativeId;

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public String getIntersial_id() {
            return intersial_id;
        }

        public String getAndroidAdmobNativeId() {
            return androidAdmobNativeId;
        }


        public String getRewarded_id() {
            return rewarded_id;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }

        public int getId() {
            return id;
        }

        public String getBanner_id() {
            return banner_id;
        }


        public String getCreatedAt() {
            return createdAt;
        }


    }

    public static class GenreItem {

        @SerializedName("id")
        private int id;

        @SerializedName("title")
        private String title;

        @SerializedName("updated_at")
        private String updatedAt;

        @SerializedName("created_at")
        private String createdAt;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getTitle() {
            return title == null ? "" : title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(String updatedAt) {
            this.updatedAt = updatedAt;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }
    }

    public static class LanguageItem {

        @SerializedName("id")
        private int id;

        @SerializedName("title")
        private String title;

        @SerializedName("updated_at")
        private String updatedAt;

        @SerializedName("created_at")
        private String createdAt;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getTitle() {
            return title == null ? "" : title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(String updatedAt) {
            this.updatedAt = updatedAt;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }
    }
}