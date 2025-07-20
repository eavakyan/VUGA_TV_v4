package com.retry.vuga.fragments;

import static com.retry.vuga.activities.BaseActivity.increaseView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;

import com.google.gson.Gson;
import com.retry.vuga.R;
import com.retry.vuga.activities.BaseActivity;
import com.retry.vuga.activities.PlayerNewActivity;
import com.retry.vuga.activities.ProActivity;
import com.retry.vuga.adapters.LiveTvChipsAdapter;
import com.retry.vuga.adapters.LiveTvNameAdapter;
import com.retry.vuga.databinding.FragmentLiveTvBinding;
import com.retry.vuga.model.LiveTv;
import com.retry.vuga.retrofit.RetrofitClient;
import com.retry.vuga.utils.Const;
import com.retry.vuga.utils.CustomDialogBuilder;
import com.retry.vuga.utils.adds.MyRewardAds;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;


public class LiveTvFragment extends BaseFragment {


    FragmentLiveTvBinding binding;

    LiveTvChipsAdapter liveTvChipsAdapter;
    LiveTvNameAdapter liveTvNameAdapter;
    MyRewardAds myRewardAds;
    CompositeDisposable disposable;

    boolean rewardEarned = false;
    List<LiveTv.CategoryItem> list = new ArrayList<>();


    public LiveTvFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_live_tv, container, false);

        initialization();
        setListeners();
        getAllLiveTvChannels();

        return binding.getRoot();
    }



    private void setListeners() {

        binding.blurView.setOnClickListener(v -> {

        });
        binding.centerLoader.setOnClickListener(v -> {

        });


        liveTvNameAdapter.setOnItemClick(new LiveTvNameAdapter.OnItemClick() {
            @Override
            public void onClick(LiveTv.CategoryItem.TvChannelItem model) {
                //                 AccessType :  1:free , 2:paid , 3:ad
                if (model.getAccessType() == 1) {

                    increaseView(model.getId());
                    Intent intent = new Intent(getActivity(), PlayerNewActivity.class);
                    intent.putExtra(Const.DataKey.LIVE_TV_MODEL, new Gson().toJson(model));
                    getActivity().startActivity(intent);

                } else if (model.getAccessType() == 2) {
//                        premium pop up
                    showPremiumPopup();


                } else if (model.getAccessType() == 3) {
//                      video ad pop up
                    showADDPopup(model);

                }
            }
        });

    }


    private void showADDPopup(LiveTv.CategoryItem.TvChannelItem model) {

        binding.blurView.setVisibility(View.VISIBLE);

        new CustomDialogBuilder(requireActivity()).showUnlockDialog(new CustomDialogBuilder.OnDismissListener() {
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

                    Intent intent = new Intent(getActivity(), PlayerNewActivity.class);
                    intent.putExtra(Const.DataKey.LIVE_TV_MODEL, new Gson().toJson(model));
                    getActivity().startActivity(intent);
                    rewardEarned = false;
                }
                myRewardAds = new MyRewardAds(getActivity());

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


        new CustomDialogBuilder(requireActivity()).showPremiumDialog(new CustomDialogBuilder.OnDismissListener() {
            @Override
            public void onPositiveDismiss() {

                startActivity(new Intent(getActivity(), ProActivity.class));

            }


            @Override
            public void onDismiss() {
                
                binding.blurView.setVisibility(View.GONE);
            }
        });


    }

    private void getAllLiveTvChannels() {


        disposable.clear();


        disposable.add(RetrofitClient.getService().getLiveTvChannel()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .doOnSubscribe(disposable1 -> {


                    binding.centerLoader.setVisibility(View.VISIBLE);
                    binding.mainLout.setVisibility(View.GONE);
                    binding.tvNoContent.setVisibility(View.GONE);


                })
                .doOnTerminate(() -> {

                    binding.mainLout.setVisibility(View.VISIBLE);
                    binding.centerLoader.setVisibility(View.GONE);


                }).doOnError(throwable -> {

                    binding.centerLoader.setVisibility(View.VISIBLE);
                    binding.mainLout.setVisibility(View.GONE);


                })
                .subscribe((liveTv, throwable) -> {


                    if (liveTv != null) {

                        if (liveTv.getStatus()) {
                            if (!liveTv.getData().isEmpty()) {

                                for (int i = 0; i < liveTv.getData().size(); i++) {
                                    if (!liveTv.getData().get(i).getChannels().isEmpty()) {
                                        list.add(liveTv.getData().get(i));
                                    }
                                }

                                liveTvNameAdapter.updateItems(list);
                                liveTvChipsAdapter.updateItems(list);
                            }
                        } else {

                            binding.tvNoContent.setVisibility(View.VISIBLE);
                        }
                    }

                }));


    }

    private void initialization() {
        myRewardAds = new MyRewardAds(getActivity());
        disposable = new CompositeDisposable();

        liveTvChipsAdapter = new LiveTvChipsAdapter();
        liveTvNameAdapter = new LiveTvNameAdapter();


        binding.rvChips.setAdapter(liveTvChipsAdapter);
        binding.rvLiveTv.setAdapter(liveTvNameAdapter);

        if (requireActivity() instanceof BaseActivity) {
            ((BaseActivity) requireActivity()).setBlur(binding.blurView, binding.rootLout, 10f);
        }

    }
}