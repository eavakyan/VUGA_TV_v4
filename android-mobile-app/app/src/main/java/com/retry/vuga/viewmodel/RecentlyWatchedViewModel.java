package com.retry.vuga.viewmodel;

import android.content.Context;
import android.util.Log;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.retry.vuga.model.RecentlyWatchedContent;
import com.retry.vuga.retrofit.RetrofitClient;
import com.retry.vuga.utils.SessionManager;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RecentlyWatchedViewModel extends ViewModel {
    private static final String TAG = "RecentlyWatchedVM";
    
    private CompositeDisposable disposables = new CompositeDisposable();
    private SessionManager sessionManager;
    
    // LiveData for UI observation
    private MutableLiveData<List<RecentlyWatchedContent.DataItem>> recentlyWatchedContents = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    
    public RecentlyWatchedViewModel(Context context) {
        this.sessionManager = new SessionManager(context);
        this.isLoading.setValue(false);
        this.recentlyWatchedContents.setValue(new ArrayList<>());
    }
    
    /**
     * Fetches recently watched content from API using content IDs
     * This mimics the iOS implementation that gets content IDs from local storage
     * and then fetches full content details from the API
     */
    public void fetchRecentlyWatchedFromAPI() {
        Log.d(TAG, "fetchRecentlyWatchedFromAPI called");
        
        // Get recently watched content IDs from local storage (SharedPreferences)
        // In a real implementation, you'd have these stored locally like iOS does with Core Data
        List<Integer> contentIds = getLocallyStoredContentIds();
        
        if (contentIds.isEmpty()) {
            Log.d(TAG, "No content IDs found, clearing recently watched");
            recentlyWatchedContents.setValue(new ArrayList<>());
            return;
        }
        
        // Fetch content details from API
        fetchContentDetails(contentIds);
    }
    
    /**
     * Gets locally stored content IDs (simulates Core Data functionality from iOS)
     * In a real implementation, this would read from a local database or SharedPreferences
     */
    private List<Integer> getLocallyStoredContentIds() {
        // TODO: Implement actual local storage reading
        // For now, returning empty list - you would implement this based on your app's
        // local storage mechanism (Room database, SharedPreferences, etc.)
        
        // Example implementation using SharedPreferences:
        // SharedPreferences prefs = context.getSharedPreferences("recently_watched", Context.MODE_PRIVATE);
        // String contentIdsJson = prefs.getString("content_ids", "[]");
        // Type listType = new TypeToken<List<Integer>>(){}.getType();
        // return gson.fromJson(contentIdsJson, listType);
        
        return new ArrayList<>();
    }
    
    /**
     * Saves content ID to local storage when user watches content
     */
    public void addToRecentlyWatched(int contentId) {
        // TODO: Implement local storage saving
        // This would save the content ID to local storage (SharedPreferences, Room, etc.)
        // and then refresh the recently watched list
        
        Log.d(TAG, "Adding content ID to recently watched: " + contentId);
        
        // After saving locally, refresh the list
        fetchRecentlyWatchedFromAPI();
    }
    
    /**
     * Clears all recently watched content
     */
    public void clearRecentlyWatched() {
        // TODO: Implement local storage clearing
        Log.d(TAG, "Clearing all recently watched content");
        
        recentlyWatchedContents.setValue(new ArrayList<>());
    }
    
    /**
     * Fetches content details from API using content IDs
     */
    private void fetchContentDetails(List<Integer> contentIds) {
        Log.d(TAG, "fetchContentDetails called with " + contentIds.size() + " IDs");
        
        if (contentIds.isEmpty()) {
            recentlyWatchedContents.setValue(new ArrayList<>());
            return;
        }
        
        isLoading.setValue(true);
        
        // Convert content IDs to comma-separated string
        StringBuilder contentIdsStr = new StringBuilder();
        for (int i = 0; i < contentIds.size(); i++) {
            contentIdsStr.append(contentIds.get(i));
            if (i < contentIds.size() - 1) {
                contentIdsStr.append(",");
            }
        }
        
        // Get user and profile info
        int userId = sessionManager.getUser() != null ? sessionManager.getUser().getId() : 0;
        Integer profileId = sessionManager.getUser() != null ? sessionManager.getUser().getLastActiveProfileId() : null;
        
        Log.d(TAG, "API call - contentIds: " + contentIdsStr.toString() + ", userId: " + userId + ", profileId: " + profileId);
        
        // Make API call to fetch content details
        disposables.add(
            RetrofitClient.getService().getRecentlyWatchedContent(
                contentIdsStr.toString(),
                userId,
                profileId
            )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                response -> {
                    isLoading.setValue(false);
                    
                    if (response.getStatus() && response.getData() != null) {
                        Log.d(TAG, "Successfully fetched " + response.getData().size() + " items");
                        
                        // Remove duplicates and sort by most recent
                        List<RecentlyWatchedContent.DataItem> uniqueItems = removeDuplicatesAndSort(response.getData());
                        recentlyWatchedContents.setValue(uniqueItems);
                    } else {
                        Log.d(TAG, "API response status false or no data");
                        errorMessage.setValue(response.getMessage() != null ? response.getMessage() : "Failed to fetch recently watched content");
                    }
                },
                error -> {
                    isLoading.setValue(false);
                    Log.e(TAG, "Error fetching recently watched content", error);
                    errorMessage.setValue("Network error: " + error.getMessage());
                }
            )
        );
    }
    
    /**
     * Removes duplicate content and sorts by watch date
     */
    private List<RecentlyWatchedContent.DataItem> removeDuplicatesAndSort(List<RecentlyWatchedContent.DataItem> items) {
        // Use Set to track seen content IDs
        Set<Integer> seenIds = new HashSet<>();
        List<RecentlyWatchedContent.DataItem> uniqueItems = new ArrayList<>();
        
        for (RecentlyWatchedContent.DataItem item : items) {
            if (!seenIds.contains(item.getContentId())) {
                seenIds.add(item.getContentId());
                uniqueItems.add(item);
            }
        }
        
        // Sort by watched_at date (most recent first)
        // Note: You might need to implement proper date parsing based on your date format
        Collections.sort(uniqueItems, (item1, item2) -> {
            // Simple string comparison - you might want to parse actual dates
            return item2.getWatchedAt().compareTo(item1.getWatchedAt());
        });
        
        return uniqueItems;
    }
    
    // Getters for LiveData
    public MutableLiveData<List<RecentlyWatchedContent.DataItem>> getRecentlyWatchedContents() {
        return recentlyWatchedContents;
    }
    
    public MutableLiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    
    public MutableLiveData<String> getErrorMessage() {
        return errorMessage;
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear();
    }
}