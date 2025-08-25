package com.retry.vuga.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import android.widget.PopupWindow;
import android.view.ViewGroup;
import android.content.Intent;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.android.flexbox.AlignItems;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.retry.vuga.R;
import com.retry.vuga.activities.BaseActivity;
import com.retry.vuga.activities.MainActivity;
import com.retry.vuga.activities.DownloadsActivity;
import com.retry.vuga.activities.SearchLiveTvActivity;
import com.retry.vuga.activities.ContentByGenreActivity;
import com.retry.vuga.activities.ContentByDistributorActivity;
import com.retry.vuga.adapters.CategoryDropdownAdapter;
import com.retry.vuga.adapters.DropdownMenuAdapter;
import com.retry.vuga.adapters.ContentDetailGenreAdapter;
import com.retry.vuga.adapters.HomeCatNameAdapter;
import com.retry.vuga.adapters.HomeFeaturedAdapter;
import com.retry.vuga.adapters.HomeTopItemsAdapter;
import com.retry.vuga.adapters.HomeWatchlistAdapter;
import com.retry.vuga.adapters.NewReleasesAdapter;
import com.retry.vuga.adapters.MovieHistoryAdapter;
import com.retry.vuga.adapters.HorizontalCategoryAdapter;
import com.retry.vuga.databinding.FragmentHomeBinding;
import com.retry.vuga.model.ContentDetail;
import com.retry.vuga.model.UserRegistration;
import com.retry.vuga.model.HomePage;
import com.retry.vuga.model.RecentlyWatchedContent;
import com.retry.vuga.model.RestResponse;
import com.retry.vuga.retrofit.RetrofitClient;
import com.retry.vuga.utils.Const;
import com.retry.vuga.utils.Global;
import com.retry.vuga.utils.ViewModelFactory;
import com.retry.vuga.viewmodel.HomeViewModel;
import com.retry.vuga.viewmodel.MainViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;


public class HomeFragment extends BaseFragment {
    FragmentHomeBinding binding;

    HomeFeaturedAdapter homeFeaturedAdapter;
    HomeWatchlistAdapter homeRecentlyWatchedAdapter;
    HomeTopItemsAdapter homeTopItemsAdapter;
    HomeCatNameAdapter homeCatNameAdapter;
    NewReleasesAdapter newReleasesAdapter;

    CompositeDisposable disposable;

    List<ContentDetail.DataItem> featuredList = new ArrayList<>();
    List<ContentDetail.DataItem> recentlyWatchedList = new ArrayList<>();
    List<HomePage.TopContentItem> topList = new ArrayList<>();
    List<HomePage.GenreContents> catList = new ArrayList<>();
    List<ContentDetail.DataItem> newReleasesList = new ArrayList<>();

    MainViewModel mainViewModel;
    HomeViewModel viewModel;
    ContentDetailGenreAdapter genreAdapter;
    
    // New UI components
    private HorizontalCategoryAdapter horizontalCategoryAdapter;
    private CategoryDropdownAdapter categoryDropdownAdapter;
    private DropdownMenuAdapter dropdownMenuAdapter;
    private PopupWindow categoryPopupWindow;
    private String selectedFilter = "TV Shows"; // Default filter


    private boolean scrolledByUser = false;
    private Handler handler;
    private boolean reversed = false;
    private int scrollingPos = 0;
    
