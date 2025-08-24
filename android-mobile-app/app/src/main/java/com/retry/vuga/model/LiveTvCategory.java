package com.retry.vuga.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class LiveTvCategory implements Serializable {
    
    @SerializedName("id")
    private int id;
    
    @SerializedName("tv_category_id")  // Old API uses this field name
    private int tvCategoryId;
    
    @SerializedName("name")
    private String name;
    
    @SerializedName("title")  // Old API uses "title" instead of "name"
    private String title;
    
    @SerializedName("slug")
    private String slug;
    
    @SerializedName("icon")
    private String icon;
    
    @SerializedName("order")
    private int order;
    
    @SerializedName("is_active")
    private boolean isActive;
    
    @SerializedName("channel_count")
    private int channelCount;
    
    @SerializedName("channels")  // For old API format
    private List<LiveTvChannel> channels;
    
    // Local property for UI state
    private boolean isSelected = false;
    
    // Constructor
    public LiveTvCategory() {}
    
    public LiveTvCategory(int id, String name) {
        this.id = id;
        this.name = name;
    }
    
    // Getters and Setters
    public int getId() {
        // Return tvCategoryId if id is 0 (for old API compatibility)
        return id > 0 ? id : tvCategoryId;
    }
    
    public void setId(int id) {
        this.id = id;
        this.tvCategoryId = id;
    }
    
    public String getName() {
        // Use title if name is null (for old API compatibility)
        String displayName = name != null ? name : title;
        return displayName != null ? displayName : "";
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getSlug() {
        return slug;
    }
    
    public void setSlug(String slug) {
        this.slug = slug;
    }
    
    public String getIcon() {
        return icon;
    }
    
    public void setIcon(String icon) {
        this.icon = icon;
    }
    
    public int getOrder() {
        return order;
    }
    
    public void setOrder(int order) {
        this.order = order;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
    }
    
    public int getChannelCount() {
        return channelCount;
    }
    
    public void setChannelCount(int channelCount) {
        this.channelCount = channelCount;
    }
    
    public boolean isSelected() {
        return isSelected;
    }
    
    public void setSelected(boolean selected) {
        isSelected = selected;
    }
    
    public List<LiveTvChannel> getChannels() {
        return channels;
    }
    
    public void setChannels(List<LiveTvChannel> channels) {
        this.channels = channels;
    }
    
    // Predefined categories
    public static class PredefinedCategories {
        public static final LiveTvCategory ALL = new LiveTvCategory(0, "All");
        public static final LiveTvCategory FEATURED = new LiveTvCategory(-1, "Featured");
        public static final LiveTvCategory RECENTLY_WATCHED = new LiveTvCategory(-2, "Recently Watched");
    }
}