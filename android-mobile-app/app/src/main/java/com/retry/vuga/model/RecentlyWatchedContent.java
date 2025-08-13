package com.retry.vuga.model;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

public class RecentlyWatchedContent {

    @SerializedName("data")
    private List<DataItem> data;

    @SerializedName("message")
    private String message;

    @SerializedName("status")
    private boolean status;

    public List<DataItem> getData() {
        return data == null ? new ArrayList<>() : data;
    }

    public void setData(List<DataItem> data) {
        this.data = data;
    }

    public String getMessage() {
        return message == null ? "" : message;
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

    public static class DataItem {
        @SerializedName("content_id")
        private int contentId;

        @SerializedName("title")
        private String title;

        @SerializedName("vertical_poster")
        private String verticalPoster;

        @SerializedName("horizontal_poster")
        private String horizontalPoster;

        @SerializedName("duration")
        private int duration; // Duration in seconds

        @SerializedName("release_year")
        private int releaseYear;

        @SerializedName("type")
        private int type;

        @SerializedName("watched_at")
        private String watchedAt;

        @SerializedName("watch_position")
        private int watchPosition;

        @SerializedName("watch_percentage")
        private double watchPercentage;

        // Getters and setters
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

        public String getVerticalPoster() {
            return verticalPoster == null ? "" : verticalPoster;
        }

        public void setVerticalPoster(String verticalPoster) {
            this.verticalPoster = verticalPoster;
        }

        public String getHorizontalPoster() {
            return horizontalPoster == null ? "" : horizontalPoster;
        }

        public void setHorizontalPoster(String horizontalPoster) {
            this.horizontalPoster = horizontalPoster;
        }

        public int getDuration() {
            return duration;
        }

        public void setDuration(int duration) {
            this.duration = duration;
        }

        public int getReleaseYear() {
            return releaseYear;
        }

        public void setReleaseYear(int releaseYear) {
            this.releaseYear = releaseYear;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public String getWatchedAt() {
            return watchedAt == null ? "" : watchedAt;
        }

        public void setWatchedAt(String watchedAt) {
            this.watchedAt = watchedAt;
        }

        public int getWatchPosition() {
            return watchPosition;
        }

        public void setWatchPosition(int watchPosition) {
            this.watchPosition = watchPosition;
        }

        public double getWatchPercentage() {
            return watchPercentage;
        }

        public void setWatchPercentage(double watchPercentage) {
            this.watchPercentage = watchPercentage;
        }

        /**
         * Formats duration from seconds to "X hr Y min" format
         * @return Formatted duration string
         */
        public String getFormattedDuration() {
            if (duration <= 0) {
                return "";
            }

            int hours = duration / 3600;
            int minutes = (duration % 3600) / 60;

            if (hours > 0) {
                if (minutes > 0) {
                    return hours + " hr " + minutes + " min";
                } else {
                    return hours + " hr";
                }
            } else {
                return minutes + " min";
            }
        }

        /**
         * Gets year as string without thousands separator
         * @return Year as string
         */
        public String getYearString() {
            return String.valueOf(releaseYear);
        }
    }
}