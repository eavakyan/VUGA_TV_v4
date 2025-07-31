package com.vugaenterprises.androidtv.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.vugaenterprises.androidtv.data.model.Profile
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.userDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val gson = Gson()

    companion object {
        val USER_ID = intPreferencesKey("user_id")
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_EMAIL = stringPreferencesKey("user_email")
        val USER_TOKEN = stringPreferencesKey("user_token")
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val USER_DATA_JSON = stringPreferencesKey("user_data_json")
        val SELECTED_PROFILE_JSON = stringPreferencesKey("selected_profile_json")
        val SELECTED_PROFILE_ID = intPreferencesKey("selected_profile_id")
    }

    // Save user data after successful authentication
    suspend fun saveUserData(userData: UserData) {
        context.userDataStore.edit { preferences ->
            preferences[USER_ID] = userData.id
            preferences[USER_NAME] = userData.fullname
            preferences[USER_EMAIL] = userData.email
            preferences[USER_TOKEN] = userData.token ?: ""
            preferences[IS_LOGGED_IN] = true
            preferences[USER_DATA_JSON] = gson.toJson(userData)
        }
    }

    // Get user data
    fun getUserData(): Flow<UserData?> = context.userDataStore.data
        .map { preferences ->
            val userJson = preferences[USER_DATA_JSON]
            if (userJson != null) {
                try {
                    gson.fromJson(userJson, UserData::class.java)
                } catch (e: Exception) {
                    null
                }
            } else {
                null
            }
        }

    // Check if user is logged in
    fun isLoggedIn(): Flow<Boolean> = context.userDataStore.data
        .map { preferences ->
            preferences[IS_LOGGED_IN] ?: false
        }

    // Get user ID
    fun getUserId(): Flow<Int?> = context.userDataStore.data
        .map { preferences ->
            preferences[USER_ID]
        }

    // Clear user data (logout)
    suspend fun clearUserData() {
        context.userDataStore.edit { preferences ->
            preferences.clear()
        }
    }
    
    // Save selected profile
    suspend fun saveSelectedProfile(profile: Profile) {
        context.userDataStore.edit { preferences ->
            preferences[SELECTED_PROFILE_JSON] = gson.toJson(profile)
            preferences[SELECTED_PROFILE_ID] = profile.profileId
        }
    }
    
    // Get selected profile
    fun getSelectedProfile(): Flow<Profile?> = context.userDataStore.data
        .map { preferences ->
            val profileJson = preferences[SELECTED_PROFILE_JSON]
            if (profileJson != null) {
                try {
                    gson.fromJson(profileJson, Profile::class.java)
                } catch (e: Exception) {
                    null
                }
            } else {
                null
            }
        }
    
    // Clear selected profile (for profile switching)
    suspend fun clearSelectedProfile() {
        context.userDataStore.edit { preferences ->
            preferences.remove(SELECTED_PROFILE_JSON)
            preferences.remove(SELECTED_PROFILE_ID)
        }
    }
    
    // Check if profile is selected
    fun hasSelectedProfile(): Flow<Boolean> = context.userDataStore.data
        .map { preferences ->
            preferences[SELECTED_PROFILE_ID] != null
        }
}

// User data model
data class UserData(
    val id: Int,
    val fullname: String,
    val email: String,
    val token: String? = null,
    val profileImage: String? = null,
    val isPremium: Boolean = false
)