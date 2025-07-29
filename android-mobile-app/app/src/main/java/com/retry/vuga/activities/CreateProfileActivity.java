package com.retry.vuga.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;

import com.retry.vuga.R;
import com.retry.vuga.adapters.AvatarColorAdapter;
import com.retry.vuga.databinding.ActivityCreateProfileBinding;
import com.retry.vuga.retrofit.RetrofitClient;

import java.util.Arrays;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class CreateProfileActivity extends BaseActivity implements AvatarColorAdapter.OnColorSelectedListener {

    private ActivityCreateProfileBinding binding;
    private CompositeDisposable disposable;
    private String selectedColor = "#FF5252";
    private boolean isKidsProfile = false;
    private int profileId = -1;
    private boolean isEditMode = false;

    private List<String> avatarColors = Arrays.asList(
            "#FF5252", "#E91E63", "#9C27B0", "#673AB7",
            "#3F51B5", "#2196F3", "#03A9F4", "#00BCD4",
            "#009688", "#4CAF50", "#8BC34A", "#CDDC39",
            "#FFEB3B", "#FFC107", "#FF9800", "#FF5722"
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_profile);
        disposable = new CompositeDisposable();

        // Check if editing existing profile
        if (getIntent().hasExtra("profile_id")) {
            isEditMode = true;
            profileId = getIntent().getIntExtra("profile_id", -1);
            String profileName = getIntent().getStringExtra("profile_name");
            selectedColor = getIntent().getStringExtra("profile_color");
            isKidsProfile = getIntent().getBooleanExtra("is_kids", false);

            binding.etProfileName.setText(profileName);
            binding.switchKidsProfile.setChecked(isKidsProfile);
            binding.tvTitle.setText("Edit Profile");
            binding.btnCreate.setText("Update Profile");
        }

        setupViews();
        setupListeners();
    }

    private void setupViews() {
        // Setup color adapter
        AvatarColorAdapter colorAdapter = new AvatarColorAdapter(this, avatarColors, selectedColor, this);
        binding.rvColors.setLayoutManager(new GridLayoutManager(this, 4));
        binding.rvColors.setAdapter(colorAdapter);

        // Update preview
        updatePreview();
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> finish());

        binding.switchKidsProfile.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isKidsProfile = isChecked;
            updatePreview();
        });

        binding.btnCreate.setOnClickListener(v -> {
            String profileName = binding.etProfileName.getText().toString().trim();
            if (profileName.isEmpty()) {
                Toast.makeText(this, "Please enter profile name", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isEditMode) {
                updateProfile(profileName);
            } else {
                createProfile(profileName);
            }
        });
    }

    private void createProfile(String profileName) {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnCreate.setEnabled(false);

        int userId = sessionManager.getUser().getId();

        disposable.add(RetrofitClient.getService()
                .createProfile(userId, profileName, "color", "", selectedColor, isKidsProfile ? 1 : 0)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnTerminate(() -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnCreate.setEnabled(true);
                })
                .subscribe((response, throwable) -> {
                    if (response != null && response.isStatus()) {
                        Toast.makeText(this, "Profile created successfully", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        String message = response != null ? response.getMessage() : "Failed to create profile";
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                    }
                }));
    }

    private void updateProfile(String profileName) {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnCreate.setEnabled(false);

        disposable.add(RetrofitClient.getService()
                .updateProfile(profileId, profileName, "color", "", selectedColor, isKidsProfile ? 1 : 0)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnTerminate(() -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnCreate.setEnabled(true);
                })
                .subscribe((response, throwable) -> {
                    if (response != null && response.isStatus()) {
                        Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        String message = response != null ? response.getMessage() : "Failed to update profile";
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                    }
                }));
    }

    private void updatePreview() {
        try {
            binding.viewPreviewAvatar.setCardBackgroundColor(Color.parseColor(selectedColor));
        } catch (Exception e) {
            binding.viewPreviewAvatar.setCardBackgroundColor(Color.parseColor("#FF5252"));
        }

        String name = binding.etProfileName.getText().toString().trim();
        if (!name.isEmpty()) {
            binding.tvPreviewInitial.setText(name.substring(0, 1).toUpperCase());
        } else {
            binding.tvPreviewInitial.setText("P");
        }
    }

    @Override
    public void onColorSelected(String color) {
        selectedColor = color;
        updatePreview();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }
}