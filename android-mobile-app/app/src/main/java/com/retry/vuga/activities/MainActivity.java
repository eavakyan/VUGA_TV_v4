package com.retry.vuga.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableInt;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.google.gson.Gson;
import com.retry.vuga.R;
import com.retry.vuga.adapters.ViewPagerAdapter;
import com.retry.vuga.databinding.ActivityMainBinding;
import com.retry.vuga.model.Downloads;
import com.retry.vuga.model.UserRegistration;
import com.retry.vuga.utils.BindingAdapters;
import com.retry.vuga.utils.Const;
import com.retry.vuga.utils.DeviceUtils;
import com.retry.vuga.utils.SessionManager;
import com.retry.vuga.utils.ViewModelFactory;
import com.retry.vuga.viewmodel.MainViewModel;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.CompositeDisposable;

public class MainActivity extends BaseActivity {
    ActivityMainBinding binding;
    SessionManager sessionManager;

    CompositeDisposable disposable;
    ObservableInt currentPosition = new ObservableInt(0);


    MainViewModel viewModel;

    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {

                Log.i("TAG", ": " + result);
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        viewModel = new ViewModelProvider(this, new ViewModelFactory(new MainViewModel()).createFor()).get(MainViewModel.class);
        initialization();
        setListeners();
        checkNotificationPermission();
        binding.setCurrentPosition(currentPosition);
        checkDownloads();


    }

