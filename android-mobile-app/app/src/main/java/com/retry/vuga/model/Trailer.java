package com.retry.vuga.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Trailer {

    @SerializedName("content_trailer_id")
    private int id;

    @SerializedName("content_id")
    private int contentId;

    @SerializedName("title")
    private String title;

    @SerializedName("youtube_id")
    private String youtubeId;

    @SerializedName("trailer_url")
    private String trailerUrl;

    @SerializedName("embed_url")
    private String embedUrl;

    @SerializedName("watch_url")
    private String watchUrl;

    @SerializedName("thumbnail_url")
    private String thumbnailUrl;

    @SerializedName("is_primary")
    private boolean isPrimary;

    @SerializedName("sort_order")
    private int sortOrder;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;

    // Constructors
    public Trailer() {
    }

    public Trailer(int id, int contentId, String title, String youtubeId, String trailerUrl, boolean isPrimary, int sortOrder) {
        this.id = id;
        this.contentId = contentId;
        this.title = title;
        this.youtubeId = youtubeId;
        this.trailerUrl = trailerUrl;
        this.isPrimary = isPrimary;
        this.sortOrder = sortOrder;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getContentId() {
        return contentId;
    }

    public void setContentId(int contentId) {
        this.contentId = contentId;
    }

    public String getTitle() {
        return title == null ? "" : title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getYoutubeId() {
        return youtubeId == null ? "" : youtubeId;
    }

    public void setYoutubeId(String youtubeId) {
        this.youtubeId = youtubeId;
    }

    public String getTrailerUrl() {
        return trailerUrl == null ? "" : trailerUrl;
    }

    public void setTrailerUrl(String trailerUrl) {
        this.trailerUrl = trailerUrl;
    }

    public String getEmbedUrl() {
        return embedUrl == null ? "" : embedUrl;
    }

    public void setEmbedUrl(String embedUrl) {
        this.embedUrl = embedUrl;
    }

    public String getWatchUrl() {
        return watchUrl == null ? "" : watchUrl;
    }

    public void setWatchUrl(String watchUrl) {
        this.watchUrl = watchUrl;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl == null ? "" : thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public boolean isPrimary() {
        return isPrimary;
    }

    public void setPrimary(boolean primary) {
        isPrimary = primary;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getCreatedAt() {
        return createdAt == null ? "" : createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt == null ? "" : updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Utility methods
    public String getYoutubeThumbnailUrl() {
        if (youtubeId != null && !youtubeId.isEmpty()) {
            return "https://img.youtube.com/vi/" + youtubeId + "/maxresdefault.jpg";
        }
        return getThumbnailUrl();
    }

    public String getYoutubeEmbedUrl() {
        if (youtubeId != null && !youtubeId.isEmpty()) {
            return "https://www.youtube.com/embed/" + youtubeId;
        }
        return getEmbedUrl();
    }

    public String getYoutubeWatchUrl() {
        if (youtubeId != null && !youtubeId.isEmpty()) {
            return "https://www.youtube.com/watch?v=" + youtubeId;
        }
        return getWatchUrl();
    }

    // For backward compatibility - use this as the main trailer URL
    public String getMainUrl() {
        return getTrailerUrl();
    }

    @Override
    public String toString() {
        return "Trailer{" +
                "id=" + id +
                ", contentId=" + contentId +
                ", title='" + title + '\'' +
                ", youtubeId='" + youtubeId + '\'' +
                ", isPrimary=" + isPrimary +
                ", sortOrder=" + sortOrder +
                '}';
    }

    // Response wrapper for API calls
    public static class TrailerResponse {
        @SerializedName("status")
        private boolean status;

        @SerializedName("message")
        private String message;

        @SerializedName("data")
        private List<Trailer> data;

        public boolean isStatus() {
            return status;
        }

        public void setStatus(boolean status) {
            this.status = status;
        }

        public String getMessage() {
            return message == null ? "" : message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public List<Trailer> getData() {
            return data;
        }

        public void setData(List<Trailer> data) {
            this.data = data;
        }
    }

    // Single trailer response
    public static class SingleTrailerResponse {
        @SerializedName("status")
        private boolean status;

        @SerializedName("message")
        private String message;

        @SerializedName("data")
        private Trailer data;

        public boolean isStatus() {
            return status;
        }

        public void setStatus(boolean status) {
            this.status = status;
        }

        public String getMessage() {
            return message == null ? "" : message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Trailer getData() {
            return data;
        }

        public void setData(Trailer data) {
            this.data = data;
        }
    }
}