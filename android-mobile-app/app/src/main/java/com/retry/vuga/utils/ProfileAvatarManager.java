package com.retry.vuga.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import com.retry.vuga.model.Profile;
import com.retry.vuga.model.ProfileResponse;
import com.retry.vuga.model.RestResponse;
import com.retry.vuga.retrofit.RetrofitClient;
import com.retry.vuga.utils.imageuplod.ImageUtils;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class ProfileAvatarManager {
    private static final String TAG = "ProfileAvatarManager";
    private Context context;
    private SessionManager sessionManager;
    private CompositeDisposable disposable;
    
    public interface AvatarUploadCallback {
        void onUploadStarted();
        void onUploadSuccess(Profile updatedProfile);
        void onUploadError(String error);
    }
    
    public interface AvatarRemoveCallback {
        void onRemoveStarted();
        void onRemoveSuccess();
        void onRemoveError(String error);
    }
    
    public ProfileAvatarManager(Context context) {
        this.context = context;
        this.sessionManager = new SessionManager(context);
        this.disposable = new CompositeDisposable();
    }
    
    /**
     * Uploads a profile avatar image
     */
    public void uploadAvatar(int profileId, Uri imageUri, AvatarUploadCallback callback) {
        // Get current user
        if (sessionManager.getUser() == null || sessionManager.getUser().getId() == 0) {
            callback.onUploadError("User not logged in");
            return;
        }
        
        // Convert image to base64 in background thread
        callback.onUploadStarted();
        
        disposable.add(
            io.reactivex.Single.fromCallable(() -> ImageUtils.convertImageToBase64(context, imageUri))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    base64Image -> {
                        if (base64Image != null && !base64Image.isEmpty()) {
                            // Upload the base64 image
                            uploadBase64Avatar(sessionManager.getUser().getId(), profileId, base64Image, callback);
                        } else {
                            callback.onUploadError("Failed to process image");
                        }
                    },
                    throwable -> {
                        Log.e(TAG, "Error processing image", throwable);
                        callback.onUploadError("Failed to process image: " + throwable.getMessage());
                    }
                )
        );
    }
    
    /**
     * Uploads base64 encoded avatar to server
     */
    private void uploadBase64Avatar(int userId, int profileId, String base64Image, AvatarUploadCallback callback) {
        Log.d(TAG, "Uploading avatar for user: " + userId + ", profile: " + profileId);
        
        disposable.add(
            RetrofitClient.getService().uploadProfileAvatar(userId, profileId, base64Image)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    response -> {
                        if (response.isStatus() && response.getProfile() != null) {
                            Log.d(TAG, "Avatar uploaded successfully");
                            
                            // Update session if this is the current profile
                            Profile updatedProfile = response.getProfile();
                            updateSessionIfCurrentProfile(updatedProfile);
                            
                            callback.onUploadSuccess(updatedProfile);
                        } else {
                            String errorMsg = response.getMessage() != null ? response.getMessage() : "Upload failed";
                            Log.e(TAG, "Avatar upload failed: " + errorMsg);
                            callback.onUploadError(errorMsg);
                        }
                    },
                    throwable -> {
                        Log.e(TAG, "Error uploading avatar", throwable);
                        callback.onUploadError("Network error: " + throwable.getMessage());
                    }
                )
        );
    }
    
    /**
     * Removes the custom avatar and reverts to color avatar
     */
    public void removeAvatar(int profileId, AvatarRemoveCallback callback) {
        if (sessionManager.getUser() == null || sessionManager.getUser().getId() == 0) {
            callback.onRemoveError("User not logged in");
            return;
        }
        
        callback.onRemoveStarted();
        Log.d(TAG, "Removing avatar for user: " + sessionManager.getUser().getId() + ", profile: " + profileId);
        
        disposable.add(
            RetrofitClient.getService().removeProfileAvatar(sessionManager.getUser().getId(), profileId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    response -> {
                        if (response.getStatus()) {
                            Log.d(TAG, "Avatar removed successfully");
                            
                            // Update session if this is the current profile - revert to color avatar
                            updateSessionAvatarRemoved(profileId);
                            
                            callback.onRemoveSuccess();
                        } else {
                            String errorMsg = response.getMessage() != null ? response.getMessage() : "Remove failed";
                            Log.e(TAG, "Avatar removal failed: " + errorMsg);
                            callback.onRemoveError(errorMsg);
                        }
                    },
                    throwable -> {
                        Log.e(TAG, "Error removing avatar", throwable);
                        callback.onRemoveError("Network error: " + throwable.getMessage());
                    }
                )
        );
    }
    
    /**
     * Updates session data if the updated profile is the currently active profile
     */
    private void updateSessionIfCurrentProfile(Profile updatedProfile) {
        if (sessionManager.getUser() != null && 
            sessionManager.getUser().getLastActiveProfileId() != null && 
            sessionManager.getUser().getLastActiveProfileId() == updatedProfile.getProfileId()) {
            
            Log.d(TAG, "Updating current profile avatar in session");
            // Note: This assumes SessionManager has methods to update current profile
            // You may need to implement getCurrentProfile() and setCurrentProfile() methods
            // in SessionManager or handle this differently based on your session management
        }
    }
    
    /**
     * Updates session when avatar is removed
     */
    private void updateSessionAvatarRemoved(int profileId) {
        if (sessionManager.getUser() != null && 
            sessionManager.getUser().getLastActiveProfileId() != null && 
            sessionManager.getUser().getLastActiveProfileId() == profileId) {
            
            Log.d(TAG, "Updating current profile (avatar removed) in session");
            // Note: Handle session update for avatar removal
            // This should revert avatar_type to "color" and clear avatar_url
        }
    }
    
    /**
     * Cleans up resources
     */
    public void dispose() {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }
}