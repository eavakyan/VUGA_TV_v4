package com.retry.vuga.utils;

import android.app.Activity;
import android.app.Application;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.downloader.PRDownloader;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.retry.vuga.R;
import com.revenuecat.purchases.LogLevel;
import com.revenuecat.purchases.Purchases;
import com.revenuecat.purchases.PurchasesConfiguration;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

import io.branch.referral.Branch;
import com.google.android.gms.cast.framework.CastContext;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Branch.getAutoInstance(this);
        Branch.enableLogging();

        // Initialize Google Cast SDK
        try {
            CastContext.getSharedInstance(this);
        } catch (Exception e) {
            // Cast may not be available on all devices
            Log.e("MyApplication", "Cast SDK initialization failed", e);
        }

        setLanguage();
        PRDownloader.initialize(this);


        Purchases.setLogLevel(LogLevel.DEBUG);
        Purchases.configure(new PurchasesConfiguration.Builder(this, getString(R.string.revenue_cat_google_api_key)).build());

        //get firebase_device_token
        SessionManager sessionManager = new SessionManager(this);
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<String> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    Log.i("TAG", "onComplete: fomMyAppliation token : " + task.getResult());
                    sessionManager.saveFireBaseToken(task.getResult());
                }
            }
        });
        PRDownloader.initialize(getApplicationContext());
    }


    private void setLanguage() {


        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {

            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {

            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {
                // Trigger sync when activity is paused (app going to background)
                Log.d("WatchHistorySync", "Activity paused, triggering sync");
                WatchHistorySyncHelper.getInstance(MyApplication.this).forceSync();
            }

            @Override
            public void onActivityStopped(@NonNull Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {

            }
        });

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle bundle) {
                updateLanguage(activity);
            }

            @Override
            public void onActivityStarted(Activity activity) {
            }

            @Override
            public void onActivityResumed(Activity activity) {
                updateLanguage(activity);
            }

            @Override
            public void onActivityPaused(Activity activity) {
            }

            @Override
            public void onActivityStopped(Activity activity) {
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
            }
        });
    }

    private void updateLanguage(Activity activity) {
        SessionManager sessionManager = new SessionManager(activity);
        Locale locale = new Locale(sessionManager.getLanguage());
        Locale.setDefault(locale);

        Resources resources = activity.getResources();
        Configuration configuration = resources.getConfiguration();
        configuration.setLocale(locale);
        configuration.setLayoutDirection(locale);

        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
    }

    @Override
    public void onTerminate() {
        Log.d("TAG", "onTerminate: ");
        super.onTerminate();
    }
}
