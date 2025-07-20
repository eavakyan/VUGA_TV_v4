package com.retry.vuga.activities;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.retry.vuga.R;
import com.retry.vuga.model.Downloads;
import com.retry.vuga.retrofit.RetrofitClient;
import com.retry.vuga.utils.Const;
import com.retry.vuga.utils.DeviceUtils;
import com.retry.vuga.utils.DownloadService;
import com.retry.vuga.utils.Global;
import com.retry.vuga.utils.SessionManager;
import com.revenuecat.purchases.CustomerInfo;
import com.revenuecat.purchases.Purchases;
import com.revenuecat.purchases.PurchasesError;
import com.revenuecat.purchases.interfaces.LogInCallback;
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback;

import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import eightbitlab.com.blurview.BlurAlgorithm;
import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderEffectBlur;
import eightbitlab.com.blurview.RenderScriptBlur;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.RequestBody;

public class BaseActivity extends AppCompatActivity {
    public static SessionManager sessionManager;
    public static CompositeDisposable disposable;
    MutableLiveData<Downloads> downloading_obj = new MutableLiveData<>();



    DownloadService downloadService;
    private final ServiceConnection downloadConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (downloadService == null) {
                DownloadService.DownloadBinder binder = (DownloadService.DownloadBinder) service;
                downloadService = binder.getService();


            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    Intent downloadServiceIntent;


    public BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Log.i("TAG", "internet onReceive: base ");

            String i = intent.getStringExtra(Const.DataKey.DOWNLOAD_OBJ);
            if (i != null) {

                Downloads downloads = new Gson().fromJson(i, Downloads.class);
                downloading_obj.setValue(downloads);
            }


        }
    };
    public static void increaseView(String channelId) {
        disposable.clear();
        disposable.add(RetrofitClient.getService().increaseTvChannelView(channelId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .doOnSubscribe(disposable1 -> {


                })
                .doOnTerminate(() -> {


                }).doOnError(throwable -> {


                })
                .subscribe((response, throwable) -> {
                    if (response != null) {
                        if (response.getStatus()) {
                            Log.i("TAG", "increaseDownloads: " + response.getMessage());
                        }
                    }

                }));
    }

    public void removeBlur(BlurView blurView, ViewGroup rootView) {
        blurView.setBlurEnabled(false);

    }

    public void fetchRevenueData() {


        Purchases.getSharedInstance().getCustomerInfo(new ReceiveCustomerInfoCallback() {
            @Override
            public void onReceived(@NonNull CustomerInfo customerInfo) {


                Log.i("TAG", "proceedAfterPurchase onReceived: " + customerInfo);



                if (customerInfo.getLatestExpirationDate() != null && new Date().before(customerInfo.getLatestExpirationDate())) {
                    sessionManager.saveBooleanValue(Const.DataKey.IS_PREMIUM, true);

                } else {
                    sessionManager.saveBooleanValue(Const.DataKey.IS_PREMIUM, false);

                }
//                Global.customerInfo = customerInfo;
//                Toast.makeText(BaseActivity.this, String.valueOf(sessionManager.getBooleanValue(Const.DataKey.IS_PREMIUM)), Toast.LENGTH_SHORT).show();


            }

            @Override
            public void onError(@NonNull PurchasesError purchasesError) {
                sessionManager.saveBooleanValue(Const.DataKey.IS_PREMIUM, false);

            }
        });


    }

    public static void addRemoveWatchlist(int content_id, boolean isAdd, OnWatchList onWatchList) {
        if (sessionManager.getUser() == null) {
            return;
        }

        String s = sessionManager.getUser().getWatchlist_content_ids();
        List<String> watchList = Global.convertStringToList(s);
        if (isAdd) {
            if (!watchList.contains(String.valueOf(content_id))) {
                watchList.add(String.valueOf(content_id));
            }
        } else {
            if (watchList.contains(String.valueOf(content_id))) {
                int index = watchList.indexOf(String.valueOf(content_id));

                watchList.remove(index);
            }

        }

        s = Global.listOfIntegerToString(watchList);
        HashMap<String, RequestBody> hashMap = new HashMap<>();
        hashMap.put(Const.ApiKey.user_id, RequestBody.create(String.valueOf(sessionManager.getUser().getId()), MediaType.parse("text/plain")));
        hashMap.put(Const.ApiKey.watchlist_content_ids, RequestBody.create(String.valueOf(s), MediaType.parse("text/plain")));


        disposable.add(RetrofitClient.getService().updateProfile(hashMap, null)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .doOnTerminate(() -> {
                    onWatchList.onTerminate();
                })
                .doOnError(throwable -> {
                    Log.i("TAG", "addRemoveWatchlist: " + throwable.getMessage());
                    onWatchList.onError();

                })
                .subscribe((user, throwable) -> {

                    if (user != null && user.getData() != null) {
                        onWatchList.onSuccess();

                        sessionManager.saveUser(user.getData());
                    }

                })
        );
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(broadcastReceiver, Const.DOWNLOAD_RECEIVER, Context.RECEIVER_EXPORTED);
        } else {
            registerReceiver(broadcastReceiver, Const.DOWNLOAD_RECEIVER);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Apply tablet theme if device is a tablet
        if (DeviceUtils.isTablet(this)) {
            // Use the correct style name as defined in res/values-sw600dp/themes.xml
            setTheme(R.style.Theme_Vuga_Tablet);
        }

        setStatusBarTransparentFlag();
        sessionManager = new SessionManager(this);
        disposable = new CompositeDisposable();

        downloadServiceIntent = new Intent(this, DownloadService.class);
        bindService(downloadServiceIntent, downloadConnection, BIND_AUTO_CREATE);
    }

    public void loginRevenueCat() {
        Purchases.getSharedInstance().logIn(sessionManager.getUser().getIdentity(), new LogInCallback() {
            @Override
            public void onReceived(@NotNull CustomerInfo customerInfo, boolean created) {
                Log.i("TAG", "onlogin onReceived: " + customerInfo);

                if (customerInfo.getLatestExpirationDate() != null && new Date().before(customerInfo.getLatestExpirationDate())) {

                    sessionManager.saveBooleanValue(Const.DataKey.IS_PREMIUM, true);

                } else {
                    sessionManager.saveBooleanValue(Const.DataKey.IS_PREMIUM, false);

                }


            }

            @Override
            public void onError(@NotNull PurchasesError error) {
                Log.i("TAG", "onlogin onReceived onError: " + error.getMessage());
                sessionManager.saveBooleanValue(Const.DataKey.IS_PREMIUM, false);

            }
        });

    }

    public void setBlur(BlurView blurView, ViewGroup rootView, float v) {

        final Drawable windowBackground = getWindow().getDecorView().getBackground();
        BlurAlgorithm algorithm = getBlurAlgorithm();
        blurView.setBlurEnabled(true);
        blurView.setupWith(rootView, algorithm)
                .setFrameClearDrawable(windowBackground)
                .setBlurRadius(v);
    }

    public interface OnWatchList {

        void onTerminate();

        void onError();

        void onSuccess();
    }

    @NonNull
    private BlurAlgorithm getBlurAlgorithm() {
        BlurAlgorithm algorithm;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            algorithm = new RenderEffectBlur();
        } else {
            algorithm = new RenderScriptBlur(this);
        }
        return algorithm;
    }

    protected void setStatusBarTransparentFlag() {

        View decorView = getWindow().getDecorView();
        decorView.setOnApplyWindowInsetsListener((v, insets) -> {
            WindowInsets defaultInsets = v.onApplyWindowInsets(insets);
            return defaultInsets.replaceSystemWindowInsets(
                    defaultInsets.getSystemWindowInsetLeft(),
                    0,
                    defaultInsets.getSystemWindowInsetRight(),
                    defaultInsets.getSystemWindowInsetBottom());
        });
        ViewCompat.requestApplyInsets(decorView);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, android.R.color.transparent));
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(downloadConnection);
        try {
            unregisterReceiver(broadcastReceiver);

        } catch (Exception e) {

        }
    }
}
