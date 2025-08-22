package com.retry.vuga.utils;

import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import com.retry.vuga.R;

/**
 * Simple network status indicator view
 */
public class NetworkStatusView extends CardView {
    
    private TextView statusText;
    private LinearLayout container;
    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;
    private boolean isOnline = true;
    
    public NetworkStatusView(@NonNull Context context) {
        super(context);
        init(context);
    }
    
    public NetworkStatusView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    
    private void init(Context context) {
        // Setup card view
        setCardElevation(8f);
        setRadius(8f);
        setCardBackgroundColor(Color.parseColor("#FF5722")); // Orange for warning
        
        // Create container
        container = new LinearLayout(context);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setGravity(Gravity.CENTER);
        container.setPadding(24, 16, 24, 16);
        
        // Create text view
        statusText = new TextView(context);
        statusText.setText("No Internet Connection");
        statusText.setTextColor(Color.WHITE);
        statusText.setTextSize(14f);
        
        container.addView(statusText);
        addView(container);
        
        // Initially hidden
        setVisibility(View.GONE);
        
        // Setup network monitoring
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        startNetworkMonitoring();
    }
    
    private void startNetworkMonitoring() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            networkCallback = new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(@NonNull Network network) {
                    post(() -> setOnlineStatus(true));
                }
                
                @Override
                public void onLost(@NonNull Network network) {
                    post(() -> setOnlineStatus(false));
                }
                
                @Override
                public void onCapabilitiesChanged(@NonNull Network network, 
                                                 @NonNull NetworkCapabilities capabilities) {
                    boolean hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
                    post(() -> {
                        if (hasInternet) {
                            // Check connection speed
                            int bandwidth = capabilities.getLinkDownstreamBandwidthKbps();
                            if (bandwidth < 150) { // Less than 150 Kbps
                                showSlowConnection();
                            } else {
                                setOnlineStatus(true);
                            }
                        }
                    });
                }
            };
            
            NetworkRequest request = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build();
            
            connectivityManager.registerNetworkCallback(request, networkCallback);
        }
        
        // Check initial status
        checkNetworkStatus();
    }
    
    private void checkNetworkStatus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network network = connectivityManager.getActiveNetwork();
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
            boolean hasInternet = capabilities != null && 
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
            setOnlineStatus(hasInternet);
        }
    }
    
    private void setOnlineStatus(boolean online) {
        isOnline = online;
        if (online) {
            setVisibility(View.GONE);
        } else {
            setVisibility(View.VISIBLE);
            statusText.setText("No Internet Connection");
            setCardBackgroundColor(Color.parseColor("#F44336")); // Red for offline
        }
    }
    
    private void showSlowConnection() {
        setVisibility(View.VISIBLE);
        statusText.setText("Slow Connection Detected");
        setCardBackgroundColor(Color.parseColor("#FF9800")); // Orange for slow
    }
    
    public boolean isOnline() {
        return isOnline;
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && networkCallback != null) {
            connectivityManager.unregisterNetworkCallback(networkCallback);
        }
    }
}