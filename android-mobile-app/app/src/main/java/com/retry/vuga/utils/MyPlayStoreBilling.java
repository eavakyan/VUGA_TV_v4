package com.retry.vuga.utils;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.ProductDetailsResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;
import com.google.common.collect.ImmutableList;

import java.util.List;

/**
 * Created by jeelkhokhariya
 * on 04/08/21
 */
public class MyPlayStoreBilling {

    private final BillingClient billingClient;
    private final Activity activity;
    private final OnPurchaseComplete onPurchaseComplete;
    private boolean isConsumable = false;
    private boolean isConnected = false;

    public MyPlayStoreBilling(Activity activity, OnPurchaseComplete onPurchaseComplete) {

        PurchasesUpdatedListener purchasesUpdatedListener = (billingResult, purchases) -> {
            // To be implemented in a later section.
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK
                    && purchases != null) {
                for (Purchase purchase : purchases) {
                    Log.d("TAG", "onPurchasesUpdated: " + purchase);
                    handlePurchase(purchase);
                }
            } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
                // Handle an error caused by a user cancelling the purchase flow.
                Log.d("TAG", "USER_CANCELED: ");
                onPurchaseComplete.onPurchaseResult(false);
            } else {
                // Handle any other error codes.
                onPurchaseComplete.onPurchaseResult(false);
                Log.d("TAG", "Error: ");
            }
        };
        billingClient = BillingClient.newBuilder(activity)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build();
        this.activity = activity;
        this.onPurchaseComplete = onPurchaseComplete;
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    isConnected = true;
                    onPurchaseComplete.onConnected(true);
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                isConnected = false;
            }
        });
    }

    void handlePurchase(Purchase purchase) {
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged()) {
                AcknowledgePurchaseParams acknowledgePurchaseParams =
                        AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(purchase.getPurchaseToken())
                                .build();
                billingClient.acknowledgePurchase(acknowledgePurchaseParams,
                        billingResult -> Log.d("TAG", "acknowledgePurchase: " + billingResult.getDebugMessage()));
                if (isConsumable) {
                    consumePurchase(purchase);
                }
            }
            onPurchaseComplete.onPurchaseResult(true);
        }
    }

    void consumePurchase(Purchase purchase) {
        ConsumeParams consumeParams =
                ConsumeParams.newBuilder()
                        .setPurchaseToken(purchase.getPurchaseToken())
                        .build();

        ConsumeResponseListener listener = (billingResult, purchaseToken) -> {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                Log.d("TAG", "consumePurchase: OK");
            }
        };

        billingClient.consumeAsync(consumeParams, listener);
    }

    public void startPurchase(String productId, String productType, boolean isConsumable) {
        if (isConnected) {
            this.isConsumable = isConsumable;
            QueryProductDetailsParams queryProductDetailsParams =
                    QueryProductDetailsParams.newBuilder()
                            .setProductList(
                                    ImmutableList.of(
                                            QueryProductDetailsParams.Product.newBuilder()
                                                    .setProductId(productId)
                                                    .setProductType(productType)
                                                    .build()))
                            .build();


            billingClient.queryProductDetailsAsync(queryProductDetailsParams, new ProductDetailsResponseListener() {
                @Override
                public void onProductDetailsResponse(@NonNull BillingResult billingResult, @NonNull List<ProductDetails> list) {
                    Log.d("TAG", "startPurchase: " + list.size());
                    if (list.isEmpty()) {
//                        onPurchaseComplete.onError(true);


                    } else {


                        String offerToken = null;

                        if (productType.equals(BillingClient.ProductType.SUBS)) {

                            if (list.get(0).getSubscriptionOfferDetails() != null) {
                                offerToken = list.get(0)
                                        .getSubscriptionOfferDetails().get(0).getOfferToken();
                            }
                        }


                        ImmutableList productDetailsParamsList;
                        if (offerToken != null) {
                            productDetailsParamsList = ImmutableList.of(
                                    BillingFlowParams.ProductDetailsParams.newBuilder()
                                            .setProductDetails(list.get(0))
                                            .setOfferToken(offerToken)
                                            .build()
                            );
                        } else {
                            productDetailsParamsList = ImmutableList.of(
                                    BillingFlowParams.ProductDetailsParams.newBuilder()
                                            .setProductDetails(list.get(0))
                                            .build()
                            );
                        }

                        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                                .setProductDetailsParamsList(productDetailsParamsList)
                                .build();

                        billingClient.launchBillingFlow(activity, billingFlowParams);

                    }
                }
            });
        }
    }

    public boolean isSubscriptionRunning() {
        final boolean[] retur = {false};
        billingClient.queryPurchasesAsync(QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build(), new PurchasesResponseListener() {
            @Override
            public void onQueryPurchasesResponse(@NonNull BillingResult billingResult, @NonNull List<Purchase> list) {
                retur[0] = !list.isEmpty();
            }
        });
        return retur[0];
    }

    public void onDestroy() {
        if (isConnected)
            billingClient.endConnection();
    }

    public interface OnPurchaseComplete {

        void onConnected(boolean isConnect);

        void onPurchaseResult(boolean isPurchaseSuccess);
    }
}
