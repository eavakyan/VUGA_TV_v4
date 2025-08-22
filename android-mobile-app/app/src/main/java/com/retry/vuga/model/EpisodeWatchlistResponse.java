package com.retry.vuga.model;

import com.google.gson.annotations.SerializedName;

public class EpisodeWatchlistResponse {

    @SerializedName("status")
    private boolean status;

    @SerializedName("is_in_watchlist")
    private boolean isInWatchlist;

    @SerializedName("message")
    private String message;

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public boolean isInWatchlist() {
        return isInWatchlist;
    }

    public void setInWatchlist(boolean inWatchlist) {
        isInWatchlist = inWatchlist;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}