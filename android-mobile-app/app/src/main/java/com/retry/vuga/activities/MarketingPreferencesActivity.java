package com.retry.vuga.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;

import com.retry.vuga.R;
import com.retry.vuga.databinding.ActivityMarketingPreferencesBinding;
import com.retry.vuga.model.User;
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
        User user = sessionManager.getUser();
        if (user != null) {
            emailConsent = user.isEmailConsent();
            smsConsent = user.isSmsConsent();
            
            binding.cbEmailConsent.setChecked(emailConsent);
            binding.cbSmsConsent.setChecked(smsConsent);
        }
    }

    private void setListeners() {
        binding.btnBack.setOnClickListener(v -> {
            getOnBackPressedDispatcher().onBackPressed();
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
        binding.loader.setVisibility(View.VISIBLE);
        
        HashMap<String, RequestBody> params = new HashMap<>();
        params.put(Const.ApiKey.user_id, RequestBody.create(
                String.valueOf(sessionManager.getUserId()), MediaType.parse("text/plain")));
        params.put(Const.ApiKey.email_consent, RequestBody.create(
                String.valueOf(emailConsent ? 1 : 0), MediaType.parse("text/plain")));
        params.put(Const.ApiKey.sms_consent, RequestBody.create(
                String.valueOf(smsConsent ? 1 : 0), MediaType.parse("text/plain")));

        disposable.add(RetrofitClient.getRetrofitInstance()
                .getApiService()
                .updateProfile(params)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    binding.loader.setVisibility(View.GONE);
                    
                    if (response.getData() != null) {
                        // Update the stored user with new consent values
                        sessionManager.saveUser(response.getData());
                        hasChanges = false;
                        updateSaveButtonVisibility();
                        Toast.makeText(this, "Preferences updated successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, response.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }, throwable -> {
                    binding.loader.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to update preferences", Toast.LENGTH_SHORT).show();
                }));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }
}