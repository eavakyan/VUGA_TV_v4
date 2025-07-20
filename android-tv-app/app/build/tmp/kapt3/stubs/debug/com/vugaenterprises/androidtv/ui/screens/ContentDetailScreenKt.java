package com.vugaenterprises.androidtv.ui.screens;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000.\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010 \n\u0000\u001aP\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0012\u0010\u0004\u001a\u000e\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u00010\u00052\f\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\u00010\b2\u0012\u0010\t\u001a\u000e\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u00010\u00052\b\b\u0002\u0010\n\u001a\u00020\u000bH\u0007\u001a\u001e\u0010\f\u001a\u00020\u00012\u0006\u0010\r\u001a\u00020\u00062\f\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00010\bH\u0007\u001a*\u0010\u000f\u001a\u00020\u00012\f\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u00060\u00112\u0012\u0010\u0004\u001a\u000e\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u00010\u0005H\u0007\u00a8\u0006\u0012"}, d2 = {"ContentDetailScreen", "", "contentId", "", "onContentClick", "Lkotlin/Function1;", "Lcom/vugaenterprises/androidtv/data/model/Content;", "onNavigateBack", "Lkotlin/Function0;", "onPlayVideo", "viewModel", "Lcom/vugaenterprises/androidtv/ui/viewmodels/ContentDetailViewModel;", "RelatedContentCard", "content", "onClick", "RelatedContentSection", "relatedContent", "", "app_debug"})
public final class ContentDetailScreenKt {
    
    @androidx.compose.runtime.Composable()
    public static final void ContentDetailScreen(int contentId, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super com.vugaenterprises.androidtv.data.model.Content, kotlin.Unit> onContentClick, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateBack, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super com.vugaenterprises.androidtv.data.model.Content, kotlin.Unit> onPlayVideo, @org.jetbrains.annotations.NotNull()
    com.vugaenterprises.androidtv.ui.viewmodels.ContentDetailViewModel viewModel) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void RelatedContentCard(@org.jetbrains.annotations.NotNull()
    com.vugaenterprises.androidtv.data.model.Content content, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onClick) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void RelatedContentSection(@org.jetbrains.annotations.NotNull()
    java.util.List<com.vugaenterprises.androidtv.data.model.Content> relatedContent, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super com.vugaenterprises.androidtv.data.model.Content, kotlin.Unit> onContentClick) {
    }
}