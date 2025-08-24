package com.retry.vuga.fragments;

import static com.retry.vuga.activities.BaseActivity.addRemoveWatchlist;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.retry.vuga.R;
import com.retry.vuga.activities.BaseActivity;
import com.retry.vuga.adapters.WatchListAdapter;
import com.retry.vuga.adapters.UnifiedWatchlistAdapter;
import com.retry.vuga.databinding.FragmentWatchListBinding;
import com.retry.vuga.model.ContentDetail;
import com.retry.vuga.model.UserRegistration;
import com.retry.vuga.model.UnifiedWatchlistItem;
import com.retry.vuga.model.UnifiedWatchlistResponse;
import com.retry.vuga.retrofit.RetrofitClient;
import com.retry.vuga.utils.Const;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;


public class WatchListFragment extends BaseFragment {
    private static final int ALL = 0;
    private static final int MOVIES = 1;
    private static final int SERIES = 2;
    private static final int CAST = 3;
    int contentType = ALL;
    FragmentWatchListBinding binding;

    WatchListAdapter watchListAdapter;
    UnifiedWatchlistAdapter unifiedAdapter;
    boolean useUnifiedApi = true;

    CompositeDisposable disposable;
    LinearLayoutManager linearLayoutManager;
    boolean isLoading = false;
    boolean dataOver = false;
    List<ContentDetail.DataItem> list = new ArrayList<>();
    List<UnifiedWatchlistItem> unifiedList = new ArrayList<>();
    Integer currentProfileId = null;
    
