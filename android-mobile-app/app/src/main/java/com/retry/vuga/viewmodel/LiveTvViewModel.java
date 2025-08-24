package com.retry.vuga.viewmodel;

import android.app.Application;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.retry.vuga.model.LiveTvCategory;
import com.retry.vuga.model.LiveTvChannel;
import com.retry.vuga.model.LiveTvResponse;
import com.retry.vuga.retrofit.RetrofitClient;
import com.retry.vuga.retrofit.RetrofitService;
import com.retry.vuga.utils.SessionManager;
import com.retry.vuga.model.UserRegistration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class LiveTvViewModel extends AndroidViewModel {
    
    private final MutableLiveData<List<LiveTvChannel>> channels = new MutableLiveData<>();
    private final MutableLiveData<List<LiveTvCategory>> categories = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> hasMoreChannels = new MutableLiveData<>(false);
    
    private final CompositeDisposable disposables = new CompositeDisposable();
    private final RetrofitService apiService;
    private SessionManager sessionManager;
    
    private int currentPage = 1;
    private boolean isLoadingMore = false;
    
    public LiveTvViewModel(@NonNull Application application) {
        super(application);
        apiService = RetrofitClient.getService();
        sessionManager = new SessionManager(application);
    }
    
    public LiveData<List<LiveTvChannel>> getChannels() {
        return channels;
    }
    
    public LiveData<List<LiveTvCategory>> getCategories() {
        return categories;
    }
    
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
    
    public LiveData<Boolean> hasMoreChannels() {
        return hasMoreChannels;
    }
    
    public LiveData<Boolean> isEmpty() {
        MutableLiveData<Boolean> isEmpty = new MutableLiveData<>();
        List<LiveTvChannel> currentChannels = channels.getValue();
        isEmpty.setValue(currentChannels == null || currentChannels.isEmpty());
        return isEmpty;
    }
    
    public void loadChannelsAndCategories() {
        isLoading.setValue(true);
        currentPage = 1;
        
        // Load categories - disabled for now as V2 endpoint not available on production
        // loadCategories();
        
        // Load channels
        loadChannels(false);
    }
    
    public void refreshChannels() {
        currentPage = 1;
        loadChannels(false);
    }
    
    public void loadMoreChannels() {
        if (!isLoadingMore && Boolean.TRUE.equals(hasMoreChannels.getValue())) {
            currentPage++;
            loadChannels(true);
        }
    }
    
    private void loadChannels(boolean append) {
        if (!append) {
            isLoading.setValue(true);
        }
        isLoadingMore = true;
        
        HashMap<String, Object> params = new HashMap<>();
        UserRegistration.Data user = sessionManager.getUser();
        int userId = user != null ? user.getId() : 0;
        params.put("userId", userId);
        params.put("page", currentPage);
        params.put("per_page", 20);
        
        disposables.add(
            apiService.getLiveTvChannelsWithPrograms(params)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    response -> {
                        Log.d("LiveTvViewModel", "API Response received");
                        if (response != null && response.getStatus()) {
                            Log.d("LiveTvViewModel", "Response status is true");
                            // Clear any previous error
                            errorMessage.setValue(null);
                            // Handle old API format - extract channels from categories
                            List<LiveTvChannel> allChannels = new ArrayList<>();
                            List<LiveTvCategory> categoriesData = response.getData();
                            
                            if (categoriesData != null) {
                                Log.d("LiveTvViewModel", "Categories found: " + categoriesData.size());
                                for (LiveTvCategory category : categoriesData) {
                                    List<LiveTvChannel> categoryChannels = category.getChannels();
                                    if (categoryChannels != null) {
                                        Log.d("LiveTvViewModel", "Category " + category.getName() + " has " + categoryChannels.size() + " channels");
                                        allChannels.addAll(categoryChannels);
                                    }
                                }
                            }
                            
                            // Also check if direct channels are available (new format)
                            List<LiveTvChannel> directChannels = response.getChannels();
                            if (directChannels != null && !directChannels.isEmpty()) {
                                allChannels = directChannels;
                            }
                            
                            Log.d("LiveTvViewModel", "Total channels found: " + allChannels.size());
                            if (!allChannels.isEmpty()) {
                                if (append) {
                                    List<LiveTvChannel> currentChannels = channels.getValue();
                                    if (currentChannels == null) {
                                        currentChannels = new ArrayList<>();
                                    }
                                    currentChannels.addAll(allChannels);
                                    channels.setValue(currentChannels);
                                } else {
                                    channels.setValue(allChannels);
                                }
                                
                                hasMoreChannels.setValue(allChannels.size() >= 20);
                            } else {
                                // No channels found - set empty list but don't show error
                                channels.setValue(new ArrayList<>());
                                hasMoreChannels.setValue(false);
                            }
                        } else if (response == null) {
                            // Only show error if response is null
                            errorMessage.setValue("Failed to load channels");
                        }
                        isLoading.setValue(false);
                        isLoadingMore = false;
                    },
                    error -> {
                        Log.e("LiveTvViewModel", "Error loading channels", error);
                        errorMessage.setValue("Failed to load channels");
                        isLoading.setValue(false);
                        isLoadingMore = false;
                    }
                )
        );
    }
    
    private void loadCategories() {
        disposables.add(
            apiService.getLiveTvCategories()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    response -> {
                        if (response != null && response.getStatus()) {
                            List<LiveTvCategory> categoryList = response.getCategories();
                            if (categoryList != null) {
                                categories.setValue(categoryList);
                            }
                        }
                    },
                    error -> {
                        // Silently fail for categories, they're not critical
                    }
                )
        );
    }
    
    public void trackChannelView(HashMap<String, Object> params) {
        // Fire and forget API call to track channel view
        disposables.add(
            apiService.trackLiveTvView(params)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    response -> {
                        // Success - no action needed
                    },
                    error -> {
                        // Silently fail - tracking is not critical
                    }
                )
        );
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear();
    }
}