package com.retry.vuga.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class LiveTv {

    @SerializedName("data")
    private List<CategoryItem> data;

    @SerializedName("message")
    private String message;

    @SerializedName("status")
    private boolean status;

    public List<CategoryItem> getData() {
        return data;
    }

    public void setData(List<CategoryItem> data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public static class CategoryItem {

        @SerializedName("image")
        private String image;

        @SerializedName("title")
        private String title;

        @SerializedName("id")
        private int id;

        @SerializedName("channels")
        private List<TvChannelItem> channels;

        public String getImage() {
            return image == null ? "" : image;
        }

        public void setImage(String image) {
            this.image = image;
        }

        public String getTitle() {
            return title == null ? "" : title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public List<TvChannelItem> getChannels() {
            return channels;
        }

        public void setChannels(List<TvChannelItem> channels) {
            this.channels = channels;
        }

        public static class TvChannelItem {

            @SerializedName("title")
            private String title;

            @SerializedName("access_type")
            private int accessType;

            @SerializedName("thumbnail")
            private String thumbnail;

            @SerializedName("category_ids")
            private String category_ids;

            @SerializedName("id")
            private String id;

            @SerializedName("type")
            private int type;
            @SerializedName("source")
            private String source;

            public String getCategory_ids() {
                return category_ids == null ? "" : category_ids;
            }

            public void setCategory_ids(String category_ids) {
                this.category_ids = category_ids;
            }

            public int getType() {
                return type;
            }

            public void setType(int type) {
                this.type = type;
            }

            public String getSource() {
                return source == null ? "" : source;
            }

            public void setSource(String source) {
                this.source = source;
            }


            public String getTitle() {
                return title == null ? "" : title;
            }

            public void setTitle(String title) {
                this.title = title;
            }

            public int getAccessType() {
                return accessType;
            }

            public void setAccessType(int accessType) {
                this.accessType = accessType;
            }

            public String getThumbnail() {
                return thumbnail == null ? "" : thumbnail;
            }

            public void setThumbnail(String thumbnail) {
                this.thumbnail = thumbnail;
            }


            public String getId() {
                return id == null ? "" : id;
            }

            public void setId(String id) {
                this.id = id;
            }
        }
    }
}