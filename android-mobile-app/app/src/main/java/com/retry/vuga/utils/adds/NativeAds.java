package com.retry.vuga.utils.adds;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.retry.vuga.R;
import com.retry.vuga.utils.Const;
import com.retry.vuga.utils.SessionManager;


public class NativeAds extends LinearLayout {
    CustomNativeAds customNativeAds;
    SessionManager sessionManager;
    private Context context;

    public NativeAds(Context context) {
        super(context);
        this.context = context;
        sessionManager = new SessionManager(context);
    }

    public NativeAds(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        sessionManager = new SessionManager(context);
        this.context = context;
        showAds(attrs);
    }

    public NativeAds(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        sessionManager = new SessionManager(context);
        showAds(attrs);
    }

    public NativeAds(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.context = context;
        sessionManager = new SessionManager(context);
        showAds(attrs);
    }

    private void showAds(AttributeSet attrs) {

        if (!sessionManager.getBooleanValue(Const.DataKey.IS_PREMIUM) && sessionManager.getAppSettings().getSettings().getIsAdmobAnd() == 1) {
            Log.i("TAG", "showAds: ");

            TypedArray a = getContext().getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.Ads,
                    0, 0);
            String type = a.getString(R.styleable.Ads_type);
            switch (type) {
                case "big":
                    customNativeAds = new CustomNativeAds(context, this, R.layout.admob_native_big);
                    break;
                case "small":
                    customNativeAds = new CustomNativeAds(context, this, R.layout.admob_native_small);
                    break;
                case "large":
                    customNativeAds = new CustomNativeAds(context, this, R.layout.admob_native_large);
                    break;
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (customNativeAds != null) {
            customNativeAds.isEnabledAds = false;
        }
    }
}
