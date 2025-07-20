package com.retry.vuga.activities;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.databinding.DataBindingUtil;

import com.retry.vuga.R;
import com.retry.vuga.adapters.LanguagesAdapter;
import com.retry.vuga.databinding.ActivityLanguageBinding;
import com.retry.vuga.model.Language;
import com.retry.vuga.utils.Global;
import com.retry.vuga.utils.SessionManager;

import java.util.Objects;

public class LanguageActivity extends AppCompatActivity {
    ActivityLanguageBinding binding;
    LanguagesAdapter languagesAdapter;
    SessionManager sessionManager;
    Language model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        binding = DataBindingUtil.setContentView(this, R.layout.activity_language);
        sessionManager = new SessionManager(this);

        languagesAdapter = new LanguagesAdapter();
        binding.rvLang.setAdapter(languagesAdapter);
        languagesAdapter.updateItems(Global.getLanguages());
        languagesAdapter.onClick = new LanguagesAdapter.OnClick() {
            @Override
            public void onSelect(Language model) {
                binding.setEnableDone(!Objects.equals(model.getId(), sessionManager.getLanguage()));
                LanguageActivity.this.model = model;
            }
        };

        binding.imgBack.setOnClickListener(v -> {
            getOnBackPressedDispatcher().onBackPressed();
        });

        binding.btnDone.setOnClickListener(v -> {


            if (binding.getEnableDone() != null && binding.getEnableDone()) {
                if (model != null) {
                    sessionManager.saveLanguage(model.getId());
                }
                startActivity(new Intent(this, MainActivity.class));
                this.finishAffinity();
            }


        });


    }
}