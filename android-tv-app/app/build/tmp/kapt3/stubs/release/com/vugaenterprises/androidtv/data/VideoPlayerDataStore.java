package com.vugaenterprises.androidtv.data;

@javax.inject.Singleton()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0004\b\u0007\u0018\u00002\u00020\u0001B\u0007\b\u0007\u00a2\u0006\u0002\u0010\u0002J\u0006\u0010\u0005\u001a\u00020\u0006J\b\u0010\u0007\u001a\u0004\u0018\u00010\u0004J\u000e\u0010\b\u001a\u00020\u00062\u0006\u0010\t\u001a\u00020\u0004R\u0010\u0010\u0003\u001a\u0004\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\n"}, d2 = {"Lcom/vugaenterprises/androidtv/data/VideoPlayerDataStore;", "", "()V", "currentContent", "Lcom/vugaenterprises/androidtv/data/model/Content;", "clearCurrentContent", "", "getCurrentContent", "setCurrentContent", "content", "app_release"})
public final class VideoPlayerDataStore {
    @org.jetbrains.annotations.Nullable()
    private com.vugaenterprises.androidtv.data.model.Content currentContent;
    
    @javax.inject.Inject()
    public VideoPlayerDataStore() {
        super();
    }
    
    public final void setCurrentContent(@org.jetbrains.annotations.NotNull()
    com.vugaenterprises.androidtv.data.model.Content content) {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.vugaenterprises.androidtv.data.model.Content getCurrentContent() {
        return null;
    }
    
    public final void clearCurrentContent() {
    }
}