package com.retry.vuga.utils.adds;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class MyBannerAdView extends FrameLayout {
    public MyBannerAdView(@NonNull Context context) {
        super(context);
        initAds();
    }

    public MyBannerAdView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initAds();
    }

    public MyBannerAdView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAds();
    }

    public MyBannerAdView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initAds();
    }

    private void initAds() {
        new BannerAds(getContext(), this);
    }
}
