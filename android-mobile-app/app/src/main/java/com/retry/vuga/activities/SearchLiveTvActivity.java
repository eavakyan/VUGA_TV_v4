package com.retry.vuga.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.retry.vuga.R;
import com.retry.vuga.adapters.ContentGridAdapter;
import com.retry.vuga.databinding.ActivitySearchLiveTvBinding;
import com.retry.vuga.model.AllContent;
import com.retry.vuga.retrofit.RetrofitClient;
import com.retry.vuga.utils.Const;
import com.retry.vuga.utils.CustomDialogBuilder;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class SearchLiveTvActivity extends BaseActivity {
    ActivitySearchLiveTvBinding binding;

    CompositeDisposable disposable;
    boolean isLoading = false;
    ContentGridAdapter contentGridAdapter;
    GridLayoutManager gridLayoutManager;
    boolean dataOver = false;
    String keyWord = "";
    HashMap<String, Object> hashMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_search_live_tv);

        initialization();
        setListeners();

    }

    private void searchContent() {


        binding.tvType.setVisibility(keyWord.isEmpty() ? View.VISIBLE : View.GONE);
        binding.centerLoader.setVisibility(View.GONE);
        binding.tvNoContent.setVisibility(View.GONE);

        if (dataOver || isLoading || keyWord.isEmpty()) {
            return;
        }
        isLoading = true;
        hashMap.clear();
        hashMap.put(Const.ApiKey.start, contentGridAdapter.getItemCount());
        hashMap.put(Const.ApiKey.limit, Const.PAGINATION_COUNT);
        hashMap.put(Const.ApiKey.keyword, keyWord);

        disposable.clear();
        disposable.add(RetrofitClient.getService().searchContent(hashMap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .doOnSubscribe(disposable1 -> {

                    if (contentGridAdapter.getItemCount() == 0) {
                        binding.centerLoader.setVisibility(View.VISIBLE);
                        binding.rv.setVisibility(View.GONE);

                    }
                    binding.tvNoContent.setVisibility(View.GONE);


                })
                .doOnTerminate(() -> {

                    binding.centerLoader.setVisibility(View.GONE);
                    isLoading = false;


                }).doOnError(throwable -> {

                    Toast.makeText(this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();

                    isLoading = false;


                })
                .subscribe((searchResult, throwable) -> {

                    binding.centerLoader.setVisibility(View.GONE);

                    if (searchResult != null && searchResult.getStatus()) {

                        if (searchResult.getData().isEmpty()) {
                            if (contentGridAdapter.getItemCount() == 0) {
                                binding.tvNoContent.setVisibility(View.VISIBLE);

                            } else {
                                dataOver = true;
                            }
                        } else {


                            if (contentGridAdapter.getItemCount() == 0) {
                                contentGridAdapter.updateItems(searchResult.getData());

                            } else {
                                contentGridAdapter.loadMoreItems(searchResult.getData());

                            }
                            binding.rv.setVisibility(View.VISIBLE);


                        }


                    }

                }));

    }

    // LiveTV specific methods removed - content clicks are handled in adapter

    private void setListeners() {
        binding.centerLoader.setOnClickListener(v -> {

        });

        binding.btnBack.setOnClickListener(v -> {
            getOnBackPressedDispatcher().onBackPressed();
        });

        // Content item clicks are handled in the adapter itself


        binding.rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull @NotNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                gridLayoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {

                    Log.i("TAG", "onCreate: " + contentGridAdapter.getItemCount() + gridLayoutManager.findLastVisibleItemPosition());

                    if (contentGridAdapter.getItemCount() - 1 == gridLayoutManager.findLastVisibleItemPosition()
                            && !isLoading) {
                        searchContent();
                    }
                }

            }


        });

        binding.etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                binding.etSearch.clearFocus();
                InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                in.hideSoftInputFromWindow(binding.etSearch.getWindowToken(), 0);
                changeData();


                return true;
            }
            return false;
        });

        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                keyWord = s.toString();
                disposable.clear();
                isLoading = false;
                changeData();
            }
        });

    }

    private void changeData() {

        dataOver = false;
        contentGridAdapter.clear();
        searchContent();
    }

    private void initialization() {
        disposable = new CompositeDisposable();
        contentGridAdapter = new ContentGridAdapter();
        gridLayoutManager = new GridLayoutManager(this, 3); // 3 columns for grid

        binding.rv.setLayoutManager(gridLayoutManager);
        binding.rv.setAdapter(contentGridAdapter);
        binding.rv.setItemAnimator(null);


        binding.tvType.setVisibility(View.VISIBLE);
    }
}