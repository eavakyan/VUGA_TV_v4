package com.retry.vuga.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.retry.vuga.R;
import com.retry.vuga.adapters.LiveTvObjectAdapter;
import com.retry.vuga.databinding.ActivityChannelByCategoriesBinding;
import com.retry.vuga.model.LiveTv;
import com.retry.vuga.retrofit.RetrofitClient;
import com.retry.vuga.utils.Const;
import com.retry.vuga.utils.CustomDialogBuilder;
import com.retry.vuga.utils.adds.MyRewardAds;

import org.jetbrains.annotations.NotNull;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class ChannelByCategoriesActivity extends BaseActivity {
    ActivityChannelByCategoriesBinding binding;
    int catId;
    String categoryName;
    MyRewardAds myRewardAds;


    boolean rewardEarned = false;
    CompositeDisposable disposable;
    boolean isLoading = false;
    LiveTvObjectAdapter liveTvObjectAdapter;
    GridLayoutManager gridLayoutManager;
    boolean dataOver = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_channel_by_categories);

        initialization();
        setListeners();


        getChannels();

    }


    private void initialization() {
        myRewardAds = new MyRewardAds(this);
        disposable = new CompositeDisposable();
        liveTvObjectAdapter = new LiveTvObjectAdapter(this);

        binding.rv.setAdapter(liveTvObjectAdapter);
        binding.rv.setItemAnimator(null);


        catId = getIntent().getIntExtra(Const.DataKey.CAT_ID, 0);
        categoryName = getIntent().getStringExtra(Const.DataKey.CAT_NAME);


        if (categoryName != null) {

            binding.tvName.setText(categoryName);
        }

        setBlur(binding.blurView, binding.rootLout, 10f);
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
                String accessType = model.getAccessType();
                if ("1".equals(accessType)) {

                    BaseActivity.increaseView(String.valueOf(model.getId()));
                    Intent intent = new Intent(ChannelByCategoriesActivity.this, PlayerNewActivity.class);
                    intent.putExtra(Const.DataKey.LIVE_TV_MODEL, new Gson().toJson(model));
                    startActivity(intent);

                } else if ("2".equals(accessType)) {
//                        premium pop up
                    showPremiumPopup();


                } else if ("3".equals(accessType)) {
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
                            && !isLoading && catId != 0) {
                        getChannels();
                    }
                }
            }
        });
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
                    BaseActivity.increaseView(String.valueOf(model.getId())); //api

                    Intent intent = new Intent(ChannelByCategoriesActivity.this, PlayerNewActivity.class);
                    intent.putExtra(Const.DataKey.LIVE_TV_MODEL, new Gson().toJson(model));
                    startActivity(intent);
                    rewardEarned = false;
                }
                myRewardAds = new MyRewardAds(ChannelByCategoriesActivity.this);

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

                startActivity(new Intent(ChannelByCategoriesActivity.this, ProActivity.class));
            }

            @Override
            public void onDismiss() {
                binding.blurView.setVisibility(View.GONE);

            }
        });

    }

    private void getChannels() {

        if (!dataOver) {

            disposable.clear();

            disposable.add(RetrofitClient.getService().getChannelByCategories(liveTvObjectAdapter.getItemCount(), Const.PAGINATION_COUNT, catId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .unsubscribeOn(Schedulers.io())
                    .doOnSubscribe(disposable1 -> {

                        if (liveTvObjectAdapter.getItemCount() == 0) {
                            binding.centerLoader.setVisibility(View.VISIBLE);
                        }
                        isLoading = true;
                        binding.tvNoContent.setVisibility(View.GONE);


                    })
                    .doOnTerminate(() -> {

                        isLoading = false;

                        binding.centerLoader.setVisibility(View.GONE);


                    }).doOnError(throwable -> {

                        Toast.makeText(this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();

                        isLoading = false;


                    })
                    .subscribe((channelByCategories, throwable) -> {

                        binding.centerLoader.setVisibility(View.GONE);

                        if (channelByCategories != null && channelByCategories.getStatus() && channelByCategories.getData() != null) {

                            if (channelByCategories.getData().getChannels().isEmpty()) {
                                if (liveTvObjectAdapter.getItemCount() == 0) {
                                    binding.tvNoContent.setVisibility(View.VISIBLE);

                                } else {
                                    dataOver = true;
                                }
                            } else {


                                if (liveTvObjectAdapter.getItemCount() == 0) {
                                    liveTvObjectAdapter.updateItems(channelByCategories.getData().getChannels());

                                } else {
                                    liveTvObjectAdapter.loadMoreItems(channelByCategories.getData().getChannels());

                                }


                            }


                        }

                    }));
        }
    }

}