package com.retry.vuga.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * Broadcast receiver to monitor network connectivity changes
 * Used to adapt video buffering strategy based on network conditions
 */
public class NetworkChangeReceiver extends BroadcastReceiver {
    private static final String TAG = "NetworkChangeReceiver";
    
    public interface NetworkChangeListener {
        void onNetworkChanged(boolean isConnected, int networkType);
    }
    
    private NetworkChangeListener listener;
    
    public void setNetworkChangeListener(NetworkChangeListener listener) {
        this.listener = listener;
    }
    
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && 
            intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            
            ConnectivityManager cm = (ConnectivityManager) 
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
                
            if (cm != null) {
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                boolean isConnected = activeNetwork != null && activeNetwork.isConnected();
                int networkType = isConnected ? activeNetwork.getType() : -1;
                
                Log.d(TAG, "Network changed - Connected: " + isConnected + 
                          ", Type: " + getNetworkTypeName(networkType));
                
                if (listener != null) {
                    listener.onNetworkChanged(isConnected, networkType);
                }
                
                // Update cache manager's network type
                VideoCacheManager.getInstance(context).getCurrentNetworkType();
            }
        }
    }
    
    private String getNetworkTypeName(int type) {
        switch (type) {
            case ConnectivityManager.TYPE_WIFI:
                return "WiFi";
            case ConnectivityManager.TYPE_MOBILE:
                return "Mobile";
            case ConnectivityManager.TYPE_ETHERNET:
                return "Ethernet";
            default:
                return "Unknown";
        }
    }
}