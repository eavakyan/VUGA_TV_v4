package com.retry.vuga.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;

import com.retry.vuga.R;
import com.retry.vuga.adapters.ProfileAdapter;
import com.retry.vuga.databinding.ActivityProfileSelectionBinding;
import com.retry.vuga.model.Profile;
import com.retry.vuga.model.ProfileResponse;
import com.retry.vuga.model.UserRegistration;
import com.retry.vuga.retrofit.RetrofitClient;
import com.retry.vuga.utils.Const;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class ProfileSelectionActivity extends BaseActivity implements ProfileAdapter.OnProfileClickListener {
    
    private ActivityProfileSelectionBinding binding;
    private CompositeDisposable disposable;
    private ProfileAdapter profileAdapter;
    private List<Profile> profileList = new ArrayList<>();
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_profile_selection);
        disposable = new CompositeDisposable();
        
        setupRecyclerView();
        setupListeners();
        loadProfiles();
    }

    private void setupRecyclerView() {
        profileAdapter = new ProfileAdapter(this, profileList, this);
        binding.rvProfiles.setLayoutManager(new GridLayoutManager(this, 2));
        binding.rvProfiles.setAdapter(profileAdapter);
    }

    private void setupListeners() {
        binding.btnManageProfiles.setOnClickListener(v -> {
            isEditMode = !isEditMode;
            profileAdapter.setEditMode(isEditMode);
            binding.btnManageProfiles.setText(isEditMode ? "Done" : "Manage Profiles");
        });

        binding.btnAddProfile.setOnClickListener(v -> {
            if (profileList.size() < 4) {
                Intent intent = new Intent(this, CreateProfileActivity.class);
                startActivityForResult(intent, 100);
            } else {
                Toast.makeText(this, "Maximum 4 profiles allowed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadProfiles() {
        binding.progressBar.setVisibility(View.VISIBLE);
        
        int userId = sessionManager.getUser().getId();
        
        disposable.add(RetrofitClient.getService()
                .getUserProfiles(userId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(d -> binding.progressBar.setVisibility(View.VISIBLE))
                .doOnTerminate(() -> binding.progressBar.setVisibility(View.GONE))
                .subscribe((response, throwable) -> {
                    if (throwable != null) {
                        Log.e("ProfileSelection", "Error loading profiles: " + throwable.getMessage());
                        Toast.makeText(this, "Error: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    } else if (response != null && response.isStatus()) {
                        profileList.clear();
                        if (response.getProfiles() != null) {
                            profileList.addAll(response.getProfiles());
                            // Debug log the profiles
                            for (Profile p : response.getProfiles()) {
                                Log.d("ProfileSelection", "Profile loaded: " + p.getName() +
                                    ", Type: " + p.getAvatarType() +
                                    ", URL: " + p.getAvatarUrl() +
                                    ", Color: " + p.getAvatarColor());
                            }
                        }
                        profileAdapter.notifyDataSetChanged();
                        
                        // Update current profile in session if it was edited
                        UserRegistration.Data userData = sessionManager.getUser();
                        if (userData != null && userData.getLastActiveProfile() != null) {
                            int currentProfileId = userData.getLastActiveProfile().getProfileId();
                            for (Profile profile : profileList) {
                                if (profile.getProfileId() == currentProfileId) {
                                    // Update the session with latest profile data
                                    UserRegistration.Profile updatedProfile = new UserRegistration.Profile();
                                    updatedProfile.setProfileId(profile.getProfileId());
                                    updatedProfile.setName(profile.getName());
                                    updatedProfile.setAvatarType(profile.getAvatarType());
                                    updatedProfile.setAvatarUrl(profile.getAvatarUrl());
                                    updatedProfile.setAvatarColor(profile.getAvatarColor());
                                    updatedProfile.setKids(profile.isKids());
                                    userData.setLastActiveProfile(updatedProfile);
                                    userData.setLastActiveProfileId(profile.getProfileId());
                                    sessionManager.saveUser(userData);
                                    
                                    // Send broadcast to notify about profile change
                                    Intent intent = new Intent("com.retry.vuga.PROFILE_CHANGED");
                                    intent.putExtra("profile_id", profile.getProfileId());
                                    LocalBroadcastManager.getInstance(ProfileSelectionActivity.this).sendBroadcast(intent);
                                    
                                    break;
                                }
                            }
                        }
                        
                        // Show/hide add profile button
                        binding.btnAddProfile.setVisibility(profileList.size() < 4 ? View.VISIBLE : View.GONE);
                    } else {
                        String message = response != null && response.getMessage() != null ? 
                            response.getMessage() : "Failed to load profiles";
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                    }
                }));
    }

    @Override
    public void onProfileClick(Profile profile) {
        if (isEditMode) {
            // Edit profile
            Intent intent = new Intent(this, CreateProfileActivity.class);
            intent.putExtra("profile_id", profile.getProfileId());
            intent.putExtra("profile_name", profile.getName());
            intent.putExtra("profile_color", profile.getAvatarColor());
            intent.putExtra("avatar_id", profile.getAvatarId());
            intent.putExtra("is_kids", profile.isKids());
            startActivityForResult(intent, 100);
        } else {
            // Select profile
            selectProfile(profile);
        }
    }

    @Override
    public void onDeleteClick(Profile profile) {
        if (profileList.size() > 1) {
            deleteProfile(profile);
        } else {
            Toast.makeText(this, "You must keep at least one profile", Toast.LENGTH_SHORT).show();
        }
    }

    private void selectProfile(Profile profile) {
        binding.progressBar.setVisibility(View.VISIBLE);
        
        int userId = sessionManager.getUser().getId();
        
        disposable.add(RetrofitClient.getService()
                .selectProfile(userId, profile.getProfileId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnTerminate(() -> binding.progressBar.setVisibility(View.GONE))
                .subscribe((response, throwable) -> {
                    if (response != null && response.getStatus()) {
                        // Profile switch successful - update local data and go to main
                        UserRegistration.Data userData = sessionManager.getUser();
                        UserRegistration.Profile selectedProfile = new UserRegistration.Profile();
                        selectedProfile.setProfileId(profile.getProfileId());
                        selectedProfile.setName(profile.getName());
                        selectedProfile.setAvatarType(profile.getAvatarType());
                        selectedProfile.setAvatarUrl(profile.getAvatarUrl());
                        selectedProfile.setAvatarColor(profile.getAvatarColor());
                        selectedProfile.setKids(profile.isKids());
                        userData.setLastActiveProfile(selectedProfile);
                        userData.setLastActiveProfileId(profile.getProfileId());
                        
                        // Don't clear watchlist - MainActivity should fetch fresh data
                        sessionManager.saveUser(userData);
                        
                        // Send broadcast to notify about profile change
                        Intent intent = new Intent("com.retry.vuga.PROFILE_CHANGED");
                        intent.putExtra("profile_id", profile.getProfileId());
                        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                        
                        goToMainActivity();
                    } else {
                        Toast.makeText(this, "Failed to select profile", Toast.LENGTH_SHORT).show();
                    }
                }));
    }

    private void goToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("profile_switched", true);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    private void deleteProfile(Profile profile) {
        binding.progressBar.setVisibility(View.VISIBLE);
        
        int userId = sessionManager.getUser().getId();
        
        disposable.add(RetrofitClient.getService()
                .deleteProfile(profile.getProfileId(), userId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnTerminate(() -> binding.progressBar.setVisibility(View.GONE))
                .subscribe((response, throwable) -> {
                    if (response != null && response.getStatus()) {
                        Toast.makeText(this, "Profile deleted successfully", Toast.LENGTH_SHORT).show();
                        loadProfiles();
                    } else {
                        String message = response != null ? response.getMessage() : "Failed to delete profile";
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                    }
                }));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            loadProfiles();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }
}