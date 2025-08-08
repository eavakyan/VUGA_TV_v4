package com.retry.vuga.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.retry.vuga.model.ContentDetail;
import com.retry.vuga.model.Trailer;

import java.util.List;

/**
 * Utility class for handling trailer functionality across the app
 */
public class TrailerUtils {
    
    private static final String TAG = "TrailerUtils";
    
    /**
     * Get the effective trailer URL for a content item
     * Prioritizes new trailers array, falls back to legacy trailer_url
     */
    public static String getEffectiveTrailerUrl(ContentDetail.DataItem content) {
        if (content == null) {
            return "";
        }
        
        // Try to get primary trailer from trailers list first
        Trailer primaryTrailer = getPrimaryTrailer(content);
        if (primaryTrailer != null && !primaryTrailer.getTrailerUrl().isEmpty()) {
            return primaryTrailer.getTrailerUrl();
        }
        
        // Fall back to legacy trailer_url field
        return content.getTrailerUrl();
    }
    
    /**
     * Get the effective YouTube ID for a content item
     */
    public static String getEffectiveYouTubeId(ContentDetail.DataItem content) {
        if (content == null) {
            return "";
        }
        
        // Try to get primary trailer from trailers list first
        Trailer primaryTrailer = getPrimaryTrailer(content);
        if (primaryTrailer != null && !primaryTrailer.getYoutubeId().isEmpty()) {
            return primaryTrailer.getYoutubeId();
        }
        
        // Fall back to extracting from legacy trailer_url field
        return extractYouTubeId(content.getTrailerUrl());
    }
    
    /**
     * Get the primary trailer from a content item
     */
    public static Trailer getPrimaryTrailer(ContentDetail.DataItem content) {
        if (content == null) {
            return null;
        }
        
        return content.getPrimaryTrailer();
    }
    
    /**
     * Get all trailers for a content item, sorted properly
     */
    public static List<Trailer> getAllTrailers(ContentDetail.DataItem content) {
        if (content == null) {
            return null;
        }
        
        List<Trailer> trailers = content.getTrailers();
        if (trailers != null && !trailers.isEmpty()) {
            return trailers;
        }
        
        return null;
    }
    
    /**
     * Check if content has any trailers
     */
    public static boolean hasTrailers(ContentDetail.DataItem content) {
        if (content == null) {
            return false;
        }
        
        // Check new trailers list
        List<Trailer> trailers = content.getTrailers();
        if (trailers != null && !trailers.isEmpty()) {
            return true;
        }
        
        // Check legacy trailer_url
        String trailerUrl = content.getTrailerUrl();
        return trailerUrl != null && !trailerUrl.isEmpty();
    }
    
    /**
     * Get trailer thumbnail URL for display in UI
     */
    public static String getTrailerThumbnailUrl(ContentDetail.DataItem content) {
        if (content == null) {
            return "";
        }
        
        // Try to get from primary trailer
        Trailer primaryTrailer = getPrimaryTrailer(content);
        if (primaryTrailer != null) {
            String thumbnailUrl = primaryTrailer.getThumbnailUrl();
            if (!thumbnailUrl.isEmpty()) {
                return thumbnailUrl;
            }
            
            // Generate from YouTube ID if available
            String youtubeId = primaryTrailer.getYoutubeId();
            if (!youtubeId.isEmpty()) {
                return "https://img.youtube.com/vi/" + youtubeId + "/maxresdefault.jpg";
            }
        }
        
        // Fall back to generating from legacy trailer_url
        String youtubeId = extractYouTubeId(content.getTrailerUrl());
        if (!youtubeId.isEmpty()) {
            return "https://img.youtube.com/vi/" + youtubeId + "/maxresdefault.jpg";
        }
        
        return "";
    }
    
    /**
     * Open trailer in YouTube app or web browser
     */
    public static void openTrailer(Context context, ContentDetail.DataItem content) {
        if (context == null || content == null) {
            return;
        }
        
        String trailerUrl = getEffectiveTrailerUrl(content);
        if (trailerUrl.isEmpty()) {
            Log.w(TAG, "No trailer URL available for content: " + content.getTitle());
            return;
        }
        
        try {
            // Try to open in YouTube app first
            String youtubeId = getEffectiveYouTubeId(content);
            if (!youtubeId.isEmpty()) {
                Intent youtubeIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + youtubeId));
                youtubeIntent.putExtra("force_fullscreen", true);
                
                if (youtubeIntent.resolveActivity(context.getPackageManager()) != null) {
                    context.startActivity(youtubeIntent);
                    return;
                }
            }
            
            // Fall back to web browser
            Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(trailerUrl));
            context.startActivity(webIntent);
            
        } catch (Exception e) {
            Log.e(TAG, "Error opening trailer: " + e.getMessage(), e);
        }
    }
    
    /**
     * Extract YouTube ID from various URL formats
     */
    public static String extractYouTubeId(String url) {
        if (url == null || url.isEmpty()) {
            return "";
        }
        
        try {
            // Standard YouTube URL: https://www.youtube.com/watch?v=VIDEO_ID
            if (url.contains("youtube.com/watch?v=")) {
                String[] parts = url.split("v=");
                if (parts.length > 1) {
                    String id = parts[1].split("&")[0]; // Remove additional parameters
                    if (id.length() == 11) {
                        return id;
                    }
                }
            }
            
            // YouTube short URL: https://youtu.be/VIDEO_ID
            if (url.contains("youtu.be/")) {
                String[] parts = url.split("youtu.be/");
                if (parts.length > 1) {
                    String id = parts[1].split("\\?")[0]; // Remove parameters
                    if (id.length() == 11) {
                        return id;
                    }
                }
            }
            
            // YouTube embed URL: https://www.youtube.com/embed/VIDEO_ID
            if (url.contains("youtube.com/embed/")) {
                String[] parts = url.split("embed/");
                if (parts.length > 1) {
                    String id = parts[1].split("\\?")[0]; // Remove parameters
                    if (id.length() == 11) {
                        return id;
                    }
                }
            }
            
            // Check if it's already just the ID
            if (url.length() == 11 && url.matches("^[A-Za-z0-9_-]{11}$")) {
                return url;
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error extracting YouTube ID from URL: " + url, e);
        }
        
        return "";
    }
    
    /**
     * Generate YouTube embed URL from ID
     */
    public static String generateEmbedUrl(String youtubeId) {
        if (youtubeId == null || youtubeId.isEmpty()) {
            return "";
        }
        return "https://www.youtube.com/embed/" + youtubeId;
    }
    
    /**
     * Generate YouTube watch URL from ID
     */
    public static String generateWatchUrl(String youtubeId) {
        if (youtubeId == null || youtubeId.isEmpty()) {
            return "";
        }
        return "https://www.youtube.com/watch?v=" + youtubeId;
    }
}