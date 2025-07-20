package com.retry.vuga.utils;


import android.content.IntentFilter;

import com.retry.vuga.BuildConfig;

import org.jetbrains.annotations.Nullable;

public class Const {


    public static final String BASE = "https://iosdev.gossip-stone.com/";
    public static final String API_KEY = "jpwc3pny";
    public static final String BASE_URL = BASE + "api/";
    public static final String IMAGE_URL = ""; //BASE + "public/storage/"
    public static final String PREF_NAME = BuildConfig.APPLICATION_ID;
    //do not change "_android"
    public static final String FIREBASE_SUB_TOPIC = "flixy" + "_android";
    public static final int PAGINATION_COUNT = 10;
    public static final int FEATURED_SCROLL = 5 * 1000;
    public static final int PLAYER_SEC = 10;
    public static final int HISTORY_COUNT = 10;
    public static final String TERMS_URL = BASE + "termsOfUse";
    public static final String PRIVACY_URL = BASE + "privacyPolicy";
    public static final IntentFilter DOWNLOAD_RECEIVER = new IntentFilter(DataKey.DOWNLOAD_BROAD_CAST);


    public enum ItemType {
        DOWNLOAD, WATCHLIST, DISCOVER
    }


    public static class DownloadStatus {
        public static final int START = 1;
        public static final int QUEUED = 2;
        public static final int PAUSED = 3;
        public static final int COMPLETED = 4;
        public static final int ERROR = 5;
        public static final int PROGRESSING = 5;
    }

    public static class ApiKey {

        public static final String apikey = "apikey";


        public static final String user_id = "user_id";
        public static final String watchlist_content_ids = "watchlist_content_ids";
        public static final String id = "id";
        public static final String content_id = "content_id";
        public static final String episode_id = "episode_id";
        public static final String start = "start";
        public static final String limit = "limit";
        public static final String genre_id = "genre_id";

        public static final String tv_category_id = "tv_category_id";

        public static final String device_type = "device_type";
        public static final String is_android = "is_android";
        public static final String actor_id = "actor_id";
        public static final String custom_ad_id = "custom_ad_id";
        public static final String metric = "metric";
        public static final String channel_id = "channel_id";
        public static final String fullname = "fullname";
        public static final String device_token = "device_token";
        public static final String identity = "identity";
        public static final String login_type = "login_type";
        public static final String email = "email";
        public static final String keyword = "keyword";
        public static final String language_id = "language_id";
        public static final String type = "type";
        public static final String profile_image = "profile_image";


    }

    public static class DataKey {
        public static final String TOKEN = "token";
        public static final String CUSTOM_ADS = "custom_ads";
        public static final String USER = "user";
        public static final String GENRE_ID = "genreId";
        public static final String CONTENT_ID = "content_id";
        public static final String TRAILER_URL = "trailerUrl";

        public static final String CONTENT_SOURCE = "contentSource";
        public static final String NOTIFICATION = "notification";
        public static final String NOT_NEW_USER = "not_new_user";
        public static final String CAT_ID = "catId";
        public static final String DOWNLOAD_OBJ = "download_obj";
        public static final String CAT_NAME = "catName";
        public static final String APP_SETTINGS = "appSettings";
        public static final String CONTENT_TYPE = "contentType";
        public static final String IS_NOTIFICATION = "is_notification";
        public static final String BRANCH_DATA = "branchData";
        public static final String IS_BRANCH_LINK = "is_branch_link";
        public static final String NAME = "name";
        public static final String IMAGE = "image";
        public static final String episode_name = "episode_name";


        public static final String DOWNLOADS = "downloads";
        public static final String pending_list = "pending_list";
        public static final String LIVE_TV_MODEL = "live_tv_model";
        public static final String SEASON_COUNT = "season_count";
        public static final String EPISODE_COUNT = "episode_count";
        public static final String content_duration = "content_duration";
        public static final String episode_image = "episode_image";
        public static final String SUB_TITLES = "sub_titles";
        public static final String SUBSCRIPTION_DATA = "subscription_data";
        public static final String IS_PREMIUM = "is_premium";
        public static final String IS_DOWNLOAD_PAUSED = "is_download_paused";
        public static final String SUBTITLE_POSITION = "subtitle_position";


        public static final String actor_id = "actor_id";
        public static final String DATA = "data";
        public static final String LANGUAGE = "language";

        public static final String DOWNLOAD_BROAD_CAST = "download_broad_cast";

        public static final String MOVIE_HISTORY = "movie_history";
        public static final String THUMBNAIL = "thumbnail";
        @Nullable
        public static final String PROGRESS = "progress";
        public static final String CONTENT_NAME = "content_name";
    }


}
