package com.retry.vuga.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.retry.vuga.R;
import com.retry.vuga.adapters.ContentGridAdapter;
import com.retry.vuga.databinding.ActivityContentByGenreBinding;
import com.retry.vuga.model.HomePage;
import com.retry.vuga.retrofit.RetrofitClient;
import com.retry.vuga.utils.Const;
import com.retry.vuga.utils.SessionManager;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class ContentByGenreActivity extends BaseActivity {
    ActivityContentByGenreBinding binding;

    CompositeDisposable disposable;
    SessionManager sessionManager;
    boolean isLoading = false;
    ContentGridAdapter contentGridAdapter;
    GridLayoutManager gridLayoutManager;
    boolean dataOver = false;
    HomePage.GenreContents genreContents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_content_by_genre);
        initialization();
        setListeners();


        getContent();

    }

    boolean isFirstTime = true;

    private void initialization() {
        disposable = new CompositeDisposable();
        sessionManager = new SessionManager(this);
        contentGridAdapter = new ContentGridAdapter();
        gridLayoutManager = new GridLayoutManager(this, 3); // 3 columns for grid

        binding.rv.setLayoutManager(gridLayoutManager);
        binding.rv.setAdapter(contentGridAdapter);
        binding.rv.setItemAnimator(null);
        String s = getIntent().getStringExtra(Const.DataKey.DATA);
        Log.d("ContentByGenre", "Received data: " + s);
        
        try {
            genreContents = new Gson().fromJson(s, HomePage.GenreContents.class);
            if (genreContents != null) {
                binding.tvName.setText(genreContents.getTitle());
                Log.d("ContentByGenre", "Genre: " + genreContents.getTitle() + ", ID: " + genreContents.getId());
            } else {
                Log.e("ContentByGenre", "Failed to parse genre data");
                binding.tvName.setText("Content");
            }
        } catch (Exception e) {
            Log.e("ContentByGenre", "Error parsing genre data", e);
            binding.tvName.setText("Content");
            // Don't finish the activity, let it continue with empty/default data
        }
    }

    private void setListeners() {

        binding.swipeRefresh.setOnRefreshListener(() -> {
            contentGridAdapter.clear();
            getContent();
        });
        binding.centerLoader.setOnClickListener(v -> {

        });
        binding.btnBack.setOnClickListener(v -> {
            getOnBackPressedDispatcher().onBackPressed();
        });

        binding.rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull @NotNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                gridLayoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (contentGridAdapter.getItemCount() - 1 == gridLayoutManager.findLastVisibleItemPosition()
                            && !isLoading) {
                        getContent();
                    }
                }
            }
        });
    }

    private void getContent() {
        
        // Check if genreContents is null
        if (genreContents == null) {
            Log.e("ContentByGenre", "Genre data is null");
            binding.centerLoader.setVisibility(View.GONE);
            binding.swipeRefresh.setRefreshing(false);
            binding.tvNoContent.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Unable to load content. Invalid category.", Toast.LENGTH_LONG).show();
            return;
        }
        
        // If ID is 0, try showing the pre-loaded content
        if (genreContents.getId() <= 0) {
            Log.w("ContentByGenre", "Genre ID is 0 or invalid. Genre: " + genreContents.getGenre() + 
                  ", Title: " + genreContents.getTitle());
            
            // Show the content that was already loaded with the genre from the home page
            Log.d("ContentByGenre", "Showing pre-loaded content for genre: " + genreContents.getGenre());
            if (genreContents.getContent() != null && !genreContents.getContent().isEmpty()) {
                contentGridAdapter.updateItems(genreContents.getContent());
                binding.rv.setVisibility(View.VISIBLE);
                binding.centerLoader.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);
                // Hide the swipe refresh since we're not loading from API
                binding.swipeRefresh.setEnabled(false);
            } else {
                binding.tvNoContent.setVisibility(View.VISIBLE);
                binding.centerLoader.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);
                Toast.makeText(this, "No content available for this category", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        
        if (!dataOver) {
            
            disposable.clear();
            isLoading = true;
            
            // Calculate page number (V2 API uses page-based pagination instead of offset-based)
            int currentPage = (contentGridAdapter.getItemCount() / Const.PAGINATION_COUNT) + 1;
            
            Log.d("ContentByGenre", "Loading content for category ID: " + genreContents.getId() + 
                  ", page: " + currentPage + 
                  ", per_page: " + Const.PAGINATION_COUNT);

            disposable.add(RetrofitClient.getService().getContentByGenre(currentPage, Const.PAGINATION_COUNT, genreContents.getId())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .unsubscribeOn(Schedulers.io())
                    .doOnSubscribe(disposable1 -> {
                        if (isFirstTime) {
                            isFirstTime = false;
                            binding.centerLoader.setVisibility(View.VISIBLE);
                        }
                        if (contentGridAdapter.getItemCount() == 0) {
                            binding.rv.setVisibility(View.GONE);
                        } else {
                        }
                        binding.tvNoContent.setVisibility(View.GONE);
                    })
                    .doOnTerminate(() -> {

                        binding.centerLoader.setVisibility(View.GONE);
                        binding.swipeRefresh.setRefreshing(false);
                        isLoading = false;


                    }).doOnError(throwable -> {
                        Log.e("ContentByGenre", "API Error: " + throwable.getMessage(), throwable);
                        String errorMsg = "Error loading content";
                        if (throwable.getMessage() != null && throwable.getMessage().contains("404")) {
                            errorMsg = "Category not found";
                        } else if (throwable.getMessage() != null && throwable.getMessage().contains("500")) {
                            errorMsg = "Server error. Please try again later.";
                        }
                        Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
                        if (contentGridAdapter.getItemCount() == 0) {
                            binding.tvNoContent.setVisibility(View.VISIBLE);
                        }
                        isLoading = false;
                    })
                    .subscribe((contentByGenre, throwable) -> {
                        if (throwable != null) {
                            Log.e("ContentByGenre", "Subscribe Error: " + throwable.getMessage(), throwable);
                            String errorDetail = throwable.getMessage() != null ? throwable.getMessage() : "Unknown error";
                            Log.e("ContentByGenre", "Error details - Category: " + genreContents.getTitle() + 
                                  ", ID: " + genreContents.getId() + ", Error: " + errorDetail);
                            return;
                        }

                        if (contentByGenre != null && contentByGenre.getStatus() && contentByGenre.getData() != null) {


                            if (contentByGenre.getData().isEmpty()) {
                                if (contentGridAdapter.getItemCount() == 0) {
                                    binding.tvNoContent.setVisibility(View.VISIBLE);

                                } else {
                                    dataOver = true;
                                }
                            } else {


                                if (contentGridAdapter.getItemCount() == 0) {
                                    contentGridAdapter.updateItems(contentByGenre.getData());

                                } else {
                                    contentGridAdapter.loadMoreItems(contentByGenre.getData());

                                }

                                binding.rv.setVisibility(View.VISIBLE);

                            }

                        } else
                            Toast.makeText(this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();


                    }));
        }
    }
}