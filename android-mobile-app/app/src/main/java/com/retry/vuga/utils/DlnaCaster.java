package com.retry.vuga.utils;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Handles DLNA/UPnP casting operations
 */
public class DlnaCaster {
    private static final String TAG = "DlnaCaster";
    
    private ExecutorService executor;
    private Handler mainHandler;
    private String controlUrl;
    private String deviceLocation;
    
    // SOAP action constants
    private static final String SET_AV_TRANSPORT_URI = "SetAVTransportURI";
    private static final String PLAY = "Play";
    private static final String PAUSE = "Pause";
    private static final String STOP = "Stop";
    private static final String GET_POSITION_INFO = "GetPositionInfo";
    
    // SOAP templates
    private static final String SOAP_ENVELOPE_START = 
        "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
        "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
        "s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
        "<s:Body>\n";
    
    private static final String SOAP_ENVELOPE_END = 
        "</s:Body>\n" +
        "</s:Envelope>";
    
    public interface OnCastListener {
        void onSuccess();
        void onError(String error);
    }
    
    public DlnaCaster() {
        this.executor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }
    
    public void connectToDevice(String deviceLocation, OnCastListener listener) {
        executor.execute(() -> {
            try {
                Log.d(TAG, "Connecting to DLNA device at: " + deviceLocation);
                
                // Fetch device description
                URL url = new URL(deviceLocation);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000); // Increased timeout
                connection.setReadTimeout(10000);
                connection.setRequestProperty("User-Agent", "Android DLNA Client/1.0");
                
                Log.d(TAG, "Fetching device description...");
                int responseCode = connection.getResponseCode();
                Log.d(TAG, "Device description response code: " + responseCode);
                
                if (responseCode == 200) {
                    // Parse device description to get control URLs
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    factory.setNamespaceAware(true);
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    Document doc = builder.parse(connection.getInputStream());
                    
                    // Log all services found
                    NodeList services = doc.getElementsByTagName("service");
                    Log.d(TAG, "Found " + services.getLength() + " services in device description");
                    
                    // Find AVTransport service control URL
                    for (int i = 0; i < services.getLength(); i++) {
                        Element service = (Element) services.item(i);
                        NodeList serviceTypes = service.getElementsByTagName("serviceType");
                        if (serviceTypes.getLength() > 0) {
                            String serviceType = serviceTypes.item(0).getTextContent();
                            Log.d(TAG, "Service " + i + " type: " + serviceType);
                            
                            if (serviceType.contains("AVTransport")) {
                                Log.d(TAG, "Found AVTransport service!");
                                NodeList controlUrls = service.getElementsByTagName("controlURL");
                                if (controlUrls.getLength() > 0) {
                                    String controlPath = controlUrls.item(0).getTextContent();
                                    Log.d(TAG, "Control path: " + controlPath);
                                    
                                    // Build full control URL
                                    URL baseUrl = new URL(deviceLocation);
                                    int port = baseUrl.getPort();
                                    if (port == -1) {
                                        port = baseUrl.getDefaultPort();
                                    }
                                    
                                    this.controlUrl = baseUrl.getProtocol() + "://" + 
                                                     baseUrl.getHost() + ":" + port + 
                                                     controlPath;
                                    this.deviceLocation = deviceLocation;
                                    
                                    Log.d(TAG, "Connected to DLNA device. Control URL: " + this.controlUrl);
                                    notifySuccess(listener);
                                    return;
                                }
                            }
                        }
                    }
                    
                    Log.e(TAG, "AVTransport service not found in device description");
                    notifyError(listener, "AVTransport service not found");
                } else {
                    Log.e(TAG, "Failed to fetch device description. Response code: " + responseCode);
                    notifyError(listener, "Failed to fetch device description (HTTP " + responseCode + ")");
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error connecting to DLNA device: " + e.getMessage(), e);
                notifyError(listener, "Connection failed: " + e.getMessage());
            }
        });
    }
    
