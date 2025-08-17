package com.retry.vuga.retrofit;

import com.retry.vuga.model.ActorData;
import com.retry.vuga.model.AllContent;
import com.retry.vuga.model.AppSetting;
import com.retry.vuga.model.ChannelByCategories;
import com.retry.vuga.model.ContentByGenre;
import com.retry.vuga.model.ContentByDistributor;
import com.retry.vuga.model.ContentDetail;
import com.retry.vuga.model.HomePage;
import com.retry.vuga.model.LiveTv;
import com.retry.vuga.model.ProfileResponse;
import com.retry.vuga.model.RestResponse;
import com.retry.vuga.model.SearchChannel;
import com.retry.vuga.model.UserRegistration;
import com.retry.vuga.model.AgeRatingResponse;
import com.retry.vuga.model.RecentlyWatchedContent;
import com.retry.vuga.model.SubscriptionModels;
import com.retry.vuga.model.ads.CustomAds;
import com.retry.vuga.utils.Const;

import java.util.HashMap;

import io.reactivex.Single;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;

public interface RetrofitService {


    @POST("fetchSettings")
    Single<AppSetting> getAppSettings();


    @Multipart
    @POST("userRegistration")
    Single<UserRegistration> registerUser(@PartMap HashMap<String, RequestBody> hashMap);


    @FormUrlEncoded
    @POST("fetchHomePageData")
    Single<HomePage> getHomeData(@Field(Const.ApiKey.user_id) int id
    );


    @Multipart
    @POST("updateProfile")
    Single<UserRegistration> updateProfile(@PartMap HashMap<String, RequestBody> hashMap,
                                           @Part MultipartBody.Part image);
    



    @FormUrlEncoded
    @POST("searchContent")
    Single<AllContent> searchContent(@FieldMap HashMap<String, Object> hashMap);

    @FormUrlEncoded
    @POST("fetchContentsByGenre")
    Single<ContentByGenre> getContentByGenre(@Field(Const.ApiKey.start) int start,
                                             @Field(Const.ApiKey.limit) int limit,
                                             @Field(Const.ApiKey.genre_id) int id);
    
    @FormUrlEncoded
    @POST("fetchContentsByDistributor")
    Single<ContentByDistributor> getContentByDistributor(@FieldMap HashMap<String, Object> params);


    @FormUrlEncoded
    @POST("fetchContentDetails")
    Single<ContentDetail> getContentDetail(@Field(Const.ApiKey.user_id) int userId,
                                           @Field(Const.ApiKey.content_id) int contentId,
                                           @Field("profile_id") Integer profileId);

    @FormUrlEncoded
    @POST("fetchWatchList")
    Single<AllContent> getWatchList(@Field(Const.ApiKey.type) int type,
                                    @Field(Const.ApiKey.user_id) int userId,
                                   @Field(Const.ApiKey.start) int start,
                                   @Field(Const.ApiKey.limit) int limit,
                                   @Field("profile_id") Integer profileId);
    



    @FormUrlEncoded
    @POST("fetchTVChannelByCategory")
    Single<ChannelByCategories> getChannelByCategories(@Field(Const.ApiKey.start) int start,
                                                       @Field(Const.ApiKey.limit) int limit,
                                                       @Field(Const.ApiKey.tv_category_id) int tv_category_id);


    @FormUrlEncoded
    @POST("searchTVChannel")
    Single<SearchChannel> searchTVChannel(@FieldMap HashMap<String, Object> hashMap);

    @POST("fetchLiveTVPageData")
    Single<LiveTv> getLiveTvChannel();

    @FormUrlEncoded
    @POST("logOut")
    Single<UserRegistration> logOutUser(@Field(Const.ApiKey.user_id) int id);

    @FormUrlEncoded
    @POST("deleteMyAccount")
    Single<RestResponse> deleteAccount(@Field(Const.ApiKey.user_id) int user_id);


    @FormUrlEncoded
    @POST("increaseContentDownload")
    Single<RestResponse> increaseContentDownload(@Field(Const.ApiKey.content_id) int id
    );

    @FormUrlEncoded
    @POST("increaseEpisodeDownload")
    Single<RestResponse> increaseEpisodeDownload(@Field(Const.ApiKey.episode_id) int id
    );

    @FormUrlEncoded
    @POST("increaseContentView")
    Single<RestResponse> increaseContentView(@Field(Const.ApiKey.content_id) int id
    );

    @FormUrlEncoded
    @POST("increaseEpisodeView")
    Single<RestResponse> increaseEpisodeView(@Field(Const.ApiKey.episode_id) int id
    );

    @FormUrlEncoded
    @POST("increaseContentShare")
    Single<RestResponse> increaseContentShare(@Field(Const.ApiKey.content_id) int id
    );

    @FormUrlEncoded
    @POST("increaseTVChannelView")
    Single<RestResponse> increaseTvChannelView(@Field(Const.ApiKey.channel_id) String id
    );

    @FormUrlEncoded
    @POST("fetchCustomAds")
    Single<CustomAds> fetchCustomAds(@Field(Const.ApiKey.is_android) int deviceType);

    @FormUrlEncoded
    @POST("fetchActorDetails")
    Single<ActorData> fetchActorDetails(@Field(Const.ApiKey.actor_id) int actor_id);

