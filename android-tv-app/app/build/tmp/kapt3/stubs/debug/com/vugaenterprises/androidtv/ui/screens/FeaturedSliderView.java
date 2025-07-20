package com.vugaenterprises.androidtv.ui.screens;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000X\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010 \n\u0002\b\b\u0018\u0000 %2\u00020\u0001:\u0001%B%\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u0012\b\b\u0002\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\bJ\b\u0010\u0019\u001a\u00020\u0016H\u0014J\b\u0010\u001a\u001a\u00020\u0016H\u0002J\b\u0010\u001b\u001a\u00020\u0016H\u0002J\u0014\u0010\u001c\u001a\u00020\u00162\f\u0010\u001d\u001a\b\u0012\u0004\u0012\u00020\u00150\u001eJ\u001a\u0010\u001f\u001a\u00020\u00162\u0012\u0010 \u001a\u000e\u0012\u0004\u0012\u00020\u0015\u0012\u0004\u0012\u00020\u00160\u0014J\b\u0010!\u001a\u00020\u0016H\u0002J\b\u0010\"\u001a\u00020\u0016H\u0002J\b\u0010#\u001a\u00020\u0016H\u0002J\b\u0010$\u001a\u00020\u0016H\u0002R\u000e\u0010\t\u001a\u00020\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\r\u001a\u0004\u0018\u00010\u000eX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000f\u001a\u00020\u0007X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0010\u001a\u00020\u0011X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0012\u001a\u00020\u0011X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001c\u0010\u0013\u001a\u0010\u0012\u0004\u0012\u00020\u0015\u0012\u0004\u0012\u00020\u0016\u0018\u00010\u0014X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0017\u001a\u00020\u0018X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006&"}, d2 = {"Lcom/vugaenterprises/androidtv/ui/screens/FeaturedSliderView;", "Landroid/widget/LinearLayout;", "context", "Landroid/content/Context;", "attrs", "Landroid/util/AttributeSet;", "defStyleAttr", "", "(Landroid/content/Context;Landroid/util/AttributeSet;I)V", "adapter", "Lcom/vugaenterprises/androidtv/ui/components/FeaturedSliderAdapter;", "autoSlideHandler", "Landroid/os/Handler;", "autoSlideRunnable", "Ljava/lang/Runnable;", "currentPosition", "isAutoSliding", "", "isProgrammaticFocusChange", "onContentClick", "Lkotlin/Function1;", "Lcom/vugaenterprises/androidtv/data/model/Content;", "", "recyclerView", "Landroidx/recyclerview/widget/RecyclerView;", "onDetachedFromWindow", "pauseAutoSlide", "resumeAutoSlide", "setContent", "content", "", "setOnContentClick", "listener", "slideToNext", "startAutoSlide", "transferFocusToCurrentPosition", "updateCurrentPosition", "Companion", "app_debug"})
public final class FeaturedSliderView extends android.widget.LinearLayout {
    @org.jetbrains.annotations.NotNull()
    private final androidx.recyclerview.widget.RecyclerView recyclerView = null;
    @org.jetbrains.annotations.NotNull()
    private final com.vugaenterprises.androidtv.ui.components.FeaturedSliderAdapter adapter = null;
    @org.jetbrains.annotations.NotNull()
    private final android.os.Handler autoSlideHandler = null;
    @org.jetbrains.annotations.Nullable()
    private java.lang.Runnable autoSlideRunnable;
    private int currentPosition = 0;
    private boolean isAutoSliding = true;
    private boolean isProgrammaticFocusChange = false;
    @org.jetbrains.annotations.Nullable()
    private kotlin.jvm.functions.Function1<? super com.vugaenterprises.androidtv.data.model.Content, kotlin.Unit> onContentClick;
    private static final long AUTO_SLIDE_DELAY = 5000L;
    @org.jetbrains.annotations.NotNull()
    public static final com.vugaenterprises.androidtv.ui.screens.FeaturedSliderView.Companion Companion = null;
    
    @kotlin.jvm.JvmOverloads()
    public FeaturedSliderView(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.Nullable()
    android.util.AttributeSet attrs, int defStyleAttr) {
        super(null);
    }
    
    public final void setContent(@org.jetbrains.annotations.NotNull()
    java.util.List<com.vugaenterprises.androidtv.data.model.Content> content) {
    }
    
    public final void setOnContentClick(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super com.vugaenterprises.androidtv.data.model.Content, kotlin.Unit> listener) {
    }
    
    private final void startAutoSlide() {
    }
    
    private final void slideToNext() {
    }
    
    private final void pauseAutoSlide() {
    }
    
    private final void resumeAutoSlide() {
    }
    
    private final void updateCurrentPosition() {
    }
    
    private final void transferFocusToCurrentPosition() {
    }
    
    @java.lang.Override()
    protected void onDetachedFromWindow() {
    }
    
    @kotlin.jvm.JvmOverloads()
    public FeaturedSliderView(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super(null);
    }
    
    @kotlin.jvm.JvmOverloads()
    public FeaturedSliderView(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.Nullable()
    android.util.AttributeSet attrs) {
        super(null);
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\t\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0005"}, d2 = {"Lcom/vugaenterprises/androidtv/ui/screens/FeaturedSliderView$Companion;", "", "()V", "AUTO_SLIDE_DELAY", "", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}