package com.retry.vuga.utils;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiInfo;
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
    
    private static final String ALL_DEVICES_SEARCH = 
        "M-SEARCH * HTTP/1.1\r\n" +
        "HOST: 239.255.255.250:1900\r\n" +
        "MAN: \"ssdp:discover\"\r\n" +
        "ST: ssdp:all\r\n" +
        "MX: 3\r\n\r\n";
    
    private static final String ROOT_DEVICE_SEARCH = 
        "M-SEARCH * HTTP/1.1\r\n" +
        "HOST: 239.255.255.250:1900\r\n" +
        "MAN: \"ssdp:discover\"\r\n" +
        "ST: upnp:rootdevice\r\n" +
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
        
        Log.d(TAG, "Starting DLNA device discovery");
        this.discoveryListener = listener;
        this.discoveredDevices.clear();
        this.isDiscovering = true;
        
        new Thread(this::performDiscovery).start();
    }
    
    private void performDiscovery() {
        MulticastSocket socket = null;
        
        try {
            // Log network info for debugging
            logNetworkInfo();
            
            if (multicastLock != null) {
                Log.d(TAG, "Acquiring multicast lock");
                multicastLock.acquire();
            } else {
                Log.w(TAG, "Multicast lock is null - DLNA discovery may not work");
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
            
            // Send M-SEARCH requests
            InetAddress group = InetAddress.getByName(SSDP_IP);
            
            // Log socket info
            Log.d(TAG, "Socket info - LocalPort: " + socket.getLocalPort() + ", LocalAddress: " + socket.getLocalAddress());
            Log.d(TAG, "Multicast enabled: " + !socket.getLoopbackMode());
            
            // First, search for MediaRenderer devices
            byte[] searchBytes = MEDIA_RENDERER_SEARCH.getBytes();
            DatagramPacket searchPacket = new DatagramPacket(searchBytes, searchBytes.length, group, SSDP_PORT);
            
            Log.d(TAG, "Sending DLNA MediaRenderer discovery request to " + group + ":" + SSDP_PORT);
            Log.d(TAG, "M-SEARCH packet size: " + searchBytes.length + " bytes");
            socket.send(searchPacket);
            
            // Also search for all devices to catch TVs that might not advertise as MediaRenderer
            Thread.sleep(100);
            byte[] allSearchBytes = ALL_DEVICES_SEARCH.getBytes();
            DatagramPacket allSearchPacket = new DatagramPacket(allSearchBytes, allSearchBytes.length, group, SSDP_PORT);
            
            Log.d(TAG, "Sending DLNA all devices discovery request...");
            socket.send(allSearchPacket);
            
            // Send root device search specifically for Samsung TVs
            Thread.sleep(100);
            byte[] rootSearchBytes = ROOT_DEVICE_SEARCH.getBytes();
            DatagramPacket rootSearchPacket = new DatagramPacket(rootSearchBytes, rootSearchBytes.length, group, SSDP_PORT);
            
            Log.d(TAG, "Sending root device discovery request...");
            socket.send(rootSearchPacket);
            
            // Listen for responses
            socket.setSoTimeout(3000); // 3 second timeout per receive
            
            long startTime = System.currentTimeMillis();
            byte[] buffer = new byte[4096]; // Larger buffer for responses
            int responseCount = 0;
            
            Log.d(TAG, "Listening for DLNA responses for 10 seconds...");
            
            while (System.currentTimeMillis() - startTime < 10000) { // 10 seconds total
                try {
                    DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length);
                    socket.receive(responsePacket);
                    
                    responseCount++;
                    String response = new String(responsePacket.getData(), 0, responsePacket.getLength());
                    String deviceIP = responsePacket.getAddress().getHostAddress();
                    
                    // Log only in verbose mode for production
                    // Log.d(TAG, "Response #" + responseCount + " from " + deviceIP);
                    
                    // Parse response
                    DlnaDevice device = parseDeviceResponse(response, deviceIP);
                    if (device != null && !deviceExists(device)) {
                        discoveredDevices.add(device);
                        notifyDeviceDiscovered(device);
                    }
                    
                } catch (IOException e) {
                    // Timeout or socket closed
                    if (System.currentTimeMillis() - startTime >= 10000) {
                        Log.d(TAG, "Discovery timeout reached");
                    } else {
                        // Continue silently
                    }
                }
            }
            
            Log.d(TAG, "Discovery complete. Total responses received: " + responseCount);
            
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
            String server = null;
            
            for (String line : lines) {
                String lowerLine = line.toLowerCase();
                if (lowerLine.startsWith("location:")) {
                    location = line.substring(line.indexOf(":") + 1).trim();
                } else if (lowerLine.startsWith("usn:")) {
                    usn = line.substring(line.indexOf(":") + 1).trim();
                } else if (lowerLine.startsWith("st:")) {
                    st = line.substring(line.indexOf(":") + 1).trim();
                } else if (lowerLine.startsWith("server:")) {
                    server = line.substring(line.indexOf(":") + 1).trim();
                }
            }
            
            // Check if this is a MediaRenderer device, TV, or other casting device
            if (st != null || usn != null) {
                // Log what we found for debugging
                // Log.d(TAG, "Checking device - ST: " + st + ", USN: " + usn + ", Location: " + location);
                
                // Check for various device types
                boolean isMediaDevice = false;
                
                // Standard DLNA MediaRenderer
                if (st != null && (st.contains("MediaRenderer") || 
                                  st.contains("AVTransport"))) {
                    isMediaDevice = true;
                }
                
                // Roku devices
                else if (st != null && st.contains("roku:ecp")) {
                    isMediaDevice = true;
                }
                
                // Check USN for TV brands
                else if (usn != null && (usn.toLowerCase().contains("samsung") || 
                                        usn.toLowerCase().contains("lg") || 
                                        usn.toLowerCase().contains("sony") ||
                                        usn.toLowerCase().contains("tv") ||
                                        usn.toLowerCase().contains("roku"))) {
                    isMediaDevice = true;
                }
                
                // Check for root devices that might be TVs
                else if (st != null && st.contains("upnp:rootdevice") && location != null) {
                    // For root devices, we'll accept them and later check their capabilities
                    isMediaDevice = true;
                }
                
                if (isMediaDevice) {
                    String deviceName = extractDeviceName(usn, deviceIP, server);
                    // Log.d(TAG, "Found DLNA/UPnP device: " + deviceName + " at " + deviceIP);
                    return new DlnaDevice(deviceName, deviceIP, location, usn);
                }
            }
            
        } catch (Exception e) {
            Log.w(TAG, "Error parsing device response", e);
        }
        
        return null;
    }
    
    private String extractDeviceName(String usn, String deviceIP, String server) {
        // Check server string first for device identification
        if (server != null) {
            String serverLower = server.toLowerCase();
            if (serverLower.contains("samsung")) {
                return "Samsung TV";
            } else if (serverLower.contains("lg")) {
                return "LG TV";
            } else if (serverLower.contains("sony") || serverLower.contains("bravia")) {
                return "Sony TV";
            } else if (serverLower.contains("roku")) {
                return "Roku Device";
            } else if (serverLower.contains("vizio")) {
                return "Vizio TV";
            } else if (serverLower.contains("philips")) {
                return "Philips TV";
            } else if (serverLower.contains("tcl")) {
                return "TCL TV";
            } else if (serverLower.contains("hisense")) {
                return "Hisense TV";
            }
        }
        
        if (usn != null) {
            // Check for Roku-specific USN format
            if (usn.contains("roku:ecp:")) {
                // Extract Roku device ID
                String[] parts = usn.split(":");
                if (parts.length >= 3) {
                    return "Roku (" + parts[2] + ")";
                }
                return "Roku Device";
            }
            
            // Try to extract a meaningful name from USN
            if (usn.contains("::")) {
                String[] parts = usn.split("::");
                if (parts.length > 0) {
                    String uuid = parts[0];
                    if (uuid.startsWith("uuid:")) {
                        String deviceId = uuid.substring(5);
                        // Look for known device patterns
                        if (deviceId.contains("android") || usn.toLowerCase().contains("android")) {
                            return "Android TV (" + deviceIP + ")";
                        } else if (deviceId.contains("roku") || usn.toLowerCase().contains("roku")) {
                            return "Roku Device (" + deviceIP + ")";
                        } else if (deviceId.toLowerCase().contains("samsung") || usn.toLowerCase().contains("samsung")) {
                            return "Samsung TV (" + deviceIP + ")";
                        } else if (deviceId.toLowerCase().contains("lg") || usn.toLowerCase().contains("lg")) {
                            return "LG TV (" + deviceIP + ")";
                        } else if (deviceId.toLowerCase().contains("sony") || deviceId.toLowerCase().contains("bravia") || 
                                   usn.toLowerCase().contains("sony") || usn.toLowerCase().contains("bravia")) {
                            return "Sony TV (" + deviceIP + ")";
                        } else if (deviceId.toLowerCase().contains("vizio") || usn.toLowerCase().contains("vizio")) {
                            return "Vizio TV (" + deviceIP + ")";
                        } else if (deviceId.toLowerCase().contains("philips") || usn.toLowerCase().contains("philips")) {
                            return "Philips TV (" + deviceIP + ")";
                        } else if (deviceId.toLowerCase().contains("toshiba") || usn.toLowerCase().contains("toshiba")) {
                            return "Toshiba TV (" + deviceIP + ")";
                        } else if (deviceId.toLowerCase().contains("hisense") || usn.toLowerCase().contains("hisense")) {
                            return "Hisense TV (" + deviceIP + ")";
                        } else if (deviceId.toLowerCase().contains("tcl") || usn.toLowerCase().contains("tcl")) {
                            return "TCL TV (" + deviceIP + ")";
                        }
                    }
                }
            }
        }
        
        return "DLNA Device (" + deviceIP + ")";
    }
    
    private boolean deviceExists(DlnaDevice newDevice) {
        for (DlnaDevice existing : discoveredDevices) {
            // For Samsung TVs and other devices that advertise multiple services,
            // we only check IP address to avoid duplicates
            if (existing.ipAddress.equals(newDevice.ipAddress)) {
                // If it's the same IP but potentially better name, update the name
                if (newDevice.name.contains("TV") && !existing.name.contains("TV")) {
                    existing.name = newDevice.name;
                }
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
    
    private void logNetworkInfo() {
        try {
            // Log WiFi state
            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wifiManager != null) {
                Log.d(TAG, "WiFi enabled: " + wifiManager.isWifiEnabled());
                android.net.wifi.WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                if (wifiInfo != null) {
                    Log.d(TAG, "WiFi SSID: " + wifiInfo.getSSID());
                    Log.d(TAG, "WiFi IP: " + intToIp(wifiInfo.getIpAddress()));
                }
            }
            
            // Log all network interfaces
            Log.d(TAG, "Network interfaces:");
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); 
                 en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                Log.d(TAG, "  Interface: " + intf.getName() + " (" + intf.getDisplayName() + ")");
                Log.d(TAG, "    Up: " + intf.isUp() + ", Multicast: " + intf.supportsMulticast());
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); 
                     enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    Log.d(TAG, "    Address: " + inetAddress.getHostAddress());
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error logging network info", e);
        }
    }
    
    private String intToIp(int ipAddress) {
        return ((ipAddress & 0xFF) + "." +
                ((ipAddress >> 8) & 0xFF) + "." +
                ((ipAddress >> 16) & 0xFF) + "." +
                ((ipAddress >> 24) & 0xFF));
    }
}