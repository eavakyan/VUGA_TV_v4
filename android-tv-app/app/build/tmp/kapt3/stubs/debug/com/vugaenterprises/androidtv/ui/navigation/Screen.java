package com.vugaenterprises.androidtv.ui.navigation;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000<\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\r\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\b6\u0018\u00002\u00020\u0001:\n\u0007\b\t\n\u000b\f\r\u000e\u000f\u0010B\u000f\b\u0004\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006\u0082\u0001\n\u0011\u0012\u0013\u0014\u0015\u0016\u0017\u0018\u0019\u001a\u00a8\u0006\u001b"}, d2 = {"Lcom/vugaenterprises/androidtv/ui/navigation/Screen;", "", "route", "", "(Ljava/lang/String;)V", "getRoute", "()Ljava/lang/String;", "CastDetail", "ContentDetail", "ContentInfo", "EpisodeSelection", "Favorites", "History", "Home", "Profile", "Search", "VideoPlayer", "Lcom/vugaenterprises/androidtv/ui/navigation/Screen$CastDetail;", "Lcom/vugaenterprises/androidtv/ui/navigation/Screen$ContentDetail;", "Lcom/vugaenterprises/androidtv/ui/navigation/Screen$ContentInfo;", "Lcom/vugaenterprises/androidtv/ui/navigation/Screen$EpisodeSelection;", "Lcom/vugaenterprises/androidtv/ui/navigation/Screen$Favorites;", "Lcom/vugaenterprises/androidtv/ui/navigation/Screen$History;", "Lcom/vugaenterprises/androidtv/ui/navigation/Screen$Home;", "Lcom/vugaenterprises/androidtv/ui/navigation/Screen$Profile;", "Lcom/vugaenterprises/androidtv/ui/navigation/Screen$Search;", "Lcom/vugaenterprises/androidtv/ui/navigation/Screen$VideoPlayer;", "app_debug"})
public abstract class Screen {
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String route = null;
    
