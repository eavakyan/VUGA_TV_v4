
package com.retry.vuga.model.ads;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;


public class CustomAds {

    @Expose
    private List<DataItem> data;
    @Expose
    private String message;
    @Expose
    private Boolean status;

    public List<DataItem> getData() {
        return data;
    }

    public void setData(List<DataItem> data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public static class DataItem {
        @SerializedName("custom_ad_id")
        private Long id;
        @SerializedName("title")
        private String title;
        @SerializedName("brand_name")
        private String brandName;
        @SerializedName("brand_logo")
        private String brandLogo;
        @SerializedName("button_text")
        private String buttonText;
        @SerializedName("is_android")
        private Long isAndroid;

        @SerializedName("android_link")
        private String androidLink;
        @SerializedName("is_ios")
        private Long isIos;
        @SerializedName("ios_link")
        private String iosLink;
        @SerializedName("start_date")
        private String startDate;
        @SerializedName("end_date")
        private String endDate;

        @SerializedName("status")
        private Long status;
        @SerializedName("views")
        private Long views;
        @SerializedName("clicks")
        private Long clicks;
        @SerializedName("created_at")
        private String createdAt;

        @SerializedName("updated_at")
        private String updatedAt;


        @SerializedName("sources")
        private List<Sources> sources;


        public List<Sources> getSources() {
            return sources;
        }

        public void setAdVideos(List<Sources> sources) {
            this.sources = sources;
        }

        public String getAndroidLink() {
            return androidLink;
        }

        public void setAndroidLink(String androidLink) {
            this.androidLink = androidLink;
        }

        public String getBrandLogo() {
            return brandLogo;
        }

        public void setBrandLogo(String brandLogo) {
            this.brandLogo = brandLogo;
        }

        public String getBrandName() {
            return brandName;
        }

        public void setBrandName(String brandName) {
            this.brandName = brandName;
        }

        public String getButtonText() {
            return buttonText;
        }

        public void setButtonText(String buttonText) {
            this.buttonText = buttonText;
        }


        public Long getClicks() {
            return clicks;
        }

        public void setClicks(Long clicks) {
            this.clicks = clicks;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }

        public String getEndDate() {
            return endDate;
        }

        public void setEndDate(String endDate) {
            this.endDate = endDate;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getIosLink() {
            return iosLink;
        }

        public void setIosLink(String iosLink) {
            this.iosLink = iosLink;
        }

        public Long getIsAndroid() {
            return isAndroid;
        }

        public void setIsAndroid(Long isAndroid) {
            this.isAndroid = isAndroid;
        }

        public Long getIsIos() {
            return isIos;
        }

        public void setIsIos(Long isIos) {
            this.isIos = isIos;
        }

        public String getStartDate() {
            return startDate;
        }

        public void setStartDate(String startDate) {
            this.startDate = startDate;
        }

        public Long getStatus() {
            return status;
        }

        public void setStatus(Long status) {
            this.status = status;
        }

        public String getTitle() {
            return title;
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

        public Long getViews() {
            return views;
        }

        public void setViews(Long views) {
            this.views = views;
        }

    }

    public static class Sources {

        @SerializedName("id")
        private Long id;

        @SerializedName("custom_ad_id")
        private Long custom_ad_id;
        @SerializedName("type")
        private Long type;
        @SerializedName("content")
        private String content;
        @SerializedName("headline")
        private String headline;
        @SerializedName("description")
        private String description;
        @SerializedName("show_time")
        private int show_time;
        @SerializedName("is_skippable")
        private int is_skippable;
        @SerializedName("created_at")
        private String createdAt;
        @SerializedName("updated_at")
        private String updatedAt;


        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getHeadline() {
            return headline;
        }

        public void setHeadline(String headline) {
            this.headline = headline;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Long getType() {
            return type;
        }

        public void setType(Long type) {
            this.type = type;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(String updatedAt) {
            this.updatedAt = updatedAt;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public Long getCustom_ad_id() {
            return custom_ad_id;
        }

        public void setCustom_ad_id(Long custom_ad_id) {
            this.custom_ad_id = custom_ad_id;
        }

        public int getShow_time() {
            return show_time;
        }

        public void setShow_time(int show_time) {
            this.show_time = show_time;
        }

        public int getIs_skippable() {
            return is_skippable;
        }

        public void setIs_skippable(int is_skippable) {
            this.is_skippable = is_skippable;
        }
    }

}
