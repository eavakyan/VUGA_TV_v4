package com.retry.vuga.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ContentByGenre {

    @SerializedName("data")
    private List<ContentDetail.DataItem> data;

    @SerializedName("message")
    private String message;

    @SerializedName("status")
    private boolean status;

    public List<ContentDetail.DataItem> getData() {
        return data;
    }

    public String getMessage() {
        return message == null ? "" : message;
    }

    public boolean getStatus() {
        return status;
    }


}