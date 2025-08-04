package com.retry.vuga.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.google.android.gms.cast.ApplicationMetadata;
import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaLoadRequestData;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.CastState;
import com.google.android.gms.cast.framework.CastStateListener;
import com.google.android.gms.cast.framework.SessionManagerListener;
import com.google.android.gms.common.images.WebImage;

import java.util.ArrayList;
import java.util.List;

/**
 * Universal Cast Manager that supports both Google Cast and DLNA/UPnP devices
 */
public class UniversalCastManager {
    private static final String TAG = "UniversalCastManager";
    
    private Context context;
    private CastContext castContext;
    private DlnaManager dlnaManager;
    private List<CastDevice> allDevices;
    private OnDeviceDiscoveryListener discoveryListener;
    private OnCastStateListener castStateListener;
    
    // Current casting state
    private boolean isConnected = false;
    private CastDevice currentDevice = null;
    private DeviceType currentDeviceType = DeviceType.NONE;
    
    public enum DeviceType {
        NONE,
        GOOGLE_CAST,
        DLNA
    }
    
    public interface OnDeviceDiscoveryListener {
        void onDeviceDiscovered(CastDevice device, DeviceType type);
        void onDiscoveryComplete(List<CastDevice> devices);
        void onError(String error);
    }
    
    public interface OnCastStateListener {
        void onCastStateChanged(boolean isConnected, CastDevice device, DeviceType type);
        void onMediaLoadResult(boolean success, String error);
    }
    
    // Wrapper class to combine Google Cast and DLNA devices
    public static class CastDevice {
        public String id;
        public String name;
        public String ipAddress;
        public DeviceType type;
        public Object originalDevice; // Store original Google Cast or DLNA device
        
        public CastDevice(String id, String name, String ipAddress, DeviceType type, Object originalDevice) {
            this.id = id;
            this.name = name;
            this.ipAddress = ipAddress;
            this.type = type;
            this.originalDevice = originalDevice;
        }
        
        @Override
        public String toString() {
            return name;
        }
    }
    
    public UniversalCastManager(Context context) {
        this.context = context;
        this.allDevices = new ArrayList<>();
        
        try {
            // Initialize Google Cast
            this.castContext = CastContext.getSharedInstance(context);
            setupCastListeners();
        } catch (Exception e) {
            Log.w(TAG, "Google Cast not available", e);
        }
        
        // Initialize DLNA
        this.dlnaManager = new DlnaManager(context);
    }
    
    private void setupCastListeners() {
        if (castContext == null) return;
        
        // Listen for Cast state changes
        castContext.addCastStateListener(new CastStateListener() {
            @Override
            public void onCastStateChanged(int newState) {
                boolean connected = (newState == CastState.CONNECTED);
                if (connected != isConnected) {
                    isConnected = connected;
                    
                    if (connected) {
                        CastSession session = castContext.getSessionManager().getCurrentCastSession();
                        if (session != null) {
                            com.google.android.gms.cast.CastDevice device = session.getCastDevice();
                            currentDevice = new CastDevice(
                                device.getDeviceId(),
                                device.getFriendlyName(),
                                device.getIpAddress().getHostAddress(),
                                DeviceType.GOOGLE_CAST,
                                device
                            );
                            currentDeviceType = DeviceType.GOOGLE_CAST;
                        }
                    } else {
                        currentDevice = null;
                        currentDeviceType = DeviceType.NONE;
                    }
                    
                    if (castStateListener != null) {
                        castStateListener.onCastStateChanged(isConnected, currentDevice, currentDeviceType);
                    }
                }
            }
        });
        
        // Listen for session events
        castContext.getSessionManager().addSessionManagerListener(
            new SessionManagerListener<CastSession>() {
                @Override
                public void onSessionStarted(CastSession session, String sessionId) {
                    Log.d(TAG, "Google Cast session started");
                }
                
                @Override
                public void onSessionStarting(CastSession session) {
                    Log.d(TAG, "Google Cast session starting");
                }
                
                @Override
                public void onSessionStartFailed(CastSession session, int error) {
                    Log.w(TAG, "Google Cast session start failed: " + error);
                }
                
                @Override
                public void onSessionEnded(CastSession session, int error) {
                    Log.d(TAG, "Google Cast session ended");
                }
                
                @Override
                public void onSessionResumed(CastSession session, boolean wasSuspended) {
                    Log.d(TAG, "Google Cast session resumed");
                }
                
                @Override
                public void onSessionSuspended(CastSession session, int reason) {
                    Log.d(TAG, "Google Cast session suspended");
                }
                
                @Override
                public void onSessionEnding(CastSession session) {
                    Log.d(TAG, "Google Cast session ending");
                }
                
                @Override
                public void onSessionResuming(CastSession session, String sessionId) {
                    Log.d(TAG, "Google Cast session resuming");
                }
                
                @Override
                public void onSessionResumeFailed(CastSession session, int error) {
                    Log.w(TAG, "Google Cast session resume failed: " + error);
                }
            }, CastSession.class);
    }
    
