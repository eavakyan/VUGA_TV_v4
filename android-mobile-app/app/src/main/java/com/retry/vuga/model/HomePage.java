package com.retry.vuga.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

// ContentDetail.DataItem
// ContentDetail.DataItem
// HomePage.DataItem.ContentItem
public class HomePage {
    @SerializedName("status")
    private boolean status;
    @SerializedName("message")
    private String message;

    @SerializedName("featured")
    private List<ContentDetail.DataItem> featured;

    @SerializedName("watchlist")
    private List<ContentDetail.DataItem> watchlist;

    @SerializedName("topContents")
    private List<TopContentItem> topContents;

    @SerializedName("genreContents")
    private List<GenreContents> genreContents;


    public List<ContentDetail.DataItem> getFeatured() {
        return featured;
    }

    public void setFeatured(List<ContentDetail.DataItem> featured) {
        this.featured = featured;
    }

    public List<GenreContents> getGenreContents() {
        return genreContents;
    }

    public void setGenreContents(List<GenreContents> genreContents) {
        this.genreContents = genreContents;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<ContentDetail.DataItem> getWatchlist() {
        return watchlist;
    }

    public List<TopContentItem> getTopContents() {
        return topContents == null ? new ArrayList<>() : topContents;
    }

    public void setTopContents(List<TopContentItem> topContents) {
        this.topContents = topContents;
    }

    public void setWatchlist(List<ContentDetail.DataItem> watchlist) {
        this.watchlist = watchlist;
    }

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }


    public static class TopContentItem {


        @SerializedName("top_content_id")
        private int id;

        @SerializedName("content_index")
        private int content_index;

        @SerializedName("content_id")
        private int content_id;

        @SerializedName("content")
        private ContentDetail.DataItem content;


        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getContent_index() {
            return content_index;
        }

        public void setContent_index(int content_index) {
            this.content_index = content_index;
        }

        public int getContent_id() {
            return content_id;
        }

        public void setContent_id(int content_id) {
            this.content_id = content_id;
        }

        public ContentDetail.DataItem getContent() {
            return content;
        }

        public void setContent(ContentDetail.DataItem content) {
            this.content = content;
        }
    }

    public static class GenreContents {

        @SerializedName("title")
        private String title;
        
        @SerializedName("genre")
        private String genre;

        @SerializedName("genre_id")
        private int id;

        @SerializedName("contents")
        private List<ContentDetail.DataItem> content;

        public String getTitle() {
            return title == null ? "" : title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
        
        public String getGenre() {
            // Return genre if available, otherwise fall back to title
            return genre == null ? getTitle() : genre;
        }
        
        public void setGenre(String genre) {
            this.genre = genre;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public List<ContentDetail.DataItem> getContent() {
            return content == null ? new ArrayList<>() : content;
        }

        public void setContent(List<ContentDetail.DataItem> content) {
            this.content = content;
        }

    }


}