package com.retry.vuga.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class LiveTvResponse {
    
    @SerializedName("status")
    private boolean status;
    
    @SerializedName("message")
    private String message;
    
    @SerializedName("channels")
    private List<LiveTvChannel> channels;
    
    @SerializedName("categories")
    private List<LiveTvCategory> categories;
    
    @SerializedName("data")  // For old API format compatibility
    private List<LiveTvCategory> data;
    
    @SerializedName("total")
    private int total;
    
    @SerializedName("current_page")
    private int currentPage;
    
    @SerializedName("last_page")
    private int lastPage;
    
    // Getters and Setters
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
    
    public List<LiveTvChannel> getChannels() {
        return channels;
    }
    
    public void setChannels(List<LiveTvChannel> channels) {
        this.channels = channels;
    }
    
    public List<LiveTvCategory> getCategories() {
        return categories;
    }
    
    public void setCategories(List<LiveTvCategory> categories) {
        this.categories = categories;
    }
    
    public List<LiveTvCategory> getData() {
        return data;
    }
    
    public void setData(List<LiveTvCategory> data) {
        this.data = data;
    }
    
    public int getTotal() {
        return total;
    }
    
    public void setTotal(int total) {
        this.total = total;
    }
    
    public int getCurrentPage() {
        return currentPage;
    }
    
    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }
    
    public int getLastPage() {
        return lastPage;
    }
    
    public void setLastPage(int lastPage) {
        this.lastPage = lastPage;
    }
    
    // Inner classes for other response types
    public static class ScheduleGridResponse {
        @SerializedName("status")
        private boolean status;
        
        @SerializedName("message")
        private String message;
        
        @SerializedName("schedule")
        private Object schedule; // Can be customized based on actual response
        
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
        
        public Object getSchedule() {
            return schedule;
        }
        
        public void setSchedule(Object schedule) {
            this.schedule = schedule;
        }
    }
    
    public static class ViewTrackingResponse {
        @SerializedName("status")
        private boolean status;
        
        @SerializedName("message")
        private String message;
        
        @SerializedName("view_count")
        private int viewCount;
        
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
        
        public int getViewCount() {
            return viewCount;
        }
        
        public void setViewCount(int viewCount) {
            this.viewCount = viewCount;
        }
    }
}