package com.vugaenterprises.androidtv.data.api;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000d\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\t\n\u0000\n\u0002\u0010\u000e\n\u0002\b\b\n\u0002\u0018\u0002\n\u0002\b\u000e\bf\u0018\u00002\u00020\u0001J\u0018\u0010\u0002\u001a\u00020\u00032\b\b\u0001\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u0018\u0010\u0007\u001a\u00020\b2\b\b\u0001\u0010\t\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u0018\u0010\n\u001a\u00020\u000b2\b\b\u0001\u0010\f\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u000e\u0010\r\u001a\u00020\u000eH\u00a7@\u00a2\u0006\u0002\u0010\u000fJ,\u0010\u0010\u001a\u00020\u00112\b\b\u0001\u0010\u0012\u001a\u00020\u00052\b\b\u0001\u0010\u0013\u001a\u00020\u00052\b\b\u0001\u0010\u0014\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0015J\"\u0010\u0016\u001a\u00020\u00172\b\b\u0001\u0010\u0004\u001a\u00020\u00052\b\b\u0001\u0010\u0018\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0019J\u0018\u0010\u001a\u001a\u00020\u001b2\b\b\u0001\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J6\u0010\u001c\u001a\u00020\u001d2\b\b\u0001\u0010\u001e\u001a\u00020\u00052\b\b\u0001\u0010\u0004\u001a\u00020\u00052\b\b\u0001\u0010\u0012\u001a\u00020\u00052\b\b\u0001\u0010\u0013\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u001fJ\"\u0010 \u001a\u00020\u00032\b\b\u0001\u0010!\u001a\u00020\"2\b\b\u0001\u0010#\u001a\u00020$H\u00a7@\u00a2\u0006\u0002\u0010%J\u0018\u0010&\u001a\u00020\u00032\b\b\u0001\u0010\u0018\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u0018\u0010\'\u001a\u00020\u00032\b\b\u0001\u0010\u0018\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u0018\u0010(\u001a\u00020\u00032\b\b\u0001\u0010\u0018\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u0018\u0010)\u001a\u00020\u00032\b\b\u0001\u0010*\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u0018\u0010+\u001a\u00020\u00032\b\b\u0001\u0010*\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u0018\u0010,\u001a\u00020-2\b\b\u0001\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J@\u0010.\u001a\u00020-2\b\b\u0001\u0010/\u001a\u00020$2\b\b\u0001\u00100\u001a\u00020$2\b\b\u0001\u00101\u001a\u00020$2\b\b\u0001\u00102\u001a\u00020$2\b\b\u0003\u00103\u001a\u00020$H\u00a7@\u00a2\u0006\u0002\u00104J,\u00105\u001a\u00020\u001d2\b\b\u0001\u00106\u001a\u00020$2\b\b\u0003\u0010\u0012\u001a\u00020\u00052\b\b\u0003\u0010\u0013\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u00107J\u000e\u00108\u001a\u00020$H\u00a7@\u00a2\u0006\u0002\u0010\u000fJ,\u00109\u001a\u00020-2\b\b\u0001\u0010\u0004\u001a\u00020\u00052\b\b\u0001\u0010/\u001a\u00020$2\b\b\u0001\u00100\u001a\u00020$H\u00a7@\u00a2\u0006\u0002\u0010:\u00f8\u0001\u0000\u0082\u0002\u0006\n\u0004\b!0\u0001\u00a8\u0006;\u00c0\u0006\u0001"}, d2 = {"Lcom/vugaenterprises/androidtv/data/api/ApiService;", "", "deleteAccount", "Lcom/vugaenterprises/androidtv/data/model/RestResponse;", "userId", "", "(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "fetchActorDetails", "Lcom/vugaenterprises/androidtv/data/model/ActorDataResponse;", "actorId", "fetchCustomAds", "Lcom/vugaenterprises/androidtv/data/model/CustomAdsResponse;", "deviceType", "getAppSettings", "Lcom/vugaenterprises/androidtv/data/model/AppSettingResponse;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getContentByGenre", "Lcom/vugaenterprises/androidtv/data/model/ContentByGenreResponse;", "start", "limit", "genreId", "(IIILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getContentDetail", "Lcom/vugaenterprises/androidtv/data/model/ContentDetailResponse;", "contentId", "(IILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getHomeData", "Lcom/vugaenterprises/androidtv/data/model/HomePageResponse;", "getWatchList", "Lcom/vugaenterprises/androidtv/data/model/AllContentResponse;", "type", "(IIIILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "increaseAdMetric", "customAdId", "", "metric", "", "(JLjava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "increaseContentDownload", "increaseContentShare", "increaseContentView", "increaseEpisodeDownload", "episodeId", "increaseEpisodeView", "logOutUser", "Lcom/vugaenterprises/androidtv/data/model/UserRegistrationResponse;", "registerUser", "fullname", "email", "identity", "loginType", "deviceToken", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "searchContent", "keyword", "(Ljava/lang/String;IILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "testConnection", "updateProfile", "(ILjava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_release"})
public abstract interface ApiService {
    
    @retrofit2.http.GET(value = ".")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object testConnection(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.String> $completion);
    
    @retrofit2.http.POST(value = "fetchSettings")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getAppSettings(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.vugaenterprises.androidtv.data.model.AppSettingResponse> $completion);
    
    @retrofit2.http.FormUrlEncoded()
    @retrofit2.http.POST(value = "userRegistration")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object registerUser(@retrofit2.http.Field(value = "fullname")
    @org.jetbrains.annotations.NotNull()
    java.lang.String fullname, @retrofit2.http.Field(value = "email")
    @org.jetbrains.annotations.NotNull()
    java.lang.String email, @retrofit2.http.Field(value = "identity")
    @org.jetbrains.annotations.NotNull()
    java.lang.String identity, @retrofit2.http.Field(value = "login_type")
    @org.jetbrains.annotations.NotNull()
    java.lang.String loginType, @retrofit2.http.Field(value = "device_token")
    @org.jetbrains.annotations.NotNull()
    java.lang.String deviceToken, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.vugaenterprises.androidtv.data.model.UserRegistrationResponse> $completion);
    
    @retrofit2.http.FormUrlEncoded()
    @retrofit2.http.POST(value = "updateProfile")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object updateProfile(@retrofit2.http.Field(value = "user_id")
    int userId, @retrofit2.http.Field(value = "fullname")
    @org.jetbrains.annotations.NotNull()
    java.lang.String fullname, @retrofit2.http.Field(value = "email")
    @org.jetbrains.annotations.NotNull()
    java.lang.String email, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.vugaenterprises.androidtv.data.model.UserRegistrationResponse> $completion);
    
    @retrofit2.http.FormUrlEncoded()
    @retrofit2.http.POST(value = "logOut")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object logOutUser(@retrofit2.http.Field(value = "user_id")
    int userId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.vugaenterprises.androidtv.data.model.UserRegistrationResponse> $completion);
    
    @retrofit2.http.FormUrlEncoded()
    @retrofit2.http.POST(value = "deleteMyAccount")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteAccount(@retrofit2.http.Field(value = "user_id")
    int userId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.vugaenterprises.androidtv.data.model.RestResponse> $completion);
    
    @retrofit2.http.FormUrlEncoded()
    @retrofit2.http.POST(value = "fetchHomePageData")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getHomeData(@retrofit2.http.Field(value = "user_id")
    int userId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.vugaenterprises.androidtv.data.model.HomePageResponse> $completion);
    
    @retrofit2.http.FormUrlEncoded()
    @retrofit2.http.POST(value = "fetchContentDetails")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getContentDetail(@retrofit2.http.Field(value = "user_id")
    int userId, @retrofit2.http.Field(value = "content_id")
    int contentId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.vugaenterprises.androidtv.data.model.ContentDetailResponse> $completion);
    
    @retrofit2.http.FormUrlEncoded()
    @retrofit2.http.POST(value = "fetchContentsByGenre")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getContentByGenre(@retrofit2.http.Field(value = "start")
    int start, @retrofit2.http.Field(value = "limit")
    int limit, @retrofit2.http.Field(value = "genre_id")
    int genreId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.vugaenterprises.androidtv.data.model.ContentByGenreResponse> $completion);
    
    @retrofit2.http.FormUrlEncoded()
    @retrofit2.http.POST(value = "fetchWatchList")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getWatchList(@retrofit2.http.Field(value = "type")
    int type, @retrofit2.http.Field(value = "user_id")
    int userId, @retrofit2.http.Field(value = "start")
    int start, @retrofit2.http.Field(value = "limit")
    int limit, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.vugaenterprises.androidtv.data.model.AllContentResponse> $completion);
    
    @retrofit2.http.FormUrlEncoded()
    @retrofit2.http.POST(value = "searchContent")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object searchContent(@retrofit2.http.Field(value = "keyword")
    @org.jetbrains.annotations.NotNull()
    java.lang.String keyword, @retrofit2.http.Field(value = "start")
    int start, @retrofit2.http.Field(value = "limit")
    int limit, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.vugaenterprises.androidtv.data.model.AllContentResponse> $completion);
    
    @retrofit2.http.FormUrlEncoded()
    @retrofit2.http.POST(value = "increaseContentView")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object increaseContentView(@retrofit2.http.Field(value = "content_id")
    int contentId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.vugaenterprises.androidtv.data.model.RestResponse> $completion);
    
    @retrofit2.http.FormUrlEncoded()
    @retrofit2.http.POST(value = "increaseContentShare")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object increaseContentShare(@retrofit2.http.Field(value = "content_id")
    int contentId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.vugaenterprises.androidtv.data.model.RestResponse> $completion);
    
    @retrofit2.http.FormUrlEncoded()
    @retrofit2.http.POST(value = "increaseContentDownload")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object increaseContentDownload(@retrofit2.http.Field(value = "content_id")
    int contentId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.vugaenterprises.androidtv.data.model.RestResponse> $completion);
    
    @retrofit2.http.FormUrlEncoded()
    @retrofit2.http.POST(value = "increaseEpisodeView")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object increaseEpisodeView(@retrofit2.http.Field(value = "episode_id")
    int episodeId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.vugaenterprises.androidtv.data.model.RestResponse> $completion);
    
    @retrofit2.http.FormUrlEncoded()
    @retrofit2.http.POST(value = "increaseEpisodeDownload")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object increaseEpisodeDownload(@retrofit2.http.Field(value = "episode_id")
    int episodeId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.vugaenterprises.androidtv.data.model.RestResponse> $completion);
    
    @retrofit2.http.FormUrlEncoded()
    @retrofit2.http.POST(value = "fetchActorDetails")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object fetchActorDetails(@retrofit2.http.Field(value = "actor_id")
    int actorId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.vugaenterprises.androidtv.data.model.ActorDataResponse> $completion);
    
    @retrofit2.http.FormUrlEncoded()
    @retrofit2.http.POST(value = "fetchCustomAds")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object fetchCustomAds(@retrofit2.http.Field(value = "is_android")
    int deviceType, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.vugaenterprises.androidtv.data.model.CustomAdsResponse> $completion);
    
    @retrofit2.http.FormUrlEncoded()
    @retrofit2.http.POST(value = "increaseAdMetric")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object increaseAdMetric(@retrofit2.http.Field(value = "custom_ad_id")
    long customAdId, @retrofit2.http.Field(value = "metric")
    @org.jetbrains.annotations.NotNull()
    java.lang.String metric, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.vugaenterprises.androidtv.data.model.RestResponse> $completion);
}