package com.retry.vuga.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.retry.vuga.model.ContentDetail;
import com.retry.vuga.model.HomePage;
import com.retry.vuga.model.Profile;
import com.retry.vuga.model.RecentlyWatchedContent;
import com.retry.vuga.model.UserRegistration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class OfflineDataManager {
    private static final String TAG = "OfflineDataManager";
    private static OfflineDataManager instance;
    
    private final Context context;
    private final SharedPreferences preferences;
    private final Gson gson;
    private final ConnectionMonitor connectionMonitor;
    
    // Cache directory and settings
    private static final String CACHE_DIR_NAME = "vuga_offline_cache";
    private static final String PREFS_NAME = "offline_data_prefs";
    
    // Cache TTL settings (in milliseconds)
    private static final Map<CacheType, Long> CACHE_TTL = new HashMap<>();
    static {
        CACHE_TTL.put(CacheType.USER, 86400000L);              // 24 hours
        CACHE_TTL.put(CacheType.PROFILES, 86400000L);          // 24 hours
        CACHE_TTL.put(CacheType.HOME_CONTENT, 3600000L);       // 1 hour
        CACHE_TTL.put(CacheType.WATCHLIST, 1800000L);          // 30 minutes
        CACHE_TTL.put(CacheType.RECENTLY_WATCHED, 1800000L);   // 30 minutes
        CACHE_TTL.put(CacheType.DOWNLOADED_CONTENT, 604800000L); // 7 days
        CACHE_TTL.put(CacheType.SETTINGS, 86400000L);          // 24 hours
        CACHE_TTL.put(CacheType.LANGUAGES, 604800000L);        // 7 days
        CACHE_TTL.put(CacheType.GENRES, 604800000L);           // 7 days
    }
    
    // Cache availability tracking
    private final Set<CacheType> cachedDataAvailable = ConcurrentHashMap.newKeySet();
    private final Map<OfflineDataListener, Boolean> listeners = new ConcurrentHashMap<>();
    
    public enum CacheType {
        USER,
        PROFILES,
        HOME_CONTENT,
        CONTENT_DETAIL,
        WATCHLIST,
        RECENTLY_WATCHED,
        DOWNLOADED_CONTENT,
        SETTINGS,
        LANGUAGES,
        GENRES
    }
    
    public enum CachePolicy {
        NETWORK_FIRST,      // Try network first, fallback to cache
        CACHE_FIRST,        // Use cache first, update in background
        NETWORK_ONLY,       // Network only, fail if no connection
        CACHE_ONLY          // Cache only, never make network calls
    }
    
    public interface OfflineDataListener {
        void onOfflineModeChanged(boolean isOfflineMode, String message);
        void onCachedDataChanged(Set<CacheType> availableData);
    }
    
    public static class CacheEntry<T> {
        private final T data;
        private final long timestamp;
        private final long expiryTime;
        
        public CacheEntry(T data, long ttl) {
            this.data = data;
            this.timestamp = System.currentTimeMillis();
            this.expiryTime = timestamp + ttl;
        }
        
        public T getData() { return data; }
        public long getTimestamp() { return timestamp; }
        public boolean isExpired() { return System.currentTimeMillis() > expiryTime; }
        public long getAge() { return System.currentTimeMillis() - timestamp; }
    }
    
    private OfflineDataManager(Context context) {
        this.context = context.getApplicationContext();
        this.preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
        this.connectionMonitor = ConnectionMonitor.getInstance(context);
        
        setupCacheDirectory();
        updateCachedDataAvailability();
        monitorNetworkStatus();
    }
    
    public static synchronized OfflineDataManager getInstance(Context context) {
        if (instance == null) {
            instance = new OfflineDataManager(context);
        }
        return instance;
    }
    
    public static OfflineDataManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("OfflineDataManager not initialized. Call getInstance(Context) first.");
        }
        return instance;
    }
    
    private void setupCacheDirectory() {
        File cacheDir = getCacheDirectory();
        if (!cacheDir.exists()) {
            boolean created = cacheDir.mkdirs();
            Log.d(TAG, "Cache directory created: " + created);
        }
    }
    
    private File getCacheDirectory() {
        return new File(context.getCacheDir(), CACHE_DIR_NAME);
    }
    
    private void monitorNetworkStatus() {
        connectionMonitor.addListener(new ConnectionMonitor.ConnectionListener() {
            @Override
            public void onConnectionChanged(boolean isConnected, ConnectionMonitor.ConnectionType type, ConnectionMonitor.ConnectionQuality quality) {
                updateOfflineStatus(isConnected, quality);
            }
            
            @Override
            public void onConnectionAlert(boolean show, String message) {
                // Handle connection alerts if needed
            }
        });
    }
    
    private void updateOfflineStatus(boolean isConnected, ConnectionMonitor.ConnectionQuality quality) {
        boolean isOfflineMode = !isConnected;
        String message = "";
        
        if (!isConnected) {
            message = "You're offline. Using cached data.";
        } else if (quality == ConnectionMonitor.ConnectionQuality.POOR) {
            message = "Slow connection. Some features may be limited.";
        }
        
        notifyOfflineModeChanged(isOfflineMode, message);
    }
    
    // MARK: - Generic Cache Operations
    
    public <T> void cache(T data, CacheType type, Long customTTL) {
        long ttl = customTTL != null ? customTTL : CACHE_TTL.getOrDefault(type, 3600000L);
        CacheEntry<T> entry = new CacheEntry<>(data, ttl);
        
        try {
            String json = gson.toJson(entry);
            File cacheFile = getCacheFile(type, null);
            
            try (FileOutputStream fos = new FileOutputStream(cacheFile)) {
                fos.write(json.getBytes());
                cachedDataAvailable.add(type);
                notifyCachedDataChanged();
                Log.d(TAG, "Cached " + type + " successfully");
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to cache " + type + ": " + e.getMessage());
        }
    }
    
    public <T> void cache(T data, CacheType type) {
        cache(data, type, null);
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getCachedData(CacheType type, Class<T> dataType) {
        return getCachedData(type, null, dataType);
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getCachedData(CacheType type, String identifier, Class<T> dataType) {
        File cacheFile = getCacheFile(type, identifier);
        
        if (!cacheFile.exists()) {
            return null;
        }
        
        try (FileInputStream fis = new FileInputStream(cacheFile)) {
            byte[] data = new byte[(int) cacheFile.length()];
            fis.read(data);
            String json = new String(data);
            
            // Use TypeToken for generic CacheEntry
            TypeToken<CacheEntry<T>> typeToken = new TypeToken<CacheEntry<T>>() {};
            CacheEntry<T> entry = gson.fromJson(json, typeToken.getType());
            
            if (entry.isExpired()) {
                clearCache(type, identifier);
                return null;
            }
            
            return entry.getData();
        } catch (Exception e) {
            Log.e(TAG, "Failed to load cached " + type + ": " + e.getMessage());
            return null;
        }
    }
    
    public void clearCache(CacheType type) {
        clearCache(type, null);
    }
    
    public void clearCache(CacheType type, String identifier) {
        File cacheFile = getCacheFile(type, identifier);
        if (cacheFile.exists()) {
            boolean deleted = cacheFile.delete();
            if (deleted && identifier == null) {
                cachedDataAvailable.remove(type);
                notifyCachedDataChanged();
            }
            Log.d(TAG, "Cleared cache for " + type + ": " + deleted);
        }
    }
    
    public void clearAllCache() {
        File cacheDir = getCacheDirectory();
        deleteRecursive(cacheDir);
        setupCacheDirectory();
        cachedDataAvailable.clear();
        notifyCachedDataChanged();
        Log.d(TAG, "Cleared all cache");
    }
    
    private void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            File[] children = fileOrDirectory.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursive(child);
                }
            }
        }
        fileOrDirectory.delete();
    }
    
    private File getCacheFile(CacheType type, String identifier) {
        String filename;
        switch (type) {
            case USER:
                filename = "user.json";
                break;
            case PROFILES:
                filename = "profiles.json";
                break;
            case HOME_CONTENT:
                filename = "homeContent.json";
                break;
            case CONTENT_DETAIL:
                filename = "content_" + (identifier != null ? identifier : "unknown") + ".json";
                break;
            case WATCHLIST:
                filename = "watchlist.json";
                break;
            case RECENTLY_WATCHED:
                filename = "recentlyWatched.json";
                break;
            case DOWNLOADED_CONTENT:
                filename = "downloadedContent.json";
                break;
            case SETTINGS:
                filename = "settings.json";
                break;
            case LANGUAGES:
                filename = "languages.json";
                break;
            case GENRES:
                filename = "genres.json";
                break;
            default:
                filename = "unknown.json";
        }
        return new File(getCacheDirectory(), filename);
    }
    
    // MARK: - Specific Data Caching Methods
    
    public void cacheUser(UserRegistration.Data user) {
        cache(user, CacheType.USER);
    }
    
    public UserRegistration.Data getCachedUser() {
        return getCachedData(CacheType.USER, UserRegistration.Data.class);
    }
    
    public void cacheProfiles(List<Profile> profiles) {
        cache(profiles, CacheType.PROFILES);
    }
    
    @SuppressWarnings("unchecked")
    public List<Profile> getCachedProfiles() {
        Object cachedData = getCachedData(CacheType.PROFILES, Object.class);
        if (cachedData != null) {
            return gson.fromJson(gson.toJson(cachedData), new TypeToken<List<Profile>>(){}.getType());
        }
        return null;
    }
    
    public void cacheHomeContent(HomePage homeData) {
        cache(homeData, CacheType.HOME_CONTENT);
    }
    
    public HomePage getCachedHomeContent() {
        return getCachedData(CacheType.HOME_CONTENT, HomePage.class);
    }
    
    public void cacheContentDetail(ContentDetail content, int id) {
        cache(content, CacheType.CONTENT_DETAIL, null);
        
        // Also store the mapping
        preferences.edit()
                .putString("content_detail_" + id, String.valueOf(id))
                .apply();
    }
    
    public ContentDetail getCachedContentDetail(int id) {
        return getCachedData(CacheType.CONTENT_DETAIL, String.valueOf(id), ContentDetail.class);
    }
    
    public void cacheRecentlyWatched(List<RecentlyWatchedContent> recentlyWatched) {
        cache(recentlyWatched, CacheType.RECENTLY_WATCHED);
    }
    
    @SuppressWarnings("unchecked")
    public List<RecentlyWatchedContent> getCachedRecentlyWatched() {
        Object cachedData = getCachedData(CacheType.RECENTLY_WATCHED, Object.class);
        if (cachedData != null) {
            return gson.fromJson(gson.toJson(cachedData), new TypeToken<List<RecentlyWatchedContent>>(){}.getType());
        }
        return new ArrayList<>();
    }
    
    // MARK: - Cache Statistics and Management
    
    private void updateCachedDataAvailability() {
        cachedDataAvailable.clear();
        
        for (CacheType type : CacheType.values()) {
            File cacheFile = getCacheFile(type, null);
            if (cacheFile.exists()) {
                // Check if cache is not expired
                try {
                    Object data = getCachedData(type, Object.class);
                    if (data != null) {
                        cachedDataAvailable.add(type);
                    }
                } catch (Exception e) {
                    // Cache file might be corrupted, remove it
                    cacheFile.delete();
                }
            }
        }
        
        notifyCachedDataChanged();
    }
    
    public long getCacheSize() {
        File cacheDir = getCacheDirectory();
        return calculateDirectorySize(cacheDir);
    }
    
    private long calculateDirectorySize(File directory) {
        long size = 0;
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        size += calculateDirectorySize(file);
                    } else {
                        size += file.length();
                    }
                }
            }
        } else {
            size = directory.length();
        }
        return size;
    }
    
    public String getCacheSizeFormatted() {
        long bytes = getCacheSize();
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
    }
    
    public boolean isDataCached(CacheType type) {
        return cachedDataAvailable.contains(type);
    }
    
    public Long getCacheAge(CacheType type) {
        return getCacheAge(type, null);
    }
    
    public Long getCacheAge(CacheType type, String identifier) {
        File cacheFile = getCacheFile(type, identifier);
        
        if (!cacheFile.exists()) {
            return null;
        }
        
        try (FileInputStream fis = new FileInputStream(cacheFile)) {
            byte[] data = new byte[(int) cacheFile.length()];
            fis.read(data);
            String json = new String(data);
            
            TypeToken<CacheEntry<Object>> typeToken = new TypeToken<CacheEntry<Object>>() {};
            CacheEntry<Object> entry = gson.fromJson(json, typeToken.getType());
            
            return entry.getAge();
        } catch (Exception e) {
            return null;
        }
    }
    
    public boolean shouldRefreshCache(CacheType type, long maxAge) {
        Long age = getCacheAge(type);
        return age == null || age > maxAge;
    }
    
    // MARK: - Offline Content Suggestions
    
    public List<String> getOfflineContentSuggestions() {
        List<String> suggestions = new ArrayList<>();
        
        if (cachedDataAvailable.contains(CacheType.DOWNLOADED_CONTENT)) {
            suggestions.add("View your downloaded movies and shows");
        }
        
        if (cachedDataAvailable.contains(CacheType.RECENTLY_WATCHED)) {
            suggestions.add("Resume watching from your history");
        }
        
        if (cachedDataAvailable.contains(CacheType.WATCHLIST)) {
            suggestions.add("Browse your watchlist");
        }
        
        if (cachedDataAvailable.contains(CacheType.PROFILES)) {
            suggestions.add("Switch between user profiles");
        }
        
        if (suggestions.isEmpty()) {
            suggestions.add("Download content for offline viewing");
        }
        
        return suggestions;
    }
    
    // MARK: - Listener Management
    
    public void addListener(OfflineDataListener listener) {
        listeners.put(listener, true);
    }
    
    public void removeListener(OfflineDataListener listener) {
        listeners.remove(listener);
    }
    
    private void notifyOfflineModeChanged(boolean isOfflineMode, String message) {
        for (OfflineDataListener listener : listeners.keySet()) {
            try {
                listener.onOfflineModeChanged(isOfflineMode, message);
            } catch (Exception e) {
                Log.e(TAG, "Error notifying listener: " + e.getMessage());
            }
        }
    }
    
    private void notifyCachedDataChanged() {
        Set<CacheType> copy = new HashSet<>(cachedDataAvailable);
        for (OfflineDataListener listener : listeners.keySet()) {
            try {
                listener.onCachedDataChanged(copy);
            } catch (Exception e) {
                Log.e(TAG, "Error notifying listener: " + e.getMessage());
            }
        }
    }
    
    // MARK: - Utility Methods
    
    public boolean isOfflineMode() {
        return !connectionMonitor.isConnected();
    }
    
    public ConnectionMonitor.ConnectionQuality getConnectionQuality() {
        return connectionMonitor.getConnectionQuality();
    }
    
    public Set<CacheType> getCachedDataAvailable() {
        return new HashSet<>(cachedDataAvailable);
    }
}