package com.retry.vuga.utils.custom_view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import com.retry.vuga.R;
import com.retry.vuga.utils.ConnectionMonitor;

public class NetworkStatusView extends LinearLayout implements ConnectionMonitor.ConnectionListener {
    
    private TextView tvNetworkStatus;
    private View statusIndicator;
    private ConnectionMonitor connectionMonitor;
    private boolean isVisible = false;
    
    public NetworkStatusView(Context context) {
        super(context);
        init(context);
    }
    
    public NetworkStatusView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    
    public NetworkStatusView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }
    
    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.layout_network_status, this, true);
        
        tvNetworkStatus = findViewById(R.id.tv_network_status);
        statusIndicator = findViewById(R.id.status_indicator);
        
        // Initialize with good connection
        updateUI(true, ConnectionMonitor.ConnectionType.WIFI, ConnectionMonitor.ConnectionQuality.GOOD);
        
        // Hide initially
        setVisibility(GONE);
        
        // Setup connection monitor
        try {
            connectionMonitor = ConnectionMonitor.getInstance();
            connectionMonitor.addListener(this);
        } catch (IllegalStateException e) {
            // ConnectionMonitor not initialized yet, will be added later
        }
    }
    
    public void initializeConnectionMonitor(Context context) {
        if (connectionMonitor == null) {
            connectionMonitor = ConnectionMonitor.getInstance(context);
            connectionMonitor.addListener(this);
        }
    }
    
    @Override
    public void onConnectionChanged(boolean isConnected, ConnectionMonitor.ConnectionType type, ConnectionMonitor.ConnectionQuality quality) {
        post(() -> updateUI(isConnected, type, quality));
    }
    
    @Override
    public void onConnectionAlert(boolean show, String message) {
        post(() -> {
            if (show && !message.isEmpty()) {
                showStatus(message, getQualityColor(connectionMonitor != null ? connectionMonitor.getConnectionQuality() : ConnectionMonitor.ConnectionQuality.POOR));
            } else if (!show && isVisible) {
                hideStatus();
            }
        });
    }
    
    private void updateUI(boolean isConnected, ConnectionMonitor.ConnectionType type, ConnectionMonitor.ConnectionQuality quality) {
        String statusText = quality.getDisplayText();
        int color = getQualityColor(quality);
        
        tvNetworkStatus.setText(statusText);
        statusIndicator.setBackgroundColor(color);
        
        // Show/hide based on connection quality
        if (!isConnected || quality == ConnectionMonitor.ConnectionQuality.POOR || quality == ConnectionMonitor.ConnectionQuality.FAIR) {
            showStatus(statusText, color);
        } else if (quality == ConnectionMonitor.ConnectionQuality.GOOD || quality == ConnectionMonitor.ConnectionQuality.EXCELLENT) {
            // Show briefly for good connections then hide
            showStatus(statusText, color);
            postDelayed(this::hideStatus, 2000);
        }
    }
    
    private void showStatus(String message, int color) {
        if (!isVisible) {
            tvNetworkStatus.setText(message);
            statusIndicator.setBackgroundColor(color);
            setVisibility(VISIBLE);
            isVisible = true;
            
            // Animate in
            setAlpha(0f);
            animate()
                .alpha(1f)
                .setDuration(300)
                .start();
        }
    }
    
    private void hideStatus() {
        if (isVisible) {
            animate()
                .alpha(0f)
                .setDuration(300)
                .withEndAction(() -> {
                    setVisibility(GONE);
                    isVisible = false;
                })
                .start();
        }
    }
    
    private int getQualityColor(ConnectionMonitor.ConnectionQuality quality) {
        switch (quality) {
            case EXCELLENT:
            case GOOD:
                return ContextCompat.getColor(getContext(), android.R.color.holo_green_dark);
            case FAIR:
                return ContextCompat.getColor(getContext(), android.R.color.holo_orange_dark);
            case POOR:
            case OFFLINE:
                return ContextCompat.getColor(getContext(), android.R.color.holo_red_dark);
            default:
                return ContextCompat.getColor(getContext(), android.R.color.darker_gray);
        }
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (connectionMonitor != null) {
            connectionMonitor.removeListener(this);
        }
    }
}