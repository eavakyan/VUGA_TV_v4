package com.retry.vuga.utils;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.retry.vuga.model.ContentDetail;
import com.retry.vuga.model.UserRegistration;
import com.retry.vuga.model.history.MovieHistory;
import com.retry.vuga.retrofit.RetrofitClient;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Helper class to sync watch history with the server
 * Provides simple sync functionality that can be called from anywhere
 */
public class WatchHistorySyncHelper {
    private static final String TAG = "WatchHistorySync";
    private static final long DEBOUNCE_INTERVAL = 5 * 60 * 1000; // 5 minutes
    
    private static WatchHistorySyncHelper instance;
    private final SessionManager sessionManager;
    private final CompositeDisposable disposables = new CompositeDisposable();
    private long lastSyncTime = 0;
    private boolean isSyncing = false;
    
    private WatchHistorySyncHelper(Context context) {
        this.sessionManager = new SessionManager(context);
    }
    
    public static synchronized WatchHistorySyncHelper getInstance(Context context) {
        if (instance == null) {
            instance = new WatchHistorySyncHelper(context.getApplicationContext());
        }
        return instance;
    }
    
    /**
     * Trigger a debounced sync - used when watch history is updated
     */
    public void triggerSync() {
        // Check if we should sync (debounce mechanism)
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastSyncTime < DEBOUNCE_INTERVAL) {
            Log.d(TAG, "Debouncing sync, last sync too recent");
            return;
        }
        
        // Check if user is logged in and has active profile
        UserRegistration.Data user = sessionManager.getUser();
        if (user == null || user.getLastActiveProfileId() == null || user.getLastActiveProfileId() <= 0) {
            Log.d(TAG, "No user or profile, skipping sync");
            return;
        }
        
        lastSyncTime = currentTime;
        performSync();
    }
    
    /**
     * Force sync - used when app is closing
     */
    public void forceSync() {
        UserRegistration.Data user = sessionManager.getUser();
        if (user == null || user.getLastActiveProfileId() == null || user.getLastActiveProfileId() <= 0) {
            return;
        }
        
        performSync();
    }
    
    private void performSync() {
        if (isSyncing) {
            Log.d(TAG, "Sync already in progress");
            return;
        }
        
        isSyncing = true;
        
        try {
            UserRegistration.Data user = sessionManager.getUser();
            if (user == null || user.getLastActiveProfileId() == null) {
                isSyncing = false;
                return;
            }
            
            Integer profileId = user.getLastActiveProfileId();
            
            // Get local watch history
            ArrayList<MovieHistory> localHistory = sessionManager.getMovieHistories();
            if (localHistory.isEmpty()) {
                Log.d(TAG, "No local watch history to sync");
                isSyncing = false;
                return;
            }
            
            // Convert to sync format
            JsonArray syncItems = convertToSyncFormat(localHistory);
            if (syncItems.size() == 0) {
                Log.d(TAG, "No valid items to sync");
                isSyncing = false;
                return;
            }
            
            // Create request body with replace mode
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("profile_id", profileId);
            requestBody.add("watch_history", syncItems);
            requestBody.addProperty("sync_mode", "replace"); // Replace all server items with current local state
            
            Log.d(TAG, "Syncing " + syncItems.size() + " items for profile " + profileId + " with replace mode");
            Log.d(TAG, "Request body: " + requestBody.toString());
            
            // Make API call
            disposables.add(
                RetrofitClient.getService().syncWatchHistory(requestBody)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        response -> {
                            isSyncing = false;
                            if (response.isSuccessful() && response.body() != null) {
                                JsonObject responseBody = response.body();
                                boolean status = responseBody.has("status") && responseBody.get("status").getAsBoolean();
                                
                                if (status) {
                                    Log.i(TAG, "Watch history sync successful");
                                    
                                    // Log sync details if available
                                    if (responseBody.has("data") && responseBody.get("data").isJsonObject()) {
                                        JsonObject data = responseBody.getAsJsonObject("data");
                                        int deleted = data.has("deleted") ? data.get("deleted").getAsInt() : 0;
                                        int synced = data.has("synced_new") ? data.get("synced_new").getAsInt() : 0;
                                        int updated = data.has("updated_existing") ? data.get("updated_existing").getAsInt() : 0;
                                        Log.i(TAG, "Sync result - deleted: " + deleted + ", synced: " + synced + ", updated: " + updated);
                                    }
                                    
                                    // Save last sync timestamp
                                    sessionManager.saveStringValue("last_watch_history_sync", 
                                        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
                                } else {
                                    String message = responseBody.has("message") ? 
                                        responseBody.get("message").getAsString() : "Unknown error";
                                    Log.e(TAG, "Watch history sync failed: " + message);
                                }
                            } else {
                                Log.e(TAG, "Watch history sync request failed");
                            }
                        },
                        error -> {
                            isSyncing = false;
                            Log.e(TAG, "Watch history sync error", error);
                        }
                    )
            );
            
        } catch (Exception e) {
            isSyncing = false;
            Log.e(TAG, "Error during watch history sync", e);
        }
    }
    
    private JsonArray convertToSyncFormat(ArrayList<MovieHistory> localHistory) {
        JsonArray syncItems = new JsonArray();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        
        for (MovieHistory history : localHistory) {
            if (history.getMovieId() == null || history.getSources() == null || history.getSources().isEmpty()) {
                continue;
            }
            
            // Get the most recent source (last watched)
            ArrayList<ContentDetail.SourceItem> sources = history.getSources();
            ContentDetail.SourceItem latestSource = sources.get(sources.size() - 1);
            
            if (latestSource == null || latestSource.getPlayProgress() <= 0) {
                continue;
            }
            
            JsonObject syncItem = new JsonObject();
            syncItem.addProperty("content_id", history.getMovieId());
            syncItem.addProperty("last_watched_position", latestSource.getPlayProgress());
            
            // Duration is not available in SourceItem model, use 0 as default
            // The server will handle it based on content metadata
            syncItem.addProperty("total_duration", 0);
            syncItem.addProperty("completed", false);
            
            syncItem.addProperty("device_type", 1); // 1 = mobile
            
            // Add timestamp
            if (history.getTime() != null) {
                syncItem.addProperty("watched_at", dateFormat.format(new Date(history.getTime())));
            } else {
                syncItem.addProperty("watched_at", dateFormat.format(new Date()));
            }
            
            syncItems.add(syncItem);
        }
        
        return syncItems;
    }
    
    public void cleanup() {
        disposables.clear();
    }
}