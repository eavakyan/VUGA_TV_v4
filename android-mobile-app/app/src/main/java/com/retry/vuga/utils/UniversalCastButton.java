package com.retry.vuga.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastState;
import com.retry.vuga.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Universal Cast Button that handles both Google Cast and DLNA devices
 */
public class UniversalCastButton extends ImageButton {
    private static final String TAG = "UniversalCastButton";
    
    private UniversalCastManager castManager;
    private OnCastDeviceSelectedListener deviceSelectedListener;
    private boolean isDiscovering = false;
    private List<UniversalCastManager.CastDevice> availableDevices;
    
    public interface OnCastDeviceSelectedListener {
        void onDeviceSelected(UniversalCastManager.CastDevice device);
        void onDeviceDisconnected();
    }
    
    public UniversalCastButton(@NonNull Context context) {
        super(context);
        init(context);
    }
    
    public UniversalCastButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    
    public UniversalCastButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }
    
    private void init(Context context) {
        this.availableDevices = new ArrayList<>();
        this.castManager = new UniversalCastManager(context);
        
        // Set initial cast icon
        setImageResource(R.drawable.ic_cast);
        setContentDescription("Cast to TV");
        
        // Set up cast state listener
        castManager.setCastStateListener(new UniversalCastManager.OnCastStateListener() {
            @Override
            public void onCastStateChanged(boolean isConnected, UniversalCastManager.CastDevice device, UniversalCastManager.DeviceType type) {
                updateCastIcon(isConnected);
                
                if (!isConnected && deviceSelectedListener != null) {
                    deviceSelectedListener.onDeviceDisconnected();
                }
            }
            
            @Override
            public void onMediaLoadResult(boolean success, String error) {
                if (!success && error != null) {
                    Toast.makeText(getContext(), "Cast failed: " + error, Toast.LENGTH_SHORT).show();
                }
            }
        });
        
        // Set click listener
        setOnClickListener(v -> {
            if (castManager.isConnected()) {
                // Show disconnect option
                showDisconnectDialog();
            } else {
                // Show device selection dialog
                showDeviceSelectionDialog();
            }
        });
        
        // Try to integrate with Google Cast SDK's built-in behavior
        try {
            CastContext castContext = CastContext.getSharedInstance(context);
            // Update icon based on Cast state
            castContext.addCastStateListener(newState -> {
                if (newState == CastState.CONNECTED) {
                    updateCastIcon(true);
                } else if (newState == CastState.NOT_CONNECTED) {
                    updateCastIcon(false);
                }
            });
        } catch (Exception e) {
            Log.w(TAG, "Could not integrate with Google Cast SDK", e);
        }
    }
    
    private void updateCastIcon(boolean isConnected) {
        post(() -> {
            if (isConnected) {
                setImageResource(R.drawable.ic_cast_connected);
                setContentDescription("Connected to Cast device");
            } else {
                setImageResource(R.drawable.ic_cast);
                setContentDescription("Cast to TV");
            }
        });
    }
    
    private void showDeviceSelectionDialog() {
        if (isDiscovering) {
            return;
        }
        
        isDiscovering = true;
        availableDevices.clear();
        
        // Show progress dialog while discovering
        AlertDialog progressDialog = new AlertDialog.Builder(getContext())
                .setTitle("Discovering devices...")
                .setMessage("Looking for Cast and DLNA devices...")
                .setCancelable(true)
                .setNegativeButton("Cancel", (dialog, which) -> {
                    isDiscovering = false;
                    dialog.dismiss();
                })
                .create();
        
        progressDialog.show();
        
        // Start device discovery
        castManager.discoverDevices(new UniversalCastManager.OnDeviceDiscoveryListener() {
            @Override
            public void onDeviceDiscovered(UniversalCastManager.CastDevice device, UniversalCastManager.DeviceType type) {
                availableDevices.add(device);
                Log.d(TAG, "Discovered device: " + device.name + " (" + type + ")");
            }
            
            @Override
            public void onDiscoveryComplete(List<UniversalCastManager.CastDevice> devices) {
                isDiscovering = false;
                progressDialog.dismiss();
                
                if (devices.isEmpty()) {
                    // Show "no devices found" dialog
                    new AlertDialog.Builder(getContext())
                            .setTitle("No devices found")
                            .setMessage("No Cast or DLNA devices were found on your network. Make sure your TV supports casting and is connected to the same WiFi network.")
                            .setNeutralButton("OK", null)
                            .show();
                } else {
                    // Show device selection dialog
                    showDeviceList(new ArrayList<>(availableDevices));
                }
            }
            
            @Override
            public void onError(String error) {
                isDiscovering = false;
                progressDialog.dismiss();
                
                Toast.makeText(getContext(), "Discovery error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void showDeviceList(List<UniversalCastManager.CastDevice> devices) {
        // Create adapter for device list
        ArrayAdapter<UniversalCastManager.CastDevice> adapter = new ArrayAdapter<UniversalCastManager.CastDevice>(
                getContext(), 
                android.R.layout.simple_list_item_1, 
                devices) {
            @Override
            public View getView(int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                UniversalCastManager.CastDevice device = getItem(position);
                
                if (device != null) {
                    String displayName = device.name;
                    if (device.type == UniversalCastManager.DeviceType.DLNA) {
                        displayName += " (DLNA)";
                    } else if (device.type == UniversalCastManager.DeviceType.GOOGLE_CAST) {
                        displayName += " (Cast)";
                    }
                    ((android.widget.TextView) view).setText(displayName);
                }
                
                return view;
            }
        };
        
        // Show device selection dialog
        new AlertDialog.Builder(getContext())
                .setTitle("Select Cast Device")
                .setAdapter(adapter, (dialog, which) -> {
                    UniversalCastManager.CastDevice selectedDevice = devices.get(which);
                    Log.d(TAG, "Selected device: " + selectedDevice.name + " (" + selectedDevice.type + ")");
                    
                    if (deviceSelectedListener != null) {
                        deviceSelectedListener.onDeviceSelected(selectedDevice);
                    }
                    
                    // For Google Cast devices, let the Cast SDK handle the connection
                    // For DLNA devices, we would need to implement connection logic
                    if (selectedDevice.type == UniversalCastManager.DeviceType.GOOGLE_CAST) {
                        // The Cast SDK will handle the connection when media is cast
                        Toast.makeText(getContext(), "Selected " + selectedDevice.name + ". Start playing a video to cast.", Toast.LENGTH_LONG).show();
                    } else if (selectedDevice.type == UniversalCastManager.DeviceType.DLNA) {
                        Toast.makeText(getContext(), "Selected DLNA device: " + selectedDevice.name, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    private void showDisconnectDialog() {
        UniversalCastManager.CastDevice currentDevice = castManager.getCurrentDevice();
        String deviceName = currentDevice != null ? currentDevice.name : "Unknown device";
        
        new AlertDialog.Builder(getContext())
                .setTitle("Disconnect from " + deviceName + "?")
                .setPositiveButton("Disconnect", (dialog, which) -> {
                    castManager.stopCasting();
                    updateCastIcon(false);
                    
                    if (deviceSelectedListener != null) {
                        deviceSelectedListener.onDeviceDisconnected();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    public void setOnCastDeviceSelectedListener(OnCastDeviceSelectedListener listener) {
        this.deviceSelectedListener = listener;
    }
    
    public UniversalCastManager getCastManager() {
        return castManager;
    }
    
    public boolean isConnected() {
        return castManager.isConnected();
    }
    
    public UniversalCastManager.CastDevice getCurrentDevice() {
        return castManager.getCurrentDevice();
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (castManager != null) {
            castManager.cleanup();
        }
    }
}