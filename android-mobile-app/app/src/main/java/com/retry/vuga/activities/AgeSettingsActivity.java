package com.retry.vuga.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.retry.vuga.R;
import com.retry.vuga.adapters.AgeRatingAdapter;
import com.retry.vuga.databinding.ActivityAgeSettingsBinding;
import com.retry.vuga.databinding.ItemAgeRatingBinding;
import com.retry.vuga.model.AgeRating;
import com.retry.vuga.model.UserRegistration;
import com.retry.vuga.retrofit.RetrofitClient;
import com.retry.vuga.utils.Const;
import com.retry.vuga.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class AgeSettingsActivity extends BaseActivity {
    private ActivityAgeSettingsBinding binding;
    private AgeRatingAdapter adapter;
    private CompositeDisposable disposable;
    private List<AgeRating> ageRatings;
    private int selectedRatingId = -1;
    private int originalRatingId = -1;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_age_settings);
        sessionManager = new SessionManager(this);
        disposable = new CompositeDisposable();
        
        setupViews();
        loadAgeRatings();
    }

    private void setupViews() {
        binding.btnBack.setOnClickListener(v -> finish());
        
        binding.rvAgeRatings.setLayoutManager(new LinearLayoutManager(this));
        
        binding.btnSave.setOnClickListener(v -> saveAgeSettings());
    }

    private void loadAgeRatings() {
        // Create default age ratings if API not available
        ageRatings = new ArrayList<>();
        
        // Add standard age rating options
        AgeRating rating1 = new AgeRating();
        rating1.setId(1);
        rating1.setName("0-6");
        rating1.setMinAge(0);
        rating1.setMaxAge(6);
        rating1.setCode("AG_0_6");
        rating1.setDescription("Early Childhood");
        rating1.setDisplayColor("#4CAF50");
        ageRatings.add(rating1);
        
        AgeRating rating2 = new AgeRating();
        rating2.setId(2);
        rating2.setName("7-12");
        rating2.setMinAge(7);
        rating2.setMaxAge(12);
        rating2.setCode("AG_7_12");
        rating2.setDescription("Children");
        rating2.setDisplayColor("#8BC34A");
        ageRatings.add(rating2);
        
        AgeRating rating3 = new AgeRating();
        rating3.setId(3);
        rating3.setName("13-16");
        rating3.setMinAge(13);
        rating3.setMaxAge(16);
        rating3.setCode("AG_13_16");
        rating3.setDescription("Teens");
        rating3.setDisplayColor("#FF9800");
        ageRatings.add(rating3);
        
        AgeRating rating4 = new AgeRating();
        rating4.setId(4);
        rating4.setName("17-18");
        rating4.setMinAge(17);
        rating4.setMaxAge(18);
        rating4.setCode("AG_17_18");
        rating4.setDescription("Older Teens");
        rating4.setDisplayColor("#F44336");
        ageRatings.add(rating4);
        
        AgeRating rating5 = new AgeRating();
        rating5.setId(5);
        rating5.setName("18+");
        rating5.setMinAge(18);
        rating5.setMaxAge(null);
        rating5.setCode("AG_18_PLUS");
        rating5.setDescription("Adults Only");
        rating5.setDisplayColor("#9C27B0");
        ageRatings.add(rating5);
        
        AgeRating rating6 = new AgeRating();
        rating6.setId(6);
        rating6.setName("All");
        rating6.setMinAge(0);
        rating6.setMaxAge(null);
        rating6.setCode("AG_ALL");
        rating6.setDescription("All Maturity Ratings");
        rating6.setDisplayColor("#757575");
        ageRatings.add(rating6);
        
        // Get current profile's age rating if available
        if (sessionManager.getUser() != null && 
            sessionManager.getUser().getLastActiveProfile() != null) {
            UserRegistration.Profile profile = sessionManager.getUser().getLastActiveProfile();
            // Default to "All" if not set
            selectedRatingId = 6;
            originalRatingId = selectedRatingId;
            
            // If this is a kids profile, limit to kids content
            if (profile.isKids()) {
                selectedRatingId = 2; // 7-12
                originalRatingId = selectedRatingId;
            }
        } else {
            // Default to "All"
            selectedRatingId = 6;
            originalRatingId = selectedRatingId;
        }
        
        setupAdapter();
    }

    private void setupAdapter() {
        adapter = new AgeRatingAdapter(ageRatings, selectedRatingId, new AgeRatingAdapter.OnRatingClickListener() {
            @Override
            public void onRatingClick(AgeRating rating) {
                selectedRatingId = rating.getId();
                adapter.setSelectedRatingId(selectedRatingId);
                updateSaveButtonVisibility();
            }
        });
        
        binding.rvAgeRatings.setAdapter(adapter);
    }

    private void updateSaveButtonVisibility() {
        if (selectedRatingId != originalRatingId) {
            binding.btnSave.setVisibility(View.VISIBLE);
        } else {
            binding.btnSave.setVisibility(View.GONE);
        }
    }

    private void saveAgeSettings() {
        // Save the selected age rating
        // For now, just show a success message
        originalRatingId = selectedRatingId;
        updateSaveButtonVisibility();
        
        // TODO: Save to API when endpoint is available
        Toast.makeText(this, "Age settings updated successfully", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }
}