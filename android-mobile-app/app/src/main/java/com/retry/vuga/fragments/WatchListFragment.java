package com.retry.vuga.fragments;

import static com.retry.vuga.activities.BaseActivity.addRemoveWatchlist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.retry.vuga.R;
import com.retry.vuga.activities.BaseActivity;
import com.retry.vuga.adapters.WatchListAdapter;
import com.retry.vuga.databinding.FragmentWatchListBinding;
import com.retry.vuga.model.ContentDetail;
import com.retry.vuga.retrofit.RetrofitClient;
import com.retry.vuga.utils.Const;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;


public class WatchListFragment extends BaseFragment {
    private static final int ALL = 0;
    private static final int MOVIES = 1;
    private static final int SERIES = 2;
    int contentType = ALL;
    FragmentWatchListBinding binding;

    WatchListAdapter watchListAdapter;

    CompositeDisposable disposable;
    LinearLayoutManager linearLayoutManager;
    boolean isLoading = false;
    boolean dataOver = false;
    List<ContentDetail.DataItem> list = new ArrayList<>();


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

        binding.rvWatchlist.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull @NotNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (watchListAdapter.getItemCount() - 1 == linearLayoutManager.findLastVisibleItemPosition() && !isLoading) {
                        getWatchlist();
                    }
                }
            }
        });
    }


    private void changeData() {

        dataOver = false;
        list.clear();
        watchListAdapter.updateItems(list);
        getWatchlist();
    }

    @Override
    public void onResume() {
        super.onResume();
        dataOver = false;
        list.clear();
        watchListAdapter.updateItems(list);
        getWatchlist();

    }



    private void getWatchlist() {

        if (!dataOver) {

            disposable.clear();
            disposable.add(RetrofitClient.getService().getWatchList(contentType, sessionManager.getUser().getId(), watchListAdapter.getItemCount(), Const.PAGINATION_COUNT)
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

    private void initialization() {

//        if (Global.customerInfo != null) {
//            binding.tvRevenue.setText(String.valueOf(Global.customerInfo));
//        }

        disposable = new CompositeDisposable();
        watchListAdapter = new WatchListAdapter();

        binding.rvWatchlist.setAdapter(watchListAdapter);
        binding.rvWatchlist.setItemAnimator(null);


    }
}