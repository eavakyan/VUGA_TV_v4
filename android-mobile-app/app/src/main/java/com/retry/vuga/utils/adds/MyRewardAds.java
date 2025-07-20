package com.retry.vuga.utils.adds;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.facebook.ads.InterstitialAd;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.retry.vuga.utils.SessionManager;

public class MyRewardAds {

    private static final String TAG = "rewardads";
    RewardAdListnear rewardAdListnear;
    SessionManager sessionManager;
    private RewardedAd rewardedAd;
    private Context context;
    private InterstitialAd interstitialAdfb;

    public MyRewardAds(Context context) {
        this.context = context;
        sessionManager = new SessionManager(context);

        initGoogle();
    }

    public RewardAdListnear getRewardAdListnear() {
        return rewardAdListnear;
    }

    public void setRewardAdListnear(RewardAdListnear rewardAdListnear) {
        this.rewardAdListnear = rewardAdListnear;
    }

    private void initGoogle() {
        if (sessionManager.getAppSettings().getAds().isEmpty())
            return;

        if (sessionManager.getAppSettings().getAds().get(0).getRewarded_id() == null)
            return;

        RewardedAd.load(context, sessionManager.getAppSettings().getAds().get(0).getRewarded_id(), new AdRequest.Builder().build(), new RewardedAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                super.onAdLoaded(rewardedAd);
                MyRewardAds.this.rewardedAd = rewardedAd;
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                Log.d(TAG, "onRewardedAdFailedToLoad: " + loadAdError);
//                initFacebook();
            }

        });

    }

//    private void initFacebook() {
//        interstitialAdfb = new InterstitialAd(context, sessionManager.getFBInt());
//        interstitialAdfb.loadAd(
//                interstitialAdfb.buildLoadAdConfig()
//
//                        .build());
//
//    }


    public void showAd() {
        if (rewardedAd != null) {
            Activity activityContext = (Activity) context;
            rewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                    super.onAdFailedToShowFullScreenContent(adError);
                }

                @Override
                public void onAdShowedFullScreenContent() {
                    super.onAdShowedFullScreenContent();
                }

                @Override
                public void onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent();
                    rewardAdListnear.onAdClosed();
                }
            });
            rewardedAd.show(activityContext, new OnUserEarnedRewardListener() {
                @Override
                public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                    rewardAdListnear.onEarned();

                }


            });


        }
//        else if (interstitialAdfb != null && interstitialAdfb.isAdLoaded()) {
//            interstitialAdfb.show();
//            rewardAdListnear.onEarned();
//
//        }

    }


    public interface RewardAdListnear {
        void onAdClosed();

        void onEarned();
    }
}
