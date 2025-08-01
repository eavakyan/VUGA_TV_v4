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
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.flexbox.AlignItems;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.retry.vuga.R;
import com.retry.vuga.activities.BaseActivity;
import com.retry.vuga.activities.MainActivity;
import com.retry.vuga.adapters.ContentDetailGenreAdapter;
import com.retry.vuga.adapters.HomeCatNameAdapter;
import com.retry.vuga.adapters.HomeFeaturedAdapter;
import com.retry.vuga.adapters.HomeTopItemsAdapter;
import com.retry.vuga.adapters.HomeWatchlistAdapter;
import com.retry.vuga.adapters.MovieHistoryAdapter;
import com.retry.vuga.databinding.FragmentHomeBinding;
import com.retry.vuga.model.ContentDetail;
import com.retry.vuga.model.HomePage;
import com.retry.vuga.retrofit.RetrofitClient;
import com.retry.vuga.utils.Const;
import com.retry.vuga.utils.Global;
import com.retry.vuga.utils.ViewModelFactory;
import com.retry.vuga.viewmodel.HomeViewModel;
import com.retry.vuga.viewmodel.MainViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;


public class HomeFragment extends BaseFragment {
    FragmentHomeBinding binding;

    HomeFeaturedAdapter homeFeaturedAdapter;
    HomeWatchlistAdapter homeWatchlistAdapter;
    HomeTopItemsAdapter homeTopItemsAdapter;
    HomeCatNameAdapter homeCatNameAdapter;

    CompositeDisposable disposable;

    List<ContentDetail.DataItem> featuredList = new ArrayList<>();
    List<ContentDetail.DataItem> watchList = new ArrayList<>();
    List<HomePage.TopContentItem> topList = new ArrayList<>();
    List<HomePage.GenreContents> catList = new ArrayList<>();

    MainViewModel mainViewModel;
    HomeViewModel viewModel;
    ContentDetailGenreAdapter genreAdapter;


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

        binding.btnWatchlistMore.setOnClickListener(v -> {

            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).openWatchList();
            }
        });

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

                            scrollToPos(true);
                        }
                        scrolledByUser = false;
                    }
                }
            });
        }
        binding.swipeRefresh.setOnRefreshListener(() -> {
            getHomePageData();
        });

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
        if (handler != null && !requireActivity().isDestroyed()) {
            handler.postDelayed(runnable, Const.FEATURED_SCROLL);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
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

        // Only restart auto-scrolling if everything is properly initialized
        if (homeFeaturedAdapter != null && 
            homeFeaturedAdapter.getItemCount() != 0 && 
            handler != null && 
            binding != null && 
            binding.rvFeatured != null &&
            !requireActivity().isDestroyed()) {
            handler.postDelayed(runnable, Const.FEATURED_SCROLL);
        }
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

                    if (homePage != null && !requireActivity().isDestroyed()) {

                        if (homePage.getFeatured() != null && !homePage.getFeatured().isEmpty()) {
                            featuredList = new ArrayList<>();
                            featuredList.addAll(homePage.getFeatured());
                            homeFeaturedAdapter.updateItems(featuredList);
                            setContentData();
                            List<String> dotlist = new ArrayList<>();
                            for (int i = 0; i < homeFeaturedAdapter.getList().size(); i++) {
                                dotlist.add(" ");
                            }
                            // Only start auto-scrolling if fragment is still active
                            if (handler != null && !requireActivity().isDestroyed()) {
                                handler.removeCallbacks(runnable);
                                handler.postDelayed(runnable, Const.FEATURED_SCROLL);
                            }

                        }
                        if (homePage.getWatchlist() != null) {

                            if (!homePage.getWatchlist().isEmpty()) {
                                watchList = new ArrayList<>();
                                watchList.addAll(homePage.getWatchlist());
                                homeWatchlistAdapter.updateItems(watchList);
                                if (binding.loutWathlist != null) {
                                    binding.loutWathlist.setVisibility(View.VISIBLE);
                                }
                            } else {
                                if (binding.loutWathlist != null) {
                                    binding.loutWathlist.setVisibility(View.GONE);
                                }
                            }
                        } else {
                            if (binding.loutWathlist != null) {
                                binding.loutWathlist.setVisibility(View.GONE);
                            }
                        }

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

    private void initialization() {

        SnapHelper snapHelper = new PagerSnapHelper();
        if (binding.rvFeatured != null) {
            snapHelper.attachToRecyclerView(binding.rvFeatured);
        }
        handler = new Handler(Looper.getMainLooper());

        disposable = new CompositeDisposable();

        homeCatNameAdapter = new HomeCatNameAdapter();
        homeWatchlistAdapter = new HomeWatchlistAdapter();
        homeTopItemsAdapter = new HomeTopItemsAdapter();
        homeFeaturedAdapter = new HomeFeaturedAdapter();
        genreAdapter = new ContentDetailGenreAdapter();
        
        if (binding.swipeRefresh != null) {
            binding.swipeRefresh.setProgressViewOffset(true, 150, 350);
        }

        // Set adapters only if RecyclerViews exist
        if (binding.rvFeatured != null) {
            binding.rvFeatured.setAdapter(homeFeaturedAdapter);
        }
        if (binding.rvWatchlist != null) {
            binding.rvWatchlist.setAdapter(homeWatchlistAdapter);
        }
        if (binding.rvCat != null) {
            binding.rvCat.setAdapter(homeCatNameAdapter);
        }
        if (binding.rvTop10 != null) {
            binding.rvTop10.setAdapter(homeTopItemsAdapter);
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


}