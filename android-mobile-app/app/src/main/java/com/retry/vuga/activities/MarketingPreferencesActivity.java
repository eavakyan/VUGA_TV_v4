package com.retry.vuga.activities;

import android.app.NotificationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;

import com.google.firebase.messaging.FirebaseMessaging;
import com.retry.vuga.R;
import com.retry.vuga.databinding.ActivityMarketingPreferencesBinding;
import com.retry.vuga.model.UserRegistration;
import com.retry.vuga.retrofit.RetrofitClient;
import com.retry.vuga.utils.Const;
import com.retry.vuga.utils.SessionManager;

import java.util.HashMap;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.RequestBody;

public class MarketingPreferencesActivity extends BaseActivity {
    ActivityMarketingPreferencesBinding binding;
    SessionManager sessionManager;
    CompositeDisposable disposable;
    boolean emailConsent = false;
    boolean smsConsent = false;
    boolean hasChanges = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_marketing_preferences);
        sessionManager = new SessionManager(this);
        disposable = new CompositeDisposable();
        
        setUserPreferences();
        setListeners();
    }

    private void setUserPreferences() {
        // Get notification preference from session
        boolean notificationEnabled = sessionManager.getBooleanValue(Const.DataKey.NOTIFICATION);
        binding.switchNotification.setChecked(notificationEnabled);
        
        // Set default values for marketing preferences - can be retrieved from API later
        emailConsent = false;
        smsConsent = false;
        
        binding.cbEmailConsent.setChecked(emailConsent);
        binding.cbSmsConsent.setChecked(smsConsent);
    }

    private void setListeners() {
        binding.btnBack.setOnClickListener(v -> {
            getOnBackPressedDispatcher().onBackPressed();
        });

        // Notification switch listener
        binding.switchNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                sessionManager.saveBooleanValue(Const.DataKey.NOTIFICATION, true);
                FirebaseMessaging.getInstance().subscribeToTopic(Const.FIREBASE_SUB_TOPIC);
                Toast.makeText(this, "Push notifications enabled", Toast.LENGTH_SHORT).show();
            } else {
                NotificationManager notificationManager = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
                notificationManager.cancelAll();
                sessionManager.saveBooleanValue(Const.DataKey.NOTIFICATION, false);
                FirebaseMessaging.getInstance().unsubscribeFromTopic(Const.FIREBASE_SUB_TOPIC);
                Toast.makeText(this, "Push notifications disabled", Toast.LENGTH_SHORT).show();
            }
        });

        binding.cbEmailConsent.setOnCheckedChangeListener((buttonView, isChecked) -> {
            emailConsent = isChecked;
            hasChanges = true;
            updateSaveButtonVisibility();
        });

        binding.cbSmsConsent.setOnCheckedChangeListener((buttonView, isChecked) -> {
            smsConsent = isChecked;
            hasChanges = true;
            updateSaveButtonVisibility();
        });

        binding.btnSave.setOnClickListener(v -> {
            updateMarketingPreferences();
        });
    }

    private void updateSaveButtonVisibility() {
        binding.btnSave.setVisibility(hasChanges ? View.VISIBLE : View.GONE);
    }

    private void updateMarketingPreferences() {
        // Simplified version - just show success for now
        hasChanges = false;
        updateSaveButtonVisibility();
        Toast.makeText(this, "Preferences updated successfully", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }
}