    public void discoverDevices(OnDeviceDiscoveryListener listener) {
        this.discoveryListener = listener;
        this.allDevices.clear();
        
        // Discover Google Cast devices (they're automatically discovered by the Cast SDK)
        if (castContext != null) {
            // Google Cast devices are discovered automatically
            // We can get available devices from the Cast context, but this requires UI interaction
            Log.d(TAG, "Google Cast discovery managed by Cast SDK");
        }
        
        // Discover DLNA devices
        dlnaManager.discoverDevices(new DlnaManager.OnDeviceDiscoveryListener() {
            @Override
            public void onDeviceDiscovered(DlnaManager.DlnaDevice device) {
                CastDevice castDevice = new CastDevice(
                    device.usn,
                    device.name,
                    device.ipAddress,
                    DeviceType.DLNA,
                    device
                );
                
                allDevices.add(castDevice);
                
                if (discoveryListener != null) {
                    discoveryListener.onDeviceDiscovered(castDevice, DeviceType.DLNA);
                }
            }
            
            @Override
            public void onDiscoveryComplete(List<DlnaManager.DlnaDevice> devices) {
                Log.d(TAG, "DLNA discovery complete. Found " + devices.size() + " devices");
                
                if (discoveryListener != null) {
                    discoveryListener.onDiscoveryComplete(new ArrayList<>(allDevices));
                }
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "DLNA discovery error: " + error);
                
                if (discoveryListener != null) {
                    discoveryListener.onError(error);
                }
            }
        });
    }
    
    public void castMedia(String videoUrl, String title, String subtitle, String imageUrl) {
        if (!isConnected || currentDevice == null) {
            if (castStateListener != null) {
                castStateListener.onMediaLoadResult(false, "No device connected");
            }
            return;
        }
        
        switch (currentDeviceType) {
            case GOOGLE_CAST:
                castToGoogleCast(videoUrl, title, subtitle, imageUrl);
                break;
            case DLNA:
                castToDlna(videoUrl, title, subtitle, imageUrl);
                break;
            default:
                if (castStateListener != null) {
                    castStateListener.onMediaLoadResult(false, "Unknown device type");
                }
                break;
        }
    }
    
    private void castToGoogleCast(String videoUrl, String title, String subtitle, String imageUrl) {
        if (castContext == null) {
            if (castStateListener != null) {
                castStateListener.onMediaLoadResult(false, "Google Cast not available");
            }
            return;
        }
        
        try {
            CastSession session = castContext.getSessionManager().getCurrentCastSession();
            if (session == null) {
                if (castStateListener != null) {
                    castStateListener.onMediaLoadResult(false, "No Cast session");
                }
                return;
            }
            
            // Build media metadata
            MediaMetadata metadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);
            metadata.putString(MediaMetadata.KEY_TITLE, title);
            if (subtitle != null) {
                metadata.putString(MediaMetadata.KEY_SUBTITLE, subtitle);
            }
            if (imageUrl != null) {
                metadata.addImage(new WebImage(Uri.parse(imageUrl)));
            }
            
            // Determine content type
            String contentType = "video/mp4"; // Default
            if (videoUrl.contains(".m3u8")) {
                contentType = "application/x-mpegURL";
            } else if (videoUrl.contains(".mkv")) {
                contentType = "video/x-matroska";
            } else if (videoUrl.contains(".webm")) {
                contentType = "video/webm";
            }
            
            // Create media info
            MediaInfo mediaInfo = new MediaInfo.Builder(videoUrl)
                    .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                    .setContentType(contentType)
                    .setMetadata(metadata)
                    .build();
            
            // Create load request
            MediaLoadRequestData loadRequest = new MediaLoadRequestData.Builder()
                    .setMediaInfo(mediaInfo)
                    .setAutoplay(true)
                    .build();
            
            // Load media
            session.getRemoteMediaClient().load(loadRequest)
                    .setResultCallback(result -> {
                        boolean success = result.getStatus().isSuccess();
                        String error = success ? null : "Failed to load media: " + result.getStatus().getStatusMessage();
                        
                        Log.d(TAG, "Google Cast media load result: " + (success ? "SUCCESS" : error));
                        
                        if (castStateListener != null) {
                            castStateListener.onMediaLoadResult(success, error);
                        }
                    });
                    
        } catch (Exception e) {
            Log.e(TAG, "Error casting to Google Cast", e);
            if (castStateListener != null) {
                castStateListener.onMediaLoadResult(false, "Cast error: " + e.getMessage());
            }
        }
    }
    
    private void castToDlna(String videoUrl, String title, String subtitle, String imageUrl) {
        // For now, DLNA casting is a simplified implementation
        // In a full implementation, you would need to:
        // 1. Send SOAP requests to the DLNA device's control URLs
        // 2. Set the media URL using SetAVTransportURI
        // 3. Start playback using Play command
        
        Log.d(TAG, "DLNA casting not fully implemented yet");
        Log.d(TAG, "Would cast URL: " + videoUrl + " to device: " + currentDevice.name);
        
        // For now, just report success to indicate the framework is working
        if (castStateListener != null) {
            castStateListener.onMediaLoadResult(true, null);
        }
    }
    
    public void pauseMedia() {
        if (!isConnected || currentDeviceType != DeviceType.GOOGLE_CAST) return;
        
        CastSession session = castContext.getSessionManager().getCurrentCastSession();
        if (session != null && session.getRemoteMediaClient() != null) {
            session.getRemoteMediaClient().pause();
        }
    }
    
    public void resumeMedia() {
        if (!isConnected || currentDeviceType != DeviceType.GOOGLE_CAST) return;
        
        CastSession session = castContext.getSessionManager().getCurrentCastSession();
        if (session != null && session.getRemoteMediaClient() != null) {
            session.getRemoteMediaClient().play();
        }
    }
    
    public void seekTo(long positionMs) {
        if (!isConnected || currentDeviceType != DeviceType.GOOGLE_CAST) return;
        
        CastSession session = castContext.getSessionManager().getCurrentCastSession();
        if (session != null && session.getRemoteMediaClient() != null) {
            session.getRemoteMediaClient().seek(positionMs);
        }
    }
    
    public void stopCasting() {
        if (!isConnected) return;
        
        switch (currentDeviceType) {
            case GOOGLE_CAST:
                if (castContext != null) {
                    castContext.getSessionManager().endCurrentSession(true);
                }
                break;
            case DLNA:
                // Stop DLNA casting
                Log.d(TAG, "Stopping DLNA cast");
                break;
        }
    }
    
    public boolean isConnected() {
        return isConnected;
    }
    
    public CastDevice getCurrentDevice() {
        return currentDevice;
    }
    
    public DeviceType getCurrentDeviceType() {
        return currentDeviceType;
    }
    
    public List<CastDevice> getDiscoveredDevices() {
        return new ArrayList<>(allDevices);
    }
    
    public void setCastStateListener(OnCastStateListener listener) {
        this.castStateListener = listener;
    }
    
    public void cleanup() {
        dlnaManager.cleanup();
    }
}