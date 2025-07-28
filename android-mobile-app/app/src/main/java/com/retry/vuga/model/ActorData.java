package com.retry.vuga.model;

import com.google.gson.annotations.SerializedName;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ActorData {

    @SerializedName("data")
    private Data data;

    @SerializedName("message")
    private String message;

    @SerializedName("status")
    private boolean status;

    public Data getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }

    public boolean isStatus() {
        return status;
    }

    public static class Data {

        @SerializedName("profile_image")
        private String profileImage;

        @SerializedName("updated_at")
        private String updatedAt;

        @SerializedName("dob")
        private String dob;

        @SerializedName("bio")
        private String bio;

        @SerializedName("created_at")
        private String createdAt;

        @SerializedName("actor_id")
        private int id;

        @SerializedName("fullname")
        private String fullname;

        @SerializedName("actorContent")
        private List<ContentDetail.DataItem> actorContent;

        public String getProfileImage() {
            return profileImage == null ? "" : profileImage;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }

        public void setProfileImage(String profileImage) {
            this.profileImage = profileImage;
        }

        public String getDob() {
            try {
                Date date = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).parse(dob);
                if (date != null)
                    return new SimpleDateFormat("dd MMM, yyyy", Locale.ENGLISH).format(date);
                else
                    return dob == null ? "" : dob;
            } catch (ParseException e) {
                return dob == null ? "" : dob;
            }
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public int getId() {
            return id;
        }

        public String getFullname() {
            return fullname;
        }

        public void setDob(String dob) {
            this.dob = dob;
        }

        public String getBio() {
            return bio == null ? "" : bio;
        }

        public void setBio(String bio) {
            this.bio = bio;
        }

        public void setUpdatedAt(String updatedAt) {
            this.updatedAt = updatedAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }

        public void setId(int id) {
            this.id = id;
        }

        public void setFullname(String fullname) {
            this.fullname = fullname;
        }

        public List<ContentDetail.DataItem> getActorContent() {
            return actorContent == null ? new ArrayList<>() : actorContent;
        }

        public void setActorContent(List<ContentDetail.DataItem> actorContent) {
            this.actorContent = actorContent;
        }
    }
}