    private Screen(java.lang.String route) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getRoute() {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0018\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000b2\b\b\u0002\u0010\f\u001a\u00020\tR\u0017\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0006\u0010\u0007\u00a8\u0006\r"}, d2 = {"Lcom/vugaenterprises/androidtv/ui/navigation/Screen$CastDetail;", "Lcom/vugaenterprises/androidtv/ui/navigation/Screen;", "()V", "arguments", "", "Landroidx/navigation/NamedNavArgument;", "getArguments", "()Ljava/util/List;", "createRoute", "", "actorId", "", "characterName", "app_debug"})
    public static final class CastDetail extends com.vugaenterprises.androidtv.ui.navigation.Screen {
        @org.jetbrains.annotations.NotNull()
        private static final java.util.List<androidx.navigation.NamedNavArgument> arguments = null;
        @org.jetbrains.annotations.NotNull()
        public static final com.vugaenterprises.androidtv.ui.navigation.Screen.CastDetail INSTANCE = null;
        
        private CastDetail() {
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.util.List<androidx.navigation.NamedNavArgument> getArguments() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String createRoute(int actorId, @org.jetbrains.annotations.NotNull()
        java.lang.String characterName) {
            return null;
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000$\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0000\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000bR\u0017\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0006\u0010\u0007\u00a8\u0006\f"}, d2 = {"Lcom/vugaenterprises/androidtv/ui/navigation/Screen$ContentDetail;", "Lcom/vugaenterprises/androidtv/ui/navigation/Screen;", "()V", "arguments", "", "Landroidx/navigation/NamedNavArgument;", "getArguments", "()Ljava/util/List;", "createRoute", "", "contentId", "", "app_debug"})
    public static final class ContentDetail extends com.vugaenterprises.androidtv.ui.navigation.Screen {
        @org.jetbrains.annotations.NotNull()
        private static final java.util.List<androidx.navigation.NamedNavArgument> arguments = null;
        @org.jetbrains.annotations.NotNull()
        public static final com.vugaenterprises.androidtv.ui.navigation.Screen.ContentDetail INSTANCE = null;
        
        private ContentDetail() {
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.util.List<androidx.navigation.NamedNavArgument> getArguments() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String createRoute(int contentId) {
            return null;
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000$\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0000\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000bR\u0017\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0006\u0010\u0007\u00a8\u0006\f"}, d2 = {"Lcom/vugaenterprises/androidtv/ui/navigation/Screen$ContentInfo;", "Lcom/vugaenterprises/androidtv/ui/navigation/Screen;", "()V", "arguments", "", "Landroidx/navigation/NamedNavArgument;", "getArguments", "()Ljava/util/List;", "createRoute", "", "contentId", "", "app_debug"})
    public static final class ContentInfo extends com.vugaenterprises.androidtv.ui.navigation.Screen {
        @org.jetbrains.annotations.NotNull()
        private static final java.util.List<androidx.navigation.NamedNavArgument> arguments = null;
        @org.jetbrains.annotations.NotNull()
        public static final com.vugaenterprises.androidtv.ui.navigation.Screen.ContentInfo INSTANCE = null;
        
        private ContentInfo() {
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.util.List<androidx.navigation.NamedNavArgument> getArguments() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String createRoute(int contentId) {
            return null;
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000$\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0000\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000bR\u0017\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0006\u0010\u0007\u00a8\u0006\f"}, d2 = {"Lcom/vugaenterprises/androidtv/ui/navigation/Screen$EpisodeSelection;", "Lcom/vugaenterprises/androidtv/ui/navigation/Screen;", "()V", "arguments", "", "Landroidx/navigation/NamedNavArgument;", "getArguments", "()Ljava/util/List;", "createRoute", "", "contentId", "", "app_debug"})
    public static final class EpisodeSelection extends com.vugaenterprises.androidtv.ui.navigation.Screen {
        @org.jetbrains.annotations.NotNull()
        private static final java.util.List<androidx.navigation.NamedNavArgument> arguments = null;
        @org.jetbrains.annotations.NotNull()
        public static final com.vugaenterprises.androidtv.ui.navigation.Screen.EpisodeSelection INSTANCE = null;
        
        private EpisodeSelection() {
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.util.List<androidx.navigation.NamedNavArgument> getArguments() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String createRoute(int contentId) {
            return null;
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/vugaenterprises/androidtv/ui/navigation/Screen$Favorites;", "Lcom/vugaenterprises/androidtv/ui/navigation/Screen;", "()V", "app_debug"})
    public static final class Favorites extends com.vugaenterprises.androidtv.ui.navigation.Screen {
        @org.jetbrains.annotations.NotNull()
        public static final com.vugaenterprises.androidtv.ui.navigation.Screen.Favorites INSTANCE = null;
        
        private Favorites() {
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/vugaenterprises/androidtv/ui/navigation/Screen$History;", "Lcom/vugaenterprises/androidtv/ui/navigation/Screen;", "()V", "app_debug"})
    public static final class History extends com.vugaenterprises.androidtv.ui.navigation.Screen {
        @org.jetbrains.annotations.NotNull()
        public static final com.vugaenterprises.androidtv.ui.navigation.Screen.History INSTANCE = null;
        
        private History() {
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/vugaenterprises/androidtv/ui/navigation/Screen$Home;", "Lcom/vugaenterprises/androidtv/ui/navigation/Screen;", "()V", "app_debug"})
    public static final class Home extends com.vugaenterprises.androidtv.ui.navigation.Screen {
        @org.jetbrains.annotations.NotNull()
        public static final com.vugaenterprises.androidtv.ui.navigation.Screen.Home INSTANCE = null;
        
        private Home() {
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/vugaenterprises/androidtv/ui/navigation/Screen$Profile;", "Lcom/vugaenterprises/androidtv/ui/navigation/Screen;", "()V", "app_debug"})
    public static final class Profile extends com.vugaenterprises.androidtv.ui.navigation.Screen {
        @org.jetbrains.annotations.NotNull()
        public static final com.vugaenterprises.androidtv.ui.navigation.Screen.Profile INSTANCE = null;
        
        private Profile() {
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/vugaenterprises/androidtv/ui/navigation/Screen$Search;", "Lcom/vugaenterprises/androidtv/ui/navigation/Screen;", "()V", "app_debug"})
    public static final class Search extends com.vugaenterprises.androidtv.ui.navigation.Screen {
        @org.jetbrains.annotations.NotNull()
        public static final com.vugaenterprises.androidtv.ui.navigation.Screen.Search INSTANCE = null;
        
        private Search() {
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0006\u0010\u0003\u001a\u00020\u0004\u00a8\u0006\u0005"}, d2 = {"Lcom/vugaenterprises/androidtv/ui/navigation/Screen$VideoPlayer;", "Lcom/vugaenterprises/androidtv/ui/navigation/Screen;", "()V", "createRoute", "", "app_debug"})
    public static final class VideoPlayer extends com.vugaenterprises.androidtv.ui.navigation.Screen {
        @org.jetbrains.annotations.NotNull()
        public static final com.vugaenterprises.androidtv.ui.navigation.Screen.VideoPlayer INSTANCE = null;
        
        private VideoPlayer() {
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String createRoute() {
            return null;
        }
    }
}