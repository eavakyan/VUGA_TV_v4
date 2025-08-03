package com.retry.vuga.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class AgeRatingResponse {
    @SerializedName("status")
    private boolean status;
    
    @SerializedName("message")
    private String message;
    
    @SerializedName("age_ratings")
    private List<AgeRating> ageRatings;
    
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
    
    public List<AgeRating> getAgeRatings() {
        return ageRatings;
    }
    
    public void setAgeRatings(List<AgeRating> ageRatings) {
        this.ageRatings = ageRatings;
    }
}