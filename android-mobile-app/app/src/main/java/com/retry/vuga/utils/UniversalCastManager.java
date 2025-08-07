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
    private DlnaCaster dlnaCaster;
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
        this.dlnaCaster = new DlnaCaster();
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
        
        Log.d(TAG, "Starting device discovery for both Cast and DLNA");
        
        // Discover Google Cast devices (they're automatically discovered by the Cast SDK)
        if (castContext != null) {
            // Google Cast devices are discovered automatically
            // We can get available devices from the Cast context, but this requires UI interaction
            Log.d(TAG, "Google Cast discovery managed by Cast SDK");
        }
        
        // Discover DLNA devices
        Log.d(TAG, "Starting DLNA device discovery");
        Log.d(TAG, "DLNA manager instance: " + dlnaManager);
        Log.d(TAG, "Is DLNA already discovering: " + dlnaManager.isDiscovering());
        
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
                
                // Check if device already exists by IP address
                boolean deviceExists = false;
                for (CastDevice existingDevice : allDevices) {
                    if (existingDevice.ipAddress.equals(castDevice.ipAddress) && 
                        existingDevice.type == DeviceType.DLNA) {
                        deviceExists = true;
                        break;
                    }
                }
                
                if (!deviceExists) {
                    allDevices.add(castDevice);
                    
                    if (discoveryListener != null) {
                        discoveryListener.onDeviceDiscovered(castDevice, DeviceType.DLNA);
                    }
                }
            }
            
            @Override
            public void onDiscoveryComplete(List<DlnaManager.DlnaDevice> devices) {
                Log.d(TAG, "DLNA discovery complete. Raw device count: " + devices.size());
                Log.d(TAG, "Total unique devices in allDevices: " + allDevices.size());
                
                // Log each unique device
                for (CastDevice device : allDevices) {
                    Log.d(TAG, "  - " + device.name + " (" + device.type + ") at " + device.ipAddress);
                }
                
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
        if (currentDevice == null || !(currentDevice.originalDevice instanceof DlnaManager.DlnaDevice)) {
            Log.e(TAG, "Invalid DLNA device for casting");
            if (castStateListener != null) {
                castStateListener.onMediaLoadResult(false, "Invalid DLNA device");
            }
            return;
        }
        
        DlnaManager.DlnaDevice dlnaDevice = (DlnaManager.DlnaDevice) currentDevice.originalDevice;
        Log.d(TAG, "Casting to DLNA device: " + dlnaDevice.name + " at " + dlnaDevice.location);
        Log.d(TAG, "Video URL: " + videoUrl);
        Log.d(TAG, "Device IP: " + dlnaDevice.ipAddress);
        Log.d(TAG, "Device USN: " + dlnaDevice.usn);
        
        // Connect to DLNA device
        dlnaCaster.connectToDevice(dlnaDevice.location, new DlnaCaster.OnCastListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "DLNA device connection successful");
                
                // Determine content type based on URL
                String contentType = "video/mp4"; // Default
                if (videoUrl.contains(".m3u8")) {
                    contentType = "video/x-mpegURL";
                } else if (videoUrl.contains(".mkv")) {
                    contentType = "video/x-matroska";
                } else if (videoUrl.contains(".webm")) {
                    contentType = "video/webm";
                } else if (videoUrl.contains(".avi")) {
                    contentType = "video/x-msvideo";
                } else if (videoUrl.contains(".mov")) {
                    contentType = "video/quicktime";
                }
                
                Log.d(TAG, "Casting video with content type: " + contentType);
                Log.d(TAG, "Full video URL being cast: " + videoUrl);
                
                // Cast the video
                dlnaCaster.castVideo(videoUrl, title, contentType, new DlnaCaster.OnCastListener() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "DLNA cast successful - media loaded and playing");
                        isConnected = true;
                        currentDeviceType = DeviceType.DLNA;
                        
                        if (castStateListener != null) {
                            castStateListener.onMediaLoadResult(true, null);
                            castStateListener.onCastStateChanged(true, currentDevice, DeviceType.DLNA);
                        }
                    }
                    
                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "DLNA cast failed: " + error);
                        Log.e(TAG, "Failed URL was: " + videoUrl);
                        if (castStateListener != null) {
                            castStateListener.onMediaLoadResult(false, error);
                        }
                    }
                });
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "DLNA connection failed: " + error);
                Log.e(TAG, "Failed device location: " + dlnaDevice.location);
                if (castStateListener != null) {
                    castStateListener.onMediaLoadResult(false, "Connection failed: " + error);
                }
            }
        });
    }
    
    public void pauseMedia() {
        if (!isConnected) return;
        
        switch (currentDeviceType) {
            case GOOGLE_CAST:
                CastSession session = castContext.getSessionManager().getCurrentCastSession();
                if (session != null && session.getRemoteMediaClient() != null) {
                    session.getRemoteMediaClient().pause();
                }
                break;
            case DLNA:
                dlnaCaster.pause(new DlnaCaster.OnCastListener() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "DLNA pause successful");
                    }
                    
                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "DLNA pause failed: " + error);
                    }
                });
                break;
        }
    }
    
    public void resumeMedia() {
        if (!isConnected) return;
        
        switch (currentDeviceType) {
            case GOOGLE_CAST:
                CastSession session = castContext.getSessionManager().getCurrentCastSession();
                if (session != null && session.getRemoteMediaClient() != null) {
                    session.getRemoteMediaClient().play();
                }
                break;
            case DLNA:
                dlnaCaster.play(new DlnaCaster.OnCastListener() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "DLNA resume successful");
                    }
                    
                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "DLNA resume failed: " + error);
                    }
                });
                break;
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
                dlnaCaster.stop(new DlnaCaster.OnCastListener() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "DLNA stop successful");
                        isConnected = false;
                        currentDevice = null;
                        currentDeviceType = DeviceType.NONE;
                        
                        if (castStateListener != null) {
                            castStateListener.onCastStateChanged(false, null, DeviceType.NONE);
                        }
                    }
                    
                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "DLNA stop failed: " + error);
                    }
                });
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
    
    public void connectToDlnaDevice(CastDevice device) {
        if (device == null || device.type != DeviceType.DLNA) {
            Log.w(TAG, "Invalid DLNA device");
            return;
        }
        
        currentDevice = device;
        currentDeviceType = DeviceType.DLNA;
        
        // For DLNA, we don't maintain a persistent connection
        // Connection is established when casting media
        isConnected = true;
        
        if (castStateListener != null) {
            castStateListener.onCastStateChanged(true, device, DeviceType.DLNA);
        }
    }
    
    public void cleanup() {
        dlnaManager.cleanup();
        dlnaCaster.cleanup();
    }
}