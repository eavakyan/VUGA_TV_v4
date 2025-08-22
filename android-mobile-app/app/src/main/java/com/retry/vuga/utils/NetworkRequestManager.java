package com.retry.vuga.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class NetworkRequestManager {
    private static final String TAG = "NetworkRequestManager";
    private static NetworkRequestManager instance;
    
    private final Context context;
    private final OkHttpClient httpClient;
    private final ConnectionMonitor connectionMonitor;
    private final OfflineDataManager offlineDataManager;
    private final Handler mainHandler;
    private final ExecutorService executor;
    private final Gson gson;
    
    // Request management
    private final Set<String> activeRequests = ConcurrentHashMap.newKeySet();
    private static final int MAX_CONCURRENT_REQUESTS = 5;
    
    // Retry configuration
    public static class RetryConfig {
        public final int maxRetries;
        public final long baseDelay;
        public final long maxDelay;
        public final double backoffMultiplier;
        public final Set<Integer> retryableStatusCodes;
        
        public RetryConfig(int maxRetries, long baseDelay, long maxDelay, double backoffMultiplier, Set<Integer> retryableStatusCodes) {
            this.maxRetries = maxRetries;
            this.baseDelay = baseDelay;
            this.maxDelay = maxDelay;
            this.backoffMultiplier = backoffMultiplier;
            this.retryableStatusCodes = retryableStatusCodes;
        }
        
        public static RetryConfig getDefault() {
            Set<Integer> codes = ConcurrentHashMap.newKeySet();
            codes.add(408);
            codes.add(429);
            codes.add(500);
            codes.add(502);
            codes.add(503);
            codes.add(504);
            
            return new RetryConfig(3, 1000, 10000, 2.0, codes);
        }
        
        public static RetryConfig getAggressive() {
            Set<Integer> codes = ConcurrentHashMap.newKeySet();
            codes.add(408);
            codes.add(429);
            codes.add(500);
            codes.add(502);
            codes.add(503);
            codes.add(504);
            
            return new RetryConfig(5, 500, 15000, 1.5, codes);
        }
    }
    
    public enum NetworkError {
        NO_CONNECTION("No internet connection. Please check your network settings."),
        SLOW_CONNECTION("Connection is very slow. Some features may not work properly."),
        SERVER_ERROR("Server error. Please try again later."),
        TIMEOUT("Request timed out. Please check your connection."),
        PARSING_ERROR("Unable to process server response."),
        UNKNOWN("An unknown error occurred.");
        
        private final String message;
        
        NetworkError(String message) {
            this.message = message;
        }
        
        public String getMessage() {
            return message;
        }
    }
    
    public interface NetworkCallback<T> {
        void onSuccess(T data);
        void onFailure(NetworkError error, String message);
    }
    
    private NetworkRequestManager(Context context) {
        this.context = context.getApplicationContext();
        this.connectionMonitor = ConnectionMonitor.getInstance(context);
        this.offlineDataManager = OfflineDataManager.getInstance(context);
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.executor = Executors.newFixedThreadPool(4);
        this.gson = new Gson();
        
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }
    
    public static synchronized NetworkRequestManager getInstance(Context context) {
        if (instance == null) {
            instance = new NetworkRequestManager(context);
        }
        return instance;
    }
    
    public static NetworkRequestManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("NetworkRequestManager not initialized. Call getInstance(Context) first.");
        }
        return instance;
    }
    
    // MARK: - Main Request Method
    
    public <T> void request(
            String url,
            Map<String, String> parameters,
            OfflineDataManager.CacheType cacheType,
            OfflineDataManager.CachePolicy cachePolicy,
            Class<T> responseType,
            NetworkCallback<T> callback
    ) {
        request(url, parameters, cacheType, cachePolicy, responseType, RetryConfig.getDefault(), callback);
    }
    
    public <T> void request(
            String url,
            Map<String, String> parameters,
            OfflineDataManager.CacheType cacheType,
            OfflineDataManager.CachePolicy cachePolicy,
            Class<T> responseType,
            RetryConfig retryConfig,
            NetworkCallback<T> callback
    ) {
        String requestKey = url + "_" + parameters.hashCode();
        
        // Check cache policy
        if (cacheType != null) {
            switch (cachePolicy) {
                case CACHE_ONLY:
                    handleCacheOnlyRequest(cacheType, responseType, callback);
                    return;
                    
                case CACHE_FIRST:
                    T cachedData = offlineDataManager.getCachedData(cacheType, responseType);
                    if (cachedData != null) {
                        callback.onSuccess(cachedData);
                        // Update in background if network is available
                        if (connectionMonitor.isConnected()) {
                            performBackgroundUpdate(url, parameters, cacheType, responseType, retryConfig);
                        }
                        return;
                    }
                    // Fall through to network request if no cache
                    break;
                    
                case NETWORK_FIRST:
                    // Check connection quality first
                    if (!connectionMonitor.isConnected()) {
                        handleOfflineRequest(cacheType, responseType, callback);
                        return;
                    }
                    break;
                    
                case NETWORK_ONLY:
                    if (!connectionMonitor.isConnected()) {
                        callback.onFailure(NetworkError.NO_CONNECTION, NetworkError.NO_CONNECTION.getMessage());
                        return;
                    }
                    break;
            }
        }
        
        // Proceed with network request
        executeNetworkRequest(url, parameters, requestKey, cacheType, responseType, retryConfig, 0, callback);
    }
    
    private <T> void executeNetworkRequest(
            String url,
            Map<String, String> parameters,
            String requestKey,
            OfflineDataManager.CacheType cacheType,
            Class<T> responseType,
            RetryConfig retryConfig,
            int retryCount,
            NetworkCallback<T> callback
    ) {
        // Check for too many concurrent requests
        if (activeRequests.size() >= MAX_CONCURRENT_REQUESTS) {
            mainHandler.postDelayed(() -> 
                executeNetworkRequest(url, parameters, requestKey, cacheType, responseType, retryConfig, retryCount, callback), 
                1000
            );
            return;
        }
        
        activeRequests.add(requestKey);
        
        // Build request
        FormBody.Builder formBuilder = new FormBody.Builder();
        if (parameters != null) {
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                formBuilder.add(entry.getKey(), entry.getValue());
            }
        }
        
        Request request = new Request.Builder()
                .url(url)
                .post(formBuilder.build())
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();
        
        long startTime = System.currentTimeMillis();
        
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                activeRequests.remove(requestKey);
                handleRequestFailure(e, null, url, parameters, requestKey, cacheType, responseType, retryConfig, retryCount, callback);
            }
            
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                activeRequests.remove(requestKey);
                
                long duration = System.currentTimeMillis() - startTime;
                connectionMonitor.recordResponseTime(duration);
                
                try (ResponseBody responseBody = response.body()) {
                    if (response.isSuccessful() && responseBody != null) {
                        handleSuccessResponse(responseBody.string(), cacheType, responseType, callback);
                    } else {
                        handleRequestFailure(null, response, url, parameters, requestKey, cacheType, responseType, retryConfig, retryCount, callback);
                    }
                } catch (Exception e) {
                    handleRequestFailure(e, response, url, parameters, requestKey, cacheType, responseType, retryConfig, retryCount, callback);
                }
            }
        });
    }
    
    private <T> void handleSuccessResponse(
            String responseString,
            OfflineDataManager.CacheType cacheType,
            Class<T> responseType,
            NetworkCallback<T> callback
    ) {
        try {
            T data = gson.fromJson(responseString, responseType);
            
            // Cache the response if cache type is specified
            if (cacheType != null && data != null) {
                offlineDataManager.cache(data, cacheType);
            }
            
            mainHandler.post(() -> callback.onSuccess(data));
        } catch (JsonSyntaxException e) {
            Log.e(TAG, "JSON parsing error: " + e.getMessage());
            mainHandler.post(() -> callback.onFailure(NetworkError.PARSING_ERROR, "Failed to parse server response"));
        }
    }
    
    private <T> void handleRequestFailure(
            Exception exception,
            Response response,
            String url,
            Map<String, String> parameters,
            String requestKey,
            OfflineDataManager.CacheType cacheType,
            Class<T> responseType,
            RetryConfig retryConfig,
            int retryCount,
            NetworkCallback<T> callback
    ) {
        NetworkError networkError;
        String errorMessage;
        
        if (response != null) {
            int statusCode = response.code();
            networkError = NetworkError.SERVER_ERROR;
            errorMessage = "Server error (" + statusCode + "). Please try again later.";
            
            // Check if we should retry
            if (retryCount < retryConfig.maxRetries && retryConfig.retryableStatusCodes.contains(statusCode)) {
                scheduleRetry(url, parameters, requestKey, cacheType, responseType, retryConfig, retryCount + 1, callback);
                return;
            }
        } else if (exception instanceof IOException) {
            if (exception.getMessage() != null && exception.getMessage().contains("timeout")) {
                networkError = NetworkError.TIMEOUT;
                errorMessage = NetworkError.TIMEOUT.getMessage();
                
                // Retry on timeout
                if (retryCount < retryConfig.maxRetries) {
                    scheduleRetry(url, parameters, requestKey, cacheType, responseType, retryConfig, retryCount + 1, callback);
                    return;
                }
            } else {
                networkError = NetworkError.NO_CONNECTION;
                errorMessage = NetworkError.NO_CONNECTION.getMessage();
                
                // Try cache if available
                if (cacheType != null) {
                    handleOfflineRequest(cacheType, responseType, callback);
                    return;
                }
            }
        } else {
            networkError = NetworkError.UNKNOWN;
            errorMessage = exception != null ? exception.getMessage() : "Unknown error occurred";
        }
        
        mainHandler.post(() -> callback.onFailure(networkError, errorMessage));
    }
    
    private <T> void scheduleRetry(
            String url,
            Map<String, String> parameters,
            String requestKey,
            OfflineDataManager.CacheType cacheType,
            Class<T> responseType,
            RetryConfig retryConfig,
            int retryCount,
            NetworkCallback<T> callback
    ) {
        long delay = Math.min(
                (long) (retryConfig.baseDelay * Math.pow(retryConfig.backoffMultiplier, retryCount - 1)),
                retryConfig.maxDelay
        );
        
        Log.d(TAG, "Retrying request in " + delay + "ms (attempt " + (retryCount + 1) + "/" + (retryConfig.maxRetries + 1) + ")");
        
        mainHandler.postDelayed(() -> 
            executeNetworkRequest(url, parameters, requestKey, cacheType, responseType, retryConfig, retryCount, callback),
            delay
        );
    }
    
    private <T> void handleOfflineRequest(
            OfflineDataManager.CacheType cacheType,
            Class<T> responseType,
            NetworkCallback<T> callback
    ) {
        T cachedData = offlineDataManager.getCachedData(cacheType, responseType);
        if (cachedData != null) {
            callback.onSuccess(cachedData);
        } else {
            callback.onFailure(NetworkError.NO_CONNECTION, "No internet connection and no cached data available");
        }
    }
    
    private <T> void handleCacheOnlyRequest(
            OfflineDataManager.CacheType cacheType,
            Class<T> responseType,
            NetworkCallback<T> callback
    ) {
        T cachedData = offlineDataManager.getCachedData(cacheType, responseType);
        if (cachedData != null) {
            callback.onSuccess(cachedData);
        } else {
            callback.onFailure(NetworkError.PARSING_ERROR, "No cached data available");
        }
    }
    
    private <T> void performBackgroundUpdate(
            String url,
            Map<String, String> parameters,
            OfflineDataManager.CacheType cacheType,
            Class<T> responseType,
            RetryConfig retryConfig
    ) {
        executeNetworkRequest(url, parameters, "bg_" + url, cacheType, responseType, retryConfig, 0, new NetworkCallback<T>() {
            @Override
            public void onSuccess(T data) {
                Log.d(TAG, "Background update completed for " + cacheType);
            }
            
            @Override
            public void onFailure(NetworkError error, String message) {
                Log.d(TAG, "Background update failed for " + cacheType + ": " + message);
            }
        });
    }
    
    // MARK: - Convenience Methods
    
    public <T> void requestWithCache(
            String url,
            Map<String, String> parameters,
            OfflineDataManager.CacheType cacheType,
            Class<T> responseType,
            NetworkCallback<T> callback
    ) {
        OfflineDataManager.CachePolicy policy = 
                (connectionMonitor.getConnectionQuality() == ConnectionMonitor.ConnectionQuality.POOR || 
                 connectionMonitor.getConnectionQuality() == ConnectionMonitor.ConnectionQuality.OFFLINE) 
                ? OfflineDataManager.CachePolicy.CACHE_FIRST 
                : OfflineDataManager.CachePolicy.NETWORK_FIRST;
        
        request(url, parameters, cacheType, policy, responseType, callback);
    }
    
    public <T> void requestCriticalData(
            String url,
            Map<String, String> parameters,
            Class<T> responseType,
            NetworkCallback<T> callback
    ) {
        request(url, parameters, null, OfflineDataManager.CachePolicy.NETWORK_ONLY, responseType, RetryConfig.getAggressive(), callback);
    }
    
    public void cancelAllRequests() {
        httpClient.dispatcher().cancelAll();
        activeRequests.clear();
    }
    
    // MARK: - Utility Methods
    
    public boolean canMakeRequest() {
        return activeRequests.size() < MAX_CONCURRENT_REQUESTS;
    }
    
    public int getActiveRequestsCount() {
        return activeRequests.size();
    }
    
    public void cleanup() {
        cancelAllRequests();
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}