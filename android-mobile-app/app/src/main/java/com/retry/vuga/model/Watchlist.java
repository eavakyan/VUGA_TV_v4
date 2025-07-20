package com.retry.vuga.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Watchlist {

    @SerializedName("data")
    private List<ContentDetail.DataItem> data;

    @SerializedName("message")
    private String message;

    @SerializedName("status")
    private boolean status;

    public List<ContentDetail.DataItem> getData() {
        return data;
    }

    public void setData(List<ContentDetail.DataItem> data) {
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