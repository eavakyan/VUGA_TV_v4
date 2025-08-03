package com.vugaenterprises.androidtv.data.model

import com.google.gson.annotations.SerializedName

data class AgeRating(
    @SerializedName("age_limit_id")
    val id: Int,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("min_age")
    val minAge: Int,
    
    @SerializedName("max_age")
    val maxAge: Int? = null,
    
    @SerializedName("description")
    val description: String? = null,
    
    @SerializedName("code")
    val code: String,
    
    @SerializedName("created_at")
    val createdAt: String? = null,
    
    @SerializedName("updated_at")
    val updatedAt: String? = null
) {
    // Helper to check if this rating is appropriate for kids
    val isKidsFriendly: Boolean
        get() = maxAge != null && maxAge <= 12
        
    // Helper to check if a given age can access this content
    fun canAccessByAge(age: Int?): Boolean {
        return age == null || age >= minAge
    }
    
    // Get display color based on age group
    val displayColor: String
        get() = when (code) {
            "AG_0_6" -> "#4CAF50" // Green
            "AG_7_12" -> "#8BC34A" // Light Green
            "AG_13_16" -> "#FF9800" // Orange
            "AG_17_18" -> "#F44336" // Red
            "AG_18_PLUS" -> "#9C27B0" // Purple
            else -> "#757575" // Gray
        }
}

data class AgeRatingResponse(
    @SerializedName("status")
    val status: Boolean,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("age_ratings")
    val ageRatings: List<AgeRating>? = null
)