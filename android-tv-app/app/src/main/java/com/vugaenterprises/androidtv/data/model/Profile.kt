package com.vugaenterprises.androidtv.data.model

import com.google.gson.annotations.SerializedName

data class Profile(
    @SerializedName("profile_id")
    val profileId: Int,
    
    @SerializedName("app_user_id")
    val appUserId: Int? = null,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("avatar_type")
    val avatarType: String,
    
    @SerializedName("avatar_url")
    val avatarUrl: String? = null,
    
    @SerializedName("avatar_color")
    val avatarColor: String,
    
    @SerializedName("avatar_id")
    val avatarId: Int? = null,
    
    @SerializedName("is_kids")
    val isKids: Boolean = false,
    
    @SerializedName("is_active")
    val isActive: Boolean? = null,
    
    @SerializedName("created_at")
    val createdAt: String? = null,
    
    @SerializedName("updated_at")
    val updatedAt: String? = null
) {
    // Helper to get the display initial
    val initial: String
        get() = name.take(1).uppercase()
        
    // Helper to check if it's a kids profile
    val isKidsProfile: Boolean
        get() = isKids
}

data class ProfilesResponse(
    @SerializedName("status")
    val status: Boolean,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("profiles")
    val profiles: List<Profile>? = null,
    
    @SerializedName("profile")
    val profile: Profile? = null
)

data class CreateProfileRequest(
    @SerializedName("user_id")
    val userId: Int,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("avatar_id")
    val avatarId: Int,
    
    @SerializedName("is_kids")
    val isKids: Int, // API expects 0 or 1
    
    @SerializedName("avatar_type")
    val avatarType: String = "color"
)

data class SelectProfileRequest(
    @SerializedName("user_id")
    val userId: Int,
    
    @SerializedName("profile_id")
    val profileId: Int
)

// Avatar color options (matching iOS app)
object ProfileColors {
    val colors = listOf(
        "#FF6B6B", // Red
        "#4ECDC4", // Teal
        "#45B7D1", // Blue
        "#96CEB4", // Green
        "#FECA57", // Yellow
        "#DDA0DD", // Plum
        "#FF8B94", // Pink
        "#B4A7D6"  // Lavender
    )
    
    fun getColorForId(avatarId: Int): String {
        return colors.getOrElse(avatarId - 1) { colors[0] }
    }
}