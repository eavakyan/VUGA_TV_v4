package com.retry.vuga.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.gson.Gson;
import com.retry.vuga.R;
import com.retry.vuga.adapters.LiveTvCategoryAdapter;
import com.retry.vuga.adapters.LiveTvChannelGridAdapter;
import com.retry.vuga.databinding.ActivityLiveTvBinding;
import com.retry.vuga.model.LiveTvCategory;
import com.retry.vuga.model.LiveTvChannel;
import com.retry.vuga.model.UserRegistration;
import com.retry.vuga.utils.Const;
import com.retry.vuga.utils.CustomDialogBuilder;
import com.retry.vuga.utils.DeviceUtils;
import com.retry.vuga.utils.adds.MyRewardAds;
import com.retry.vuga.viewmodel.LiveTvViewModel;
import com.retry.vuga.utils.SessionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Enhanced Live TV Activity with grid layout, search, and category filtering
 */
public class LiveTvActivity extends BaseActivity {

    private ActivityLiveTvBinding binding;
    private LiveTvViewModel viewModel;
    
    private LiveTvChannelGridAdapter channelAdapter;
    private LiveTvCategoryAdapter categoryAdapter;
    private MyRewardAds myRewardAds;
    
    private List<LiveTvChannel> allChannels = new ArrayList<>();
    private List<LiveTvChannel> filteredChannels = new ArrayList<>();
    private List<LiveTvCategory> categories = new ArrayList<>();
    
