package com.retry.vuga.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class SearchChannel {

    @SerializedName("data")
    private List<LiveTv.CategoryItem.TvChannelItem> data;

    @SerializedName("message")
    private String message;

    @SerializedName("status")
    private boolean status;

    public List<LiveTv.CategoryItem.TvChannelItem> getData() {
        return data == null ? new ArrayList<>() : data;
    }

    public void setData(List<LiveTv.CategoryItem.TvChannelItem> data) {
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
