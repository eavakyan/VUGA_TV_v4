package com.retry.vuga.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.retry.vuga.R;
import com.retry.vuga.databinding.ActivityProBinding;
import com.retry.vuga.utils.Const;
import com.revenuecat.purchases.CustomerInfo;
import com.revenuecat.purchases.Offerings;
import com.revenuecat.purchases.Package;
import com.revenuecat.purchases.PackageType;
import com.revenuecat.purchases.PurchaseParams;
import com.revenuecat.purchases.Purchases;
import com.revenuecat.purchases.PurchasesError;
import com.revenuecat.purchases.interfaces.PurchaseCallback;
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback;
import com.revenuecat.purchases.interfaces.ReceiveOfferingsCallback;
import com.revenuecat.purchases.models.StoreTransaction;

import java.util.Date;
import java.util.List;



public class ProActivity extends BaseActivity {
    ActivityProBinding binding;
    public Package packageMonthly;
    public Package packageYearly;
    Package selectedPackage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_pro);
        initListeners();
        initData();

    }


    private void proceedAfterPurchase(CustomerInfo customerInfo) {

        Log.i("TAG", "proceedAfterPurchase onReceived: " + customerInfo);

        if (customerInfo.getLatestExpirationDate() != null && new Date().before(customerInfo.getLatestExpirationDate())) {

            sessionManager.saveBooleanValue(Const.DataKey.IS_PREMIUM, true);
            startActivity(new Intent(ProActivity.this, SplashActivity.class));
            finishAffinity();
        } else {
            sessionManager.saveBooleanValue(Const.DataKey.IS_PREMIUM, false);

        }


    }

    private void fetchSubscriptionDetails() {

        Purchases.getSharedInstance().getOfferings(new ReceiveOfferingsCallback() {
            @Override
            public void onReceived(@NonNull Offerings offerings) {
                if (offerings.getCurrent() != null) {
                    List<Package> availablePackages = offerings.getCurrent().getAvailablePackages();
                    Package aPackage = availablePackages.get(0);

                    Log.i("TAG", "onReceived: " + aPackage);
                    Log.i("TAG", "onReceived: " + aPackage.getProduct().getTitle());
                    Log.i("TAG", "onReceived: " + aPackage.getProduct().getPurchasingData().getProductId());
                    Log.i("TAG", "onReceived: " + aPackage.getProduct().getPurchasingData().getProductType());
                    Log.i("TAG", "onReceived: " + aPackage.getProduct().getPrice());
                    Log.i("TAG", "onReceived: " + aPackage.getProduct().getPrice().getFormatted());


                    for (int i = 0; i < availablePackages.size(); i++) {

                        if (availablePackages.get(i).getPackageType() == PackageType.MONTHLY) {
                            packageMonthly = availablePackages.get(i);
                        }
                        if (availablePackages.get(i).getPackageType() == PackageType.ANNUAL) {
                            packageYearly = availablePackages.get(i);
                            selectedPackage = packageYearly;

                        }
                    }


                    if (packageMonthly != null) {
                        binding.tvMonthPrice.setText(packageMonthly.getProduct().getPrice().getFormatted());
                    }
                    if (packageYearly != null) {
                        binding.tvYearPrice.setText(packageYearly.getProduct().getPrice().getFormatted());
                    }

//                            Package(identifier=$rc_monthly, packageType=MONTHLY,
//                            product=GoogleStoreProduct(productId=com.retrytech.lifesound.monthly, basePlanId=monthly-base,type=SUBS,
//                            price=Price(formatted=₹490.00, amountMicros=490000000, currencyCode=INR), title=Monthly
                }
            }

            @Override
            public void onError(@NonNull PurchasesError error) {
                Log.i("TAG", "onError: " + error);
                //There's a problem with your configuration. None of the products registered in the RevenueCat dashboard could be fetched from the Play Store.
                //More information: https://rev.cat/why-are-offerings-empty
            }
        });


    }

    private void initData() {

        fetchSubscriptionDetails();


        binding.cardYearly.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.app_color));
        binding.cardMonthly.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.et_stroke_color));



    }


    private void initListeners() {
        binding.loutProgress.setOnClickListener(v -> {

        });

        binding.btnRestore.setOnClickListener(v -> {

            binding.loutProgress.setVisibility(View.VISIBLE);
            Purchases.getSharedInstance().restorePurchases(new ReceiveCustomerInfoCallback() {
                @Override
                public void onReceived(@NonNull CustomerInfo customerInfo) {
                    binding.loutProgress.setVisibility(View.GONE);

                    proceedAfterPurchase(customerInfo);

                }

                @Override
                public void onError(@NonNull PurchasesError purchasesError) {
                    binding.loutProgress.setVisibility(View.GONE);

                    Log.i("TAG", "onError: restore ");

                }
            });

        });

        binding.btnSubscribe.setOnClickListener(v -> {
            if (selectedPackage != null) {
                Purchases.getSharedInstance().purchase(
                        new PurchaseParams.Builder(this, selectedPackage).build(),
                        new PurchaseCallback() {
                            @Override
                            public void onCompleted(@NonNull StoreTransaction storeTransaction, @NonNull CustomerInfo customerInfo) {


                                proceedAfterPurchase(customerInfo);


                            }

                            @Override
                            public void onError(@NonNull PurchasesError purchasesError, boolean b) {
                                // No purchase
                            }
                        }
                );
            }

        });

        binding.cardYearly.setOnClickListener(view -> {
            selectedPackage = packageYearly;

            binding.cardYearly.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.app_color));
            binding.cardMonthly.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.et_stroke_color));
        });

        binding.cardMonthly.setOnClickListener(view -> {
            selectedPackage = packageMonthly;
            binding.cardMonthly.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.app_color));
            binding.cardYearly.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.et_stroke_color));

        });

        binding.imgBack.setOnClickListener(view -> onBackPressed());
    }


}