package com.retry.vuga.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.retry.vuga.R;
import com.retry.vuga.adapters.HomeCatNameAdapter;
import com.retry.vuga.databinding.ActivityTvShowsCategoriesBinding;
import com.retry.vuga.model.HomePage;
import com.retry.vuga.retrofit.RetrofitClient;
import com.retry.vuga.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class TVShowsCategoriesActivity extends BaseActivity {
    
    private ActivityTvShowsCategoriesBinding binding;
    private CompositeDisposable disposable;
    private SessionManager sessionManager;
    private HomeCatNameAdapter adapter;
    private List<HomePage.GenreContents> tvShowCategories = new ArrayList<>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_tv_shows_categories);
        
        initialization();
        setListeners();
        loadTVShowsData();
    }
    
    private void initialization() {
        disposable = new CompositeDisposable();
        sessionManager = new SessionManager(this);
        adapter = new HomeCatNameAdapter();
        
        binding.tvTitle.setText("TV Shows");
        binding.rvCategories.setLayoutManager(new LinearLayoutManager(this));
        binding.rvCategories.setAdapter(adapter);
    }
    
    private void setListeners() {
        binding.btnBack.setOnClickListener(v -> {
            getOnBackPressedDispatcher().onBackPressed();
        });
        
        binding.swipeRefresh.setOnRefreshListener(() -> {
            loadTVShowsData();
        });
    }
    
    private void loadTVShowsData() {
        disposable.add(RetrofitClient.getService().getHomeData(sessionManager.getUser().getId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable1 -> {
                    binding.loutLoader.setVisibility(View.VISIBLE);
                    binding.tvNoData.setVisibility(View.GONE);
                })
                .doOnTerminate(() -> {
                    binding.loutLoader.setVisibility(View.GONE);
                    binding.swipeRefresh.setRefreshing(false);
                })
                .subscribe(homePage -> {
                    if (homePage != null && homePage.getGenreContents() != null && !homePage.getGenreContents().isEmpty()) {
                        // Filter for TV Shows content (content_type = 2 for TV shows)
                        tvShowCategories = homePage.getGenreContents().stream()
                                .filter(genreContents -> !genreContents.getContent().isEmpty())
                                .filter(genreContents -> {
                                    // Check if this category contains TV shows
                                    return genreContents.getContent().stream()
                                            .anyMatch(content -> content.getType() == 2);
                                })
                                .collect(Collectors.toList());
                        
                        // Filter each category to only show TV shows
                        for (HomePage.GenreContents category : tvShowCategories) {
                            List<com.retry.vuga.model.ContentDetail.DataItem> tvShowsOnly = 
                                    category.getContent().stream()
                                            .filter(content -> content.getType() == 2)
                                            .collect(Collectors.toList());
                            category.setContent(tvShowsOnly);
                        }
                        
                        if (!tvShowCategories.isEmpty()) {
                            adapter.updateItems(tvShowCategories);
                            binding.rvCategories.setVisibility(View.VISIBLE);
                            binding.tvNoData.setVisibility(View.GONE);
                        } else {
                            showNoData();
                        }
                    } else {
                        showNoData();
                    }
                }, throwable -> {
                    Log.e("TVShowsCategories", "Error loading TV shows data", throwable);
                    showNoData();
                }));
    }
    
    private void showNoData() {
        binding.tvNoData.setVisibility(View.VISIBLE);
        binding.rvCategories.setVisibility(View.GONE);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }
}