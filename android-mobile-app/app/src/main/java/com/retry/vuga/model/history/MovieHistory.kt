package com.retry.vuga.model.history

import com.retry.vuga.model.ContentDetail.SourceItem

class MovieHistory(
    var id: Int? = null,
    var movieId: Int? = null,
    var movieName: String? = null,
    var thumbnail: String? = null,
    var time: Long? = null,
    var sources: ArrayList<SourceItem>? = null,
    var releaseYear: Int? = null,
    var duration: String? = null
) {
    /**
     * Formats duration from seconds to "X hr Y min" format
     */
    val formattedDuration: String
        get() {
            if (duration.isNullOrEmpty()) {
                return ""
            }
            
            return try {
                // Parse duration as integer (seconds from database)
                val totalSeconds = duration!!.toInt()
                
                // Convert seconds to minutes
                val totalMinutes = totalSeconds / 60
                
                if (totalMinutes < 60) {
                    // Under 1 hour - show only minutes
                    "$totalMinutes min"
                } else {
                    // 1 hour or more - show hours and minutes
                    val hours = totalMinutes / 60
                    val minutes = totalMinutes % 60
                    
                    if (minutes == 0) {
                        "$hours hr"
                    } else {
                        "$hours hr $minutes min"
                    }
                }
            } catch (e: NumberFormatException) {
                // If duration is not a number, return as is
                duration ?: ""
            }
        }
}
