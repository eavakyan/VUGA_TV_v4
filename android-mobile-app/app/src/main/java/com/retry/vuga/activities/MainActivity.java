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
        
        // Top bar Download button
        if (binding.btnDownloadMain != null) {
            binding.btnDownloadMain.setOnClickListener(v -> {
                startActivity(new Intent(this, DownloadsActivity.class));
            });
        }
        
        // Top bar Search button - removed from layout
        // if (binding.btnSearchMain != null) {
        //     binding.btnSearchMain.setOnClickListener(v -> {
        //         startActivity(new Intent(this, SearchLiveTvActivity.class));
        //     });
        // }
        
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


        // Bottom navigation - Search
        if (binding.btnSearchNav != null) {
            binding.btnSearchNav.setOnClickListener(v -> {
                startActivity(new Intent(this, SearchLiveTvActivity.class));
            });
        }

        // Bottom navigation - Subscriptions
        if (binding.btnSubscriptions != null) {
            binding.btnSubscriptions.setOnClickListener(v -> {
                // Navigate to ProActivity which shows subscription options
                startActivity(new Intent(this, ProActivity.class));
            });
        }

        // Bottom navigation - Watch List
        if (binding.btnWatch != null) {
            binding.btnWatch.setOnClickListener(v -> {
                // Navigate to dedicated WatchList page
                startActivity(new Intent(this, WatchListActivity.class));
            });
        }

        // Bottom navigation - Profile
        if (binding.btnProfile != null) {
            binding.btnProfile.setOnClickListener(v -> {
                startActivity(new Intent(this, ProfileActivity.class));
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
                // Navigate to dedicated WatchList page
                startActivity(new Intent(this, WatchListActivity.class));
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
        
        // Update bottom navigation profile avatar
        updateBottomNavProfileAvatar();

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
        
        // Update bottom navigation profile avatar
        updateBottomNavProfileAvatar();
    }

    private void updateProfileAvatar() {
        // Profile avatar removed from top bar - these views no longer exist
        // Keeping method to avoid breaking other calls
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
    
    private void updateBottomNavProfileAvatar() {
        if (binding.navProfileInitial != null) {
            UserRegistration.Data userData = sessionManager.getUser();
            if (userData != null && userData.getLastActiveProfile() != null) {
                String profileName = userData.getLastActiveProfile().getName();
                if (profileName != null && !profileName.isEmpty()) {
                    String firstLetter = profileName.substring(0, 1).toUpperCase();
                    binding.navProfileInitial.setText(firstLetter);
                } else {
                    binding.navProfileInitial.setText("U");
                }
            } else {
                binding.navProfileInitial.setText("U");
            }
        }
    }

    @Override
    protected void onDestroy() {
        Log.i("TAG", "onDestroy: mainActivity ");

        super.onDestroy();
    }
}