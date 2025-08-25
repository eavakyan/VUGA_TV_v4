package com.vugaenterprises.androidtv.utils

object TimeUtils {
    /**
     * Formats runtime in seconds to a human-readable string
     * @param durationInSeconds Duration in seconds
     * @return Formatted string like "90 Min" or "2 H 15 Min"
     */
    fun formatRuntime(durationInSeconds: Int): String {
        if (durationInSeconds <= 0) return "0 Min"
        
        val minutes = durationInSeconds / 60
        
        return when {
            minutes == 0 -> "0 Min"
            minutes < 60 -> "$minutes Min"
            minutes % 60 == 0 -> "${minutes / 60} H"
            else -> {
                val hours = minutes / 60
                val remainingMinutes = minutes % 60
                "$hours H $remainingMinutes Min"
            }
        }
    }
    
    /**
     * Formats runtime from string duration to properly formatted runtime
     * API returns duration in seconds, this function converts to proper hour/minute display
     * Handles various input formats like "7200", "3600", etc.
     */
    fun formatRuntimeFromString(duration: String?): String {
        if (duration == null || duration.isBlank()) return "0 Min"
        
        // Extract numbers from the string
        val numbers = duration.filter { it.isDigit() }
        if (numbers.isBlank()) return "0 Min" // Return 0 Min if no numbers found
        
        return try {
            val durationValue = numbers.toInt()
            
            // Check if the value is likely in minutes (less than 300) or seconds (greater)
            // Most episodes are between 20-60 minutes, so values under 300 are likely minutes
            if (durationValue < 300) {
                // Treat as minutes
                when {
                    durationValue == 0 -> "0 Min"
                    durationValue < 60 -> "$durationValue Min"
                    durationValue % 60 == 0 -> "${durationValue / 60} H"
                    else -> {
                        val hours = durationValue / 60
                        val remainingMinutes = durationValue % 60
                        "$hours H $remainingMinutes Min"
                    }
                }
            } else {
                // Treat as seconds
                formatRuntime(durationValue)
            }
        } catch (e: NumberFormatException) {
            "0 Min" // Return 0 Min if parsing fails
        }
    }
}