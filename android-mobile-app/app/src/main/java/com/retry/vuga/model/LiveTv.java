package com.retry.vuga.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class LiveTv implements Serializable {
    
    @SerializedName("status")
    private boolean status;
    
    @SerializedName("data")
    private List<CategoryItem> data;
    
    // Getters and Setters
    public boolean isStatus() {
        return status;
    }
    
    public void setStatus(boolean status) {
        this.status = status;
    }
    
    public List<CategoryItem> getData() {
        return data;
    }
    
    public void setData(List<CategoryItem> data) {
        this.data = data;
    }
    
    // Inner class for Category
    public static class CategoryItem implements Serializable {
        
        @SerializedName("category_id")
        private int categoryId;
        
        @SerializedName("category_name")
        private String categoryName;
        
        @SerializedName("channels")
        private List<TvChannelItem> channels;
        
        // Getters and Setters
        public int getCategoryId() {
            return categoryId;
        }
        
        public void setCategoryId(int categoryId) {
            this.categoryId = categoryId;
        }
        
        public String getCategoryName() {
            return categoryName;
        }
        
        public void setCategoryName(String categoryName) {
            this.categoryName = categoryName;
        }
        
        public List<TvChannelItem> getChannels() {
            return channels;
        }
        
        public void setChannels(List<TvChannelItem> channels) {
            this.channels = channels;
        }
        
        // Inner class for TV Channel
        public static class TvChannelItem implements Serializable {
            
            @SerializedName("id")
            private int id;
            
            @SerializedName("channel_title")
            private String channelTitle;
            
            @SerializedName("channel_thumbnail")
            private String channelThumbnail;
            
            @SerializedName("stream_url")
            private String streamUrl;
            
            @SerializedName("access_type")
            private String accessType;
            
            @SerializedName("is_premium")
            private boolean isPremium;
            
            @SerializedName("description")
            private String description;
            
            // Getters and Setters
            public int getId() {
                return id;
            }
            
            public void setId(int id) {
                this.id = id;
            }
            
            public String getChannelTitle() {
                return channelTitle;
            }
            
            public void setChannelTitle(String channelTitle) {
                this.channelTitle = channelTitle;
            }
            
            public String getChannelThumbnail() {
                return channelThumbnail;
            }
            
            public void setChannelThumbnail(String channelThumbnail) {
                this.channelThumbnail = channelThumbnail;
            }
            
            public String getStreamUrl() {
                return streamUrl;
            }
            
            public void setStreamUrl(String streamUrl) {
                this.streamUrl = streamUrl;
            }
            
            public String getAccessType() {
                return accessType;
            }
            
            public void setAccessType(String accessType) {
                this.accessType = accessType;
            }
            
            public boolean isPremium() {
                return isPremium;
            }
            
            public void setPremium(boolean premium) {
                isPremium = premium;
            }
            
            public String getDescription() {
                return description;
            }
            
            public void setDescription(String description) {
                this.description = description;
            }
        }
    }
}