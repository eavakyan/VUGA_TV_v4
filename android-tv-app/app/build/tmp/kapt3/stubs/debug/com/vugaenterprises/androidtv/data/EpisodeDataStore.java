package com.vugaenterprises.androidtv.data;

@javax.inject.Singleton()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0003\b\u0007\u0018\u00002\u00020\u0001B\u0007\b\u0007\u00a2\u0006\u0002\u0010\u0002J\u0006\u0010\n\u001a\u00020\u000bJ\u0010\u0010\f\u001a\u00020\u000b2\b\u0010\r\u001a\u0004\u0018\u00010\u0005R\u0016\u0010\u0003\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0019\u0010\u0006\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00050\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\b\u0010\t\u00a8\u0006\u000e"}, d2 = {"Lcom/vugaenterprises/androidtv/data/EpisodeDataStore;", "", "()V", "_selectedEpisode", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/vugaenterprises/androidtv/data/model/EpisodeItem;", "selectedEpisode", "Lkotlinx/coroutines/flow/StateFlow;", "getSelectedEpisode", "()Lkotlinx/coroutines/flow/StateFlow;", "clearSelectedEpisode", "", "setSelectedEpisode", "episode", "app_debug"})
public final class EpisodeDataStore {
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.vugaenterprises.androidtv.data.model.EpisodeItem> _selectedEpisode = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.vugaenterprises.androidtv.data.model.EpisodeItem> selectedEpisode = null;
    
    @javax.inject.Inject()
    public EpisodeDataStore() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.vugaenterprises.androidtv.data.model.EpisodeItem> getSelectedEpisode() {
        return null;
    }
    
    public final void setSelectedEpisode(@org.jetbrains.annotations.Nullable()
    com.vugaenterprises.androidtv.data.model.EpisodeItem episode) {
    }
    
    public final void clearSelectedEpisode() {
    }
}