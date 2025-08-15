package com.retry.vuga.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.retry.vuga.R;
import com.retry.vuga.adapters.SubscriptionPlansAdapter;
import com.retry.vuga.databinding.ActivitySubscriptionsBinding;
import com.retry.vuga.model.SubscriptionModels;
import com.retry.vuga.retrofit.RetrofitClient;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import java.util.ArrayList;
import java.util.List;

public class SubscriptionsActivity extends BaseActivity {
    private static final String TAG = "SubscriptionsActivity";
    
    private ActivitySubscriptionsBinding binding;
    private CompositeDisposable disposables;
    private SubscriptionPlansAdapter basePlansAdapter;
    private SubscriptionPlansAdapter distributorPlansAdapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_subscriptions);
        
        disposables = new CompositeDisposable();
        
        initViews();
        setListeners();
        fetchSubscriptionData();
    }
    
    private void initViews() {
        // Setup RecyclerViews for base and distributor subscriptions
        basePlansAdapter = new SubscriptionPlansAdapter(new ArrayList<>(), this::onSubscriptionPlanClicked);
        binding.rvBasePlans.setLayoutManager(new LinearLayoutManager(this));
        binding.rvBasePlans.setAdapter(basePlansAdapter);
        
        distributorPlansAdapter = new SubscriptionPlansAdapter(new ArrayList<>(), this::onSubscriptionPlanClicked);
        binding.rvDistributorPlans.setLayoutManager(new LinearLayoutManager(this));
        binding.rvDistributorPlans.setAdapter(distributorPlansAdapter);
    }
    
    private void setListeners() {
        binding.btnBack.setOnClickListener(v -> onBackPressed());
        
        binding.btnMySubscriptions.setOnClickListener(v -> fetchMySubscriptions());
        
        binding.swipeRefresh.setOnRefreshListener(() -> {
            fetchSubscriptionData();
            fetchMySubscriptions();
        });
    }
    
    private void fetchSubscriptionData() {
        Log.d(TAG, "Fetching subscription plans");
        showLoading(true);
        
        disposables.add(
            RetrofitClient.getService().getSubscriptionPlans()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    response -> {
                        showLoading(false);
                        binding.swipeRefresh.setRefreshing(false);
                        
                        if (response.isStatus() && response.getData() != null) {
                            updateSubscriptionPlans(response.getData());
                        } else {
                            showError(response.getMessage() != null ? response.getMessage() : "Failed to fetch subscription plans");
                        }
                    },
                    error -> {
                        showLoading(false);
                        binding.swipeRefresh.setRefreshing(false);
                        Log.e(TAG, "Error fetching subscription plans", error);
                        showError("Network error: " + error.getMessage());
                    }
                )
        );
    }
    
    private void fetchMySubscriptions() {
        if (sessionManager.getUser() == null || sessionManager.getUser().getId() == 0) {
            showError("Please login to view your subscriptions");
            return;
        }
        
        Log.d(TAG, "Fetching user subscriptions");
        
        disposables.add(
            RetrofitClient.getService().getMySubscriptions(sessionManager.getUser().getId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    response -> {
                        if (response.isStatus() && response.getData() != null) {
                            updateMySubscriptions(response.getData());
                        } else {
                            showError(response.getMessage() != null ? response.getMessage() : "Failed to fetch your subscriptions");
                        }
                    },
                    error -> {
                        Log.e(TAG, "Error fetching user subscriptions", error);
                        showError("Network error: " + error.getMessage());
                    }
                )
        );
    }
    
    private void updateSubscriptionPlans(SubscriptionModels.SubscriptionPlanData data) {
        // Update base subscription plans
        if (data.getBase() != null && !data.getBase().isEmpty()) {
            binding.tvBasePlansTitle.setVisibility(View.VISIBLE);
            binding.rvBasePlans.setVisibility(View.VISIBLE);
            basePlansAdapter.updatePlans(data.getBase());
        } else {
            binding.tvBasePlansTitle.setVisibility(View.GONE);
            binding.rvBasePlans.setVisibility(View.GONE);
        }
        
        // Update distributor subscription plans
        if (data.getDistributors() != null && !data.getDistributors().isEmpty()) {
            binding.tvDistributorPlansTitle.setVisibility(View.VISIBLE);
            binding.rvDistributorPlans.setVisibility(View.VISIBLE);
            distributorPlansAdapter.updatePlans(data.getDistributors());
        } else {
            binding.tvDistributorPlansTitle.setVisibility(View.GONE);
            binding.rvDistributorPlans.setVisibility(View.GONE);
        }
    }
    
    private void updateMySubscriptions(SubscriptionModels.UserSubscriptionData subscriptionData) {
        // Update subscription status
        if (subscriptionData.getBaseSubscription() != null) {
            binding.tvActiveSubscription.setVisibility(View.VISIBLE);
            binding.tvActiveSubscription.setText(
                "Base Subscription: " + subscriptionData.getBaseSubscription().getStatusText()
            );
        }
        
        if (subscriptionData.getDistributorSubscriptions() != null && !subscriptionData.getDistributorSubscriptions().isEmpty()) {
            binding.tvActiveDistributors.setVisibility(View.VISIBLE);
            binding.tvActiveDistributors.setText(
                "Active Distributors: " + subscriptionData.getActiveDistributorCount()
            );
        }
        
        // Show subscription summary
        String summary = String.format(
            "Base Active: %s | Distributors: %d",
            subscriptionData.isHasActiveBase() ? "Yes" : "No",
            subscriptionData.getActiveDistributorCount()
        );
        binding.tvSubscriptionSummary.setText(summary);
        binding.tvSubscriptionSummary.setVisibility(View.VISIBLE);
    }
    
    private void onSubscriptionPlanClicked(SubscriptionModels.SubscriptionPricingModel plan) {
        // Handle subscription plan selection
        Log.d(TAG, "Subscription plan clicked: " + plan.getDisplayName());
        
        // Here you would typically:
        // 1. Show a confirmation dialog
        // 2. Integrate with payment processor (Google Play Billing, etc.)
        // 3. Process the subscription purchase
        
        Toast.makeText(this, "Selected: " + plan.getDisplayName() + " - " + plan.getFormattedPrice() + " " + plan.getIntervalText(), Toast.LENGTH_LONG).show();
        
        // For now, just show details
        showSubscriptionDetails(plan);
    }
    
    private void showSubscriptionDetails(SubscriptionModels.SubscriptionPricingModel plan) {
        String details = String.format(
            "Plan: %s\nPrice: %s %s\nType: %s\nDistributor: %s",
            plan.getDisplayName(),
            plan.getFormattedPrice(),
            plan.getIntervalText(),
            plan.getPricingType() != null ? plan.getPricingType() : "Standard",
            plan.getDistributorName() != null ? plan.getDistributorName() : "Base Plan"
        );
        
        if (plan.getDescription() != null && !plan.getDescription().isEmpty()) {
            details += "\n\nDescription: " + plan.getDescription();
        }
        
        // Show details in a dialog or navigate to a details activity
        Toast.makeText(this, details, Toast.LENGTH_LONG).show();
    }
    
    private void showLoading(boolean show) {
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }
    
    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        Log.e(TAG, "Error: " + message);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (disposables != null && !disposables.isDisposed()) {
            disposables.dispose();
        }
    }
}