    public void castVideo(String videoUrl, String title, String mimeType, OnCastListener listener) {
        if (controlUrl == null) {
            notifyError(listener, "Not connected to device");
            return;
        }
        
        executor.execute(() -> {
            try {
                // Build DIDL-Lite metadata
                String didlLite = buildDidlLite(videoUrl, title, mimeType);
                
                // Build SOAP request
                String soapBody = SOAP_ENVELOPE_START +
                    "<u:" + SET_AV_TRANSPORT_URI + " xmlns:u=\"urn:schemas-upnp-org:service:AVTransport:1\">\n" +
                    "<InstanceID>0</InstanceID>\n" +
                    "<CurrentURI>" + escapeXml(videoUrl) + "</CurrentURI>\n" +
                    "<CurrentURIMetaData>" + escapeXml(didlLite) + "</CurrentURIMetaData>\n" +
                    "</u:" + SET_AV_TRANSPORT_URI + ">\n" +
                    SOAP_ENVELOPE_END;
                
                // Send SOAP request
                boolean success = sendSoapRequest(SET_AV_TRANSPORT_URI, soapBody);
                
                if (success) {
                    // Start playback
                    Thread.sleep(500); // Small delay
                    play(listener);
                } else {
                    notifyError(listener, "Failed to set media URI");
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error casting video", e);
                notifyError(listener, "Cast failed: " + e.getMessage());
            }
        });
    }
    
    public void play(OnCastListener listener) {
        executor.execute(() -> {
            String soapBody = SOAP_ENVELOPE_START +
                "<u:" + PLAY + " xmlns:u=\"urn:schemas-upnp-org:service:AVTransport:1\">\n" +
                "<InstanceID>0</InstanceID>\n" +
                "<Speed>1</Speed>\n" +
                "</u:" + PLAY + ">\n" +
                SOAP_ENVELOPE_END;
            
            boolean success = sendSoapRequest(PLAY, soapBody);
            if (success) {
                notifySuccess(listener);
            } else {
                notifyError(listener, "Failed to start playback");
            }
        });
    }
    
    public void pause(OnCastListener listener) {
        executor.execute(() -> {
            String soapBody = SOAP_ENVELOPE_START +
                "<u:" + PAUSE + " xmlns:u=\"urn:schemas-upnp-org:service:AVTransport:1\">\n" +
                "<InstanceID>0</InstanceID>\n" +
                "</u:" + PAUSE + ">\n" +
                SOAP_ENVELOPE_END;
            
            boolean success = sendSoapRequest(PAUSE, soapBody);
            if (success) {
                notifySuccess(listener);
            } else {
                notifyError(listener, "Failed to pause playback");
            }
        });
    }
    
    public void stop(OnCastListener listener) {
        executor.execute(() -> {
            String soapBody = SOAP_ENVELOPE_START +
                "<u:" + STOP + " xmlns:u=\"urn:schemas-upnp-org:service:AVTransport:1\">\n" +
                "<InstanceID>0</InstanceID>\n" +
                "</u:" + STOP + ">\n" +
                SOAP_ENVELOPE_END;
            
            boolean success = sendSoapRequest(STOP, soapBody);
            if (success) {
                notifySuccess(listener);
            } else {
                notifyError(listener, "Failed to stop playback");
            }
        });
    }
    
    private boolean sendSoapRequest(String action, String soapBody) {
        try {
            Log.d(TAG, "Sending SOAP request to: " + controlUrl);
            Log.d(TAG, "Action: " + action);
            Log.d(TAG, "SOAP Body: " + soapBody);
            
            URL url = new URL(controlUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setConnectTimeout(10000); // Increased timeout
            connection.setReadTimeout(10000);
            
            // Set headers
            connection.setRequestProperty("Content-Type", "text/xml; charset=\"utf-8\"");
            connection.setRequestProperty("SOAPAction", "\"urn:schemas-upnp-org:service:AVTransport:1#" + action + "\"");
            connection.setRequestProperty("User-Agent", "Android DLNA Client/1.0");
            
            // Send request
            try (OutputStream os = connection.getOutputStream()) {
                os.write(soapBody.getBytes("UTF-8"));
                os.flush();
            }
            
            int responseCode = connection.getResponseCode();
            Log.d(TAG, "SOAP response code: " + responseCode);
            
            if (responseCode == 200) {
                // Success - read response
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()))) {
                    String line;
                    StringBuilder response = new StringBuilder();
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    Log.d(TAG, "SOAP success response: " + response.toString());
                }
                return true;
            } else {
                // Error - read error stream
                if (connection.getErrorStream() != null) {
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(connection.getErrorStream()))) {
                        String line;
                        StringBuilder error = new StringBuilder();
                        while ((line = reader.readLine()) != null) {
                            error.append(line);
                        }
                        Log.e(TAG, "SOAP error (" + responseCode + "): " + error.toString());
                    }
                } else {
                    Log.e(TAG, "SOAP error (" + responseCode + ") with no error stream");
                }
                return false;
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error sending SOAP request: " + e.getMessage(), e);
            return false;
        }
    }
    
    private String buildDidlLite(String url, String title, String mimeType) {
        return "<DIDL-Lite xmlns=\"urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/\" " +
               "xmlns:dc=\"http://purl.org/dc/elements/1.1/\" " +
               "xmlns:upnp=\"urn:schemas-upnp-org:metadata-1-0/upnp/\">" +
               "<item id=\"0\" parentID=\"-1\" restricted=\"1\">" +
               "<dc:title>" + escapeXml(title) + "</dc:title>" +
               "<upnp:class>object.item.videoItem.movie</upnp:class>" +
               "<res protocolInfo=\"http-get:*:" + mimeType + ":*\">" + escapeXml(url) + "</res>" +
               "</item>" +
               "</DIDL-Lite>";
    }
    
    private String escapeXml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&apos;");
    }
    
    private void notifySuccess(OnCastListener listener) {
        if (listener != null) {
            mainHandler.post(listener::onSuccess);
        }
    }
    
    private void notifyError(OnCastListener listener, String error) {
        if (listener != null) {
            mainHandler.post(() -> listener.onError(error));
        }
    }
    
    public void cleanup() {
        if (executor != null) {
            executor.shutdown();
        }
    }
}