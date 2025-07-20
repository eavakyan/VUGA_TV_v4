package com.vugaenterprises.androidtv.data;

@javax.inject.Singleton()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0002\b\u0004\b\u0007\u0018\u00002\u00020\u0001B\u0007\b\u0007\u00a2\u0006\u0002\u0010\u0002J\u0006\u0010\u000f\u001a\u00020\u0010J\u001e\u0010\u0011\u001a\u00020\u00102\u0006\u0010\u0012\u001a\u00020\u00052\u000e\b\u0002\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\b0\u0007R\u0016\u0010\u0003\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0006\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00070\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0019\u0010\t\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00050\n\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\fR\u001d\u0010\r\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00070\n\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\f\u00a8\u0006\u0014"}, d2 = {"Lcom/vugaenterprises/androidtv/data/CastDetailDataStore;", "", "()V", "_currentCastMember", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/vugaenterprises/androidtv/data/model/CastItem;", "_relatedContent", "", "Lcom/vugaenterprises/androidtv/data/model/Content;", "currentCastMember", "Lkotlinx/coroutines/flow/StateFlow;", "getCurrentCastMember", "()Lkotlinx/coroutines/flow/StateFlow;", "relatedContent", "getRelatedContent", "clearCurrentCastMember", "", "setCurrentCastMember", "castMember", "content", "app_release"})
public final class CastDetailDataStore {
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.vugaenterprises.androidtv.data.model.CastItem> _currentCastMember = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.vugaenterprises.androidtv.data.model.CastItem> currentCastMember = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.util.List<com.vugaenterprises.androidtv.data.model.Content>> _relatedContent = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.vugaenterprises.androidtv.data.model.Content>> relatedContent = null;
    
    @javax.inject.Inject()
    public CastDetailDataStore() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.vugaenterprises.androidtv.data.model.CastItem> getCurrentCastMember() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.vugaenterprises.androidtv.data.model.Content>> getRelatedContent() {
        return null;
    }
    
    public final void setCurrentCastMember(@org.jetbrains.annotations.NotNull()
    com.vugaenterprises.androidtv.data.model.CastItem castMember, @org.jetbrains.annotations.NotNull()
    java.util.List<com.vugaenterprises.androidtv.data.model.Content> content) {
    }
    
    public final void clearCurrentCastMember() {
    }
}