    private int selectedCategoryId = 0; // 0 = All
    private String currentSearchQuery = "";
    private boolean rewardEarned = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_live_tv);
        
        initializeComponents();
        setupRecyclerViews();
        setupListeners();
        observeViewModel();
        
        loadLiveTvData();
    }

    private void initializeComponents() {
        viewModel = new ViewModelProvider(this).get(LiveTvViewModel.class);
        myRewardAds = new MyRewardAds(this);
        
        // Setup blur effect for premium/ad dialogs
        setBlur(binding.blurView, binding.rootLayout, 10f);
        
        // Set up toolbar - cast to Toolbar
        androidx.appcompat.widget.Toolbar toolbar = (androidx.appcompat.widget.Toolbar) binding.toolbar;
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Live TV");
        }
        
        toolbar.setNavigationOnClickListener(v -> navigateToHome());
    }

    private void setupRecyclerViews() {
        // Setup category chips RecyclerView
        categoryAdapter = new LiveTvCategoryAdapter(this::onCategorySelected);
        binding.rvCategories.setLayoutManager(
            new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        binding.rvCategories.setAdapter(categoryAdapter);
        
        // Setup channels grid RecyclerView
        channelAdapter = new LiveTvChannelGridAdapter(this::onChannelClicked);
        
        // Dynamic grid layout based on device type
        int spanCount = DeviceUtils.isTablet(this) ? 4 : 2;
        if (getResources().getConfiguration().orientation == 
                android.content.res.Configuration.ORIENTATION_LANDSCAPE) {
            spanCount = DeviceUtils.isTablet(this) ? 6 : 3;
        }
        
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, spanCount);
        binding.rvChannels.setLayoutManager(gridLayoutManager);
        binding.rvChannels.setAdapter(channelAdapter);
        binding.rvChannels.setNestedScrollingEnabled(false);
        
        // Note: Pagination scroll listener removed as RecyclerView is inside NestedScrollView
        // All channels will be displayed at once
    }

    private void setupListeners() {
        // Get the EditText from the layout
        android.widget.EditText etSearch = (android.widget.EditText) binding.etSearch;
        
        // Search functionality
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString().trim();
                filterChannels();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Search icon click
        binding.ivSearchIcon.setOnClickListener(v -> {
            if (binding.searchInputLayout.getVisibility() == View.VISIBLE) {
                binding.searchInputLayout.setVisibility(View.GONE);
                etSearch.setText("");
            } else {
                binding.searchInputLayout.setVisibility(View.VISIBLE);
                etSearch.requestFocus();
            }
        });

        // Pull to refresh
        binding.swipeRefresh.setOnRefreshListener(() -> {
            viewModel.refreshChannels();
        });

        // Clear search button
        binding.ivClearSearch.setOnClickListener(v -> {
            etSearch.setText("");
        });
        
        // Blur view click (dismiss dialogs)
        binding.blurView.setOnClickListener(v -> {
            binding.blurView.setVisibility(View.GONE);
        });
    }

    private void observeViewModel() {
        // Observe channels
        viewModel.getChannels().observe(this, channels -> {
            if (channels != null) {
                this.allChannels = channels;
                filterChannels();
            }
        });

        // Observe categories
        viewModel.getCategories().observe(this, categories -> {
            if (categories != null) {
                this.categories = categories;
                
                // Add "All" category at the beginning
                List<LiveTvCategory> allCategories = new ArrayList<>();
                LiveTvCategory allCategory = LiveTvCategory.PredefinedCategories.ALL;
                allCategory.setSelected(selectedCategoryId == 0);
                allCategories.add(allCategory);
                allCategories.addAll(categories);
                
                categoryAdapter.updateCategories(allCategories);
            }
        });

        // Observe loading state
        viewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading != null) {
                binding.swipeRefresh.setRefreshing(isLoading);
                binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
        });

        // Observe error messages
        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                showError(error);
            }
        });

        // Observe empty state
        viewModel.isEmpty().observe(this, isEmpty -> {
            if (isEmpty != null) {
                binding.tvNoChannels.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
                binding.rvChannels.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
            }
        });
    }

    private void loadLiveTvData() {
        viewModel.loadChannelsAndCategories();
    }

    private void onCategorySelected(LiveTvCategory category) {
        selectedCategoryId = category.getId();
        
        // Update category selection
        for (LiveTvCategory cat : categories) {
            cat.setSelected(cat.getId() == selectedCategoryId);
        }
        categoryAdapter.notifyDataSetChanged();
        
        // Filter channels by category
        filterChannels();
    }

    private void filterChannels() {
        filteredChannels.clear();
        
        for (LiveTvChannel channel : allChannels) {
            boolean matchesCategory = (selectedCategoryId == 0) || 
                                    channel.getCategoryIds().contains(String.valueOf(selectedCategoryId));
            
            boolean matchesSearch = currentSearchQuery.isEmpty() || 
                                  channel.getTitle().toLowerCase().contains(currentSearchQuery.toLowerCase()) ||
                                  channel.getCurrentProgramTitle().toLowerCase().contains(currentSearchQuery.toLowerCase());
            
            if (matchesCategory && matchesSearch) {
                filteredChannels.add(channel);
            }
        }
        
        channelAdapter.updateChannels(filteredChannels);
        
        // Update search clear button visibility
        binding.ivClearSearch.setVisibility(
            currentSearchQuery.isEmpty() ? View.GONE : View.VISIBLE
        );
        
        // Update empty state
        boolean isEmpty = filteredChannels.isEmpty() && !viewModel.getIsLoading().getValue();
        binding.tvNoChannels.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        binding.rvChannels.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void onChannelClicked(LiveTvChannel channel) {
        // Handle different access types
        if (channel.isFree()) {
            playChannel(channel);
        } else if (channel.isPremium()) {
            showPremiumDialog();
        } else if (channel.requiresAds()) {
            showAdDialog(channel);
        }
    }

    private void playChannel(LiveTvChannel channel) {
        // Track view count
        trackChannelView(channel.getId());
        
        // Launch player activity
        Intent intent = new Intent(this, PlayerNewActivity.class);
        intent.putExtra(Const.DataKey.LIVE_TV_MODEL, new Gson().toJson(channel));
        startActivity(intent);
    }
    
    private void trackChannelView(int channelId) {
        // Track channel view via API
        HashMap<String, Object> params = new HashMap<>();
        params.put("channel_id", channelId);
        SessionManager sessionManager = new SessionManager(this);
        UserRegistration.Data user = sessionManager.getUser();
        int userId = user != null ? user.getId() : 0;
        params.put("userId", userId);
        
        // Fire and forget API call
        viewModel.trackChannelView(params);
    }

    private void showPremiumDialog() {
        binding.blurView.setVisibility(View.VISIBLE);
        
        new CustomDialogBuilder(this).showPremiumDialog(new CustomDialogBuilder.OnDismissListener() {
            @Override
            public void onPositiveDismiss() {
                startActivity(new Intent(LiveTvActivity.this, ProActivity.class));
            }

            @Override
            public void onDismiss() {
                binding.blurView.setVisibility(View.GONE);
            }
        });
    }

    private void showAdDialog(LiveTvChannel channel) {
        binding.blurView.setVisibility(View.VISIBLE);
        
        new CustomDialogBuilder(this).showUnlockDialog(new CustomDialogBuilder.OnDismissListener() {
            @Override
            public void onPositiveDismiss() {
                loadRewardedAd(channel);
            }

            @Override
            public void onDismiss() {
                binding.blurView.setVisibility(View.GONE);
            }
        });
    }

    private void loadRewardedAd(LiveTvChannel channel) {
        myRewardAds.showAd();
        
        myRewardAds.setRewardAdListnear(new MyRewardAds.RewardAdListnear() {
            @Override
            public void onAdClosed() {
                if (rewardEarned) {
                    playChannel(channel);
                    rewardEarned = false;
                }
                myRewardAds = new MyRewardAds(LiveTvActivity.this);
            }

            @Override
            public void onEarned() {
                rewardEarned = true;
            }
        });
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    
    private void navigateToHome() {
        // Simply finish this activity to return to MainActivity
        // MainActivity should already be in the back stack
        finish();
        // Use a slide animation for better UX
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Handle toolbar back button
            navigateToHome();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public void onBackPressed() {
        android.widget.EditText etSearch = (android.widget.EditText) binding.etSearch;
        if (binding.searchInputLayout.getVisibility() == View.VISIBLE && !currentSearchQuery.isEmpty()) {
            etSearch.setText("");
        } else if (binding.searchInputLayout.getVisibility() == View.VISIBLE) {
            binding.searchInputLayout.setVisibility(View.GONE);
        } else {
            // Navigate back to Home
            navigateToHome();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // ViewModel cleanup is handled automatically by the framework
    }
}