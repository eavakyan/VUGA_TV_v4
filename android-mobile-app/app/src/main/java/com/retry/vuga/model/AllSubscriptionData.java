package com.retry.vuga.model;

import com.google.gson.annotations.SerializedName;

public class AllSubscriptionData {

    @SerializedName("monthly_data")
    private DataItem monthlyData;

    @SerializedName("yearly_data")
    private DataItem yearlyData;

    @SerializedName("message")
    private String message;

    @SerializedName("status")
    private int status;

    public DataItem getMonthlyData() {
        return monthlyData;
    }

    public void setMonthlyData(DataItem monthlyData) {
        this.monthlyData = monthlyData;
    }

    public DataItem getYearlyData() {
        return yearlyData;
    }

    public void setYearlyData(DataItem yearlyData) {
        this.yearlyData = yearlyData;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public static class DataItem {

        @SerializedName("duration")
        private int duration;

        @SerializedName("android_product_id")
        private String androidProductId;

        @SerializedName("ios_product_id")
        private String iosProductId;

        @SerializedName("updated_at")
        private String updatedAt;

        @SerializedName("price")
        private String price;

        @SerializedName("days")
        private String days;

        @SerializedName("created_at")
        private String createdAt;

        @SerializedName("currency")
        private String currency;

        @SerializedName("package_id")
        private int packageId;

        public int getDuration() {
            return duration;
        }

        public void setDuration(int duration) {
            this.duration = duration;
        }

        public String getAndroidProductId() {
            return androidProductId;
        }

        public void setAndroidProductId(String androidProductId) {
            this.androidProductId = androidProductId;
        }

        public String getIosProductId() {
            return iosProductId;
        }

        public void setIosProductId(String iosProductId) {
            this.iosProductId = iosProductId;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(String updatedAt) {
            this.updatedAt = updatedAt;
        }

        public String getPrice() {
            return price;
        }

        public void setPrice(String price) {
            this.price = price;
        }

        public String getDays() {
            return days;
        }

        public void setDays(String days) {
            this.days = days;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }

        public String getCurrency() {
            return currency;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }

        public int getPackageId() {
            return packageId;
        }

        public void setPackageId(int packageId) {
            this.packageId = packageId;
        }
    }


}