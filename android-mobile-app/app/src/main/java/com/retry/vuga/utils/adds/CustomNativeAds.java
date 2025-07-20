package com.retry.vuga.utils.adds;

import static android.content.ContentValues.TAG;
import static com.google.android.gms.ads.formats.NativeAdOptions.ADCHOICES_TOP_RIGHT;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Keep;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.retry.vuga.R;
import com.retry.vuga.utils.Const;
import com.retry.vuga.utils.SessionManager;

@Keep
public class CustomNativeAds {
    public boolean isEnabledAds = true;
    private Context context;
    private SessionManager sessionManager;
    private LinearLayout adsContainer;
    private int admobNative;
    private int facebookNative;
    private int customNative;
    private AdLoader adLoader;
    private int index;

    public CustomNativeAds(Context context, LinearLayout adsContainer, int admobNative) {
        this.context = context;
        sessionManager = new SessionManager(context);
        this.adsContainer = adsContainer;
        this.admobNative = admobNative;
        this.facebookNative = facebookNative;
        this.customNative = customNative;
        if (sessionManager != null) {
            if (!sessionManager.getBooleanValue(Const.DataKey.IS_PREMIUM) && sessionManager.getAppSettings().getSettings().getIsAdmobAnd() == 1) {

                initAds();
            }
        }
    }


    private void initAds() {
        loadNativeAds();
    }


    private void loadNativeAds() {


        AdLoader.Builder builder = null;

        if (sessionManager.getAppSettings().getAds().isEmpty())
            return;

        if (sessionManager.getAppSettings().getAds().get(0).getAndroidAdmobNativeId() == null)
            return;

        builder = new AdLoader.Builder(context, sessionManager.getAppSettings().getAds().get(0).getAndroidAdmobNativeId());

        // A native ad loaded successfully, check if the ad loader has finished loading
        // and if so, insert the ads into the list.
        // A native ad failed to load, check if the ad loader has finished loading
        // and if so, insert the ads into the list.
        AdLoader adLoader = builder.forNativeAd(
                        unifiedNativeAd -> {
                            Log.d(TAG, "onUnifiedNativeAdLoaded: ");
                            if (isEnabledAds) {
                                showAdmobAds(unifiedNativeAd);
                            }
                            // A native ad loaded successfully, check if the ad loader has finished loading
                            // and if so, insert the ads into the list.
                        }).withAdListener(
                new AdListener() {

                    @Override
                    public void onAdFailedToLoad(LoadAdError loadAdError) {
                        super.onAdFailedToLoad(loadAdError);
                        Log.e(TAG, "The previous native ad failed to load. Attempting to"
                                + " load another." + loadAdError.getCode());

                        if (isEnabledAds) {

                        }
                    }
                })
                .withNativeAdOptions(new com.google.android.gms.ads.nativead.NativeAdOptions.Builder()
                        .setRequestCustomMuteThisAd(true)
                        .setAdChoicesPlacement(ADCHOICES_TOP_RIGHT)
                        .build()).build();


        AdRequest request = new AdRequest.Builder()
                .build();


        adLoader.loadAds(request, 1);
    }

    private void showAdmobAds(com.google.android.gms.ads.nativead.NativeAd unifiedNativeAd) {
        if (context != null) {
            View view = LayoutInflater.from(context).inflate(admobNative, null, false);
            com.google.android.gms.ads.nativead.NativeAdView adView = view.findViewById(R.id.ad_view);

            // The MediaView will display a video asset if one is present in the ad, and the
            // first image asset otherwise.
            adView.setMediaView(adView.findViewById(R.id.ad_media));

            // Register the view used for each individual asset.
            adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
            adView.setBodyView(adView.findViewById(R.id.ad_body));
            adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
            adView.setIconView(adView.findViewById(R.id.ad_app_icon));
            adView.setPriceView(adView.findViewById(R.id.ad_price));
            adView.setStarRatingView(adView.findViewById(R.id.ad_stars));
            adView.setStoreView(adView.findViewById(R.id.ad_store));
            adView.setAdvertiserView(adView.findViewById(R.id.ad_advertiser));
            adsContainer.removeAllViews();
            adsContainer.addView(view, 0);
            populateNativeAdView(unifiedNativeAd, adView);
        }
    }


    private void populateNativeAdView(com.google.android.gms.ads.nativead.NativeAd nativeAd,
                                      NativeAdView adView) {
        // Some assets are guaranteed to be in every UnifiedNativeAd.
        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());
        ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        com.google.android.gms.ads.nativead.NativeAd.Image icon = nativeAd.getIcon();

        if (icon == null) {
            adView.getIconView().setVisibility(View.INVISIBLE);
        } else if (context != null && isEnabledAds) {
            Glide.with(adView.getIconView())
                    .load(icon.getDrawable())
                    .circleCrop()
                    .into((ImageView) adView.getIconView());
            adView.getIconView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getPrice() == null) {
            adView.getPriceView().setVisibility(View.INVISIBLE);
        } else {
            adView.getPriceView().setVisibility(View.VISIBLE);
            ((TextView) adView.getPriceView()).setText(nativeAd.getPrice());
        }
//
//        if (nativeAd.getStore() == null) {
//            adView.getStoreView().setVisibility(View.INVISIBLE);
//        } else {
//            adView.getStoreView().setVisibility(View.VISIBLE);
//            ((TextView) adView.getStoreView()).setText(nativeAd.getStore());
//        }
//
//        if (nativeAd.getStarRating() == null) {
//            adView.getStarRatingView().setVisibility(View.INVISIBLE);
//        } else {
//            ((RatingBar) adView.getStarRatingView())
//                    .setRating(nativeAd.getStarRating().floatValue());
//            adView.getStarRatingView().setVisibility(View.VISIBLE);
//        }

        if (nativeAd.getAdvertiser() == null) {
            adView.getAdvertiserView().setVisibility(View.INVISIBLE);
        } else {
            ((TextView) adView.getAdvertiserView()).setText(nativeAd.getAdvertiser());
            adView.getAdvertiserView().setVisibility(View.VISIBLE);
        }

        // Assign native ad object to the native view.
        adView.setNativeAd(nativeAd);

    }


}