    @FormUrlEncoded
    @POST("increaseAdMetric")
    Single<RestResponse> increaseAdMetric(@Field(Const.ApiKey.custom_ad_id) Long custom_ad_id,
                                          @Field(Const.ApiKey.metric) String metric);

    @FormUrlEncoded
    @POST("tv-auth/authenticate")
    Single<RestResponse> authenticateTVSession(@Field("session_token") String sessionToken,
                                               @Field("app_user_id") int userId);

    @FormUrlEncoded
    @POST("getUserProfiles")
    Single<ProfileResponse> getUserProfiles(@Field(Const.ApiKey.user_id) int userId);

    @FormUrlEncoded
    @POST("createProfile")
    Single<ProfileResponse> createProfile(@Field(Const.ApiKey.user_id) int userId,
                                        @Field("name") String name,
                                        @Field("avatar_id") int avatarId,
                                        @Field("is_kids") int isKids);

    @FormUrlEncoded
    @POST("profile/update")
    Single<ProfileResponse> updateProfile(@Field("profile_id") int profileId,
                                        @Field(Const.ApiKey.user_id) int userId,
                                        @Field("name") String name,
                                        @Field("avatar_id") int avatarId,
                                        @Field("is_kids") int isKids);
    
    @Multipart
    @POST("profile/update")
    Single<ProfileResponse> updateProfileWithImage(@Part("profile_id") RequestBody profileId,
                                                 @Part("user_id") RequestBody userId,
                                                 @Part("name") RequestBody name,
                                                 @Part("avatar_type") RequestBody avatarType,
                                                 @Part MultipartBody.Part profileImage);

    @FormUrlEncoded
    @POST("deleteProfile")
    Single<RestResponse> deleteProfile(@Field("profile_id") int profileId,
                                     @Field(Const.ApiKey.user_id) int userId);

    @FormUrlEncoded
    @POST("selectProfile")
    Single<RestResponse> selectProfile(@Field(Const.ApiKey.user_id) int userId,
                                     @Field("profile_id") int profileId);

    @FormUrlEncoded
    @POST("user/toggle-watchlist")
    Single<UserRegistration> toggleWatchlist(@FieldMap HashMap<String, Object> params);

    @FormUrlEncoded
    @POST("user/toggle-favorite")
    Single<UserRegistration> toggleFavorite(@FieldMap HashMap<String, Object> params);

    @FormUrlEncoded
    @POST("user/rate-content")
    Single<RestResponse> rateContent(@FieldMap HashMap<String, Object> params);
    
    @FormUrlEncoded
    @POST("user/rate-episode")
    Single<RestResponse> rateEpisode(@FieldMap HashMap<String, Object> params);

    @FormUrlEncoded
    @POST("watch/update-progress")
    Single<RestResponse> updateWatchProgress(@FieldMap HashMap<String, Object> params);

    @FormUrlEncoded
    @POST("watch/continue-watching")
    Single<RestResponse> getContinueWatching(@FieldMap HashMap<String, Object> params);

    @FormUrlEncoded
    @POST("watch/mark-completed")
    Single<RestResponse> markAsCompleted(@FieldMap HashMap<String, Object> params);

    @FormUrlEncoded
    @POST("user/watch-history")
    Single<RestResponse> getWatchHistory(@FieldMap HashMap<String, Object> params);

    @POST("profile/age-ratings")
    Single<AgeRatingResponse> getAgeRatings();

    @FormUrlEncoded
    @POST("profile/update-age-settings")
    Single<RestResponse> updateAgeSettings(
            @Field("profile_id") int profileId,
            @Field("user_id") int userId,
            @Field("age") Integer age,
            @Field("is_kids_profile") boolean isKidsProfile
    );

    // Genre endpoints
    @POST("genre/all")
    Single<HomePage> getAllGenres();

    // Recently watched content by IDs
    @FormUrlEncoded
    @POST("content/by-ids")
    Single<RecentlyWatchedContent> getRecentlyWatchedContent(@Field("content_ids") String contentIds,
                                                           @Field("user_id") int userId,
                                                           @Field("profile_id") Integer profileId);

    // Profile avatar upload
    @FormUrlEncoded
    @POST("profiles/avatar/upload")
    Single<ProfileResponse> uploadProfileAvatar(@Field("user_id") int userId,
                                              @Field("profile_id") int profileId,
                                              @Field("image_data") String imageData);

    // Profile avatar removal
    @FormUrlEncoded
    @POST("profiles/avatar/remove")
    Single<RestResponse> removeProfileAvatar(@Field("user_id") int userId,
                                           @Field("profile_id") int profileId);

    // Subscription APIs
    @POST("subscription/plans")
    Single<SubscriptionModels.SubscriptionPlansResponse> getSubscriptionPlans();

    @FormUrlEncoded
    @POST("subscription/my-subscriptions")
    Single<SubscriptionModels.MySubscriptionsResponse> getMySubscriptions(@Field("user_id") int userId);

    @FormUrlEncoded
    @POST("subscription/validate-promo")
    Single<RestResponse> validatePromoCode(@Field("promo_code") String promoCode,
                                         @Field("user_id") int userId);

}
