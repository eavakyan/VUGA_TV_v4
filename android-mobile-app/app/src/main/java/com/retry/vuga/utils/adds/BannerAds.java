package com.retry.vuga.utils.adds;

import static com.google.android.gms.ads.AdRequest.Builder;
import static com.google.android.gms.ads.AdSize.BANNER;

import android.content.Context;
import android.util.Log;
import android.widget.FrameLayout;

import androidx.annotation.Keep;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.retry.vuga.utils.Const;
import com.retry.vuga.utils.SessionManager;


@Keep
public class BannerAds {
    SessionManager sessionManager;
    private Context context;
    private FrameLayout adsContainer;

    public BannerAds(Context context, FrameLayout adsContainer) {
        this.context = context;
        this.adsContainer = adsContainer;
        if (context != null) {
            sessionManager = new SessionManager(context);
            if (!sessionManager.getBooleanValue(Const.DataKey.IS_PREMIUM) && sessionManager.getAppSettings().getSettings().getIsAdmobAnd() == 1) {
                initAds();
            }
        }
    }

    private void initAds() {

        AdView adView = new AdView(context);
        adView.setAdSize(BANNER);
        if (sessionManager.getAppSettings().getAds().isEmpty())
            return;

        if (sessionManager.getAppSettings().getAds().get(0).getBanner_id() == null)
            return;
        adView.setAdUnitId(sessionManager.getAppSettings().getAds().get(0).getBanner_id());
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                Log.d("TAG", "onAdFailedToLoad: " + loadAdError.toString());
            }


            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                if (adsContainer != null) {
                    adsContainer.removeAllViews();
                    adsContainer.addView(adView);
                }
                Log.d("TAG", "onAdLoaded: ad loaded ");
            }
        });
        adView.loadAd(new Builder().build());
        if (adsContainer != null) {
            adsContainer.removeAllViews();
            adsContainer.addView(adView);
        }


    }


}
