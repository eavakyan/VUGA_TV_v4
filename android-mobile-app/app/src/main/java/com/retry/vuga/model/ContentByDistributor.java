package com.retry.vuga.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ContentByDistributor {

    @SerializedName("data")
    private List<HomePage.GenreContents> data;

    @SerializedName("message")
    private String message;

    @SerializedName("status")
    private boolean status;

    public List<HomePage.GenreContents> getData() {
        return data;
    }

    public String getMessage() {
        return message == null ? "" : message;
    }

    public boolean getStatus() {
        return status;
    }
}