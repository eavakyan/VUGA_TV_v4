package com.retry.vuga.activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.retry.vuga.R;
import com.retry.vuga.databinding.ActivitySplashBinding;
import com.retry.vuga.retrofit.RetrofitClient;
import com.retry.vuga.utils.Const;
import com.retry.vuga.utils.DownloadService;
import com.retry.vuga.utils.SessionManager;

import org.json.JSONObject;

import io.branch.referral.Branch;
import io.branch.referral.BranchError;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class SplashActivity extends BaseActivity {
    ActivitySplashBinding binding;
    SessionManager sessionManager;
    CompositeDisposable disposable;
    private final Branch.BranchReferralInitListener branchReferralInitListener = new Branch.BranchReferralInitListener() {
        @Override
        public void onInitFinished(@Nullable @org.jetbrains.annotations.Nullable JSONObject linkProperties, @Nullable @org.jetbrains.annotations.Nullable BranchError error) {
            if (linkProperties != null && linkProperties.has(Const.DataKey.CONTENT_ID)) {
                sessionManager.saveBranchData(new Gson().toJson(linkProperties));
            }
            getSettingData();

        }
    };


    @Override
    protected void onStart() {
        super.onStart();
        Branch.sessionBuilder(this).withCallback(branchReferralInitListener).withData(getIntent() != null ? getIntent().getData() : null).init();
        sessionManager.removeBranchData();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        // if activity is in foreground (or in backstack but partially visible) launching the same
        // activity will skip onStart, handle this case with reInitSession
        Branch.sessionBuilder(this).withCallback(branchReferralInitListener).reInit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash);
        sessionManager = new SessionManager(this);
        disposable = new CompositeDisposable();

        if (!sessionManager.getBooleanValue(Const.DataKey.NOT_NEW_USER)) {

            sessionManager.saveBooleanValue(Const.DataKey.NOT_NEW_USER, true);
            sessionManager.saveBooleanValue(Const.DataKey.NOTIFICATION, true);
            FirebaseMessaging.getInstance().subscribeToTopic(Const.FIREBASE_SUB_TOPIC);


        }
        Log.d("TAG", "onCreate: " + getIntent().getIntExtra("content_id", -1));
        Intent service = new Intent(getApplicationContext(), DownloadService.class);
        this.startService(service);
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    private void getSettingData() {

        disposable.add(RetrofitClient.getService()
                .fetchCustomAds(1)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .doOnError(Throwable::printStackTrace)
                .subscribe((customAds, throwable) -> {
                            sessionManager.saveCustomAds(customAds);
                        }
                )
        );

        disposable.add(RetrofitClient.getService().getAppSettings()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .doOnSubscribe(disposable1 -> {


                })
                .doOnTerminate(() -> {


                }).doOnError(throwable -> {
                    throwable.printStackTrace();
                    if (!isNetworkConnected()) {

                        startActivity(new Intent(this, NoInternetActivity.class));
                        finish();

                    } else
                        Toast.makeText(this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();


                })
                .subscribe((appSetting, throwable) -> {


                    if (appSetting != null) {

                        if (appSetting.getStatus()) {
                            sessionManager.saveSettingData(appSetting);
                            Log.i("TAG", "getSettingData: " + sessionManager.getUser());

                            if (sessionManager.getUser() != null) {

                                fetchRevenueData();

                                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                            } else {
                                startActivity(new Intent(SplashActivity.this, SignInActivity.class));
                            }
                            finish();

                        } else {
                            Toast.makeText(this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                        }
                    }

                }));


    }


}