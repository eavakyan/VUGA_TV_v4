package com.retry.vuga.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.retry.vuga.R;
import com.retry.vuga.adapters.HomeCatNameAdapter;
import com.retry.vuga.databinding.ActivityMoviesCategoriesBinding;
import com.retry.vuga.model.HomePage;
import com.retry.vuga.retrofit.RetrofitClient;
import com.retry.vuga.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class MoviesCategoriesActivity extends BaseActivity {
    
    private ActivityMoviesCategoriesBinding binding;
    private CompositeDisposable disposable;
    private SessionManager sessionManager;
    private HomeCatNameAdapter adapter;
    private List<HomePage.GenreContents> movieCategories = new ArrayList<>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_movies_categories);
        
        initialization();
        setListeners();
        loadMoviesData();
    }
    
    private void initialization() {
        disposable = new CompositeDisposable();
        sessionManager = new SessionManager(this);
        adapter = new HomeCatNameAdapter();
        
        binding.tvTitle.setText("Movies");
        binding.rvCategories.setLayoutManager(new LinearLayoutManager(this));
        binding.rvCategories.setAdapter(adapter);
    }
    
    private void setListeners() {
        binding.btnBack.setOnClickListener(v -> {
            getOnBackPressedDispatcher().onBackPressed();
        });
        
        binding.swipeRefresh.setOnRefreshListener(() -> {
            loadMoviesData();
        });
    }
    
    private void loadMoviesData() {
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
                        // Filter for Movies content (content_type = 1 for movies)
                        movieCategories = homePage.getGenreContents().stream()
                                .filter(genreContents -> !genreContents.getContent().isEmpty())
                                .filter(genreContents -> {
                                    // Check if this category contains movies
                                    return genreContents.getContent().stream()
                                            .anyMatch(content -> content.getType() == 1);
                                })
                                .collect(Collectors.toList());
                        
                        // Filter each category to only show movies
                        for (HomePage.GenreContents category : movieCategories) {
                            List<com.retry.vuga.model.ContentDetail.DataItem> moviesOnly = 
                                    category.getContent().stream()
                                            .filter(content -> content.getType() == 1)
                                            .collect(Collectors.toList());
                            category.setContent(moviesOnly);
                        }
                        
                        if (!movieCategories.isEmpty()) {
                            adapter.updateItems(movieCategories);
                            binding.rvCategories.setVisibility(View.VISIBLE);
                            binding.tvNoData.setVisibility(View.GONE);
                        } else {
                            showNoData();
                        }
                    } else {
                        showNoData();
                    }
                }, throwable -> {
                    Log.e("MoviesCategories", "Error loading movies data", throwable);
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