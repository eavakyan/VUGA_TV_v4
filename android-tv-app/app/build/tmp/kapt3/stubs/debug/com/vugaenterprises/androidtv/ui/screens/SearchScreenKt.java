package com.vugaenterprises.androidtv.ui.screens;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000>\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\u001a\u0010\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u0003H\u0007\u001a,\u0010\u0004\u001a\u00020\u00012\u0006\u0010\u0005\u001a\u00020\u00062\u0012\u0010\u0007\u001a\u000e\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u00010\b2\u0006\u0010\t\u001a\u00020\nH\u0007\u001a2\u0010\u000b\u001a\u00020\u00012\f\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u000e0\r2\u0006\u0010\u0002\u001a\u00020\u00032\u0012\u0010\u000f\u001a\u000e\u0012\u0004\u0012\u00020\u000e\u0012\u0004\u0012\u00020\u00010\bH\u0007\u001a4\u0010\u0010\u001a\u00020\u00012\u0012\u0010\u000f\u001a\u000e\u0012\u0004\u0012\u00020\u000e\u0012\u0004\u0012\u00020\u00010\b2\f\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\u00010\u00122\b\b\u0002\u0010\u0013\u001a\u00020\u0014H\u0007\u001aF\u0010\u0015\u001a\u00020\u00012\f\u0010\u0016\u001a\b\u0012\u0004\u0012\u00020\u00030\r2\f\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u00030\r2\u0012\u0010\u0018\u001a\u000e\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u00010\b2\f\u0010\u0019\u001a\b\u0012\u0004\u0012\u00020\u00010\u0012H\u0007\u00a8\u0006\u001a"}, d2 = {"NoResultsFound", "", "query", "", "SearchBar", "value", "Landroidx/compose/ui/text/input/TextFieldValue;", "onValueChange", "Lkotlin/Function1;", "focusRequester", "Landroidx/compose/ui/focus/FocusRequester;", "SearchResults", "results", "", "Lcom/vugaenterprises/androidtv/data/model/Content;", "onContentClick", "SearchScreen", "onNavigateBack", "Lkotlin/Function0;", "viewModel", "Lcom/vugaenterprises/androidtv/ui/viewmodels/SearchViewModel;", "SearchSuggestions", "popularSearches", "recentSearches", "onSuggestionClick", "onClearRecentSearches", "app_debug"})
public final class SearchScreenKt {
    
    @androidx.compose.runtime.Composable()
    public static final void SearchScreen(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super com.vugaenterprises.androidtv.data.model.Content, kotlin.Unit> onContentClick, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateBack, @org.jetbrains.annotations.NotNull()
    com.vugaenterprises.androidtv.ui.viewmodels.SearchViewModel viewModel) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void SearchBar(@org.jetbrains.annotations.NotNull()
    androidx.compose.ui.text.input.TextFieldValue value, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super androidx.compose.ui.text.input.TextFieldValue, kotlin.Unit> onValueChange, @org.jetbrains.annotations.NotNull()
    androidx.compose.ui.focus.FocusRequester focusRequester) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void SearchResults(@org.jetbrains.annotations.NotNull()
    java.util.List<com.vugaenterprises.androidtv.data.model.Content> results, @org.jetbrains.annotations.NotNull()
    java.lang.String query, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super com.vugaenterprises.androidtv.data.model.Content, kotlin.Unit> onContentClick) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void NoResultsFound(@org.jetbrains.annotations.NotNull()
    java.lang.String query) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void SearchSuggestions(@org.jetbrains.annotations.NotNull()
    java.util.List<java.lang.String> popularSearches, @org.jetbrains.annotations.NotNull()
    java.util.List<java.lang.String> recentSearches, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onSuggestionClick, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onClearRecentSearches) {
    }
}