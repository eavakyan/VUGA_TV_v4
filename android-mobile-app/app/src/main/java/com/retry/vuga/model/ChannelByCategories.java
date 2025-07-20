package com.retry.vuga.model;

import com.google.gson.annotations.SerializedName;

public class ChannelByCategories {

    @SerializedName("data")
    private LiveTv.CategoryItem data;

    @SerializedName("message")
    private String message;

    @SerializedName("status")
    private boolean status;

    public LiveTv.CategoryItem getData() {
        return data;
    }

    public void setData(LiveTv.CategoryItem data) {
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

}
