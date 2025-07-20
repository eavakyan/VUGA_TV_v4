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
import com.retry.vuga.adapters.LiveTvObjectAdapter;
import com.retry.vuga.databinding.ActivitySearchLiveTvBinding;
import com.retry.vuga.model.LiveTv;
import com.retry.vuga.retrofit.RetrofitClient;
import com.retry.vuga.utils.Const;
import com.retry.vuga.utils.CustomDialogBuilder;
import com.retry.vuga.utils.adds.MyRewardAds;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class SearchLiveTvActivity extends BaseActivity {
    ActivitySearchLiveTvBinding binding;

    MyRewardAds myRewardAds;
    boolean rewardEarned = false;
    CompositeDisposable disposable;
    boolean isLoading = false;
    LiveTvObjectAdapter liveTvObjectAdapter;
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

    private void getChannels() {


        binding.tvType.setVisibility(keyWord.isEmpty() ? View.VISIBLE : View.GONE);
        binding.centerLoader.setVisibility(View.GONE);
        binding.tvNoContent.setVisibility(View.GONE);

        if (dataOver || isLoading || keyWord.isEmpty()) {
            return;
        }
        isLoading = true;
        hashMap.clear();
        hashMap.put(Const.ApiKey.start, liveTvObjectAdapter.getItemCount());
        hashMap.put(Const.ApiKey.limit, Const.PAGINATION_COUNT);
        hashMap.put(Const.ApiKey.keyword, keyWord);

        disposable.clear();
        disposable.add(RetrofitClient.getService().searchTVChannel(hashMap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .doOnSubscribe(disposable1 -> {

                    if (liveTvObjectAdapter.getItemCount() == 0) {
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
                .subscribe((searchChannel, throwable) -> {

                    binding.centerLoader.setVisibility(View.GONE);

                    if (searchChannel != null && searchChannel.getStatus()) {

                        if (searchChannel.getData().isEmpty()) {
                            if (liveTvObjectAdapter.getItemCount() == 0) {
                                binding.tvNoContent.setVisibility(View.VISIBLE);

                            } else {
                                dataOver = true;
                            }
                        } else {


                            if (liveTvObjectAdapter.getItemCount() == 0) {
                                liveTvObjectAdapter.updateItems(searchChannel.getData());

                            } else {
                                liveTvObjectAdapter.loadMoreItems(searchChannel.getData());

                            }
                            binding.rv.setVisibility(View.VISIBLE);


                        }


                    }

                }));

    }

    private void showADDPopup(LiveTv.CategoryItem.TvChannelItem model) {
        binding.blurView.setVisibility(View.VISIBLE);

        new CustomDialogBuilder(this).showUnlockDialog(new CustomDialogBuilder.OnDismissListener() {
            @Override
            public void onPositiveDismiss() {
                loadRewardedAdd(model);
            }


            @Override
            public void onDismiss() {
                
                binding.blurView.setVisibility(View.GONE);
            }
        });


    }

    private void loadRewardedAdd(LiveTv.CategoryItem.TvChannelItem model) {
        myRewardAds.showAd();

        myRewardAds.setRewardAdListnear(new MyRewardAds.RewardAdListnear() {
            @Override
            public void onAdClosed() {

                Log.i("TAG", "add:closed ");
                if (rewardEarned) {
                    increaseView(model.getId()); //api

                    Intent intent = new Intent(SearchLiveTvActivity.this, PlayerNewActivity.class);
                    intent.putExtra(Const.DataKey.LIVE_TV_MODEL, new Gson().toJson(model));
                    startActivity(intent);
                    rewardEarned = false;
                }
                myRewardAds = new MyRewardAds(SearchLiveTvActivity.this);

            }

            @Override
            public void onEarned() {

                rewardEarned = true;
                Log.i("TAG", "add:earned ");


            }
        });


    }


    private void showPremiumPopup() {
        binding.blurView.setVisibility(View.VISIBLE);


        new CustomDialogBuilder(this).showPremiumDialog(new CustomDialogBuilder.OnDismissListener() {
            @Override
            public void onPositiveDismiss() {

                startActivity(new Intent(SearchLiveTvActivity.this, ProActivity.class));
            }

            @Override
            public void onDismiss() {
                binding.blurView.setVisibility(View.GONE);

            }
        });

    }

    private void setListeners() {
        binding.blurView.setOnClickListener(v -> {

        });
        binding.centerLoader.setOnClickListener(v -> {

        });

        binding.btnBack.setOnClickListener(v -> {
            getOnBackPressedDispatcher().onBackPressed();
        });

        liveTvObjectAdapter.setOnItemClick(new LiveTvObjectAdapter.OnItemClick() {
            @Override
            public void onClick(LiveTv.CategoryItem.TvChannelItem model) {
                //                 AccessType :  1:free , 2:paid , 3:ad
                if (model.getAccessType() == 1) {

                    increaseView(model.getId());
                    Intent intent = new Intent(SearchLiveTvActivity.this, PlayerNewActivity.class);
                    intent.putExtra(Const.DataKey.LIVE_TV_MODEL, new Gson().toJson(model));
                    startActivity(intent);

                } else if (model.getAccessType() == 2) {
//                        premium pop up
                    showPremiumPopup();


                } else if (model.getAccessType() == 3) {
//                      video ad pop up
                    showADDPopup(model);

                }
            }
        });


        binding.rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull @NotNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                gridLayoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {

                    Log.i("TAG", "onCreate: " + liveTvObjectAdapter.getItemCount() + gridLayoutManager.findLastVisibleItemPosition());

                    if (liveTvObjectAdapter.getItemCount() - 1 == gridLayoutManager.findLastVisibleItemPosition()
                            && !isLoading) {
                        getChannels();
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
        liveTvObjectAdapter.clear();
        liveTvObjectAdapter.updateItems(new ArrayList<>());
        getChannels();
    }

    private void initialization() {
        myRewardAds = new MyRewardAds(this);
        disposable = new CompositeDisposable();
        liveTvObjectAdapter = new LiveTvObjectAdapter(2);

        binding.rv.setAdapter(liveTvObjectAdapter);
        binding.rv.setItemAnimator(null);

        setBlur(binding.blurView, binding.rootLout, 10f);

        binding.tvType.setVisibility(View.VISIBLE);
    }
}