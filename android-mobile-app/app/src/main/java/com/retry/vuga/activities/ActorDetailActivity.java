package com.retry.vuga.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.databinding.DataBindingUtil;

import com.retry.vuga.R;
import com.retry.vuga.adapters.DiscoverAdapter;
import com.retry.vuga.databinding.ActivityActorDetailBinding;
import com.retry.vuga.retrofit.RetrofitClient;
import com.retry.vuga.utils.Const;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class ActorDetailActivity extends BaseActivity {
    ActivityActorDetailBinding binding;
    DiscoverAdapter discoverAdapter;
    CompositeDisposable disposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_actor_detail);


        initView();
        initListeners();
        getAcorDetails();


    }

    private void getAcorDetails() {
        int id = getIntent().getIntExtra(Const.DataKey.actor_id, 0);
        if (id == 0) {
            return;
        }

        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                disposable.add(RetrofitClient.getService().fetchActorDetails(id)
                        .observeOn(AndroidSchedulers.mainThread())
                        .unsubscribeOn(Schedulers.io())
                        .doOnSubscribe(disposab -> {


                            binding.centerLoader.setVisibility(View.VISIBLE);

                        })
                        .doOnTerminate(() -> binding.centerLoader.setVisibility(View.GONE))
                        .doOnError(throwable -> Log.i("TAG", "getHomePageData: " + throwable.getMessage()))
                        .subscribe((actorData, throwable) -> {


                            if (actorData != null && actorData.isStatus() && actorData.getData() != null) {


                                binding.setModel(actorData.getData());

                                if (actorData.getData().getActorContent().isEmpty()) {
                                    binding.tvNoContent.setVisibility(View.VISIBLE);
                                } else {
                                    discoverAdapter.updateItems(actorData.getData().getActorContent());
                                }

                            }


                        }));
            }
        });

        thread.start();


    }

    private void initView() {
        disposable = new CompositeDisposable();

        discoverAdapter = new DiscoverAdapter();
        binding.rv.setAdapter(discoverAdapter);


        setBlur(binding.loutBioBlur, binding.rootLout, 20f);


    }

    private void initListeners() {


        binding.loutBioBlur.setOnClickListener(v -> {

        });

        binding.tvDes.setOnClickListener(v -> {
            binding.loutBioBlur.setVisibility(View.VISIBLE);
        });

        binding.btnCloseBio.setOnClickListener(v -> {
            binding.loutBioBlur.setVisibility(View.GONE);

        });

        binding.btnBack.setOnClickListener(v -> {
            getOnBackPressedDispatcher().onBackPressed();
        });

    }
}