    private BroadcastReceiver profileChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("com.retry.vuga.PROFILE_CHANGED".equals(intent.getAction())) {
                refreshForProfileChange();
            }
        }
    };


    public WatchListFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_watch_list, container, false);
        initialization();

        setListeners();


        return binding.getRoot();
    }

    private void setListeners() {

        binding.centerLoader.setOnClickListener(v -> {

        });


        binding.tvAll.setOnClickListener(v -> {


            contentType = ALL;
            binding.setType(contentType);
            changeData();


        });


        binding.tvMovies.setOnClickListener(v -> {


            contentType = MOVIES;
            binding.setType(contentType);

            changeData();


        });

        binding.tvSeries.setOnClickListener(v -> {


            contentType = SERIES;
            binding.setType(contentType);
            changeData();


        });

        binding.tvCast.setOnClickListener(v -> {
            contentType = CAST;
            binding.setType(contentType);
            changeData();
        });


        if (useUnifiedApi && unifiedAdapter != null) {
            unifiedAdapter.setOnItemClick(new UnifiedWatchlistAdapter.OnItemClick() {
                @Override
                public void onRemoveClick(UnifiedWatchlistItem item) {
                    binding.centerLoader.setVisibility(View.VISIBLE);
                    
                    if (item.isEpisode()) {
                        // Remove episode from watchlist
                        removeEpisodeFromWatchlist(item);
                    } else {
                        // Remove content from watchlist
                        addRemoveWatchlist(getContext(), item.getContentId(), false, new BaseActivity.OnWatchList() {
                            @Override
                            public void onTerminate() {
                                binding.centerLoader.setVisibility(View.GONE);
                            }

                            @Override
                            public void onError() {
                                binding.centerLoader.setVisibility(View.GONE);
                            }

                            @Override
                            public void onSuccess() {
                                int position = unifiedAdapter.getList().indexOf(item);
                                unifiedAdapter.getList().remove(item);

                                if (unifiedAdapter.getList().isEmpty()) {
                                    unifiedAdapter.notifyDataSetChanged();
                                    binding.tvEmpty.setVisibility(View.VISIBLE);
                                } else {
                                    unifiedAdapter.notifyItemRemoved(position);
                                    unifiedAdapter.notifyItemRangeChanged(position, unifiedAdapter.getList().size());
                                }
                            }
                        });
                    }
                }

                @Override
                public void onContentDetailFetched(ContentDetail.DataItem content, UnifiedWatchlistItem item) {
                    // Optional: Handle content detail fetched if needed
                }
            });
        } else if (watchListAdapter != null) {
            watchListAdapter.setOnItemClick(new WatchListAdapter.OnItemClick() {
                @Override
                public void onRemoveClick(ContentDetail.DataItem model) {

                    binding.centerLoader.setVisibility(View.VISIBLE);
                    addRemoveWatchlist(getContext(), model.getId(), false, new BaseActivity.OnWatchList() {
                        @Override
                        public void onTerminate() {
                            binding.centerLoader.setVisibility(View.GONE);

                        }

                        @Override
                        public void onError() {
                            binding.centerLoader.setVisibility(View.GONE);

                        }

                        @Override
                        public void onSuccess() {

                            int position = watchListAdapter.getList().indexOf(model);
                            watchListAdapter.getList().remove(model);

                            if (watchListAdapter.getList().isEmpty()) {
                                watchListAdapter.notifyDataSetChanged();
                                binding.tvEmpty.setVisibility(View.VISIBLE);
                            } else {
                                watchListAdapter.notifyItemRemoved(position);
                                watchListAdapter.notifyItemRangeChanged(position, watchListAdapter.getList().size());

                            }
                        }
                    });

                }

            });
        }

        binding.rvWatchlist.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull @NotNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    int itemCount = useUnifiedApi && unifiedAdapter != null ? 
                            unifiedAdapter.getItemCount() : 
                            (watchListAdapter != null ? watchListAdapter.getItemCount() : 0);
                    
                    if (itemCount - 1 == linearLayoutManager.findLastVisibleItemPosition() && !isLoading) {
                        getWatchlist();
                    }
                }
            }
        });
    }


    private void changeData() {
        dataOver = false;
        if (useUnifiedApi) {
            unifiedList.clear();
            if (unifiedAdapter != null) {
                unifiedAdapter.updateItems(unifiedList);
            }
        } else {
            list.clear();
            if (watchListAdapter != null) {
                watchListAdapter.updateItems(list);
            }
        }
        getWatchlist();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Register broadcast receiver for profile changes
        IntentFilter filter = new IntentFilter("com.retry.vuga.PROFILE_CHANGED");
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(profileChangeReceiver, filter);
        
        dataOver = false;
        if (useUnifiedApi) {
            unifiedList.clear();
            if (unifiedAdapter != null) {
                unifiedAdapter.updateItems(unifiedList);
            }
        } else {
            list.clear();
            if (watchListAdapter != null) {
                watchListAdapter.updateItems(list);
            }
        }
        getWatchlist();
    }
    
    @Override
    public void onPause() {
        super.onPause();
        // Unregister broadcast receiver
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(profileChangeReceiver);
    }



    private void getWatchlist() {

        if (!dataOver) {
            UserRegistration.Data user = sessionManager.getUser();
            Integer profileId = user != null ? user.getLastActiveProfileId() : null;
            
            // Check if profile has changed and reset data if needed
            if (currentProfileId == null || !currentProfileId.equals(profileId)) {
                currentProfileId = profileId;
                dataOver = false;
                if (useUnifiedApi) {
                    unifiedList.clear();
                    if (unifiedAdapter != null) {
                        unifiedAdapter.updateItems(unifiedList);
                    }
                } else {
                    list.clear();
                    if (watchListAdapter != null) {
                        watchListAdapter.updateItems(list);
                    }
                }
                binding.tvEmpty.setVisibility(View.GONE);
            }

            disposable.clear();
            
            if (useUnifiedApi) {
                // Use unified watchlist API
                // Pass null for ALL (0), or the actual type value for filtering
                Integer typeParam = (contentType == ALL) ? null : contentType;
                int startOffset = unifiedAdapter != null ? unifiedAdapter.getItemCount() : 0;
                
                disposable.add(RetrofitClient.getService().getUnifiedWatchlist(
                        user != null ? user.getId() : 0, 
                        profileId, 
                        startOffset, 
                        Const.PAGINATION_COUNT, 
                        typeParam)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .unsubscribeOn(Schedulers.io())
                    .doOnSubscribe(disposable1 -> {
                        if (unifiedAdapter.getItemCount() == 0) {
                            binding.centerLoader.setVisibility(View.VISIBLE);
                        }
                        isLoading = true;
                        binding.tvEmpty.setVisibility(View.GONE);
                    })
                    .doOnTerminate(() -> {
                        isLoading = false;
                        binding.centerLoader.setVisibility(View.GONE);
                    })
                    .doOnError(throwable -> {
                        Toast.makeText(getActivity(), getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                        isLoading = false;
                    })
                    .subscribe((response, throwable) -> {
                        if (response != null && response.isStatus() && response.getData() != null) {
                            if (response.getData().isEmpty()) {
                                if (unifiedAdapter.getItemCount() == 0) {
                                    binding.tvEmpty.setVisibility(View.VISIBLE);
                                } else {
                                    dataOver = true;
                                }
                            } else {
                                if (unifiedAdapter.getItemCount() == 0) {
                                    unifiedAdapter.updateItems(response.getData());
                                } else {
                                    unifiedAdapter.loadMoreItems(response.getData());
                                }
                            }
                        }
                    }));
            } else {
                // Use legacy watchlist API
                disposable.add(RetrofitClient.getService().getWatchList(contentType, user != null ? user.getId() : 0, watchListAdapter.getItemCount(), Const.PAGINATION_COUNT, profileId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .unsubscribeOn(Schedulers.io())
                    .doOnSubscribe(disposable1 -> {
                        if (watchListAdapter.getItemCount() == 0) {

                            binding.centerLoader.setVisibility(View.VISIBLE);
                        }

                        isLoading = true;
                        binding.tvEmpty.setVisibility(View.GONE);


                    })
                    .doOnTerminate(() -> {

                        isLoading = false;
                        binding.centerLoader.setVisibility(View.GONE);


                    }).doOnError(throwable -> {

                        Toast.makeText(getActivity(), getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                        isLoading = false;
                    })
                    .subscribe((watchlist, throwable) -> {


                        if (watchlist != null && watchlist.getStatus() && watchlist.getData() != null) {


                            if (watchlist.getData().isEmpty()) {
                                if (watchListAdapter.getItemCount() == 0) {
                                    binding.tvEmpty.setVisibility(View.VISIBLE);

                                } else {
                                    dataOver = true;
                                }
                            } else {

                                if (watchListAdapter.getItemCount() == 0) {

                                    watchListAdapter.updateItems(watchlist.getData());

                                } else {

                                    watchListAdapter.loadMoreItems(watchlist.getData());

                                }

                            }
                        }


                    }));
            }
        }

    }

    private void initialization() {

//        if (Global.customerInfo != null) {
//            binding.tvRevenue.setText(String.valueOf(Global.customerInfo));
//        }

        disposable = new CompositeDisposable();
        
        if (useUnifiedApi) {
            unifiedAdapter = new UnifiedWatchlistAdapter(sessionManager);
            binding.rvWatchlist.setAdapter(unifiedAdapter);
        } else {
            watchListAdapter = new WatchListAdapter();
            binding.rvWatchlist.setAdapter(watchListAdapter);
        }
        
        binding.rvWatchlist.setItemAnimator(null);
    }
    
    // Method to refresh watchlist when profile changes
    public void refreshForProfileChange() {
        currentProfileId = null; // Force profile check
        dataOver = false;
        if (useUnifiedApi) {
            unifiedList.clear();
            if (unifiedAdapter != null) {
                unifiedAdapter.updateItems(unifiedList);
            }
        } else {
            list.clear();
            if (watchListAdapter != null) {
                watchListAdapter.updateItems(list);
            }
        }
        getWatchlist();
    }
    
    // Method to remove episode from watchlist
    private void removeEpisodeFromWatchlist(UnifiedWatchlistItem item) {
        if (item.getEpisodeId() == null) return;
        
        UserRegistration.Data user = sessionManager.getUser();
        if (user == null) return;
        
        HashMap<String, Object> params = new HashMap<>();
        params.put("app_user_id", user.getId());
        params.put("episode_id", item.getEpisodeId());
        
        Integer profileId = user.getLastActiveProfileId();
        if (profileId != null) {
            params.put("profile_id", profileId);
        }
        
        disposable.add(RetrofitClient.getService()
                .toggleEpisodeWatchlist(params)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnTerminate(() -> binding.centerLoader.setVisibility(View.GONE))
                .subscribe((response, throwable) -> {
                    if (response != null && response.getStatus()) {
                        // Remove episode from list
                        int position = unifiedAdapter.getList().indexOf(item);
                        unifiedAdapter.getList().remove(item);
                        
                        if (unifiedAdapter.getList().isEmpty()) {
                            unifiedAdapter.notifyDataSetChanged();
                            binding.tvEmpty.setVisibility(View.VISIBLE);
                        } else {
                            unifiedAdapter.notifyItemRemoved(position);
                            unifiedAdapter.notifyItemRangeChanged(position, unifiedAdapter.getList().size());
                        }
                        
                        Toast.makeText(getContext(), "Episode removed from watchlist", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                    }
                }));
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (unifiedAdapter != null) {
            unifiedAdapter.onDestroy();
        }
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }
}