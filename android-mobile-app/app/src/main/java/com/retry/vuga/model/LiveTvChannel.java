package com.retry.vuga.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class LiveTvChannel implements Serializable {
    
    @SerializedName("id")
    private int id;
    
    @SerializedName("tv_channel_id")  // Old API uses this field name
    private int tvChannelId;
    
    @SerializedName("title")
    private String title;
    
    @SerializedName("stream_url")
    private String streamUrl;
    
    @SerializedName("source")  // Old API uses "source" instead of "stream_url"
    private String source;
    
    @SerializedName("logo_url")
    private String logoUrl;
    
    @SerializedName("thumbnail")
    private String thumbnail;
    
    @SerializedName("category_ids")
    private String categoryIds;
    
    @SerializedName("channel_number")
    private int channelNumber;
    
    @SerializedName("access_type")  // Old API: 1=Free, 2=Premium, 3=Ads
    private int accessType;
    
    @SerializedName("is_premium")
    private boolean isPremium;
    
    @SerializedName("is_free")
    private boolean isFree;
    
    @SerializedName("requires_ads")
    private boolean requiresAds;
    
    @SerializedName("current_program_title")
    private String currentProgramTitle;
    
    @SerializedName("current_program_description")
    private String currentProgramDescription;
    
    @SerializedName("current_program_start")
    private String currentProgramStart;
    
    @SerializedName("current_program_end")
    private String currentProgramEnd;
    
    @SerializedName("next_program_title")
    private String nextProgramTitle;
    
    @SerializedName("next_program_start")
    private String nextProgramStart;
    
    @SerializedName("is_active")
    private boolean isActive;
    
    @SerializedName("quality")
    private String quality;
    
    @SerializedName("description")
    private String description;
    
    // Getters and Setters
    public int getId() {
        // Return tvChannelId if id is 0 (for old API compatibility)
        return id > 0 ? id : tvChannelId;
    }
    
    public void setId(int id) {
        this.id = id;
        this.tvChannelId = id;
    }
    
    public String getTitle() {
        return title != null ? title : "";
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getStreamUrl() {
        // Use source if streamUrl is null (for old API compatibility)
        return streamUrl != null ? streamUrl : source;
    }
    
    public void setStreamUrl(String streamUrl) {
        this.streamUrl = streamUrl;
        this.source = streamUrl;
    }
    
    public String getLogoUrl() {
        return logoUrl;
    }
    
    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }
    
    public String getThumbnail() {
        return thumbnail != null ? thumbnail : logoUrl;
    }
    
    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }
    
    public String getCategoryIds() {
        return categoryIds != null ? categoryIds : "";
    }
    
    public void setCategoryIds(String categoryIds) {
        this.categoryIds = categoryIds;
    }
    
    public int getChannelNumber() {
        return channelNumber;
    }
    
    public void setChannelNumber(int channelNumber) {
        this.channelNumber = channelNumber;
    }
    
    public boolean isPremium() {
        // Check accessType if boolean fields are not set (old API)
        if (accessType > 0) {
            return accessType == 2;
        }
        return isPremium;
    }
    
    public void setPremium(boolean premium) {
        isPremium = premium;
    }
    
    public boolean isFree() {
        // Check accessType if boolean fields are not set (old API)
        if (accessType > 0) {
            return accessType == 1;
        }
        return isFree;
    }
    
    public void setFree(boolean free) {
        isFree = free;
    }
    
    public boolean requiresAds() {
        // Check accessType if boolean fields are not set (old API)
        if (accessType > 0) {
            return accessType == 3;
        }
        return requiresAds;
    }
    
    public void setRequiresAds(boolean requiresAds) {
        this.requiresAds = requiresAds;
    }
    
    public String getCurrentProgramTitle() {
        return currentProgramTitle != null ? currentProgramTitle : "No program info";
    }
    
    public void setCurrentProgramTitle(String currentProgramTitle) {
        this.currentProgramTitle = currentProgramTitle;
    }
    
    public String getCurrentProgramDescription() {
        return currentProgramDescription;
    }
    
    public void setCurrentProgramDescription(String currentProgramDescription) {
        this.currentProgramDescription = currentProgramDescription;
    }
    
    public String getCurrentProgramStart() {
        return currentProgramStart;
    }
    
    public void setCurrentProgramStart(String currentProgramStart) {
        this.currentProgramStart = currentProgramStart;
    }
    
    public String getCurrentProgramEnd() {
        return currentProgramEnd;
    }
    
    public void setCurrentProgramEnd(String currentProgramEnd) {
        this.currentProgramEnd = currentProgramEnd;
    }
    
    public String getNextProgramTitle() {
        return nextProgramTitle;
    }
    
    public void setNextProgramTitle(String nextProgramTitle) {
        this.nextProgramTitle = nextProgramTitle;
    }
    
    public String getNextProgramStart() {
        return nextProgramStart;
    }
    
    public void setNextProgramStart(String nextProgramStart) {
        this.nextProgramStart = nextProgramStart;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
    }
    
    public String getQuality() {
        return quality != null ? quality : "HD";
    }
    
    public void setQuality(String quality) {
        this.quality = quality;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
}