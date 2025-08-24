package com.vugaenterprises.androidtv.utils

object CategoryUtils {
    // Map of category IDs to names based on common streaming service categories
    private val categoryMap = mapOf(
        "1" to "Action",
        "2" to "Adventure",
        "3" to "Animation",
        "4" to "Comedy",
        "5" to "Crime",
        "6" to "Documentary",
        "7" to "Drama",
        "8" to "Family",
        "9" to "Fantasy",
        "10" to "Horror",
        "11" to "Mystery",
        "12" to "Romance",
        "13" to "Sci-Fi",
        "14" to "Thriller",
        "15" to "War",
        "16" to "Western",
        "17" to "Biography",
        "18" to "History",
        "19" to "Music",
        "20" to "Musical",
        "21" to "Sport",
        "22" to "Anime",
        "23" to "Kids",
        "24" to "Reality TV",
        "25" to "Talk Show",
        "26" to "Game Show",
        "27" to "News",
        "28" to "HBO",
        "29" to "Cinemax",
        "30" to "Showtime",
        "31" to "Netflix Original",
        "32" to "Amazon Original",
        "33" to "Apple TV+",
        "34" to "Disney+",
        "35" to "Hulu Original"
    )
    
    /**
     * Convert comma-separated category IDs to names
     * Example: "1,5,7" -> ["Action", "Crime", "Drama"]
     */
    fun getCategoryNames(categoryIds: String): List<String> {
        if (categoryIds.isBlank()) return emptyList()
        
        return categoryIds.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .mapNotNull { categoryMap[it] }
    }
    
    /**
     * Get a single category name from ID
     */
    fun getCategoryName(categoryId: String): String {
        return categoryMap[categoryId.trim()] ?: "Unknown"
    }
    
    /**
     * Format category names as a display string
     * Example: ["Action", "Crime", "Drama"] -> "Action • Crime • Drama"
     */
    fun formatCategories(categoryIds: String): String {
        val names = getCategoryNames(categoryIds)
        return if (names.isNotEmpty()) {
            names.joinToString(" • ")
        } else {
            ""
        }
    }
}