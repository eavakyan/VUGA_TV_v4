package com.vugaenterprises.androidtv.ui.viewmodels;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0015\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001Bk\u0012\u000e\b\u0002\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003\u0012\u000e\b\u0002\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003\u0012\u000e\b\u0002\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003\u0012\u000e\b\u0002\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003\u0012\u000e\b\u0002\u0010\b\u001a\b\u0012\u0004\u0012\u00020\t0\u0003\u0012\b\b\u0002\u0010\n\u001a\u00020\u000b\u0012\n\b\u0002\u0010\f\u001a\u0004\u0018\u00010\r\u00a2\u0006\u0002\u0010\u000eJ\u000f\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003H\u00c6\u0003J\u000f\u0010\u0019\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003H\u00c6\u0003J\u000f\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003H\u00c6\u0003J\u000f\u0010\u001b\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003H\u00c6\u0003J\u000f\u0010\u001c\u001a\b\u0012\u0004\u0012\u00020\t0\u0003H\u00c6\u0003J\t\u0010\u001d\u001a\u00020\u000bH\u00c6\u0003J\u000b\u0010\u001e\u001a\u0004\u0018\u00010\rH\u00c6\u0003Jo\u0010\u001f\u001a\u00020\u00002\u000e\b\u0002\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\u000e\b\u0002\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\u000e\b\u0002\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\u000e\b\u0002\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\u000e\b\u0002\u0010\b\u001a\b\u0012\u0004\u0012\u00020\t0\u00032\b\b\u0002\u0010\n\u001a\u00020\u000b2\n\b\u0002\u0010\f\u001a\u0004\u0018\u00010\rH\u00c6\u0001J\u0013\u0010 \u001a\u00020\u000b2\b\u0010!\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\"\u001a\u00020#H\u00d6\u0001J\t\u0010$\u001a\u00020\rH\u00d6\u0001R\u0017\u0010\b\u001a\b\u0012\u0004\u0012\u00020\t0\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u0010R\u0013\u0010\f\u001a\u0004\u0018\u00010\r\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\u0012R\u0017\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u0010R\u0011\u0010\n\u001a\u00020\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u0014R\u0017\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0010R\u0017\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0010R\u0017\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0017\u0010\u0010\u00a8\u0006%"}, d2 = {"Lcom/vugaenterprises/androidtv/ui/viewmodels/HomeUiState;", "", "featuredContent", "", "Lcom/vugaenterprises/androidtv/data/model/Content;", "trendingContent", "newContent", "recommendations", "continueWatching", "Lcom/vugaenterprises/androidtv/data/model/WatchHistory;", "isLoading", "", "error", "", "(Ljava/util/List;Ljava/util/List;Ljava/util/List;Ljava/util/List;Ljava/util/List;ZLjava/lang/String;)V", "getContinueWatching", "()Ljava/util/List;", "getError", "()Ljava/lang/String;", "getFeaturedContent", "()Z", "getNewContent", "getRecommendations", "getTrendingContent", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "copy", "equals", "other", "hashCode", "", "toString", "app_release"})
public final class HomeUiState {
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<com.vugaenterprises.androidtv.data.model.Content> featuredContent = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<com.vugaenterprises.androidtv.data.model.Content> trendingContent = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<com.vugaenterprises.androidtv.data.model.Content> newContent = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<com.vugaenterprises.androidtv.data.model.Content> recommendations = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<com.vugaenterprises.androidtv.data.model.WatchHistory> continueWatching = null;
    private final boolean isLoading = false;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String error = null;
    
    public HomeUiState(@org.jetbrains.annotations.NotNull()
    java.util.List<com.vugaenterprises.androidtv.data.model.Content> featuredContent, @org.jetbrains.annotations.NotNull()
    java.util.List<com.vugaenterprises.androidtv.data.model.Content> trendingContent, @org.jetbrains.annotations.NotNull()
    java.util.List<com.vugaenterprises.androidtv.data.model.Content> newContent, @org.jetbrains.annotations.NotNull()
    java.util.List<com.vugaenterprises.androidtv.data.model.Content> recommendations, @org.jetbrains.annotations.NotNull()
    java.util.List<com.vugaenterprises.androidtv.data.model.WatchHistory> continueWatching, boolean isLoading, @org.jetbrains.annotations.Nullable()
    java.lang.String error) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.vugaenterprises.androidtv.data.model.Content> getFeaturedContent() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.vugaenterprises.androidtv.data.model.Content> getTrendingContent() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.vugaenterprises.androidtv.data.model.Content> getNewContent() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.vugaenterprises.androidtv.data.model.Content> getRecommendations() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.vugaenterprises.androidtv.data.model.WatchHistory> getContinueWatching() {
        return null;
    }
    
    public final boolean isLoading() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getError() {
        return null;
    }
    
    public HomeUiState() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.vugaenterprises.androidtv.data.model.Content> component1() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.vugaenterprises.androidtv.data.model.Content> component2() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.vugaenterprises.androidtv.data.model.Content> component3() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.vugaenterprises.androidtv.data.model.Content> component4() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.vugaenterprises.androidtv.data.model.WatchHistory> component5() {
        return null;
    }
    
    public final boolean component6() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component7() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.vugaenterprises.androidtv.ui.viewmodels.HomeUiState copy(@org.jetbrains.annotations.NotNull()
    java.util.List<com.vugaenterprises.androidtv.data.model.Content> featuredContent, @org.jetbrains.annotations.NotNull()
    java.util.List<com.vugaenterprises.androidtv.data.model.Content> trendingContent, @org.jetbrains.annotations.NotNull()
    java.util.List<com.vugaenterprises.androidtv.data.model.Content> newContent, @org.jetbrains.annotations.NotNull()
    java.util.List<com.vugaenterprises.androidtv.data.model.Content> recommendations, @org.jetbrains.annotations.NotNull()
    java.util.List<com.vugaenterprises.androidtv.data.model.WatchHistory> continueWatching, boolean isLoading, @org.jetbrains.annotations.Nullable()
    java.lang.String error) {
        return null;
    }
    
    @java.lang.Override()
    public boolean equals(@org.jetbrains.annotations.Nullable()
    java.lang.Object other) {
        return false;
    }
    
    @java.lang.Override()
    public int hashCode() {
        return 0;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public java.lang.String toString() {
        return null;
    }
}