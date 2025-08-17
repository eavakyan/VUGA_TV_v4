package com.retry.vuga.activities;

import android.app.NotificationManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import androidx.annotation.Nullable;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;
import com.retry.vuga.BuildConfig;
import com.retry.vuga.R;
import com.retry.vuga.bottomSheets.WebBottomSheet;
import com.retry.vuga.databinding.ActivityProfileBinding;
import com.retry.vuga.retrofit.RetrofitClient;
import com.retry.vuga.utils.Const;
import com.retry.vuga.utils.CustomDialogBuilder;
import com.retry.vuga.utils.GoogleLoginManager;
import com.retry.vuga.model.UserRegistration;
import com.revenuecat.purchases.Purchases;

import org.jetbrains.annotations.NotNull;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class ProfileActivity extends BaseActivity {
    ActivityProfileBinding binding;
    WebBottomSheet webBottomSheet;
    CompositeDisposable disposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_profile);
        disposable = new CompositeDisposable();
        setBlur(binding.blurView, binding.rootLout, 10f);
        setUserDetail();
        setListeners();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh user details when returning from EditProfileActivity
        setUserDetail();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            // Profile was updated, refresh the user data
            fetchUpdatedUserData();
        }
    }
    
    private void fetchUpdatedUserData() {
        // Fetch updated profile data from API
        disposable.add(RetrofitClient.getService()
                .getUserProfiles(sessionManager.getUser().getId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((response, throwable) -> {
                    if (response != null && response.isStatus()) {
                        // Update the current profile in session
                        if (sessionManager.getUser() != null && 
                            sessionManager.getUser().getLastActiveProfile() != null && 
                            response.getProfiles() != null) {
                            
                            int currentProfileId = sessionManager.getUser().getLastActiveProfile().getProfileId();
                            for (com.retry.vuga.model.Profile profile : response.getProfiles()) {
                                if (profile.getProfileId() == currentProfileId) {
                                    // Update the active profile in session
                                    UserRegistration.Profile updatedProfile = new UserRegistration.Profile();
                                    updatedProfile.setProfileId(profile.getProfileId());
                                    updatedProfile.setName(profile.getName());
                                    updatedProfile.setAvatarType(profile.getAvatarType());
                                    updatedProfile.setAvatarUrl(profile.getAvatarUrl());
                                    updatedProfile.setAvatarColor(profile.getAvatarColor());
                                    updatedProfile.setKids(profile.isKids());
                                    
                                    UserRegistration.Data userData = sessionManager.getUser();
                                    userData.setLastActiveProfile(updatedProfile);
                                    sessionManager.saveUser(userData);
                                    break;
                                }
                            }
                        }
                        setUserDetail(); // Refresh the UI
                    }
                }));
    }

    private void setListeners() {

        binding.centerLoader.setOnClickListener(v -> {

        });

        binding.blurView.setOnClickListener(v -> {

        });

        binding.btnBack.setOnClickListener(v -> {
            getOnBackPressedDispatcher().onBackPressed();
        });

        binding.loutPrivacy.setOnClickListener(v -> {
            webBottomSheet = new WebBottomSheet(1);
            if (!webBottomSheet.isAdded()) {
                webBottomSheet.show(getSupportFragmentManager(), webBottomSheet.getClass().getSimpleName());
            }
        });

        binding.loutTearms.setOnClickListener(v -> {
            webBottomSheet = new WebBottomSheet(2);
            if (!webBottomSheet.isAdded()) {
                webBottomSheet.show(getSupportFragmentManager(), webBottomSheet.getClass().getSimpleName());
            }
        });

        binding.loutPro.setOnClickListener(v -> {

            startActivity(new Intent(this, ProActivity.class));

        });
        binding.loutRate.setOnClickListener(v -> {

            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID)));

        });
        binding.loutMarketing.setOnClickListener(v -> {
            startActivity(new Intent(this, MarketingPreferencesActivity.class));
        });

        binding.loutDownloads.setOnClickListener(v -> {
            startActivity(new Intent(this, DownloadsActivity.class));

        });

        binding.loutLanguage.setOnClickListener(v -> {
            startActivity(new Intent(this, LanguageActivity.class));

        });
        
        binding.loutAgeSettings.setOnClickListener(v -> {
            // TODO: Temporarily disabled age settings
            // startActivity(new Intent(this, AgeSettingsActivity.class));
            Toast.makeText(this, "Age settings temporarily unavailable", Toast.LENGTH_SHORT).show();
        });

        binding.loutTvConnect.setOnClickListener(v -> {
            startActivity(new Intent(this, QRScannerActivity.class));
        });

        binding.loutSwitchProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileSelectionActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        binding.imgEdit.setOnClickListener(v -> {
            // Launch CreateProfileActivity in edit mode for the current profile
            if (sessionManager.getUser().getLastActiveProfile() != null) {
                UserRegistration.Profile profile = sessionManager.getUser().getLastActiveProfile();
                Intent intent = new Intent(this, CreateProfileActivity.class);
                intent.putExtra("profile_id", profile.getProfileId());
                intent.putExtra("profile_name", profile.getName());
                intent.putExtra("profile_color", profile.getAvatarColor());
                intent.putExtra("profile_avatar_url", profile.getAvatarUrl());
                intent.putExtra("is_kids", profile.isKids());
                startActivityForResult(intent, 100);
            } else {
                // Fallback to old EditProfileActivity for user-level editing
                startActivity(new Intent(this, EditProfileActivity.class));
            }
        });

        binding.loutLogOut.setOnClickListener(v -> {

            showLogoutPopUp();
        });

        binding.loutDelete.setOnClickListener(v -> {

            showDeletePopUp();

        });

        binding.switchNoti.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if (isChecked) {

                sessionManager.saveBooleanValue(Const.DataKey.NOTIFICATION, true);
                FirebaseMessaging.getInstance().subscribeToTopic(Const.FIREBASE_SUB_TOPIC);

            } else {
                NotificationManager notificationManager = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
                notificationManager.cancelAll();
                sessionManager.saveBooleanValue(Const.DataKey.NOTIFICATION, false);
                FirebaseMessaging.getInstance().unsubscribeFromTopic(Const.FIREBASE_SUB_TOPIC);


            }
        });


    }

    private void showLogoutPopUp() {
        binding.blurView.setVisibility(View.VISIBLE);

        new CustomDialogBuilder(this).showLogoutDialog(new CustomDialogBuilder.OnDismissListener() {
            @Override
            public void onPositiveDismiss() {



                if (sessionManager.getUser().getLoginType() == 4) {

                    FirebaseAuth.getInstance().signOut();
                    logoutApi();


                }


                if (sessionManager.getUser().getLoginType() == 1) {

                    GoogleLoginManager googleLoginManager = new GoogleLoginManager(ProfileActivity.this);
                    googleLoginManager.onClickLogOut();
                    googleLoginManager.onSignOut = () -> logoutApi();


                }
            }

            @Override
            public void onDismiss() {
                binding.blurView.setVisibility(View.GONE);

            }
        });


    }

    private void showDeletePopUp() {

        binding.blurView.setVisibility(View.VISIBLE);

        new CustomDialogBuilder(this).showDeleteDialog(new CustomDialogBuilder.OnDismissListener() {
            @Override
            public void onPositiveDismiss() {

                if (sessionManager.getUser().getLoginType() == 4) {
                    final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) {
                        user.delete();
                    }
                    deleteApi();


                }


                if (sessionManager.getUser().getLoginType() == 1) {

                    GoogleLoginManager googleLoginManager = new GoogleLoginManager(ProfileActivity.this);
                    googleLoginManager.onClickLogOut();

                    googleLoginManager.onSignOut = new GoogleLoginManager.OnSignOut() {
                        @Override
                        public void onSignOutSuccess() {
                            deleteApi();
                        }
                    };


                }
            }

            @Override
            public void onDismiss() {
                binding.blurView.setVisibility(View.GONE);

            }
        });


    }


    private void logoutApi() {


        disposable.add(RetrofitClient.getService().logOutUser(sessionManager.getUser().getId()).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .doOnSubscribe(disposab -> {


                    binding.centerLoader.setVisibility(View.VISIBLE);

                })
                .doOnTerminate(() -> binding.centerLoader.setVisibility(View.GONE)).doOnError(throwable -> Log.i("TAG", "getHomePageData: " + throwable.getMessage()))
                .subscribe((userLogout, throwable) -> {


                    if (userLogout != null && userLogout.getStatus()) {
                        Purchases.getSharedInstance().logOut();
                        Toast.makeText(ProfileActivity.this, userLogout.getMessage(), Toast.LENGTH_SHORT).show();
                        sessionManager.clear();
                        saveToken();

                        startActivity(new Intent(ProfileActivity.this, SplashActivity.class));
                        finishAffinity();


                    }


                }));

    }

    private void saveToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<String> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    Log.i("TAG", "onComplete: from profile token : " + task.getResult());
                    sessionManager.saveFireBaseToken(task.getResult());
                }
            }
        });
    }

    private void deleteApi() {


        disposable.add(RetrofitClient.getService().deleteAccount(sessionManager.getUser().getId()).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .doOnSubscribe(disposab -> {

                    binding.centerLoader.setVisibility(View.VISIBLE);

                })
                .doOnError(throwable -> {
                    Toast.makeText(ProfileActivity.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();

                })
                .doOnTerminate(() -> binding.centerLoader.setVisibility(View.GONE)).doOnError(throwable -> Log.i("TAG", "getHomePageData: " + throwable.getMessage()))
                .subscribe((response, throwable) -> {


                    if (response != null && response.getStatus()) {
                        Purchases.getSharedInstance().logOut();
                        Toast.makeText(ProfileActivity.this, response.getMessage(), Toast.LENGTH_SHORT).show();
                        sessionManager.clear();
                        saveToken();
                        startActivity(new Intent(ProfileActivity.this, SplashActivity.class));
                        finishAffinity();


                    } else {
                        Toast.makeText(ProfileActivity.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();

                    }


                }));

    }

    private void setUserDetail() {

        boolean notification = sessionManager.getBooleanValue(Const.DataKey.NOTIFICATION);
        binding.switchNoti.setChecked(notification);

        // Show the current profile's avatar
        if (sessionManager.getUser().getLastActiveProfile() != null) {
            UserRegistration.Profile profile = sessionManager.getUser().getLastActiveProfile();
            String avatarUrl = profile.getAvatarUrl();
            String avatarType = profile.getAvatarType();
            
            // Check if profile has a custom image (avatar_type is "custom" and has URL)
            if ("custom".equals(avatarType) && avatarUrl != null && !avatarUrl.isEmpty() && !avatarUrl.equals("null")) {
                // Show custom profile image
                binding.cardImageHolder.setVisibility(View.VISIBLE);
                binding.viewColorAvatar.setVisibility(View.GONE);
                binding.imgUser.setVisibility(View.GONE);
                
                // Add timestamp to force cache refresh
                String imageUrl = avatarUrl;
                if (!imageUrl.startsWith("http")) {
                    imageUrl = Const.IMAGE_URL + imageUrl;
                }
                if (!imageUrl.contains("?")) {
                    imageUrl = imageUrl + "?t=" + System.currentTimeMillis();
                }
                
                Glide.with(this)
                        .load(imageUrl)
                        .apply(new RequestOptions()
                                .error(R.color.edit_text_bg_color)
                                .priority(Priority.HIGH)
                                .skipMemoryCache(true)  // Skip memory cache
                                .signature(new com.bumptech.glide.signature.ObjectKey(System.currentTimeMillis())))  // Force refresh
                        .centerCrop()
                        .into(binding.imgProfile);
            } else {
                // Show color avatar with initials
                binding.cardImageHolder.setVisibility(View.GONE);
                binding.viewColorAvatar.setVisibility(View.VISIBLE);
                binding.imgUser.setVisibility(View.GONE);
                
                // Set avatar background color
                String avatarColor = profile.getAvatarColor();
                if (avatarColor == null || avatarColor.isEmpty() || avatarColor.equals("null")) {
                    // Generate a color based on the name
                    String[] colors = {"#FF5252", "#FF9800", "#4CAF50", "#2196F3", "#9C27B0", "#00BCD4"};
                    int colorIndex = Math.abs(profile.getName().hashCode()) % colors.length;
                    avatarColor = colors[colorIndex];
                }
                
                try {
                    binding.viewColorAvatar.setCardBackgroundColor(android.graphics.Color.parseColor(avatarColor));
                } catch (Exception e) {
                    binding.viewColorAvatar.setCardBackgroundColor(android.graphics.Color.parseColor("#FF5252"));
                }
                
                // Generate initials - first letter of up to 2 words
                String name = profile.getName();
                String initials = generateInitials(name);
                binding.tvInitial.setText(initials);
            }
        } else if (!sessionManager.getUser().getProfileImage().isEmpty()) {
            // Fallback to user's profile image
            binding.cardImageHolder.setVisibility(View.VISIBLE);
            binding.viewColorAvatar.setVisibility(View.GONE);
            binding.imgUser.setVisibility(View.GONE);
            
            String imageUrl = Const.IMAGE_URL + sessionManager.getUser().getProfileImage() + "?t=" + System.currentTimeMillis();
            Glide.with(this)
                    .load(imageUrl)
                    .apply(new RequestOptions()
                            .error(R.color.edit_text_bg_color)
                            .priority(Priority.HIGH)
                            .skipMemoryCache(true)
                            .signature(new com.bumptech.glide.signature.ObjectKey(System.currentTimeMillis())))
                    .centerCrop()
                    .into(binding.imgProfile);
        } else {
            // No avatar at all, show default user icon
            binding.cardImageHolder.setVisibility(View.GONE);
            binding.viewColorAvatar.setVisibility(View.GONE);
            binding.imgUser.setVisibility(View.VISIBLE);
        }

        // Show the current profile name, not the user's fullname
        if (sessionManager.getUser().getLastActiveProfile() != null) {
            binding.tvFullAme.setText(sessionManager.getUser().getLastActiveProfile().getName());
        } else {
            binding.tvFullAme.setText(sessionManager.getUser().getFullname());
        }
        
        // Hide Connect TV if user is not logged in
        if (sessionManager.getUser() == null || sessionManager.getUser().getId() == 0) {
            binding.loutTvConnect.setVisibility(View.GONE);
        } else {
            binding.loutTvConnect.setVisibility(View.VISIBLE);
        }
        
        setPremiumlayout();


    }


    private void setPremiumlayout() {
        boolean isPremium = sessionManager.getBooleanValue(Const.DataKey.IS_PREMIUM);

        if (isPremium) {
            Log.i("TAG", "setUserDetail: is premium ");
            binding.tvForPro.setText(getString(R.string.you_are));
            binding.loutPro.setEnabled(false);
//                binding.tvDate.setText(finalDate);
//                binding.loutDate.setVisibility(View.VISIBLE);
        } else {
            Log.i("TAG", "setUserDetail: is not premium ");

            binding.tvForPro.setText(getString(R.string.subscribe_to));
            binding.loutPro.setEnabled(true);
//                binding.loutDate.setVisibility(View.GONE);

        }
    }
    
    private String generateInitials(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "P";
        }
        
        String trimmedName = name.trim();
        String[] words = trimmedName.split("\\s+");
        
        if (words.length == 0) {
            return "P";
        } else if (words.length == 1) {
            // Single word - take first letter
            return words[0].substring(0, 1).toUpperCase();
        } else {
            // Multiple words - take first letter of first two words
            String firstInitial = words[0].substring(0, 1).toUpperCase();
            String secondInitial = words[1].substring(0, 1).toUpperCase();
            return firstInitial + secondInitial;
        }
    }

}