    private void checkDownloads() {

        List<Downloads> list = sessionManager.getPendings();
        List<Downloads> listNew = new ArrayList<>();
        Log.i("TAG", " onnnn checkDownloads: ");
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getDownloadStatus() == Const.DownloadStatus.START
                    || list.get(i).getDownloadStatus() == Const.DownloadStatus.PROGRESSING) {
                listNew.add(list.get(i));
            }
        }
        for (int i = 0; i < listNew.size(); i++) {
            sessionManager.changePendingStatus(listNew.get(i), Const.DownloadStatus.PAUSED, listNew.get(i).getProgress());

        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (downloadService != null && downloadService.getMyDownloader() != null && !sessionManager.getBooleanValue(Const.DataKey.IS_DOWNLOAD_PAUSED)) {
                    downloadService.getMyDownloader().checkForPending();
                }
            }
        }, 2000);

    }

    private void checkNotificationPermission() {


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);


            }
        }

    }


    private void setListeners() {
        // Add null checks for all binding references to prevent crashes
        if (binding.progress != null) {
            binding.progress.setOnClickListener(v -> {
                // Handle progress click if needed
            });
        }

        if (binding.btnSearch != null) {
            binding.btnSearch.setOnClickListener(v -> {
                startActivity(new Intent(this, SearchLiveTvActivity.class));
            });
        }
        
        // Handle tablet-specific navigation
        if (DeviceUtils.isTabletLandscape(this)) {
            setupTabletNavigation();
        }
        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (currentPosition.get() != 0) {
                    if (viewModel.hideTopBar.getValue() != null && viewModel.hideTopBar.getValue()) {
                        viewModel.hideBottomSheet.setValue(true);
                        return;
                    }
                    if (binding.viewPager != null) {
                        binding.viewPager.setCurrentItem(0, false);
                    }
                    currentPosition.set(0);

                } else {
                    finish();
                }
            }
        });

        viewModel.blurScreen.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (binding.topBar != null) {
                    if (aBoolean) {
                        Log.i("TAG", "onChanged: true ");
//                        setBlur(binding.blurView, binding.rootLout, 20f);
                        binding.topBar.setVisibility(View.VISIBLE);
                    } else {
//                        removeBlur(binding.blurView, binding.rootLout);
                        binding.topBar.setVisibility(View.GONE);
                    }
                }
            }
        });
        viewModel.hideTopBar.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) {
                    Log.i("TAG", "onChanged: true ");
//                    binding.blurView.setVisibility(View.GONE);
                    if (binding.bottomBar != null) {
                        binding.bottomBar.setVisibility(View.GONE);
                    }
                    if (binding.topBar != null) {
                        binding.topBar.setVisibility(View.GONE);
                    }
                } else {
//                    binding.blurView.setVisibility(View.VISIBLE);
                    if (binding.topBar != null) {
                        binding.topBar.setVisibility(View.VISIBLE);
                    }
                    if (binding.bottomBar != null) {
                        binding.bottomBar.setVisibility(View.VISIBLE);
                    }
                }
            }
        });


        if (sessionManager.getBranchData() != null) {

            String data = sessionManager.getBranchData();
            JSONObject linkProperties = new Gson().fromJson(data, JSONObject.class);
            if (linkProperties != null) {

                furtherProcess(linkProperties);


            }


        }

        if (binding.viewPager != null) {
            binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                    super.onPageScrolled(position, positionOffset, positionOffsetPixels);
                    currentPosition.set(position);
                    if (position == 0) {
                        if (viewModel.blurScreen.getValue()) {
                            Log.i("TAG", "onChanged: true ");
//                            setBlur(binding.blurView, binding.rootLout, 20f);
                            if (binding.topBar != null) {
                                binding.topBar.setVisibility(View.VISIBLE);
                            }
                        } else {
//                            removeBlur(binding.blurView, binding.rootLout);
                            if (binding.topBar != null) {
                                binding.topBar.setVisibility(View.GONE);
                            }
                        }
                    } else {
                        if (binding.topBar != null) {
                            binding.topBar.setVisibility(View.VISIBLE);
                        }
                    }
                }
            });
        }


        if (binding.imgProfile != null) {
            binding.imgProfile.setOnClickListener(v -> {
                startActivity(new Intent(this, ProfileActivity.class));
            });
        }


        if (binding.btnHome != null) {
            binding.btnHome.setOnClickListener(v -> {
                if (binding.viewPager != null) {
                    binding.viewPager.setCurrentItem(0, false);
                }
            });
        }


        if (binding.btnDiscover != null) {
            binding.btnDiscover.setOnClickListener(v -> {
                if (binding.viewPager != null) {
                    binding.viewPager.setCurrentItem(1, false);
                }
            });
        }

        if (binding.btnTv != null) {
            binding.btnTv.setOnClickListener(v -> {
                if (binding.viewPager != null) {
                    binding.viewPager.setCurrentItem(2, false);
                }
            });
        }


        if (binding.btnWatch != null) {
            binding.btnWatch.setOnClickListener(v -> {
                if (binding.viewPager != null) {
                    binding.viewPager.setCurrentItem(3, false);
                }
            });
        }

    }
    
    /**
     * Setup tablet-specific navigation for landscape mode
     */
    private void setupTabletNavigation() {
        // Hide mobile bottom bar on tablets
        if (binding.bottomBar != null) {
            binding.bottomBar.setVisibility(View.GONE);
        }
        
        // Show tablet navigation
        if (binding.tabletNavigation != null) {
            binding.tabletNavigation.setVisibility(View.VISIBLE);
        }
        
        // Setup tablet navigation click listeners
        if (binding.btnHome != null) {
            binding.btnHome.setOnClickListener(v -> {
                binding.viewPager.setCurrentItem(0, false);
                updateTabletNavigationSelection(0);
            });
        }
        
        if (binding.btnDiscover != null) {
            binding.btnDiscover.setOnClickListener(v -> {
                binding.viewPager.setCurrentItem(1, false);
                updateTabletNavigationSelection(1);
            });
        }
        
        if (binding.btnTv != null) {
            binding.btnTv.setOnClickListener(v -> {
                binding.viewPager.setCurrentItem(2, false);
                updateTabletNavigationSelection(2);
            });
        }
        
        if (binding.btnWatch != null) {
            binding.btnWatch.setOnClickListener(v -> {
                binding.viewPager.setCurrentItem(3, false);
                updateTabletNavigationSelection(3);
            });
        }
    }
    
    /**
     * Update tablet navigation selection state
     * @param position The selected position
     */
    private void updateTabletNavigationSelection(int position) {
        currentPosition.set(position);
        // Force refresh of the binding
        binding.invalidateAll();
    }


    private void furtherProcess(JSONObject linkProperties) {
        try {

            startActivity(new Intent(this, MovieDetailActivity.class)
                    .putExtra(Const.DataKey.CONTENT_ID, Integer.parseInt(linkProperties.getString(Const.DataKey.CONTENT_ID)))
                    .putExtra(Const.DataKey.IS_BRANCH_LINK, true));


            sessionManager.removeBranchData();
            finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void openWatchList() {
        if (binding.viewPager != null) {
            binding.viewPager.setCurrentItem(3);
        }
    }


    private void initialization() {

        sessionManager = new SessionManager(this);

        disposable = new CompositeDisposable();

        // Force top bar to be visible
        if (binding.topBar != null) {
            binding.topBar.setVisibility(View.VISIBLE);
        }

        // Update profile name
        updateProfileNameInTopBar();

        if (binding.btnTv != null) {
            if (sessionManager.getAppSettings().getSettings().getLiveTvEnable() == 0) {
                binding.btnTv.setVisibility(View.GONE);
                // Also hide tablet TV button if it exists
                if (DeviceUtils.isTabletLandscape(this)) {
                    binding.btnTv.setVisibility(View.GONE);
                }
            } else {
                binding.btnTv.setVisibility(View.VISIBLE);
                // Also show tablet TV button if it exists
                if (DeviceUtils.isTabletLandscape(this)) {
                    binding.btnTv.setVisibility(View.VISIBLE);
                }
            }
        }

        if (binding.viewPager != null) {
            binding.viewPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager(), getLifecycle(), this));
            binding.viewPager.setUserInputEnabled(false);
        }

        if (binding.imgPic != null && binding.imgUser != null) {
            updateProfileAvatar();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh profile avatar in case it changed
        if (binding != null && binding.imgPic != null && binding.imgUser != null) {
            updateProfileAvatar();
        }
        // Update profile name in top bar
        updateProfileNameInTopBar();
    }

    private void updateProfileAvatar() {
        UserRegistration.Data userData = sessionManager.getUser();
        if (userData != null && userData.getLastActiveProfile() != null) {
            UserRegistration.Profile currentProfile = userData.getLastActiveProfile();
            
            if ("default".equals(currentProfile.getAvatarType()) || "color".equals(currentProfile.getAvatarType())) {
                // Show color avatar
                binding.imgPic.setVisibility(View.GONE);
                binding.imgUser.setVisibility(View.GONE);
                binding.viewColorAvatar.setVisibility(View.VISIBLE);
                binding.tvProfileInitial.setVisibility(View.VISIBLE);
                
                try {
                    binding.viewColorAvatar.setCardBackgroundColor(Color.parseColor(currentProfile.getAvatarColor()));
                } catch (Exception e) {
                    binding.viewColorAvatar.setCardBackgroundColor(Color.parseColor("#FF5252"));
                }
                
                // Set initial
                String name = currentProfile.getName();
                if (name != null && !name.isEmpty()) {
                    binding.tvProfileInitial.setText(name.substring(0, 1).toUpperCase());
                }
            } else if ("custom".equals(currentProfile.getAvatarType()) && currentProfile.getAvatarUrl() != null && !currentProfile.getAvatarUrl().isEmpty()) {
                // Show custom uploaded image avatar
                binding.imgPic.setVisibility(View.VISIBLE);
                binding.imgUser.setVisibility(View.GONE);
                binding.viewColorAvatar.setVisibility(View.GONE);
                binding.tvProfileInitial.setVisibility(View.GONE);
                
                String avatarUrl = currentProfile.getAvatarUrl();
                if (!avatarUrl.startsWith("http")) {
                    avatarUrl = Const.IMAGE_URL + avatarUrl;
                }
                BindingAdapters.loadImage(binding.imgPic, avatarUrl);
            } else {
                // Show default user icon
                binding.imgPic.setVisibility(View.GONE);
                binding.imgUser.setVisibility(View.VISIBLE);
                binding.viewColorAvatar.setVisibility(View.GONE);
                binding.tvProfileInitial.setVisibility(View.GONE);
            }
        } else {
            // No profile, show default user icon
            binding.imgPic.setVisibility(View.GONE);
            binding.imgUser.setVisibility(View.VISIBLE);
            binding.viewColorAvatar.setVisibility(View.GONE);
            binding.tvProfileInitial.setVisibility(View.GONE);
        }
    }

    private void updateProfileNameInTopBar() {
        if (binding.tvProfileNameMain != null) {
            UserRegistration.Data userData = sessionManager.getUser();
            if (userData != null && userData.getLastActiveProfile() != null) {
                String profileName = userData.getLastActiveProfile().getName();
                if (profileName != null && !profileName.isEmpty()) {
                    binding.tvProfileNameMain.setText(profileName);
                } else {
                    binding.tvProfileNameMain.setText("User");
                }
            } else {
                binding.tvProfileNameMain.setText("User");
            }
        }
    }

    @Override
    protected void onDestroy() {
        Log.i("TAG", "onDestroy: mainActivity ");

        super.onDestroy();
    }
}