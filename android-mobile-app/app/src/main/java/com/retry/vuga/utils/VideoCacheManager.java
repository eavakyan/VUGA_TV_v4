package com.retry.vuga.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.database.StandaloneDatabaseProvider;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.FileDataSource;
import com.google.android.exoplayer2.upstream.cache.CacheDataSink;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;

import java.io.File;

/**
 * Manages video caching for improved playback on poor network connections
 * Uses ExoPlayer's built-in caching with adaptive buffering
 */
public class VideoCacheManager {
    private static final String TAG = "VideoCacheManager";
    private static final long MAX_CACHE_SIZE = 200 * 1024 * 1024; // 200MB cache
    private static final String CACHE_DIR_NAME = "video_cache";
    
    private static VideoCacheManager instance;
    private SimpleCache simpleCache;
    private final Context context;
    private NetworkType currentNetworkType = NetworkType.UNKNOWN;
    
    public enum NetworkType {
        WIFI,
        CELLULAR,
        OFFLINE,
        UNKNOWN
    }
    
    private VideoCacheManager(Context context) {
        this.context = context.getApplicationContext();
        initializeCache();
        updateNetworkType();
    }
    
    public static synchronized VideoCacheManager getInstance(Context context) {
        if (instance == null) {
            instance = new VideoCacheManager(context);
        }
        return instance;
    }
    
    private void initializeCache() {
        try {
            File cacheDir = new File(context.getCacheDir(), CACHE_DIR_NAME);
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }
            
            // Use LRU eviction strategy with 200MB max size
            LeastRecentlyUsedCacheEvictor evictor = new LeastRecentlyUsedCacheEvictor(MAX_CACHE_SIZE);
            
            // Initialize the cache
            simpleCache = new SimpleCache(
                cacheDir,
                evictor,
                new StandaloneDatabaseProvider(context)
            );
            
            Log.d(TAG, "Video cache initialized with max size: " + MAX_CACHE_SIZE);
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize cache", e);
        }
    }
    
    /**
     * Creates a cache-enabled data source factory for ExoPlayer
     */
    public DataSource.Factory getCacheDataSourceFactory() {
        if (simpleCache == null) {
            Log.w(TAG, "Cache not initialized, returning default data source");
            return new DefaultDataSource.Factory(context);
        }
        
        // Create upstream data source (network)
        DefaultHttpDataSource.Factory httpDataSourceFactory = new DefaultHttpDataSource.Factory()
            .setConnectTimeoutMs(10000)
            .setReadTimeoutMs(10000)
            .setAllowCrossProtocolRedirects(true);
        
        // Create cache data source factory
        return new CacheDataSource.Factory()
            .setCache(simpleCache)
            .setUpstreamDataSourceFactory(new DefaultDataSource.Factory(context, httpDataSourceFactory))
            .setCacheWriteDataSinkFactory(new CacheDataSink.Factory().setCache(simpleCache))
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR | CacheDataSource.FLAG_IGNORE_CACHE_FOR_UNSET_LENGTH_REQUESTS);
    }
    
    /**
     * Creates adaptive LoadControl based on network conditions
     */
    public LoadControl getAdaptiveLoadControl() {
        updateNetworkType();
        
        DefaultLoadControl.Builder builder = new DefaultLoadControl.Builder();
        
        switch (currentNetworkType) {
            case WIFI:
                // WiFi: Aggressive buffering for smooth playback
                builder.setBufferDurationsMs(
                    15000,  // Min buffer: 15s
                    50000,  // Max buffer: 50s
                    2500,   // Buffer for playback: 2.5s
                    5000    // Buffer for rebuffer: 5s
                );
                break;
                
            case CELLULAR:
                // Cellular: Moderate buffering to balance data usage
                builder.setBufferDurationsMs(
                    10000,  // Min buffer: 10s
                    30000,  // Max buffer: 30s
                    2000,   // Buffer for playback: 2s
                    4000    // Buffer for rebuffer: 4s
                );
                break;
                
            case OFFLINE:
            case UNKNOWN:
            default:
                // Poor/Unknown: Conservative buffering
                builder.setBufferDurationsMs(
                    5000,   // Min buffer: 5s
                    15000,  // Max buffer: 15s
                    1500,   // Buffer for playback: 1.5s
                    3000    // Buffer for rebuffer: 3s
                );
                break;
        }
        
        builder.setTargetBufferBytes(C.LENGTH_UNSET);
        builder.setPrioritizeTimeOverSizeThresholds(true);
        
        Log.d(TAG, "LoadControl configured for network type: " + currentNetworkType);
        return builder.build();
    }
    
    /**
     * Updates the current network type
     */
    private void updateNetworkType() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            if (activeNetwork != null && activeNetwork.isConnected()) {
                if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                    currentNetworkType = NetworkType.WIFI;
                } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                    currentNetworkType = NetworkType.CELLULAR;
                } else {
                    currentNetworkType = NetworkType.UNKNOWN;
                }
            } else {
                currentNetworkType = NetworkType.OFFLINE;
            }
        }
    }
    
    /**
     * Gets the current network type
     */
    public NetworkType getCurrentNetworkType() {
        updateNetworkType();
        return currentNetworkType;
    }
    
    /**
     * Clears the video cache
     */
    public void clearCache() {
        try {
            if (simpleCache != null) {
                simpleCache.release();
                simpleCache = null;
            }
            
            File cacheDir = new File(context.getCacheDir(), CACHE_DIR_NAME);
            if (cacheDir.exists()) {
                deleteRecursive(cacheDir);
            }
            
            // Reinitialize cache after clearing
            initializeCache();
            
            Log.d(TAG, "Video cache cleared");
        } catch (Exception e) {
            Log.e(TAG, "Failed to clear cache", e);
        }
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
    
    /**
     * Gets the current cache size in bytes
     */
    public long getCacheSize() {
        File cacheDir = new File(context.getCacheDir(), CACHE_DIR_NAME);
        return getDirectorySize(cacheDir);
    }
    
    private long getDirectorySize(File directory) {
        long size = 0;
        if (directory != null && directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        size += getDirectorySize(file);
                    } else {
                        size += file.length();
                    }
                }
            }
        }
        return size;
    }
    
    /**
     * Release resources when done
     */
    public void release() {
        try {
            if (simpleCache != null) {
                simpleCache.release();
                simpleCache = null;
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to release cache", e);
        }
    }
}