package com.retry.vuga.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.NonNull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ConnectionMonitor {
    private static final String TAG = "ConnectionMonitor";
    private static ConnectionMonitor instance;
    
    private Context context;
    private ConnectivityManager connectivityManager;
    private NetworkCallbackImpl networkCallback;
    private Handler mainHandler;
    private ExecutorService executor;
    private OkHttpClient httpClient;
    
    // Connection state
    private boolean isConnected = true;
    private ConnectionType connectionType = ConnectionType.WIFI;
    private ConnectionQuality connectionQuality = ConnectionQuality.GOOD;
    private boolean showConnectionAlert = false;
    private long latency = 0;
    private double downloadSpeed = 0.0;
    
    // Response time tracking
    private List<Long> recentResponseTimes = new ArrayList<>();
    private static final int MAX_RESPONSE_TIME_SAMPLES = 10;
    
    // Quality thresholds (in milliseconds)
    private static final long EXCELLENT_THRESHOLD = 500;
    private static final long GOOD_THRESHOLD = 1000;
    private static final long FAIR_THRESHOLD = 2000;
    private static final long POOR_THRESHOLD = 4000;
    
    // Listeners
    private List<ConnectionListener> listeners = new ArrayList<>();
    
    public enum ConnectionType {
        WIFI, CELLULAR, NONE
    }
    
    public enum ConnectionQuality {
        EXCELLENT("Excellent Connection"),
        GOOD("Good Connection"),
        FAIR("Slow Connection"),
        POOR("Very Slow Connection"),
        OFFLINE("No Internet Connection");
        
        private final String displayText;
        
        ConnectionQuality(String displayText) {
            this.displayText = displayText;
        }
        
        public String getDisplayText() {
            return displayText;
        }
    }
    
    public interface ConnectionListener {
        void onConnectionChanged(boolean isConnected, ConnectionType type, ConnectionQuality quality);
        void onConnectionAlert(boolean show, String message);
    }
    
    private ConnectionMonitor(Context context) {
        this.context = context.getApplicationContext();
        this.connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.executor = Executors.newSingleThreadExecutor();
        this.httpClient = new OkHttpClient();
        this.networkCallback = new NetworkCallbackImpl();
        
        startMonitoring();
    }
    
    public static synchronized ConnectionMonitor getInstance(Context context) {
        if (instance == null) {
            instance = new ConnectionMonitor(context);
        }
        return instance;
    }
    
    public static ConnectionMonitor getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ConnectionMonitor not initialized. Call getInstance(Context) first.");
        }
        return instance;
    }
    
    private void startMonitoring() {
        NetworkRequest.Builder builder = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        
        connectivityManager.registerNetworkCallback(builder.build(), networkCallback);
        
        // Check initial state
        updateConnectionStatus();
        
        // Start periodic quality checks
        startPeriodicQualityCheck();
    }
    
    private void startPeriodicQualityCheck() {
        mainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isConnected) {
                    checkConnectionQuality();
                }
                mainHandler.postDelayed(this, 30000); // Check every 30 seconds
            }
        }, 30000);
    }
    
    private void updateConnectionStatus() {
        Network activeNetwork = connectivityManager.getActiveNetwork();
        boolean wasConnected = isConnected;
        
        if (activeNetwork != null) {
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(activeNetwork);
            if (capabilities != null) {
                isConnected = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
                
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    connectionType = ConnectionType.WIFI;
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    connectionType = ConnectionType.CELLULAR;
                } else {
                    connectionType = ConnectionType.NONE;
                }
            } else {
                isConnected = false;
                connectionType = ConnectionType.NONE;
            }
        } else {
            isConnected = false;
            connectionType = ConnectionType.NONE;
        }
        
        // Update quality based on connection status
        if (!isConnected) {
            connectionQuality = ConnectionQuality.OFFLINE;
            showConnectionAlert = true;
            notifyConnectionAlert(true, "No internet connection");
        } else if (!wasConnected && isConnected) {
            showConnectionAlert = false;
            notifyConnectionAlert(false, "");
            checkConnectionQuality();
        }
        
        // Notify listeners
        notifyConnectionChanged();
        
        Log.d(TAG, "Connection status - Connected: " + isConnected + ", Type: " + connectionType + ", Quality: " + connectionQuality);
    }
    
    public void recordResponseTime(long responseTime) {
        mainHandler.post(() -> {
            recentResponseTimes.add(responseTime);
            if (recentResponseTimes.size() > MAX_RESPONSE_TIME_SAMPLES) {
                recentResponseTimes.remove(0);
            }
            updateConnectionQuality();
        });
    }
    
    private void updateConnectionQuality() {
        if (!isConnected) {
            connectionQuality = ConnectionQuality.OFFLINE;
            return;
        }
        
        if (recentResponseTimes.isEmpty()) {
            connectionQuality = ConnectionQuality.GOOD;
            return;
        }
        
        // Calculate average response time
        long sum = 0;
        for (Long time : recentResponseTimes) {
            sum += time;
        }
        long averageResponseTime = sum / recentResponseTimes.size();
        latency = averageResponseTime;
        
        ConnectionQuality previousQuality = connectionQuality;
        
        // Determine quality based on average response time
        if (averageResponseTime < EXCELLENT_THRESHOLD) {
            connectionQuality = ConnectionQuality.EXCELLENT;
        } else if (averageResponseTime < GOOD_THRESHOLD) {
            connectionQuality = ConnectionQuality.GOOD;
        } else if (averageResponseTime < FAIR_THRESHOLD) {
            connectionQuality = ConnectionQuality.FAIR;
        } else if (averageResponseTime < POOR_THRESHOLD) {
            connectionQuality = ConnectionQuality.POOR;
        } else {
            connectionQuality = ConnectionQuality.POOR;
        }
        
        // Show alert if connection becomes poor
        if (previousQuality != connectionQuality && 
            (connectionQuality == ConnectionQuality.POOR || connectionQuality == ConnectionQuality.FAIR)) {
            showConnectionAlert = true;
            notifyConnectionAlert(true, connectionQuality.getDisplayText());
            
            // Auto-hide after 3 seconds for non-critical alerts
            mainHandler.postDelayed(() -> {
                if (connectionQuality != ConnectionQuality.OFFLINE) {
                    showConnectionAlert = false;
                    notifyConnectionAlert(false, "");
                }
            }, 3000);
        }
        
        notifyConnectionChanged();
    }
    
    private void checkConnectionQuality() {
        if (!isConnected) return;
        
        executor.execute(this::testLatency);
    }
    
    private void testLatency() {
        Request request = new Request.Builder()
                .url("https://www.google.com")
                .head()
                .build();
        
        long startTime = System.currentTimeMillis();
        
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                // Connection is very slow or problematic
                recordResponseTime(5000);
            }
            
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                long responseTime = System.currentTimeMillis() - startTime;
                recordResponseTime(responseTime);
                response.close();
            }
        });
    }
    
    public void startSpeedTest(SpeedTestCallback callback) {
        if (!isConnected) {
            callback.onResult(0.0);
            return;
        }
        
        Request request = new Request.Builder()
                .url("https://www.google.com/images/branding/googlelogo/2x/googlelogo_color_272x92dp.png")
                .build();
        
        long startTime = System.currentTimeMillis();
        
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                mainHandler.post(() -> {
                    downloadSpeed = 0.0;
                    callback.onResult(0.0);
                });
            }
            
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                try {
                    if (response.body() != null) {
                        byte[] bytes = response.body().bytes();
                        long elapsedTime = System.currentTimeMillis() - startTime;
                        
                        double bytesDownloaded = bytes.length;
                        double bits = bytesDownloaded * 8;
                        double megabits = bits / 1_000_000;
                        double speed = megabits / (elapsedTime / 1000.0);
                        
                        mainHandler.post(() -> {
                            downloadSpeed = speed;
                            callback.onResult(speed);
                        });
                    }
                } catch (IOException e) {
                    mainHandler.post(() -> {
                        downloadSpeed = 0.0;
                        callback.onResult(0.0);
                    });
                } finally {
                    response.close();
                }
            }
        });
    }
    
    public void addListener(ConnectionListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    public void removeListener(ConnectionListener listener) {
        listeners.remove(listener);
    }
    
    private void notifyConnectionChanged() {
        for (ConnectionListener listener : listeners) {
            listener.onConnectionChanged(isConnected, connectionType, connectionQuality);
        }
    }
    
    private void notifyConnectionAlert(boolean show, String message) {
        for (ConnectionListener listener : listeners) {
            listener.onConnectionAlert(show, message);
        }
    }
    
    // Getters
    public boolean isConnected() {
        return isConnected;
    }
    
    public ConnectionType getConnectionType() {
        return connectionType;
    }
    
    public ConnectionQuality getConnectionQuality() {
        return connectionQuality;
    }
    
    public boolean shouldShowConnectionAlert() {
        return showConnectionAlert;
    }
    
    public long getLatency() {
        return latency;
    }
    
    public double getDownloadSpeed() {
        return downloadSpeed;
    }
    
    public void stopMonitoring() {
        if (connectivityManager != null && networkCallback != null) {
            connectivityManager.unregisterNetworkCallback(networkCallback);
        }
        if (executor != null) {
            executor.shutdown();
        }
    }
    
    public interface SpeedTestCallback {
        void onResult(double speedMbps);
    }
    
    private class NetworkCallbackImpl extends ConnectivityManager.NetworkCallback {
        @Override
        public void onAvailable(@NonNull Network network) {
            Log.d(TAG, "Network available: " + network);
            mainHandler.post(() -> updateConnectionStatus());
        }
        
        @Override
        public void onLost(@NonNull Network network) {
            Log.d(TAG, "Network lost: " + network);
            mainHandler.post(() -> updateConnectionStatus());
        }
        
        @Override
        public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
            Log.d(TAG, "Network capabilities changed: " + network);
            mainHandler.post(() -> updateConnectionStatus());
        }
    }
}