package com.retry.vuga.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.retry.vuga.model.Profile;
import com.retry.vuga.model.ProfileResponse;
import com.retry.vuga.model.UserRegistration;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Simple profile caching manager for offline support
 */
public class ProfileCacheManager {
    private static final String PREF_NAME = "profile_cache";
    private static final String KEY_USER_DATA = "user_data";
    private static final String KEY_PROFILES = "profiles";
    private static final String KEY_LAST_SYNC = "last_sync";
    private static final long CACHE_VALIDITY = 24 * 60 * 60 * 1000; // 24 hours
    
    private SharedPreferences prefs;
    private Gson gson;
    
    public ProfileCacheManager(Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }
    
    // Save user data to cache
    public void saveUserData(UserRegistration.Data userData) {
        if (userData != null) {
            String json = gson.toJson(userData);
            prefs.edit()
                .putString(KEY_USER_DATA, json)
                .putLong(KEY_LAST_SYNC, System.currentTimeMillis())
                .apply();
        }
    }
    
    // Get cached user data
    public UserRegistration.Data getCachedUserData() {
        String json = prefs.getString(KEY_USER_DATA, null);
        if (json != null) {
            return gson.fromJson(json, UserRegistration.Data.class);
        }
        return null;
    }
    
    // Save profiles to cache
    public void saveProfiles(List<Profile> profiles) {
        if (profiles != null) {
            String json = gson.toJson(profiles);
            prefs.edit()
                .putString(KEY_PROFILES, json)
                .putLong(KEY_LAST_SYNC, System.currentTimeMillis())
                .apply();
        }
    }
    
    // Get cached profiles
    public List<Profile> getCachedProfiles() {
        String json = prefs.getString(KEY_PROFILES, null);
        if (json != null) {
            Type listType = new TypeToken<List<Profile>>(){}.getType();
            return gson.fromJson(json, listType);
        }
        return null;
    }
    
    // Check if cache is still valid
    public boolean isCacheValid() {
        long lastSync = prefs.getLong(KEY_LAST_SYNC, 0);
        return (System.currentTimeMillis() - lastSync) < CACHE_VALIDITY;
    }
    
    // Clear all cached data
    public void clearCache() {
        prefs.edit().clear().apply();
    }
    
    // Update last sync time
    public void updateLastSyncTime() {
        prefs.edit()
            .putLong(KEY_LAST_SYNC, System.currentTimeMillis())
            .apply();
    }
}