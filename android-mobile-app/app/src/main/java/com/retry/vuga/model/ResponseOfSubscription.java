package com.retry.vuga.model;

import com.google.gson.annotations.SerializedName;

public class ResponseOfSubscription {

    @SerializedName("response_code")
    private int responseCode;

    @SerializedName("message")
    private String message;

    @SerializedName("status")
    private boolean status;

    public int getResponseCode() {
        return responseCode;
    }

    public String getMessage() {
        return message == null ? "" : message;
    }

    public boolean isStatus() {
        return status;
    }
}