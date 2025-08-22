package com.retry.vuga.model;

import com.google.gson.annotations.SerializedName;

public class UnifiedWatchlistItem {
    
    @SerializedName("id")
    private int id;
    
    @SerializedName("item_type")
    private String itemType; // "content" or "episode"
    
    @SerializedName("content_id")
    private Integer contentId;
    
    @SerializedName("episode_id")
    private Integer episodeId;
    
    @SerializedName("title")
    private String title;
    
    @SerializedName("type")
    private Integer type; // 1=movie, 2=series (for content items)
    
    @SerializedName("thumbnail")
    private String thumbnail;
    
    @SerializedName("poster")
    private String poster;
    
    @SerializedName("horizontal_poster")
    private String horizontalPoster;
    
    @SerializedName("vertical_poster")
    private String verticalPoster;
    
    @SerializedName("ratings")
    private String ratings;
    
    @SerializedName("release_year")
    private String releaseYear;
    
    @SerializedName("genre_ids")
    private String genreIds;
    
    @SerializedName("added_at")
    private String addedAt;
    
    // Episode-specific fields
    @SerializedName("series_title")
    private String seriesTitle;
    
    @SerializedName("season_number")
    private Integer seasonNumber;
    
    @SerializedName("episode_number")
    private Integer episodeNumber;
    
    @SerializedName("episode_thumbnail")
    private String episodeThumbnail;
    
    @SerializedName("duration")
    private String duration;
    
    // Helper method to check if this is an episode
    public boolean isEpisode() {
        return "episode".equals(itemType);
    }
    
    // Helper method to get the best available poster
    public String getBestPoster() {
        if (horizontalPoster != null && !horizontalPoster.isEmpty()) {
            return horizontalPoster;
        }
        if (verticalPoster != null && !verticalPoster.isEmpty()) {
            return verticalPoster;
        }
        if (poster != null && !poster.isEmpty()) {
            return poster;
        }
        if (isEpisode() && episodeThumbnail != null) {
            return episodeThumbnail;
        }
        return thumbnail;
    }
    
    // Getters and setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getItemType() {
        return itemType;
    }
    
    public void setItemType(String itemType) {
        this.itemType = itemType;
    }
    
    public Integer getContentId() {
        return contentId;
    }
    
    public void setContentId(Integer contentId) {
        this.contentId = contentId;
    }
    
    public Integer getEpisodeId() {
        return episodeId;
    }
    
    public void setEpisodeId(Integer episodeId) {
        this.episodeId = episodeId;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public Integer getType() {
        return type;
    }
    
    public void setType(Integer type) {
        this.type = type;
    }
    
    public String getThumbnail() {
        return thumbnail;
    }
    
    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }
    
    public String getPoster() {
        return poster;
    }
    
    public void setPoster(String poster) {
        this.poster = poster;
    }
    
    public String getHorizontalPoster() {
        return horizontalPoster;
    }
    
    public void setHorizontalPoster(String horizontalPoster) {
        this.horizontalPoster = horizontalPoster;
    }
    
    public String getVerticalPoster() {
        return verticalPoster;
    }
    
    public void setVerticalPoster(String verticalPoster) {
        this.verticalPoster = verticalPoster;
    }
    
    public String getRatings() {
        return ratings;
    }
    
    public void setRatings(String ratings) {
        this.ratings = ratings;
    }
    
    public String getReleaseYear() {
        return releaseYear;
    }
    
    public void setReleaseYear(String releaseYear) {
        this.releaseYear = releaseYear;
    }
    
    public String getGenreIds() {
        return genreIds;
    }
    
    public void setGenreIds(String genreIds) {
        this.genreIds = genreIds;
    }
    
    public String getAddedAt() {
        return addedAt;
    }
    
    public void setAddedAt(String addedAt) {
        this.addedAt = addedAt;
    }
    
    public String getSeriesTitle() {
        return seriesTitle;
    }
    
    public void setSeriesTitle(String seriesTitle) {
        this.seriesTitle = seriesTitle;
    }
    
    public Integer getSeasonNumber() {
        return seasonNumber;
    }
    
    public void setSeasonNumber(Integer seasonNumber) {
        this.seasonNumber = seasonNumber;
    }
    
    public Integer getEpisodeNumber() {
        return episodeNumber;
    }
    
    public void setEpisodeNumber(Integer episodeNumber) {
        this.episodeNumber = episodeNumber;
    }
    
    public String getEpisodeThumbnail() {
        return episodeThumbnail;
    }
    
    public void setEpisodeThumbnail(String episodeThumbnail) {
        this.episodeThumbnail = episodeThumbnail;
    }
    
    public String getDuration() {
        return duration;
    }
    
    public void setDuration(String duration) {
        this.duration = duration;
    }
}