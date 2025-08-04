package com.retry.vuga.utils;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public class DlnaManager {
    private static final String TAG = "DlnaManager";
    private static final String SSDP_IP = "239.255.255.250";
    private static final int SSDP_PORT = 1900;
    private static final String MEDIA_RENDERER_SEARCH = 
        "M-SEARCH * HTTP/1.1\r\n" +
        "HOST: 239.255.255.250:1900\r\n" +
        "MAN: \"ssdp:discover\"\r\n" +
        "ST: urn:schemas-upnp-org:device:MediaRenderer:1\r\n" +
        "MX: 3\r\n\r\n";
    
    private Context context;
    private WifiManager.MulticastLock multicastLock;
    private List<DlnaDevice> discoveredDevices;
    private OnDeviceDiscoveryListener discoveryListener;
    private boolean isDiscovering = false;
    private Handler mainHandler;
    
    public interface OnDeviceDiscoveryListener {
        void onDeviceDiscovered(DlnaDevice device);
        void onDiscoveryComplete(List<DlnaDevice> devices);
        void onError(String error);
    }
    
    public static class DlnaDevice {
        public String name;
        public String ipAddress;
        public String location;
        public String usn;
        
        public DlnaDevice(String name, String ipAddress, String location, String usn) {
            this.name = name;
            this.ipAddress = ipAddress;
            this.location = location;
            this.usn = usn;
        }
        
        @Override
        public String toString() {
            return name + " (" + ipAddress + ")";
        }
    }
    
    public DlnaManager(Context context) {
        this.context = context;
        this.discoveredDevices = new ArrayList<>();
        this.mainHandler = new Handler(Looper.getMainLooper());
        
        // Acquire multicast lock
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null) {
            multicastLock = wifiManager.createMulticastLock("VUGA_DLNA");
            multicastLock.setReferenceCounted(true);
        }
    }
    
    public void discoverDevices(OnDeviceDiscoveryListener listener) {
        if (isDiscovering) {
            Log.w(TAG, "Discovery already in progress");
            return;
        }
        
        this.discoveryListener = listener;
        this.discoveredDevices.clear();
        this.isDiscovering = true;
        
        new Thread(this::performDiscovery).start();
    }
    
    private void performDiscovery() {
        MulticastSocket socket = null;
        
        try {
            if (multicastLock != null) {
                multicastLock.acquire();
            }
            
            // Create multicast socket
            socket = new MulticastSocket();
            socket.setTimeToLive(4);
            
            // Get local IP address
            String localIP = getLocalIPAddress();
            if (localIP != null) {
                InetAddress localAddress = InetAddress.getByName(localIP);
                socket.setInterface(localAddress);
            }
            
            // Send M-SEARCH request
            InetAddress group = InetAddress.getByName(SSDP_IP);
            byte[] searchBytes = MEDIA_RENDERER_SEARCH.getBytes();
            DatagramPacket searchPacket = new DatagramPacket(searchBytes, searchBytes.length, group, SSDP_PORT);
            
            Log.d(TAG, "Sending DLNA discovery request...");
            socket.send(searchPacket);
            
            // Listen for responses
            socket.setSoTimeout(5000); // 5 second timeout
            
            long startTime = System.currentTimeMillis();
            byte[] buffer = new byte[1024];
            
            while (System.currentTimeMillis() - startTime < 5000) {
                try {
                    DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length);
                    socket.receive(responsePacket);
                    
                    String response = new String(responsePacket.getData(), 0, responsePacket.getLength());
                    String deviceIP = responsePacket.getAddress().getHostAddress();
                    
                    Log.d(TAG, "Received SSDP response from " + deviceIP);
                    
                    // Parse response
                    DlnaDevice device = parseDeviceResponse(response, deviceIP);
                    if (device != null && !deviceExists(device)) {
                        discoveredDevices.add(device);
                        notifyDeviceDiscovered(device);
                    }
                    
                } catch (IOException e) {
                    // Timeout or socket closed
                    break;
                }
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error during DLNA discovery", e);
            notifyError("Discovery failed: " + e.getMessage());
        } finally {
            if (socket != null) {
                socket.close();
            }
            if (multicastLock != null && multicastLock.isHeld()) {
                multicastLock.release();
            }
            isDiscovering = false;
            notifyDiscoveryComplete();
        }
    }
    
    private DlnaDevice parseDeviceResponse(String response, String deviceIP) {
        try {
            String[] lines = response.split("\r\n");
            String location = null;
            String usn = null;
            String st = null;
            
            for (String line : lines) {
                String lowerLine = line.toLowerCase();
                if (lowerLine.startsWith("location:")) {
                    location = line.substring(line.indexOf(":") + 1).trim();
                } else if (lowerLine.startsWith("usn:")) {
                    usn = line.substring(line.indexOf(":") + 1).trim();
                } else if (lowerLine.startsWith("st:")) {
                    st = line.substring(line.indexOf(":") + 1).trim();
                }
            }
            
            // Check if this is a MediaRenderer device
            if (st != null && st.contains("MediaRenderer")) {
                String deviceName = extractDeviceName(usn, deviceIP);
                return new DlnaDevice(deviceName, deviceIP, location, usn);
            }
            
        } catch (Exception e) {
            Log.w(TAG, "Error parsing device response", e);
        }
        
        return null;
    }
    
    private String extractDeviceName(String usn, String deviceIP) {
        if (usn != null) {
            // Try to extract a meaningful name from USN
            if (usn.contains("::")) {
                String[] parts = usn.split("::");
                if (parts.length > 0) {
                    String uuid = parts[0];
                    if (uuid.startsWith("uuid:")) {
                        String deviceId = uuid.substring(5);
                        // Look for known device patterns
                        if (deviceId.contains("android")) {
                            return "Android TV (" + deviceIP + ")";
                        } else if (deviceId.contains("roku")) {
                            return "Roku Device (" + deviceIP + ")";
                        } else if (deviceId.contains("samsung")) {
                            return "Samsung TV (" + deviceIP + ")";
                        } else if (deviceId.contains("lg")) {
                            return "LG TV (" + deviceIP + ")";
                        }
                    }
                }
            }
        }
        
        return "DLNA Device (" + deviceIP + ")";
    }
    
    private boolean deviceExists(DlnaDevice newDevice) {
        for (DlnaDevice existing : discoveredDevices) {
            if (existing.ipAddress.equals(newDevice.ipAddress) && 
                existing.usn != null && existing.usn.equals(newDevice.usn)) {
                return true;
            }
        }
        return false;
    }
    
    private String getLocalIPAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); 
                 en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); 
                     enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress() && 
                        inetAddress.isSiteLocalAddress()) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting local IP address", e);
        }
        return null;
    }
    
    private void notifyDeviceDiscovered(DlnaDevice device) {
        if (discoveryListener != null) {
            mainHandler.post(() -> discoveryListener.onDeviceDiscovered(device));
        }
    }
    
    private void notifyDiscoveryComplete() {
        if (discoveryListener != null) {
            mainHandler.post(() -> discoveryListener.onDiscoveryComplete(new ArrayList<>(discoveredDevices)));
        }
    }
    
    private void notifyError(String error) {
        if (discoveryListener != null) {
            mainHandler.post(() -> discoveryListener.onError(error));
        }
    }
    
    public void stopDiscovery() {
        isDiscovering = false;
    }
    
    public List<DlnaDevice> getDiscoveredDevices() {
        return new ArrayList<>(discoveredDevices);
    }
    
    public boolean isDiscovering() {
        return isDiscovering;
    }
    
    public void cleanup() {
        stopDiscovery();
        if (multicastLock != null && multicastLock.isHeld()) {
            multicastLock.release();
        }
    }
}