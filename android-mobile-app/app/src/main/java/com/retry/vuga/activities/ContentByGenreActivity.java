package com.retry.vuga.activities;

import android.os.Bundle;
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
        genreContents = new Gson().fromJson(s, HomePage.GenreContents.class);

        binding.tvName.setText(genreContents.getTitle());
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

        if (!dataOver) {


            disposable.clear();
            isLoading = true;

            disposable.add(RetrofitClient.getService().getContentByGenre(contentGridAdapter.getItemCount(), Const.PAGINATION_COUNT, genreContents.getId())
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

                        Toast.makeText(this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();

                        isLoading = false;


                    })
                    .subscribe((contentByGenre, throwable) -> {


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