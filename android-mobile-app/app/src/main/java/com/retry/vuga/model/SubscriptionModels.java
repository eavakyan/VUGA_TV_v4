package com.retry.vuga.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SubscriptionModels {

    // MARK: - API Response Models
    public static class SubscriptionPlansResponse {
        @SerializedName("status")
        private boolean status;
        
        @SerializedName("message")
        private String message;
        
        @SerializedName("data")
        private SubscriptionPlanData data;

        // Getters and setters
        public boolean isStatus() { return status; }
        public void setStatus(boolean status) { this.status = status; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public SubscriptionPlanData getData() { return data; }
        public void setData(SubscriptionPlanData data) { this.data = data; }
    }

    public static class SubscriptionPlanData {
        @SerializedName("base")
        private List<SubscriptionPricingModel> base;
        
        @SerializedName("distributors")
        private List<SubscriptionPricingModel> distributors;

        // Getters and setters
        public List<SubscriptionPricingModel> getBase() { return base; }
        public void setBase(List<SubscriptionPricingModel> base) { this.base = base; }
        
        public List<SubscriptionPricingModel> getDistributors() { return distributors; }
        public void setDistributors(List<SubscriptionPricingModel> distributors) { this.distributors = distributors; }
    }

    public static class MySubscriptionsResponse {
        @SerializedName("status")
        private boolean status;
        
        @SerializedName("message")
        private String message;
        
        @SerializedName("data")
        private UserSubscriptionData data;

        // Getters and setters
        public boolean isStatus() { return status; }
        public void setStatus(boolean status) { this.status = status; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public UserSubscriptionData getData() { return data; }
        public void setData(UserSubscriptionData data) { this.data = data; }
    }

    public static class UserSubscriptionData {
        @SerializedName("base_subscription")
        private BaseSubscriptionModel baseSubscription;
        
        @SerializedName("distributor_subscriptions")
        private List<DistributorAccessModel> distributorSubscriptions;
        
        @SerializedName("has_active_base")
        private boolean hasActiveBase;
        
        @SerializedName("active_distributor_count")
        private int activeDistributorCount;

        // Getters and setters
        public BaseSubscriptionModel getBaseSubscription() { return baseSubscription; }
        public void setBaseSubscription(BaseSubscriptionModel baseSubscription) { this.baseSubscription = baseSubscription; }
        
        public List<DistributorAccessModel> getDistributorSubscriptions() { return distributorSubscriptions; }
        public void setDistributorSubscriptions(List<DistributorAccessModel> distributorSubscriptions) { this.distributorSubscriptions = distributorSubscriptions; }
        
        public boolean isHasActiveBase() { return hasActiveBase; }
        public void setHasActiveBase(boolean hasActiveBase) { this.hasActiveBase = hasActiveBase; }
        
        public int getActiveDistributorCount() { return activeDistributorCount; }
        public void setActiveDistributorCount(int activeDistributorCount) { this.activeDistributorCount = activeDistributorCount; }
    }

    // MARK: - Subscription Models
    public static class SubscriptionPricingModel {
        @SerializedName("pricing_id")
        private int pricingId;
        
        @SerializedName("pricing_type")
        private String pricingType;
        
        @SerializedName("billing_period")
        private String billingPeriod;
        
        @SerializedName("price")
        private String price;
        
        @SerializedName("currency")
        private String currency;
        
        @SerializedName("display_name")
        private String displayName;
        
        @SerializedName("description")
        private String description;
        
        @SerializedName("distributor_name")
        private String distributorName;
        
        @SerializedName("distributor_code")
        private String distributorCode;
        
        @SerializedName("distributor_logo")
        private String distributorLogo;

        // Getters and setters
        public int getPricingId() { return pricingId; }
        public void setPricingId(int pricingId) { this.pricingId = pricingId; }
        
        public String getPricingType() { return pricingType; }
        public void setPricingType(String pricingType) { this.pricingType = pricingType; }
        
        public String getBillingPeriod() { return billingPeriod; }
        public void setBillingPeriod(String billingPeriod) { this.billingPeriod = billingPeriod; }
        
        public String getPrice() { return price; }
        public void setPrice(String price) { this.price = price; }
        
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
        
        public String getDisplayName() { return displayName; }
        public void setDisplayName(String displayName) { this.displayName = displayName; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getDistributorName() { return distributorName; }
        public void setDistributorName(String distributorName) { this.distributorName = distributorName; }
        
        public String getDistributorCode() { return distributorCode; }
        public void setDistributorCode(String distributorCode) { this.distributorCode = distributorCode; }
        
        public String getDistributorLogo() { return distributorLogo; }
        public void setDistributorLogo(String distributorLogo) { this.distributorLogo = distributorLogo; }

        // Helper methods
        public String getFormattedPrice() {
            String currencySymbol = "USD".equals(currency) ? "$" : currency;
            return currencySymbol + price;
        }

        public String getIntervalText() {
            switch (billingPeriod != null ? billingPeriod : "") {
                case "daily": return "per day";
                case "weekly": return "per week";
                case "monthly": return "per month";
                case "quarterly": return "per quarter";
                case "yearly": return "per year";
                case "lifetime": return "lifetime";
                default: return billingPeriod != null ? billingPeriod : "";
            }
        }
    }

    public static class DistributorModel {
        @SerializedName("distributor_name")
        private String distributorName;
        
        @SerializedName("distributor_code")
        private String distributorCode;
        
        @SerializedName("distributor_logo")
        private String distributorLogo;
        
        @SerializedName("description")
        private String description;

        public DistributorModel() {}

        public DistributorModel(String distributorName, String distributorCode, String distributorLogo, String description) {
            this.distributorName = distributorName;
            this.distributorCode = distributorCode;
            this.distributorLogo = distributorLogo;
            this.description = description;
        }

        // Getters and setters
        public String getDistributorName() { return distributorName != null ? distributorName : ""; }
        public void setDistributorName(String distributorName) { this.distributorName = distributorName; }
        
        public String getDistributorCode() { return distributorCode != null ? distributorCode : ""; }
        public void setDistributorCode(String distributorCode) { this.distributorCode = distributorCode; }
        
        public String getDistributorLogo() { return distributorLogo; }
        public void setDistributorLogo(String distributorLogo) { this.distributorLogo = distributorLogo; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        // Helper methods
        public String getName() { return getDistributorName(); }
        public String getCode() { return getDistributorCode(); }
        public String getLogoUrl() { return getDistributorLogo(); }
    }

    public static class BaseSubscriptionModel {
        @SerializedName("subscription_id")
        private int subscriptionId;
        
        @SerializedName("app_user_id")
        private int appUserId;
        
        @SerializedName("start_date")
        private String startDate;
        
        @SerializedName("end_date")
        private String endDate;
        
        @SerializedName("is_active")
        private int isActive;
        
        @SerializedName("subscription_type")
        private String subscriptionType;
        
        @SerializedName("auto_renew")
        private int autoRenew;
        
        @SerializedName("created_at")
        private String createdAt;
        
        @SerializedName("updated_at")
        private String updatedAt;

        // Getters and setters
        public int getSubscriptionId() { return subscriptionId; }
        public void setSubscriptionId(int subscriptionId) { this.subscriptionId = subscriptionId; }
        
        public int getAppUserId() { return appUserId; }
        public void setAppUserId(int appUserId) { this.appUserId = appUserId; }
        
        public String getStartDate() { return startDate; }
        public void setStartDate(String startDate) { this.startDate = startDate; }
        
        public String getEndDate() { return endDate; }
        public void setEndDate(String endDate) { this.endDate = endDate; }
        
        public int getIsActive() { return isActive; }
        public void setIsActive(int isActive) { this.isActive = isActive; }
        
        public String getSubscriptionType() { return subscriptionType; }
        public void setSubscriptionType(String subscriptionType) { this.subscriptionType = subscriptionType; }
        
        public int getAutoRenew() { return autoRenew; }
        public void setAutoRenew(int autoRenew) { this.autoRenew = autoRenew; }
        
        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
        
        public String getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

        // Helper methods
        public boolean isActiveStatus() {
            return isActive == 1;
        }

        public String getStatusText() {
            return isActiveStatus() ? "Active" : "Inactive";
        }

        public String getFormattedEndDate() {
            // TODO: Format date properly
            return endDate;
        }
    }

    public static class DistributorAccessModel {
        @SerializedName("access_id")
        private int accessId;
        
        @SerializedName("app_user_id")
        private int appUserId;
        
        @SerializedName("content_distributor_id")
        private int contentDistributorId;
        
        @SerializedName("start_date")
        private String startDate;
        
        @SerializedName("end_date")
        private String endDate;
        
        @SerializedName("is_active")
        private int isActive;
        
        @SerializedName("subscription_type")
        private String subscriptionType;
        
        @SerializedName("auto_renew")
        private int autoRenew;
        
        @SerializedName("created_at")
        private String createdAt;
        
        @SerializedName("updated_at")
        private String updatedAt;
        
        @SerializedName("distributor_name")
        private String distributorName;
        
        @SerializedName("distributor_code")
        private String distributorCode;
        
        @SerializedName("distributor_logo")
        private String distributorLogo;

        // Getters and setters
        public int getAccessId() { return accessId; }
        public void setAccessId(int accessId) { this.accessId = accessId; }
        
        public int getAppUserId() { return appUserId; }
        public void setAppUserId(int appUserId) { this.appUserId = appUserId; }
        
        public int getContentDistributorId() { return contentDistributorId; }
        public void setContentDistributorId(int contentDistributorId) { this.contentDistributorId = contentDistributorId; }
        
        public String getStartDate() { return startDate; }
        public void setStartDate(String startDate) { this.startDate = startDate; }
        
        public String getEndDate() { return endDate; }
        public void setEndDate(String endDate) { this.endDate = endDate; }
        
        public int getIsActive() { return isActive; }
        public void setIsActive(int isActive) { this.isActive = isActive; }
        
        public String getSubscriptionType() { return subscriptionType; }
        public void setSubscriptionType(String subscriptionType) { this.subscriptionType = subscriptionType; }
        
        public int getAutoRenew() { return autoRenew; }
        public void setAutoRenew(int autoRenew) { this.autoRenew = autoRenew; }
        
        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
        
        public String getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
        
        public String getDistributorName() { return distributorName; }
        public void setDistributorName(String distributorName) { this.distributorName = distributorName; }
        
        public String getDistributorCode() { return distributorCode; }
        public void setDistributorCode(String distributorCode) { this.distributorCode = distributorCode; }
        
        public String getDistributorLogo() { return distributorLogo; }
        public void setDistributorLogo(String distributorLogo) { this.distributorLogo = distributorLogo; }

        // Helper methods
        public boolean isActiveStatus() {
            return isActive == 1;
        }

        public String getStatusText() {
            return isActiveStatus() ? "Active" : "Inactive";
        }

        public DistributorModel getDistributor() {
            return new DistributorModel(
                distributorName != null ? distributorName : "",
                distributorCode != null ? distributorCode : "",
                distributorLogo,
                null
            );
        }
    }
}