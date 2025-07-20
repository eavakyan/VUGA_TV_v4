package com.retry.vuga.model;

import com.google.gson.annotations.SerializedName;

public class UserLogout {

    @SerializedName("response_code")
    private int responseCode;

    @SerializedName("success_code")
    private int successCode;

    @SerializedName("response_message")
    private String responseMessage;

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public int getSuccessCode() {
        return successCode;
    }

    public void setSuccessCode(int successCode) {
        this.successCode = successCode;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }
}