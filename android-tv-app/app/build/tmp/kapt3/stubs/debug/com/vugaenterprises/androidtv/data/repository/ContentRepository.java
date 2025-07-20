package com.vugaenterprises.androidtv.data.repository;

@javax.inject.Singleton()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000D\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\b\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0004\b\u0007\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J0\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u00062\u0006\u0010\b\u001a\u00020\t2\b\b\u0002\u0010\n\u001a\u00020\t2\b\b\u0002\u0010\u000b\u001a\u00020\tH\u0086@\u00a2\u0006\u0002\u0010\fJ\"\u0010\r\u001a\u0004\u0018\u00010\u00072\u0006\u0010\u000e\u001a\u00020\t2\b\b\u0002\u0010\u000f\u001a\u00020\tH\u0086@\u00a2\u0006\u0002\u0010\u0010J\u001e\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\u00120\u00062\b\b\u0002\u0010\u000f\u001a\u00020\tH\u0086@\u00a2\u0006\u0002\u0010\u0013J\u001e\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00070\u00062\b\b\u0002\u0010\u000f\u001a\u00020\tH\u0086@\u00a2\u0006\u0002\u0010\u0013J\u0018\u0010\u0015\u001a\u00020\u00162\b\b\u0002\u0010\u000f\u001a\u00020\tH\u0086@\u00a2\u0006\u0002\u0010\u0013J\u001e\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u00070\u00062\b\b\u0002\u0010\u000f\u001a\u00020\tH\u0086@\u00a2\u0006\u0002\u0010\u0013J\u001e\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\u00070\u00062\b\b\u0002\u0010\u000f\u001a\u00020\tH\u0086@\u00a2\u0006\u0002\u0010\u0013J\u001e\u0010\u0019\u001a\b\u0012\u0004\u0012\u00020\u00070\u00062\b\b\u0002\u0010\u000f\u001a\u00020\tH\u0086@\u00a2\u0006\u0002\u0010\u0013J\u0016\u0010\u001a\u001a\u00020\u001b2\u0006\u0010\u000e\u001a\u00020\tH\u0086@\u00a2\u0006\u0002\u0010\u0013J\u0016\u0010\u001c\u001a\u00020\u001b2\u0006\u0010\u000e\u001a\u00020\tH\u0086@\u00a2\u0006\u0002\u0010\u0013J\u001c\u0010\u001d\u001a\b\u0012\u0004\u0012\u00020\u00070\u00062\u0006\u0010\u001e\u001a\u00020\u001fH\u0086@\u00a2\u0006\u0002\u0010 J\u000e\u0010!\u001a\u00020\u001fH\u0086@\u00a2\u0006\u0002\u0010\"R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006#"}, d2 = {"Lcom/vugaenterprises/androidtv/data/repository/ContentRepository;", "", "apiService", "Lcom/vugaenterprises/androidtv/data/api/ApiService;", "(Lcom/vugaenterprises/androidtv/data/api/ApiService;)V", "getContentByGenre", "", "Lcom/vugaenterprises/androidtv/data/model/Content;", "genreId", "", "start", "limit", "(IIILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getContentById", "contentId", "userId", "(IILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getContinueWatching", "Lcom/vugaenterprises/androidtv/data/model/WatchHistory;", "(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getFeaturedContent", "getHomeData", "Lcom/vugaenterprises/androidtv/data/model/HomePageResponse;", "getNewContent", "getRecommendations", "getTrendingContent", "increaseContentShare", "", "increaseContentView", "searchContent", "query", "", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "testApiConnection", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public final class ContentRepository {
    @org.jetbrains.annotations.NotNull()
    private final com.vugaenterprises.androidtv.data.api.ApiService apiService = null;
    
    @javax.inject.Inject()
    public ContentRepository(@org.jetbrains.annotations.NotNull()
    com.vugaenterprises.androidtv.data.api.ApiService apiService) {
        super();
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object testApiConnection(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getHomeData(int userId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.vugaenterprises.androidtv.data.model.HomePageResponse> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getFeaturedContent(int userId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.vugaenterprises.androidtv.data.model.Content>> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getTrendingContent(int userId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.vugaenterprises.androidtv.data.model.Content>> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getNewContent(int userId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.vugaenterprises.androidtv.data.model.Content>> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getRecommendations(int userId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.vugaenterprises.androidtv.data.model.Content>> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getContinueWatching(int userId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.vugaenterprises.androidtv.data.model.WatchHistory>> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getContentById(int contentId, int userId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.vugaenterprises.androidtv.data.model.Content> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object searchContent(@org.jetbrains.annotations.NotNull()
    java.lang.String query, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.vugaenterprises.androidtv.data.model.Content>> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getContentByGenre(int genreId, int start, int limit, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.vugaenterprises.androidtv.data.model.Content>> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object increaseContentView(int contentId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object increaseContentShare(int contentId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
}