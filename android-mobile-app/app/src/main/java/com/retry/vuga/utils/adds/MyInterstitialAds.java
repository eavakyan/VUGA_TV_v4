package com.retry.vuga.utils.adds;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.retry.vuga.utils.Const;
import com.retry.vuga.utils.SessionManager;

public class MyInterstitialAds {

    SessionManager sessionManager;
    private final Context context;
    private InterstitialAd mInterstitialAd;

    public MyInterstitialAds(Context context) {
        this.context = context;
        sessionManager = new SessionManager(context);
        if (!sessionManager.getBooleanValue(Const.DataKey.IS_PREMIUM) && sessionManager.getAppSettings().getSettings().getIsAdmobAnd() == 1) {
            initAds();
        }
    }


    private void initAds() {

        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(context, sessionManager.getAppSettings().getAds().get(0).getIntersial_id(), adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
            }

            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                super.onAdLoaded(interstitialAd);
                mInterstitialAd = interstitialAd;
            }
        });


    }


    public void showAds() {
        if (mInterstitialAd != null) {
            mInterstitialAd.show((Activity) context);
        }
    }
}

