package com.vugaenterprises.androidtv.ui.components;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u00004\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\u001a4\u0010\u0000\u001a\u00020\u00012\f\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\u0012\u0010\u0005\u001a\u000e\u0012\u0004\u0012\u00020\u0004\u0012\u0004\u0012\u00020\u00010\u00062\b\b\u0002\u0010\u0007\u001a\u00020\bH\u0007\u001aH\u0010\t\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00042\u0006\u0010\n\u001a\u00020\u000b2\f\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u00010\r2\u0014\b\u0002\u0010\u000e\u001a\u000e\u0012\u0004\u0012\u00020\u000b\u0012\u0004\u0012\u00020\u00010\u00062\n\b\u0002\u0010\u000f\u001a\u0004\u0018\u00010\u0010H\u0007\u00a8\u0006\u0011"}, d2 = {"AutoScrollingBanner", "", "content", "", "Lcom/vugaenterprises/androidtv/data/model/Content;", "onContentClick", "Lkotlin/Function1;", "modifier", "Landroidx/compose/ui/Modifier;", "BannerItem", "isFocused", "", "onClick", "Lkotlin/Function0;", "onFocusChanged", "focusRequester", "Landroidx/compose/ui/focus/FocusRequester;", "app_release"})
public final class AutoScrollingBannerKt {
    
    @androidx.compose.runtime.Composable()
    public static final void AutoScrollingBanner(@org.jetbrains.annotations.NotNull()
    java.util.List<com.vugaenterprises.androidtv.data.model.Content> content, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super com.vugaenterprises.androidtv.data.model.Content, kotlin.Unit> onContentClick, @org.jetbrains.annotations.NotNull()
    androidx.compose.ui.Modifier modifier) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void BannerItem(@org.jetbrains.annotations.NotNull()
    com.vugaenterprises.androidtv.data.model.Content content, boolean isFocused, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onClick, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.Boolean, kotlin.Unit> onFocusChanged, @org.jetbrains.annotations.Nullable()
    androidx.compose.ui.focus.FocusRequester focusRequester) {
    }
}