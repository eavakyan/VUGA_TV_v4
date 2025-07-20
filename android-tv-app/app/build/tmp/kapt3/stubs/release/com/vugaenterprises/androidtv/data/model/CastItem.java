package com.vugaenterprises.androidtv.data.model;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0010\n\u0002\u0010\u000b\n\u0002\b\u0004\b\u0086\b\u0018\u00002\u00020\u0001B7\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0005\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0006\u001a\u00020\u0007\u0012\b\b\u0002\u0010\b\u001a\u00020\t\u00a2\u0006\u0002\u0010\nJ\t\u0010\u0013\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0014\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0015\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0016\u001a\u00020\u0007H\u00c6\u0003J\t\u0010\u0017\u001a\u00020\tH\u00c6\u0003J;\u0010\u0018\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00032\b\b\u0002\u0010\u0006\u001a\u00020\u00072\b\b\u0002\u0010\b\u001a\u00020\tH\u00c6\u0001J\u0013\u0010\u0019\u001a\u00020\u001a2\b\u0010\u001b\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u001c\u001a\u00020\u0003H\u00d6\u0001J\t\u0010\u001d\u001a\u00020\u0007H\u00d6\u0001R\u0016\u0010\b\u001a\u00020\t8\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\fR\u0016\u0010\u0005\u001a\u00020\u00038\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000eR\u0016\u0010\u0006\u001a\u00020\u00078\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u0010R\u0016\u0010\u0004\u001a\u00020\u00038\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\u000eR\u0016\u0010\u0002\u001a\u00020\u00038\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u000e\u00a8\u0006\u001e"}, d2 = {"Lcom/vugaenterprises/androidtv/data/model/CastItem;", "", "id", "", "contentId", "actorId", "characterName", "", "actor", "Lcom/vugaenterprises/androidtv/data/model/ActorItem;", "(IIILjava/lang/String;Lcom/vugaenterprises/androidtv/data/model/ActorItem;)V", "getActor", "()Lcom/vugaenterprises/androidtv/data/model/ActorItem;", "getActorId", "()I", "getCharacterName", "()Ljava/lang/String;", "getContentId", "getId", "component1", "component2", "component3", "component4", "component5", "copy", "equals", "", "other", "hashCode", "toString", "app_release"})
public final class CastItem {
    @com.google.gson.annotations.SerializedName(value = "id")
    private final int id = 0;
    @com.google.gson.annotations.SerializedName(value = "content_id")
    private final int contentId = 0;
    @com.google.gson.annotations.SerializedName(value = "actor_id")
    private final int actorId = 0;
    @com.google.gson.annotations.SerializedName(value = "character_name")
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String characterName = null;
    @com.google.gson.annotations.SerializedName(value = "actor")
    @org.jetbrains.annotations.NotNull()
    private final com.vugaenterprises.androidtv.data.model.ActorItem actor = null;
    
    public CastItem(int id, int contentId, int actorId, @org.jetbrains.annotations.NotNull()
    java.lang.String characterName, @org.jetbrains.annotations.NotNull()
    com.vugaenterprises.androidtv.data.model.ActorItem actor) {
        super();
    }
    
    public final int getId() {
        return 0;
    }
    
    public final int getContentId() {
        return 0;
    }
    
    public final int getActorId() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getCharacterName() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.vugaenterprises.androidtv.data.model.ActorItem getActor() {
        return null;
    }
    
    public CastItem() {
        super();
    }
    
    public final int component1() {
        return 0;
    }
    
    public final int component2() {
        return 0;
    }
    
    public final int component3() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component4() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.vugaenterprises.androidtv.data.model.ActorItem component5() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.vugaenterprises.androidtv.data.model.CastItem copy(int id, int contentId, int actorId, @org.jetbrains.annotations.NotNull()
    java.lang.String characterName, @org.jetbrains.annotations.NotNull()
    com.vugaenterprises.androidtv.data.model.ActorItem actor) {
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