    private BroadcastReceiver watchlistUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("com.retry.vuga.WATCHLIST_UPDATED".equals(intent.getAction())) {
                int contentId = intent.getIntExtra("content_id", 0);
                boolean isAdded = intent.getBooleanExtra("is_added", false);
                Log.d("Watchlist", "HomeFragment received broadcast - content_id: " + contentId + ", is_added: " + isAdded);
                // Refresh the home page data to update watchlist
                getHomePageData();
            }
        }
    };

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false);
        mainViewModel = new ViewModelProvider(requireActivity(), new ViewModelFactory(new MainViewModel()).createFor()).get(MainViewModel.class);
        viewModel = new ViewModelProvider(this, new ViewModelFactory(new HomeViewModel()).createFor()).get(HomeViewModel.class);


        initialization();
        initListeners();
        getHomePageData();


        return binding.getRoot();
    }


    ContentDetail.DataItem scrollingItem = null;

    MovieHistoryAdapter movieHistoryAdapter = new MovieHistoryAdapter();
    boolean isFirstTime = true;


    @Override
    public void onPause() {
        super.onPause();
        // Unregister the broadcast receiver
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(watchlistUpdateReceiver);
        
        // Stop auto-scrolling to prevent crashes during rotation
        if (handler != null) {
            handler.removeCallbacks(runnable);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clean up resources to prevent memory leaks and crashes
        if (handler != null) {
            handler.removeCallbacks(runnable);
            handler = null;
        }
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
        binding = null;
    }

    private void initListeners() {
        
        // Initialize new UI components
        setupHeaderWithLogoAndProfile();
        setupHorizontalCategoryList();
        setupCategoryDropdown();

        // Set up Recently Watched "More" button if it exists
        if (binding.btnRecentlyWatchedMore != null) {
            binding.btnRecentlyWatchedMore.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    // For now, open watchlist - can be changed to a recently watched view later
                    ((MainActivity) getActivity()).openWatchList();
                }
            });
        }

        binding.centerLoader.setOnClickListener(v -> {

        });


        if (binding.rvFeatured != null) {
            binding.rvFeatured.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                }

                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);

                    if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                        scrolledByUser = true;
                    }

                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        if (scrolledByUser && binding.rvFeatured != null && 
                            binding.rvFeatured.getLayoutManager() != null && 
                            homeFeaturedAdapter != null) {
                            handler.removeCallbacks(runnable);

                            scrollingPos = ((LinearLayoutManager) binding.rvFeatured.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
                            reversed = scrollingPos + 1 > homeFeaturedAdapter.getItemCount() - 1;

                            // Disabled auto-scrolling - manual sliding only
                            // scrollToPos(true);
                        }
                        scrolledByUser = false;
                    }
                }
            });
        }
        if (binding.swipeRefresh != null) {
            binding.swipeRefresh.setOnRefreshListener(() -> {
                getHomePageData();
            });
        }

