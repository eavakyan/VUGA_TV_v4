package com.vugaenterprises.androidtv.utils

object TimeUtils {
    /**
     * Formats runtime in seconds to a human-readable string
     * @param durationInSeconds Duration in seconds
     * @return Formatted string like "90 min" or "2h 15min"
     */
    fun formatRuntime(durationInSeconds: Int): String {
        if (durationInSeconds <= 0) return ""
        
        val minutes = durationInSeconds / 60
        
        return when {
            minutes < 60 -> "${minutes}min"
            minutes % 60 == 0 -> "${minutes / 60}h"
            else -> {
                val hours = minutes / 60
                val remainingMinutes = minutes % 60
                "${hours}h ${remainingMinutes}min"
            }
        }
    }
    
    /**
     * Formats runtime from string duration to properly formatted runtime
     * API returns duration in seconds, this function converts to proper hour/minute display
     * Handles various input formats like "7200", "3600", etc.
     */
    fun formatRuntimeFromString(duration: String?): String {
        if (duration == null || duration.isBlank()) return ""
        
        // Extract numbers from the string
        val numbers = duration.filter { it.isDigit() }
        if (numbers.isBlank()) return duration // Return as-is if no numbers found
        
        return try {
            val durationInSeconds = numbers.toInt()
            // Use the existing formatRuntime function which properly handles seconds
            formatRuntime(durationInSeconds)
        } catch (e: NumberFormatException) {
            duration // Return original if parsing fails
        }
    }
}