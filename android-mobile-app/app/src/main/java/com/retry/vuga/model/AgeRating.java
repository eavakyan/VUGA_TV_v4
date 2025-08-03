package com.retry.vuga.model;

import com.google.gson.annotations.SerializedName;

public class AgeRating {
    @SerializedName("age_limit_id")
    private int id;
    
    @SerializedName("name")
    private String name;
    
    @SerializedName("min_age")
    private int minAge;
    
    @SerializedName("max_age")
    private Integer maxAge;
    
    @SerializedName("description")
    private String description;
    
    @SerializedName("code")
    private String code;
    
    @SerializedName("display_color")
    private String displayColor;
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public int getMinAge() {
        return minAge;
    }
    
    public void setMinAge(int minAge) {
        this.minAge = minAge;
    }
    
    public Integer getMaxAge() {
        return maxAge;
    }
    
    public void setMaxAge(Integer maxAge) {
        this.maxAge = maxAge;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public String getDisplayColor() {
        if (displayColor != null) {
            return displayColor;
        }
        
        // Generate color based on age group code
        switch (code != null ? code : "") {
            case "AG_0_6":
                return "#4CAF50"; // Green
            case "AG_7_12":
                return "#8BC34A"; // Light Green
            case "AG_13_16":
                return "#FF9800"; // Orange
            case "AG_17_18":
                return "#F44336"; // Red
            case "AG_18_PLUS":
                return "#9C27B0"; // Purple
            default:
                return "#757575"; // Gray
        }
    }
    
    public void setDisplayColor(String displayColor) {
        this.displayColor = displayColor;
    }
    
    public boolean isKidsFriendly() {
        // Kids profiles can access content for ages 12 and under
        return maxAge != null && maxAge <= 12;
    }
}