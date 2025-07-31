package com.vugaenterprises.androidtv.data.repository

import com.vugaenterprises.androidtv.data.api.ApiService
import com.vugaenterprises.androidtv.data.model.CreateProfileRequest
import com.vugaenterprises.androidtv.data.model.Profile
import com.vugaenterprises.androidtv.data.model.ProfilesResponse
import com.vugaenterprises.androidtv.data.model.SelectProfileRequest
import com.vugaenterprises.androidtv.data.UserDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor(
    private val apiService: ApiService,
    private val userDataStore: UserDataStore
) {
    
    suspend fun getUserProfiles(): Flow<Result<List<Profile>>> = flow {
        try {
            val userId = userDataStore.getUserId().first()
            if (userId != null) {
                val response = apiService.getUserProfiles(userId)
                if (response.status && response.profiles != null) {
                    emit(Result.success(response.profiles))
                } else {
                    emit(Result.failure(Exception(response.message)))
                }
            } else {
                emit(Result.failure(Exception("User not logged in")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
    
    suspend fun createProfile(
        name: String,
        avatarId: Int,
        isKids: Boolean
    ): Flow<Result<Profile>> = flow {
        try {
            val userId = userDataStore.getUserId().first()
            if (userId != null) {
                val request = CreateProfileRequest(
                    userId = userId,
                    name = name,
                    avatarId = avatarId,
                    isKids = if (isKids) 1 else 0,
                    avatarType = "color"
                )
                
                val response = apiService.createProfile(request)
                if (response.status && response.profile != null) {
                    emit(Result.success(response.profile))
                } else {
                    emit(Result.failure(Exception(response.message)))
                }
            } else {
                emit(Result.failure(Exception("User not logged in")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
    
    suspend fun deleteProfile(profileId: Int): Flow<Result<Boolean>> = flow {
        try {
            val userId = userDataStore.getUserId().first()
            if (userId != null) {
                val response = apiService.deleteProfile(userId, profileId)
                if (response.status) {
                    emit(Result.success(true))
                } else {
                    emit(Result.failure(Exception(response.message)))
                }
            } else {
                emit(Result.failure(Exception("User not logged in")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
    
    suspend fun selectProfile(profile: Profile): Flow<Result<Profile>> = flow {
        try {
            val userId = userDataStore.getUserId().first()
            if (userId != null) {
                val request = SelectProfileRequest(userId = userId, profileId = profile.profileId)
                val response = apiService.selectProfile(request)
                
                if (response.status) {
                    // Save the selected profile locally
                    userDataStore.saveSelectedProfile(profile)
                    emit(Result.success(profile))
                } else {
                    emit(Result.failure(Exception(response.message)))
                }
            } else {
                emit(Result.failure(Exception("User not logged in")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}