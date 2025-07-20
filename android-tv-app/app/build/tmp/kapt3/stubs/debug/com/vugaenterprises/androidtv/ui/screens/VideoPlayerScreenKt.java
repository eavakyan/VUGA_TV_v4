package com.vugaenterprises.androidtv.ui.screens;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000<\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\u001a(\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00032\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bH\u0007\u001a&\u0010\t\u001a\u00020\u00012\u0006\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\u000b2\f\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u00010\u000eH\u0007\u001a,\u0010\u000f\u001a\u00020\u00012\b\u0010\u0010\u001a\u0004\u0018\u00010\u00112\n\b\u0002\u0010\u0012\u001a\u0004\u0018\u00010\u00132\f\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u00010\u000eH\u0007\u001a\u0010\u0010\u0014\u001a\u00020\u000b2\u0006\u0010\u0015\u001a\u00020\u0003H\u0002\u00a8\u0006\u0016"}, d2 = {"TimelineOverlay", "", "currentPosition", "", "duration", "isSeeking", "", "seekDirection", "", "VideoPlayer", "videoUrl", "", "contentTitle", "onNavigateBack", "Lkotlin/Function0;", "VideoPlayerScreen", "content", "Lcom/vugaenterprises/androidtv/data/model/Content;", "episode", "Lcom/vugaenterprises/androidtv/data/model/EpisodeItem;", "formatTime", "timeMs", "app_debug"})
public final class VideoPlayerScreenKt {
    
    @androidx.compose.runtime.Composable()
    public static final void VideoPlayerScreen(@org.jetbrains.annotations.Nullable()
    com.vugaenterprises.androidtv.data.model.Content content, @org.jetbrains.annotations.Nullable()
    com.vugaenterprises.androidtv.data.model.EpisodeItem episode, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateBack) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void VideoPlayer(@org.jetbrains.annotations.NotNull()
    java.lang.String videoUrl, @org.jetbrains.annotations.NotNull()
    java.lang.String contentTitle, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateBack) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void TimelineOverlay(long currentPosition, long duration, boolean isSeeking, int seekDirection) {
    }
    
    private static final java.lang.String formatTime(long timeMs) {
        return null;
    }
}