package com.retry.vuga.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.retry.vuga.R;
import com.retry.vuga.adapters.AgeRatingAdapter;
import com.retry.vuga.databinding.ActivityAgeSettingsBinding;
import com.retry.vuga.model.AgeRating;
import com.retry.vuga.model.Profile;
import com.retry.vuga.retrofit.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class AgeSettingsActivity extends BaseActivity {

    private ActivityAgeSettingsBinding binding;
    private CompositeDisposable disposable;
    private Profile profile;
    private List<AgeRating> ageRatings = new ArrayList<>();
    private AgeRatingAdapter ageRatingAdapter;
    private boolean isKidsProfile = false;
    private Integer selectedAge = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_age_settings);
        disposable = new CompositeDisposable();

        // Get profile data from intent
        int profileId = getIntent().getIntExtra("profile_id", -1);
        String profileName = getIntent().getStringExtra("profile_name");
        String profileColor = getIntent().getStringExtra("profile_color");
        isKidsProfile = getIntent().getBooleanExtra("is_kids_profile", false);
        selectedAge = getIntent().hasExtra("age") ? getIntent().getIntExtra("age", 0) : null;

        // Create profile object
        profile = new Profile();
        profile.setProfileId(profileId);
        profile.setName(profileName);
        profile.setAvatarColor(profileColor);
        profile.setIsKidsProfile(isKidsProfile);
        profile.setAge(selectedAge);

        setupViews();
        setupListeners();
        loadAgeRatings();
    }

    private void setupViews() {
        // Profile info
        binding.tvProfileName.setText(profile.getName());
        binding.viewProfileColor.setBackgroundColor(Color.parseColor(profile.getAvatarColor()));
        binding.tvProfileInitial.setText(profile.getName().substring(0, 1).toUpperCase());

        // Kids profile toggle
        binding.switchKidsProfile.setChecked(isKidsProfile);
        binding.tvKidsDescription.setText("Kids profiles can only access content for ages 12 and under");

        // Age spinner setup
        setupAgeSpinner();

        // Age ratings list
        ageRatingAdapter = new AgeRatingAdapter(ageRatings, profile);
        binding.rvAgeRatings.setLayoutManager(new LinearLayoutManager(this));
        binding.rvAgeRatings.setAdapter(ageRatingAdapter);

        updateUI();
    }

    private void setupAgeSpinner() {
        List<String> ages = new ArrayList<>();
        ages.add("Not Set");
        for (int i = 1; i <= 100; i++) {
            ages.add(String.valueOf(i));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, ages);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerAge.setAdapter(adapter);

        // Set current selection
        if (selectedAge != null && selectedAge > 0) {
            binding.spinnerAge.setSelection(selectedAge);
        } else {
            binding.spinnerAge.setSelection(0);
        }
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> finish());

        binding.switchKidsProfile.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isKidsProfile = isChecked;
            profile.setIsKidsProfile(isKidsProfile);
            updateUI();
        });

        binding.spinnerAge.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    selectedAge = null;
                } else {
                    selectedAge = position;
                }
                profile.setAge(selectedAge);
                ageRatingAdapter.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        binding.btnSave.setOnClickListener(v -> saveAgeSettings());
    }

    private void updateUI() {
        if (isKidsProfile) {
            binding.layoutAgeSelection.setVisibility(View.GONE);
            binding.layoutAgeSelection.setAlpha(0.5f);
        } else {
            binding.layoutAgeSelection.setVisibility(View.VISIBLE);
            binding.layoutAgeSelection.setAlpha(1.0f);
        }
        
        ageRatingAdapter.notifyDataSetChanged();
    }

    private void loadAgeRatings() {
        binding.progressBar.setVisibility(View.VISIBLE);

        disposable.add(RetrofitClient.getService()
                .getAgeRatings()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnTerminate(() -> binding.progressBar.setVisibility(View.GONE))
                .subscribe((response, throwable) -> {
                    if (response != null && response.isStatus() && response.getAgeRatings() != null) {
                        ageRatings.clear();
                        ageRatings.addAll(response.getAgeRatings());
                        ageRatingAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "Failed to load age ratings", Toast.LENGTH_SHORT).show();
                    }
                }));
    }

    private void saveAgeSettings() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnSave.setEnabled(false);

        int userId = sessionManager.getUser().getId();
        Integer ageToSave = isKidsProfile ? null : selectedAge;

        disposable.add(RetrofitClient.getService()
                .updateAgeSettings(profile.getProfileId(), userId, ageToSave, isKidsProfile)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnTerminate(() -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnSave.setEnabled(true);
                })
                .subscribe((response, throwable) -> {
                    if (response != null && response.getStatus()) {
                        Toast.makeText(this, "Age settings updated successfully", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        String message = response != null ? response.getMessage() : "Failed to update age settings";
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                    }
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