//        binding.appBar.addOnOffsetChangedListener(new AppBarLayout.BaseOnOffsetChangedListener() {
//            @Override
//            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
//
//
//                boolean collapsed = Math.abs(verticalOffset) - appBarLayout.getTotalScrollRange() > -5;
//                boolean currentValue = mainViewModel.blurScreen.getValue();
//                if (collapsed && !currentValue) {
//                    mainViewModel.blurScreen.setValue(collapsed);
//
//                } else if (!collapsed && currentValue) {
//                    mainViewModel.blurScreen.setValue(collapsed);
//
//                }
//            }
//        });
    }

    private void scrollToPos(boolean fromUser) {
        // Check if everything is properly initialized before proceeding
        if (binding == null || binding.rvFeatured == null || 
            homeFeaturedAdapter == null || requireActivity().isDestroyed()) {
            return;
        }
        
        if (!fromUser && scrollingPos < featuredList.size() && scrollingPos >= 0) {
            binding.rvFeatured.smoothScrollToPosition(scrollingPos);
        }
        setContentData();
        // Disabled auto-scrolling - manual sliding only
        // if (handler != null && !requireActivity().isDestroyed()) {
        //     handler.postDelayed(runnable, Const.FEATURED_SCROLL);
        // }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Update profile name when returning to fragment
        updateProfileName();
        
        // Register the broadcast receiver for watchlist updates
        IntentFilter filter = new IntentFilter("com.retry.vuga.WATCHLIST_UPDATED");
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(watchlistUpdateReceiver, filter);
        
        initHistory();
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
//                refreshFavList();
            }
        }, 1000);

        // Disabled auto-scrolling - manual sliding only
        // if (homeFeaturedAdapter != null && 
        //     homeFeaturedAdapter.getItemCount() != 0 && 
        //     handler != null && 
        //     binding != null && 
        //     binding.rvFeatured != null &&
        //     !requireActivity().isDestroyed()) {
        //     handler.postDelayed(runnable, Const.FEATURED_SCROLL);
        // }
    }

    private void initHistory() {
        if (binding.rvHistory != null && binding.tvHistory != null) {
            movieHistoryAdapter.updateData(sessionManager.getMovieHistories());
            binding.rvHistory.setAdapter(movieHistoryAdapter);
            movieHistoryAdapter.setOnUpdateList(isEmpty -> {
                binding.tvHistory.setVisibility(movieHistoryAdapter.getItemCount() == 0 ? View.GONE : View.VISIBLE);
                binding.rvHistory.setVisibility(movieHistoryAdapter.getItemCount() == 0 ? View.GONE : View.VISIBLE);
            });
            binding.tvHistory.setVisibility(movieHistoryAdapter.getItemCount() == 0 ? View.GONE : View.VISIBLE);
            binding.rvHistory.setVisibility(movieHistoryAdapter.getItemCount() == 0 ? View.GONE : View.VISIBLE);
        }
    }

    private void getHomePageData() {

        disposable.add(RetrofitClient.getService().getHomeData(sessionManager.getUser().getId()).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .doOnSubscribe(disposable1 -> {
                    if (isFirstTime) {
                        isFirstTime = false;
                        if (binding.centerLoader != null) {
                            binding.centerLoader.setVisibility(View.VISIBLE);
                        }
                    }
                    if (binding.loutMain != null) {
                        binding.loutMain.setVisibility(View.GONE);
                    }
                })
                .doOnTerminate(() -> {
                    if (binding.swipeRefresh != null) {
                        binding.swipeRefresh.setRefreshing(false);
                    }
                    if (binding.loutMain != null) {
                        binding.loutMain.setVisibility(View.VISIBLE);
                    }
                    if (binding.centerLoader != null) {
                        binding.centerLoader.setVisibility(View.GONE);
                    }

                }).doOnError(throwable -> {
                    if (binding.swipeRefresh != null) {
                        binding.swipeRefresh.setRefreshing(false);
                    }

//                    binding.centerLoader.setVisibility(View.VISIBLE);
                    if (binding.loutMain != null) {
                        binding.loutMain.setVisibility(View.GONE);
                    }
                    Log.i("TAG", "getHomePageData: " + throwable.getMessage());
                })
                .subscribe((homePage, throwable) -> {

                    if (homePage != null && !requireActivity().isDestroyed() && binding != null) {

                        if (homePage.getFeatured() != null && !homePage.getFeatured().isEmpty()) {
                            featuredList = new ArrayList<>();
                            featuredList.addAll(homePage.getFeatured());
                            homeFeaturedAdapter.updateItems(featuredList);
                            setContentData();
                            List<String> dotlist = new ArrayList<>();
                            for (int i = 0; i < homeFeaturedAdapter.getList().size(); i++) {
                                dotlist.add(" ");
                            }
                            // Disabled auto-scrolling - manual sliding only
                            // if (handler != null && !requireActivity().isDestroyed()) {
                            //     handler.removeCallbacks(runnable);
                            //     handler.postDelayed(runnable, Const.FEATURED_SCROLL);
                            // }

                        }
                        
                        // Fetch Recently Watched content
                        fetchRecentlyWatched();

                        if (homePage.getTopContents() != null) {

                            if (!homePage.getTopContents().isEmpty()) {
                                topList = new ArrayList<>();
                                topList.addAll(homePage.getTopContents());
                                homeTopItemsAdapter.updateItems(topList);
                            } else {
                                if (binding.loutTop != null) {
                                    binding.loutTop.setVisibility(View.GONE);
                                }
                            }
                        }

                        if (homePage.getGenreContents() != null && !homePage.getGenreContents().isEmpty()) {
                            catList = new ArrayList<>();
                            catList.addAll(homePage.getGenreContents().stream().filter(genreContents -> !genreContents.getContent().isEmpty()).collect(Collectors.toList()));
                            homeCatNameAdapter.updateItems(catList);
                            // Update horizontal categories
                            horizontalCategoryAdapter.updateGenres(catList);
                            
                            // Extract new releases from genre contents
                            extractNewReleases(homePage.getGenreContents());
                        }

                    }
                    if (binding.swipeRefresh != null) {
                        binding.swipeRefresh.setRefreshing(false);
                    }

                }));
        if (binding.swipeRefresh != null) {
            binding.swipeRefresh.dispatchNestedFling(100, 100, true);
        }

    }
    
    private void fetchRecentlyWatched() {
        if (sessionManager == null || sessionManager.getUser() == null) {
            return;
        }
        
        // First get the watch history to get content IDs
        HashMap<String, Object> params = new HashMap<>();
        params.put("user_id", sessionManager.getUser().getId());
        params.put("limit", 20);
        
        // Add profile ID if available
        if (sessionManager.getUser().getLastActiveProfileId() != null && 
            sessionManager.getUser().getLastActiveProfileId() > 0) {
            params.put("profile_id", sessionManager.getUser().getLastActiveProfileId());
        }
        
        disposable.add(RetrofitClient.getService().getWatchHistory(params)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    if (response != null && response.getStatus()) {
                        try {
                            // Parse the response to get content IDs
                            Gson gson = new Gson();
                            String jsonData = gson.toJson(response);
                            JsonObject jsonObject = gson.fromJson(jsonData, JsonObject.class);
                            
                            if (jsonObject.has("data") && jsonObject.get("data").isJsonArray()) {
                                JsonArray dataArray = jsonObject.getAsJsonArray("data");
                                
                                if (dataArray.size() > 0) {
                                    // Collect content IDs
                                    List<Integer> contentIds = new ArrayList<>();
                                    for (JsonElement element : dataArray) {
                                        if (element.isJsonObject()) {
                                            JsonObject item = element.getAsJsonObject();
                                            if (item.has("content_id")) {
                                                contentIds.add(item.get("content_id").getAsInt());
                                            }
                                        }
                                    }
                                    
                                    if (!contentIds.isEmpty()) {
                                        // Fetch content details by IDs
                                        fetchContentByIds(contentIds);
                                    } else {
                                        hideRecentlyWatched();
                                    }
                                } else {
                                    hideRecentlyWatched();
                                }
                            } else {
                                hideRecentlyWatched();
                            }
                        } catch (Exception e) {
                            Log.e("HomeFragment", "Error parsing watch history: " + e.getMessage());
                            hideRecentlyWatched();
                        }
                    } else {
                        hideRecentlyWatched();
                    }
                }, throwable -> {
                    Log.e("HomeFragment", "Error fetching watch history: " + throwable.getMessage());
                    hideRecentlyWatched();
                }));
    }
    
    private void fetchContentByIds(List<Integer> contentIds) {
        // Convert list to comma-separated string
        StringBuilder idsBuilder = new StringBuilder();
        for (int i = 0; i < contentIds.size(); i++) {
            if (i > 0) idsBuilder.append(",");
            idsBuilder.append(contentIds.get(i));
        }
        
        Integer profileId = (sessionManager.getUser().getLastActiveProfileId() != null && 
                            sessionManager.getUser().getLastActiveProfileId() > 0) ? 
                           sessionManager.getUser().getLastActiveProfileId() : null;
        
        disposable.add(RetrofitClient.getService().getRecentlyWatchedContent(
                idsBuilder.toString(),
                sessionManager.getUser().getId(),
                profileId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    if (response != null && response.getStatus() && response.getData() != null) {
                        List<RecentlyWatchedContent.DataItem> items = response.getData();
                        if (!items.isEmpty()) {
                            if (binding.loutRecentlyWatched != null) {
                                binding.loutRecentlyWatched.setVisibility(View.VISIBLE);
                            }
                            
                            // Convert to ContentDetail.DataItem format for the adapter
                            List<ContentDetail.DataItem> contentItems = new ArrayList<>();
                            for (RecentlyWatchedContent.DataItem item : items) {
                                ContentDetail.DataItem contentItem = new ContentDetail.DataItem();
                                contentItem.setId(item.getContentId());
                                contentItem.setTitle(item.getDisplayTitle());
                                contentItem.setVerticalPoster(item.getVerticalPoster());
                                contentItem.setHorizontalPoster(item.getDisplayPoster());
                                contentItem.setDuration(item.getDisplayDuration());
                                contentItem.setReleaseYear(item.getReleaseYear());
                                contentItem.setType(item.getType());
                                contentItems.add(contentItem);
                            }
                            
                            if (homeRecentlyWatchedAdapter != null) {
                                homeRecentlyWatchedAdapter.updateItems(contentItems);
                            }
                        } else {
                            hideRecentlyWatched();
                        }
                    } else {
                        hideRecentlyWatched();
                    }
                }, throwable -> {
                    Log.e("HomeFragment", "Error fetching content by IDs: " + throwable.getMessage());
                    hideRecentlyWatched();
                }));
    }
    
    private void hideRecentlyWatched() {
        if (binding != null && binding.loutRecentlyWatched != null) {
            binding.loutRecentlyWatched.setVisibility(View.GONE);
        }
    }

    private void initialization() {

        SnapHelper snapHelper = new PagerSnapHelper();
        if (binding.rvFeatured != null) {
            snapHelper.attachToRecyclerView(binding.rvFeatured);
        }
        handler = new Handler(Looper.getMainLooper());

        disposable = new CompositeDisposable();

        homeCatNameAdapter = new HomeCatNameAdapter();
        homeRecentlyWatchedAdapter = new HomeWatchlistAdapter();
        homeTopItemsAdapter = new HomeTopItemsAdapter();
        homeFeaturedAdapter = new HomeFeaturedAdapter();
        genreAdapter = new ContentDetailGenreAdapter();
        horizontalCategoryAdapter = new HorizontalCategoryAdapter();
        newReleasesAdapter = new NewReleasesAdapter();
        
        if (binding.swipeRefresh != null) {
            binding.swipeRefresh.setProgressViewOffset(true, 150, 350);
        }

        // Set adapters only if RecyclerViews exist
        if (binding.rvFeatured != null) {
            binding.rvFeatured.setAdapter(homeFeaturedAdapter);
        }
        if (binding.rvRecentlyWatched != null) {
            binding.rvRecentlyWatched.setAdapter(homeRecentlyWatchedAdapter);
        }
        if (binding.rvCat != null) {
            binding.rvCat.setAdapter(homeCatNameAdapter);
        }
        if (binding.rvTop10 != null) {
            binding.rvTop10.setAdapter(homeTopItemsAdapter);
        }
        if (binding.rvNewReleases != null) {
            binding.rvNewReleases.setAdapter(newReleasesAdapter);
        }

        // Set up genre RecyclerView if it exists
        if (binding.rvGenere != null) {
            FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(requireActivity());
            layoutManager.setFlexDirection(FlexDirection.ROW);
            layoutManager.setJustifyContent(JustifyContent.CENTER);
            layoutManager.setAlignItems(AlignItems.CENTER);

            binding.rvGenere.setLayoutManager(layoutManager);
            binding.rvGenere.setAdapter(genreAdapter);
        }
        
        // Set up horizontal categories RecyclerView
        if (binding.rvHorizontalCategories != null) {
            binding.rvHorizontalCategories.setAdapter(horizontalCategoryAdapter);
        }

        // Set blur only if views exist
        if (binding.blurLout != null && binding.rootLout != null && requireActivity() instanceof BaseActivity) {
            ((BaseActivity) requireActivity()).setBlur(binding.blurLout, binding.rootLout, 15f);
        }

    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            // Comprehensive null checks to prevent crashes
            if (homeFeaturedAdapter == null || 
                binding == null || 
                binding.rvFeatured == null || 
                requireActivity().isDestroyed()) {
                return; // Exit early if anything is null or activity is destroyed
            }
            
            if (reversed) {
                if (scrollingPos - 1 < 0) {
                    scrollingPos += 1;
                    reversed = false;
                } else {
                    scrollingPos -= 1;
                }
            } else {
                if (scrollingPos + 1 > homeFeaturedAdapter.getItemCount() - 1) {
                    scrollingPos -= 1;
                    reversed = true;
                } else {
                    scrollingPos += 1;
                    reversed = false;
                }
            }

            scrollToPos(false);
        }
    };

    private void setContentData() {

        if (homeFeaturedAdapter == null || homeFeaturedAdapter.getList() == null) {
            return; // Exit early if adapter or list is null
        }

        if (-1 < scrollingPos && scrollingPos < homeFeaturedAdapter.getList().size()) {

            scrollingItem = homeFeaturedAdapter.getList().get(scrollingPos);
            if (scrollingItem != null && getActivity() != null && !requireActivity().isDestroyed() && 
                binding != null && binding.img != null) {
                binding.setContent(scrollingItem);
                Glide.with(binding.img)
                    .load(Const.IMAGE_URL + scrollingItem.getVerticalPoster())
                    .placeholder(R.drawable.bg_for_bottomsheet) // Use a static drawable as placeholder
                    .apply(
                        new RequestOptions().error(
                            R.color.transparent
                        ).priority(Priority.HIGH)
                    ).into(binding.img);

                if (scrollingItem.getGenreString().isEmpty()) {
                    List<String> list = Global.getGenreListFromIds(scrollingItem.getGenreIds(), requireActivity());
                    scrollingItem.setGenreList(list);
                    setGenreAdapter(list);

                } else {
                    setGenreAdapter(scrollingItem.getGenreList());

                }
            }
        }

    }


    private void setGenreAdapter(List<String> list) {
        if (genreAdapter == null || list == null) {
            return; // Exit early if adapter is null
        }
        
        if (list.size() > 8) {
            List<String> newList = list.subList(0, 8);
            Log.i("TAG", "setGenreAdapter: " + newList.size());
            genreAdapter.updateItems(newList);

        } else {
            genreAdapter.updateItems(list);

        }
    }

    private void setupHeaderWithLogoAndProfile() {
        // Update profile name based on current user profile
        updateProfileName();
        
        // Removed header buttons - no longer needed after UI update
        
        // Set up sticky categories RecyclerView
        // TODO: Add sticky categories if needed
        // if (binding.rvCategoriesSticky != null) {
        //     binding.rvCategoriesSticky.setAdapter(horizontalCategoryAdapter);
        // }
    }
    
    private void setupHorizontalCategoryList() {
        // Setup fixed navigation buttons
        setupFixedNavigationButtons();
        
        Log.d("HomeFragment", "Fixed navigation buttons set up successfully");
    }
    
    private void setupFixedNavigationButtons() {
        // TV Shows button
        if (binding.btnTVShows != null) {
            binding.btnTVShows.setOnClickListener(v -> {
                Log.d("HomeFragment", "TV Shows clicked");
                Intent intent = new Intent(getActivity(), com.retry.vuga.activities.TVShowsCategoriesActivity.class);
                startActivity(intent);
                updateSelectedNavButton(binding.btnTVShows);
            });
        }
        
        // Movies button  
        if (binding.btnMovies != null) {
            binding.btnMovies.setOnClickListener(v -> {
                Log.d("HomeFragment", "Movies clicked");
                Intent intent = new Intent(getActivity(), com.retry.vuga.activities.MoviesCategoriesActivity.class);
                startActivity(intent);
                updateSelectedNavButton(binding.btnMovies);
            });
        }
        
        // Live TV button
        if (binding.btnLiveTV != null) {
            binding.btnLiveTV.setOnClickListener(v -> {
                Log.d("HomeFragment", "Live TV clicked");
                navigateToLiveTv();
                updateSelectedNavButton(binding.btnLiveTV);
            });
        }
        
        // No default button selection - user must explicitly choose
    }
    
    private void updateSelectedNavButton(TextView selectedButton) {
        // Reset all buttons to unselected state
        if (binding.btnTVShows != null) {
            binding.btnTVShows.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), R.color.transparent));
            binding.btnTVShows.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
        }
        
        if (binding.btnMovies != null) {
            binding.btnMovies.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), R.color.transparent));
            binding.btnMovies.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
        }
        
        if (binding.btnLiveTV != null) {
            binding.btnLiveTV.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), R.color.transparent));
            binding.btnLiveTV.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
        }
        
        // Set selected button styling
        if (selectedButton != null) {
            selectedButton.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), R.color.app_color));
            selectedButton.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
        }
    }
    
    private void navigateToLiveTv() {
        // Get the MainActivity and switch to Live TV tab (position 2)
        Log.d("HomeFragment", "navigateToLiveTv called");
        try {
            // Try to get the ViewPager2 directly from the parent activity
            if (getActivity() != null && getActivity().findViewById(R.id.viewPager) != null) {
                Log.d("HomeFragment", "Found viewPager, switching to tab 2");
                androidx.viewpager2.widget.ViewPager2 viewPager = getActivity().findViewById(R.id.viewPager);
                viewPager.setCurrentItem(2, false); // Live TV is at position 2
                Log.d("HomeFragment", "Successfully switched to Live TV tab");
            } else {
                Log.d("HomeFragment", "Could not find viewPager");
                // Fallback: try MainActivity approach
                if (getActivity() instanceof MainActivity) {
                    Log.d("HomeFragment", "Using MainActivity fallback");
                    MainActivity mainActivity = (MainActivity) getActivity();
                    mainActivity.navigateToTab(2);
                }
            }
        } catch (Exception e) {
            Log.e("HomeFragment", "Error navigating to Live TV: " + e.getMessage(), e);
            e.printStackTrace();
        }
    }
    
    // filterContentByType method removed - now using separate activities for TV Shows and Movies
    
    private void setupCategoryDropdown() {
        if (binding.btnCategoryDropdown == null) return;
        
        dropdownMenuAdapter = new DropdownMenuAdapter();
        
        binding.btnCategoryDropdown.setOnClickListener(v -> {
            showCategoryDropdown();
        });
    }
    
    private void showCategoryDropdown() {
        if (categoryPopupWindow != null && categoryPopupWindow.isShowing()) {
            categoryPopupWindow.dismiss();
            return;
        }
        
        // Create popup window
        View popupView = LayoutInflater.from(getContext()).inflate(R.layout.dropdown_menu, null);
        RecyclerView rvDropdown = popupView.findViewById(R.id.rvDropdownItems);
        
        // Setup RecyclerView
        rvDropdown.setLayoutManager(new LinearLayoutManager(getContext()));
        rvDropdown.setAdapter(dropdownMenuAdapter);
        
        // Prepare dropdown items
        List<DropdownMenuAdapter.DropdownItem> items = new ArrayList<>();
        
        // Add section header for Genres
        items.add(new DropdownMenuAdapter.DropdownItem("── Genres ──", "header", null));
        
        // Add genres from categories
        if (catList != null && !catList.isEmpty()) {
            for (HomePage.GenreContents genre : catList) {
                if (genre.getGenre() != null && !genre.getGenre().isEmpty()) {
                    // Log the genre data to debug
                    Log.d("HomeFragment", "Genre: " + genre.getGenre() + ", ID: " + genre.getId() + ", Title: " + genre.getTitle());
                    items.add(new DropdownMenuAdapter.DropdownItem(
                        genre.getGenre(), 
                        "genre", 
                        genre
                    ));
                }
            }
        }
        
        // Add section header for Distributors
        items.add(new DropdownMenuAdapter.DropdownItem("── Distributors ──", "header", null));
        
        // Add distributors
        items.add(new DropdownMenuAdapter.DropdownItem("Amediateka", "distributor", "Amediateka"));
        items.add(new DropdownMenuAdapter.DropdownItem("HBO", "distributor", "HBO"));
        items.add(new DropdownMenuAdapter.DropdownItem("Disney", "distributor", "Disney"));
        
        dropdownMenuAdapter.setItems(items);
        
        // Set item click listener
        dropdownMenuAdapter.setOnItemClickListener((item, position) -> {
            if ("header".equals(item.type)) {
                return; // Ignore header clicks
            }
            
            categoryPopupWindow.dismiss();
            
            if ("genre".equals(item.type)) {
                // Navigate to genre content
                Intent intent = new Intent(getActivity(), ContentByGenreActivity.class);
                intent.putExtra(Const.DataKey.DATA, new Gson().toJson(item.data));
                startActivity(intent);
            } else if ("distributor".equals(item.type)) {
                // Navigate to distributor content
                Intent intent = new Intent(getActivity(), ContentByDistributorActivity.class);
                intent.putExtra("distributor_name", (String) item.data);
                startActivity(intent);
            }
        });
        
        // Create and show popup
        categoryPopupWindow = new PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        );
        
        categoryPopupWindow.setBackgroundDrawable(
            ContextCompat.getDrawable(getContext(), R.drawable.bg_for_edit_text_13)
        );
        categoryPopupWindow.setElevation(8);
        
        // Show dropdown below the button
        int[] location = new int[2];
        binding.btnCategoryDropdown.getLocationOnScreen(location);
        categoryPopupWindow.showAtLocation(
            binding.btnCategoryDropdown,
            Gravity.NO_GRAVITY,
            location[0],
            location[1] + binding.btnCategoryDropdown.getHeight()
        );
    }
    
    private void updateProfileName() {
        // Profile name display removed from header - no longer needed after UI update
    }
    
    private void extractNewReleases(List<HomePage.GenreContents> genreContents) {
        // Extract all content from genres
        List<ContentDetail.DataItem> allContent = new ArrayList<>();
        for (HomePage.GenreContents genre : genreContents) {
            if (genre.getContent() != null) {
                allContent.addAll(genre.getContent());
            }
        }
        
        // Get current year
        int currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
        
        // Filter content released in current year and remove duplicates
        Set<Integer> seenIds = new HashSet<>();
        newReleasesList = new ArrayList<>();
        
        for (ContentDetail.DataItem content : allContent) {
            // Check if release year matches current year (getReleaseYear returns primitive int)
            if (content.getReleaseYear() == currentYear) {
                if (!seenIds.contains(content.getId())) {
                    seenIds.add(content.getId());
                    newReleasesList.add(content);
                }
            }
        }
        
        // Sort by ID in descending order (newer content typically has higher IDs)
        Collections.sort(newReleasesList, (a, b) -> {
            return Integer.compare(b.getId(), a.getId());
        });
        
        // Limit to 20 items
        if (newReleasesList.size() > 20) {
            newReleasesList = newReleasesList.subList(0, 20);
        }
        
        // Update adapter
        if (newReleasesAdapter != null && !newReleasesList.isEmpty()) {
            newReleasesAdapter.updateItems(newReleasesList);
            if (binding.loutNewReleases != null) {
                binding.loutNewReleases.setVisibility(View.VISIBLE);
            }
        } else {
            if (binding.loutNewReleases != null) {
                binding.loutNewReleases.setVisibility(View.GONE);
            }
        }
    }
    
    // Removed old navigation and dropdown methods - replaced with horizontal category list

}