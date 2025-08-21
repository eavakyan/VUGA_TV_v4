package com.retry.vuga.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class ContentDetail {

    @SerializedName("data")
    private DataItem dataItem;

    @SerializedName("message")
    private String message;

    @SerializedName("status")
    private boolean status;

    public DataItem getData() {
        return dataItem;
    }

    public void setData(DataItem dataItem) {
        this.dataItem = dataItem;
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

    public static class DataItem {

        @SerializedName("content_id")
        private int id;

        @SerializedName("title")
        private String title;

        @SerializedName("description")
        private String description;

        @SerializedName("type")
        private int type;

        @SerializedName("duration")
        private String duration;

        @SerializedName("release_year")
        private int releaseYear;

        @SerializedName("ratings")
        private double ratings;

        @SerializedName("language_id")
        private int language_id;

        @SerializedName("download_link")
        private String downloadLink;

        @SerializedName("trailer_url")
        private String trailerUrl;

        @SerializedName("trailer_youtube_id")
        private String trailerYoutubeId;

        @SerializedName("trailers")
        private List<Trailer> trailers;

        @SerializedName("vertical_poster")
        private String verticalPoster;

        @SerializedName("horizontal_poster")
        private String horizontalPoster;

        @SerializedName("genre_ids")
        private String genreIds;

        @SerializedName("is_featured")
        private int is_featured;

        @SerializedName("total_download")
        private int totalDownload;

        @SerializedName("total_share")
        private int totalShare;

        @SerializedName("total_view")
        private int totalView;

        @SerializedName("actor_ids")
        private String actor_ids;

        @SerializedName("is_watchlist")
        private boolean is_watchlist;

        @SerializedName("contentCast")
        private List<CastItem> moviecast;

        @SerializedName("content_sources")
        private List<SourceItem> content_sources;

        @SerializedName("seasons")
        private List<ContentDetail.SeasonItem> seasons;

        @SerializedName("more_like_this")
        private List<ContentDetail.DataItem> moreLikeThis;
        
        @SerializedName("age_ratings")
        private List<AgeRating> ageRatings;
        
        @SerializedName("user_rating")
        private Double userRating;


//-------------------------------------------

        @SerializedName("content_subtitles")
        private List<SubtitlesItem> subtitles;


        private String genreString;
        private List<String> genreList;


        public List<String> getGenreList() {
            return genreList == null ? new ArrayList<>() : genreList;
        }

        public void setGenreList(List<String> genreList) {
            this.genreList = genreList;
        }

        public List<SubtitlesItem> getSubtitles() {
            return subtitles == null ? new ArrayList<>() : subtitles;
        }


        public String getGenreString() {
            return genreString == null ? "" : genreString;
        }

        public void setGenreString(String genreString) {
            this.genreString = genreString;
        }

        public void setSubtitles(List<SubtitlesItem> subtitles) {
            this.subtitles = subtitles;
        }


        public int getId() {
            return id;
        }


        public int getReleaseYear() {
            return releaseYear;
        }

        public void setReleaseYear(int releaseYear) {
            this.releaseYear = releaseYear;
        }

        public boolean getIs_watchlist() {
            return is_watchlist;
        }

        public void setIs_watchlist(boolean is_watchlist) {
            this.is_watchlist = is_watchlist;
        }

        public String getHorizontalPoster() {
            return horizontalPoster == null ? "" : horizontalPoster;
        }

        public void setHorizontalPoster(String horizontalPoster) {
            this.horizontalPoster = horizontalPoster;
        }

        public String getDescription() {
            return description == null ? "" : description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public List<ContentDetail.DataItem> getMoreLikeThis() {
            return moreLikeThis == null ? new ArrayList<>() : moreLikeThis;
        }

        public void setMoreLikeThis(List<ContentDetail.DataItem> moreLikeThis) {
            this.moreLikeThis = moreLikeThis;
        }

        public List<SourceItem> getContent_sources() {
            return content_sources == null ? new ArrayList<>() : content_sources;
        }

        public void setContent_sources(List<SourceItem> content_sources) {
            this.content_sources = content_sources;
        }

        public String getVerticalPoster() {
            return verticalPoster == null ? "" : verticalPoster;
        }

        public void setVerticalPoster(String verticalPoster) {
            this.verticalPoster = verticalPoster;
        }

        public List<CastItem> getCast() {
            return moviecast == null ? new ArrayList<>() : moviecast;
        }

        public void setMoviecast(List<CastItem> moviecast) {
            this.moviecast = moviecast;
        }

        public String getGenreIds() {
            return genreIds == null ? "" : genreIds;
        }


        public void setGenreIds(String genreIds) {
            this.genreIds = genreIds;
        }

        public String getTrailerUrl() {
            return trailerUrl == null ? "" : trailerUrl;
        }

        public void setTrailerUrl(String trailerUrl) {
            this.trailerUrl = trailerUrl;
        }

        public String getTrailerYoutubeId() {
            return trailerYoutubeId == null ? "" : trailerYoutubeId;
        }

        public void setTrailerYoutubeId(String trailerYoutubeId) {
            this.trailerYoutubeId = trailerYoutubeId;
        }

        public List<Trailer> getTrailers() {
            return trailers == null ? new ArrayList<>() : trailers;
        }

        public void setTrailers(List<Trailer> trailers) {
            this.trailers = trailers;
        }

        // Get primary trailer from the trailers list
        public Trailer getPrimaryTrailer() {
            List<Trailer> trailerList = getTrailers();
            for (Trailer trailer : trailerList) {
                if (trailer.isPrimary()) {
                    return trailer;
                }
            }
            // Return first trailer if no primary trailer found
            return trailerList.isEmpty() ? null : trailerList.get(0);
        }

        // Get all non-primary trailers
        public List<Trailer> getAdditionalTrailers() {
            List<Trailer> trailerList = getTrailers();
            List<Trailer> additionalTrailers = new ArrayList<>();
            for (Trailer trailer : trailerList) {
                if (!trailer.isPrimary()) {
                    additionalTrailers.add(trailer);
                }
            }
            return additionalTrailers;
        }

        // Backward compatibility: if trailerUrl is empty, get from primary trailer
        public String getEffectiveTrailerUrl() {
            if (trailerUrl != null && !trailerUrl.isEmpty()) {
                return trailerUrl;
            }
            Trailer primaryTrailer = getPrimaryTrailer();
            return primaryTrailer != null ? primaryTrailer.getTrailerUrl() : "";
        }

        // Backward compatibility: if trailerYoutubeId is empty, get from primary trailer
        public String getEffectiveTrailerYoutubeId() {
            if (trailerYoutubeId != null && !trailerYoutubeId.isEmpty()) {
                return trailerYoutubeId;
            }
            Trailer primaryTrailer = getPrimaryTrailer();
            return primaryTrailer != null ? primaryTrailer.getYoutubeId() : "";
        }

        public String getTitle() {
            return title == null ? "" : title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDuration() {
            return duration == null ? "" : duration;
        }
        
        public String getFormattedDuration() {
            if (duration == null || duration.isEmpty()) {
                return "";
            }
            
            try {
                // Parse duration as integer (seconds from database)
                int totalSeconds = Integer.parseInt(duration);
                
                // Convert seconds to minutes
                int totalMinutes = totalSeconds / 60;
                
                if (totalMinutes < 60) {
                    // Under 1 hour - show only minutes
                    return totalMinutes + " min";
                } else {
                    // 1 hour or more - show hours and minutes
                    int hours = totalMinutes / 60;
                    int minutes = totalMinutes % 60;
                    
                    if (minutes == 0) {
                        return hours + " hr";
                    } else {
                        return hours + " hr " + minutes + " min";
                    }
                }
            } catch (NumberFormatException e) {
                // If duration is not a number, return as is
                return duration;
            }
        }

        public void setDuration(String duration) {
            this.duration = duration;
        }


        public int getTotalDownload() {
            return totalDownload;
        }

        public void setTotalDownload(int totalDownload) {
            this.totalDownload = totalDownload;
        }

        public String getDownloadLink() {
            return downloadLink == null ? "" : downloadLink;
        }

        public void setDownloadLink(String downloadLink) {
            this.downloadLink = downloadLink;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public int getTotalShare() {
            return totalShare;
        }

        public void setTotalShare(int totalShare) {
            this.totalShare = totalShare;
        }

        public int getTotalView() {
            return totalView;
        }

        public void setTotalView(int totalView) {
            this.totalView = totalView;
        }

        public double getRatings() {
            return ratings;
        }

        public void setRatings(double ratings) {
            this.ratings = ratings;
        }

        public List<ContentDetail.SeasonItem> getSeasons() {
            return seasons == null ? new ArrayList<>() : seasons;
        }

        public void setSeasons(List<ContentDetail.SeasonItem> seasons) {
            this.seasons = seasons;
        }

        public void setId(int id) {
            this.id = id;
        }
        
        public List<AgeRating> getAgeRatings() {
            return ageRatings == null ? new ArrayList<>() : ageRatings;
        }
        
        public void setAgeRatings(List<AgeRating> ageRatings) {
            this.ageRatings = ageRatings;
        }
        
        public String getAgeRatingCode() {
            if (ageRatings != null && !ageRatings.isEmpty()) {
                return ageRatings.get(0).getCode();
            }
            return "NR";
        }
        
        public String getAgeRatingColor() {
            if (ageRatings != null && !ageRatings.isEmpty()) {
                return ageRatings.get(0).getDisplayColor();
            }
            return "#666666";
        }
        
        public int getMinimumAge() {
            if (ageRatings != null && !ageRatings.isEmpty()) {
                return ageRatings.get(0).getMinAge();
            }
            return 0;
        }
        
        public boolean isAppropriateFor(Profile profile) {
            if (profile == null) {
                return true;
            }
            
            if (profile.isEffectiveKidsProfile()) {
                return ageRatings != null && ageRatings.stream().anyMatch(AgeRating::isKidsFriendly);
            }
            
            if (profile.getAge() != null) {
                return getMinimumAge() <= profile.getAge();
            }
            
            return true;
        }
        
        public Double getUserRating() {
            return userRating;
        }
        
        public void setUserRating(Double userRating) {
            this.userRating = userRating;
        }


    }

    public static class SourceItem {


        public int downloadStatus;


        public int progress;
        public int playProgress;
        public long time = 0;


        @SerializedName("content_source_id")
        private int id;

        @SerializedName("content_id")
        private int content_id;

        @SerializedName("title")
        private String title;

        @SerializedName("quality")
        private String quality;

        @SerializedName("size")
        private String size;

        @SerializedName("is_download")
        private int is_download;

        @SerializedName("access_type")
        private int access_type;

        @SerializedName("type")
        private int type; //1=youtube , 2=m3u8,7=file

        @SerializedName("source")
        private String source;

        @SerializedName("episode_id") // in series-> episode-> source item
        private int episodeId;

        @SerializedName("media") // in series-> episode-> source item
        private MediaItem mediaItem;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getContent_id() {
            return content_id;
        }

        public void setContent_id(int content_id) {
            this.content_id = content_id;
        }

        public int getProgress() {
            return progress;
        }

        public void setProgress(int progress) {
            this.progress = progress;
        }

        public int getDownloadStatus() {
            return downloadStatus;
        }

        public void setDownloadStatus(int downloadStatus) {
            this.downloadStatus = downloadStatus;
        }

        public MediaItem getMediaItem() {
            return mediaItem;
        }

        public void setMediaItem(MediaItem mediaItem) {
            this.mediaItem = mediaItem;
        }

        public int getEpisodeId() {
            return episodeId;
        }

        public void setEpisodeId(int episodeId) {
            this.episodeId = episodeId;
        }

        public String getSize() {
            return size == null ? "" : size;
        }

        public void setSize(String size) {
            this.size = size;
        }

        public int getIs_download() {
            return is_download;
        }

        public void setIs_download(int is_download) {
            this.is_download = is_download;
        }

        public int getAccess_type() {
            return access_type;
        }

        public void setAccess_type(int access_type) {
            this.access_type = access_type;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public String getQuality() {
            return quality == null ? "" : quality;
        }

        public void setQuality(String quality) {
            this.quality = quality;
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

        public int getPlayProgress() {
            return playProgress;
        }

        public void setPlayProgress(int playProgress) {
            this.playProgress = playProgress;
        }


        public static class MediaItem {


            @SerializedName("media_gallery_id")
            private int id;

            @SerializedName("file")
            private String file;

            @SerializedName("title")
            private String title;

            public int getId() {
                return id;
            }

            public void setId(int id) {
                this.id = id;
            }

            public String getFile() {
                return file == null ? " " : file;
            }

            public void setFile(String file) {
                this.file = file;
            }

            public String getTitle() {
                return title == null ? " " : title;
            }

            public void setTitle(String title) {
                this.title = title;
            }
        }

    }

    public static class SubtitlesItem {

        @SerializedName("subtitle_id")
        private int id;

        @SerializedName("episode_id")
        private int episode_id;

        @SerializedName("content_id")
        private int content_id;

        @SerializedName("language_id")
        private int language_id;

        @SerializedName("file")
        private String subtitleFile;

        public int getId() {
            return id;
        }

        public int getEpisode_id() {
            return episode_id;
        }

        public int getLanguage_id() {
            return language_id;
        }

        public String getSubtitleFile() {
            return subtitleFile;
        }

        public void setSubtitleFile(String subtitleFile) {
            this.subtitleFile = subtitleFile;
        }
    }

    public static class SeasonItem {

        @SerializedName("season_id")
        private int id;

        @SerializedName("content_id")
        private int content_id;

        @SerializedName("title")
        private String title;

        @SerializedName("trailer_url")
        private String trailerUrl;


        @SerializedName("episodes")
        private List<EpisodesItem> episodes;


        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getContent_id() {
            return content_id;
        }

        public void setContent_id(int content_id) {
            this.content_id = content_id;
        }

        public String getTrailerUrl() {
            return trailerUrl == null ? "" : trailerUrl;
        }

        public void setTrailerUrl(String trailerUrl) {
            this.trailerUrl = trailerUrl;
        }

        public String getTitle() {
            return title == null ? "" : title;
        }

        public void setTitle(String title) {
            this.title = title;
        }


        public List<EpisodesItem> getEpisodes() {
            return episodes == null ? new ArrayList<>() : episodes;
        }

        public void setEpisodes(List<EpisodesItem> episodes) {
            this.episodes = episodes;
        }

        public static class EpisodesItem {

            @SerializedName("episode_id")
            private int id;

            @SerializedName("season_id")
            private int seasonId;

            @SerializedName("number")
            private int number;

            @SerializedName("thumbnail")
            private String thumbnail;

            @SerializedName("title")
            private String title;

            @SerializedName("description")
            private String description;

            @SerializedName("duration")
            private String duration;

            @SerializedName("access_type")
            private int accessType;

            @SerializedName("total_view")
            private int totalView;

            @SerializedName("total_download")
            private int totalDownload;
            
            @SerializedName("ratings")
            private double ratings;
            
            @SerializedName("user_rating")
            private Double userRating;


            @SerializedName("sources")
            private List<ContentDetail.SourceItem> sources;


            @SerializedName("subtitles")
            private List<ContentDetail.SubtitlesItem> subtitles;


            public int getNumber() {
                return number;
            }

            public void setNumber(int number) {
                this.number = number;
            }

            public int getTotalView() {
                return totalView;
            }

            public void setTotalView(int totalView) {
                this.totalView = totalView;
            }

            public int getTotalDownload() {
                return totalDownload;
            }

            public void setTotalDownload(int totalDownload) {
                this.totalDownload = totalDownload;
            }

            public List<ContentDetail.SubtitlesItem> getSubtitles() {
                return subtitles == null ? new ArrayList<>() : subtitles;
            }

            public void setSubtitles(List<ContentDetail.SubtitlesItem> subtitles) {
                this.subtitles = subtitles;
            }

            public int getAccessType() {
                return accessType;
            }

            public void setAccessType(int accessType) {
                this.accessType = accessType;
            }

            public int getId() {
                return id;
            }

            public void setId(int id) {
                this.id = id;
            }

            public List<ContentDetail.SourceItem> getSources() {
                return sources == null ? new ArrayList<>() : sources;
            }

            public void setSources(List<ContentDetail.SourceItem> sources) {
                this.sources = sources;
            }


            public String getDuration() {
                return duration == null ? "" : duration;
            }
            
            public String getFormattedDuration() {
                if (duration == null || duration.isEmpty()) {
                    return "";
                }
                
                try {
                    // Parse duration as integer (seconds from database)
                    int totalSeconds = Integer.parseInt(duration);
                    
                    // Convert seconds to minutes
                    int totalMinutes = totalSeconds / 60;
                    
                    if (totalMinutes < 60) {
                        // Under 1 hour - show only minutes
                        return totalMinutes + " min";
                    } else {
                        // 1 hour or more - show hours and minutes
                        int hours = totalMinutes / 60;
                        int minutes = totalMinutes % 60;
                        
                        if (minutes == 0) {
                            return hours + " hr";
                        } else {
                            return hours + " hr " + minutes + " min";
                        }
                    }
                } catch (NumberFormatException e) {
                    // If duration is not a number, return as is
                    return duration;
                }
            }

            public void setDuration(String duration) {
                this.duration = duration;
            }

            public String getTitle() {
                return title == null ? "" : title;
            }

            public void setTitle(String title) {
                this.title = title;
            }

            public int getSeasonId() {
                return seasonId;
            }

            public void setSeasonId(int seasonId) {
                this.seasonId = seasonId;
            }

            public String getThumbnail() {
                return thumbnail == null ? "" : thumbnail;
            }

            public void setThumbnail(String thumbnail) {
                this.thumbnail = thumbnail;
            }

            public String getDescription() {
                return description == null ? "" : description;
            }

            public void setDescription(String description) {
                this.description = description;
            }
            
            public double getRatings() {
                return ratings;
            }
            
            public void setRatings(double ratings) {
                this.ratings = ratings;
            }
            
            public Double getUserRating() {
                return userRating;
            }
            
            public void setUserRating(Double userRating) {
                this.userRating = userRating;
            }


        }
    }

    public static class CastItem {

        @SerializedName("content_cast_id")
        private int id;

        @SerializedName("content_id")
        private int content_id;

        @SerializedName("actor_id")
        private int actor_id;

        @SerializedName("character_name")
        private String charactorName;


        @SerializedName("actor")
        private ActorItem actor;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getContent_id() {
            return content_id;
        }

        public void setContent_id(int content_id) {
            this.content_id = content_id;
        }

        public int getActor_id() {
            return actor_id;
        }

        public void setActor_id(int actor_id) {
            this.actor_id = actor_id;
        }

        public String getCharactorName() {
            return charactorName == null ? "" : charactorName;
        }

        public void setCharactorName(String charactorName) {
            this.charactorName = charactorName;
        }

        public ActorItem getActor() {
            return actor == null ? new ActorItem() : actor;
        }

        public void setActor(ActorItem actor) {
            this.actor = actor;
        }
    }

    public static class ActorItem {

        @SerializedName("actor_id")
        private int id;

        @SerializedName("fullname")
        private String name;

        @SerializedName("profile_image")
        private String image;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name == null ? "" : name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getImage() {
            return image == null ? "" : image;
        }

        public void setImage(String image) {
            this.image = image;
        }
    }


}