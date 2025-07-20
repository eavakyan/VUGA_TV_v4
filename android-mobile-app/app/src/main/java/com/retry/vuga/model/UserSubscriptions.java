package com.retry.vuga.model;

import com.google.gson.annotations.SerializedName;

public class UserSubscriptions {

    @SerializedName("response_code")
    private int responseCode;

    @SerializedName("data")
    private DataItem data;

    @SerializedName("message")
    private String message;

    @SerializedName("status")
    private boolean status;

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public DataItem getData() {
        return data == null ? new DataItem() : data;
    }

    public void setData(DataItem data) {
        this.data = data;
    }

    public String getMessage() {
        return message == null ? "" : message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public static class DataItem {

        @SerializedName("subscription_id")
        private String subscriptionId;

        @SerializedName("duration")
        private int duration;

        @SerializedName("transaction_id")
        private Object transactionId;

        @SerializedName("amount")
        private float amount;

        @SerializedName("payment_type")
        private int paymentType;

        @SerializedName("currency")
        private String currency;

        @SerializedName("package_id")
        private int packageId;

        @SerializedName("expired_date")
        private String expiredDate;

        @SerializedName("start_date")
        private String startDate;


        @SerializedName("summary")
        private Object summary;

        @SerializedName("content_id")
        private String contentId;

        @SerializedName("created_at")
        private String createdAt;


        @SerializedName("is_delete")
        private int isDelete;


        @SerializedName("updated_at")
        private String updatedAt;

        @SerializedName("user_id")
        private String userId;


        @SerializedName("id")
        private int id;


        @SerializedName("days")
        private Object days;


        public String getSubscriptionId() {
            return subscriptionId == null ? "" : subscriptionId;
        }

        public void setSubscriptionId(String subscriptionId) {
            this.subscriptionId = subscriptionId;
        }

        public int getDuration() {
            return duration;
        }

        public void setDuration(int duration) {
            this.duration = duration;
        }

        public Object getTransactionId() {
            return transactionId;
        }

        public void setTransactionId(Object transactionId) {
            this.transactionId = transactionId;
        }

        public float getAmount() {
            return amount;
        }

        public void setAmount(float amount) {
            this.amount = amount;
        }

        public int getPaymentType() {
            return paymentType;
        }

        public void setPaymentType(int paymentType) {
            this.paymentType = paymentType;
        }

        public String getCurrency() {
            return currency == null ? "" : currency;
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

        public String getExpiredDate() {
            return expiredDate;
        }

        public void setExpiredDate(String expiredDate) {
            this.expiredDate = expiredDate;
        }

        public String getStartDate() {
            return startDate == null ? "" : startDate;
        }

        public void setStartDate(String startDate) {
            this.startDate = startDate;
        }


        public Object getSummary() {
            return summary;
        }

        public void setSummary(Object summary) {
            this.summary = summary;
        }

        public String getContentId() {
            return contentId == null ? "" : contentId;
        }

        public void setContentId(String contentId) {
            this.contentId = contentId;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }

        public int getIsDelete() {
            return isDelete;
        }

        public void setIsDelete(int isDelete) {
            this.isDelete = isDelete;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(String updatedAt) {
            this.updatedAt = updatedAt;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public Object getDays() {
            return days;
        }

        public void setDays(Object days) {
            this.days = days;
        }
    }
}