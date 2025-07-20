package com.retry.vuga.model;

import com.retry.vuga.utils.Const;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class Downloads {
    private String title;
    List<Downloads> episodeList;
    boolean isSeasonTitle;
    private String contentImage;
    private int type;
    private int seasonCount;
    private int episodeCount;
    private String episodeName;
    private int contentId;
    private String episodeImage;
    private ContentDetail.SourceItem sourceItem;
    private String quality;

    private String path;
    private String fileName;
    private String url;
    private int downloadStatus; //0=notDownloaded, 1=started/resumed,2=paused,3=error,4=completed
    private int progress;
    private int playProgress;
    private int id;
    private String duration;
    private String size;

    public Downloads(int progress, int id) {
        this.id = id;
        this.progress = progress;

    }

    public Downloads(int seasonCount, boolean isSeasonTitle) {
        this.seasonCount = seasonCount;
        this.isSeasonTitle = isSeasonTitle;

    }

    public Downloads(HashMap<String, Object> map) {

        title = map.containsKey(Const.DataKey.NAME) ? (String) map.get(Const.DataKey.NAME) : "";
        contentImage = map.containsKey(Const.DataKey.IMAGE) ? (String) map.get(Const.DataKey.IMAGE) : "";
        episodeName = map.containsKey(Const.DataKey.episode_name) ? (String) map.get(Const.DataKey.episode_name) : "";
        type = map.containsKey(Const.DataKey.CONTENT_TYPE) ? (int) map.get(Const.DataKey.CONTENT_TYPE) : 0;
        contentId = map.containsKey(Const.DataKey.CONTENT_ID) ? (int) map.get(Const.DataKey.CONTENT_ID) : 0;
        seasonCount = map.containsKey(Const.DataKey.SEASON_COUNT) ? (int) map.get(Const.DataKey.SEASON_COUNT) : 0;
        episodeCount = map.containsKey(Const.DataKey.EPISODE_COUNT) ? (int) map.get(Const.DataKey.EPISODE_COUNT) : 0;
        duration = map.containsKey(Const.DataKey.content_duration) ? (String) map.get(Const.DataKey.content_duration) : "";
        episodeImage = map.containsKey(Const.DataKey.episode_image) ? (String) map.get(Const.DataKey.episode_image) : "";

    }


    public boolean isSeasonTitle() {
        return isSeasonTitle;
    }

    public void setSeasonTitle(boolean seasonTitle) {
        isSeasonTitle = seasonTitle;
    }

    public int getContentId() {
        return contentId;
    }

    public void setContentId(int contentId) {
        this.contentId = contentId;
    }

    public List<Downloads> getEpisodeList() {
        return episodeList == null ? new ArrayList<>() : episodeList;
    }

    public void setEpisodeList(List<Downloads> episodeList) {
        this.episodeList = episodeList;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getEpisodeImage() {
        return episodeImage;
    }

    public void setEpisodeImage(String episodeImage) {
        this.episodeImage = episodeImage;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getEpisodeName() {
        return episodeName == null ? "" : episodeName;
    }

    public void setEpisodeName(String episodeName) {
        this.episodeName = episodeName;
    }


    public int getDownloadStatus() {
        return downloadStatus == 0 ? Const.DownloadStatus.QUEUED : downloadStatus;
    }

    public void setDownloadStatus(int downloadStatus) {    // Queued,Paused
        this.downloadStatus = downloadStatus;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getSeasonCount() {
        return seasonCount;
    }

    public void setSeasonCount(int seasonCount) {
        this.seasonCount = seasonCount;
    }

    public int getEpisodeCount() {
        return episodeCount;
    }

    public void setEpisodeCount(int episodeCount) {
        this.episodeCount = episodeCount;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getTitle() {
        return title == null ? "" : title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getContentImage() {
        return contentImage == null ? "" : contentImage;
    }

    public void setContentImage(String contentImage) {
        this.contentImage = contentImage;
    }

    public String getQuality() {
        return quality == null ? "" : quality;
    }

    public void setQuality(String quality) {
        this.quality = quality;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Downloads)) return false;
        Downloads downloads = (Downloads) o;
        return Objects.equals(getId(), downloads.getId());

    }

    public int getPlayProgress() {
        return playProgress;
    }

    public void setPlayProgress(int playProgress) {
        this.playProgress = playProgress;
    }

    public ContentDetail.SourceItem getSourceItem() {
        return sourceItem;
    }

    public void setSourceItem(ContentDetail.SourceItem sourceItem) {
        this.sourceItem = sourceItem;
    }
}
