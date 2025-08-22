package com.retry.vuga.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class UnifiedWatchlistResponse {
    
    @SerializedName("status")
    private boolean status;
    
    @SerializedName("message")
    private String message;
    
    @SerializedName("data")
    private List<UnifiedWatchlistItem> data;
    
    public boolean isStatus() {
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
    
    public List<UnifiedWatchlistItem> getData() {
        return data;
    }
    
    public void setData(List<UnifiedWatchlistItem> data) {
        this.data = data;
    }
}