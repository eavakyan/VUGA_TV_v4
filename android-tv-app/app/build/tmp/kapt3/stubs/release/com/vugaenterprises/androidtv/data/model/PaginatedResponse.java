package com.vugaenterprises.androidtv.data.model;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010 \n\u0000\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\u0016\n\u0002\u0010\u000e\n\u0000\b\u0086\b\u0018\u0000*\u0004\b\u0000\u0010\u00012\u00020\u0002B;\u0012\f\u0010\u0003\u001a\b\u0012\u0004\u0012\u00028\u00000\u0004\u0012\u0006\u0010\u0005\u001a\u00020\u0006\u0012\u0006\u0010\u0007\u001a\u00020\u0006\u0012\u0006\u0010\b\u001a\u00020\u0006\u0012\u0006\u0010\t\u001a\u00020\n\u0012\u0006\u0010\u000b\u001a\u00020\n\u00a2\u0006\u0002\u0010\fJ\u000f\u0010\u0016\u001a\b\u0012\u0004\u0012\u00028\u00000\u0004H\u00c6\u0003J\t\u0010\u0017\u001a\u00020\u0006H\u00c6\u0003J\t\u0010\u0018\u001a\u00020\u0006H\u00c6\u0003J\t\u0010\u0019\u001a\u00020\u0006H\u00c6\u0003J\t\u0010\u001a\u001a\u00020\nH\u00c6\u0003J\t\u0010\u001b\u001a\u00020\nH\u00c6\u0003JQ\u0010\u001c\u001a\b\u0012\u0004\u0012\u00028\u00000\u00002\u000e\b\u0002\u0010\u0003\u001a\b\u0012\u0004\u0012\u00028\u00000\u00042\b\b\u0002\u0010\u0005\u001a\u00020\u00062\b\b\u0002\u0010\u0007\u001a\u00020\u00062\b\b\u0002\u0010\b\u001a\u00020\u00062\b\b\u0002\u0010\t\u001a\u00020\n2\b\b\u0002\u0010\u000b\u001a\u00020\nH\u00c6\u0001J\u0013\u0010\u001d\u001a\u00020\n2\b\u0010\u001e\u001a\u0004\u0018\u00010\u0002H\u00d6\u0003J\t\u0010\u001f\u001a\u00020\u0006H\u00d6\u0001J\t\u0010 \u001a\u00020!H\u00d6\u0001R\u001c\u0010\u0003\u001a\b\u0012\u0004\u0012\u00028\u00000\u00048\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000eR\u0016\u0010\u0007\u001a\u00020\u00068\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u0010R\u0016\u0010\t\u001a\u00020\n8\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\u0012R\u0016\u0010\u000b\u001a\u00020\n8\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u0012R\u0016\u0010\b\u001a\u00020\u00068\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u0010R\u0016\u0010\u0005\u001a\u00020\u00068\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0010\u00a8\u0006\""}, d2 = {"Lcom/vugaenterprises/androidtv/data/model/PaginatedResponse;", "T", "", "content", "", "totalPages", "", "currentPage", "totalItems", "hasNext", "", "hasPrevious", "(Ljava/util/List;IIIZZ)V", "getContent", "()Ljava/util/List;", "getCurrentPage", "()I", "getHasNext", "()Z", "getHasPrevious", "getTotalItems", "getTotalPages", "component1", "component2", "component3", "component4", "component5", "component6", "copy", "equals", "other", "hashCode", "toString", "", "app_release"})
public final class PaginatedResponse<T extends java.lang.Object> {
    @com.google.gson.annotations.SerializedName(value = "content")
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<T> content = null;
    @com.google.gson.annotations.SerializedName(value = "totalPages")
    private final int totalPages = 0;
    @com.google.gson.annotations.SerializedName(value = "currentPage")
    private final int currentPage = 0;
    @com.google.gson.annotations.SerializedName(value = "totalItems")
    private final int totalItems = 0;
    @com.google.gson.annotations.SerializedName(value = "hasNext")
    private final boolean hasNext = false;
    @com.google.gson.annotations.SerializedName(value = "hasPrevious")
    private final boolean hasPrevious = false;
    
    public PaginatedResponse(@org.jetbrains.annotations.NotNull()
    java.util.List<? extends T> content, int totalPages, int currentPage, int totalItems, boolean hasNext, boolean hasPrevious) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<T> getContent() {
        return null;
    }
    
    public final int getTotalPages() {
        return 0;
    }
    
    public final int getCurrentPage() {
        return 0;
    }
    
    public final int getTotalItems() {
        return 0;
    }
    
    public final boolean getHasNext() {
        return false;
    }
    
    public final boolean getHasPrevious() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<T> component1() {
        return null;
    }
    
    public final int component2() {
        return 0;
    }
    
    public final int component3() {
        return 0;
    }
    
    public final int component4() {
        return 0;
    }
    
    public final boolean component5() {
        return false;
    }
    
    public final boolean component6() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.vugaenterprises.androidtv.data.model.PaginatedResponse<T> copy(@org.jetbrains.annotations.NotNull()
    java.util.List<? extends T> content, int totalPages, int currentPage, int totalItems, boolean hasNext, boolean hasPrevious) {
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